package projetoSockets_Grupo2;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Servidor {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Indique a porta:");
		int porta = sc.nextInt();
		
		try 
		    (ServerSocket serverSocket = new ServerSocket(porta)){
			Logger.log("Servidor iniciado na porta: " + porta);
			System.out.println("A aguardar clientes...");
			
			//arrancar o servidor UDP numa thread separada
			Thread threadUDP = new Thread (new Runnable() {
				public void run() {
					UdpServidorDescoberta.iniciar();
				}
			});
			
			threadUDP.start();
			
			//loop principal - aceitar clientes TCP
			while(true) {
				Socket socket = serverSocket.accept();
//--- TIMEOUT ---
// se o cliente nao enviar nada durante 3 minutos (180000 ms), desliga automaticamente
// sem isto o servidor ficaria bloqueado para sempre à espera do cliente
				socket.setSoTimeout(180000);
				
				Logger.log("Novo cliente ligado");
				Atendente client = new Atendente(socket);
				new Thread(client).start();
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}

