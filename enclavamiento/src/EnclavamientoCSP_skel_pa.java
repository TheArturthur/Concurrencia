// TODO : importar estructuras de datos, si os hace falta
import org.jcsp.lang.Alternative;
import org.jcsp.lang.Any2OneChannel;
import org.jcsp.lang.CSProcess;
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
    private static final Any2OneChannel chAvisarPresencia     = Channel.any2one();
    private static final Any2OneChannel chLeerCambioBarrera   = Channel.any2one();
    private static final Any2OneChannel chLeerCambioFreno     = Channel.any2one();
    private static final Any2OneChannel chLeerCambioSemaforo  = Channel.any2one();
    private static final Any2OneChannel chAvisarPasoPorBaliza = Channel.any2one();

    // Clases auxiliares para las peticiones que se envÃ­an al servidor
    public class PeticionLeerCambioBarrera{
        protected One2OneChannel channel;
        protected boolean value;

        public PeticionLeerCambioBarrera(One2OneChannel channel, boolean value) {
            this.channel = channel;
            this.value = value;
        }
    }

    public class PeticionLeerCambioFreno{
        protected One2OneChannel channel;
        protected boolean value;

        public PeticionLeerCambioFreno(One2OneChannel channel, boolean value) {
            this.channel = channel;
            this.value = value;
        }
    }

    public class PeticionLeerCambioSemaforo{
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

        return (boolean) ch.in().read();
    }

    @Override
    public boolean leerCambioFreno(boolean accionado) {
        One2OneChannel ch = Channel.one2one();
        chLeerCambioFreno.out().write(new PeticionLeerCambioFreno(ch, accionado));

        return (boolean) ch.in().read();
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
        //
        // TODO : inicializaciÃ³n del estado del recurso
        //
        //
        //
        //

        // TODO : Declarar estructuras auxiliares para guardar
        //        las peticiones aplazadas en el servidor
        //
        //
        //
        //
        //
        //

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
                    // TODO : actualizar estado del recurso
                    //
                    break;

                case LEER_CAMBIO_BARRERA:
                    //@ assume pre
                    // TODO : leer peticiÃ³n del canal
                    // TODO : guardar la peticiÃ³n tal cual
                    break;

                case LEER_CAMBIO_FRENO:
                    //@ assume pre
                    // TODO : leer peticiÃ³n del canal
                    // TODO : guardar la peticiÃ³n tal cual
                    break;

                case LEER_CAMBIO_SEMAFORO:
                    //@ assume pre
                    // TODO : leer peticiÃ³n del canal
                    // TODO : guardar la peticiÃ³n tal cual
                    break;

                case AVISAR_PASO_POR_BALIZA:
                    //@ assume pre && cpre
                    // TODO : leer peticiÃ³n del canal
                    // TODO : actualizar estado del recurso
                    //
                    //
                    //
                    break;
            } // switch

            // CÃ³digo de desbloqueo
            // TODO : cÃ³digo que recorre vuestras peticiones aplazadas
            //        procesando aquellas cuya CPRE se cumple,
            //        hasta que no quede ninguna
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
            //
        } // end bucle servicio
    } // end run method

    // TODO : meted aquÃ­ vuestros mÃ©todos auxiliares para
    //        ajustar luces, cÃ¡lculo de CPREs, etc.
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

} // end CLASS