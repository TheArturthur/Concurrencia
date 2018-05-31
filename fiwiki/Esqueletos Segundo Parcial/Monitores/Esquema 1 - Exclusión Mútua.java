// Monitores

@author Enzo Ferey

public class MiClaseMonitor
{
	// Atributos

	// Monitor
	private Monitor mutex = new Monitor();
	
	// Constructor

	public void metodo1()
	{
		mutex.enter();

		// Seccion critica

		mutex.leave();
	}

}