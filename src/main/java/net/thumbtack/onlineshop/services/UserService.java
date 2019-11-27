package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.dto.UserDTO;


public interface UserService {

    UserDTO login(String login, String password);
    UserDTO logout(String sessionId);
    UserDTO getUserInfo(String sessionId);
    String clearBase();
}
