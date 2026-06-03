package projetoSockets_Grupo2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    // variavel para controlar se o cliente esta ligado
    private static boolean ativo = true;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // pedir host e porta ao utilizador
        System.out.println("Host do servidor (ex.: 127.0.0.1): ");
        String host = sc.nextLine();

        System.out.println("Porta do servidor (ex.: 5000): ");
        int porta = Integer.parseInt(sc.nextLine());

        try {
            // ligar ao servidor
            Socket socket = new Socket(host, porta);
            System.out.println("Ligado ao servidor " + host + ":" + porta);
            System.out.println("Escreva HELP para ver os comandos.");

            // stream para ler mensagens do servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // stream para enviar mensagens ao servidor (true = envia imediatamente)
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // criar thread para receber mensagens do servidor em paralelo
            ThreadRecetor recetor = new ThreadRecetor(in);
            Thread threadRecetor = new Thread(recetor);
            threadRecetor.start();

            // loop principal: ler comandos do teclado e enviar ao servidor
            while (ativo) {
                String comando = sc.nextLine();

                if (comando == null) {
                    break;
                }

                comando = comando.trim();

                if (comando.isEmpty()) {
                    continue;
                }

                // tratamento especial para SEND
                if (comando.toUpperCase().startsWith("SEND ")) {
                    String nomeFicheiro = comando.substring(5).trim();
                    enviarFicheiro(nomeFicheiro, socket, out);
                    continue;
                }

                // enviar o comando ao servidor
                out.println(comando);

                // se o utilizador escreveu QUIT, terminar
                if (comando.equalsIgnoreCase("QUIT")) {
                    ativo = false;
                }
            }

            // fechar tudo
            socket.close();
            System.out.println("Cliente terminado.");

        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }

        sc.close();
    }

    // metodo para enviar ficheiro ao servidor
    private static void enviarFicheiro(String nomeFicheiro, Socket socket, PrintWriter out) {

        // verificar se o ficheiro existe
        File ficheiro = new File(nomeFicheiro);

        if (!ficheiro.exists()) {
            System.out.println("Erro: ficheiro nao encontrado.");
            return;
        }

        if (ficheiro.length() == 0) {
            System.out.println("Erro: ficheiro vazio.");
            return;
        }

        // verificar tamanho maximo (5 MB)
        long limiteBytes = 5 * 1024 * 1024;
        if (ficheiro.length() > limiteBytes) {
            System.out.println("Erro: ficheiro demasiado grande (maximo 5 MB).");
            return;
        }

        try {
            // avisar o servidor que vai receber um ficheiro
            out.println("SEND " + ficheiro.getName());

            // usar DataOutputStream para enviar bytes
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // 1. enviar tamanho do nome
            byte[] nomeBytes = ficheiro.getName().getBytes("UTF-8");
            dos.writeInt(nomeBytes.length);

            // 2. enviar nome do ficheiro
            dos.write(nomeBytes);

            // 3. enviar tamanho do ficheiro
            dos.writeLong(ficheiro.length());

            // 4. enviar conteudo do ficheiro
            FileInputStream fis = new FileInputStream(ficheiro);
            byte[] buffer = new byte[4096];
            int lido;
            while ((lido = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, lido);
            }
            fis.close();
            dos.flush();

            System.out.println("Ficheiro enviado: " + ficheiro.getName() + " (" + ficheiro.length() + " bytes)");

        } catch (Exception e) {
            System.out.println("Erro ao enviar ficheiro: " + e.getMessage());
        }
    }
}
