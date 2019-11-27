package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.model.OperationsWithClients;
import net.thumbtack.onlineshop.data.repository.OperationsWithClientsRepository;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.repository.ClientRepository;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.services.AdminService;
import net.thumbtack.onlineshop.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private AdminService adminService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private OperationsWithClientsRepository operationsWithClientsRepository;

    private static HashMap<String, Client> clientSessions = new HashMap<>();

    @Override
    public List<UserDTO> getAllClientsByAdmin(String session) {
        List<UserDTO> result = new ArrayList<>();
        if (!adminService.isLogin(session)) {
            UserDTO userDTO = new UserDTO();
            userDTO.clearFieldsToGetError();
            userDTO.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            result.add(userDTO);
            return result;
        }

        List<Client> clients = clientRepository.findAll();
        for (Client client : clients) {
            result.add(transformClientToUserDTO(client));
        }

        if (result.isEmpty()) {
            UserDTO userDTO = new UserDTO();
            userDTO.clearFieldsToGetError();
            userDTO.getErrors().add(new UserError(UserErrorCode.CLIENTS_NOT_FOUND, "Список пуст", "LIST OF CLIENTS"));
            result.add(userDTO);
            return result;
        }

        return result;
    }

    @Override
    public Boolean isLogin(String sessionId) {
        boolean isContains = clientSessions.containsKey(sessionId);
        if (isContains) {
            OperationsWithClients operations = new OperationsWithClients();
            operations.setClientId(clientSessions.get(sessionId).getId());
            operations.setDate(LocalDateTime.now());
            operations.setSession(sessionId);
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            operations.setMethod(stackTrace[2].getMethodName());
            operationsWithClientsRepository.save(operations);
        }

        return isContains;
    }

    @Override
    public UserDTO registerClient(UserDTO userDTO) {
        UserDTO result = new UserDTO();
        Client client = clientRepository.getClientByLogin(userDTO.getLogin());
        if (client != null) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LOGIN, "Пользователь с таким логином уже существует", "login"));
            return userDTO;
        }
        if (!userDTO.getErrors().isEmpty()) {
            return userDTO;
        }
        Client clientNew = clientRepository.save(transformUserDTOtoClient(userDTO));
        String uuid = UUID.randomUUID().toString();
        clientSessions.put(uuid, clientNew);
        result = transformClientToUserDTO(clientNew);
        result.setUuid(uuid);
        return result;
    }

    @Override
    public String editClientProfile(String session, UserDTO userDTO) {
        Client client = clientSessions.get(session);
        if (userDTO.getNewPassword() != null) client.setPassword(userDTO.getNewPassword());
        if (userDTO.getFirstName() != null) client.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) client.setLastName(userDTO.getLastName());
        if (userDTO.getPatronymic() != null) client.setPatronymic(userDTO.getPatronymic());
        if (userDTO.getEmail() != null) client.setEmail(userDTO.getEmail());
        if (userDTO.getAddress() != null) client.setAddress(userDTO.getAddress());
        if (userDTO.getPhone() != null) client.setPhone(userDTO.getPhone());
        client = clientRepository.save(client);
        return client.getId().toString();
    }

    @Override
    public UserDTO addCash(String session, UserDTO userDTO) {
        UserDTO result = new UserDTO();
        String cash = userDTO.getDeposit();
        int deposit = 0;
        if (!isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        try {
            deposit = Integer.parseInt(cash);
        }
        catch (NumberFormatException e) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_DEPOSIT, "Введите кол-во денег в верном формате", "deposit"));
            return result;
        }
        Client client = clientSessions.get(session);
        int depositNew = client.getDeposit() + deposit;
        client.setDeposit(depositNew);
        clientRepository.save(client);
        result = transformClientToUserDTO(client);
        return result;
    }

    @Override
    public UserDTO getCash(String session) {
        UserDTO result = new UserDTO();
        if (!clientService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        Client client = clientSessions.get(session);
        result = transformClientToUserDTO(client);
        return result;
    }

    @Override
    public boolean isCorrectPassword(String id, String oldPassword) {
        String oldPass = clientSessions.get(id).getPassword();
        return oldPass.equals(oldPassword);
    }

    public HashMap<String, Client> getClientSessions() {
        return clientSessions;
    }

    private UserDTO transformClientToUserDTO(Client client){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(client.getId().toString());
        userDTO.setFirstName(client.getFirstName());
        userDTO.setLastName(client.getLastName());
        userDTO.setPatronymic(client.getPatronymic());
        userDTO.setEmail(client.getEmail());
        userDTO.setAddress(client.getAddress());
        userDTO.setPhone(client.getPhone());
        userDTO.setDeposit(client.getDeposit().toString());
        userDTO.setUserType("client");
        userDTO.setErrors(new ArrayList<>());
        return userDTO;
    }

    public Client transformUserDTOtoClient(UserDTO userDTO) {
        Client client = new Client();
        client.setFirstName(userDTO.getFirstName());
        client.setLastName(userDTO.getLastName());
        client.setPatronymic(userDTO.getPatronymic());
        client.setLogin(userDTO.getLogin());
        client.setPassword(userDTO.getPassword());
        client.setEmail(userDTO.getEmail());
        client.setAddress(userDTO.getAddress());
        client.setPhone(userDTO.getPhone().replaceAll("[-()]",""));
        client.setDeposit(0);
        return client;
    }

}
