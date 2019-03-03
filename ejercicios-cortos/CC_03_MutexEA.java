public class CC_03_MutexEA {
    static final int N_PASOS = 10000;

    // Generador de números aleatorios para simular tiempos de
    // ejecución
    // static final java.util.Random RNG = new java.util.Random(0);

    // Variable compartida
    volatile static int n = 0;

    // Variables para asegurar exclusión mutua
    volatile static boolean wants_inc = false;
    volatile static boolean wants_dec = false;
    volatile static boolean turn = false; //Turn's value will be assigned as 'true' to INC and 'false' to DEC.

    // Secciones críticas
    static void sc_inc() {
        // System.out.println("Incrementando");
        n++;
    }

    static void sc_dec() {
        // System.out.println("Decrementando");
        n--;
    }

    // La labor del proceso incrementador es ejecutar no_sc() y luego
    // sc_inc() durante N_PASOS asegurando exclusión mutua sobre
    // sc_inc().
    static class Incrementador extends Thread {
        public void run () {
            for (int i = 0; i < N_PASOS; i++) {
                // Sección no crítica
                wants_inc = true;
                turn = false;

                // Protocolo de acceso a la sección crítica
                while (wants_dec && !turn) {}

                // Sección crítica
                sc_inc();

                // Protocolo de salida de la sección crítica
                wants_inc = false;
            }
        }
    }

    // La labor del proceso incrementador es ejecutar no_sc() y luego
    // sc_dec() durante N_PASOS asegurando exclusión mutua sobre
    // sc_dec().
    static class Decrementador extends Thread {
        public void run () {
            for (int i = 0; i < N_PASOS; i++) {
                // Sección no crítica
                wants_dec = true;
                turn = true;

                // Protocolo de acceso a la sección crítica
                while (wants_inc && turn) {}

                // Sección crítica
                sc_dec();

                // Protocolo de salida de la sección crítica
                wants_dec = false;
            }
        }
    }

    public static final void main(final String[] args)
            throws InterruptedException
    {
        // Creamos las tareas
        Thread t1 = new Incrementador();
        Thread t2 = new Decrementador();

        // Las ponemos en marcha
        t1.start();
        t2.start();

        // Esperamos a que terminen
        t1.join();
        t2.join();

        // Simplemente se muestra el valor final de la variable:
        System.out.println(n);
    }
}
