package server;

import java.io.BufferedReader; //lê texto de forma eficiente usando um buffer de memória.
import java.io.DataInputStream; //lê dados binários e tipos primitivos (int, double, boolean etc.) de um fluxo de entrada.
import java.io.File;//representa um arquivo ou diretório e permite manipular suas informações.
import java.io.FileOutputStream;//grava bytes diretamente em um arquivo.
import java.io.InputStreamReader; //converte bytes de uma entrada em caracteres de texto.
import java.io.PrintWriter; //escreve texto formatado em arquivos, streams ou conexões de rede.
import java.net.Socket; //cria e gerencia uma conexão TCP entre um cliente e um servidor.

//---NOVO - IMPORT---
// importar SocketTimeoutException para tratar o timeout separadamente
import java.net.SocketTimeoutException;

public class Atendente implements Runnable {
	/**
	 * Runnable é uma interface do Java que indica que a classe possui uma tarefa que pode ser executada por
	 * uma thread (linha de execução).
     * A interface exige a implementação do método: PUBLIC VOID RUN()
     * 
     * Quando start() é chamado, a thread executa automaticamente o método run().
     * Objetivo: permitir que vários clientes sejam atendidos simultaneamente sem travar o servidor.
	 */
    private Socket socket; //Guarda a conexão com o cliente.
    private BufferedReader in; //Serve para ler mensagens enviadas pelo cliente.
    private PrintWriter out; //Serve para enviar mensagens ao cliente.
    private String username; //Armazena o nome do utilizador conectado.

