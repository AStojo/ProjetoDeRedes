package projetoSockets_Grupo2;

import java.util.concurrent.ConcurrentHashMap;

public class GerirCliente {
	private static ConcurrentHashMap<String, Atendente> clientes = new ConcurrentHashMap<>();
	
/**
 * Metodo adicionar Cliente
 */
	public static void addClient(String nome, Atendente cliente) {
		clientes.put(nome,cliente);
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
 * Metodo Transmissao
 */
	public static void transmissao(String mensagem) {
		for (Atendente a: clientes.values()) {
			a.enviarMensagem(mensagem);
		}
	}
}
