// TODO : importar estructuras de datos, si os hace falta
import es.upm.aedlib.fifo.FIFOList;
import org.jcsp.lang.Alternative;
import org.jcsp.lang.Any2OneChannel;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ProcessManager;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

/**
 * Implementation using CSP (Mixed).
 * TÃ©cnica de peticiones aplazadas
 * excepto en las ops. de aviso (no bloqueantes)
 *
 * @author rul0
 */
public class EnclavamientoCSP implements CSProcess, Enclavamiento {

    /** WRAPPER IMPLEMENTATION */
    //** Channels for receiving external requests
    // Un canal por op. del recurso
    private final Any2OneChannel chAvisarPresencia     = Channel.any2one();
    private final Any2OneChannel chLeerCambioBarrera   = Channel.any2one();
    private final Any2OneChannel chLeerCambioFreno     = Channel.any2one();
    private final Any2OneChannel chLeerCambioSemaforo  = Channel.any2one();
    private final Any2OneChannel chAvisarPasoPorBaliza = Channel.any2one();

    public EnclavamientoCSP() {
        new ProcessManager(this).start();
    }

    // Clases auxiliares para las peticiones que se envÃ­an al servidor
    public static class PeticionLeerCambioBarrera{
        protected One2OneChannel channel;
        protected boolean value;

        public PeticionLeerCambioBarrera(One2OneChannel channel, boolean value) {
            this.channel = channel;
            this.value = value;
        }
    }

    public static class PeticionLeerCambioFreno{
        protected One2OneChannel channel;
        protected boolean value;

        public PeticionLeerCambioFreno(One2OneChannel channel, boolean value) {
            this.channel = channel;
            this.value = value;
        }
    }

    public static class PeticionLeerCambioSemaforo{
        protected One2OneChannel channel;
        protected Control.Color color;
        protected int index;

        public PeticionLeerCambioSemaforo(One2OneChannel channel,
                                          Control.Color color,
                                          int index) {
            this.channel = channel;
            this.color = color;
            this.index = index;
        }
    }

    // ImplementaciÃ³n de la interfaz Enclavamiento
    @Override
    public void avisarPresencia(boolean presencia) {
        chAvisarPresencia.out().write(presencia);
    }

    @Override
    public boolean leerCambioBarrera(boolean abierta) {
        One2OneChannel ch = Channel.one2one();
        chLeerCambioBarrera.out().write(new PeticionLeerCambioBarrera(ch, abierta));

        return (Boolean) ch.in().read();
    }

    @Override
    public boolean leerCambioFreno(boolean accionado) {
        One2OneChannel ch = Channel.one2one();
        chLeerCambioFreno.out().write(new PeticionLeerCambioFreno(ch, accionado));

        return (Boolean) ch.in().read();
    }

    /** notice that exceptions can be thrown outside the server */
    @Override
    public Control.Color leerCambioSemaforo(int i, Control.Color color) {
        if (i == 0 )
            throw new PreconditionFailedException("Semaforo 0 no existe");

        One2OneChannel ch = Channel.one2one();
        chLeerCambioSemaforo.out().write(new PeticionLeerCambioSemaforo(ch, color, i));

        return (Control.Color) ch.in().read();
    }

    @Override
    public void avisarPasoPorBaliza(int i) {
        if (i == 0 )
            throw new PreconditionFailedException("Baliza 0 no existe");

        chAvisarPasoPorBaliza.out().write(i);
    }


    /** SERVER IMPLEMENTATION */
    static final int AVISAR_PRESENCIA = 0;
    static final int LEER_CAMBIO_BARRERA = 1;
    static final int LEER_CAMBIO_FRENO  = 2;
    static final int LEER_CAMBIO_SEMAFORO  = 3;
    static final int AVISAR_PASO_POR_BALIZA = 4;