    public Atendente(Socket socket) {
        this.socket = socket;
    }
//socket.getInputStream(); - permite receber e enviar mensagens.
//socket.getOutputStream(); - permite receber e enviar mensagens.
    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream())); //recebe e lê os dados enviados pelo cliente através da conexão (Socket).
            out = new PrintWriter(socket.getOutputStream(), true); //envia dados e mensagens do servidor para o cliente através da conexão (Socket).

            // pedir nome ao cliente
            out.println("Username:");
            username = in.readLine();

            // validar se nome esta vazio
            if (username == null || username.trim().isEmpty()) {
                out.println("400 nome invalido");
                socket.close();
                return;
            }
            username = username.trim(); // trim - Remove espaços em branco no início e no fim da string.

            // validar tamanho do nome
            if (username.length() < 3 || username.length() > 20) {
                out.println("400 nome deve ter entre 3 a 20 caracteres");
                socket.close();
                return;
            }

            // validar se nome ja esta em uso
            if (GerirCliente.existeCliente(username)) {
                out.println("409 nome ja utilizado");
                socket.close();
                return;
            }

            // registar cliente
            GerirCliente.addClient(username, this);
            Logger.log(username + " entrou");
            out.println("200 bem-vindo " + username);

            // loop principal de comandos
            String mensagem;
            while ((mensagem = in.readLine()) != null) {

                mensagem = mensagem.trim();
                Logger.log(username + " : " + mensagem);

                if (mensagem.equalsIgnoreCase("HELP")) {
                    tratarHelp();

                } else if (mensagem.equalsIgnoreCase("WHO")) {
                    tratarWho();

                } else if (mensagem.toUpperCase().startsWith("MSG ")) {
                    tratarMsg(mensagem);

                } else if (mensagem.toUpperCase().startsWith("PM ")) {
                    tratarPm(mensagem);

                } else if (mensagem.toUpperCase().startsWith("SEND ")) {
                    tratarSend();

                } else if (mensagem.equalsIgnoreCase("QUIT")) {
                    tratarQuit();
                    break;

                } else if (mensagem.isEmpty()) {
                    // ignorar linhas vazias

                } else {
                    out.println("400 comando invalido");
                }
            }

        //NOVO - CATCH TIMEOUT
        // este catch e novo - antes so existia catch (Exception e)
        // agora tratamos o timeout separadamente:
        // 1. avisar o cliente com "408 timeout"
        // 2. registar no log que foi por timeout e nao por erro
            
        } catch (SocketTimeoutException e) {
            out.println("408 timeout");
            Logger.log(username + " desligado por timeout");

        } catch (Exception e) {
            Logger.log("Cliente desligado inesperadamente");

        } finally {
            GerirCliente.removerCliente(username);
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }

    // HELP - mostrar comandos disponiveis
    private void tratarHelp() {
        out.println("200 comandos: WHO | MSG <texto> | PM <nick> <texto> | SEND <ficheiro> | QUIT");
    }

    // WHO - listar utilizadores ligados
    private void tratarWho() {
        String lista = GerirCliente.listarClientes();
        out.println("200 utilizadores: " + lista);
    }

    // MSG - enviar mensagem publica para todos
    private void tratarMsg(String mensagem) {
        String texto = mensagem.substring(4).trim();

        if (texto.isEmpty()) {
            out.println("400 formato: MSG <texto>");
            return;
        }

        GerirCliente.transmissao("MSG " + username + ": " + texto);
        out.println("200 mensagem enviada");
        Logger.log(username + " enviou mensagem publica: " + texto);
    }

    // PM - enviar mensagem privada para um utilizador
    private void tratarPm(String mensagem) {
        String resto = mensagem.substring(3).trim();
        int espaco = resto.indexOf(" ");

        if (espaco == -1) {
            out.println("400 formato: PM <nick> <texto>");
            return;
        }

        String destinatario = resto.substring(0, espaco);
        String texto = resto.substring(espaco + 1).trim();

        if (texto.isEmpty()) {
            out.println("400 formato: PM <nick> <texto>");
            return;
        }

        Atendente cliente = GerirCliente.getCliente(destinatario);

        if (cliente == null) {
            out.println("404 utilizador nao encontrado");
            return;
        }

        cliente.enviarMensagem("PM " + username + ": " + texto);
        out.println("200 mensagem privada enviada");
        Logger.log(username + " enviou mensagem privada para " + destinatario);
    }

    // SEND - receber ficheiro do cliente
    private void tratarSend() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            // 1. ler tamanho do nome
            int tamanhoNome = dis.readInt();

            // 2. ler nome do ficheiro
            byte[] nomeBytes = new byte[tamanhoNome];
            dis.readFully(nomeBytes);
            String nomeFicheiro = new String(nomeBytes, "UTF-8");

            // validar nome - nao pode ter .. ou / (path traversal)
            if (nomeFicheiro.contains("..") || nomeFicheiro.contains("/") || nomeFicheiro.contains("\\")) {
                out.println("400 nome invalido");
                return;
            }

            // 3. ler tamanho do ficheiro
            long tamanhoFicheiro = dis.readLong();

            // validar tamanho
            if (tamanhoFicheiro == 0) {
                out.println("400 ficheiro vazio");
                return;
            }

            long limiteBytes = 5 * 1024 * 1024; // 5 MB
            if (tamanhoFicheiro > limiteBytes) {
                out.println("400 tamanho excede limite");
                return;
            }

            // 4. criar pasta uploads se nao existir
            File pasta = new File("uploads");
            if (!pasta.exists()) {
                pasta.mkdir();
            }

            // 5. ler conteudo e guardar ficheiro
            File ficheiro = new File("uploads/" + nomeFicheiro);
            FileOutputStream fos = new FileOutputStream(ficheiro);

            byte[] buffer = new byte[4096];
            long bytesRestantes = tamanhoFicheiro;

            while (bytesRestantes > 0) {
                int aLer = (int) Math.min(buffer.length, bytesRestantes);
                int lido  = dis.read(buffer, 0, aLer);
                fos.write(buffer, 0, lido);
                bytesRestantes -= lido;
            }

            fos.close();

            out.println("200 ficheiro recebido " + nomeFicheiro + " " + tamanhoFicheiro + " bytes");
            Logger.log("Ficheiro recebido de " + username + ": " + nomeFicheiro + " (" + tamanhoFicheiro + " bytes)");

        } catch (Exception e) {
            out.println("400 erro na transferencia");
            Logger.log("Erro ao receber ficheiro de " + username + ": " + e.getMessage());
        }
    }

    // QUIT - desligar o cliente
    private void tratarQuit() {
        out.println("200 adeus");
        Logger.log(username + " saiu");
    }

    // metodo para enviar uma mensagem a este cliente
    public void enviarMensagem(String mensagem) {
        out.println(mensagem);
    }
}