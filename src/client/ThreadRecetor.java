package client;
import java.io.BufferedReader;

/**
 * Thread responsável pela receção de mensagens provenientes
 * do servidor.
 *
 * Esta classe executa em paralelo com a thread principal
 * do cliente, permitindo receber mensagens enquanto o
 * utilizador continua a introduzir comandos na consola.
 *
 * A thread permanece ativa enquanto a ligação TCP estiver
 * estabelecida, lendo continuamente mensagens enviadas
 * pelo servidor e apresentando-as no terminal.
 *
 * A leitura é efetuada de forma bloqueante através de um
 * BufferedReader associado ao socket TCP.
 *
 * Caso a ligação seja interrompida ou ocorra um erro de
 * comunicação, a thread termina automaticamente e informa
 * o utilizador.
 *
 * Esta classe implementa a interface Runnable para poder
 * ser executada numa thread independente.
 *
 * @author Grupo 2
 * @version 3.0
 */

public class ThreadRecetor implements Runnable {
	private BufferedReader in;

	public ThreadRecetor(BufferedReader in) {
		this.in = in;
	}

	@Override
	public void run() {
		try {
			String mensagem;
			
			//ler mensagens do servidor enquanto a ligacao estiver ativa
			while ((mensagem = in.readLine()) != null) {
				System.out.println(mensagem);
			}
		}catch (Exception e) {
			System.out.println("Ligacao com o servidor perdida!");
		}
	}
}
