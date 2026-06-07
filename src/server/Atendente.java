package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Atendente implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public Atendente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // FASE DE LOGIN
            // antes de ter nome, o cliente pode usar HELP, QUIT ou NICK
            out.println("200 Use NICK <nome> para entrar | HELP para ajuda | QUIT para sair");

            String mensagem;
            while ((mensagem = in.readLine()) != null) {
                mensagem = mensagem.trim();

                if (mensagem.equalsIgnoreCase("HELP")) {
                    out.println("200 antes do login: NICK <nome> | HELP | QUIT");
                    continue;
                }

                if (mensagem.equalsIgnoreCase("QUIT")) {
                    out.println("200 adeus");
                    socket.close();
                    return;
                }

                if (mensagem.toUpperCase().startsWith("NICK ")) {
                    String nome = mensagem.substring(5).trim();

                    // validar se nome esta vazio
                    if (nome.isEmpty()) {
                        out.println("400 nome invalido. Tente: NICK <nome>");
                        continue; // pede novamente sem fechar
                    }

                    // validar tamanho
                    if (nome.length() < 3 || nome.length() > 20) {
                        out.println("400 nome deve ter entre 3 a 20 caracteres. Tente: NICK <nome>");
                        continue; // pede novamente sem fechar
                    }

                    // validar se ja esta em uso
                    if (GerirCliente.existeCliente(nome)) {
                        out.println("409 nome ja utilizado. Tente: NICK <outro nome>");
                        continue; // pede novamente sem fechar
                    }

                    // nome aceite — regista e avanca
                    username = nome;
                    GerirCliente.addClient(username, this);
                    Logger.log(username + " entrou");
                    out.println("200 bem-vindo " + username);
                    break; // sai do loop de login
                }

                // qualquer outro comando antes do login
                out.println("400 faca login primeiro. Use NICK <nome>");
            }

            // FASE PRINCIPAL — loop de comandos apos login
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

        } catch (SocketTimeoutException e) {
            if (out != null) out.println("408 timeout");
            Logger.log((username != null ? username : "cliente") + " desligado por timeout");

        } catch (Exception e) {
            Logger.log("Cliente desligado inesperadamente");

        } finally {
            GerirCliente.removerCliente(username);
            try { socket.close(); } catch (Exception ignored) {}
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

    // MSG - enviar mensagem publica para todos EXCETO o remetente
    private void tratarMsg(String mensagem) {
        String texto = mensagem.substring(4).trim();

        if (texto.isEmpty()) {
            out.println("400 formato: MSG <texto>");
            return;
        }

        // envia para todos menos para o proprio
        GerirCliente.transmissaoExceto("MSG " + username + ": " + texto, username);

        // o remetente recebe so a confirmacao
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