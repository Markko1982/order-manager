from pathlib import Path

path = Path("src/main/java/com/example/ordermanager/order/OrderService.java")
data = path.read_text(encoding="utf-8")

marker = "order.setTotalAmount(total);"

if marker not in data:
    raise SystemExit("Marcador 'order.setTotalAmount(total);' não encontrado no OrderService.java")

injection = """
        // regra de negócio: valor máximo permitido por pedido
        if (total.compareTo(new java.math.BigDecimal("1000.00")) > 0) {
            throw new IllegalStateException("Valor máximo do pedido excedido. Total calculado: " + total);
        }
"""

idx = data.index(marker) + len(marker)
new_data = data[:idx] + injection + data[idx:]

path.write_text(new_data, encoding="utf-8")
print("Regra de total máximo adicionada no OrderService.")
