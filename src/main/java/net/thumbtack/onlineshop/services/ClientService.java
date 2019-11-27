package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.model.Product;
import net.thumbtack.onlineshop.data.dto.UserDTO;

import java.util.HashMap;
import java.util.List;


public interface ClientService {

    UserDTO registerClient(UserDTO userDTO);
    String editClientProfile(String session, UserDTO userDTO);
    UserDTO addCash(String session, UserDTO userDTO);
    UserDTO getCash(String session);
    List<UserDTO> getAllClientsByAdmin(String session);
    Boolean isLogin(String sessionId);
    boolean isCorrectPassword(String id, String oldPassword);
    HashMap<String, Client> getClientSessions();
}
