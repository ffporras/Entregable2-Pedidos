
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;

public class ProcesadorDePedidosTest {

    private ProcesadorDePedidos procesadorDePedidos;

    @BeforeEach
    public void setUp() {
        procesadorDePedidos = new ProcesadorDePedidos();
    }

    @Test
    public void testAgregarPedido() {
        Pedido pedido1 = new Pedido(1, false);
        Pedido pedido2 = new Pedido(2, true); // Pedido urgente

        procesadorDePedidos.agregarPedido(pedido1);
        procesadorDePedidos.agregarPedido(pedido2);

        // Verificamos que la cola de pedidos contiene los dos pedidos agregados
        assertEquals(2, procesadorDePedidos.colaPedidos.size(), "El tamaño de la cola de pedidos debe ser 2.");
    }

    @Test
    public void testProcesarPedidos() {
        Pedido pedido1 = new Pedido(1, false);
        Pedido pedido2 = new Pedido(2, true); // Pedido urgente

        procesadorDePedidos.agregarPedido(pedido1);
        procesadorDePedidos.agregarPedido(pedido2);

        // Procesamos los pedidos
        procesadorDePedidos.procesarPedidos();

        // Verificamos el estado final de los pedidos
        assertEquals(EstadoPedido.ENVIADO, pedido1.getEstado(), "El pedido 1 debería estar en estado ENVIADO.");
        assertEquals(EstadoPedido.ENVIADO, pedido2.getEstado(), "El pedido 2 debería estar en estado ENVIADO.");
    }

    @Test
    public void testVerificarEstadosPedido() throws ExecutionException, InterruptedException {
        Pedido pedido = new Pedido(1, false);
        ProcesadorDePedidos procesadorDePedidos = new ProcesadorDePedidos();

        // Procesamos el pedido paso por paso
        CompletableFuture<Void> pagoVerificado = CompletableFuture.runAsync(() -> procesadorDePedidos.verificadorDePago.verificar(pedido))
                .thenRun(() -> pedido.setEstado(EstadoPedido.PAGO_VERIFICADO));

        // Esperamos a que se complete la verificación de pago
        pagoVerificado.get();
        assertEquals(EstadoPedido.PAGO_VERIFICADO, pedido.getEstado(), "El pedido debería estar en estado PAGO_VERIFICADO.");

        // Procesamos el empaquetado
        CompletableFuture<Void> empaquetado = CompletableFuture.runAsync(() -> procesadorDePedidos.empaquetador.empaquetar(pedido))
                .thenRun(() -> pedido.setEstado(EstadoPedido.EMPAQUETADO));

        // Esperamos a que se complete el empaquetado
        empaquetado.get();
        assertEquals(EstadoPedido.EMPAQUETADO, pedido.getEstado(), "El pedido debería estar en estado EMPAQUETADO.");

        // Procesamos el envío
        CompletableFuture<Void> enviado = CompletableFuture.runAsync(() -> procesadorDePedidos.enviador.enviar(pedido))
                .thenRun(() -> pedido.setEstado(EstadoPedido.ENVIADO));

        // Esperamos a que se complete el envío
        enviado.get();
        assertEquals(EstadoPedido.ENVIADO, pedido.getEstado(), "El pedido debería estar en estado ENVIADO.");
    }
    @Test
    public void testPrioridadPedidos() {
        Pedido pedido1 = new Pedido(1, false); // Pedido normal
        Pedido pedido2 = new Pedido(2, true);  // Pedido urgente

        procesadorDePedidos.agregarPedido(pedido1);
        procesadorDePedidos.agregarPedido(pedido2);

        // El pedido urgente debe procesarse antes que el pedido normal
        Pedido pedidoPrioritario = procesadorDePedidos.colaPedidos.poll();
        assertEquals(2, pedidoPrioritario.getId(), "El pedido urgente debería ser procesado primero.");
    }

    @Test
    public void testShutdown() {
        procesadorDePedidos.shutdown();

        // Verificamos que los ejecutores están apagados
        assertTrue(procesadorDePedidos.verificadorDePago.executor.isShutdown(), "El ejecutor de verificación de pago debería estar apagado.");
        assertTrue(procesadorDePedidos.empaquetador.pool.isShutdown(), "El pool de empaquetado debería estar apagado.");
        assertTrue(procesadorDePedidos.enviador.executor.isShutdown(), "El ejecutor de envíos debería estar apagado.");
    }
}
