package projetoSockets_Grupo2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClienteDescoberta {

    public static void main(String[] args) {

        // porta UDP do servidor
        int portaUDP = 6000;

        // timeout de 3 segundos à espera de resposta
        int timeout = 3000;

        try {
            DatagramSocket socketUDP = new DatagramSocket();

            // definir timeout para não ficar bloqueado para sempre
            socketUDP.setSoTimeout(timeout);

            // preparar mensagem DISCOVER
            String mensagem = "DISCOVER";
            byte[] mensagemBytes = mensagem.getBytes();

            // enviar para o endereço de broadcast da rede local
            InetAddress enderecoServidor = InetAddress.getByName("255.255.255.255");

            DatagramPacket pedido = new DatagramPacket(
                mensagemBytes,
                mensagemBytes.length,
                enderecoServidor,
                portaUDP
            );

            System.out.println("A procurar servidor na rede...");
            socketUDP.send(pedido);

            // aguardar resposta do servidor
            byte[] buffer = new byte[256];
            DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);

            try {
                socketUDP.receive(resposta);

                // ler a resposta
                String mensagemResposta = new String(resposta.getData(), 0, resposta.getLength());
                System.out.println("Servidor encontrado: " + mensagemResposta);

            } catch (Exception e) {
                // timeout — nenhum servidor respondeu
                System.out.println("Nenhum servidor encontrado na rede.");
            }

            socketUDP.close();

        } catch (Exception e) {
            System.out.println("Erro no cliente UDP: " + e.getMessage());
        }
    }
}