package src;

import es.upm.babel.cclib.Producto;
import es.upm.babel.cclib.Semaphore;
import es.upm.babel.cclib.Almacen;

// TODO: importar la clase de los semáforos.

/**
 * Implementación de la clase Almacen que permite el almacenamiento
 * de producto y el uso simultáneo del almacen por varios threads.
 */
class Almacen1 implements Almacen {
   // Producto a almacenar: null representa que no hay producto
   private Producto almacenado = null;

   // TODO: declaración e inicialización de los semáforos
   // necesarios
   private volatile Semaphore producerSem = new Semaphore(1);
   private volatile Semaphore consumerSem = new Semaphore(0);
   
   public Almacen1() {
   }

   public void almacenar(Producto producto) {
      // TODO: protocolo de acceso a la sección crítica y código de
      // sincronización para poder almacenar.
	  producerSem.await();
	  
      // Sección crítica
      almacenado = producto;

      // TODO: protocolo de salida de la sección crítica y código de
      // sincronización para poder extraer.
      consumerSem.signal();
   }

   public Producto extraer() {
      Producto result;
      // TODO: protocolo de acceso a la sección crítica y código de
      // sincronización para poder extraer.
      consumerSem.await();
     
      // Sección crítica
      result = almacenado;
      almacenado = null;
      
      // TODO: protocolo de salida de la sección crítica y código de
      // sincronización para poder almacenar.
      producerSem.signal();
      return result;
   }
}
