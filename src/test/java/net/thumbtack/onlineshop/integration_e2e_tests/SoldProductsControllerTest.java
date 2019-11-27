package net.thumbtack.onlineshop.integration_e2e_tests;

import net.thumbtack.onlineshop.data.dto.*;
import net.thumbtack.onlineshop.data.model.Product;
import net.thumbtack.onlineshop.data.repository.*;
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
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SoldProductsControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    private CategoryDTO categoryDTO = new CategoryDTO();
    private ProductDTO productDTO = new ProductDTO();
    private URL urlString;
    private HttpHeaders httpHeadersClient;
    private HttpHeaders httpHeadersAdmin;
    private int clientId;
    private int productOneId;
    private int productTwoId;
    private int productThreeId;

    @Before
    public void beforeTest() throws MalformedURLException {
        urlString = new URL("http://localhost:8080/api");
        UserDTO client = new UserDTO();
        client.setFirstName("Роман");
        client.setLastName("Коваценко");
        client.setPatronymic("Александрович");
        client.setLogin("Логин12345");
        client.setPassword("пароль12345");
        client.setDeposit("500");
        client.setAddress("Radioman");
        client.setPhone("+79873355010");
        client.setEmail("Kovatsenko@gmail.com");
        UserDTO admin = new UserDTO();
        admin.setFirstName("Василий");
        admin.setLastName("Коваценко");
        admin.setPatronymic("Александрович");
        admin.setLogin("Логин7777");
        admin.setPassword("пароль12345");
        admin.setPosition("director");
        ResponseEntity<UserDTO> responseClient = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(responseClient.getStatusCode(), is(HttpStatus.OK));
        clientId = Integer.parseInt(Objects.requireNonNull(responseClient.getBody()).getId());
        httpHeadersClient = responseClient.getHeaders();
        String set_cookie1 = httpHeadersClient.getFirst(HttpHeaders.SET_COOKIE);
        httpHeadersClient = new HttpHeaders();
        httpHeadersClient.add(HttpHeaders.COOKIE, set_cookie1);
        ResponseEntity<UserDTO> responseAdmin = restTemplate.postForEntity(urlString + "/admins", admin, UserDTO.class);
        assertThat(responseAdmin.getStatusCode(), is(HttpStatus.OK));
        httpHeadersAdmin = responseAdmin.getHeaders();
        String set_cookie2 = httpHeadersAdmin.getFirst(HttpHeaders.SET_COOKIE);
        httpHeadersAdmin = new HttpHeaders();
        httpHeadersAdmin.add(HttpHeaders.COOKIE, set_cookie2);
        UserDTO cash = new UserDTO();
        cash.setDeposit("500");
        HttpEntity<UserDTO> entityCash = new HttpEntity<UserDTO>(cash, httpHeadersClient);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/deposits", HttpMethod.PUT, entityCash, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getDeposit(), is("500"));
        productDTO.setName("Продукт_1");
        productDTO.setPrice("10");
        productDTO.setCount("50");
        HttpEntity<ProductDTO> entityProduct1 = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct1 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct1, ProductDTO.class);
        assertThat(responseEntityProduct1.getStatusCode(), is(HttpStatus.OK));
        productOneId = Integer.parseInt(Objects.requireNonNull(responseEntityProduct1.getBody()).getId());
        // Добавим для теста еще два товара
        ProductDTO productDTO_2 = new ProductDTO();
        productDTO_2.setName("Продукт_2");
        productDTO_2.setPrice("20");
        productDTO_2.setCount("20");
        HttpEntity<ProductDTO> entityProduct2 = new HttpEntity<ProductDTO>(productDTO_2, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct2 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct2, ProductDTO.class);
        assertThat(responseEntityProduct2.getStatusCode(), is(HttpStatus.OK));
        productTwoId = Integer.parseInt(Objects.requireNonNull(responseEntityProduct2.getBody()).getId());
        ProductDTO productDTO_3 = new ProductDTO();
        productDTO_3.setName("Продукт_3");
        productDTO_3.setPrice("100");
        productDTO_3.setCount("5");
        HttpEntity<ProductDTO> entityProduct3 = new HttpEntity<ProductDTO>(productDTO_3, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct3 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct3, ProductDTO.class);
        assertThat(responseEntityProduct3.getStatusCode(), is(HttpStatus.OK));
        productThreeId = Integer.parseInt(Objects.requireNonNull(responseEntityProduct3.getBody()).getId());
        // Добавим два из этих товаров в корзину клиенту
        ProductDTO toBasket1 = new ProductDTO();
        toBasket1.setId(Objects.requireNonNull(responseEntityProduct1.getBody()).getId());
        toBasket1.setName(productDTO.getName());
        toBasket1.setPrice(productDTO.getPrice());
        toBasket1.setCount("10");
        HttpEntity<ProductDTO> entityBasket1 = new HttpEntity<ProductDTO>(toBasket1, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket1= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket1, CartWithProductsDTO.class);
        assertThat(responseBasket1.getStatusCode(), is(HttpStatus.OK));
        ProductDTO toBasket2 = new ProductDTO();
        toBasket2.setId(Objects.requireNonNull(responseEntityProduct2.getBody()).getId());
        toBasket2.setName(productDTO_2.getName());
        toBasket2.setPrice(productDTO_2.getPrice());
        toBasket2.setCount("5");
        HttpEntity<ProductDTO> entityBasket2 = new HttpEntity<ProductDTO>(toBasket2, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket2= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket2, CartWithProductsDTO.class);
        assertThat(responseBasket2.getStatusCode(), is(HttpStatus.OK));
        //осуществляем покупку
        List<ProductDTO> productDTOList = new ArrayList<>();
        productDTOList.add(toBasket1);
        productDTOList.add(toBasket2);
        HttpEntity<List<ProductDTO>> buyProducts = new HttpEntity<List<ProductDTO>>(productDTOList, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBuyTrue= restTemplate.exchange(urlString + "/purchases/baskets", HttpMethod.POST, buyProducts, CartWithProductsDTO.class);
        assertThat(responseBuyTrue.getStatusCode(), is(HttpStatus.OK));
    }

    @After
    public void resetDb() {
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(urlString + "/debug/clear", "clear", String.class);
        assertThat(responseAdmin.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void getSoldProductsByDate() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        LocalDateTime localDateTime1 = LocalDateTime.now();
        String date1 = localDateTime1.toString().substring(0, 10);
        LocalDateTime localDateTime2 = LocalDateTime.now();
        String date2 = localDateTime2.plusDays(2).toString().substring(0, 10);
        ResponseEntity<List<SoldProductsDTO>> responseDate = restTemplate.exchange(urlString +
                        "/purchases/date?dateFirst={dateFirst}&dateSecond={dateSecond}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, date1, date2);
        assertThat(responseDate.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseDate.getBody(), is(notNullValue()));
        assertThat(responseDate.getBody().size(), is(3));
        assertThat(responseDate.getBody().get(0).getClientFullName(), is("Роман Коваценко"));
        assertThat(responseDate.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(responseDate.getBody().get(0).getProductName(), is("Продукт_1"));
        assertThat(responseDate.getBody().get(0).getCount(), is("10"));
        assertThat(responseDate.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(responseDate.getBody().get(1).getClientFullName(), is("Роман Коваценко"));
        assertThat(responseDate.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(responseDate.getBody().get(1).getProductName(), is("Продукт_2"));
        assertThat(responseDate.getBody().get(1).getCount(), is("5"));
        assertThat(responseDate.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(responseDate.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 200"));
    }

    @Test
    public void getSoldProductsByWrongDates() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> responseDate = restTemplate.exchange(urlString +
                        "/purchases/date?dateFirst={dateFirst}&dateSecond={dateSecond}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, "2019.07.06", "2019.14.16");
        assertThat(responseDate.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseDate.getBody(), is(notNullValue()));
        assertThat(responseDate.getBody().get(0).getErrors().get(0).getMessage(), is("Введите дату в формате yyyy-MM-dd"));
    }

    @Test
    public void getSoldProductsByNoDates() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> responseDate = restTemplate.exchange(urlString +
                        "/purchases/date?dateFirst={dateFirst}&dateSecond={dateSecond}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, "2019-06-10", "2019-06-12");
        assertThat(responseDate.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseDate.getBody(), is(notNullValue()));
        assertThat(responseDate.getBody().get(0).getErrors().get(0).getMessage(), is("По таким датам заказы не найдены"));
    }

    @Test
    public void getSoldProductsByWrongSession() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersClient);
        ResponseEntity<List<SoldProductsDTO>> responseDate = restTemplate.exchange(urlString +
                        "/purchases/date?dateFirst={dateFirst}&dateSecond={dateSecond}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, "2019.07.06", "2019.14.16");
        assertThat(responseDate.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseDate.getBody(), is(notNullValue()));
        assertThat(responseDate.getBody().get(0).getErrors().get(0).getMessage(), is("Нет доступа"));
    }

    @Test
    public void getSoldProductsByClient() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases/client?clientId={clientId}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, clientId);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(3));
        assertThat(response.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(0).getProductName(), is("Продукт_1"));
        assertThat(response.getBody().get(0).getCount(), is("10"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(1).getProductName(), is("Продукт_2"));
        assertThat(response.getBody().get(1).getCount(), is("5"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 200"));
    }

    @Test
    public void getSoldProductsByClientWrongSession() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersClient);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases/client?clientId={clientId}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, clientId);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().get(0).getErrors().get(0).getMessage(), is("Нет доступа"));
    }

    @Test
    public void getSoldProductsByWrongClient() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases/client?clientId={clientId}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, 3045);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().get(0).getErrors().get(0).getMessage(), is("Нет товаров с таким id клиента"));
    }

    @Test
    public void getSoldProductsByProductsId() {
        List<Integer> products = new ArrayList<>();
        products.add(productOneId);
        products.add(productTwoId);
        String listOfIds = products.stream().map(Object::toString).collect(Collectors.joining(","));
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases?products={products}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, listOfIds);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(3));
        assertThat(response.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(0).getProductName(), is("Продукт_2"));
        assertThat(response.getBody().get(0).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(0).getCount(), is("5"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(1).getProductName(), is("Продукт_1"));
        assertThat(response.getBody().get(1).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(1).getCount(), is("10"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 200"));
    }

    @Test
    public void getSoldProductsByProductsIdWithOneWrongProduct() {
        List<Integer> products = new ArrayList<>();
        products.add(productOneId);
        products.add(productTwoId);
        products.add(productThreeId);
        String listOfIds = products.stream().map(Object::toString).collect(Collectors.joining(","));
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases?products={products}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, listOfIds);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(3));
        assertThat(response.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(0).getProductName(), is("Продукт_2"));
        assertThat(response.getBody().get(0).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(0).getCount(), is("5"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(1).getProductName(), is("Продукт_1"));
        assertThat(response.getBody().get(1).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(1).getCount(), is("10"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 200"));
    }

    @Test
    public void getSoldProductsByProductsIdOrderByDate() {
        List<Integer> products = new ArrayList<>();
        products.add(productOneId);
        products.add(productTwoId);
        String listOfIds = products.stream().map(Object::toString).collect(Collectors.joining(","));
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases?products={products}&order={order}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, listOfIds, "date");
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(3));
        assertThat(response.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(0).getProductName(), is("Продукт_1"));
        assertThat(response.getBody().get(0).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(0).getCount(), is("10"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(1).getProductName(), is("Продукт_2"));
        assertThat(response.getBody().get(1).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(1).getCount(), is("5"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 200"));
    }

    @Test
    public void getSoldProductsWithoutProducts() {
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {});
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(3));
        assertThat(response.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(0).getProductName(), is("Продукт_2"));
        assertThat(response.getBody().get(0).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(0).getCount(), is("5"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(1).getProductName(), is("Продукт_1"));
        assertThat(response.getBody().get(1).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(1).getCount(), is("10"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 200"));
    }

    @Test
    public void getSoldProductsByCategories() {
        //добавим для теста еще три продукта с тестовой категорией
        categoryDTO.setName("Тестовая категория");
        HttpEntity<CategoryDTO> entity_category = new HttpEntity<CategoryDTO>(categoryDTO, httpHeadersAdmin);
        ResponseEntity<CategoryDTO> responseCategory = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_category, CategoryDTO.class);
        assertThat(responseCategory.getStatusCode(), is(HttpStatus.OK));

        ProductDTO productDTO_4 = new ProductDTO();
        productDTO_4.setName("Продукт_4");
        productDTO_4.setPrice("50");
        productDTO_4.setCount("10");
        productDTO_4.setCategories(new ArrayList<>());
        productDTO_4.getCategories().add(Integer.parseInt(Objects.requireNonNull(responseCategory.getBody()).getId()));
        HttpEntity<ProductDTO> entityProduct4 = new HttpEntity<ProductDTO>(productDTO_4, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct4 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct4, ProductDTO.class);
        assertThat(responseEntityProduct4.getStatusCode(), is(HttpStatus.OK));
        ProductDTO productDTO_5 = new ProductDTO();
        productDTO_5.setName("Продукт_5");
        productDTO_5.setPrice("50");
        productDTO_5.setCount("4");
        productDTO_5.setCategories(new ArrayList<>());
        productDTO_5.getCategories().add(Integer.parseInt(Objects.requireNonNull(responseCategory.getBody()).getId()));
        HttpEntity<ProductDTO> entityProduct5 = new HttpEntity<ProductDTO>(productDTO_5, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct5 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct5, ProductDTO.class);
        assertThat(responseEntityProduct5.getStatusCode(), is(HttpStatus.OK));
        productThreeId = Integer.parseInt(Objects.requireNonNull(responseEntityProduct5.getBody()).getId());
        // Добавим два из этих товаров в корзину клиенту
        ProductDTO toBasket4 = new ProductDTO();
        toBasket4.setId(Objects.requireNonNull(responseEntityProduct4.getBody()).getId());
        toBasket4.setName(productDTO_4.getName());
        toBasket4.setPrice(productDTO_4.getPrice());
        toBasket4.setCount("3");
        HttpEntity<ProductDTO> entityBasket4 = new HttpEntity<ProductDTO>(toBasket4, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket4= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket4, CartWithProductsDTO.class);
        assertThat(responseBasket4.getStatusCode(), is(HttpStatus.OK));
        ProductDTO toBasket5 = new ProductDTO();
        toBasket5.setId(Objects.requireNonNull(responseEntityProduct5.getBody()).getId());
        toBasket5.setName(productDTO_5.getName());
        toBasket5.setPrice(productDTO_5.getPrice());
        toBasket5.setCount("2");
        HttpEntity<ProductDTO> entityBasket5 = new HttpEntity<ProductDTO>(toBasket5, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket5= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket5, CartWithProductsDTO.class);
        assertThat(responseBasket5.getStatusCode(), is(HttpStatus.OK));
        //осуществляем покупку
        List<ProductDTO> productDTOList = new ArrayList<>();
        productDTOList.add(toBasket4);
        productDTOList.add(toBasket5);
        HttpEntity<List<ProductDTO>> buyProducts = new HttpEntity<List<ProductDTO>>(productDTOList, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBuyTrue= restTemplate.exchange(urlString + "/purchases/baskets", HttpMethod.POST, buyProducts, CartWithProductsDTO.class);
        assertThat(responseBuyTrue.getStatusCode(), is(HttpStatus.OK));
        //делаем запрос на купленные товары
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases?categories={categories}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, responseCategory.getBody().getId());
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(3));
        assertThat(response.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(0).getProductName(), is("Продукт_5"));
        assertThat(response.getBody().get(0).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(0).getCategoriesName().get(0), is("Тестовая категория"));
        assertThat(response.getBody().get(0).getCount(), is("2"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(1).getProductName(), is("Продукт_4"));
        assertThat(response.getBody().get(1).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(1).getCategoriesName().get(0), is("Тестовая категория"));
        assertThat(response.getBody().get(1).getCount(), is("3"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 250"));
    }

    @Test
    public void getSoldProductsWithoutProductsIdFromDate() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String date = localDateTime.toString().substring(0, 10);
        HttpEntity<List<SoldProductsDTO>> entitySold = new HttpEntity<List<SoldProductsDTO>>(httpHeadersAdmin);
        ResponseEntity<List<SoldProductsDTO>> response = restTemplate.exchange(urlString +
                        "/purchases?date={date}", HttpMethod.GET,
                entitySold, new ParameterizedTypeReference<List<SoldProductsDTO>>() {}, date);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody().size(), is(3));
        assertThat(response.getBody().get(0).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(0).getProductName(), is("Продукт_2"));
        assertThat(response.getBody().get(0).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(0).getCount(), is("5"));
        assertThat(response.getBody().get(0).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(1).getDate(), is(notNullValue()));
        assertThat(response.getBody().get(1).getProductName(), is("Продукт_1"));
        assertThat(response.getBody().get(1).getClientFullName(), is("Роман Коваценко"));
        assertThat(response.getBody().get(1).getCount(), is("10"));
        assertThat(response.getBody().get(1).getErrors(), is(nullValue()));
        assertThat(response.getBody().get(2).getMessageForUser(), is("Итого сделано покупок: 2, на сумму: 200"));
    }
}
