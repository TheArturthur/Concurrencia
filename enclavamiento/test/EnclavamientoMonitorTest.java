import com.sun.istack.internal.NotNull;
import com.sun.org.glassfish.gmbal.ParameterNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import es.upm.babel.cclib.*;
import static org.junit.jupiter.api.Assertions.*;

class EnclavamientoMonitorTest {

    @Test
    void avisarPresencia() {
    }

    @Test
    void leerCambioBarrera() {
    }

    @Test
    void leerCambioFreno() {
    }

    @Test
    void leerCambioSemaforo() {
    }

    @Test
    void avisarPasoPorBaliza() {
    }

    @Test
    void colorsAreCorrect() {
        EnclavamientoMonitor enclavamiento = new EnclavamientoMonitor();

        enclavamiento.leerCambioSemaforo(1, Control.Color.ROJO);
        enclavamiento.avisarPasoPorBaliza(1);
        enclavamiento.leerCambioSemaforo(1, Control.Color.VERDE);
        enclavamiento.avisarPasoPorBaliza(2);
        enclavamiento.leerCambioSemaforo(1, Control.Color.VERDE);
        enclavamiento.leerCambioSemaforo(2, Control.Color.VERDE);

        try {
            assertTrue(enclavamiento.areBarrierConditionsLocked(), "No barrier locked!");
        } catch (AssertionError ae) {
            ae.printStackTrace();
        }
        try {
            assertTrue(enclavamiento.areBrakeConditionsLocked(), "No brake locked!");
        } catch (AssertionError ae) {
            ae.printStackTrace();
        }

        try {
            assertTrue(enclavamiento.areSemaphoreConditionsLocked(), "No semaphore locked!");
        } catch (AssertionError ae) {
            ae.printStackTrace();
        }
    }


    public void main (String[] args) {


        colorsAreCorrect();

    }
}