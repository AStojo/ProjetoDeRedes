package client;
import java.io.BufferedReader;

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
