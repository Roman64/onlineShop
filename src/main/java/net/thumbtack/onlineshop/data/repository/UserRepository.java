package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
