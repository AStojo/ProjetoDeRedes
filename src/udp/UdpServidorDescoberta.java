package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import server.Logger;
import server.Servidor;
 
public class UdpServidorDescoberta {
 
    // porta UDP onde o servidor fica à escuta
    private static int portaUDP = 6000;
 
    public static void iniciar() {
        try {
            DatagramSocket socketUDP = new DatagramSocket(portaUDP);
            Logger.log("Servidor UDP à escuta na porta " + portaUDP);
 
            while (true) {
                // buffer para receber o pedido do cliente
                byte[] buffer = new byte[256];
                DatagramPacket pedido = new DatagramPacket(buffer, buffer.length);
 
                // aguardar pedido
                socketUDP.receive(pedido);
 
                // ler a mensagem recebida
                String mensagem = new String(pedido.getData(), 0, pedido.getLength());
                Logger.log("UDP recebeu: " + mensagem + " de " + pedido.getAddress());
 
                // se o cliente enviou DISCOVER, responder com o endereço e porta TCP
                if (mensagem.trim().equalsIgnoreCase("DISCOVER")) {
                    String resposta = "SERVER " + InetAddress.getLocalHost().getHostAddress() + " "+ Servidor.getPorta();
                    byte[] respostaBytes = resposta.getBytes();
 
                    // enviar resposta para o cliente
                    DatagramPacket pacoteResposta = new DatagramPacket(
                        respostaBytes,
                        respostaBytes.length,
                        pedido.getAddress(),
                        pedido.getPort()
                    );
 
                    socketUDP.send(pacoteResposta);
                    Logger.log("UDP respondeu: " + resposta);
                }
            }
 
        } catch (Exception e) {
            Logger.log("Erro no servidor UDP: " + e.getMessage());
        }
    }
}
 
