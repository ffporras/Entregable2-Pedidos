
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Pedido {
    private final int id;
    private final boolean prioridad; // true si es urgente
    private final AtomicReference<EstadoPedido> estado;

    public Pedido(int id, boolean prioridad) {
        this.id = id;
        this.prioridad = prioridad;
        this.estado = new AtomicReference<>(EstadoPedido.PAGO_PENDIENTE); // Estado inicial
    }

    public int getId() {
        return id;
    }

    public boolean isPrioridad() {
        return prioridad;
    }

    public EstadoPedido getEstado() {
        return estado.get();
    }

    public void setEstado(EstadoPedido nuevoEstado) {
        estado.set(nuevoEstado);
    }

    // Método toString para imprimir el pedido
    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", esUrgente=" + prioridad +
                ", estado=" + estado +
                '}';
    }

    // Sobrescribir equals y hashCode para comparar pedidos por ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id == pedido.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /*
    // Implementación de compareTo para priorizar pedidos urgentes
    @Override
    public int compareTo(Pedido otroPedido) {
        // Los pedidos urgentes tienen mayor prioridad (devuelve negativo si el pedido actual es más urgente)
        return Boolean.compare(otroPedido.esUrgente, this.esUrgente);
    }
    */
}

