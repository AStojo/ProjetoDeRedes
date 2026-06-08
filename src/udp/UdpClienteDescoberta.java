package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
/**
 * Cliente de descoberta automática de servidores através de UDP.
 *
 * Esta classe permite localizar um servidor disponível na rede
 * local sem que o utilizador tenha de conhecer previamente o seu
 * endereço IP ou a porta TCP utilizada.
 *
 * O funcionamento baseia-se no envio de uma mensagem UDP
 * em broadcast para a porta de descoberta do servidor.
 *
 * Processo de funcionamento:
 * 1. Cria um socket UDP temporário;
 * 2. Envia a mensagem "DISCOVER" para o endereço de broadcast;
 * 3. Aguarda uma resposta durante um período limitado;
 * 4. Recebe o endereço IP e a porta TCP do servidor;
 * 5. Apresenta a informação ao utilizador.
 *
 * Caso nenhum servidor responda dentro do tempo configurado,
 * a operação é cancelada automaticamente através de timeout.
 *
 *
 * Esta funcionalidade utiliza UDP devido ao seu baixo custo
 * de comunicação e à facilidade de utilização de mensagens
 * broadcast para descoberta de serviços na rede local.
 *
 * @author Grupo 2
 * @version 3.0
 */
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
            System.out.println("404 Erro no cliente UDP: " + e.getMessage());
        }
    }
}