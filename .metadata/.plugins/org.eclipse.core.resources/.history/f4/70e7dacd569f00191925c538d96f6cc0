public class Cruce {
    private static Control dispositivos = new ControlSimulado();

    public static class DetectorPresencia extends Thread {
        private Enclavamiento enclavamiento;
        public DetectorPresencia(Enclavamiento e) {
            enclavamiento = e;
        }
        public void run() {
            boolean presencia = true;
            while (true) {
                dispositivos.detectarPresencia(presencia);
                enclavamiento.avisarPresencia(presencia);
                presencia = !presencia;
            }
        }
    }

    public static class ControladorBarrera extends Thread {
        private Enclavamiento enclavamiento;
        public ControladorBarrera(Enclavamiento e) {
            enclavamiento = e;
        }
        public void run() {
            boolean abierta = true;
            while (true) {
                if (abierta) {
                    dispositivos.abrirBarrera();
                }
                else {
                    dispositivos.cerrarBarrera();
                }
                abierta = enclavamiento.leerCambioBarrera(abierta);
            }
        }
    }

    public static class ControladorFreno extends Thread {
        private Enclavamiento enclavamiento;
        public ControladorFreno(Enclavamiento e) {
            enclavamiento = e;
        }
        public void run() {
            boolean accionado = false;
            while (true) {
                dispositivos.accionarFreno(accionado);
                enclavamiento.leerCambioFreno(accionado);
                accionado = !accionado;
            }
        }
    }

    public static class ControladorSemaforo extends Thread {
        private int i;
        private Enclavamiento enclavamiento;
        public ControladorSemaforo(int i,
                                   Enclavamiento e) {
            this.i = i;
            enclavamiento = e;
        }
        public void run() {
            Control.Color color = Control.Color.ROJO;
            while (true) {
                dispositivos.encenderSemaforo(i, color);
                color = enclavamiento.leerCambioSemaforo(i, color);
            }
        }
    }

    public static class DetectorBaliza extends Thread {
        private int i;
        private Enclavamiento enclavamiento;
        public DetectorBaliza(int i,
                              Enclavamiento e) {
            this.i = i;
            enclavamiento = e;
        }

        public void run() {
            while (true) {
                dispositivos.detectarTren(i);
                enclavamiento.avisarPasoPorBaliza(i);
            }
        }
    }

    public static void main(String[] args) {
        Enclavamiento enclavamiento;

        //enclavamiento = new EnclavamientoMonitor();
        enclavamiento = new EnclavamientoCSP();
/*
        new DetectorPresencia(enclavamiento).start();
        new ControladorBarrera(enclavamiento).start();
        new ControladorFreno(enclavamiento).start();
        new ControladorSemaforo(1, enclavamiento).start();
        new ControladorSemaforo(2, enclavamiento).start();
        new ControladorSemaforo(3, enclavamiento).start();
        new DetectorBaliza(1, enclavamiento).start();
        new DetectorBaliza(2, enclavamiento).start();
        new DetectorBaliza(3, enclavamiento).start();
*/
        // PRUEBAS PARA ENCLAVAMIENTO MONITOR:
        /*
        System.out.println("hola");
        new Thread( new Runnable() {
            @Override
            public void run() {
                enclavamiento.leerCambioBarrera(true);
                System.out.println("hola barrera");
            }
        }).start();

        new Thread( new Runnable() {
            @Override
            public void run() {
                enclavamiento.leerCambioFreno(false);
                System.out.println("hola freno");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                enclavamiento.leerCambioSemaforo(1, Control.Color.VERDE);
                System.out.println("Hola 1");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                enclavamiento.leerCambioSemaforo(2, Control.Color.VERDE);
                System.out.println("Hola 2");
            }
        }).start();

        try {
            Thread.sleep(1000);
        }catch (Exception e){}
        enclavamiento.avisarPasoPorBaliza(1);
        enclavamiento.avisarPasoPorBaliza(2);
        */

        // PRUEBAS PARA ENCLAVAMIENTO CSP:
        System.out.println("hola");
        new Thread(new Runnable() {
            @Override
            public void run() {
                enclavamiento.leerCambioBarrera(true);
                System.out.println("barrera bloqueada");
                enclavamiento.avisarPasoPorBaliza(1);
                System.out.println("tren pasa 1");
            }
        }).start();
    }
}
