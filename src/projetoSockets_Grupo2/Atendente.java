package projetoSockets_Grupo2;

import java.io.*;
import java.net.Socket;

public class Atendente implements Runnable{
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
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(),true);
			
			//pedir nome ao cliente
			out.println("Username:");
			username=in.readLine();
			
			if (username ==null || username.trim().isEmpty()){//pode usar isto trim?
				out.println("400 nome invalido");
				socket.close();
				return;
			}
			username = username.trim();
			
			if (username.length()<3 || username.length()> 20) {
				out.println("400 nome deve ter entre 3 a 20 caracteres");
				socket.close();
				return;
			}
			//registrar cliente
			GerirCliente.addClient(username, this);
			Logger.log(username + "entrou");
			out.println("200 bem-vindo "+ username);
			
			//loop principal de comandos
			String mensagem;
			while ((mensagem = in.readLine())!= null) {
				mensagem= mensagem.trim();
				Logger.log(username+ " :  "+mensagem);
				
				if (mensagem.equalsIgnoreCase("HELP")) {
					tratarHelp();
				}
				else if (mensagem.equalsIgnoreCase("WHO")) {
					tratarWho();
				}
				else if (mensagem.toUpperCase().startsWith("MSG")) {
					tratarMsg(mensagem);
				}
				else if (mensagem.toUpperCase().startsWith("PM ")) {
                    tratarPm(mensagem);
 
                } 
				else if (mensagem.equalsIgnoreCase("QUIT")) {
                    tratarQuit();
                    break;
                }
				else if (mensagem.isEmpty()) {
					//IGNORA LINHAS VAZIAS
				}
				else {
					out.println("400 comando inválido!");
				}
			}
		}catch (Exception e) {
			Logger.log("Cliente desligado");
		
		}finally {
			GerirCliente.removerCliente(username);
			try {
				socket.close();
			}catch (Exception ignored) {}
		}
	}
	
	//HELP -mostrar comandos disponiveis
	private void tratarHelp() {
		out.println("200 comandos: NICK <nome> | WHO | MSG <texto> | PM <nick> <texto> | QUIT");
		
	}
	
	//WHO - listar utilizadores ligados
	private void tratarWho() {
		String lista = GerirCliente.listarClientes();
		out.println("200 utilizdores: " + lista);
	}
	
	//MSG - enviar mensagem public para todos
	private void tratarMsg(String mensagem) {
		//retirar o MSG 
		String texto = mensagem.substring(4).trim();
		
		if (texto.isEmpty()) {
			out.println("400 formato : MSG <texto>");
			return;
		}
		GerirCliente.transmissao("MSG " + username + ": " + texto);
        out.println("200 mensagem enviada");
        Logger.log(username + " enviou mensagem publica: " + texto);
    }
	
	//PM - enviar mensagem privada para um utilizador
	private void tratarPm(String mensagem) {
		
		//formato esperado: PM <nick> <texto>
		String resto = mensagem.substring(3).trim();
		int espaco = resto.indexOf(" ");
		
		if (espaco == -1) {
			out.println("400 formato : PM <nick> <texto>");
			return;
		}
		String destinatario =resto.substring(0, espaco);
		String texto = resto.substring(espaco+1).trim();
		
		if (texto.isEmpty()) {
			out.println("400 formato : PM <nick> <texto>");
			return;
		}
		Atendente cliente = GerirCliente.getCliente(destinatario);
		if (cliente ==null ) {
			out.println("404 utilizador não encontrado");
			return;
		}
		
		cliente.enviarMensagem("PM" + username + ": "+texto);
		out.println("200 mensagem privada enviada");
		Logger.log(username + "enviou mensagem privada para"+ destinatario);
	}
	//QUIT - desligar o cliente
	private void tratarQuit() {
		out.println("200 adeus");
		Logger.log(username+ "saiu");
	}
	
	//metodo para enviar uma mensagem a este cliente (usado pelo transmissao e PM)
	public void enviarMensagem(String mensagem) {
		out.println(mensagem);
	}	
}
