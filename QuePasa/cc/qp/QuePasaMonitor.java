package cc.qp;

//Grupo: Arturo Vidal Peña (w140307)

import es.upm.babel.cclib.Monitor;

import java.time.chrono.MinguoChronology;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QuePasaMonitor implements QuePasa{

    //Class Group for connecting group id, group name and a list of members for each group:
    class Group{
        public int uidCreador;
        public String nombre;
        public List<Integer> miembros;

        //Class constructor:
        public Group(int uidCreador, String nombre){
            this.uidCreador= uidCreador;
            this.nombre= nombre;
            this.miembros.add(uidCreador);
        }

        public Integer[] getMiembros(){
            return (Integer[]) miembros.toArray();
        }

        public int getId(){
            return uidCreador;
        }

        public String getNombre (){
            return nombre;
        }
    }

    //Monitor and method conditions:
    private Monitor mutex;
    private Monitor.Cond condLeer;
    private Group group;
    private Map<Integer, String> groupList;

    //Class constructor
    public QuePasaMonitor(){
        mutex= new Monitor();
        condLeer= mutex.newCond();
    }


    public void crearGrupo (int uid, String grupo) throws PreconditionFailedException{
        //Start, to assure mutual exclusion:
        mutex.enter();
        try {
            if(!groupList.isEmpty() && groupList.containsKey(uid)){
                throw new PreconditionFailedException();
            }else{
                group= new Group(uid, grupo);
                groupList.put(uid, grupo);
            }
        }catch (PreconditionFailedException e){
            System.out.println("El usuario ya tiene un grupo con el mismo nombre");
        }
        //End of the method and mutual exclusion for this method:
        mutex.leave();
    }

    public void anadirMiembro (int creadorUid, String grupo, int uid) throws PreconditionFailedException{
        mutex.enter();

        mutex.leave();
    }

    public void salirGrupo (int uid, String grupo) throws PreconditionFailedException{
        mutex.enter();

        mutex.leave();
    }

    public void mandarMensaje (int uid, String grupo, Object contenido) throws PreconditionFailedException{
        mutex.enter();

        mutex.leave();
    }

    public Mensaje leer (int uid){
        mutex.enter();

        mutex.leave();
        return null;
    }
}
