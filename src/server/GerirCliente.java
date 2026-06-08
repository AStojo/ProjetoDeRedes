package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Classe responsável pela gestão dos clientes autenticados.
 *
 * Mantém uma lista global de todos os utilizadores atualmente
 * ligados ao servidor, permitindo o registo, remoção,
 * consulta e comunicação entre clientes.
 *
 * A informação é armazenada numa estrutura ConcurrentHashMap,
 * garantindo acesso seguro em ambientes concorrentes onde
 * múltiplas threads podem adicionar, remover ou consultar
 * clientes simultaneamente.
 *
 * Funcionalidades principais:
 * - Registo de novos utilizadores;
 * - Remoção de utilizadores desligados;
 * - Verificação de usernames existentes;
 * - Consulta de clientes pelo nome;
 * - Listagem de utilizadores ativos;
 * - Envio de mensagens em broadcast;
 * - Envio de mensagens para todos exceto o remetente;
 * - Contagem de clientes ligados.
 *
 * Esta classe disponibiliza apenas métodos estáticos,
 * funcionando como um repositório central partilhado
 * por todas as instâncias da classe Atendente.
 *
 * @author Grupo 2
 * @version 3.0
 */

public class GerirCliente {
    private static ConcurrentHashMap<String, Atendente> clientes = new ConcurrentHashMap<>();

    /**
     * Metodo adicionar Cliente
     */
    public static void addClient(String nome, Atendente cliente) {
        clientes.put(nome, cliente);
    }

    /**
     * Metodo remover Cliente
     */
    public static void removerCliente(String nome) {
        clientes.remove(nome);
    }

    // verificar se ja existe um cliente com esse nome
    public static boolean existeCliente(String nome) {
        return clientes.containsKey(nome);
    }

    // obter um cliente pelo nome (para mensagens privadas)
    public static Atendente getCliente(String nome) {
        return clientes.get(nome);
    }

    // listar todos os clientes ligados
    public static String listarClientes() {
        String lista = "";
        for (String nome : clientes.keySet()) {
            if (lista.isEmpty()) {
                lista = nome;
            } else {
                lista = lista + ", " + nome;
            }
        }
        return lista;
    }

    /**
     * Metodo TransmissaoExceto - envia para todos EXCETO o remetente
     */
    public static void transmissaoExceto(String mensagem, String remetente) {
        for (Map.Entry<String, Atendente> entry : clientes.entrySet()) {
            if (!entry.getKey().equals(remetente)) {
                entry.getValue().enviarMensagem(mensagem);
            }
        }
    }
    
// contar quantos clientes estao ligados
    public static int contarClientes() {
        return clientes.size();
    }
}
