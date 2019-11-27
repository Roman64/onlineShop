package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.UserDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ValidateUserServiceImplTest {

    private ValidateUserServiceImpl validateUserService;
    private UserDTO admin;
    private UserDTO client;
    private final String firstName = "Роман";
    private final String lastName = "Коваценко";
    private final String position = "админ";
    private final String login = "Роман123";
    private final String password = "Роман123";
    private final String patronymic = "Александрович";
    private final String email = "Kovatsenko@gmail.com";
    private final String phone = "+79873355010";
    private final String address = "Саратов";


    @Before
    public void before() {
        validateUserService = new ValidateUserServiceImpl();
        validateUserService.setLength(50);
        validateUserService.setMinPasswordLength(8);
        admin = new UserDTO();
        client = new UserDTO();
    }

    @Test
    public void validateAdmin() {
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setPosition(position);
        admin.setLogin(login);
        admin.setPassword(password);
        admin.setPatronymic(patronymic);
        validateUserService.validateAdmin(admin);
        Assert.assertTrue(admin.getErrors().isEmpty());
        Assert.assertEquals(admin.getFirstName(), firstName);
        Assert.assertEquals(admin.getLastName(), lastName);
        Assert.assertEquals(admin.getPosition(), position);
        Assert.assertEquals(admin.getLogin(), login);
        Assert.assertEquals(admin.getPassword(), password);
        Assert.assertEquals(admin.getPatronymic(), patronymic);
    }

    @Test
    public void validateEditAdmin() {
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setPosition(position);
        admin.setLogin(login);
        admin.setPassword(password);
        admin.setPatronymic(patronymic);
        validateUserService.validateEditAdmin(admin);
        Assert.assertTrue(admin.getErrors().isEmpty());
        Assert.assertEquals(admin.getFirstName(), firstName);
        Assert.assertEquals(admin.getLastName(), lastName);
        Assert.assertEquals(admin.getPosition(), position);
        Assert.assertEquals(admin.getLogin(), login);
        Assert.assertEquals(admin.getPassword(), password);
        Assert.assertEquals(admin.getPatronymic(), patronymic);
    }

    @Test
    public void validateClient() {
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setLogin(login);
        client.setPassword(password);
        validateUserService.validateClient(client);
        Assert.assertTrue(client.getErrors().isEmpty());
        Assert.assertEquals(client.getFirstName(), firstName);
        Assert.assertEquals(client.getLastName(), lastName);
        Assert.assertEquals(client.getLogin(), login);
        Assert.assertEquals(client.getPassword(), password);
        Assert.assertEquals(client.getEmail(), email);
        Assert.assertEquals(client.getPhone(), phone);
        Assert.assertEquals(client.getAddress(), address);
    }

    @Test
    public void validateEditClient() {
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setLogin(login);
        client.setPassword(password);
        validateUserService.validateEditClient(client);
        Assert.assertTrue(client.getErrors().isEmpty());
        Assert.assertEquals(client.getFirstName(), firstName);
        Assert.assertEquals(client.getLastName(), lastName);
        Assert.assertEquals(client.getLogin(), login);
        Assert.assertEquals(client.getPassword(), password);
        Assert.assertEquals(client.getEmail(), email);
        Assert.assertEquals(client.getPhone(), phone);
        Assert.assertEquals(client.getAddress(), address);
    }

    @Test
    public void validateWrongAdmin() {
        admin.setFirstName("");
        admin.setLastName("");
        admin.setPosition("");
        admin.setLogin("");
        admin.setPassword("");
        admin.setPatronymic("");
        validateUserService.validateAdmin(admin);
        Assert.assertNotNull(admin.getErrors());
        Assert.assertEquals(admin.getErrors().size(), 6);
        Assert.assertEquals(admin.getErrors().get(0).getMessage(), "Поле имя не может быть пустым");
        Assert.assertEquals(admin.getErrors().get(1).getMessage(), "Поле фамилия не может быть пустым");
        Assert.assertEquals(admin.getErrors().get(2).getMessage(), "Отчество может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(admin.getErrors().get(3).getMessage(), "Поле логин не может быть пустым");
        Assert.assertEquals(admin.getErrors().get(4).getMessage(), "Поле пароль не может быть пустым");
        Assert.assertEquals(admin.getErrors().get(5).getMessage(), "Поле должность не должно быть пустым");

        admin = new UserDTO();
        String big = "вапоавпажовпраолвпрыволжаыовжлпырвалопрываполыварпл";
        admin.setFirstName(big);
        admin.setLastName(big);
        admin.setPosition(big);
        admin.setLogin(big);
        admin.setPassword(big);
        admin.setPatronymic(big);
        validateUserService.validateAdmin(admin);
        Assert.assertNotNull(admin.getErrors());
        Assert.assertEquals(admin.getErrors().size(), 6);
        Assert.assertEquals(admin.getErrors().get(0).getMessage(), "Максимальная длина имени 50 символов");
        Assert.assertEquals(admin.getErrors().get(1).getMessage(), "Максимальная длина фамилии 50 символов");
        Assert.assertEquals(admin.getErrors().get(2).getMessage(), "Максимальная длина отчества 50 символов");
        Assert.assertEquals(admin.getErrors().get(3).getMessage(), "Максимальная длина логина 50 символов");
        Assert.assertEquals(admin.getErrors().get(4).getMessage(), "Максимальная длина пароля 50 символов");
        Assert.assertEquals(admin.getErrors().get(5).getMessage(), "Максимальная длина должности 50 символов");

        admin = new UserDTO();
        admin.setFirstName("dfjfdjkghk");
        admin.setLastName("fdgfgdhkjhkjd");
        admin.setPosition("sfdsfsdf");
        admin.setLogin("fdkfhЫВора一月");
        admin.setPassword("dfhsfdh");
        admin.setPatronymic("dsfhjdsfhk");
        validateUserService.validateAdmin(admin);
        Assert.assertNotNull(admin.getErrors());
        Assert.assertEquals(admin.getErrors().size(), 5);
        Assert.assertEquals(admin.getErrors().get(0).getMessage(), "Имя может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(admin.getErrors().get(1).getMessage(), "Фамилия может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(admin.getErrors().get(2).getMessage(), "Отчество может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(admin.getErrors().get(3).getMessage(), "Логин может содержать только латинские и русские буквы и цифры");
        Assert.assertEquals(admin.getErrors().get(4).getMessage(), "Минимальная длина пароля 8 символов");
    }

    @Test
    public void validateWrongClient(){
        client.setFirstName(null);
        client.setLastName(null);
        client.setPatronymic("");
        client.setEmail(null);
        client.setPhone(null);
        client.setAddress(null);
        client.setLogin(null);
        client.setPassword(null);
        validateUserService.validateClient(client);
        Assert.assertNotNull(client.getErrors());
        Assert.assertEquals(client.getErrors().size(), 8);
        Assert.assertEquals(client.getErrors().get(0).getMessage(), "Поле имя не может быть пустым");
        Assert.assertEquals(client.getErrors().get(1).getMessage(), "Поле фамилия не может быть пустым");
        Assert.assertEquals(client.getErrors().get(2).getMessage(), "Отчество может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(client.getErrors().get(3).getMessage(), "Поле логин не может быть пустым");
        Assert.assertEquals(client.getErrors().get(4).getMessage(), "Поле пароль не может быть пустым");
        Assert.assertEquals(client.getErrors().get(5).getMessage(), "Поле email не должно быть пустым");
        Assert.assertEquals(client.getErrors().get(6).getMessage(), "Поле phone не должно быть пустым");
        Assert.assertEquals(client.getErrors().get(7).getMessage(), "Поле address не должно быть пустым");

        client = new UserDTO();
        String big = "вапоавпажовпраолвпрыволжаыовжлпырвалопрываполыварпл";
        client.setFirstName(big);
        client.setLastName(big);
        client.setLogin(big);
        client.setPassword(big);
        client.setPatronymic(big);
        client.setEmail(big);
        client.setAddress(big);
        client.setPhone(big);
        validateUserService.validateClient(client);
        Assert.assertNotNull(client.getErrors());
        Assert.assertEquals(client.getErrors().size(), 7);
        Assert.assertEquals(client.getErrors().get(0).getMessage(), "Максимальная длина имени 50 символов");
        Assert.assertEquals(client.getErrors().get(1).getMessage(), "Максимальная длина фамилии 50 символов");
        Assert.assertEquals(client.getErrors().get(2).getMessage(), "Максимальная длина отчества 50 символов");
        Assert.assertEquals(client.getErrors().get(3).getMessage(), "Максимальная длина логина 50 символов");
        Assert.assertEquals(client.getErrors().get(4).getMessage(), "Максимальная длина пароля 50 символов");
        Assert.assertEquals(client.getErrors().get(5).getMessage(), "Максимальная длина email 50 символов");
        Assert.assertEquals(client.getErrors().get(6).getMessage(), "Мобильный номер не валиден");

        client = new UserDTO();
        client.setFirstName("dfjfdjkghk");
        client.setLastName("fdgfgdhkjhkjd");
        client.setLogin("fdkfhЫВора一月");
        client.setPassword("dfhsfdh");
        client.setPatronymic("dsfhjdsfhk");
        client.setEmail("kovatsenko@.ru");
        client.setAddress("fdgjh");
        client.setPhone("35-16-19");
        validateUserService.validateClient(client);
        Assert.assertNotNull(client.getErrors());
        Assert.assertEquals(client.getErrors().size(), 7);
        Assert.assertEquals(client.getErrors().get(0).getMessage(), "Имя может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(client.getErrors().get(1).getMessage(), "Фамилия может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(client.getErrors().get(2).getMessage(), "Отчество может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(client.getErrors().get(3).getMessage(), "Логин может содержать только латинские и русские буквы и цифры");
        Assert.assertEquals(client.getErrors().get(4).getMessage(), "Минимальная длина пароля 8 символов");
        Assert.assertEquals(client.getErrors().get(5).getMessage(), "Email не валиден");
        Assert.assertEquals(client.getErrors().get(6).getMessage(), "Мобильный номер не валиден");
    }


}
