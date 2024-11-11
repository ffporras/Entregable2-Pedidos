
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcesamientoEnvios {
    public final ExecutorService executor;
    private final AtomicInteger hilosActivos = new AtomicInteger(0); // Contador de hilos

    public ProcesamientoEnvios(int numHilos) {
        this.executor = Executors.newFixedThreadPool(numHilos);
    }

    public void enviar(Pedido pedido) {
        executor.submit(() -> {
            try {
                hilosActivos.incrementAndGet(); // Incrementa el contador de hilos
                //System.out.println("Hilos activos en envío: " + hilosActivos.get());

                // Sincroniza el acceso al estado del pedido
                synchronized (pedido) {
                    if (pedido.getEstado() != EstadoPedido.EMPAQUETADO) {
                        throw new IllegalStateException("El pedido no está en estado EMPAQUETADO.");
                    }

                    Thread.sleep(300); // Simulación de tiempo de envío
                    //pedido.setEstado(EstadoPedido.ENVIADO);
                    //System.out.println("Pedido enviado: " + pedido.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                hilosActivos.decrementAndGet(); // Decrementa el contador al terminar
                //System.out.println("Hilos activos después de envío: " + hilosActivos.get());
            }
        });
    }

    public String generarNumeroSeguimiento() {
        return "TRACK" + (int)(Math.random() * 10000);
    }

    public int getHilosActivos() {
        return hilosActivos.get();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
