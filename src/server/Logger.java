package server;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * Classe responsável pelo registo de eventos do sistema.
 *
 * Centraliza toda a informação de monitorização da aplicação,
 * permitindo guardar eventos relevantes tanto na consola como
 * num ficheiro de log persistente.
 *
 * Os registos incluem:
 * - Arranque e encerramento do servidor;
 * - Ligações e desconexões de clientes;
 * - Comandos recebidos;
 * - Operações realizadas;
 * - Transferências de ficheiros;
 * - Erros e exceções.
 *
 * Cada entrada de log contém:
 * - Data e hora do evento;
 * - Tipo de registo;
 * - Identificador do cliente;
 * - Descrição da operação.
 *
 * O ficheiro de log é atualizado automaticamente através
 * de escrita sincronizada, garantindo consistência quando
 * múltiplas threads escrevem em simultâneo.
 *
 * Esta classe disponibiliza apenas métodos estáticos,
 * funcionando como um serviço global de logging para
 * toda a aplicação.
 *
 * @author Grupo 2
 * @version 3.0
 */
 class Logger {

	private static final String FICHEIRO_LOG = "servidor.log";
    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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