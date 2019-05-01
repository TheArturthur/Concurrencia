import es.upm.babel.cclib.Monitor;

public class EnclavamientoMonitor implements Enclavamiento {

  Monitor mutex = new Monitor();
  
  @Override
  public void avisarPresencia(boolean presencia) {
    mutex.enter();
    // chequeo de la PRE
    // chequeo de la CPRE y posible bloqueo
    // implementacion de la POST
    // codigo de desbloqueo
    mutex.leave();
  }

  @Override
  public boolean leerCambioBarrera(boolean actual) {
    mutex.enter();
    // chequeo de la PRE
    // chequeo de la CPRE y posible bloqueo
    // implementacion de la POST
    // codigo de desbloqueo
    mutex.leave();
    return false;
  }

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

  @Override
  public Control.Color leerCambioSemaforo(int i, Control.Color actual) {
    mutex.enter();
    // chequeo de la PRE
    // chequeo de la CPRE y posible bloqueo
    // implementacion de la POST
    // codigo de desbloqueo
    mutex.leave();
    return null;
  }

  @Override
  public void avisarPasoPorBaliza(int i) {
    mutex.enter();
    // chequeo de la PRE
    // chequeo de la CPRE y posible bloqueo
    // implementacion de la POST
    // codigo de desbloqueo
    mutex.leave();
  }
 
}
