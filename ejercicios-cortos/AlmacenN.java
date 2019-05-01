import es.upm.babel.cclib.Producto;
import es.upm.babel.cclib.Almacen;
import es.upm.babel.cclib.Semaphore;

// TODO: importar la clase de los semáforos.

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

   // TODO: declaración de los semáforos necesarios
   //3 semaphores: producers, consumer and inner for items stored:
   private volatile Semaphore producerSem;
   private volatile Semaphore consumerSem;
   private volatile Semaphore storageSem;

   public AlmacenN(int n) {
      capacidad = n;
      almacenado = new Producto[capacidad];
      nDatos = 0;
      aExtraer = 0;
      aInsertar = 0;

      // TODO: inicialización de los semáforos
      producerSem = new Semaphore (1);
      consumerSem = new Semaphore (0);
      storageSem = new Semaphore (1);
   }

   public void almacenar(Producto producto) {
      // TODO: protocolo de acceso a la sección crítica y código de
      // sincronización para poder almacenar.
	   producerSem.await();
	   storageSem.await();
	   
      // Sección crítica
      almacenado[aInsertar] = producto;
      nDatos++;
      aInsertar++;
      aInsertar %= capacidad;

      // TODO: protocolo de salida de la sección crítica y código de
      // sincronización para poder extraer.
      storageSem.signal();
      consumerSem.signal();
      producerSem.signal();
   }

   public Producto extraer() {
      Producto result;

      // TODO: protocolo de acceso a la sección crítica y código de
      // sincronización para poder extraer.
      consumerSem.await();
      storageSem.await();

      // Sección crítica
      result = almacenado[aExtraer];
      almacenado[aExtraer] = null;
      nDatos--;
      aExtraer++;
      aExtraer %= capacidad;

      // TODO: protocolo de salida de la sección crítica y código de
      // sincronización para poder almacenar.
      storageSem.signal();
      producerSem.signal();
      
      return result;
   }
}
