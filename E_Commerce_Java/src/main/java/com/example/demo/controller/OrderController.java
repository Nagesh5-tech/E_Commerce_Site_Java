package com.example.demo.controller;

import com.example.demo.model.Cart;
import com.example.demo.model.Order;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @PostMapping("/{userId}")
    public ResponseEntity<Order> createOrderFromCart(@PathVariable Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getProducts().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setProducts(List.copyOf(cart.getProducts()));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cart.getProducts().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId, @RequestBody String status) {
        return orderRepository.findById(orderId)
            .map(order -> {
                order.setStatus(status.replace("\"", "")); // Remove quotes if sent as JSON string
                return ResponseEntity.ok(orderRepository.save(order));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}