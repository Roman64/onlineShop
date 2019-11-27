package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.services.ValidateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidateUserServiceImpl implements ValidateUserService {
    @Value("${max_name_length}")
    private int length;

    @Value("${min_password_length}")
    private int minPasswordLength;

    @Autowired
    private AdminServiceImpl adminService;
    @Autowired
    private ClientServiceImpl clientService;

    @Override
    public void validateAdmin(UserDTO userDTO) {
        userDTO.setErrors(new ArrayList<>());
        checkFirstName(userDTO);
        checkLastName(userDTO);
        checkPatronymicName(userDTO);
        checkLogin(userDTO);
        checkPassword(userDTO, userDTO.getPassword());
        checkPosition(userDTO);
        if (!userDTO.getErrors().isEmpty()) {
            clearField(userDTO);
        }
    }

    @Override
    public void validateEditAdmin(UserDTO userDTO) {
        userDTO.setErrors(new ArrayList<>());
        if (userDTO.getFirstName() != null) checkFirstName(userDTO);
        if (userDTO.getLastName() != null) checkLastName(userDTO);
        if (userDTO.getPatronymic() != null) checkPatronymicName(userDTO);
        if (userDTO.getPosition() != null) checkPosition(userDTO);
        if (userDTO.getNewPassword() != null) checkPassword(userDTO, userDTO.getNewPassword());
        if (!userDTO.getErrors().isEmpty()) {
            clearField(userDTO);
        }
    }

    @Override
    public void validateClient(UserDTO userDTO) {
        userDTO.setErrors(new ArrayList<>());
        checkFirstName(userDTO);
        checkLastName(userDTO);
        checkPatronymicName(userDTO);
        checkLogin(userDTO);
        checkPassword(userDTO, userDTO.getPassword());
        checkEmail(userDTO);
        checkPhone(userDTO);
        checkAddress(userDTO);
        if (!userDTO.getErrors().isEmpty()) {
            clearField(userDTO);
        }
    }

    @Override
    public void validateEditClient(UserDTO userDTO) {
        userDTO.setErrors(new ArrayList<>());
        if (userDTO.getFirstName() != null) checkFirstName(userDTO);
        if (userDTO.getLastName() != null) checkLastName(userDTO);
        if (userDTO.getPatronymic() != null) checkPatronymicName(userDTO);
        if (userDTO.getNewPassword() != null) checkPassword(userDTO, userDTO.getNewPassword());
        if (userDTO.getEmail() != null) checkEmail(userDTO);
        if (userDTO.getAddress() != null) checkAddress(userDTO);
        if (userDTO.getPhone() != null) checkPhone(userDTO);
        if (!userDTO.getErrors().isEmpty()) {
            clearField(userDTO);
        }
    }

    @Override
    public HashMap<String, String> getServerSettings(String session) {
        HashMap<String, String> result = new HashMap<>();
        if (adminService.isLogin(session) || clientService.isLogin(session) || session.equals("default")) {
            result.put("max_name_length", String.valueOf(length));
            result.put("minPasswordLength", String.valueOf(minPasswordLength));
            return result;
        }
        else {
            result.put("errorCode", "404");
            result.put("field", "session");
            result.put("message", "Ошибка доступа");
            return result;
        }
    }

    private void checkAddress(UserDTO userDTO) {
        String address = userDTO.getAddress();
        if (address == null || address.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.WRONG_ADDRESS, "Поле address не должно быть пустым", "address"));
            return;
        }
    }

    private void checkPhone(UserDTO userDTO) {
        String phone = userDTO.getPhone();
        if (phone == null || phone.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.WRONG_PHONE, "Поле phone не должно быть пустым", "phone"));
            return;
        }
        String regexForLogin = "^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{10}$";
        Pattern pattern = Pattern.compile(regexForLogin);
        Matcher matcher = pattern.matcher(phone);
        if (!matcher.matches()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.WRONG_PHONE, "Мобильный номер не валиден", "phone"));
        }
    }

    private void checkEmail(UserDTO userDTO) {
        String email = userDTO.getEmail();
        if (email == null || email.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.WRONG_EMAIL, "Поле email не должно быть пустым", "email"));
            return;
        }
        if (email.length() > length) {
            userDTO.getErrors().add(new UserError(UserErrorCode.WRONG_EMAIL, "Максимальная длина email " + length + " символов", "email"));
            return;
        }
        String regexForLogin = "^(\\S+)@([a-z0-9-]+)(\\.)([a-z]{2,4})(\\.?)([a-z]{0,4})+$";
        Pattern pattern = Pattern.compile(regexForLogin);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.WRONG_EMAIL, "Email не валиден", "email"));
        }
    }


    private void checkFirstName(UserDTO userDTO) {
        String firstName = userDTO.getFirstName();
        if (firstName == null || firstName.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_FIRST_NAME, "Поле имя не может быть пустым", "firstName"));
            return;
        }
        if (firstName.length() > length) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_FIRST_NAME, "Максимальная длина имени " + length + " символов", "firstName"));
            return;
        }
        if (checkName(firstName)) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_FIRST_NAME, "Имя может содержать только русские буквы, пробелы и знак тире между символами", "firstName"));
        }
    }

    private void checkLastName(UserDTO userDTO) {
        String lastName = userDTO.getLastName();
        if (lastName == null || lastName.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LAST_NAME, "Поле фамилия не может быть пустым", "lastName"));
            return;
        }
        if (lastName.length() > length) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LAST_NAME, "Максимальная длина фамилии " + length + " символов", "lastName"));
            return;
        }
        if (checkName(lastName)) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LAST_NAME, "Фамилия может содержать только русские буквы, пробелы и знак тире между символами", "lastName"));
        }
    }

    private void checkPatronymicName(UserDTO userDTO) {
        String patronymic = userDTO.getPatronymic();
        if (patronymic == null) {
            return;
        }
        if (patronymic.length() > length) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_PATRONYMIC_NAME, "Максимальная длина отчества " + length + " символов", "patronymic"));
            return;
        }
        if (checkName(patronymic)) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_PATRONYMIC_NAME, "Отчество может содержать только русские буквы, пробелы и знак тире между символами", "patronymic"));
        }
    }

    private void checkPosition(UserDTO userDTO) {
        String position = userDTO.getPosition();
        if (position == null || position.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_POSITION, "Поле должность не должно быть пустым", "position"));
        }
        if (position.length() > length) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_POSITION, "Максимальная длина должности " + length + " символов", "position"));
            return;
        }
    }

    private void checkLogin(UserDTO userDTO) {
        String login = userDTO.getLogin();
        if (login == null || login.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LOGIN, "Поле логин не может быть пустым", "login"));
            return;
        }
        if (login.length() > length) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LOGIN, "Максимальная длина логина " + length + " символов", "login"));
            return;
        }

        String regexForLogin = "^([А-Яа-яa-zA-Z0-9]+)$";
        Pattern pattern = Pattern.compile(regexForLogin);
        Matcher matcher = pattern.matcher(login);
        if (!matcher.matches()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_LOGIN, "Логин может содержать только латинские и русские буквы и цифры", "login"));
        }
    }

    private void checkPassword(UserDTO userDTO, String password) {
        if (password == null || password.isEmpty()) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_PASSWORD, "Поле пароль не может быть пустым", "password"));
            return;
        }
        if (password.length() < minPasswordLength) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_PASSWORD, "Минимальная длина пароля " + minPasswordLength + " символов", "password"));
        }
        if (password.length() > length) {
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_PASSWORD, "Максимальная длина пароля " + length + " символов", "password"));
            return;
        }
    }

    private boolean checkName(String name) {
        String regexForFIO = "^([А-Яа-я]+(-[а-я]+)* ?)+$";
        Pattern pattern = Pattern.compile(regexForFIO);
        Matcher matcher = pattern.matcher(name);
        return !matcher.matches();
    }


    public void clearField(UserDTO userDTO) {
        userDTO.setFirstName(null);
        userDTO.setLastName(null);
        userDTO.setPatronymic(null);
        userDTO.setLogin(null);
        userDTO.setPassword(null);
        userDTO.setPosition(null);
        userDTO.setAddress(null);
        userDTO.setEmail(null);
        userDTO.setPhone(null);
        userDTO.setDeposit(null);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMinPasswordLength(int minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }
}
