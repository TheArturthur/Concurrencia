// Monitores
import es.upm.babel.cclib.Monitor;

@author Enzo Ferey

public class MiClaseMonitor
{
	// Atributos

	// Monitor
	private Monitor mutex = new Monitor();
	private Monitor.Cond condition =  mutex.newCond();

	// Constructor

	public void metodo1()
	{
		mutex.enter();

		// CPRE
		if(!CPRE)
			condition.await();

		// Seccion critica

		mutex.leave();
	}

	public void metodo2()
	{
		mutex.enter();

		// Sección crítica

		condition.signal(); // Desbloqueo del thread que hizo await
		mutex.leave();
	}

}