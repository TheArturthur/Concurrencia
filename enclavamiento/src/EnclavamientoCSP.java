import org.jcsp.lang.Alternative;
import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.ProcessManager;

/**
 * Implementation using channel replication
 */
public class EnclavamientoCSP implements CSProcess, Enclavamiento {

    /* WRAPPER IMPLEMENTATION */
    /**
     * Channels for receiving external requests
     * just one channel for nonblocking requests
     */
    private final One2OneChannel chAvisarPresencia = Channel.one2one();
    private final One2OneChannel chAvisarPasoPorBaliza = Channel.one2one();

    // leerCambioBarrera blocks depending on a boolean parameter
    private final One2OneChannel chLeerCambioBarreraT = Channel.one2one();
    private final One2OneChannel chLeerCambioBarreraF = Channel.one2one();

    // leerCambioFreno blocks depending on a boolean parameter
    private final One2OneChannel chLeerCambioFrenoT = Channel.one2one();
    private final One2OneChannel chLeerCambioFrenoF = Channel.one2one();

    // leerCambioSemaforo blocks depending on a semaphore id and a colour
    private final One2OneChannel[][] chLeerCambioSemaforo = new One2OneChannel[3][3];


    public EnclavamientoCSP () {
        // pending initializations
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                chLeerCambioSemaforo[i][j] = Channel.one2one();
            }
        }
        new ProcessManager(this).start();
    }

    @Override
    public void avisarPresencia(boolean presencia) {
        chAvisarPresencia.out().write(presencia);
    }

    @Override
    public void avisarPasoPorBaliza(int i) {
        if (i == 0 )
            throw new PreconditionFailedException("Baliza 0 no existe");

        chAvisarPasoPorBaliza.out().write(i);
    }

    @Override
    public boolean leerCambioBarrera(boolean abierta) {
        One2OneChannel chreply = Channel.one2one();
        if (abierta) {
            chLeerCambioBarreraT.out().write(chreply);
        } else {
            chLeerCambioBarreraF.out().write(chreply);
        }
        return (Boolean) (chreply.in().read());
    }

    @Override
    public boolean leerCambioFreno(boolean accionado) {
        One2OneChannel chreply = Channel.one2one();
        if (accionado) {
            chLeerCambioFrenoT.out().write(chreply);
        } else {
            chLeerCambioFrenoF.out().write(chreply);
        }
        return (Boolean) (chreply.in().read());
    }

    /** notice that the exception must be thrown outside the server */
    @Override
    public Control.Color leerCambioSemaforo (int i, Control.Color color) {
        if (i == 0 || i > 3)
            throw new PreconditionFailedException("Semaforo 0 no existe");

        One2OneChannel chreply = Channel.one2one();

        chLeerCambioSemaforo[i-1][color.ordinal()].out().write(chreply);

        return (Control.Color) (chreply.in().read());
    }

    /** SERVER IMPLEMENTATION */
    @Override
    public void run() {
        // resource state is kept in the server

        int nSegments = 3;

        boolean presence;

        int[] trains;

        Control.Color[] colors;

        // state initialization
        colors = new Control.Color[nSegments + 1];

        for (int i = 0; i < nSegments + 1; i++) {
            colors[i] = Control.Color.VERDE;
        }

        presence = false;

        trains = new int[]{0,0,0,0};


        // mapping request numbers to channels and vice versa
        // 0 <--> chAvisarPresencia
        // 1 <--> chAvisarPasoPorBaliza
        // 2 <--> chLeerCambioBarreraT
        // 3 <--> chLeerCambioBarreraF
        // 4 <--> chLeerCambioFrenoT
        // 5 <--> chLeerCambioFrenoF
        // 6 + (3*(i-1)) + j <--> chLeerCambioSemaforo[i][j]
        Guard[] inputs = new AltingChannelInput[15];
        inputs[0] = chAvisarPresencia.in();
        inputs[1] = chAvisarPasoPorBaliza.in();
        inputs[2] = chLeerCambioBarreraT.in();
        inputs[3] = chLeerCambioBarreraF.in();
        inputs[4] = chLeerCambioFrenoT.in();
        inputs[5] = chLeerCambioFrenoF.in();
        for (int i = 6; i < 15; i++) {
            inputs[i] = chLeerCambioSemaforo[(i-6) / 3][(i-6) % 3].in();
        }

        Alternative services = new Alternative(inputs);
        int chosenService;

        // conditional sincronization
        boolean[] sincCond = new boolean[15];
        // algunas condiciones de recepciÃ³n no varÃ­an durante
        // la ejecuciÃ³n del programa
        sincCond[0] = true;
        sincCond[1] = true;

        while (true){
            // actualizar sincronizaciÃ³n condicional
            sincCond[2] = checkBarrierCPRE(true, trains);
            sincCond[3] = checkBarrierCPRE(false, trains);
            sincCond[4] = checkBrakeCPRE(true, trains, presence);
            sincCond[5] = checkBrakeCPRE(false, trains, presence);

            for (int i = 6; i < 15; i++) {
                sincCond[i] = checkSemaphoreCPRE((i-6) / 3, colors[(i-6) % 3], colors);
            }

            // esperar peticiÃ³n
            chosenService = services.fairSelect(sincCond);
            One2OneChannel chreply = Channel.one2one(); // lo usamos para contestar a los clientes

            switch(chosenService){
                case 0: // avisarPresencia
                    //@ assume inv & pre && cpre of operation;

                    presence = (Boolean) (chAvisarPresencia.in().read());

                    coloresCorrectos(trains, colors, presence);

                    chAvisarPresencia.out();

                    chreply.out().write(presence);
                    break;
                case 1: // avisarPasoPorBaliza
                    //@ assume inv & pre && cpre of operation;

                    Integer index = (Integer) (chAvisarPasoPorBaliza.in().read());

                    if (index == 1) {
                        trains[index]++;
                    }else {
                        trains[index - 1]--;
                        trains[index]++;
                    }
                    coloresCorrectos(trains, colors, presence);

                    chAvisarPasoPorBaliza.out();
                    break;
                case 2: // leerCambioBarrera(true)
                    //@ assume inv & pre && cpre of operation;

                    Boolean actualT = (Boolean) (chLeerCambioBarreraT.in().read());

                    Boolean resultBT = actualT == (trains[1] + trains[2] == 0);

                    chLeerCambioBarreraT.out().write(resultBT);

                    chreply.out().write(resultBT);
                    break;
                case 3: // leerCambioBarrera(false)
                    //@ assume inv & pre && cpre of operation;

                    Boolean actualF = (Boolean) (chLeerCambioBarreraF.in().read());

                    Boolean resultBF = actualF == (trains[1] + trains[2] == 0);

                    chLeerCambioBarreraT.out().write(resultBF);

                    chreply.out().write(resultBF);
                    break;
                case 4: // leerCambioFreno(true)
                    //@ assume inv & pre && cpre of operation;

                    Boolean frenoT = (Boolean) (chLeerCambioFrenoT.in().read());

                    Boolean resultFT = frenoT == (trains[1] > 1 || trains[2] > 1 || (trains[2] == 0 && presence));

                    chLeerCambioFrenoT.out().write(resultFT);

                    chreply.out().write(resultFT);
                    break;
                case 5: // leerCambioFreno(false)
                    //@ assume inv & pre && cpre of operation;

                    Boolean frenoF = (Boolean) (chLeerCambioFrenoF.in().read());

                    Boolean resultFF = frenoF == (trains[1] > 1 || trains[2] > 1 || (trains[2] == 0 && presence));

                    chLeerCambioFrenoF.out().write(resultFF);

                    chreply.out().write(resultFF);
                    break;
                default: // leerCambioSemaforo(queSemaforo,queColor)
                    // decodificar numero de semaforo y color a partir del
                    // valor de chosenService
                    int queSemaforo = (chosenService-6) / 3;
                    int queColor = (chosenService-6) % 3;

                    //Control.Color color = (Control.Color) (chLeerCambioSemaforo[queSemaforo][queColor].in().read());
                    Object color = chLeerCambioSemaforo[queSemaforo][queColor].in().read();

                    chLeerCambioSemaforo[queSemaforo][queColor].out().write(color);

                    chreply.out().write(color);
                    break;
            } // SWITCH
        } // SERVER LOOP
    } // run()

    // mÃ©todos auxiliares varios
    //        usado en la otra prÃ¡ctica para ajustar
    //        luces de semaforos, evaluar CPREs, etc.

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

    // checks the value of the Semaphore CPRE:
    private Boolean checkSemaphoreCPRE (int index, Control.Color actual, Control.Color[] colors) {
        return colors[index].equals(actual);
    }

    // checks the value of the Brake CPRE:
    private Boolean checkBrakeCPRE (boolean actual, int[] trains, boolean presence) {
        return actual != (trains[1] > 1 || trains[2] > 1 || trains[2] == 1 && presence);
    }

    // checks the value of the Barrier CPRE:
    private Boolean checkBarrierCPRE (boolean actual, int[] trains) {
        return actual != (trains[1] + trains[2] == 0);
    }

} // end CLASS