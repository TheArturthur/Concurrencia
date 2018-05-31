// importamos la librería JCSP
import org.jcsp.lang.*;

@author Enzo Ferey

// Clase del servidor
public class MiClaseJCSP implements CSProcess
{
	// Atributos para definir valores

	// Punto de recepción de los canales para almacenar y extraer
    private AltingChannelInput petTipo1;
    private AltingChannelInput petTipo2;

	// Para evitar la construcción de servidores huérfanos
    public MiClaseJCSP() {
    }

    // Pasamos el canal por el que recibimos las peticiones
    public MiClaseJCSP(final AltingChannelInput petTipo1, final AltingChannelInput petTipo2) {
	   this.petTipo1 = petTipo1;
	   this.petTipo2  = petTipo2;
    }

    // Código del servidor
    public void run() {
		
		// EL ESTADO DEL RECURSO ES PRIVADO AL THREAD DEL SERVIDOR !!!

		// Entradas de la select
	    final AltingChannelInput[] entradas = {petTipo1, petTipo2};
        // Recepción alternativa
	    final Alternative servicios =  new Alternative (entradas);
        // Sincronización condicional en la select
        final boolean[] sincCond = new boolean[2];

		while (true) {
			// Canal respuesta para petTipo2
 			ChannelOutput resp;

            // Preparación de las precondiciones
            sincCond[VALOR1] = CPRE1;
            sincCond[VALOR2] = CPRE2;

            switch (servicios.fairSelect(sincCond)) 
            {
                case VALOR1: 
                    // Operación correspondiente
                    break;
                case VALOR2:
                    // Operación correspondiente
                
                	resp = (ChannelOutput) petTipo2.read();
                	resp.write(VALOR);
                    break;
            }
		} // FIN BUCLE SERVICIO
    } // FIN SERVIDOR
}

// Clase del cliente para operación de tipo 2 (VALOR2 en el switch del servidor)
public class ClienteJCSP extends Thread
{
	 // por este punto de envío pediremos los datos...
    private ChannelOutput petExtraer;
    // enviando el canal por el que queremos que nos respondan:
    private One2OneChannel chResp = Channel.one2one();

    // Evitando construcciones incorrectas
    private ConsumidorCSP() {
    }

    public ConsumidorCSP(ChannelOutput petExtraer) {
        this.petExtraer = petExtraer;
    }

    public void run() {
        while (true) {
	        petExtraer.write(chResp.out());
	        TipoObjeto p = (TipoObjeto) chResp.in().read();
            // hacer algo con el objeto recibido
        }
    }
}