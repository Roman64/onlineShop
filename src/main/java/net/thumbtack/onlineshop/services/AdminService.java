package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.dto.OperationsWithClientsDTO;
import net.thumbtack.onlineshop.data.model.Admin;
import net.thumbtack.onlineshop.data.dto.UserDTO;

import java.util.List;


public interface AdminService {

    Boolean isLogin(String sessionId);
    Boolean isCorrectPassword(String sessionId, String password);
    UserDTO registerAdmin(UserDTO userDTO);
    String editProfile(String session, UserDTO userDTO);
    Admin transformUserDTOtoAdmin(UserDTO userDTO);
    List<OperationsWithClientsDTO> getOperationsHistory(String session, Integer clientId, String method);

}
