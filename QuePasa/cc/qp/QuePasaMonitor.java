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
    private HashMap<String, ArrayList<Monitor.Cond>> groupConds;
    private HashMap<Integer, ArrayList<Mensaje>> userMsg;

    public QuePasaMonitor(){
        this.mutex = new Monitor();
        this.groupList = new HashMap<String, ArrayList<Integer>>();
        this.groupConds = new HashMap<String, ArrayList<Monitor.Cond>>();
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
            Monitor.Cond condition = mutex.newCond();
            ArrayList<Monitor.Cond> conditionsList = new ArrayList<Monitor.Cond>();
            conditionsList.add(condition);
            groupConds.put(grupo,conditionsList);
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
        //If the creator of grupo isn't creadorUid (grupo doesn't exists in the list) or nuevoMiembroUid is already a member, throw exception:
        if(!groupList.containsKey(grupo) || groupList.get(grupo).contains(nuevoMiembroUid)){
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            //grupo exists and nuevoMiembroUid is not a member:
            groupList.get(grupo).add(nuevoMiembroUid);
            Monitor.Cond condition = mutex.newCond();
            groupConds.get(grupo).add(groupList.get(grupo).indexOf(nuevoMiembroUid),condition);
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
        if(groupList.get(grupo).contains(miembroUid) && groupList.get(grupo).get(0)!=miembroUid){
            //If it's a member and not the creator: we remove it:
            groupConds.get(grupo).remove(groupList.get(grupo).indexOf(miembroUid));
            groupList.get(grupo).remove(miembroUid);
            mutex.leave();
        }else{
            //If it's not a member or it's the creator of grupo, throw exception:
            mutex.leave();
            throw new PreconditionFailedException();
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
        if(!groupList.containsKey(grupo) || groupList.get(grupo).contains(remitenteUid)){
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            // We create the new message:
            Mensaje msg = new Mensaje(remitenteUid, grupo, contenidos);
            for (int i : groupList.get(grupo)) {
                //For each user inside the group member's list
                ArrayList<Mensaje> messages = userMsg.get(i);
                if (messages == null) {
                    //If user doesn't have message list, we create it in the Map.
                    messages = new ArrayList<Mensaje>();
                }
                messages.add(msg);
                userMsg.put(i, messages);

                //We now block the condition for each user in the group to read the messages:(¿NECESARIO?)
                //groupConds.get(grupo).get(groupList.get(grupo).indexOf(i)).await();
            }
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
        ArrayList<String> gruposDeUid = new ArrayList<String>();
        for(String grupo : groupList.keySet()){
            if(groupList.get(grupo).contains(uid)){
                gruposDeUid.add(grupo);
            }
        }
        return null;
    }
}
