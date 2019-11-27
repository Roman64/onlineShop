package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.CartWithProducts;
import net.thumbtack.onlineshop.data.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartWithProductsRepository extends JpaRepository<CartWithProducts, Integer> {
}
