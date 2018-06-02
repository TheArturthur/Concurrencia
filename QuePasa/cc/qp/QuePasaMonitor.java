package cc.qp;

//Grupo: Arturo Vidal Peña (w140307)

import es.upm.babel.cclib.Monitor;

import javax.swing.*;
import java.time.chrono.MinguoChronology;
import java.util.*;

public class QuePasaMonitor implements QuePasa{

    private Monitor mutex;
    private Monitor.Cond cond;
    private HashMap<Integer,ArrayList<Group>> groupList;
    private HashMap<Integer,ArrayList<Mensaje>> userMsg;

    public QuePasaMonitor(){
        this.mutex= new Monitor();
        this.cond= mutex.newCond();
        this.groupList= new HashMap<>();
    }


    @Override
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException {
        //At the beginning of each method, we assure mutual exclusion:
        mutex.enter();
        if(!groupList.isEmpty()) {
            ArrayList<Group> userGroups = groupList.get(creadorUid);
            mutex.leave();
            Group group = checkGroup(grupo, userGroups);
            mutex.enter();
            if (group != null) {
                mutex.leave();
                throw new PreconditionFailedException();
            } else if(!userGroups.isEmpty()){
                group = new Group(creadorUid, grupo);
                group.addMember(creadorUid);
                userGroups.add(group);
                groupList.put(creadorUid, userGroups);
            }else{
                group = new Group(creadorUid, grupo);
                group.addMember(creadorUid);
                userGroups= new ArrayList<Group>();
                userGroups.add(group);
                groupList.put(creadorUid, userGroups);
            }
        }else{
            groupList.put(creadorUid,new ArrayList<>());
            crearGrupo(creadorUid,grupo);
        }
        mutex.leave();
    }

    @Override
    public void anadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) throws PreconditionFailedException {
        mutex.enter();
        ArrayList<Group> userGroups= groupList.get(creadorUid);
        mutex.leave();
        Group group= checkGroup(grupo,userGroups);
        mutex.enter();
        if(group== null || group.getMembers().contains(nuevoMiembroUid)){
            mutex.leave();
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
        ArrayList<Group> userGroups= groupList.get(miembroUid);
        Group group= checkGroup(grupo,userGroups);

        if(group== null || group.getCreatorId()==miembroUid){
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            group.getMembers().remove(miembroUid);
        }
        mutex.leave();
    }

    @Override
    public void mandarMensaje(int remitenteUid, String grupo, Object contenidos) throws PreconditionFailedException {
        mutex.enter();
        ArrayList<Group> userGroups= groupList.get(remitenteUid);
        Group group= checkGroup(grupo, userGroups);
        if(group!=null){
            Mensaje msg= new Mensaje (remitenteUid, grupo, contenidos);
            for(int i: group.getMembers()){
                if(i!=remitenteUid){
                    userMsg.get(i).set(1,msg);

                    leer(i);
                }
            }
        }else{
            mutex.leave();
            throw new PreconditionFailedException();
        }
        mutex.leave();

    }

    @Override
    public Mensaje leer(int uid) {
        mutex.enter();
        if(userMsg==null || userMsg.isEmpty()){
            mutex.leave();
            return null;
        }else {
            Mensaje res = userMsg.get(uid).get(1);
            userMsg.remove(1);
            mutex.leave();
            return res;
        }
    }

    private Group checkGroup(String name, ArrayList<Group> list) {
        mutex.enter();
        Group res= null;
        for(Group i: list){
            if(i.getName().equals(name)){
                res= i;
            }
        }
        mutex.leave();
        return res;
    }

    class Group{
        private int creatorId;
        private String name;
        private ArrayList<Integer> members;

        //Constructor:
        public Group(int creatorId, String name){
            this.creatorId= creatorId;
            this.name= name;
            this.members= new ArrayList<>();
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
}
