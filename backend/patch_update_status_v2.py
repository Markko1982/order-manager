from pathlib import Path

path = Path("src/main/java/com/example/ordermanager/order/OrderService.java")
text = path.read_text(encoding="utf-8")

marker = "public void updateStatus(Long id, OrderStatus"
if marker not in text:
    raise SystemExit("Não encontrei o método updateStatus(Long id, OrderStatus ...) no OrderService.java")

start = text.index(marker)

# achar o primeiro '{' depois da assinatura
brace_pos = text.index("{", start)

# agora vamos achar o '}' que fecha esse método, contando chaves
depth = 0
end = None
for i in range(brace_pos, len(text)):
    ch = text[i]
    if ch == "{":
        depth += 1
    elif ch == "}":
        depth -= 1
        if depth == 0:
            end = i + 1  # incluir a chave de fechamento
            break

if end is None:
    raise SystemExit("Não consegui encontrar o fim do método updateStatus.")

new_method = """    public void updateStatus(Long id, OrderStatus newStatus) {
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

new_text = text[:start] + new_method + text[end:]
path.write_text(new_text, encoding="utf-8")
print("updateStatus() substituído com sucesso com regras de transição de status.")
