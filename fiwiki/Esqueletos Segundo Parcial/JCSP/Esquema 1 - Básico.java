// importamos la librería JCSP
import org.jcsp.lang.*;

@author Enzo Ferey

// Clase del servidor
public class MiClaseJCSP implements CSProcess
{
	// Atributos para definir valores

	// Canal por el que vamos a recibir y enviar
	private Any2OneChannel channel;

	// Para evitar la construcción de servidores huérfanos
    public MiClaseJCSP() {
    }

    // Pasamos el canal por el que recibimos las peticiones
    public MiClaseJCSP(Any2OneChannel c) {
		this.channel = c;
    }

    // Código del servidor
    public void run() {
		
		// EL ESTADO DEL RECURSO ES PRIVADO AL THREAD DEL SERVIDOR !!!

		// el servidor es simplemente un bucle infinito de esperar a
		// que lleguen peticiones, y reaccionar en función al valor de
		// la petición:
	    TipoDatoPeticion pet;
		while (true) {
		    pet = (TipoDatoPeticion) channel.in().read(); // leemos del canal de peticiones:

		    // analizamos la petición:
		    if (pet == VALOR1) 
		    {
		    	// Operacion correspondiente
		    } 
		    else // pet == VALOR2
		    {
				// Operacion correspondiente
		    }
		} // FIN BUCLE SERVICIO
    } // FIN SERVIDOR
}

// Clase del cliente
public class ClienteJCSP extends Thread
{
	private Any2OneChannel c;

	public ClienteJCSP(Any2OneChannel c) {
	    // en el momento de su creación le decimos a los clientes
	    // cuál es el canal con el que se tienen que comunicar
	    this.c = c;
    }

	public void run() {
        // enviamos una petición de peticion por el canal del servidor
		c.out().write(VALOR);
	}
}