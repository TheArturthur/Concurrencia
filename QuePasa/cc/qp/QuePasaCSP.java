
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

import java.util.ArrayList;
import java.util.HashMap;

// TO DO: importad aquí lo que hayáis usado para implementar
//        el estado del recurso
import java.util.*;


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
        public int creadorUid;
        public String grupo;
        public int nuevoMiembroUid;
        // para tratamiento de la PRE
        public One2OneChannel chResp;

        public PetAnadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) {
            this.creadorUid = creadorUid;
            this.grupo = grupo;
            this.nuevoMiembroUid = nuevoMiembroUid;
            this.chResp = Channel.one2one();
        }
    }

    public class PetSalirGrupo {
        public int miembroUid;
        public String grupo;
        // para tratamiento de la PRE
        public One2OneChannel chResp;

        public PetSalirGrupo(int miembroUid, String grupo) {
            this.miembroUid = miembroUid;
            this.grupo = grupo;
            this.chResp = Channel.one2one();
        }
    }

    public class PetMandarMensaje {
        public int remitenteUid;
        public String grupo;
        public Object contenidos;
        // para tratamiento de la PRE
        public One2OneChannel chResp;

        public PetMandarMensaje(int remitenteUid, String grupo, Object contenidos) {
            this.remitenteUid = remitenteUid;
            this.grupo = grupo;
            this.contenidos = contenidos;
            this.chResp = Channel.one2one();
        }
    }

    public class PetLeer {
        public int uid;
        // para tratamiento de la PRE
        public One2OneChannel chResp;

        public PetLeer(int uid) {
            this.uid = uid;
            this.chResp = Channel.one2one();
        }
    }

    // Implementamos aquí los métodos de la interfaz QuePasa
    // os regalamos la implementación de crearGrupo
    public void crearGrupo(int creadorUid, String grupo)
            throws PreconditionFailedException
    {
        // creamos la petición
        PetCrearGrupo pet = new PetCrearGrupo(creadorUid, grupo);
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
        // creamos la petición
        PetAnadirMiembro pet = new PetAnadirMiembro(uid, grupo, nuevoMiembroUid);
        // la enviamos
        chAnadirMiembro.out().write(pet);
        // recibimos mensaje de status
        Boolean exito = (Boolean) pet.chResp.in().read();
        // si el estado de la petición es negativo, lanzamos excepción
        if (!exito)
            throw new PreconditionFailedException();
    }


    public void salirGrupo(int uid, String grupo)
            throws PreconditionFailedException
    {
        // creamos la petición
        PetSalirGrupo pet = new PetSalirGrupo(uid, grupo);
        // la enviamos
        chSalirGrupo.out().write(pet);
        // recibimos mensaje de status
        Boolean exito = (Boolean) pet.chResp.in().read();
        // si el estado de la petición es negativo, lanzamos excepción
        if (!exito)
            throw new PreconditionFailedException();
    }


    public void mandarMensaje(int remitenteUid, String grupo, Object contenidos)
            throws PreconditionFailedException
    {
        // creamos la petición
        PetMandarMensaje pet = new PetMandarMensaje(remitenteUid, grupo, contenidos);
        // la enviamos
        chMandarMensaje.out().write(pet);
        // recibimos mensaje de status
        Boolean exito = (Boolean) pet.chResp.in().read();
        // si el estado de la petición es negativo, lanzamos excepción
        if (!exito)
            throw new PreconditionFailedException();
    }


    public Mensaje leer(int uid)
    {
        // creamos la petición
        PetLeer pet = new PetLeer(uid);
        // la enviamos
        chPetLeer.out().write(pet);
        // devolvemos el mensaje que recibamos:
        return (Mensaje) pet.chResp.in().read();
    }



    // El servidor va en el método run()
    public void run() {

        // Mete aquí tu implementación del estado del recurso
        // (tráela de la práctica 1)

        //Map with the name of the group as key and value an ArrayList with the members ID
        //Value index 0 will ALWAYS be the creator's ID
        HashMap<String,ArrayList<Integer>> groupList = new HashMap<String,ArrayList<Integer>>();
        //Map with the user as key and the user's list of messages to read as value
        HashMap<Integer, ArrayList<Mensaje>> userMsg = new HashMap<Integer, ArrayList<Mensaje>>();

        // Colección para aplazar peticiones de leer
        // (adapta la que usaste en monitores, pero
        //  sustituye las Cond por One2OneChannel)

        // Map with the name of the group as key and value an ArrayList with the channel to read messages:
        HashMap<Integer, One2OneChannel> userChannel = new HashMap<Integer, One2OneChannel>();
        // Block list::
        LinkedList<One2OneChannel> toUnlock = new LinkedList<>();

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
                    if (groupList.containsKey(pet.grupo))
                        // status KO
                        pet.chResp.out().write(false);
                        // ejecución normal
                    else {
                        // operación
                        // TO DO: copia aquí tu implementación
                        //        de crearGrupo de la práctica 1
                        //If not, we create a new group with creadorUid as the creator in the first position of the array list:
                        ArrayList<Integer> members = new ArrayList<Integer>();
                        members.add(0,pet.creadorUid);
                        //Now if there's no message list for the user, we create one:
                        if(!userMsg.containsKey(pet.creadorUid)){
                            userMsg.put(pet.creadorUid, new ArrayList<Mensaje>());
                            //Creating also the user's condition:
                            userChannel.put(pet.creadorUid, pet.chResp);
                        }
                        //Now we add the group to the group list:
                        groupList.put(pet.grupo,members);
                        // status OK
                        pet.chResp.out().write(true);
                    }
                    break;
                }
                case ANADIR_MIEMBRO: {
                    // recepcion del mensaje
                    PetAnadirMiembro pet = (PetAnadirMiembro) chAnadirMiembro.in().read();
                    // comprobacion de la PRE
                    if (true)
                        // status KO
                        ;
                    // ejecución normal
                    else {
                        // ejecucion normal
                        //If everything is correct, we add the new member to the group:
                        groupList.get(pet.grupo).add(pet.nuevoMiembroUid);
                        //We then check if the new member has a message and condition list, and if not, we create them:
                        if(!userMsg.containsKey(pet.nuevoMiembroUid)){
                            userMsg.put(pet.nuevoMiembroUid, new ArrayList<Mensaje>());
                            userChannel.put(pet.nuevoMiembroUid, pet.chResp);
                        }
                    }
                    break;
                }
                case SALIR_GRUPO: {
                    // recepcion de la peticion
                    PetSalirGrupo pet = (PetSalirGrupo) chSalirGrupo.in().read();
                    // comprobacion de la PRE
                    if (true)
                        // status KO
                        ;
                    // ejecucion normal
                    else {
                        //If it's a member and not the creator, we remove it:
                        //We also have to remove the messages the user has of the group:

                        //To doing so, we get all the messages left for the user
                        for(int i = 0; i < userMsg.get(pet.miembroUid).size();){
                            //This variable will get us through all the messages on the list:
                            Mensaje msg = userMsg.get(pet.miembroUid).get(i);
                            //If the group of the message is the same group we're trying to exit from, we delete it:
                            if(msg.getGrupo().equals(pet.grupo)){
                                userMsg.get(pet.miembroUid).remove(msg);
                            }else{
                                //If it's not, we go to the next message on the list.
                                i++;
                                //We don't do it on top of the loop because, if we eliminate a message, the list moves backwards automatically, so we won't be checking all the messages
                            }
                        }
                        //Now, we remove the user from the member's list of the group:
                        groupList.get(pet.grupo).remove(groupList.get(pet.grupo).indexOf(pet.miembroUid));
                    }
                    break;
                }
                case MANDAR_MENSAJE: {
                    // recepcion de la peticion
                    PetMandarMensaje pet = (PetMandarMensaje) chMandarMensaje.in().read();
                    // comprobacion de la PRE
                    if (true)
                        // status KO
                        ;
                        // ejecución normal
                    else {
                        //We create the message to insert:
                        Mensaje message = new Mensaje(pet.remitenteUid, pet.grupo, pet.contenidos);
                        //For each user member of grupo, we add the message:
                        for (int pos = 0; pos < groupList.get(pet.grupo).size(); pos++) {
                            //We get the user in the position pos of the members list
                            int user = groupList.get(pet.grupo).get(pos);
                            //And we add the message to the user's list:
                            userMsg.get(user).add(message);
                        }
                    }
                    break;
                }
                case LEER: {
                    // recepcion de la peticion
                    PetLeer pet = (PetLeer) chPetLeer.in().read();
                    // no hay PRE que comprobar!
                    // TO DO: aquí lo más sencillo
                    // TO DO  es guardar la petición
                    // TO DO  según se recibe
                    // TO DO  (reutilizad la estructura que
                    // TO DO   usasteis en monitores
                    // TO DO   cambiando Cond por One2OneChannel)
                    //If there's no message list associated to the user, we create one:
                    if(!userMsg.containsKey(pet.uid)){
                        userMsg.put(pet.uid, new ArrayList<Mensaje>());
                        //We also create a list for the user's condition:
                        userChannel.put(pet.uid, pet.chResp);
                    }

                    if(userMsg.get(pet.uid).isEmpty()){
                        //If the user's message list is empty, get it awaiting:
                        toUnlock.add(userChannel.get(pet.uid));
                    }
                    //We get the first message of the list and remove it from the list:
                    Mensaje message = userMsg.get(pet.uid).remove(0);
                    pet.chResp.out().write(message);
                    break;
                }
            } // END SWITCH

            // código de desbloqueos
            // solo hay peticiones aplazadas de leer
            // TO DO: recorred la estructura
            //        con las peticiones aplazadas
            //        y responded a todas aquellas
            //        cuya CPRE se cumpla


            Object[] keys = userChannel.keySet().toArray();
            for (int i = 0; i< toUnlock.size(); i++) {
                //We get the user in position pos:
                Integer user = (Integer) keys[i];
                //We check the CPRE again (if the user has messages to read):
                if (!userMsg.get(user).isEmpty()) {
                    //If so, we now check if the user is waiting to be signaled:
                    if(userChannel.get(user).in().pending()) {
                        //As it is, we signal the user's thread:
                        toUnlock.remove(i);
                    }
                }
            }

        } // END while(true) SERVIDOR
    } // END run()
} // END class QuePasaCSP
