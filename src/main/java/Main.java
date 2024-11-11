
public class Main {
    public static void main(String[] args) {
        ProcesadorDePedidos procesador = new ProcesadorDePedidos();

        // Crear y agregar varios pedidos, algunos urgentes
        for (int i = 1; i <= 100; i++) {
            Pedido pedido = new Pedido(i, i % 5 == 0); // Cada segundo pedido es urgente
            procesador.agregarPedido(pedido);
        }

        procesador.procesarPedidos();

        // Esperar un tiempo para que todos los pedidos se procesen
        try {
            Thread.sleep(1000); // Tiempo suficiente para completar el procesamiento
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            procesador.shutdown(); // Cierra los ejecutores
        }
    }
}

//Tests
//Falta probar distintas configuraciones de hilos -> Hacer el informe