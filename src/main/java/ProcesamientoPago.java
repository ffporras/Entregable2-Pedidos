
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcesamientoPago {
    public final ExecutorService executor;
    private final AtomicInteger hilosActivos = new AtomicInteger(0); // Contador de hilos

    public ProcesamientoPago(int numHilos) {
        this.executor = Executors.newFixedThreadPool(numHilos);
    }

    public void verificar(Pedido pedido) {
        executor.submit(() -> {
            try {
                hilosActivos.incrementAndGet(); // Incrementa el contador de hilos
                //System.out.println("Hilos activos en verificación de pago: " + hilosActivos.get());

                // Sincroniza el acceso al estado del pedido
                synchronized (pedido) {
                    if (pedido.getEstado() != EstadoPedido.PAGO_PENDIENTE) {
                        throw new IllegalStateException("El pedido no está en estado PAGO_PENDIENTE.");
                    }

                    Thread.sleep(5000); // Simulación de tiempo de verificación
                    //pedido.setEstado(EstadoPedido.PAGO_VERIFICADO);
                    //System.out.println("Pago verificado para el pedido: " + pedido.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                hilosActivos.decrementAndGet(); // Decrementa el contador al terminar
                //System.out.println("Hilos activos después de verificación: " + hilosActivos.get());
            }
        });
    }

    public int getHilosActivos() {
        return hilosActivos.get();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
