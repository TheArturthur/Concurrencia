// Monitores
import es.upm.babel.cclib.Monitor;

@author Enzo Ferey

public class MiClaseMonitor
{
	// Atributos

	// Monitor
	private Monitor mutex = new Monitor();
	private Monitor.Cond c[][] = new Monitor.Cond [X][Y];

	// Constructor
	public MiClaseMonitor()
	{
		// Inicialización conditions
		for (int min = 0; min < X; min++)
		    for (int max = 0; max < Y; max++)
				c[min][max] = mutex.newCond ();
	}

	public void metodo1()
	{
		mutex.enter();

		// CPRE
		if(!CPRE)
			c[m][n].await();

		// Seccion critica

		desbloquear(); // Desbloqueo del thread que hizo await
		mutex.leave();
	}

	public void metodo2()
	{
		mutex.enter();

		// CPRE
		if(!CPRE)
			c[m][n].await();
		
		// Sección crítica

		desbloquear(); // Desbloqueo del thread que hizo await
		mutex.leave();
	}

	public void desbloquear()
	{
		// Comprobar CPRES y que condition.waiting() > 0
		// HACER UN SOLO SIGNAL !!

		boolean signaled = false;
		for (int min = 0; min < X && !signaled; min++){
		    for (int max = min; max < Y && !signaled; max++) {
				if (CPRE && c[min][max].waiting() > 0){
				    c[min][max].signal();					
				    signaled = true;
				}
		    }
	    }
	}
}