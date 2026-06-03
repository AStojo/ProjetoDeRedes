package projetoSockets_Grupo2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	
	public static void log(String mensagem) { // imprime o tempo que foi enviado ([horas: minutos:segundos])
		String tempo= LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		System.out.println("["+ tempo + "]   " + mensagem);
	/**.format(..)
	 * Aplica o formato à data/hora obtida.
	 * 
	 * DateTimeFormatter.ofPattern("HH:mm:ss")
     * Define o formato em que a hora será apresentada.
     *
     *HH → horas (00–23)
     *mm → minutos
     *ss → segundos
	 */
	
	}
}

