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

    public QuePasaMonitor(){
        this.mutex= new Monitor();
        this.cond= mutex.newCond();
        this.groupList= new HashMap<>();
    }


    @Override
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException {
        //At the beginning of each method, we assure mutual exclusion:
        mutex.enter();
        ArrayList<Group> userGroups= groupList.get(creadorUid);
        Group group= checkGroup(grupo,userGroups);

        if(group!= null){
            mutex.leave();
            throw new PreconditionFailedException();
        }else{
            group= new Group(creadorUid, grupo);
            group.addMember(creadorUid);
            userGroups.add(group);
            groupList.put(creadorUid, userGroups);
        }
        mutex.leave();
    }

    @Override
    public void anadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) throws PreconditionFailedException {
        mutex.enter();
        ArrayList<Group> userGroups= groupList.get(creadorUid);
        Group group= checkGroup(grupo,userGroups);

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

        mutex.leave();

    }

    @Override
    public Mensaje leer(int uid) {
        mutex.enter();

        mutex.leave();
        return null;
    }

    private Group checkGroup(String name, ArrayList<Group> list) {
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
