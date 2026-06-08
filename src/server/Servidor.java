package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import udp.UdpServidorDescoberta;

public class Servidor {

private static int porta;

public static int getPorta() {
    return porta;
}
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Indique a porta:");
		Servidor.porta = sc.nextInt();
		
		// criar pool de threads gerido — substitui new Thread(...).start()
        // newCachedThreadPool: reutiliza threads livres e cria novas apenas se necessário
		ExecutorService pool = Executors.newCachedThreadPool();
		
		try 
		    (ServerSocket serverSocket = new ServerSocket(porta)){
			Logger.log("Servidor iniciado na porta: " + porta);
			System.out.println("A aguardar clientes...");
			
			//arrancar o servidor UDP numa thread separada
			Thread threadUDP = new Thread(() -> UdpServidorDescoberta.iniciar()); 
			threadUDP.setDaemon(true); //termina quando o servidor terminar
			threadUDP.start();
			
			//loop principal - aceitar clientes TCP
			while(true) {
				Socket socket = serverSocket.accept();
//--- TIMEOUT ---
// se o cliente nao enviar nada durante 3 minutos (180.000 ms), desliga automaticamente
// sem isto o servidor ficaria bloqueado para sempre à espera do cliente
				socket.setSoTimeout(180000);
				
				Logger.log("Novo cliente ligado de" + socket.getInetAddress());
				Atendente client = new Atendente(socket);
				pool.execute(client);	
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			pool.shutdown(); // liberta recursos quando o servidor termina
		}
		
		
	
	}
}