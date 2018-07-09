package cc.qp;

import es.upm.babel.cclib.Monitor;

import java.util.*;

public class QuePasaMonitor implements QuePasa, Practica{


    private Monitor mutex;
    //Map with the name of the group as key and value an ArrayList with the members ID
    //Value index 0 will ALWAYS be the creator's ID
    private HashMap<String,ArrayList<Integer>> groupList;
    //Map with the name of the group as key and value an ArrayList with the condition to read messages:
    private HashMap<Integer, Monitor.Cond> userCond;
    //Map with the user as key and the user's list of messages to read as value
    private HashMap<Integer, ArrayList<Mensaje>> userMsg;

    public QuePasaMonitor(){
        //We initialize the monitor we're going to use:
        this.mutex = new Monitor();
        //And the three HashMaps with empty ones:
        this.groupList = new HashMap<String, ArrayList<Integer>>();
        this.userCond = new HashMap<Integer, Monitor.Cond>();
        this.userMsg = new HashMap<Integer, ArrayList<Mensaje>>();
    }


    @Override
    public Alumno[] getAutores() {
        return new Alumno[]{
                new Alumno("Arturo Vidal Peña", "w140307")
        };
    }

    @Override
    /**
     *
     * <PRE>
     *      grupo NOT IN dom(self.creadorUid)
     * </PRE>
     *
     * @param creadorUid
     * @param grupo
     *
     * @throws PreconditionFailedException
     *
     */
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException {
        //We assure mutual exclusion before each method:
        mutex.enter();
        //We check if the group already exists:
        if(this.groupList.containsKey(grupo)){
            //If so, we throw the exception:
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //If not, we create a new group with creadorUid as the creator in the first position of the array list:
            ArrayList<Integer> members = new ArrayList<Integer>();
            members.add(0,creadorUid);
            //Now if there's no message list for the user, we create one:
            if(!userMsg.containsKey(creadorUid)){
                userMsg.put(creadorUid, new ArrayList<Mensaje>());
                //Creating also the user's condition:
                userCond.put(creadorUid, mutex.newCond());
            }
            //Now we add the group to the group list:
            this.groupList.put(grupo,members);
            //At the end of each method, we leave mutual exclusion:
            mutex.leave();
        }
    }

