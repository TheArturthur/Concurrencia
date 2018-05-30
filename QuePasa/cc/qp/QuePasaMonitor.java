package cc.qp;

//Grupo: Arturo Vidal Peña (w140307)

import es.upm.babel.cclib.Monitor;
import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QuePasaMonitor implements QuePasa{

    //Variable to store creators mapped with their groups (shared memory variable):
    private Map<Integer,List<Group>> groupList;

    //Variables to assure mutual exclusion:
    private Monitor mutex= new Monitor();
    private Monitor.Cond cond= mutex.newCond();


    @Override
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException {
        //At the beginning of each method, we assure mutual exclusion:
        mutex.enter();
        List<Group> userGroups= groupList.get(creadorUid);
        Group group= checkGroup(grupo,userGroups);

        if(group!= null){
            throw new PreconditionFailedException();
        }else{
            group= new Group(creadorUid, grupo);
            userGroups.add(group);
            groupList.put(creadorUid, userGroups);
        }
        mutex.leave();
    }

    @Override
    public void anadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) throws PreconditionFailedException {
        mutex.enter();
        List<Group> userGroups= groupList.get(creadorUid);
        Group group= checkGroup(grupo,userGroups);

        if(group== null || group.getMembers().contains(nuevoMiembroUid)){
            throw new PreconditionFailedException();
        }else{
            group.addMember(nuevoMiembroUid);
            userGroups.add(group);
            groupList.put(creadorUid,userGroups);
        }
        mutex.leave();
    }

    @Override
    public void salirGrupo(int miembroUid, String grupo) throws PreconditionFailedException {
        mutex.enter();
        List<Group> userGroups= groupList.get(miembroUid);
        Group group= checkGroup(grupo,userGroups);

        if(group== null || group.getCreatorId()==miembroUid){
            throw new PreconditionFailedException();
        }else{
            group.getMembers().remove(miembroUid);
        }
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

    private Group checkGroup(String name, List<Group> list) {
        Group res= null;
        for(Group i: list){
            if(i.getName().equals(name)){
                res= i;
            }
        }
        return res;
    }

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
            return this.creatorId;
        }
        public String getName(){
            return this.name;
        }
        public List<Integer> getMembers() {
            return this.members;
        }

        //Setters:
        public void addMember(int userId){
            this.members.add(userId);
        }
    }
}
