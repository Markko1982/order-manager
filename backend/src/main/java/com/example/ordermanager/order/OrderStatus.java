package com.example.ordermanager.order;

public enum OrderStatus {
    PENDING,    // Pedido criado, aguardando confirmação
    CONFIRMED,  // Pedido confirmado, aguardando envio
    SHIPPED,    // Pedido enviado
    DELIVERED,  // Pedido entregue
    CANCELLED   // Pedido cancelado
}
