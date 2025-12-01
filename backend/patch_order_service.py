from pathlib import Path

# Caminho do OrderService
path = Path("src/main/java/com/example/ordermanager/order/OrderService.java")
data = path.read_text(encoding="utf-8")

old_block = """if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalStateException("Estoque insuficiente para o produto: " + product.getName());
            }"""

new_block = """if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalStateException(
                        "Estoque insuficiente para o produto '" + product.getName() +
                        "'. Disponível: " + product.getStock() +
                        ", solicitado: " + itemDTO.getQuantity()
                );
            }"""

if old_block not in data:
    raise SystemExit("Bloco antigo não encontrado; nada foi alterado.")

path.write_text(data.replace(old_block, new_block), encoding="utf-8")
print("OrderService.java atualizado com nova mensagem de estoque insuficiente.")
