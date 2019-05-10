import es.upm.babel.cclib.*;

import java.time.Month;

public class EnclavamientoMonitor implements Enclavamiento {

    //Variables to use:
    // Number of Segments there are (in case there is a change in the future, it's easier to change):
    private int nSegments = 3;

    // Monitor for Mutual Exclusion:
    private Monitor mutex;

    // In the following arrays, position 0 is never used for data. It just clarifies the rest of the array.

    // Array to save the conditions of each semaphore individually:
    private Monitor.Cond[] cLeerSemaforo =  new Monitor.Cond[nSegments + 1];

    // Array to save the conditions of each brake change individually:
    private Monitor.Cond[] cLeerFreno = new Monitor.Cond[nSegments + 1];

    private int[] trains;
    // a '0' would mean there's no train on the segment
    // whereas a '1' would mean there's in fact a train in that segment

    // colors shwon by the Semaphores' lights
    private Control.Color[] colors = new Control.Color[nSegments + 1];

    // current colors (that should be changed in the semaphores)
    private Control.Color[] current = new Control.Color[nSegments + 1];

    // boolean to check if there's a train in a certain segment
    private boolean presence;

    // Invariant (whatever happens, this mustn't be true):
    private boolean invariant;

    /**
     * Sets the initial state of the railway:
     *      All semaphore colors to VERDE,
     *      No train in the railway,
     *      No car crossing the railway,
     *      Invariant that must be always satisfied
     */
    //Constructor:
    public EnclavamientoMonitor() {
        this.mutex = new Monitor();

        // Loop to create a Condition for each Semaphore in the railway at the rate of 1 semaphore per segment,
        // and put each semaphore color to VERDE, along with the current colors (later on they will be changed):
        // This way, if there's a change in the number of segments (and the number of semaphores), it's changed automatically.
        for (int i = 0; i <= nSegments; i++) {
            this.cLeerSemaforo[i] = mutex.newCond();
            this.cLeerFreno[i] = mutex.newCond();
            this.colors[i] = Control.Color.VERDE;
            this.current[i] = Control.Color.VERDE;
        }

        // There's no car crossing the railway:
        this.presence = false;

        // There are no trains in any of the segments of the railway:
        this.trains = new int[]{0,0,0,0};

        this.invariant = trains[1] >= 0 && trains[2] >= 0 && coloresCorrectos(); // Needed??
    }

    /**
     * CPRE:    true
     * avisarPresencia(p)
     * POST:    self.presencia = p &
     *          self.train = self_PRE.train &
     *          coloresCorrectos
     *
     * @param presence  Boolean to determine the presence of a car crossing the railway
     *
     * This method shall signal a semaphore (if there's one waiting) to change its colors
     */
    @Override
    public void avisarPresencia(boolean presence) {
        mutex.enter();
        // CPRE = true => there's no lock

        // implementation of POST
        this.presence = presence;
        coloresCorrectos();

        // unlocking code: as there's no lock, there's no unlock
        boolean unlocked = false;
        for (int i = 0; i < cLeerSemaforo.length && !unlocked; i++) {
            if (cLeerSemaforo[i].waiting() > 0 && colors[i] != current[i]) {
                cLeerSemaforo[i].signal();
                unlocked = true;
            }
        }

        for (int i = 0; i < cLeerFreno.length && !unlocked; i++) {
            if (cLeerFreno[i].waiting() > 0 && /*CPRE FRENO*/) {
                cLeerFreno[i].signal();
                unlocked = true;
            }
        }

        mutex.leave();
    }

    /**
     * CPRE:    actual != (self.train(1) + self.train(2) = 0)
     * leerCambioBarrera(actual,esperado)
     * POST:    self = self_PRE && esperado <=> (self.train(1) + self.train(2) = 0)
     *
     * @param actual    Defines the actual state of the barrier
     * @return esperado Will be true when there are no trains in segments 1 or 2,
     *                  which would mean the barrier needs to be down so no car cross the railway.
     */
    @Override
    public boolean leerCambioBarrera(boolean actual) {
        mutex.enter();
        // checking of PRE
        // there's no PRE
        // checking of CPRE:
        if (actual == (trains[1] + trains[2] == 0)) {
            // CPRE will be satisfied when there's no train in segment 1 nor in 2.
            // if CPRE is not satisfied (there's a train in at least one of those segments), we lock the method:

            Monitor.Cond cond = mutex.newCond();
            // Add cond into conditions list.
            cond.await();
        }
        // implementation of POST
        trains[1] = 0;
        trains[2] = 0;

        // unlock code:
        // if the CPRE is now satisfied and there's a method waiting, we shall now unlock:
            // unlock CambioSemaforo

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
        // checking of CPRE and lock:

        // implementation of POST:

        // unlocking code:

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
        // checking PRE: if i = 0 => we throw an exception as there's no Semaphore 0.
        // 'Semaphore 0' is just for simplifying the array code
        if (i == 0) {
            throw  new PreconditionFailedException();
        }

        // checking of the CPRE and posible lock
        if (colors[i] == actual) {
            cLeerSemaforo[i].await();
        }

        // implementacion de la POST
        colors[i] = actual;

        // codigo de desbloqueo
        for (int j = 0; j < cLeerSemaforo.length; j++) {
            if (cLeerSemaforo[j].waiting() > 0 && colors[j] != current[j]) {
                cLeerSemaforo[j].signal();
                break;
            }
        }

        mutex.leave();
        return colors[i];
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
     *
     * This method shall signal a semaphore (if there's one waiting) to change its colors
     */
    @Override
    public void avisarPasoPorBaliza(int i) {
        mutex.enter();

        // chequeo de la PRE
        if(i == 0){
            throw new PreconditionFailedException();
        }

        // there's no CPRE, so there's no lock

        // implementacion de la POST
        trains[i - 1]--;
        trains[i]++;
        coloresCorrectos();

        // codigo de desbloqueo
        // unlock leerCambioBarrera (

        mutex.leave();
    }

    /**
     * Assigns the correct colors depending on the values of the state of the railway
     *
     * coloresCorrectos()
     *
     */
    private boolean /*void*/ coloresCorrectos() {
        if (trains[1] > 0) {
            current[1] = Control.Color.ROJO;
        } else if (colors[1] == Control.Color.ROJO) {
            trains[1] = 1;
        }

        if (colors[1] == Control.Color.AMARILLO) {
            trains[1] = 0;
            trains[2] = 1;
            presence = true;
        } else if (trains[1] == 0 && (trains[2] > 0 || presence)) {
            current[1] = Control.Color.AMARILLO;
        }

        if (colors[1] == Control.Color.VERDE) {
            trains[1] = 0;
            trains[2] = 0;
            presence = false;
        } else if (trains[1] == 0 && trains[2] == 0 && !presence) {
            current[1] = Control.Color.VERDE;
        }

        if (colors[2] == Control.Color.ROJO) {
            trains[2] = 1;
            presence = true;
        } else if (trains[2] > 0 || presence) {
            current[2] = Control.Color.ROJO;
        }

        if (colors[2] == Control.Color.VERDE) {
            trains[2] = 0;
            presence = false;
        } else if (trains[2] == 0 && !presence) {
            current[2] = Control.Color.VERDE;
        }

        current[3] = Control.Color.VERDE;
        return true;
    }
}
