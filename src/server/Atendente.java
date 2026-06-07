package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
        	in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        	out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // FASE DE LOGIN
            // antes de ter nome, o cliente pode usar HELP, QUIT ou NICK
            out.println("   Bem-vindo ao Sistema Distribuído de Comunicação ");
            out.println("------------------------------------------------------");
            out.println("");
            out.println("Comandos disponiveis:");
            out.println("NICK <nome>");
            out.println("HELP");
            out.println("QUIT");

            String mensagem;
            while ((mensagem = in.readLine()) != null) {
                mensagem = mensagem.trim();

                if (mensagem.equalsIgnoreCase("HELP")) {
                    out.println("200 comandos disponiveis antes do login:");
                    out.println("NICK <nome> - registar username");
                    out.println("HELP        - lista de comandos");
                    out.println("QUIT        - sair");
                    continue;
                }

                if (mensagem.equalsIgnoreCase("QUIT")) {
                    out.println("200 adeus");
                    try { socket.close(); } catch (Exception ignored) {}
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
                out.println("401 faca login primeiro. Use NICK <nome>");
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
              
                } else if (mensagem.toUpperCase().startsWith("NICK ")) {
                    tratarNickChange(mensagem);

                } else if (mensagem.equalsIgnoreCase("PING")) {
                    tratarPing();

                } else if (mensagem.equalsIgnoreCase("TIME")) {
                    tratarTime();
                    
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
            Logger.log("Cliente desligado inesperadamente: " + e.getMessage());
        
        } finally {
            if (username != null) {
                GerirCliente.removerCliente(username);
                Logger.log(username + " desligado"); // ← so se tinha login
            } else {
                Logger.log("cliente anonimo desligado"); // ← se saiu sem login
            }
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    // HELP - mostrar comandos disponiveis
    private void tratarHelp() {
    	out.println(" ");
    	    out.println("200 GLOSSARIO");
    	    out.println("---------------------------------------------------");
    	    out.println(" ");
    	    out.println("PROTOCOLOS:");
    	    out.println("WHO                - Lista utilizadores ativos");
    	    out.println("MSG <texto>        - Envia mensagem publica");
    	    out.println("PM <nick> <texto>  - Envia mensagem privada");
    	    out.println("SEND <ficheiro>    - Envia ficheiro para o servidor");
    	    out.println("NICK <novo_nome>   - Altera o teu username");
    	    out.println("PING               - Verifica ligacao ao servidor");
    	    out.println("TIME               - Devolve o tempo atual do servidor");
    	    out.println("QUIT               - Desconecta a ligacao");
    	    out.println(" ");
    	    out.println("CODIGOS DE RESPOSTA:");
    	    out.println("200 - Operacao realizada com sucesso");
    	    out.println("400 - Pedido invalido");
    	    out.println("401 - Cliente ainda não registado");
    	    out.println("404 - Recurso ou utilizador nao encontrado");
    	    out.println("408 - Timeout");
    	    out.println("409 - Nome ja utilizado");
    	    out.println("500 - Erro interno do servidor");
    	    out.println("---------------------------------------------------");
    }
    
 // WHO - listar utilizadores ligados com contagem
    private void tratarWho() {
        String lista = GerirCliente.listarClientes();
        int total = GerirCliente.contarClientes();
        out.println("200 utilizadores ligados (" + total + "): " + lista);
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

// CHANGE NICK - mudar o nome depois do login
    private void tratarNickChange(String mensagem) {
        String novoNome = mensagem.substring(5).trim();

        // validar se nome esta vazio
        if (novoNome.isEmpty()) {
            out.println("400 formato: NICK <novo_nome>");
            return;
        }

        // validar tamanho
        if (novoNome.length() < 3 || novoNome.length() > 20) {
            out.println("400 nome deve ter entre 3 a 20 caracteres");
            return;
        }

        // validar se ja esta em uso
        if (GerirCliente.existeCliente(novoNome)) {
            out.println("409 nome ja utilizado");
            return;
        }

        // guardar nome antigo para o log e notificacao
        String nomeAntigo = username;

        // atualizar no mapa — remove o antigo e adiciona o novo
        GerirCliente.removerCliente(username);
        username = novoNome;
        GerirCliente.addClient(username, this);

        // notificar todos da mudanca
        GerirCliente.transmissaoExceto("*** " + nomeAntigo + " mudou o nome para " + username, username);
        out.println("200 nome alterado para " + username);
        Logger.log(nomeAntigo + " mudou o nome para " + username);
    }

// PING - verificar se o servidor esta a responder
    private void tratarPing() {
        out.println("200 PONG");
        Logger.log(username + " fez PING");
    }

// TIME - mostrar hora atual do servidor
    private void tratarTime() {
        String hora = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        out.println("200 hora do servidor: " + hora);
        Logger.log(username + " pediu TIME");
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