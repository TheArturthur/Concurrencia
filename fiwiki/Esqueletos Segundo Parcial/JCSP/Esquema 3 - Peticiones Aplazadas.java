// importamos la librería JCSP
import org.jcsp.lang.*;

@author Enzo Ferey

// Clase del servidor
public class MiClaseJCSP implements CSProcess
{
	// Atributos

	// Canales de comunicación
    private Any2OneChannelInt chAccion1 = Channel.any2oneInt();
    private Any2OneChannel chAccion2 = Channel.any2one();

	// Para evitar la construcción de servidores huérfanos
    public MiClaseJCSP() {
    }

    // Ejecutado por el cliente
    public void accion1(int t) {
		chAccion1.out().write(t);
    }

    // Ejecutado por el cliente
    public int accion2(int min, int max) {
		One2OneChannelInt sincro = Channel.one2oneInt();
		Object[] pet = {new Integer(min), new Integer(max), sincro};
		chAccion2.out().write(pet);
		return sincro.in().read();
    }

    // Código del servidor
    public void run() {
		
		// Estado del recurso

		// Cola de bloqueados
		Queue<Object[]> esperando = new NodeQueue<Object[]>();

		// Preparando la recepción no determinista
		// Definir valores
		final int ACCION1 = 0;
		final int ACCION2  = 1;

		Guard[] entradas = {chAccion1.in(), chAccion2.in()};
		Alternative servicios = new Alternative(entradas);

		// Variables auxiliares
		Object[] pet;
		One2OneChannel chResp;

		// Bucle principal del servidor
		while (true) {            
	            // Recepción no determinista
	            switch (servicios.fairSelect()) {
		            case ACCION1:
						// Operación correspondiente
					break;

		            case ACCION2:
		            	// Operación correspondiente
						pet = (Object[]) chAccion2.in().read();
						esperando.enqueue(pet);
						break;
	            }

		        // Código de desbloqueo
		        int n = esperando.size();
		        for (int i = 0; i < n; i++) {
					pet = esperando.front(); // o esperando.poll();
					min = ((Integer) pet[0]).intValue();
					max = ((Integer) pet[1]).intValue();
					chResp = ((One2OneChannel) pet[2]);
					if (CPRE) {
					    chResp.out().write(a);
					}
					else {
					    esperando.dequeue();
					    esperando.enqueue(pet);
					}
	            }
		}
    }
}