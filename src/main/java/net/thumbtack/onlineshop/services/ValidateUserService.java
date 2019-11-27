package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.dto.UserDTO;

import java.util.HashMap;

public interface ValidateUserService {
    void validateAdmin(UserDTO userDTO);
    void validateEditAdmin(UserDTO userDTO);
    void validateClient(UserDTO userDTO);
    void validateEditClient(UserDTO userDTO);
    HashMap<String, String> getServerSettings(String session);
}
