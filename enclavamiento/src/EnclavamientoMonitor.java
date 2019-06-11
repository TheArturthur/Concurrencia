import es.upm.babel.cclib.Monitor;
import es.upm.aedlib.fifo.*;



public class EnclavamientoMonitor implements Enclavamiento {

    //Variables to use:
    // Number of Segments there are (in case there is a change in the future, it's easier to change):
    private int nSegments = 3;

    // Monitor for Mutual Exclusion:
    private Monitor mutex;

    // In the following arrays, position 0 is never used for data. It just clarifies the rest of the array.

    // FIFO List to save the conditions of each semaphore individually in order:
    private FIFOList<CondSemaphore> cLeerSemaforo;

    // Condition for brake change:
    private Monitor.Cond cLeerFreno;

    // Condition for barrier change:
    private Monitor.Cond cCambioBarrera;

    // read by:     leerCambioBarrera, leerCambioFreno
    // written by:  avisarPresencia, leerCambioBarrera, leerCambioFreno, avisarPasoPorBaliza
    private int[] trains;
    // a '0' would mean there's no train on the segment
    // whereas a '1' would mean there's in fact a train in that segment, and so on...

    // colors shwon by the Semaphores' lights
    // read by:     leerCambioSemaforo
    // written by:  leerCambioSemaforo, avisarPresencia
    private Control.Color[] colors;

    // boolean to check if there's a train in a certain segment
    // read by:     leerCambioFreno
    // written by:  avisarPresencia
    private boolean presence;


    // VARIABLES TO CONSIDER CPRE's UNLOCK:
    // Saves the argument of leerCambioBarrera when it's locked:
    private boolean barrierState;

    // Saves the argument of leerCambioFreno when it's locked:
    private boolean brakeState;

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
        this.colors = new Control.Color[nSegments + 1];

        for (int i = 1; i < nSegments + 1; i++) {
            this.colors[i] = Control.Color.VERDE;
        }

        // Initialization of the
        this.cLeerSemaforo = new FIFOList<CondSemaphore>();
        this.cLeerFreno = mutex.newCond();
        this.cCambioBarrera = mutex.newCond();

        // There's no car crossing the railway:
        this.presence = false;

        // There are no trains in any of the segments of the railway:
        this.trains = new int[]{0,0,0,0};
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

        // Set the correct colors after changing the state:
        coloresCorrectos();

        // unlock code:
        unlock();

