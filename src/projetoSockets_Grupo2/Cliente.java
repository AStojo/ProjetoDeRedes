package Redes;
package projetoSockets_Grupo2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
	//variavel para controlar se o cliente esta ligado
	private static boolean ativo = true;
	
	public static void main (String[] args) {
		Scanner sc = new Scanner (System.in);
		
		//pedir host e porta ao utilizador
		System.out.println("Host do servidor (ex.: 127.0.0.1): ");
		String host = sc.nextLine();
		
		System.out.println("Porta do servidor (ex.:5000");
		int porta= Integer.parseInt(sc.nextLine());
		
		try {
			//ligar ao servidor 
			Socket socket= new Socket (host, porta);
			System.out.println("Ligado ao servidor "+host+":"+porta);
			System.out.println("Escreva HELP para ver os comandos.");
			
			//stream para ler mensagens do servidor
			BufferedReader in =new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//stream para enviar mensagens ao servidor
			PrintWriter out =new PrintWriter(socket.getOutputStream());
			
			//criar thread para receber mensagens do servidor em paralelo
			ThreadRecetor recetor =new ThreadRecetor(in);
			Thread threadRecetor =new Thread (recetor);
			threadRecetor.start();
			
			//loop principal: ler comandos do teclado e enviar ao servidor
			while (ativo) {
				String comando = sc.nextLine();
				
				if(comando==null) {
					break;
				}
				
				//enviar o comando ao servidor
				out.println(comando);
				
				//se o utilizador escreveu QUIT, termina
				if (comando.equalsIgnoreCase("QUIT")) {
					ativo = false;
				}
			}
			
			//fechar tudo
			socket.close();
			System.out.println("Cliente terminado.");
		}catch (Exception e) {
			System.out.println("ERRO: "+e.getMessage());
		}
		sc.close();
	}
}
