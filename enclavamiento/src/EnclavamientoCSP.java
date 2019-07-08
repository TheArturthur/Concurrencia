// TO-DO : importar estructuras de datos, si os hace falta
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
        // LOCAL VARIABLES DECLARATION:
        // The number of segments present (easier to change in future if needed)
        int nSegments = 3;

        // Indicates whether there's a car crossing the railway or not:
        boolean presence;

        // Array with the number of trains in the segment (the first value is of no use):
        int[] trains;

        // Array with the semaphore colors (the first value is NULL, as it's of no use):
        Control.Color[] colors;

        // LOCAL VARIABLES INITIALIZATION:
        // At first there are no cars crossing,...
        presence = false;

        // ... no trains going through,...
        trains = new int[]{0,0,0,0};

        // ... and the semaphores are all in GREEN (except for the #0, because well, it's useless).
        colors = new Control.Color[nSegments + 1];
        for (int i = 1; i < nSegments + 1; i++) {
            colors[i] = Control.Color.VERDE;
        }

        // FIFO-typed Lists to save the petitions that are created from the methods above.
        // There's one FIFO List for each blocking method:
        FIFOList<PeticionLeerCambioBarrera> petLeerCambioBarrera = new FIFOList<PeticionLeerCambioBarrera>();
        FIFOList<PeticionLeerCambioFreno> petLeerCambioFreno = new FIFOList<PeticionLeerCambioFreno>();
        FIFOList<PeticionLeerCambioSemaforo> petLeerCambioSemaforo = new FIFOList<PeticionLeerCambioSemaforo>();

        // Building the alternative reception:
        Guard[] inputs = {
                chAvisarPresencia.in(),
                chLeerCambioBarrera.in(),
                chLeerCambioFreno.in(),
                chLeerCambioSemaforo.in(),
                chAvisarPasoPorBaliza.in()
        };

        Alternative services = new Alternative(inputs);
        int chosenService;

        // Service loop:
        while (true){
            chosenService = services.fairSelect();

            switch (chosenService) {

                case AVISAR_PRESENCIA:
                    // We assume the PRE & the CPRE are already satisfied.
                    // We read the channel and get the value sent in it, which tells us if there's a car crossing,
                    // so we save it in presence.
                    presence = (Boolean) chAvisarPresencia.in().read();
                    // Now we change the semaphore colors according to the presence value we just read:
                    coloresCorrectos(trains, colors, presence);
                    // And exit:
                    break;

                case LEER_CAMBIO_BARRERA:
                    // We assume the PRE is already satisfied.
                    // We read the channel and the value sent, which is the Petition created above for the BARRIER:
                    PeticionLeerCambioBarrera petBarrier = (PeticionLeerCambioBarrera) chLeerCambioBarrera.in().read();
                    // We now save straight away that petition, as we'll unlock it later.
                    petLeerCambioBarrera.enqueue(petBarrier);
                    // And exit:
                    break;

                case LEER_CAMBIO_FRENO:
                    // We assume the PRE is already satisfied.
                    // We read the channel and the value sent, which is the Petition created above for the BRAKE:
                    PeticionLeerCambioFreno petBrake = (PeticionLeerCambioFreno) chLeerCambioFreno.in().read();
                    // We now save straight away that petition, as we'll unlock it later.
                    petLeerCambioFreno.enqueue(petBrake);
                    // And exit:
                    break;

                case LEER_CAMBIO_SEMAFORO:
                    // We assume the PRE is already satisfied.
                    // We read the channel and the value sent, which is the Petition created, for the SEMAPHORE:
                    PeticionLeerCambioSemaforo petSemaphore = (PeticionLeerCambioSemaforo) chLeerCambioSemaforo.in().read();
                    // We now save straight away that petition, as we'll unlock it later.
                    petLeerCambioSemaforo.enqueue(petSemaphore);
                    // And exit:
                    break;

                case AVISAR_PASO_POR_BALIZA:
                    // We assume the PRE & the are already satisfied.
                    // We read the channel and the value sent, which is the number of the segment where there's a train
                    // passing through:
                    int i = (Integer) chAvisarPasoPorBaliza.in().read();
                    // Then, we actualize the trains array:
                    if (i == 1) {
                        // If it's in the first segment, we don't have to decrement the previous one, as there's none.
                        trains[1]++;
                    }else {
                        // We decrement the previous segment number of trains, and add a train in the segment given, in
                        // order to reflect the passing of the train in the railway.
                        trains[i - 1]--;
                        trains[i]++;
                    }

                    // Then, we actualize the semaphore colors based on the recent changes:
                    coloresCorrectos(trains, colors, presence);
                    // And exit:
                    break;
            } // switch

            // Unblocking code: this will unlock every petition saved that satisfies it's CPRE.

            // Barrier:
            // We get the size of the petition list:
            int BarrierSize = petLeerCambioBarrera.size();
            // Go through the petition, but just once per petition:
            for (int i = 0; i < BarrierSize; i++) {
                // We get the first petition in the list:
                PeticionLeerCambioBarrera petBarrier = petLeerCambioBarrera.dequeue();

                //And we check it's CPRE:
                if (checkBarrierCPRE(petBarrier.value, trains)) {
                    // If it's satisfied, we calculate the result, which will be true if there are no trains in segments
                    // 1 and 2, and false otherwise:
                    boolean resBarrera = trains[1] + trains[2] == 0;

                    // Afterwards, we send the result in the petition's channel:
                    petBarrier.channel.out().write(resBarrera);
                } else {
                    // If the CPRE wasn't satisfied though, we put the petition back in the List:
                    petLeerCambioBarrera.enqueue(petBarrier);
                }
            }

            // Brake:
            // We get the size of the petition list:
            int BrakeSize = petLeerCambioFreno.size();
            // Go through the petition, but just once per petition:
            for (int i = 0; i < BrakeSize; i++) {
                // We get the first petition in the list:
                PeticionLeerCambioFreno petBrake = petLeerCambioFreno.dequeue();

                //And we check it's CPRE:
                if (checkBrakeCPRE(petBrake.value, trains, presence)) {
                    // If it's satisfied, we calculate the result, which will be true if there's more than one train in
                    // the first or second segment, or if there's one train in the second and a car crossing the
                    // railway; and false otherwise:
                    boolean resFreno = trains[1] > 1 || trains[2] > 1 || (trains[2] == 1 && presence);

                    // Then, we send the result in the petition's channel:
                    petBrake.channel.out().write(resFreno);
                } else{
                    // If the CPRE wasn't satisfied though, we put the petition back in the List:
                    petLeerCambioFreno.enqueue(petBrake);
                }
            }

            // Semaphore:
            // We get the size of the petition list:
            int SemaphoreSize = petLeerCambioSemaforo.size();
            // Go through the petition, but just once per petition:
            for (int i = 0; i < SemaphoreSize; i++) {
                // We get the first petition in the list:
                PeticionLeerCambioSemaforo petSemaphore = petLeerCambioSemaforo.dequeue();

                //And we check it's CPRE:
                if (checkSemaphoreCPRE(petSemaphore.index, petSemaphore.color, colors)) {
                    // If it's satisfied, we get the result, which is the previous color of the semaphore:
                    Control.Color resSemaforo = colors[petSemaphore.index];

                    // Then, we send the result in the petition's channel:
                    petSemaphore.channel.out().write(resSemaforo);
                } else{
                    // If the CPRE wasn't satisfied though, we put the petition back in the List:
                    petLeerCambioSemaforo.enqueue(petSemaphore);
                }
            }

        } // end service loop
    } // end run method

    // AUX METHODS:

    /**
     * Sets the correct colors of the semaphores, according to the number of trains in each segment and the presence or
     * not of a car in the railway.
     * @param trains    array with the number of trains in each segment
     * @param colors    array with the colors of each semaphore
     * @param presence  boolean saying if there's a car crossing the railway or not
     */
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

    /**
     * Checks the CPRE for the Semaphores petitions
     * @param index     the number of the semaphore in the array of colors
     * @param actual    the new color of the semaphore
     * @param colors    the array of colors shown by the semaphores
     * @return          true -> if the colors are different,
     *                  false -> if not
     */
    private boolean checkSemaphoreCPRE (int index, Control.Color actual, Control.Color[] colors) {
        return !colors[index].equals(actual);
    }

    /**
     * Checks the CPRE for the Brakes petitions
     * @param actual    the new value of the brake
     * @param trains    the array of trains in each segment
     * @param presence  whether there's a car crossing or not
     * @return          true -> if there's more than one train in the first or second segment, or if there's one train
     *                          in the second and a car crossing the railway
     *                  false -> otherwise
     */
    private boolean checkBrakeCPRE (boolean actual, int[] trains, boolean presence) {
        return actual != (trains[1] > 1 || trains[2] > 1 || trains[2] == 1 && presence);
    }

    /**
     * Checks the CPRE for the Barriers petitions
     * @param actual    the new value of the barrier
     * @param trains    the array of trains in each segment
     * @return          true -> if there are trains in the first or second segment
     *                  false -> otherwise
     */
    private boolean checkBarrierCPRE (boolean actual, int[] trains) {
        return actual != (trains[1] + trains[2] == 0);
    }


} // end CLASS