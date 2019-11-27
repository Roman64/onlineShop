package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.repository.*;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.data.model.Admin;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartWithProductsRepository cartWithProductsRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AdminServiceImpl adminService;
    @Autowired
    private ClientServiceImpl clientService;
    @Autowired
    private SoldProductsRepository soldProductsRepository;
    @Autowired
    private OperationsWithClientsRepository operations;

    @Override
    public UserDTO login(String login, String password) {
        UserDTO userDTO = new UserDTO();
        String session = UUID.randomUUID().toString();
        Admin admin = adminRepository.getAdminByLoginAndPassword(login, password);
        if (admin == null) {
            Client client = clientRepository.getClientByLoginAndPassword(login, password);
            if (client == null) {
                userDTO.clearFieldsToGetError();
                userDTO.getErrors().add(new UserError(UserErrorCode.WRONG_LOGIN_OR_PASSWORD, "Неверные данные", "login, password"));
                return userDTO;
            }
            transformClientToUserDTO(client, userDTO);
            clientService.getClientSessions().put(session, client);
            userDTO.setUuid(session);
            return userDTO;
        }
        adminService.getSessions().put(session, admin);
        transformAdminToUserDTO(admin, userDTO);
        userDTO.setUuid(session);
        return userDTO;
    }

    @Override
    public UserDTO logout(String token) {
        UserDTO userDTO = new UserDTO();
        if (adminService.getSessions().containsKey(token) || clientService.getClientSessions().containsKey(token)) {
            adminService.getSessions().remove(token);
            clientService.getClientSessions().remove(token);
        } else {
            userDTO.clearFieldsToGetError();
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_SESSION, "Данной сессии не существует", "SESSIONID") );
        }
        return userDTO;
    }

    @Override
    public UserDTO getUserInfo(String sessionId) {
        UserDTO userDTO = new UserDTO();
        Admin admin = adminService.getSessions().get(sessionId);
        Client client = clientService.getClientSessions().get(sessionId);
        if (admin == null && client == null) {
            userDTO.clearFieldsToGetError();
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_SESSION, "Данной сессии не существует", "SESSIONID") );
            return userDTO;
        }
        if (client == null) {
           transformAdminToUserDTO(admin, userDTO);
           return userDTO;
        } else {
            transformClientToUserDTO(client, userDTO);
            return userDTO;
        }
    }

    private void transformAdminToUserDTO(Admin admin, UserDTO userDTO) {
        userDTO.setId(admin.getId().toString());
        userDTO.setFirstName(admin.getFirstName());
        userDTO.setLastName(admin.getLastName());
        userDTO.setPatronymic(admin.getPatronymic());
        userDTO.setPosition(admin.getPosition());
        userDTO.setErrors(new ArrayList<>());
    }

    private void transformClientToUserDTO(Client client, UserDTO userDTO) {
        userDTO.setId(client.getId().toString());
        userDTO.setFirstName(client.getFirstName());
        userDTO.setLastName(client.getLastName());
        userDTO.setPatronymic(client.getPatronymic());
        userDTO.setEmail(client.getEmail());
        userDTO.setAddress(client.getAddress());
        userDTO.setPhone(client.getPhone());
        userDTO.setDeposit(client.getDeposit().toString());
        userDTO.setErrors(new ArrayList<>());
    }

    @Override
    public String clearBase() {
        adminRepository.deleteAll();
        adminRepository.flush();
        categoryRepository.deleteAll();
        categoryRepository.flush();
        clientRepository.deleteAll();
        clientRepository.flush();
        cartWithProductsRepository.deleteAll();
        cartWithProductsRepository.flush();
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
        productRepository.deleteAll();
        productRepository.flush();
        soldProductsRepository.deleteAll();
        operations.deleteAll();
        operations.flush();
        return "";
    }


}
