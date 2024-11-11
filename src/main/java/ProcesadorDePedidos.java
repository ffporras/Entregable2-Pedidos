
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcesadorDePedidos {
    public final PriorityBlockingQueue<Pedido> colaPedidos = new PriorityBlockingQueue<>(10,
            (p1, p2) -> Boolean.compare(p2.isPrioridad(), p1.isPrioridad())); // Prioriza pedidos urgentes

    public final ProcesamientoPago verificadorDePago = new ProcesamientoPago(3); // 3 hilos para verificación
    public final EmpaquetadoPedido empaquetador = new EmpaquetadoPedido(2); // 4 hilos para empaquetado
    public final ProcesamientoEnvios enviador = new ProcesamientoEnvios(2); // 2 hilos para envío

    private final AtomicInteger pedidosEnProceso = new AtomicInteger(0); // Contador de pedidos en proceso

    // Contadores de tiempo acumulativo para cada etapa
    private long tiempoTotalVerificacion = 0;
    private long tiempoTotalEmpaquetado = 0;
    private long tiempoTotalEnvio = 0;

    // Códigos ANSI para colores
    private static final String COLOR_VIOLETA = "\u001B[35m"; // Violeta
    private static final String COLOR_AMARILLO = "\u001B[33m"; // Amarillo
    private static final String COLOR_VERDE = "\u001B[32m"; // Verde
    private static final String COLOR_RESET = "\u001B[0m"; // Resetear color


    public void agregarPedido(Pedido pedido) {
        colaPedidos.offer(pedido); // Agregar el pedido a la cola
    }

    public void procesarPedidos() {
        System.out.println("INFORMACIÓN SOBRE LA EJECUCIÓN: \n");
        System.out.println("Cantidad de hilos para verificación: " + 3);
        System.out.println("Cantidad de hilos para empaquetado: " + 4);
        System.out.println("Cantidad de hilos para envío: " + 2);
        System.out.println("\n Cantidad de pedidos a procesar: " + colaPedidos.size() + "\n");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Procesar cada pedido
        while (!colaPedidos.isEmpty()) {
            Pedido pedido = colaPedidos.poll(); // Extraer el pedido con mayor prioridad
            pedidosEnProceso.incrementAndGet(); // Incrementar el contador de pedidos en proceso
            System.out.println("Cantidad de pedidos en proceso: " + pedidosEnProceso.get());

            // Agregar el procesamiento de este pedido como un futuro en la lista
            CompletableFuture<Void> future = procesarPedido(pedido);
            futures.add(future);
        }

        // Esperar que todos los pedidos se procesen completamente (pago, empaquetado y envío)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Imprimir el tiempo total de cada etapa en segundos
        System.out.println("\n TIEMPO DE EJECUCIÓN POR ETAPAS: \n");
        System.out.printf("Tiempo total de procesamiento de pago es de: %.9f segundos%n", tiempoTotalVerificacion / 1_000_000_000.0, "\n");
        System.out.printf("Tiempo total de procesamiento de empaquetado es de: %.9f segundos%n", tiempoTotalEmpaquetado / 1_000_000_000.0, "\n");
        System.out.printf("Tiempo total de procesamiento de envío es de: %.9f segundos%n", tiempoTotalEnvio / 1_000_000_000.0, "\n");

        // Una vez completado todo el procesamiento, apagamos los ejecutores
        shutdown();
    }

    public CompletableFuture<Void> procesarPedido(Pedido pedido) {
        // Temporizadores para cada etapa
        long inicioVerificacion = System.nanoTime();

        return CompletableFuture.runAsync(() -> {
            /*if (pedido.getEstado() != EstadoPedido.PAGO_PENDIENTE) {
                throw new IllegalStateException("El pedido no ha sido verificado correctamente.");
            }
            */
            verificadorDePago.verificar(pedido);
            pedido.setEstado(EstadoPedido.PAGO_VERIFICADO);
            synchronized (pedido) {
                if (pedido.getEstado() != EstadoPedido.PAGO_VERIFICADO) {
                    throw new IllegalStateException("El pedido no ha sido verificado correctamente.");
                }
            }
            long finVerificacion = System.nanoTime();
            long tiempoVerificacion = finVerificacion - inicioVerificacion;
            tiempoTotalVerificacion += tiempoVerificacion; // Acumular el tiempo total de verificación

            System.out.printf("Pago procesado para pedido: %d Estado: %s%s%s%n",
                    pedido.getId(), COLOR_VIOLETA, pedido.getEstado(), COLOR_RESET);
            //System.out.printf("Tiempo de verificación para el pedido %d: %.9f segundos%n", pedido.getId(), tiempoVerificacion / 1_000_000_000.0);
        }).thenRunAsync(() -> {
            long inicioEmpaquetado = System.nanoTime();
            synchronized (pedido) {
                if (pedido.getEstado() != EstadoPedido.PAGO_VERIFICADO) {
                    throw new IllegalStateException("El pedido no ha sido verificado correctamente.");
                }
            }
            empaquetador.empaquetar(pedido);
            pedido.setEstado(EstadoPedido.EMPAQUETADO);
            if (pedido.getEstado() != EstadoPedido.EMPAQUETADO) {
                throw new IllegalStateException("El pedido no ha sido empaquetado correctamente.");
            }
            long finEmpaquetado = System.nanoTime();
            long tiempoEmpaquetado = finEmpaquetado - inicioEmpaquetado;
            tiempoTotalEmpaquetado += tiempoEmpaquetado; // Acumular el tiempo total de empaquetado

            System.out.printf("Pedido empaquetado: %d Estado: %s%s%s%n",
                    pedido.getId(), COLOR_AMARILLO, pedido.getEstado(), COLOR_RESET);
            //System.out.printf("Tiempo de empaquetado para el pedido %d: %.9f segundos%n", pedido.getId(), tiempoEmpaquetado / 1_000_000_000.0);
        }).thenRunAsync(() -> {
            long inicioEnvio = System.nanoTime();
            synchronized (pedido) {
                if (pedido.getEstado() != EstadoPedido.EMPAQUETADO) {
                    throw new IllegalStateException("El pedido no ha sido empaquetado correctamente.");
                }
            }
            enviador.enviar(pedido);
            pedido.setEstado(EstadoPedido.ENVIADO);
            long finEnvio = System.nanoTime();
            long tiempoEnvio = finEnvio - inicioEnvio;
            tiempoTotalEnvio += tiempoEnvio; // Acumular el tiempo total de envío

            System.out.printf("Pedido enviado: %d Estado: %s%s%s -> Número de tracking: %s%n",
                    pedido.getId(), COLOR_VERDE, pedido.getEstado(), COLOR_RESET, enviador.generarNumeroSeguimiento());

            //System.out.printf("Tiempo de envío para el pedido %d: %.9f segundos%n", pedido.getId(), tiempoEnvio / 1_000_000_000.0);
        }).exceptionally(e -> {
            System.err.println("Error procesando pedido: " + e.getMessage());
            return null;
        });
    }

    public void shutdown() {
        verificadorDePago.shutdown();
        empaquetador.shutdown();
        enviador.shutdown();
    }
}
