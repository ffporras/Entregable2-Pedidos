
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;

public class ProcesamientoEnviosTest {

    private ProcesamientoEnvios procesamientoEnvios;
    private Pedido pedido;

    @BeforeEach
    public void setUp() {
        procesamientoEnvios = new ProcesamientoEnvios(2); // Usamos 2 hilos para el procesamiento
        pedido = new Pedido(1, false);
        pedido.setEstado(EstadoPedido.EMPAQUETADO); // Aseguramos que el estado inicial sea EMPAQUETADO
    }
    /*
        @Test
        public void testEnviarPedidoCambiaEstado() throws InterruptedException, ExecutionException, TimeoutException {
            // Enviar el pedido
            procesamientoEnvios.enviar(pedido);

            // Esperamos un tiempo razonable para que el envío se complete
            TimeUnit.MILLISECONDS.sleep(400); // Un poco más que el tiempo simulado en enviar

            // Verificamos que el estado haya cambiado a ENVIADO
            assertEquals(EstadoPedido.ENVIADO, pedido.getEstado(), "El pedido debería estar en estado ENVIADO.");
        }
    */
    @Test
    public void testHilosActivos() throws InterruptedException {
        // Enviamos múltiples pedidos para verificar el conteo de hilos activos
        for (int i = 0; i < 5; i++) {
            procesamientoEnvios.enviar(new Pedido(i + 2, false));
        }

        // Esperamos un tiempo razonable para que todos los envíos se completen
        TimeUnit.MILLISECONDS.sleep(400); // Un poco más que el tiempo simulado en enviar

        // Verificamos que el número de hilos activos sea 0 después de completar el envío
        assertEquals(0, procesamientoEnvios.getHilosActivos(), "No debería haber hilos activos después de enviar los pedidos.");
    }

    @AfterEach
    public void tearDown() {
        procesamientoEnvios.shutdown();
    }
}
