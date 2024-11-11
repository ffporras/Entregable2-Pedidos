
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public class EmpaquetadoPedido {
    public final ForkJoinPool pool;
    private final AtomicInteger hilosActivos = new AtomicInteger(0); // Contador de hilos

    public EmpaquetadoPedido(int numHilos) {
        this.pool = new ForkJoinPool(numHilos);
    }

    public void empaquetar(Pedido pedido) {
        pool.submit(new EmpaquetarTask(pedido)); // Asigna la tarea al ForkJoinPool
    }

    // Tarea interna que implementa RecursiveAction
    private class EmpaquetarTask extends RecursiveAction {
        private final Pedido pedido;

        public EmpaquetarTask(Pedido pedido) {
            this.pedido = pedido;
        }

        @Override
        protected void compute() {
            try {
                hilosActivos.incrementAndGet(); // Incrementa el contador de hilos
                //System.out.println("Hilos activos en empaquetado: " + hilosActivos.get());

                // Sincroniza el acceso al estado del pedido
                synchronized (pedido) {
                    if (pedido.getEstado() != EstadoPedido.PAGO_VERIFICADO) {
                        throw new IllegalStateException("El pedido no está en estado PAGO_VERIFICADO.");
                    }

                    Thread.sleep(8000); // Simulación de tiempo de empaquetado
                    //pedido.setEstado(EstadoPedido.EMPAQUETADO);
                    //System.out.println("Pedido empaquetado: " + pedido.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                hilosActivos.decrementAndGet(); // Decrementa el contador al terminar
                //System.out.println("Hilos activos después de empaquetado: " + hilosActivos.get());
            }
        }
    }

    public int getHilosActivos() {
        return hilosActivos.get();
    }

    public void shutdown() {
        pool.shutdown(); // Cierra el ForkJoinPool
    }
}