        mutex.leave();
    }

    /**
     * CPRE:    actual != (self.train(1) + self.train(2) = 0)
     * leerCambioBarrera(actual,esperado)
     * POST:    self = self_PRE && esperado <=> (self.train(1) + self.train(2) = 0)
     *
     * @param actual    Defines the actual state of the barrier
     * @return esperado Will be true when there are no trains in segments 1 or 2,
     *                  which would mean the barrier needs to be closed so no car cross the railway.
     *                  Will be false otherwise, opening the barrier.
     */
    @Override
    public boolean leerCambioBarrera(boolean actual) {
        mutex.enter();
        // checking of PRE
        // there's no PRE
        // checking of CPRE:
        if (!checkBarrierCPRE(actual)) {
            // CPRE will be satisfied when there's no train in segment 1 nor in 2.
            // if CPRE is not satisfied (there's a train in at least one of those segments), we lock the method:
            this.barrierState = actual;
            this.cCambioBarrera.await();
        }
        // implementation of POST: true if there are no trains in segments 1 and 2, false otherwise:
        boolean result = this.trains[1] + this.trains[2] == 0;

        // unlock code: will call the next method to execute once this one leaves the Monitor
        unlock();

        // leave the Monitor and return the result:
        mutex.leave();
        return result;
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
     */
    @Override
    public boolean leerCambioFreno(boolean actual) {
        mutex.enter();
        // checking of CPRE and lock:
        if (!checkBrakeCPRE(actual)) {
            // if CPRE is not satisfied, save the actual value and lock the method:
            this.brakeState = actual;
            this.cLeerFreno.await();
        }

        // implementation of POST:
        boolean result = this.trains[1] > 1 || this.trains[2] > 1 || (this.trains[2] == 1 && this.presence);

        // unlocking code:
        unlock();

        // leave the Monitor and return the result value:
        mutex.leave();
        return result;
    }

    /**
     * PRE:     i != 0
     * CPRE:    actual != self.color(i)
     * leerCambioSemaforo(i, actual, esperado)
     * POST:    self = self_PRE &
     *          esperado = self.color(i)
     *
     * @param i the index of the semaphore to change in the array
     * @param actual the old color from which the semaphore must change
     * @return the new color of the semaphore
     */
    @Override
    public Control.Color leerCambioSemaforo(int i, Control.Color actual) {

        // checking PRE: if i = 0 => we throw an exception as there's no Semaphore 0.
        // 'Semaphore 0' is just for simplifying the array code
        if (i == 0) {
            throw new PreconditionFailedException();
        }

        // Enter the Monitor:
        mutex.enter();

        // checking of the CPRE and posible lock: if the color of the semaphore is the same, lock.
        if (checkSemaphoreCPRE(i, actual)) {
            // Create a new condition, saving it in the FIFO List
            Monitor.Cond condSemaphore = mutex.newCond();
            cLeerSemaforo.enqueue(new CondSemaphore(i, actual, condSemaphore));
            // Send it to wait:
            condSemaphore.await();
        }

        // implementacion de la POST: return new color
        Control.Color result = this.colors[i];

        // unlocking code
        unlock();

        // leave the Monitor and return the color
        mutex.leave();
        return result;
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

        // Checking of PRE before entering the Monitor, as it depends on the value passed:
        if(i == 0){
            throw new PreconditionFailedException();
        }

        // Entering the Monitor
        mutex.enter();

        // there's no CPRE, so there's no lock

        // implementation of the POST:
        // if the segment in which the train is passing through is the first one, there's no need in decrementing the
        // "segment 0"
        if (i == 1) {
            this.trains[1]++;
        }else {
            this.trains[i - 1]--;
            this.trains[i]++;
        }

        // Set the correct colors after changing the state:
        coloresCorrectos();

        // unlocking code:
        unlock();

        // leave the Monitor:
        mutex.leave();
    }

    /**
     * Assigns the correct colors depending on the values of the state of the railway
     *
     * coloresCorrectos()
     */
    private void coloresCorrectos() {
        // About the first semaphore:
        if (this.trains[1] > 0) {
            // If there's at least one train in the first segment, we put the first semaphore to RED.
            this.colors[1] = Control.Color.ROJO;
        } else if (this.trains[2] > 0 || this.presence) {
            // If there's at least one train in the second segment or there's a car,
            // we put the first semaphore to YELLOW.
            this.colors[1] = Control.Color.AMARILLO;
        } else {
            // Else, we put it GREEN.
            this.colors[1] = Control.Color.VERDE;
        }

        // About the second semaphore:
        if (this.trains[2] > 0 || this.presence) {
            // If there's at least one train in the second segment or there's a car,
            // we put the second semaphore to RED.
            this.colors[2] = Control.Color.ROJO;
        } else {
            // If not, we put it to GREEN.
            this.colors[2] = Control.Color.VERDE;
        }

        // The third semaphore will always be GREEN.
        this.colors[3] = Control.Color.VERDE;
    }

    /**
     * Searches for a sleeping thread that's waiting and signals it.
     */
    private void unlock() {

        // Variable to take into account if a thread has been signaled:
        boolean signaled = false;

        // check cLeerSemaforo
        // save the size of the FIFO List with the Semaphore conditions:
        int n = cLeerSemaforo.size();
        // we run through all the List if there's at least one condition in it:
        for (int i = 0; i < n && !signaled; i++) {
            // get the first condition available and dequeue it from the List:
            CondSemaphore condition = cLeerSemaforo.first();
            cLeerSemaforo.dequeue();

            // If the CPRE of that condition now is true, signal it and set the signaled variable to true, so there's
            // only one signal:
            if (!checkSemaphoreCPRE(condition.getId(), condition.getActual())) {
                condition.getCondition().signal();
                signaled = true;
            } else {
                // If the CPRE of that condition is still false, we put the condition back to the List:
                cLeerSemaforo.enqueue(condition);
            }
        }

        // check cLeerFreno, only if no semaphore was signaled:
        // If there is a brake condition waiting and its CPRE is now true:
        if (!signaled && this.cLeerFreno.waiting() > 0 && checkBrakeCPRE(this.brakeState)) {
            // signal the condition and set the signaled variable to true, so there's only one signal:
            this.cLeerFreno.signal();
            signaled = true;
        }

        // check cambioBarrera, only if no semaphore nor the brake was signaled:
        // If there is a barrier condition waiting and its CPRE is now true:
        if (!signaled && this.cCambioBarrera.waiting() > 0 && checkBarrierCPRE(this.barrierState)) {
            // signal the condition (now there's no need of setting the signaled variable to true, as there are no more
            // options
            this.cCambioBarrera.signal();
        }
    }

    // checks the value of the Semaphore CPRE:
    private boolean checkSemaphoreCPRE (int index, Control.Color actual) {
        return this.colors[index].equals(actual);
    }

    // checks the value of the Brake CPRE:
    private boolean checkBrakeCPRE (boolean actual) {
        return actual != (this.trains[1] > 1 || this.trains[2] > 1 || this.trains[2] == 1 && this.presence);
    }

    // checks the value of the Barrier CPRE:
    private boolean checkBarrierCPRE (boolean actual) {
        return actual != (this.trains[1] + this.trains[2] == 0);
    }

    // Object CondSemaphore to save the values needed to signal the semaphore's conditions:
    private static class CondSemaphore {
        // It saves: the semaphore id,
        private int id;
        //  the actual color from when it was called and locked,
        private Control.Color actual;
        // and the Monitor condition itself:
        private Monitor.Cond condition;

        // Getter to check the semaphore index:
        public int getId() {
            return this.id;
        }

        // Getter to signal the condition:
        public Monitor.Cond getCondition() {
            return this.condition;
        }

        // Getter to check the semaphore color for the CPRE:
        public Control.Color getActual() {
            return this.actual;
        }

        // Constructor to create the object when creating the condition:
        public CondSemaphore(int id, Control.Color actual, Monitor.Cond condition){
            this.id = id;
            this.actual = actual;
            this.condition = condition;
        }
    }
}