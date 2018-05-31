// importamos la librería JCSP
import org.jcsp.lang.*;

@author Enzo Ferey

// Clase del servidor
public class MiClaseJCSP implements CSProcess
{
	// Atributos

	// un solo canal para la operación accion1(t)
    private Any2OneChannelInt chAccion1;
    // un canal para cada llamada accion2(min,max)
    private Any2OneChannel[][] chAccion2;

    public MiClaseJCSP() {
		chAccion1 = Channel.any2oneInt();
		chAccion2 = new Any2OneChannel[50][50];

		for (int min = 0; min < 50; min++)
		    for (int max = min; max < 50; max++)
				chAccion2[min][max] = Channel.any2one();
	    }
	}
    // Ejecutado por el cliente
    public void accion1(int t) {
		chAccion1.out().write(t);
    }

    // Ejecutado por el cliente
    public int accion2(int min, int max) {
		// creamos un canal de respuesta para recibir la temperatura
		One2OneChannelInt c = Channel.one2oneInt();
		// enviamos la peticion (c) por el canal (min,max)
		chAccion2[min][max].out().write(c);
		// esperamos que el servidor nos conteste con la temperatura
		return c.in().read();
    }

    // Código del servidor
    public void run() {
		
		// Estado del recurso

		// nos vamos a crear una recepción alternativa con una burrada de canales,
		// y algunos ni se usan, pero solo se trata de ilustrar la tecnica de la
		// replicacion de canales
		Guard[] entradas = new Guard[50*50+1];

		// reservamos las 2500 primeras entradas para detectar acciones del tipo2
		for (int min = 0; min < 50; min++)
		    for (int max = min; max < 50; max++)
				entradas[50*min+max] = chAccion2[min][max].in();
		
		// ...y la 2501 para accion del tipo1
		entradas[50*50] = chAccion1.in();

		// construimos la recepcion alternativa
		Alternative servicios = new Alternative (entradas);
		// creamos el vector de booleanos para las CPREs
		boolean[] sincCond = new boolean[50*50+1];

		
		// bucle del servidor
		while (true) {
		    // actualizamos las condiciones de recepcion (sincCond)
		    for (int min = 0; min < 50; min++)
				for (int max = min; max < 50; max++)
			    	sincCond[50*min+max] = CPRE;
		    
		    sincCond[50*50] = true; // como no hay CPRE para accion1 se puede poner true directamente

		    // la select
		    int entrada = servicios.fairSelect(sincCond);
		    // respuesta decodificando el resultado devuelto por la select
		    switch (entrada) {
		    	case 50*50: // es petición de acción 1
					// Operación correspondiente
					break;

		    	default: // es una petición de acción 2, ¿pero cuál?
					// Calculo de los índices correspondientes
					One2OneChannel c = (One2OneChannel) chAccion2[m][n].in().read();
					// devolvemos la temperatura
					c.out().write(ALGO);
					break;
		    }
		}
    }
}