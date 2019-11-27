package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.OperationsWithClients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface OperationsWithClientsRepository extends JpaRepository<OperationsWithClients, Integer> {
    ArrayList<OperationsWithClients> findByClientIdOrderByDate(Integer clientId);
    ArrayList<OperationsWithClients> findByMethodOrderByDate(String method);
}
