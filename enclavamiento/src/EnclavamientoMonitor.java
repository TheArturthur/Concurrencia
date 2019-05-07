package src;
import es.upm.babel.cclib.Monitor;


public class EnclavamientoMonitor implements Enclavamiento {

                        //Variables to use:
    // Number of Segments there are (in case there is a change in the future, it's easier to change):
    private int nSegments = 3;

    // Monitor for Mutual Exclusion:
    private Monitor mutex;

    // In the following arrays, position 0 is never used for data. It just clarifies the rest of the array.

    // Array to save the conditions of each semaphore individually:
    private Monitor.Cond[] cLeerSemaforo =  new Monitor.Cond[nSegments];

    private int[] train;
    // a '0' would mean there's no train on the segment
    // whereas a '1' would mean there's in fact a train in that segment

    // colors of the Semaphores
    private Control.Color[] colors = new Control.Color[nSegments];

    // boolean to check if there's a train in a certain segment
    private boolean presence;

    /**
     * Sets the initial state of the railway:
     *      All semaphore colors to VERDE,
     *      No train in the railway,
     *      No car crossing the railway
     */
    //Constructor:
    public EnclavamientoMonitor() {
        this.mutex = new Monitor();

        // Loop to create a Condition for each Semaphore in the railway at the rate of 1 semaphore per segment,
        // and put each semaphore color to VERDE:
        // This way, if there's a change in the number of segments (and the number of semaphores), it's changed automatically.
        for (int i = 0; i < nSegments; i++) {
            this.cLeerSemaforo[i] = mutex.newCond();
            this.colors[i] = Control.Color.VERDE;
        }

        // There's no car crossing the railway:
        this.presence = false;

        // There are no trains in any of the segments of the railway:
        this.train = new int[]{0,0,0,0};
    }

    /**
     * CPRE:    true
     * avisarPresencia(p)
     * POST:    self.presencia = p &
     *          self.train = self_PRE.train &
     *          coloresCorrectos
     *
     * @param presence  Boolean to determine the presence of a train in a segment
     */
    @Override
    public void avisarPresencia(boolean presence) {
        mutex.enter();
        // CPRE = true => there's no lock

        // implementation of POST
        this.presence = presence;
        coloresCorrectos();

        // unlocking code: as there's no lock, there's no unlock
        // Should it unlock other processes???o
        mutex.leave();
    }

    /**
     * CPRE:    actual != (self.train(1) + self.train(2) = 0)
     * leerCambioBarrera(actual,esperado)
     * POST:    self = self_PRE && esperado <=> (self.train(1) + self.train(2) = 0)
     *
     * @param actual    Defines the actual state of the barrier
     * @return esperado Will be true when actual
     */
    @Override
    public boolean leerCambioBarrera(boolean actual) {
        mutex.enter();
        // checking of PRE
        // there's no PRE
        // checking of CPRE:
        if (actual == (train[1] + train[2] == 0)) {
            // if CPRE is not satisfied, we lock the method:

            Monitor.Cond cond = mutex.newCond();
            // Add cond into conditions list.
            cond.await();
        }
        // implementation of POST
        train[1] = 0;
        train[2] = 0;

        // unlock code:
        // if the CPRE is now satisfied and there's a method waiting, we shall now unlock:
        if (actual != (train[1] + train[2] == 0) && cond.waiting() > 0) {
            //we unlock the method
            cond.signal();
        }
        mutex.leave();
        return true;
    }

    /**
     * CPRE:    actual != (self.train(1) > 1 |
     *          self.train(2) > 1 |
     *          self.train(2) = 1 &
     *          self.presencia)
     * leerCambioFreno(actual,esperado)
     * POST:    self = self_PRE &
     *          esperado <=> (self.train(1) > 1 |
     *                        self.train(2) > 1 |
     *                        self.train(2) = 1 &
     *                        self.presencia)
     *
     *
     */
    @Override
    public boolean leerCambioFreno(boolean actual) {
        mutex.enter();
        // chequeo de la PRE

        // chequeo de la CPRE y posible bloqueo

        // implementacion de la POST

        // codigo de desbloqueo

        mutex.leave();
        return false;
    }

    /**
     * PRE:     i != 0
     * CPRE:    actual != self.color(i)
     * leerCambioSemaforo(i, actual, esperado)
     * POST:    self = self_PRE &
     *          esperado = self.color(i)
     *
     * @param i the index of the semaphore to change in the array
     * @param actual the actual color to which the semaphore must change
     * @return the new color of the semaphore
     */
    @Override
    public Control.Color leerCambioSemaforo(int i, Control.Color actual) {
        mutex.enter();
        // checking PRE: if i = 0 => we throw an exception as there's no Semaphore 0
        // 'Semaphore 0' is just for simplifying the array code
        if (i == 0) {
            throw  new PreconditionFailedException();
        }
        // checking of the CPRE and posible lock
        if (colors[i] == actual) {
            cLeerSemaforo[i].await();
        }
        // implementacion de la POST

        // codigo de desbloqueo

        mutex.leave();
        return null;
    }

    /**
     * PRE:     i != 0
     * CPRE:    true
     * avisarPasoPorBaliza
     * POST:    self.presencia = self_PRE.presencia &
     *          self.train = self_PRE.train + {
     *                           i - 1 => self_PRE.train(i - 1) - 1,
     *                           i     => self_PRE.train(i) + 1} &
     *          coloresCorrectos
     *
     * @param i the index of the segment the train is passing through
     */
    @Override
    public void avisarPasoPorBaliza(int i) {
        mutex.enter();
        // chequeo de la PRE

        // chequeo de la CPRE y posible bloqueo

        // implementacion de la POST

        // codigo de desbloqueo
        if (cLeerSemaforo[i].waiting() > 0 && colors[i] != actual) {
            cLeerSemaforo[i].signal();
        }
        mutex.leave();
    }

    /**
     * Assigns the correct colors depending on the values of the state of the railway
     *
     * coloresCorrectos()
     *
     */
    private void coloresCorrectos() {

    }
}
