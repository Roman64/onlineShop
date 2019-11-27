package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.OperationsWithClientsDTO;
import net.thumbtack.onlineshop.data.model.*;
import net.thumbtack.onlineshop.data.repository.AdminRepository;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.repository.OperationsWithClientsRepository;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    private static HashMap<String, Admin> sessions = new HashMap<>();

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private OperationsWithClientsRepository withClientsRepository;

    @Override
    public Boolean isLogin(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    @Override
    public Boolean isCorrectPassword(String sessionId, String password) {
        String oldPass = sessions.get(sessionId).getPassword();
        return oldPass.equals(password);
    }

    @Override
    public UserDTO registerAdmin(UserDTO userDTO) {
        UserDTO result = new UserDTO();
        Admin admin = adminRepository.getAdminByLogin(userDTO.getLogin());
        if (admin != null) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LOGIN, "Пользователь с таким логином уже существует", "login"));
            return userDTO;
        }
        if (!userDTO.getErrors().isEmpty()) {
            return userDTO;
        }
        Admin adminNew = adminRepository.save(transformUserDTOtoAdmin(userDTO));
        String uuid = UUID.randomUUID().toString();
        sessions.put(uuid, adminNew);
        result = transformAdmintoUserDTO(adminNew);
        result.setUuid(uuid);
        return result;
    }

    @Override
    public String editProfile(String session, UserDTO userDTO) {
        Admin admin = sessions.get(session);
        if (userDTO.getNewPassword() != null) admin.setPassword(userDTO.getNewPassword());
        if (userDTO.getFirstName() != null) admin.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) admin.setLastName(userDTO.getLastName());
        if (userDTO.getPatronymic() != null) admin.setPatronymic(userDTO.getPatronymic());
        if (userDTO.getPosition() != null) admin.setPosition(userDTO.getPosition());
        adminRepository.updateAdmin(admin.getId(),admin.getFirstName(), admin.getLastName(), admin.getPatronymic(), admin.getPosition(), admin.getPassword());
        return admin.getId().toString();

    }

    private UserDTO transformAdmintoUserDTO(Admin admin) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(admin.getId().toString());
        userDTO.setFirstName(admin.getFirstName());
        userDTO.setLastName(admin.getLastName());
        userDTO.setPatronymic(admin.getPatronymic());
        userDTO.setPosition(admin.getPosition());
        userDTO.setErrors(new ArrayList<>());
        return userDTO;
    }

    public Admin transformUserDTOtoAdmin(UserDTO userDTO) {
        Admin admin = new Admin();
        admin.setFirstName(userDTO.getFirstName());
        admin.setLastName(userDTO.getLastName());
        admin.setPatronymic(userDTO.getPatronymic());
        admin.setLogin(userDTO.getLogin());
        admin.setPassword(userDTO.getPassword());
        admin.setPosition(userDTO.getPosition());
        return admin;
    }

    @Override
    public List<OperationsWithClientsDTO> getOperationsHistory(String session, Integer clientId, String method) {
        List<OperationsWithClientsDTO> result = new ArrayList<>();
        if (!isLogin(session)) {
            OperationsWithClientsDTO operations = new OperationsWithClientsDTO();
            operations.clearFieldsToGetError();
            operations.getErrors().add(new UserError(UserErrorCode.INVALID_SESSION, "Нет доступа", "session"));
            result.add(operations);
            return result;
        }
        if (clientId != null && clientId!=0) {
            List<OperationsWithClients> list = withClientsRepository.findByClientIdOrderByDate(clientId);
            if (!list.isEmpty()) {
                transformOperationsToDTO(list, result);
                return result;
            } else {
                OperationsWithClientsDTO operations = new OperationsWithClientsDTO();
                operations.clearFieldsToGetError();
                operations.getErrors().add(new UserError(UserErrorCode.CLIENTS_NOT_FOUND,
                        "По данному Id клиентов не найдено", "clientId"));
                result.add(operations);
                return result;
            }
        }
        if (method != null) {
            List<OperationsWithClients> list = withClientsRepository.findByMethodOrderByDate(method);
            if (!list.isEmpty()) {
                transformOperationsToDTO(list, result);
                return result;
            } else {
                OperationsWithClientsDTO operations = new OperationsWithClientsDTO();
                operations.clearFieldsToGetError();
                operations.getErrors().add(new UserError(UserErrorCode.CLIENTS_NOT_FOUND,
                        "По запрошенному методу историй не найдено", "method"));
                result.add(operations);
                return result;
            }
        }
        OperationsWithClientsDTO operations = new OperationsWithClientsDTO();
        operations.clearFieldsToGetError();
        operations.getErrors().add(new UserError(UserErrorCode.CLIENTS_NOT_FOUND, "Не удалось найти по данным критериям", "requestBody"));
        result.add(operations);
        return result;
    }

    private void transformOperationsToDTO(List<OperationsWithClients> list, List<OperationsWithClientsDTO> result) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");
        for (OperationsWithClients operations : list) {
            OperationsWithClientsDTO dto = new OperationsWithClientsDTO();
            dto.setId(String.valueOf(operations.getId()));
            dto.setClientId(String.valueOf(operations.getClientId()));
            dto.setDate(operations.getDate().format(formatter));
            dto.setMethod(operations.getMethod());
            result.add(dto);
        }
    }

    public HashMap<String, Admin> getSessions() {
        return sessions;
    }
}
