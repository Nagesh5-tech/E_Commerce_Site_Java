package your.base.package.controller;

import your.base.package.model.Cart;
import your.base.package.model.Order;
import your.base.package.repository.CartRepository;
import your.base.package.repository.OrderRepository;
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
    public ResponseEntity<?> createOrderFromCart(@PathVariable Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return ResponseEntity.status(404).body("Cart not found");
        if (cart.getProducts().isEmpty()) return ResponseEntity.status(400).body("Cart is empty");

        Order order = new Order();
        order.setUserId(userId);
        order.setProducts(List.copyOf(cart.getProducts()));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");

        Order savedOrder = orderRepository.save(order);

        cart.getProducts().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) return ResponseEntity.status(404).body("No orders found");
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody String status) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status.replace("\"", "")); // handles raw JSON string
                    return ResponseEntity.ok(orderRepository.save(order));
                })
                .orElse(ResponseEntity.status(404).body("Order not found"));
    }
}