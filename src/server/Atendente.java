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

/**
 * Responsável pelo atendimento individual de cada cliente ligado ao servidor.
 *
 * Cada ligação TCP aceite pelo servidor origina uma instância desta classe,
 * executada numa thread independente através de um ExecutorService.
 *
 * A classe implementa todo o protocolo de comunicação da aplicação,
 * incluindo a fase de autenticação, processamento de comandos,
 * envio de mensagens e transferência de ficheiros.
 *
 * A classe é responsável por:
 * - Validar entradas dos utilizadores;
 * - Aplicar restrições de segurança nos ficheiros recebidos;
 * - Gerir timeouts de inatividade;
 * - Registar eventos e operações através do sistema de logs;
 * - Remover automaticamente clientes desligados.
 *
 * Cada cliente recebe um identificador único utilizado
 * para efeitos de monitorização e auditoria nos logs.
 *
 * Esta classe implementa a interface Runnable para permitir
 * a execução concorrente de múltiplos clientes em simultâneo.
 *
 * @author Grupo 2
 * @version 3.0
 */
public class Atendente implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private boolean saiuNormalmente = false;
    private static int contadorId = 0;
    private int clienteId;


    public Atendente(Socket socket) {
        this.socket = socket;
        this.clienteId = ++contadorId;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            
         // logo no inicio do run(), antes do login
            String enderecoRemoto = socket.getInetAddress().getHostAddress() 
                + ":" + socket.getPort();
            Logger.info(String.valueOf(clienteId), "nova ligacao de " + enderecoRemoto);

            // FASE DE LOGIN
            out.println("");
            out.println("=== Sistema Distribuído de Comunicação ===");
            out.println("");
            out.println("Comandos dísponíveis:");
            out.println("NICK");
            out.println("HELP");
            out.println("QUIT");
            out.println("");
            String mensagem;
            while ((mensagem = in.readLine()) != null) {
                mensagem = mensagem.trim();

                if (mensagem.equalsIgnoreCase("HELP")) {
                    out.println("Comandos disponiveis antes do login:");
                    out.println("  NICK <nome>  - ex: NICK alice");
                    out.println("  HELP         - lista de comandos");
                    out.println("  QUIT         - sair");
                    continue;
                }

                if (mensagem.equalsIgnoreCase("QUIT")) {
                    out.println("200 adeus");
                    try { socket.close(); 
                    } catch (Exception ignored) {}
                    return;
                }

                if (mensagem.toUpperCase().startsWith("NICK ")) {
                    String nome = mensagem.substring(5).trim();

                    if (nome.isEmpty()) {
                        out.println("400 nome invalido.");
                        continue;
                    }

                    if (nome.length() < 3 || nome.length() > 20) {
                        out.println("400 nome deve ter entre 3 a 20 caracteres.");
                        continue;
                    }

                    if (GerirCliente.existeCliente(nome)) {
                        out.println("409 nome ja utilizado. Exemplo: NICK outro_nome");
                        continue;
                    }

                    username = nome;
                    GerirCliente.addClient(username, this);
                    Logger.log(username + " entrou");
                    Logger.info(String.valueOf(clienteId), username + " ligado a partir de " + enderecoRemoto);
                    out.println("200 bem-vindo " + username);
                    break;
                }

                out.println("401 faça login primeiro. Exemplo: NICK alice");
            }

            // FASE PRINCIPAL
            while ((mensagem = in.readLine()) != null) {
                mensagem = mensagem.trim();
                Logger.recv(String.valueOf(clienteId), mensagem);

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
                    out.println("400 comando invalido. Escreve HELP para ver os comandos");
                }
            }

        } catch (SocketTimeoutException e) {
            if (out != null) out.println("408 timeout");
            Logger.erro(String.valueOf(clienteId), "timeout - cliente desligado");  // ← falta isto
        
        } catch (Exception e) {
            Logger.erro(String.valueOf(clienteId), "erro inesperado: " + e.getMessage()); // ← falta isto
        
        }finally {
            if (username != null && !saiuNormalmente) {
                GerirCliente.removerCliente(username);
                Logger.info(String.valueOf(clienteId), "sessao terminada"); // ← falta isto
                
            } else if (username != null && saiuNormalmente) {
                GerirCliente.removerCliente(username);
                // nao regista de novo — ja foi registado no tratarQuit
            } else {
                Logger.log("cliente anonimo desligado");
            }
            try { socket.close(); } catch (Exception ignored) {}}
        }

    // HELP - mostrar comandos disponiveis
    private void tratarHelp() {
        out.println("------------------------------------------------------");
        out.println("               GUIA DE FUNCIONAMENTO ");
        out.println("------------------------------------------------------");
        out.println("COMANDOS:");
        out.println("  WHO                        - Lista utilizadores ativos");
        out.println("  MSG <texto>                - Envia mensagem publica");
        out.println("                               ex: MSG ola a todos");
        out.println("  PM <nick> <texto>          - Envia mensagem privada");
        out.println("                               ex: PM alice ola");
        out.println("  SEND <ficheiro>            - Envia ficheiro para o servidor");
        out.println("                               ex: SEND foto.jpg");
        out.println("  NICK <novo_nome>           - Altera o teu username");
        out.println("                               ex: NICK novo_nome");
        out.println("  PING                       - Verifica ligacao ao servidor");
        out.println("  TIME                       - Devolve o tempo atual do servidor");
        out.println("  QUIT                       - Desconecta a ligacao");
        out.println("------------------------------------------------------");
        out.println("CODIGOS DE RESPOSTA:");
        out.println("  200 - Operacao realizada com sucesso");
        out.println("  400 - Pedido invalido");
        out.println("  401 - Cliente ainda nao registado");
        out.println("  404 - Recurso ou utilizador nao encontrado");
        out.println("  408 - Timeout");
        out.println("  409 - Nome ja utilizado");
        out.println("  500 - Erro interno do servidor");
        out.println("------------------------------------------------------");
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
            out.println("400 formato invalido. Exemplo: MSG ola a todos");
            return;
        }

        GerirCliente.transmissaoExceto("MSG " + username + ": " + texto, username);
        out.println("200 mensagem enviada");
        Logger.info(String.valueOf(clienteId), "mensagem publica enviada: " + texto);
    }

    // PM - enviar mensagem privada para um utilizador
    private void tratarPm(String mensagem) {
        String resto = mensagem.substring(3).trim();
        int espaco = resto.indexOf(" ");

        if (espaco == -1) {
            out.println("400 formato invalido.");
            return;
        }

        String destinatario = resto.substring(0, espaco);
        String texto = resto.substring(espaco + 1).trim();

        if (texto.isEmpty()) {
            out.println("400 formato invalido.");
            return;
        }

        Atendente cliente = GerirCliente.getCliente(destinatario);

        if (cliente == null) {
            out.println("404 utilizador nao encontrado. Usa WHO para ver os utilizadores ligados");
            return;
        }

        cliente.enviarMensagem("PM " + username + ": " + texto);
        out.println("200 mensagem privada enviada para " + destinatario);
        Logger.info(String.valueOf(clienteId), "mensagem privada para " + destinatario + ": " + texto);
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
                out.println("400 nome de ficheiro invalido. Exemplo: SEND foto.jpg");
                return;
            }

            // 3. ler tamanho do ficheiro
            long tamanhoFicheiro = dis.readLong();

            if (tamanhoFicheiro == 0) {
                out.println("400 ficheiro vazio. O ficheiro nao pode ter 0 bytes");
                return;
            }

            long limiteBytes = 5 * 1024 * 1024; // 5 MB
            if (tamanhoFicheiro > limiteBytes) {
                out.println("400 tamanho excede limite. Maximo permitido: 5 MB");
                return;
            }

         // 4. criar pasta uploads se nao existir
            File pasta = new File("uploads");
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            // 5. ler conteudo e guardar ficheiro
            File ficheiro = new File("uploads/" + nomeFicheiro);
            FileOutputStream fos = new FileOutputStream(ficheiro); //subistitui

            byte[] buffer = new byte[4096];
            long bytesRestantes = tamanhoFicheiro;

            while (bytesRestantes > 0) {
                int aLer = (int) Math.min(buffer.length, bytesRestantes);
                int lido  = dis.read(buffer, 0, aLer);
                fos.write(buffer, 0, lido);
                bytesRestantes -= lido;
            }
            //RECEÇÃO DO FICHEIRO
            Logger.info(String.valueOf(clienteId), "ficheiro recebido " + nomeFicheiro + " " + tamanhoFicheiro + " bytes");

            fos.close();

            out.println("200 ficheiro recebido " + nomeFicheiro + " " + tamanhoFicheiro + " bytes");
            Logger.log("Ficheiro recebido de " + username + ": " + nomeFicheiro + " (" + tamanhoFicheiro + " bytes)");

        } catch (Exception e) {
            out.println("400 erro na transferencia");
            Logger.log("Erro ao receber ficheiro de " + username + ": " + e.getMessage());
        }
    }

    // NICK - mudar o nome depois do login
    private void tratarNickChange(String mensagem) {
        String novoNome = mensagem.substring(5).trim();

        if (novoNome.isEmpty()) {
            out.println("400 formato invalido. Exemplo: NICK novo_nome");
            return;
        }

        if (novoNome.length() < 3 || novoNome.length() > 20) {
            out.println("400 nome deve ter entre 3 a 20 caracteres. Exemplo: NICK alice");
            return;
        }

        if (GerirCliente.existeCliente(novoNome)) {
            out.println("409 nome ja utilizado. Escolhe outro, exemplo: NICK outro_nome");
            return;
        }

        String nomeAntigo = username;
        GerirCliente.removerCliente(username);
        username = novoNome;
        GerirCliente.addClient(username, this);

        GerirCliente.transmissaoExceto("*** " + nomeAntigo + " mudou o nome para " + username, username);
        out.println("200 nome alterado para " + username);
        Logger.info(String.valueOf(clienteId), nomeAntigo + " mudou o nome para " + username);
    }

    // PING - verificar se o servidor esta a responder
    private void tratarPing() {
        out.println("200 PONG");
        Logger.info(String.valueOf(clienteId), username + " fez PING"); 
    }
    // TIME - mostrar hora atual do servidor
    private void tratarTime() {
        String hora = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        out.println("200 hora do servidor: " + hora);
        Logger.info(String.valueOf(clienteId), username + " pediu TIME");
    }

    // QUIT - desligar o cliente
    private void tratarQuit() {
        out.println("200 adeus");
        saiuNormalmente = true;
        Logger.info(String.valueOf(clienteId), username + " saiu com QUIT"); 
    }
    
    // metodo para enviar uma mensagem a este cliente
    public void enviarMensagem(String mensagem) {
        out.println(mensagem);
    }
}