/*
// QuePasaCSP.java
// Lars-Ake Fredlund y Julio Mariño
// Esqueleto para la realización de la práctica por paso de mensajes
// Completad las líneas marcadas con "TO DO"
// Solución basada en peticiones aplazadas
// Los huecos son orientativos: se basan en nuestra propia
// implementación (incluyendo comentarios).
package cc.qp;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.Any2OneChannel;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

// TO DO: importad aquí lo que hayáis usado para implementar
//        el estado del recurso
// TO DO
// TO DO
// TO DO 
// TO DO
// TO DO
// TO DO


public class QuePasaCSP implements QuePasa, CSProcess {

    // Creamos un canal por cada operación sin CPRE
    private Any2OneChannel chCrearGrupo =    Channel.any2one();
    private Any2OneChannel chAnadirMiembro = Channel.any2one();
    private Any2OneChannel chSalirGrupo =    Channel.any2one();
    private Any2OneChannel chMandarMensaje = Channel.any2one();
    // Creamos un canal para solicitar leer
    // Usaremos peticiones aplazadas en el servidor para tratar
    // la CPRE de leer
    private Any2OneChannel chPetLeer = Channel.any2one();

    public QuePasaCSP() { }

    // clases auxiliares para realizar peticiones al servidor
    // os regalamos la implementación de CrearGrupo
    public class PetCrearGrupo {
	public int creadorUid;
	public String grupo;
	// para tratamiento de la PRE
	public One2OneChannel chResp;

	public PetCrearGrupo(int creadorUid, String grupo) {
	    this.creadorUid = creadorUid;
	    this.grupo = grupo;
	    this.chResp = Channel.one2one();
	}
    }

    public class PetAnadirMiembro {
	// TO DO: atributos de la clase
	// TO DO
	// TO DO
	// TO DO
	// TO DO

	public PetAnadirMiembro(
 TO DO: parámetros
) {
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	}
    }

    public class PetSalirGrupo {
	// TO DO : atributos de la clase
	// TO DO
	// TO DO
	// TO DO

	public PetSalirGrupo(
 TO DO: parámetros
) {
	    // TO DO
	    // TO DO 
	    // TO DO 
	}
    }

    public class PetMandarMensaje {
	// TO DO: atributos de la clase 
	// TO DO 
	// TO DO
	// TO DO
	// TO DO

	public PetMandarMensaje(
 TO DO: parámetros
) {
	    // TO DO 
	    // TO DO
	    // TO DO 
	    // TO DO
	}
    }

    public class PetLeer {
	// TO DO: atributos de la clase
	// TO DO
	// TO DO
	
	public PetLeer(
 		// TO DO: parámetros
) {
	    // TO DO
	    // TO DO
	}
    }
    
    // Implementamos aquí los métodos de la interfaz QuePasa
    // os regalamos la implementación de crearGrupo
    public void crearGrupo(int creadorUid, String grupo) 
	throws PreconditionFailedException
    {
	// creamos la petición
	PetCrearGrupo pet = new PetCrearGrupo(creadorUid,grupo);
	// la enviamos
	chCrearGrupo.out().write(pet);
	// recibimos mensaje de status
	Boolean exito = (Boolean) pet.chResp.in().read();
	// si el estado de la petición es negativo, lanzamos excepción
	if (!exito)
	    throw new PreconditionFailedException();
    }

    public void anadirMiembro(int uid, String grupo, int nuevoMiembroUid) 
	throws PreconditionFailedException
    {
	// TO DO
	// TO DO
	// TO DO
	// TO DO
	// TO DO 
    }
  
    public void salirGrupo(int uid, String grupo) 
	throws PreconditionFailedException
    {
	// TO DO
	// TO DO 
	// TO DO
	// TO DO
	// TO DO
    }

    public void mandarMensaje(int remitenteUid, String grupo, Object contenidos)
	throws PreconditionFailedException
    {
	// TO DO
	// TO DO
	// TO DO
	// TO DO 
	// TO DO
    }
  
    public Mensaje leer(int uid)
    {
	// TO DO
	// TO DO
	// TO DO
	// TO DO
    }


    // El servidor va en el método run()
    public void run() {

	// Mete aquí tu implementación del estado del recurso
	// (tráela de la práctica 1)
	// TO DO
	// TO DO
	// TO DO
	// TO DO
	// TO DO
	// TO DO
	// Colección para aplazar peticiones de leer
	// (adapta la que usaste en monitores, pero
	//  sustituye las Cond por One2OneChannel)
	// TO DO
	// TO DO

	// Códigos de peticiones para facilitar la asociación
	// de canales a operaciones 
	final int CREAR_GRUPO    = 0;
	final int ANADIR_MIEMBRO = 1;
	final int SALIR_GRUPO    = 2;
	final int MANDAR_MENSAJE = 3;
	final int LEER           = 4;

	// recepción alternativa
	final Guard[] guards = new AltingChannelInput[5];
	guards[CREAR_GRUPO]    = chCrearGrupo.in();
	guards[ANADIR_MIEMBRO] = chAnadirMiembro.in();
	guards[SALIR_GRUPO]    = chSalirGrupo.in();
	guards[MANDAR_MENSAJE] = chMandarMensaje.in();
	guards[LEER]           = chPetLeer.in();

	final Alternative services = new Alternative(guards);
	int chosenService;

	while (true) {
	    // toda recepcion es incondicional
	    chosenService = services.fairSelect();
	    switch (chosenService) {
		// regalamos la implementación del servicio de crearGrupo
	    case CREAR_GRUPO: {
		// recepción del mensaje
		PetCrearGrupo pet = (PetCrearGrupo) chCrearGrupo.in().read();
		// comprobación de la PRE
		if (
 TO DO: copia aquí tu implementación de la PRE
)
		    // status KO
		    pet.chResp.out().write(false);
		// ejecución normal
		else {
		    // operación
		    // TO DO: copia aquí tu implementación
		    //        de crearGrupo de la práctica 1
		    // TO DO
		    // TO DO
		    // TO DO
		    // TO DO
		    // TO DO
		    // status OK
		    pet.chResp.out().write(true);
		}
		break;
	    }
	    case ANADIR_MIEMBRO: {
		// recepcion del mensaje
		// TO DO
		// comprobacion de la PRE
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// ejecucion normal
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		break;
	    }
	    case SALIR_GRUPO: {
		// recepcion de la peticion
		// TO DO
		// comprobacion de la PRE
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// ejecucion normal
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		break;
	    }
	    case MANDAR_MENSAJE: {
		// recepcion de la peticion
		// TO DO
		// comprobacion de la PRE
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// ejecucion normal
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		break;
	    }
	    case LEER: {
		// recepcion de la peticion
		// TO DO
		// no hay PRE que comprobar!
		// TO DO: aquí lo más sencillo 
		// TO DO  es guardar la petición
		// TO DO  según se recibe
		// TO DO  (reutilizad la estructura que 
		// TO DO   usasteis en monitores
		// TO DO   cambiando Cond por One2OneChannel)
		// TO DO
		// TO DO
		// TO DO
		// TO DO
		break;
	    }
	    } // END SWITCH

	    // código de desbloqueos
	    // solo hay peticiones aplazadas de leer
	    // TO DO: recorred la estructura 
	    //        con las peticiones aplazadas
	    //        y responded a todas aquellas 
	    //        cuya CPRE se cumpla
	    // TO DO
	    // TO DO
            // TO DO
	    // TO DO
            // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	    // TO DO
	} // END while(true) SERVIDOR
    } // END run()
} // END class QuePasaCSP
  
*/
