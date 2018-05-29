package cc.qp;

//Grupo: Arturo Vidal Peña (w140307)

import es.upm.babel.cclib.Monitor;

import java.time.chrono.MinguoChronology;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QuePasaMonitor implements QuePasa{

    class Group{
        private int creatorId;
        private String name;
        private List<Integer> members;

        //Constructor:
        public Group(int creatorId, String name){
            this.creatorId= creatorId;
            this.name= name;
        }

        //Getters:
        public int getCreatorId(){
            return creatorId;
        }
        public String getName(){
            return name;
        }
        public List<Integer> getMembers() {
            return members;
        }

        //Setters:
        public void addMember(int userId){
            members.add(userId);
        }
    }

    //Variable to store creators mapped with their groups names:
    private Map<Integer,List<String>> groupNames;

    //Variables to assure mutual exclusion:
    private Monitor mutex;
    private Monitor.Cond cond;


    @Override
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException {
        //At the beginning of each method, we assure mutual exclusion:
        mutex.enter();
        //List with the groups of creatorUid:
        List<String> userGroups= groupNames.get(creadorUid);
        //If the creator has another group with the same name, throw exception:
        if(userGroups.contains(grupo)){
            throw new PreconditionFailedException();
        }
        //If not, create a group and add it to the creator's list:
        else{
            Group group= new Group(creadorUid, grupo);
            userGroups.add(grupo);
            groupNames.put(creadorUid, userGroups);
        }
        mutex.leave();
    }

    @Override
    public void anadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) throws PreconditionFailedException {
        mutex.enter();
        List<String> userGroups= groupNames.get(creadorUid);
        if(userGroups.contains(grupo)){

        }
        mutex.leave();
    }

    @Override
    public void salirGrupo(int miembroUid, String grupo) throws PreconditionFailedException {
        mutex.enter();

        mutex.leave();
    }

    @Override
    public void mandarMensaje(int remitenteUid, String grupo, Object contenidos) throws PreconditionFailedException {
        mutex.enter();

        mutex.leave();

    }

    @Override
    public Mensaje leer(int uid) {
        mutex.enter();

        mutex.leave();
        return null;
    }
}
