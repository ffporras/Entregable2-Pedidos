
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals; // Importa las aserciones correctamente
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmpaquetadoPedidoTest {

    @Test
    public void testInicializacion() {
        // Inicializa con 4 hilos
        EmpaquetadoPedido empaquetadoPedido = new EmpaquetadoPedido(4);

        // Verifica que no haya hilos activos al inicio
        assertEquals(0, empaquetadoPedido.getHilosActivos());
    }

    @Test
    public void testEmpaquetarIncrementaHilosActivos() throws InterruptedException {
        EmpaquetadoPedido empaquetadoPedido = new EmpaquetadoPedido(4);
        Pedido pedido = new Pedido(1, false);

        // Simulamos que el pedido está en estado PAGO_VERIFICADO
        pedido.setEstado(EstadoPedido.PAGO_VERIFICADO);

        // Empaqueta el pedido
        empaquetadoPedido.empaquetar(pedido);

        // Espera un poco para que el hilo se active
        Thread.sleep(100);

        // Verifica que el número de hilos activos haya aumentado
        assertTrue(empaquetadoPedido.getHilosActivos() > 0);
    }


    @Test
    public void testShutdown() {
        EmpaquetadoPedido empaquetadoPedido = new EmpaquetadoPedido(4);

        // Llama al shutdown para cerrar el pool
        empaquetadoPedido.shutdown();

        // Verifica que el pool haya sido cerrado
        assertTrue(empaquetadoPedido.pool.isShutdown());
    }
}
