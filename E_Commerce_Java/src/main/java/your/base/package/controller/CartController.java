package your.base.package.controller;

import your.base.package.model.Cart;
import your.base.package.model.Product;
import your.base.package.repository.CartRepository;
import your.base.package.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @PostMapping("/{userId}/add/{productId}")
    public ResponseEntity<?> addProductToCart(@PathVariable Long userId, @PathVariable Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return c;
        });
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return ResponseEntity.status(404).body("Product not found");
        }
        cart.getProducts().add(product);
        cart.setTotalPrice(cart.getProducts().stream().mapToDouble(Product::getPrice).sum());
        Cart saved = cartRepository.save(cart);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public ResponseEntity<?> removeProductFromCart(@PathVariable Long userId, @PathVariable Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return ResponseEntity.status(404).body("Cart not found");
        }
        boolean removed = cart.getProducts().removeIf(p -> p.getId().equals(productId));
        if (!removed) {
            return ResponseEntity.status(404).body("Product not in cart");
        }
        cart.setTotalPrice(cart.getProducts().stream().mapToDouble(Product::getPrice).sum());
        Cart saved = cartRepository.save(cart);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId) {
        return cartRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("Cart not found"));
    }
}