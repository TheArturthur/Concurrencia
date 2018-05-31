// Monitores
import es.upm.babel.cclib.Monitor;

@author Enzo Ferey

public class MiClaseMonitor
{
	// Atributos

	// Monitor
	private Monitor mutex = new Monitor();
	private Monitor.Cond condition =  mutex.newCond();
	private Monitor.Cond condition2 =  mutex.newCond();

	// Constructor

	public void metodo1()
	{
		mutex.enter();

		// CPRE
		if(!CPRE)
			condition.await();

		// Seccion critica

		desbloquear(); // Desbloqueo del thread que hizo await
		mutex.leave();
	}

	public void metodo2()
	{
		mutex.enter();

		// CPRE
		if(!CPRE)
			condition2.await();
		// Sección crítica

		desbloquear(); // Desbloqueo del thread que hizo await
		mutex.leave();
	}

	public void desbloquear()
	{
		// Comprobar CPRES y que condition.waiting() > 0

		// HACER UN SOLO SIGNAL !!
	}

}