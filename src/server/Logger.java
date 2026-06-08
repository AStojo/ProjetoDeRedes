package server;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	private static final String PASTA = "uploads";
	private static final String FICHEIRO_LOG = "servidor.log";
    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
 // garante que a pasta uploads existe antes de qualquer escrita
    static {
        File pasta = new File(PASTA);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }
    }

    // log informativo — arranque, ligações, saídas, ficheiros
    public static void info(String clienteId, String mensagem) {
        escrever("INFO", clienteId, mensagem);
    }

    // log de comando recebido
    public static void recv(String clienteId, String mensagem) {
        escrever("RECV", clienteId, mensagem);
    }

    // log de erro
    public static void erro(String clienteId, String mensagem) {
        escrever("ERR ", clienteId, mensagem);
    }

    // log sem cliente (arranque do servidor)
    public static void log(String mensagem) {
        escrever("INFO", "servidor", mensagem);
    }

    private static synchronized void escrever(String tipo, String clienteId, String mensagem) {
        String linha = LocalDateTime.now().format(FORMATO)
            + " | " + tipo
            + " | cliente=" + clienteId
            + " | " + mensagem;

        System.out.println(linha);

        try (PrintWriter fw = new PrintWriter(new FileWriter(FICHEIRO_LOG, true))) {
            fw.println(linha);
        } catch (Exception e) {
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }
}