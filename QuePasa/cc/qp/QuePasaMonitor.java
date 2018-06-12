package cc.qp;

import es.upm.babel.cclib.Monitor;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.*;

public class QuePasaMonitor implements QuePasa, Practica {

    private Monitor mutex;
    //Map with key UID and value an ArrayList of the user conditions.
    private HashMap<Integer, ArrayList<Monitor.Cond>> userCond;
    //Map with key UID and value an ArrayList of groups the user is in.
    private HashMap<Integer, ArrayList<Group>> groupList;
    //Map with key UID and value an ArrayList of messages the user has to read.
    private HashMap<Integer, ArrayList<Mensaje>> userMsg;

    public QuePasaMonitor() {
        this.mutex = new Monitor();
        this.userCond = new HashMap<Integer, ArrayList<Monitor.Cond>>();
        this.groupList = new HashMap<Integer, ArrayList<Group>>();
        this.userMsg = new HashMap<Integer, ArrayList<Mensaje>>();
    }


    @Override
    public void crearGrupo(int creadorUid, String grupo) throws PreconditionFailedException {
        //At the beginning of each method, we assure mutual exclusion:
        mutex.enter();
        if (!groupList.isEmpty()) {
            ArrayList<Group> userGroups = groupList.get(creadorUid);
            Group group = checkGroup(grupo, userGroups);
            if (group != null) {
                mutex.leave();
                throw new PreconditionFailedException();
            } else if (userGroups != null && !userGroups.isEmpty()) {
                group = new Group(creadorUid, grupo);
                group.addMember(creadorUid);
                userGroups.add(group);
                groupList.put(creadorUid, userGroups);
            } else {
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
        mutex.leave();
    }

    @Override
    public void anadirMiembro(int creadorUid, String grupo, int nuevoMiembroUid) throws PreconditionFailedException {
        mutex.enter();
        ArrayList<Group> userGroups = groupList.get(creadorUid);

        //we check if the group was created by creadorUid:
        mutex.leave();
        Group group = checkGroup(grupo, userGroups);
        mutex.enter();

        if (group == null || group.getMembers().contains(nuevoMiembroUid)) {
            mutex.leave();
            throw new PreconditionFailedException();
        } else {
            //if nuevoMiembroUid isn't a member, we add it to the member list and the group to its group list:
            group.addMember(nuevoMiembroUid);
            userGroups.add(group);
            groupList.put(creadorUid, userGroups);
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
        Group group = checkGroup(grupo, userGroups);
        if (group != null) {//If group exists:
            // We create the new message:
            Mensaje msg = new Mensaje(remitenteUid, grupo, contenidos);
            for (int i : group.getMembers()) {
                //For each user inside the group member's list
                if (i != remitenteUid) {
                    //not being the message sender
                    ArrayList<Mensaje> messages = userMsg.get(i);
                    if (messages == null) {
                        //If user doesn't have message list, we create it in the Map.
                        messages = new ArrayList<Mensaje>();
                    }
                    messages.add(msg);
                    userMsg.put(i, messages);
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
        if (userMsg.get(uid) == null || userMsg.get(uid).isEmpty()) {//!CPRE
            mutex.leave();
            return null;
        } else {
            if (!userCond.containsKey(uid)) {//if user hasn't a map for conditions created:
                ArrayList<Monitor.Cond> conditions = new ArrayList<Monitor.Cond>();
                userCond.put(uid, conditions);
            }
            //now that every user has a map for conditions, we check whether it's empty or not:
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
    }

    /**
     * @param uid
     * @return unlocks the first condition on the conditions array list for the uid
     */
    private void unlock(int uid) {
        Monitor.Cond cond = userCond.get(uid).remove(0);
        cond.signal();
    }

    /**
     * @param name
     * @param list
     * @return checks if the group exists for the user, if it does and the user is a member, it returns the group.
     */
    private Group checkGroup(String name, ArrayList<Group> list) {
        Group res = null;
        if (list != null && !list.isEmpty()) {
            for (Group i : list) {
                if (i.getName().equals(name)) {
                    res = i;
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
}

