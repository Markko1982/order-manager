from pathlib import Path

path = Path("src/main/java/com/example/ordermanager/order/OrderService.java")
data = path.read_text(encoding="utf-8")

old = """    public void updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));

        order.setStatus(status);
        orderRepository.save(order);
    }
"""

new = """    public void updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));

        OrderStatus currentStatus = order.getStatus();

        // Regras de transição:
        // - PENDING -> PAID
        // - PENDING -> CANCELED
        // - Se já estiver PAID ou CANCELED, não pode mudar mais
        if (currentStatus == OrderStatus.PENDING) {
            if (newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELED) {
                throw new IllegalStateException("Transição de status inválida: " + currentStatus + " -> " + newStatus);
            }
        } else {
            // PAID ou CANCELED não podem ir para outro status
            if (newStatus != currentStatus) {
                throw new IllegalStateException("Pedido já está " + currentStatus + " e não pode ser alterado.");
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }
"""

if old not in data:
    raise SystemExit("Bloco antigo de updateStatus não encontrado; nada foi alterado.")

path.write_text(data.replace(old, new), encoding="utf-8")
print("updateStatus() atualizado com regras de transição de status.")