    @Override
    /**
     *
     * <PRE>
     *      self.creador(grupo) = creadoruid & nuevoMiembroUid NOT IN self.miembros(grupo)
     * </PRE>
     *
     * @param creadorUid
     * @param grupo
     * @param nuevoMiembroUid
     *
     * @throws PreconditionFailedException
     *
     */
    public void anadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) throws PreconditionFailedException {
        mutex.enter();
        //If grupo exists and its creator isn't creadorUid or nuevoMiembroUid is already a member, we throw exception:
        if(!this.groupList.containsKey(grupo) || getOwner(grupo)!=creadorUid || this.groupList.get(grupo).contains(nuevoMiembroUid)){
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //If everything is correct, we add the new member to the group:
            this.groupList.get(grupo).add(nuevoMiembroUid);
            //We then check if the new member has a message and condition list, and if not, we create them:
            if(!userMsg.containsKey(nuevoMiembroUid)){
                userMsg.put(nuevoMiembroUid, new ArrayList<Mensaje>());
                userCond.put(nuevoMiembroUid, mutex.newCond());
            }
            mutex.leave();
        }
    }

    @Override
    /**
     *
     * <PRE>
     *      miembroUid IN self.miembros(grupo) & miembroUid != self.creador(grupo)
     * </PRE>
     *
     * @param miembroUid
     * @param grupo
     *
     * @throws PreconditionFailedException
     *
     */
    public void salirGrupo(int miembroUid, String grupo) throws PreconditionFailedException {
        mutex.enter();
        //check if miembroUid is in grupo.members and is not the creator:
        if(this.groupList.isEmpty() || !this.groupList.containsKey(grupo) || !this.groupList.get(grupo).contains(miembroUid) || getOwner(grupo)==miembroUid){
            //If it's not a member or it's the creator of grupo, we throw exception:
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //If it's a member and not the creator, we remove it:
            //We also have to remove the messages the user has of the group:

            //To doing so, we get all the messages left for the user
            for(int i = 0; i < this.userMsg.get(miembroUid).size();){
                //This variable will get us through all the messages on the list:
                Mensaje msg = this.userMsg.get(miembroUid).get(i);
                //If the group of the message is the same group we're trying to exit from, we delete it:
                if(msg.getGrupo().equals(grupo)){
                    userMsg.get(miembroUid).remove(msg);
                }else{
                    //If it's not, we go to the next message on the list.
                    i++;
                    //We don't do it on top of the loop because, if we eliminate a message, the list moves backwards automatically, so we wouldn't be checking all the messages
                }
            }
            //Now, we remove the user from the member's list of the group:
            this.groupList.get(grupo).remove(this.groupList.get(grupo).indexOf(miembroUid));
            mutex.leave();
        }
    }

    @Override
    /**
     *
     * <PRE>
     *      remitenteUid IN grupo.miembros
     * </PRE>
     *
     * @param remitenteUid
     * @param grupo
     * @param contenidos
     *
     * @throws PreconditionFailedException
     *
     */
    public void mandarMensaje(int remitenteUid, String grupo, Object contenidos) throws PreconditionFailedException {
        mutex.enter();
        //We check if remitenteUid is a member of grupo
        if(this.groupList.isEmpty() || !this.groupList.containsKey(grupo) || !this.groupList.get(grupo).contains(remitenteUid)) {
            //If not, throw exception:
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //We create the message to insert:
            Mensaje message = new Mensaje(remitenteUid,grupo,contenidos);
            //For each user member of grupo, we add the message:
            for (int pos = 0; pos<this.groupList.get(grupo).size(); pos++){
                //We get the user in the position pos of the members list
                int user = groupList.get(grupo).get(pos);
                //And we add the message to the user's list:
                this.userMsg.get(user).add(message);
            }
            //Now we unlock the first user that has pending messages:
            unlock();
            mutex.leave();
        }
    }

    @Override
    /**
     *
     * <CPRE>
     *      self.mensajes(uid) != {}
     * </CPRE>
     *
     * @param uid
     *
     * @return message
     *
     */
    public Mensaje leer(int uid) {
        mutex.enter();
        //If there's no message list associated to the user, we create one:
        if(!this.userMsg.containsKey(uid)){
            this.userMsg.put(uid, new ArrayList<Mensaje>());
            //We also create a list for the user's condition:
            this.userCond.put(uid, mutex.newCond());
        }

        if(this.userMsg.get(uid).isEmpty()){
            //If the user's message list is empty, get it awaiting:
            this.userCond.get(uid).await();
        }
        //We get the first message of the list and remove it from the list:
        Mensaje message = this.userMsg.get(uid).remove(0);

        //Now we unlock the first user that has pending messages:
        unlock();
        mutex.leave();
        //And return the message read:
        return message;
    }

    private void unlock() {
        //We create a variable to exit the loop:
        boolean unlocked = false;
        //And an array with the users that are waiting:
        Object[] keys = this.userCond.keySet().toArray();
        for (int pos = 0; pos < keys.length && !unlocked; pos++) {
            //We get the user in position pos:
            Integer user = (Integer) keys[pos];
            //We check the CPRE again (if the user has messages to read):
            if (!this.userMsg.get(user).isEmpty()) {
                //If so, we now check if the user is waiting to be signaled:
                if(this.userCond.get(user).waiting()>0) {
                    //As it is, we signal the user's thread, and create a new condition for the user:
                    this.userCond.get(user).signal();
                    this.userCond.put(user,mutex.newCond());
                    //We update the boolean value to exit the loop, as we only unlock one user:
                    unlocked = true;
                }
            }
        }
    }

    private int getOwner(String grupo){
        //The owner of the group is always the first member of the group member's list:
        return this.groupList.get(grupo).get(0);
    }
}