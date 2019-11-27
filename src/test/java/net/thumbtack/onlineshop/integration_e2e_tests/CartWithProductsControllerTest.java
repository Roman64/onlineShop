package net.thumbtack.onlineshop.integration_e2e_tests;

import net.thumbtack.onlineshop.data.dto.CartWithProductsDTO;
import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CartWithProductsControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    private CategoryDTO categoryDTO = new CategoryDTO();
    private ProductDTO productDTO = new ProductDTO();
    private URL urlString;
    private HttpHeaders httpHeadersClient;
    private HttpHeaders httpHeadersAdmin;

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
        categoryDTO.setName("Тестовая категория_1");
        productDTO.setName("Продукт1_тестовой категории");
        productDTO.setPrice("50");
        productDTO.setCount("10");
        ResponseEntity<UserDTO> responseClient = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(responseClient.getStatusCode(), is(HttpStatus.OK));
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
    }

    @After
    public void resetDb() {
        ResponseEntity<String> responseAdmin = restTemplate.postForEntity(urlString + "/debug/clear", "clear", String.class);
        assertThat(responseAdmin.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void addProductToBasket() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        //создаем продукт для добавления в корзину
        ProductDTO toBasket = new ProductDTO();
        toBasket.setId(Objects.requireNonNull(responseEntity.getBody()).getId());
        toBasket.setName(productDTO.getName());
        toBasket.setPrice(productDTO.getPrice());
        toBasket.setCount("50");
        //добавляем
        HttpEntity<ProductDTO> entityBasket = new HttpEntity<ProductDTO>(toBasket, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseBasket.getBody().getRemaining().get(0).getCount(), is("50"));
        assertThat(responseBasket.getBody().getRemaining().get(0).getName(), is(productDTO.getName()));
        assertThat(responseBasket.getBody().getRemaining().get(0).getCount(), is(productDTO.getPrice()));
    }

    @Test
    public void addProductToBasketTwice() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        //создаем продукт для добавления в корзину
        ProductDTO toBasket = new ProductDTO();
        toBasket.setId(Objects.requireNonNull(responseEntity.getBody()).getId());
        toBasket.setName(productDTO.getName());
        toBasket.setPrice(productDTO.getPrice());
        toBasket.setCount("50");
        //добавляем первый раз
        HttpEntity<ProductDTO> entityBasket = new HttpEntity<ProductDTO>(toBasket, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseBasket.getBody().getRemaining().size(), is (1));
        assertThat(responseBasket.getBody().getRemaining().get(0).getCount(), is("50"));
        assertThat(responseBasket.getBody().getRemaining().get(0).getName(), is(productDTO.getName()));
        assertThat(responseBasket.getBody().getRemaining().get(0).getCount(), is(productDTO.getPrice()));
        //добавляем второй раз
        ResponseEntity<CartWithProductsDTO> responseBasket2= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket2.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseBasket2.getBody().getRemaining().size(), is (1));
        assertThat(responseBasket2.getBody().getRemaining().get(0).getCount(), is("100"));
        assertThat(responseBasket2.getBody().getRemaining().get(0).getName(), is(productDTO.getName()));
        assertThat(responseBasket2.getBody().getRemaining().get(0).getPrice(), is(productDTO.getPrice()));
    }

    @Test
    public void addProductWithWrongParams(){
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        //создаем продукт для добавления в корзину
        ProductDTO toBasket = new ProductDTO();
        toBasket.setId("11");
        toBasket.setName(productDTO.getName());
        toBasket.setPrice(productDTO.getPrice());
        toBasket.setCount("10");
        //добавляем с неверным ID
        HttpEntity<ProductDTO> entityBasket = new HttpEntity<ProductDTO>(toBasket, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseBasket.getBody().getErrors(), is(notNullValue()));
        assertThat(responseBasket.getBody().getErrors().get(0).getMessage(), is("Товара по данному id не найдено"));
        //добавляем с неверной ценой
        toBasket.setId(Objects.requireNonNull(responseEntity.getBody()).getId());
        toBasket.setPrice("300");
        ResponseEntity<CartWithProductsDTO> responseBasket2= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket2.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseBasket2.getBody().getErrors(), is(notNullValue()));
        assertThat(responseBasket2.getBody().getErrors().get(0).getMessage(), is("Цены товаров не совпадают"));
        //добавляем с неверным именем
        toBasket.setPrice(productDTO.getPrice());
        toBasket.setName("КУРЛЫКУРЛЫ");
        ResponseEntity<CartWithProductsDTO> responseBasket3= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket3.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseBasket3.getBody().getErrors(), is(notNullValue()));
        assertThat(responseBasket3.getBody().getErrors().get(0).getMessage(), is("Названия товаров не совпадают"));
    }

    @Test
    public void deleteProductFromBasket() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        ProductDTO productDTO_2 = new ProductDTO();
        productDTO_2.setName("Продукт 2");
        productDTO_2.setPrice("20");
        productDTO_2.setCount("20");
        HttpEntity<ProductDTO> entity_2 = new HttpEntity<ProductDTO>(productDTO_2, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntity_2 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity_2, ProductDTO.class);
        assertThat(responseEntity_2.getStatusCode(), is(HttpStatus.OK));
        //создаем продукт для добавления в корзину
        ProductDTO toBasket = new ProductDTO();
        toBasket.setId(Objects.requireNonNull(responseEntity.getBody()).getId());
        toBasket.setName(productDTO.getName());
        toBasket.setPrice(productDTO.getPrice());
        toBasket.setCount("50");
        ProductDTO toBasket2 = new ProductDTO();
        toBasket2.setId(Objects.requireNonNull(responseEntity_2.getBody()).getId());
        toBasket2.setName(productDTO_2.getName());
        toBasket2.setPrice(productDTO_2.getPrice());
        toBasket2.setCount("50");
        //добавляем
        HttpEntity<ProductDTO> entityBasket = new HttpEntity<ProductDTO>(toBasket, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> entityBasket2 = new HttpEntity<ProductDTO>(toBasket2, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket2= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket2, CartWithProductsDTO.class);
        assertThat(responseBasket2.getStatusCode(), is(HttpStatus.OK));
        //удаляем
        HttpEntity<ProductDTO> entityBasketDelete = new HttpEntity<ProductDTO>(httpHeadersClient);
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", Objects.requireNonNull(responseEntity.getBody()).getId());
        ResponseEntity<CartWithProductsDTO> responseDelete= restTemplate.exchange(urlString + "/baskets/{id}" ,
                HttpMethod.DELETE, entityBasketDelete, CartWithProductsDTO.class, params);
        assertThat(responseDelete.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void editProductsInBasket() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        //создаем продукт для добавления в корзину
        ProductDTO toBasket = new ProductDTO();
        toBasket.setId(Objects.requireNonNull(responseEntity.getBody()).getId());
        toBasket.setName(productDTO.getName());
        toBasket.setPrice(productDTO.getPrice());
        toBasket.setCount("50");
        //добавляем
        HttpEntity<ProductDTO> entityBasket = new HttpEntity<ProductDTO>(toBasket, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket, CartWithProductsDTO.class);
        assertThat(responseBasket.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseBasket.getBody().getRemaining().get(0).getCount(), is (toBasket.getCount()));
        //редактируем
        toBasket.setCount("10");
        HttpEntity<ProductDTO> entityBasketEdit = new HttpEntity<ProductDTO>(toBasket, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseEdit= restTemplate.exchange(urlString + "/baskets" ,
                HttpMethod.PUT, entityBasketEdit, CartWithProductsDTO.class);
        assertThat(responseEdit.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEdit.getBody()).getRemaining().get(0).getCount(), is (toBasket.getCount()));
    }

    @Test
    public void getProductsFromBasket() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        //Добавим еще два товара
        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setName("Продукт2");
        productDTO2.setPrice("10000");
        productDTO2.setCount("5");
        ProductDTO productDTO3 = new ProductDTO();
        productDTO3.setName("Продукт3");
        productDTO3.setPrice("5");
        productDTO3.setCount("300");
        HttpEntity<ProductDTO> entityProd2 = new HttpEntity<ProductDTO>(productDTO2, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseProd2 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProd2, ProductDTO.class);
        assertThat(responseProd2.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> entityProd3 = new HttpEntity<ProductDTO>(productDTO3, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseProd3 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProd3, ProductDTO.class);
        assertThat(responseProd3.getStatusCode(), is(HttpStatus.OK));
        //Добавим эти товары в корзину
        ProductDTO toBasket1 = new ProductDTO();
        toBasket1.setId(Objects.requireNonNull(responseEntity.getBody()).getId());
        toBasket1.setName(productDTO.getName());
        toBasket1.setPrice(productDTO.getPrice());
        toBasket1.setCount("50");
        HttpEntity<ProductDTO> entityBasket1 = new HttpEntity<ProductDTO>(toBasket1, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket1= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket1, CartWithProductsDTO.class);
        assertThat(responseBasket1.getStatusCode(), is(HttpStatus.OK));

        ProductDTO toBasket2 = new ProductDTO();
        toBasket2.setId(Objects.requireNonNull(responseProd2.getBody()).getId());
        toBasket2.setName(productDTO2.getName());
        toBasket2.setPrice(productDTO2.getPrice());
        toBasket2.setCount("2");
        HttpEntity<ProductDTO> entityBasket2 = new HttpEntity<ProductDTO>(toBasket2, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket2= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket2, CartWithProductsDTO.class);
        assertThat(responseBasket2.getStatusCode(), is(HttpStatus.OK));

        ProductDTO toBasket3 = new ProductDTO();
        toBasket3.setId(Objects.requireNonNull(responseProd3.getBody()).getId());
        toBasket3.setName(productDTO3.getName());
        toBasket3.setPrice(productDTO3.getPrice());
        toBasket3.setCount("100");
        HttpEntity<ProductDTO> entityBasket3 = new HttpEntity<ProductDTO>(toBasket3, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBasket3= restTemplate.exchange(urlString + "/baskets", HttpMethod.POST, entityBasket3, CartWithProductsDTO.class);
        assertThat(responseBasket3.getStatusCode(), is(HttpStatus.OK));
        //Достаем из корзины продукты
        HttpEntity<ProductDTO> entityGet = new HttpEntity<ProductDTO>(httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseGet= restTemplate.exchange(urlString + "/baskets", HttpMethod.GET, entityGet, CartWithProductsDTO.class);
        assertThat(responseGet.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseGet.getBody().getRemaining().size(), is(3));
        assertThat(responseGet.getBody().getRemaining().get(0).getCount(), is("50"));
        assertThat(responseGet.getBody().getRemaining().get(1).getCount(), is("2"));
        assertThat(responseGet.getBody().getRemaining().get(2).getCount(), is("100"));
    }

    @Test
    public void buyProductsFromBasket() {
        productDTO.setName("Продукт_1");
        productDTO.setPrice("10");
        productDTO.setCount("50");
        HttpEntity<ProductDTO> entityProduct1 = new HttpEntity<ProductDTO>(productDTO, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct1 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct1, ProductDTO.class);
        assertThat(responseEntityProduct1.getStatusCode(), is(HttpStatus.OK));
        // Добавим для теста еще два товара
        ProductDTO productDTO_2 = new ProductDTO();
        productDTO_2.setName("Продукт_2");
        productDTO_2.setPrice("20");
        productDTO_2.setCount("20");
        HttpEntity<ProductDTO> entityProduct2 = new HttpEntity<ProductDTO>(productDTO_2, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct2 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct2, ProductDTO.class);
        assertThat(responseEntityProduct2.getStatusCode(), is(HttpStatus.OK));
        ProductDTO productDTO_3 = new ProductDTO();
        productDTO_3.setName("Продукт_3");
        productDTO_3.setPrice("100");
        productDTO_3.setCount("5");
        HttpEntity<ProductDTO> entityProduct3 = new HttpEntity<ProductDTO>(productDTO_3, httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseEntityProduct3 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entityProduct3, ProductDTO.class);
        assertThat(responseEntityProduct3.getStatusCode(), is(HttpStatus.OK));
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
        // Теперь купим данные товары, но в первом изменим кол-во на 8, чтобы в корзине осталось еще 2 таких товара. А во втором попробуем указать больше кол-во,
        // чем в корзине. Должно купиться максимальное кол-во из корзины, но мы превысили лимит денег. На счету клиента пока 0
        toBasket1.setCount("8");
        toBasket2.setCount("10");
        List<ProductDTO> productDTOList = new ArrayList<>();
        productDTOList.add(toBasket1);
        productDTOList.add(toBasket2);
        HttpEntity<List<ProductDTO>> buyProducts = new HttpEntity<List<ProductDTO>>(productDTOList, httpHeadersClient);
        ResponseEntity<CartWithProductsDTO> responseBuy= restTemplate.exchange(urlString + "/purchases/baskets", HttpMethod.POST, buyProducts, CartWithProductsDTO.class);
        assertThat(responseBuy.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseBuy.getBody().getErrors().get(0).getMessage(), is("Не хватает денег на счету"));
        //Теперь добавим клиенту денег на счет для покупки
        UserDTO cash = new UserDTO();
        cash.setDeposit("500");
        HttpEntity<UserDTO> entityCash = new HttpEntity<UserDTO>(cash, httpHeadersClient);
        ResponseEntity<UserDTO> responseEntity = restTemplate.exchange(urlString + "/deposits", HttpMethod.PUT, entityCash, UserDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getDeposit(), is("500"));
        //Проверяем покупку
        ResponseEntity<CartWithProductsDTO> responseBuyTrue= restTemplate.exchange(urlString + "/purchases/baskets", HttpMethod.POST, buyProducts, CartWithProductsDTO.class);
        assertThat(responseBuyTrue.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseBuyTrue.getBody().getErrors(), is(nullValue()));
        assertThat(responseBuyTrue.getBody().getRemaining().size(), is(1));
        assertThat(responseBuyTrue.getBody().getBought().size(), is((2)));
        assertThat(responseBuyTrue.getBody().getRemaining().get(0).getCount(), is("2"));
        assertThat(responseBuyTrue.getBody().getBought().get(0).getCount(), is("8"));
        assertThat(responseBuyTrue.getBody().getBought().get(1).getCount(), is("5"));
        //проверяем обновленные продукты
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", responseEntityProduct1.getBody().getId());
        HttpEntity<ProductDTO> entityGetProduct = new HttpEntity<ProductDTO>(httpHeadersAdmin);
        ResponseEntity<ProductDTO> responseGetProduct1 = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.GET, entityGetProduct, ProductDTO.class, params);
        assertThat(responseGetProduct1.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseGetProduct1.getBody()).getId(), is(notNullValue()));
        assertThat(responseGetProduct1.getBody().getName(), is(productDTO.getName()));
        assertThat(responseGetProduct1.getBody().getPrice(), is(productDTO.getPrice()));
        assertThat(responseGetProduct1.getBody().getCount(), is("42"));

        params.put("id", responseEntityProduct2.getBody().getId());
        ResponseEntity<ProductDTO> responseGetProduct2 = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.GET, entityGetProduct, ProductDTO.class, params);
        assertThat(responseGetProduct2.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseGetProduct2.getBody()).getId(), is(notNullValue()));
        assertThat(responseGetProduct2.getBody().getName(), is(productDTO_2.getName()));
        assertThat(responseGetProduct2.getBody().getPrice(), is(productDTO_2.getPrice()));
        assertThat(responseGetProduct2.getBody().getCount(), is("15"));

        params.put("id", responseEntityProduct3.getBody().getId());
        ResponseEntity<ProductDTO> responseGetProduct3 = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.GET, entityGetProduct, ProductDTO.class, params);
        assertThat(responseGetProduct3.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseGetProduct3.getBody()).getId(), is(notNullValue()));
        assertThat(responseGetProduct3.getBody().getName(), is(productDTO_3.getName()));
        assertThat(responseGetProduct3.getBody().getPrice(), is(productDTO_3.getPrice()));
        assertThat(responseGetProduct3.getBody().getCount(), is("5"));
        //проверяем клиента
        HttpEntity<UserDTO> entityNewCash = new HttpEntity<UserDTO>(httpHeadersClient);
        ResponseEntity<UserDTO> responseNewCash = restTemplate.exchange(urlString + "/deposits", HttpMethod.GET, entityNewCash, UserDTO.class);
        assertThat(responseNewCash.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseNewCash.getBody()).getErrors(), is(nullValue()));
        assertThat(responseNewCash.getBody().getDeposit(), is("320"));
    }
}
