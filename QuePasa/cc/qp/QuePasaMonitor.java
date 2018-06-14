package cc.qp;

import es.upm.babel.cclib.Monitor;
import sun.security.util.PendingException;

import java.util.*;

public class QuePasaMonitor implements QuePasa, Practica{


    private Monitor mutex;
    //Map with the name of the group as key and value an ArrayList with the members ID
    //Value index 0 will ALWAYS be the creator's ID
    private HashMap<String,ArrayList<Integer>> groupList;
    //Map with the name of the group as key and value an ArrayList with the conditions to read messages
    //Conditions sorted according to groupList members (i.e: condition for user in pos 0 (creator) will be in pos 0 as well)
    private HashMap<Integer, Monitor.Cond> userCond;
    private HashMap<Integer, ArrayList<Mensaje>> userMsg;
    private ArrayList<Integer> toSignal;

    public QuePasaMonitor(){
        this.mutex = new Monitor();
        this.groupList = new HashMap<String, ArrayList<Integer>>();
        this.userCond = new HashMap<Integer, Monitor.Cond>();
        this.userMsg = new HashMap<Integer, ArrayList<Mensaje>>();
        this.toSignal = new ArrayList<Integer>();
    }
    private ArrayList<Integer> users;
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
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException
    {
        //We assure mutual exclusion before each method:
        mutex.enter();
        //We check if the group already exists:
        if(groupList.containsKey(grupo)){
            //If so, we throw the exception:
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //If not, we create a new group with creadorUid as the creator in the first position of the array list:
            ArrayList<Integer> members = new ArrayList<Integer>();
            members.add(0,creadorUid);
            groupList.put(grupo,members);
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
        //If grupo exists and its creator isn't creadorUid or nuevoMiembroUid is already a member, throw exception:
        if(!groupList.containsKey(grupo) || getOwner(grupo)!=creadorUid || groupList.get(grupo).contains(nuevoMiembroUid)){
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //grupo exists and nuevoMiembroUid is not a member:
            groupList.get(grupo).add(nuevoMiembroUid);
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
        if(!groupList.get(grupo).contains(miembroUid) || getOwner(grupo)==miembroUid){
            //If it's not a member or it's the creator of grupo, throw exception:
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //If it's a member and not the creator, we remove it:
            groupList.get(grupo).remove(miembroUid);
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
        if(!groupList.get(grupo).contains(remitenteUid)) {
            //If not, throw exception:
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //We create the message to insert:
            Mensaje message = new Mensaje(remitenteUid,grupo,contenidos);
            //For each user member of grupo, add the message:
            for (int user : groupList.get(grupo)){
                //If user isn't in userMsg, we add them:
                if(!userMsg.containsKey(user)){
                    userMsg.put(user,new ArrayList<Mensaje>());
                }
                userMsg.get(user).add(message);
                //We now create a condition for the user, if he didn't have one:
                if (!userCond.containsKey(user)) {
                    Monitor.Cond condition = mutex.newCond();
                    userCond.put(user,condition);
                    toSignal.add(user);
                }
            }
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
     * @return
     *
     */
    public Mensaje leer(int uid) {
        mutex.enter();
        Mensaje res = null;
        if (!userCond.containsKey(uid)) {
           Monitor.Cond condition = mutex.newCond();
            userCond.put(uid, condition);
        }
        //If there are no messages for uid, we block the condition:
        if(userMsg.size()==0 || userMsg.get(uid).isEmpty()){
            //We add the users to a FIFO list, to ensure reliability when signaling their conditions:
            toSignal.add(uid);
            userCond.get(uid).await();
        }else{
            //There are messages for uid.
            //We remove the first one and assign it to res:
            res = userMsg.get(uid).remove(0);
        }
        //Return the message and leaving the Monitor:
        mutex.leave();
        return res;
    }

    private void unlock(){
        int user = toSignal.remove(0);
        userCond.get(user).signal();
    }

    private int getOwner(String grupo){
        return groupList.get(grupo).get(0);
    }
}
