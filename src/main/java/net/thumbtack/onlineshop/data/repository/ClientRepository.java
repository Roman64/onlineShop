package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Client getClientByFirstName(String firstName);
    Client getClientByLoginAndPassword(String login, String password);
    Client getClientByLogin(String login);
}