    @Override
    public void run() {
        // TODO : Declarar aquÃ­ el estado del recurso:
        //        presencia, tren y color
        int nSegments = 3;
        boolean presence;
        int[] trains;
        Control.Color[] colors;
        //
        // TODO : inicializaciÃ³n del estado del recurso
        presence = false;
        trains = new int[]{0,0,0,0};
        colors = new Control.Color[nSegments + 1];

        for (int i = 1; i < nSegments + 1; i++) {
            colors[i] = Control.Color.VERDE;
        }

        // TODO : Declarar estructuras auxiliares para guardar
        //        las peticiones aplazadas en el servidor
        FIFOList<PeticionLeerCambioBarrera> petLeerCambioBarrera = new FIFOList<PeticionLeerCambioBarrera>();
        FIFOList<PeticionLeerCambioFreno> petLeerCambioFreno = new FIFOList<PeticionLeerCambioFreno>();
        FIFOList<PeticionLeerCambioSemaforo> petLeerCambioSemaforo = new FIFOList<PeticionLeerCambioSemaforo>();

        // ConstrucciÃ³n de la recepciÃ³n alternativa
        Guard[] inputs = {
                chAvisarPresencia.in(),
                chLeerCambioBarrera.in(),
                chLeerCambioFreno.in(),
                chLeerCambioSemaforo.in(),
                chAvisarPasoPorBaliza.in()
        };

        Alternative services = new Alternative(inputs);
        int chosenService = 0;

        // Bucle de servicio
        while (true){
            chosenService = services.fairSelect();

            switch (chosenService) {

                case AVISAR_PRESENCIA:
                    //@ assume pre && cpre
                    // TODO : leer peticiÃ³n del canal
                    presence = (Boolean) chAvisarPresencia.in().read();
                    // TODO : actualizar estado del recurso
                    coloresCorrectos(trains, colors, presence);
                    break;

                case LEER_CAMBIO_BARRERA:
                    //@ assume pre
                    // TODO : leer peticiÃ³n del canal
                    PeticionLeerCambioBarrera petBarrera = (PeticionLeerCambioBarrera) chLeerCambioBarrera.in().read();
                    // TODO : guardar la peticiÃ³n tal cual
                    petLeerCambioBarrera.enqueue(petBarrera);
                    break;

                case LEER_CAMBIO_FRENO:
                    //@ assume pre
                    // TODO : leer peticiÃ³n del canal
                    PeticionLeerCambioFreno petFreno = (PeticionLeerCambioFreno) chLeerCambioFreno.in().read();
                    // TODO : guardar la peticiÃ³n tal cual
                    petLeerCambioFreno.enqueue(petFreno);
                    break;

                case LEER_CAMBIO_SEMAFORO:
                    //@ assume pre
                    // TODO : leer peticiÃ³n del canal
                    PeticionLeerCambioSemaforo petSemaforo = (PeticionLeerCambioSemaforo) chLeerCambioSemaforo.in().read();
                    // TODO : guardar la peticiÃ³n tal cual
                    petLeerCambioSemaforo.enqueue(petSemaforo);
                    break;

                case AVISAR_PASO_POR_BALIZA:
                    //@ assume pre && cpre
                    // TODO : leer peticiÃ³n del canal
                    int i = (Integer) chAvisarPasoPorBaliza.in().read();
                    // TODO : actualizar estado del recurso
                    if (i == 1) {
                        trains[1]++;
                    }else {
                        trains[i - 1]--;
                        trains[i]++;
                    }
                    coloresCorrectos(trains, colors, presence);
                    break;
            } // switch

            // CÃ³digo de desbloqueo
            // TODO : cÃ³digo que recorre vuestras peticiones aplazadas
            //        procesando aquellas cuya CPRE se cumple,
            //        hasta que no quede ninguna

            // HOW T'WAS BEFORE:
            /*
            int total = petLeerCambioBarrera.size() + petLeerCambioFreno.size() + petLeerCambioSemaforo.size();
            for (int i = 0; i < total; i++) {
                ...
                if (CPRE){
                    i++;
                    method call
                }
                ...
            }

             */
            // YA SURE 'TIS LIKE THIS?:
            while (!(petLeerCambioBarrera.isEmpty() && petLeerCambioFreno.isEmpty() && petLeerCambioSemaforo.isEmpty())) {
                // Barrera:
                if (!petLeerCambioBarrera.isEmpty()) {
                    PeticionLeerCambioBarrera petBarrera = petLeerCambioBarrera.dequeue();
                    boolean resBarrera = trains[1] + trains[2] == 0;

                    if (checkBarrierCPRE(petBarrera.value, trains)) {
                        leerCambioBarrera(resBarrera);
                    } else {
                        petLeerCambioBarrera.enqueue(petBarrera);
                    }
                }
                // Freno:
                if (!petLeerCambioFreno.isEmpty()) {
                    PeticionLeerCambioFreno petFreno = petLeerCambioFreno.dequeue();
                    boolean resFreno = trains[1] > 1 || trains[2] > 1 || (trains[2] == 1 && presence);

                    if (checkBrakeCPRE(petFreno.value, trains, presence)) {
                        leerCambioFreno(resFreno);
                    } else {
                        petLeerCambioFreno.enqueue(petFreno);
                    }
                }

                //Semaforo:
                if (!petLeerCambioSemaforo.isEmpty()) {
                    PeticionLeerCambioSemaforo petSemaforo = petLeerCambioSemaforo.dequeue();
                    Control.Color resSemaforo = colors[petSemaforo.index];

                    if (checkSemaphoreCPRE(petSemaforo.index, petSemaforo.color, colors)) {
                        leerCambioSemaforo(petSemaforo.index, resSemaforo);
                    } else {
                        petLeerCambioSemaforo.enqueue(petSemaforo);
                    }
                }
            }
        } // end bucle servicio
    } // end run method

    // TODO : meted aquÃ­ vuestros mÃ©todos auxiliares para
    //        ajustar luces, cÃ¡lculo de CPREs, etc.
    private void coloresCorrectos(int[] trains, Control.Color[] colors, boolean presence) {
        // About the first semaphore:
        if (trains[1] > 0) {
            // If there's at least one train in the first segment, we put the first semaphore to RED.
            colors[1] = Control.Color.ROJO;
        } else if (trains[2] > 0 || presence) {
            // If there's at least one train in the second segment or there's a car,
            // we put the first semaphore to YELLOW.
            colors[1] = Control.Color.AMARILLO;
        } else {
            // Else, we put it GREEN.
            colors[1] = Control.Color.VERDE;
        }

        // About the second semaphore:
        if (trains[2] > 0 || presence) {
            // If there's at least one train in the second segment or there's a car,
            // we put the second semaphore to RED.
            colors[2] = Control.Color.ROJO;
        } else {
            // If not, we put it to GREEN.
            colors[2] = Control.Color.VERDE;
        }

        // The third semaphore will always be GREEN.
        colors[3] = Control.Color.VERDE;
    }

    private boolean checkSemaphoreCPRE (int index, Control.Color actual, Control.Color[] colors) {
        return !colors[index].equals(actual);
    }

    // checks the value of the Brake CPRE:
    private boolean checkBrakeCPRE (boolean actual, int[] trains, boolean presence) {
        return actual != (trains[1] > 1 || trains[2] > 1 || trains[2] == 1 && presence);
    }

    // checks the value of the Barrier CPRE:
    private boolean checkBarrierCPRE (boolean actual, int[] trains) {
        return actual != (trains[1] + trains[2] == 0);
    }


} // end CLASS