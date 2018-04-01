import es.upm.babel.cclib.Producto;
import es.upm.babel.cclib.Almacen;

import es.upm.babel.cclib.Semaphore;

/**
 * Implementación de la clase Almacen que permite el almacenamiento
 * FIFO de hasta un determinado número de productos y el uso
 * simultáneo del almacén por varios threads.
 */
class AlmacenN implements Almacen {
    private int capacidad = 0;
    private Producto[] almacenado = null;
    private int nDatos = 0;
    private int aExtraer = 0;
    private int aInsertar = 0;

    private Semaphore semaphore= null;

    public AlmacenN(int n) {
        capacidad = n;
        almacenado = new Producto[capacidad];
        nDatos = 0;
        aExtraer = 0;
        aInsertar = 0;

        semaphore= new Semaphore(1);
    }

    public void almacenar(Producto producto) {
        // sincronización para poder almacenar.
        while (true) {
            semaphore.await();
            if (nDatos == capacidad)
                semaphore.signal(); //If there's no space, wait for signal
            else
                break;//If there's space, fill
        }

        // Sección crítica
        almacenado[aInsertar] = producto;
        nDatos++;
        aInsertar++;
        aInsertar %= capacidad;

        // sincronización para poder extraer.
        semaphore.signal();
    }

    public Producto extraer() {
        Producto result;

        // sincronización para poder extraer.
        while (true) {
            semaphore.await();
            if(nDatos==0)
                semaphore.signal();//If no products, wait
            else
                break;//If there are products, consume
        }
        // Sección crítica
        result = almacenado[aExtraer];
        almacenado[aExtraer] = null;
        nDatos--;
        aExtraer++;
        aExtraer %= capacidad;

        // sincronización para poder almacenar.
        semaphore.signal();

        return result;
    }
}
