// Monitores
import es.upm.babel.cclib.Monitor;

@author Enzo Ferey

public class MiClaseMonitor
{
	// Atributos

	// Monitor
	private Monitor mutex = new Monitor();

	// Clase para representar peticiones aplazadas
    public class PeticionDetectar {
		// Atributos

		// Condition
		public Monitor.Cond c;
	
		// Constructor
		public PeticionDetectar() {
			// Inicializar atributos

			// Inicializar condition
		    this.c = mutex.newCond();
		}
    }

    // Cola de bloqueados
    Queue<PeticionDetectar> esperanDetectar = new NodeQueue<PeticionDetectar>();

	// Constructor
	public MiClaseMonitor()
	{}

	public void metodo1()
	{
		mutex.enter();

		// CPRE
		if(!CPRE)
		{
			PeticionDetectar pet = new PeticionDetectar(); //pasando parámetros que haga falta para los atributos
		    esperanDetectar.enqueue(pet);
		    pet.c.await();
		}

		// Seccion critica

		desbloquear(); // Desbloqueo del thread que hizo await
		mutex.leave();
	}

	public void metodo2()
	{
		mutex.enter();

		// CPRE
		if(!CPRE) // Si no se cumple la CPRE
		{
			PeticionDetectar pet = new PeticionDetectar(); //pasando parámetros que haga falta para los atributos
		    esperanDetectar.enqueue(pet);
		    pet.c.await();
		}

		// Sección crítica

		desbloquear(); // Desbloqueo del thread que hizo await
		mutex.leave();
	}

	public void desbloquear()
	{
		// Comprobar CPRES y que condition.waiting() > 0
		// HACER UN SOLO SIGNAL !!

		int n = esperanDetectar.size();
		boolean signaled = false;
		for (int i = 0; i < n && !signaled; i++) {
		    pet = esperanDetectar.front(); // o bien esperanDetectar.poll() y luego no hace falta hacer un dequeue();
		    if (CPRE) // Si se cumple la CPRE
		    {
				// sabemos que pet.c.waiting() == 1
				pet.c.signal();
				// eliminamos aquí la petición
				esperanDetectar.dequeue();
				// para salirnos del bucle...
				signaled = true;
		    }
		    else 
		    {
				// condition que no has de despertar, déjala pasar
				esperanDetectar.dequeue();
				esperanDetectar.enqueue(pet);
		    }
		}
	}
}