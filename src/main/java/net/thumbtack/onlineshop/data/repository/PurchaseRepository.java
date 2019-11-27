package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
}
