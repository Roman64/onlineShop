package net.thumbtack.onlineshop.integration_e2e_tests;

import net.thumbtack.onlineshop.data.dto.OperationsWithClientsDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.error.UserError;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    private UserDTO admin = new UserDTO();
    private URL urlString;


    @Before
    public void beforeTest() throws MalformedURLException {
        urlString = new URL("http://localhost:8080/api");
        admin = new UserDTO();
        admin.setFirstName("Роман");
        admin.setLastName("Коваценко");
        admin.setPatronymic("Александрович");
        admin.setLogin("Логин12345");
        admin.setPassword("пароль12345");
        admin.setPosition("тестовый");
    }

    @After
    public void resetDb() {
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(urlString + "/debug/clear", "clear", String.class);
        assertThat(responseAdmin.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void createAdmin(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getId(), notNullValue());
        assertThat(response.getBody().getFirstName(), is("Роман"));
        assertThat(response.getBody().getLastName(), is("Коваценко"));
        assertThat(response.getBody().getPatronymic(), is("Александрович"));
        assertThat(response.getBody().getPosition(), is("тестовый"));
    }

    @Test
    public void createAdminTwice(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        ResponseEntity<UserDTO> responseBad = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(responseBad.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseBad.getBody()).getErrors().get(0), notNullValue());
        List<UserError> userErrors = responseBad.getBody().getErrors();
        boolean isEquals = false;
        for (UserError userError : userErrors) {
             if (userError.getMessage().equals("Пользователь с таким логином уже существует")) isEquals = true;
        }
        Assert.assertTrue(isEquals);
    }

    @Test
    public void createAdminWithWrongParameters(){
        admin.setLastName("Kovatsenko");
        admin.setFirstName("Roman");
        admin.setPatronymic(null);
        admin.setPassword("parol");
        admin.setPosition(RandomStringUtils.random(60));
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(response.getBody()).getErrors().get(0), notNullValue());
        List<UserError> userErrors = response.getBody().getErrors();
        Assert.assertEquals(userErrors.get(0).getMessage(), "Имя может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(userErrors.get(1).getMessage(), "Фамилия может содержать только русские буквы, пробелы и знак тире между символами");
        Assert.assertEquals(userErrors.get(2).getMessage(), "Минимальная длина пароля 8 символов");
        Assert.assertEquals(userErrors.get(3).getMessage(), "Максимальная длина должности 50 символов");

    }

    @Test
    public void editAdmin(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        admin.setFirstName("Настенка");
        admin.setLastName("Пиреева");
        admin.setLogin("логин66666");
        admin.setOldPassword(admin.getPassword());
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(admin, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/admins", HttpMethod.PUT, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getFirstName(), is("Настенка"));
        assertThat(responseEntity.getBody().getLastName(), is("Пиреева"));
    }

    @Test
    public void editAdminWithoutLogin(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        System.out.println(response.getHeaders().entrySet().toString());
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        admin.setFirstName("Настенка");
        admin.setLastName("Пиреева");
        admin.setLogin("логин66666");
        admin.setOldPassword(admin.getPassword());
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie.substring(0, set_cookie.length()-2) + "1e");
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(admin, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/admins", HttpMethod.PUT, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody().getErrors().get(0).getMessage(), is("Вы не залогинены"));
    }

    @Test
    public void editAdminWithWrongPassword(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        admin.setFirstName("Настенка");
        admin.setLastName("Пиреева");
        admin.setLogin("логин66666");
        admin.setOldPassword("пароль");
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(admin, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/admins", HttpMethod.PUT, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody().getErrors().get(0).getMessage(), is("Пароли не совпадают"));
    }

    @Test
    public void getClientsHistory() {
        //создадим админа
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders httpHeadersAdmin = new HttpHeaders();
        httpHeadersAdmin.add(HttpHeaders.COOKIE, set_cookie);
        //создадим клиента
        UserDTO client = new UserDTO();
        client.setFirstName("Настя");
        client.setLastName("Карташова");
        client.setPatronymic("Сергеевна");
        client.setLogin("Логин777");
        client.setPassword("пароль12345");
        client.setAddress("KUKI STREET");
        client.setPhone("+7987-335-50-10");
        client.setDeposit("0");
        client.setEmail("Kovatsenko@gmail.com");
        ResponseEntity<UserDTO> responseClient = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(responseClient.getStatusCode(), is(HttpStatus.OK));
        httpHeaders = responseClient.getHeaders();
        set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders httpHeadersClient = new HttpHeaders();
        httpHeadersClient.add(HttpHeaders.COOKIE, set_cookie);
        // Добавим клиенту 3 действия
        UserDTO cash = new UserDTO();
        cash.setDeposit("500");
        HttpEntity<UserDTO> entityCash = new HttpEntity<UserDTO>(cash, httpHeadersClient);
        ResponseEntity<UserDTO> responseFromClient = restTemplate.exchange(urlString + "/deposits", HttpMethod.PUT, entityCash, UserDTO.class);
        assertThat(responseFromClient.getStatusCode(), is(HttpStatus.OK));
        ResponseEntity<UserDTO> responseFromClient2 = restTemplate.exchange(urlString + "/deposits", HttpMethod.PUT, entityCash, UserDTO.class);
        assertThat(responseFromClient2.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<UserDTO> entityGetCash = new HttpEntity<UserDTO>(httpHeadersClient);
        ResponseEntity<UserDTO> responseGetCash = restTemplate.exchange(urlString + "/deposits", HttpMethod.GET, entityGetCash, UserDTO.class);
        assertThat(responseGetCash.getStatusCode(), is(HttpStatus.OK));
        // Получим историю действий по Id клиента
        int clientId = Integer.parseInt(Objects.requireNonNull(responseClient.getBody()).getId());
        HttpEntity<OperationsWithClientsDTO> entityGetHistory = new HttpEntity<>(httpHeadersAdmin);
        ResponseEntity<List<OperationsWithClientsDTO>> responseList = restTemplate.exchange(urlString + "/admins/history?clientId={clientId}", HttpMethod.GET,
                entityGetHistory, new ParameterizedTypeReference<List<OperationsWithClientsDTO>>() {}, clientId);
        assertThat(responseList.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseList.getBody(), is(notNullValue()));
        assertThat(responseList.getBody().get(0).getClientId(), is(responseClient.getBody().getId()));
        assertThat(responseList.getBody().get(0).getMethod(), is("addCash"));
        assertThat(responseList.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(responseList.getBody().get(1).getClientId(), is(responseClient.getBody().getId()));
        assertThat(responseList.getBody().get(1).getMethod(), is("addCash"));
        assertThat(responseList.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(responseList.getBody().get(2).getClientId(), is(responseClient.getBody().getId()));
        assertThat(responseList.getBody().get(2).getMethod(), is("getCash"));
        assertThat(responseList.getBody().get(2).getDate(), is(notNullValue()));
    }
}
