package net.thumbtack.onlineshop.integration_e2e_tests;

import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.services.AdminService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CommonControllerTest {

    @Autowired
    private AdminService adminService;
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
    public void loginAdmin(){
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        admin.setFirstName(null);
        admin.setLastName(null);
        admin.setPatronymic(null);
        admin.setPosition(null);
        ResponseEntity<UserDTO> responseLogin = restTemplate.postForEntity(urlString + "/sessions", admin, UserDTO.class);
        assertThat(responseLogin.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void loginAdminWithWrongParams() {
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        admin.setFirstName(null);
        admin.setLastName(null);
        admin.setPatronymic(null);
        admin.setPosition(null);
        admin.setLogin("логин");
        admin.setPassword("пароль");
        ResponseEntity<UserDTO> responseLogin = restTemplate.postForEntity(urlString + "/sessions", admin, UserDTO.class);
        assertThat(responseLogin.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseLogin.getBody().getErrors().get(0).getMessage(), is("Неверные данные"));
    }

    @Test
    public void logoutAdmin() {
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(null, httpHeaders);
        Assert.assertTrue(adminService.isLogin(Objects.requireNonNull(set_cookie).substring(14)));
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/sessions", HttpMethod.DELETE, entity, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Assert.assertFalse(adminService.isLogin(Objects.requireNonNull(set_cookie).substring(14)));
    }

    @Test
    public void logoutAdminWithWrongParams() {
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie.substring(0, set_cookie.length()-1) + "aaaaa");
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(httpHeaders);
        Assert.assertTrue(adminService.isLogin(Objects.requireNonNull(set_cookie).substring(14)));
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/sessions", HttpMethod.DELETE, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody().getErrors().get(0).getMessage(), is("Данной сессии не существует"));
    }

    @Test
    public void getUserInfo() {
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(null, httpHeaders);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/accounts", HttpMethod.GET, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getId(), notNullValue());
        assertThat(responseEntity.getBody().getFirstName(), is("Роман"));
        assertThat(responseEntity.getBody().getLastName(), is("Коваценко"));
        assertThat(responseEntity.getBody().getPatronymic(), is("Александрович"));
        assertThat(responseEntity.getBody().getPosition(), is("тестовый"));
    }

    @Test
    public void getUserInfoWithWrongParams() {
        ResponseEntity<UserDTO> response = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        HttpHeaders httpHeaders = response.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie.substring(0, set_cookie.length()-1) + "aaaaa");
        HttpEntity<UserDTO> entity = new HttpEntity<UserDTO>(httpHeaders);
        Assert.assertTrue(adminService.isLogin(Objects.requireNonNull(set_cookie).substring(14)));
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/accounts", HttpMethod.GET, entity, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody().getErrors().get(0).getMessage(), is("Данной сессии не существует"));
    }
}
