package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    private static boolean ativo = true;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Host do servidor (ex.: 127.0.0.1): ");
        String host = sc.nextLine();

        System.out.println("Porta do servidor (ex.: 5000): ");
        int porta = Integer.parseInt(sc.nextLine());

        try {
            Socket socket = new Socket(host, porta);
            System.out.println("Ligado ao servidor " + host + ":" + porta);
            System.out.println("Escreva HELP para ver os comandos.");

            // UTF-8 explícito — evita dependência do encoding do sistema
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            ThreadRecetor recetor = new ThreadRecetor(in);
            Thread threadRecetor = new Thread(recetor);
            threadRecetor.start();

            while (ativo) {
                String comando = sc.nextLine();

                if (comando == null) break;

                comando = comando.trim();

                if (comando.isEmpty()) continue;

                // tratamento especial para SEND
                if (comando.toUpperCase().startsWith("SEND ")) {
                    String nomeFicheiro = comando.substring(5).trim();
                    enviarFicheiro(nomeFicheiro, socket, out);
                    continue;
                }

                out.println(comando);

                if (comando.equalsIgnoreCase("QUIT")) {
                    ativo = false;
                }
            }

            socket.close();
            System.out.println("Cliente terminado.");

        } catch (Exception e) {
            System.out.println("400 ERRO: " + e.getMessage());
        }

        sc.close();
    }

    private static void enviarFicheiro(String nomeFicheiro, Socket socket, PrintWriter out) {

        File ficheiro = new File(nomeFicheiro);

        if (!ficheiro.exists()) {
            System.out.println("400 Erro: ficheiro nao encontrado.");
            return;
        }

        if (ficheiro.length() == 0) {
            System.out.println("400 Erro: ficheiro vazio.");
            return;
        }

        long limiteBytes = 5 * 1024 * 1024;
        if (ficheiro.length() > limiteBytes) {
            System.out.println("400 Erro: ficheiro demasiado grande (maximo 5 MB).");
            return;
        }

        try {
            out.println("SEND " + ficheiro.getName());

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // UTF-8 explícito no nome do ficheiro
            byte[] nomeBytes = ficheiro.getName().getBytes("UTF-8");
            dos.writeInt(nomeBytes.length);  // [int nameLen]
            dos.write(nomeBytes);            // [nameBytes UTF-8]
            dos.writeLong(ficheiro.length()); // [long fileLen]

            // [fileBytes]
            FileInputStream fis = new FileInputStream(ficheiro);
            byte[] buffer = new byte[4096];
            int lido;
            while ((lido = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, lido);
            }
            fis.close();
            dos.flush();

            System.out.println("Ficheiro enviado: " + ficheiro.getName()
                + " (" + ficheiro.length() + " bytes)");

        } catch (Exception e) {
            System.out.println("400 Erro ao enviar ficheiro: " + e.getMessage());
        }
    }
}