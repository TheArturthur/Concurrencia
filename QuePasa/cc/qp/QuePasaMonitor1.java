/*
package cc.qp;

import es.upm.babel.cclib.Monitor;

import java.util.*;

public class QuePasaMonitor1 implements QuePasa, Practica {

    private Monitor mutex;
    //Map with key UID and value an ArrayList of the user conditions.
    private HashMap<Integer, ArrayList<Monitor.Cond>> userCond;
    //Map with key UID and value an ArrayList of groups the user is in.
    private HashMap<Integer, ArrayList<Group>> groupList;
    //Map with key UID and value an ArrayList of messages the user has to read.
    private HashMap<Integer, ArrayList<Mensaje>> userMsg;

    public QuePasaMonitor1() {
        //Initialize the class variables:
        this.mutex = new Monitor();
        this.userCond = new HashMap<Integer, ArrayList<Monitor.Cond>>();
        this.groupList = new HashMap<Integer, ArrayList<Group>>();
        this.userMsg = new HashMap<Integer, ArrayList<Mensaje>>();
    }


    @Override
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException {
        //At the beginning of each method, we assure mutual exclusion:
        mutex.enter();
        //We now check if the user already is in a group with the same name (A.K.A same group):
        if (!groupList.isEmpty()) {
            ArrayList<Group> userGroups = groupList.get(creadorUid);
            //checkGroup will give us an Group-type object if it is in user's group list (null if not):
            Group group = checkGroup(grupo, userGroups);
            //if the user is in a group with the same name, we throw the exception:
            if (group != null) {
                mutex.leave();
                throw new PreconditionFailedException();
            } else if (userGroups != null && !userGroups.isEmpty()) {
                group = new Group(creadorUid, grupo);
                group.addMember(creadorUid);
                userGroups.add(group);
                groupList.put(creadorUid, userGroups);
            } else {
                //if user's group list is empty or doesn't exists, we create a new list, add the group to the list
                //and add the list to the map groupList
                group = new Group(creadorUid, grupo);
                group.addMember(creadorUid);
                userGroups = new ArrayList<Group>();
                userGroups.add(group);
                groupList.put(creadorUid, userGroups);
            }
        } else {
            groupList.put(creadorUid, new ArrayList<Group>());
            crearGrupo(creadorUid, grupo);
        }
        //At the end of each method, we leave the mutual exclusion:
        mutex.leave();
    }

    @Override
    public void anadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) throws PreconditionFailedException {
        mutex.enter();
        ArrayList<Group> userGroups = groupList.get(creadorUid);

        //we check if the group was created by creadorUid:
        Group group = checkGroup(grupo, userGroups);

        if (group == null || group.getMembers().contains(nuevoMiembroUid)) {
            mutex.leave();
            throw new PreconditionFailedException();
        } else {
            //if nuevoMiembroUid isn't a member, we add it to the member list and the group to its group list:
            group.addMember(nuevoMiembroUid);
            userGroups.add(group);
            groupList.get(creadorUid).add(group);
            groupList.put(creadorUid, userGroups);
            ArrayList<Monitor.Cond> conds = userCond.get(nuevoMiembroUid);
            Monitor.Cond newCond = mutex.newCond();
            conds.add(newCond);
            userCond.put(nuevoMiembroUid,conds);
            userCond.get(nuevoMiembroUid).get(userCond.get(nuevoMiembroUid).size()-1).await();
        }
        mutex.leave();
    }

    @Override
    public void salirGrupo(int miembroUid, String grupo) throws PreconditionFailedException {
        mutex.enter();
        ArrayList<Group> userGroups = groupList.get(miembroUid);
        Group group = checkGroup(grupo, userGroups);

        if (group == null || group.getCreatorId() == miembroUid) {
            mutex.leave();
            throw new PreconditionFailedException();
        } else {
            //if the group has the user as a member, it removes it from the list:
            group.getMembers().remove(miembroUid);
        }
        mutex.leave();
    }

    @Override
    public void mandarMensaje(int remitenteUid, String grupo, Object contenidos) throws PreconditionFailedException {
        mutex.enter();
        //We get the user's group list:
        ArrayList<Group> userGroups;
        userGroups = groupList.get(remitenteUid);
        //We check and get the group in the user's group list:
        if (userGroups == null) {
            mutex.leave();
            throw new PreconditionFailedException();
        }
        //We now have the group object:
        Group group = checkGroup(grupo, userGroups);
        //If this group exists:
        if (group != null) {
            // We create the new message:
            Mensaje msg = new Mensaje(remitenteUid, grupo, contenidos);
            for (int i : group.getMembers()) {
                //For each user inside the group member's list
                ArrayList<Mensaje> messages = userMsg.get(i);
                if (messages == null) {
                    //If user doesn't have message list, we create it in the Map.
                    messages = new ArrayList<Mensaje>();
                }
                messages.add(msg);
                userMsg.put(i, messages);
                if(!userCond.get(remitenteUid).isEmpty()) {
                    unlock(remitenteUid);
                }
            }
        } else {
            mutex.leave();
            throw new PreconditionFailedException();
        }
        mutex.leave();
    }

    @Override
    public Mensaje leer(int uid) {
        mutex.enter();
        //first, we must check if the user has a map for conditions created. If not, we create it:
        if (!userCond.containsKey(uid)) {
            ArrayList<Monitor.Cond> conditions = new ArrayList<Monitor.Cond>();
            userCond.put(uid, conditions);
        }
        //now that every user has a map for conditions, we check whether the user has messages or not:
        //if not, we'll lock the method until the user has a message to read.

        if (userMsg.get(uid) == null || userMsg.get(uid).isEmpty()) {//!CPRE
            Monitor.Cond condition= mutex.newCond();
            condition.await();
            userCond.get(uid).add(condition);
        }
        if (!userCond.get(uid).isEmpty()) {
            //if not empty, we create a new condition and add it to the user conditions array list:
            Monitor.Cond cond = mutex.newCond();
            cond.await();
            ArrayList<Monitor.Cond> conditions = userCond.get(uid);
            conditions.add(cond);
            userCond.put(uid, conditions);
            //then we unlock the first condition on the array list:
            unlock(uid);
        }
        //either if it was empty or not, we remove and return the message:
        mutex.leave();
        return userMsg.get(uid).remove(0);
    }

    */
/**
     * @param uid
     * @return unlocks the first condition on the conditions array list for the uid
     *//*

    private void unlock(int uid) {
        Monitor.Cond cond = userCond.get(uid).remove(0);
        cond.signal();
    }

    */
/**
     * @param name
     * @param list
     * @return checks if the group exists for the user, if it does and the user is a member, it returns the group.
     *//*

    private Group checkGroup(String name, ArrayList<Group> list) {
        Group res = null;
        if (list != null && !list.isEmpty()) {
            for (Group i : list) {
                if (i.getName().equals(name)) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public Alumno[] getAutores() {
        return new Alumno[]{
                new Alumno("Arturo Vidal Peña", "w140307")
        };
    }

    class Group {
        private int creatorId;
        private String name;
        private ArrayList<Integer> members;

        //Constructor:
        public Group(int creatorId, String name) {
            this.creatorId = creatorId;
            this.name = name;
            this.members = new ArrayList<Integer>();
        }

        //Getters:
        public int getCreatorId() {
            return creatorId;
        }

        public String getName() {
            return name;
        }

        public List<Integer> getMembers() {
            return members;
        }

        //Setters:
        public void addMember(int userId) {
            members.add(userId);
        }
    }
}*/
