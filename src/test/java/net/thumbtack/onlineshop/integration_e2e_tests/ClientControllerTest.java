package net.thumbtack.onlineshop.integration_e2e_tests;

import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.repository.ClientRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ClientControllerTest {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private TestRestTemplate restTemplate;
    private CategoryDTO categoryDTO = new CategoryDTO();
    private ProductDTO productDTO = new ProductDTO();
    private UserDTO client;
    private URL urlString;
    private HttpHeaders httpHeaders;

    @Before
    public void beforeTest() throws MalformedURLException {
        urlString = new URL("http://localhost:8080/api");
        UserDTO admin = new UserDTO();
        admin.setFirstName("Роман");
        admin.setLastName("Коваценко");
        admin.setPatronymic("Александрович");
        admin.setLogin("Логин12345");
        admin.setPassword("пароль12345");
        admin.setPosition("тестовый");
        categoryDTO.setName("Тестовая категория_1");
        productDTO.setName("Продукт1_тестовой категории");
        productDTO.setPrice("50");
        productDTO.setCount("10");
        client = new UserDTO();
        client.setFirstName("Настя");
        client.setLastName("Карташова");
        client.setPatronymic("Сергеевна");
        client.setLogin("Логин777");
        client.setPassword("пароль12345");
        client.setAddress("KUKI STREET");
        client.setPhone("+7987-335-50-10");
        client.setDeposit("0");
        client.setEmail("Kovatsenko@gmail.com");
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
    }

    @After
    public void resetDb() {
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(urlString + "/debug/clear", "clear", String.class);
        assertThat(responseAdmin.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void getAllClientsByAdmin(){
        Client client1 = new Client();
        client1.setFirstName(client.getFirstName());
        client1.setLastName(client.getLastName());
        client1.setPatronymic(client.getPatronymic());
        client1.setLogin(client.getLogin());
        client1.setPassword(client.getPassword());
        client1.setPhone(client.getPhone());
        client1.setDeposit(Integer.parseInt(client.getDeposit()));
        client1.setEmail(client.getEmail());
        client1.setAddress(client.getAddress());
        Client client2 = new Client();
        client2.setFirstName("Елизавета");
        client2.setLastName("Пиреева");
        client2.setPatronymic("Андреевна");
        client2.setLogin("Логиноид777");
        client2.setPassword("паролоид777");
        client2.setPhone("+79197776707");
        client2.setDeposit(0);
        client2.setEmail("Pireeva@gmail.com");
        client2.setAddress("NUKIPUKI NOT RUKI");
        clientRepository.save(client1);
        clientRepository.save(client2);
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(httpHeaders);
        ResponseEntity<List<UserDTO>> response = restTemplate.exchange(urlString + "/clients", HttpMethod.GET,
                entity, new ParameterizedTypeReference<List<UserDTO>>() {});
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(response.getBody()).get(0).getId(), is(notNullValue()));
        assertThat(response.getBody().get(0).getFirstName(), is(client1.getFirstName()));
        assertThat(response.getBody().get(0).getLastName(), is(client1.getLastName()));
        assertThat(response.getBody().get(0).getPatronymic(), is(client1.getPatronymic()));
        assertThat(response.getBody().get(0).getEmail(), is(client1.getEmail()));
        assertThat(response.getBody().get(0).getAddress(), is(client1.getAddress()));
        assertThat(response.getBody().get(0).getPhone(), is(client1.getPhone()));
        assertThat(response.getBody().get(0).getUserType(), is("client"));
        assertThat(response.getBody().get(0).getUuid(), is(nullValue()));
        assertThat(response.getBody().get(0).getPosition(), is(nullValue()));
        assertThat(response.getBody().get(0).getLogin(), is(nullValue()));
        assertThat(response.getBody().get(0).getPassword(), is(nullValue()));
        assertThat(response.getBody().get(0).getDeposit(), is("0"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));

        assertThat(response.getBody().get(1).getId(), is(notNullValue()));
        assertThat(response.getBody().get(1).getFirstName(), is(client2.getFirstName()));
        assertThat(response.getBody().get(1).getLastName(), is(client2.getLastName()));
        assertThat(response.getBody().get(1).getPatronymic(), is(client2.getPatronymic()));
        assertThat(response.getBody().get(1).getEmail(), is(client2.getEmail()));
        assertThat(response.getBody().get(1).getAddress(), is(client2.getAddress()));
        assertThat(response.getBody().get(1).getPhone(), is(client2.getPhone()));
        assertThat(response.getBody().get(1).getUserType(), is("client"));
        assertThat(response.getBody().get(1).getUuid(), is(nullValue()));
        assertThat(response.getBody().get(1).getPosition(), is(nullValue()));
        assertThat(response.getBody().get(1).getLogin(), is(nullValue()));
        assertThat(response.getBody().get(1).getPassword(), is(nullValue()));
        assertThat(response.getBody().get(1).getDeposit(), is("0"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
    }

    @Test
    public void getAllClientsByAdminWithWrongId() {
        Client client1 = new Client();
        client1.setFirstName(client.getFirstName());
        client1.setLastName(client.getLastName());
        client1.setPatronymic(client.getPatronymic());
        client1.setLogin(client.getLogin());
        client1.setPassword(client.getPassword());
        client1.setPhone(client.getPhone());
        client1.setDeposit(Integer.parseInt(client.getDeposit()));
        client1.setEmail(client.getEmail());
        client1.setAddress(client.getAddress());
        clientRepository.save(client1);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, "JAVASESSIONID=0007");
        HttpEntity<ProductDTO> entityWrong = new HttpEntity<ProductDTO>(httpHeaders);
        ResponseEntity<List<UserDTO>> response = restTemplate.exchange(urlString + "/clients", HttpMethod.GET,
                entityWrong, new ParameterizedTypeReference<List<UserDTO>>() {});
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(response.getBody()).get(0).getErrors(), is(notNullValue()));
        assertThat(response.getBody().get(0).getErrors().get(0).getMessage(), is("Нет доступа"));
    }

    @Test
    public void addClient() {
        ResponseEntity<UserDTO> responseClient = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(responseClient.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseClient.getBody()).getId(), is(notNullValue()));
        assertThat(responseClient.getBody().getFirstName(), is(client.getFirstName()));
        assertThat(responseClient.getBody().getLastName(), is(client.getLastName()));
        assertThat(responseClient.getBody().getPatronymic(), is(client.getPatronymic()));
        assertThat(responseClient.getBody().getEmail(), is(client.getEmail()));
        assertThat(responseClient.getBody().getAddress(), is(client.getAddress()));
        assertThat(responseClient.getBody().getPhone(), is("+79873355010"));
        assertThat(responseClient.getBody().getUserType(), is("client"));
        assertThat(responseClient.getBody().getUuid(), is(nullValue()));
        assertThat(responseClient.getBody().getPosition(), is(nullValue()));
        assertThat(responseClient.getBody().getLogin(), is(nullValue()));
        assertThat(responseClient.getBody().getPassword(), is(nullValue()));
        assertThat(responseClient.getBody().getDeposit(), is("0"));
        assertThat(responseClient.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void addClientWithWrongParams() {
        client.setFirstName("Roman");
        client.setEmail("Yarkin.paraaaam.ru");
        client.setAddress("");
        client.setPhone("358490");
        ResponseEntity<UserDTO> responseClient = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(responseClient.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseClient.getBody()).getErrors(), is(notNullValue()));
        assertThat(responseClient.getBody().getErrors().size(), is(4));
        assertThat(responseClient.getBody().getErrors().get(0).getMessage(), is("Имя может содержать только русские буквы, пробелы и знак тире между символами"));
        assertThat(responseClient.getBody().getErrors().get(1).getMessage(), is("Email не валиден"));
        assertThat(responseClient.getBody().getErrors().get(2).getMessage(), is("Мобильный номер не валиден"));
        assertThat(responseClient.getBody().getErrors().get(3).getMessage(), is("Поле address не должно быть пустым"));
    }

    @Test
    public void editClient(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        client.setFirstName("Настенка");
        client.setLastName("Пиреева");
        client.setLogin("логин66666");
        client.setOldPassword(client.getPassword());
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(client, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/clients", HttpMethod.PUT, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getFirstName(), is("Настенка"));
        assertThat(responseEntity.getBody().getLastName(), is("Пиреева"));
        assertThat(responseEntity.getBody().getLogin(), is("логин66666"));
    }

    @Test
    public void editClientWithWrongPassword(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        client.setFirstName("Настенка");
        client.setLastName("Пиреева");
        client.setLogin("логин66666");
        client.setOldPassword("пароль");
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(client, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/clients", HttpMethod.PUT, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody().getErrors().get(0).getMessage(), is("Пароли не совпадают"));
    }

    @Test
    public void addCash() {
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        UserDTO cash = new UserDTO();
        cash.setDeposit("500");
        HttpEntity<UserDTO> entityCash = new HttpEntity<UserDTO>(cash, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/deposits", HttpMethod.PUT, entityCash, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getDeposit(), is("500"));
    }

    @Test
    public void addCashWithWrongParam() {
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        UserDTO cash = new UserDTO();
        cash.setDeposit("kurlukurlu");
        HttpEntity<UserDTO> entityCash = new HttpEntity<UserDTO>(cash, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/deposits", HttpMethod.PUT, entityCash, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors(), is(notNullValue()));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors().get(0).getMessage(), is("Введите кол-во денег в верном формате"));
    }

    @Test
    public void getCash(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        HttpEntity<UserDTO> entityCash = new HttpEntity<UserDTO>(httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/deposits", HttpMethod.GET, entityCash, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors(), is(nullValue()));
        assertThat(responseEntity.getBody().getDeposit(), is(client.getDeposit()));
    }
}
