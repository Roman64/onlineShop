package net.thumbtack.onlineshop.integration_e2e_tests;

import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
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
import java.util.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProductControllerTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestRestTemplate restTemplate;
    private CategoryDTO categoryDTO = new CategoryDTO();
    private ProductDTO productDTO = new ProductDTO();
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
    public void addProductWithoutCategory() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getId(), is(notNullValue()));
        assertThat(responseEntity.getBody().getName(), is(productDTO.getName()));
        assertThat(responseEntity.getBody().getPrice(), is(productDTO.getPrice()));
        assertThat(responseEntity.getBody().getCount(), is(productDTO.getCount()));
        assertThat(responseEntity.getBody().getCategoriesName(), is(nullValue()));
        assertThat(responseEntity.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void addProductWithWrongCount() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        productDTO.setCount("-20");
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getId(), is(notNullValue()));
        assertThat(responseEntity.getBody().getName(), is(productDTO.getName()));
        assertThat(responseEntity.getBody().getPrice(), is(productDTO.getPrice()));
        assertThat(responseEntity.getBody().getCount(), is("0"));
        assertThat(responseEntity.getBody().getCategoriesName(), is(nullValue()));
        assertThat(responseEntity.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void addProductWithWrongPrice() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        productDTO.setPrice("-200");
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors(), is(notNullValue()));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors().get(0).getMessage(), is("Цена товара не может быть <=0"));
    }

    @Test
    public void addProductWithWrongAdminId() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, "JAVASESSIONID=0");
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors(), is(notNullValue()));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors().get(0).getMessage(), is("Нет доступа"));
    }
    @Test
    public void addProductWithCategory() {
        HttpEntity<CategoryDTO> entity_category = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_category, CategoryDTO.class);
        assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> entity_product = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        List<Integer> for_product = new ArrayList<>();
        for_product.add(Integer.parseInt(Objects.requireNonNull(responseEntity1.getBody()).getId()));
        productDTO.setCategories(for_product);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity_product, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getId(), is(notNullValue()));
        assertThat(responseEntity.getBody().getName(), is(productDTO.getName()));
        assertThat(responseEntity.getBody().getPrice(), is(productDTO.getPrice()));
        assertThat(responseEntity.getBody().getCount(), is(productDTO.getCount()));
        assertThat(responseEntity.getBody().getCategoriesName(), is(notNullValue()));
        assertThat(responseEntity.getBody().getCategoriesName().get(0), is (categoryDTO.getName()));
        assertThat(responseEntity.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void editProductWithCategory() {
        HttpEntity<CategoryDTO> entity_category = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_category, CategoryDTO.class);
        assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> entity_product = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        List<Integer> for_product = new ArrayList<>();
        for_product.add(Integer.parseInt(Objects.requireNonNull(responseEntity1.getBody()).getId()));
        productDTO.setCategories(for_product);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity_product, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", Objects.requireNonNull(responseEntity.getBody()).getId());
        productDTO.setName("Новое имя товара");
        productDTO.setCount("100");
        productDTO.setPrice(null);
        productDTO.setCategories(null);
        ResponseEntity<ProductDTO> responseEdit = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.PUT, entity_product, ProductDTO.class, params);
        assertThat(responseEdit.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEdit.getBody()).getId(), is(notNullValue()));
        assertThat(Objects.requireNonNull(responseEdit.getBody()).getName(), is(productDTO.getName()));
        assertThat(responseEdit.getBody().getPrice(), is("50"));
        assertThat(responseEdit.getBody().getCount(), is(productDTO.getCount()));
        assertThat(responseEdit.getBody().getCategoriesName(), is(notNullValue()));
        assertThat(responseEdit.getBody().getCategoriesName().get(0), is (categoryDTO.getName()));
        assertThat(responseEdit.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void deleteProduct() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", Objects.requireNonNull(responseEntity.getBody()).getId());
        HttpEntity<ProductDTO> entityDelete = new HttpEntity<ProductDTO>(httpHeaders);
        ResponseEntity<ProductDTO> responseDelete = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.DELETE, entityDelete, ProductDTO.class, params);
        assertThat(responseDelete.getStatusCode(), is(HttpStatus.OK));
        Optional<Product> product  = productRepository.findById(Integer.parseInt(responseEntity.getBody().getId()));
        Assert.assertFalse(product.isPresent());
    }

    @Test
    public void deleteProductWithWrongId() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", "0000001");
        HttpEntity<ProductDTO> entityDelete = new HttpEntity<ProductDTO>(httpHeaders);
        ResponseEntity<ProductDTO> responseDelete = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.DELETE, entityDelete, ProductDTO.class, params);
        assertThat(responseDelete.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseDelete.getBody()).getErrors(), is(notNullValue()));
        assertThat(Objects.requireNonNull(responseDelete.getBody()).getErrors().get(0).getMessage(), is("Нет продукта с данным id"));
    }

    @Test
    public void getProductInfo() {
        HttpEntity<CategoryDTO> entity_category = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_category, CategoryDTO.class);
        assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> entity_product = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        List<Integer> for_product = new ArrayList<>();
        for_product.add(Integer.parseInt(Objects.requireNonNull(responseEntity1.getBody()).getId()));
        productDTO.setCategories(for_product);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity_product, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", Objects.requireNonNull(responseEntity.getBody()).getId());
        HttpEntity<ProductDTO> entityGet = new HttpEntity<ProductDTO>(httpHeaders);
        ResponseEntity<ProductDTO> responseGet = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.GET, entityGet, ProductDTO.class, params);
        assertThat(responseGet.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseGet.getBody()).getId(), is(notNullValue()));
        assertThat(responseGet.getBody().getName(), is(productDTO.getName()));
        assertThat(responseGet.getBody().getPrice(), is(productDTO.getPrice()));
        assertThat(responseGet.getBody().getCount(), is(productDTO.getCount()));
        assertThat(responseGet.getBody().getCategoriesName(), is(notNullValue()));
        assertThat(responseGet.getBody().getCategoriesName().get(0), is (categoryDTO.getName()));
        assertThat(responseGet.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void getProductInfoWithWrongId() {
        HttpEntity<CategoryDTO> entity_category = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_category, CategoryDTO.class);
        assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> entity_product = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        List<Integer> for_product = new ArrayList<>();
        for_product.add(Integer.parseInt(Objects.requireNonNull(responseEntity1.getBody()).getId()));
        productDTO.setCategories(for_product);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity_product, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", "000000001");
        HttpEntity<ProductDTO> entityGet = new HttpEntity<ProductDTO>(httpHeaders);
        ResponseEntity<ProductDTO> responseGet = restTemplate.exchange(urlString + "/products/{id}", HttpMethod.GET, entityGet, ProductDTO.class, params);
        assertThat(responseGet.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseGet.getBody()).getErrors(), is(notNullValue()));
        assertThat(Objects.requireNonNull(responseGet.getBody()).getErrors().get(0).getMessage(), is("Нет продукта с данным id"));
    }

    @Test
    public void getAllProducts() {
        //список продуктов
        ProductDTO productDTO_2 = new ProductDTO();
        productDTO_2.setName("Продукт2_тестовой категории");
        productDTO_2.setPrice("20");
        productDTO_2.setCount("10");
        ProductDTO productDTO_3 = new ProductDTO();
        productDTO_3.setName("Продукт3_тестовой категории");
        productDTO_3.setPrice("100");
        productDTO_3.setCount("20");
        ProductDTO productDTO_4 = new ProductDTO();
        productDTO_4.setName("Продукт4_тестовой категории");
        productDTO_4.setPrice("200");
        productDTO_4.setCount("5");
        ProductDTO productDTO_5 = new ProductDTO();
        productDTO_5.setName("Продукт5_тестовой категории");
        productDTO_5.setPrice("20");
        productDTO_5.setCount("10");
        //список категорий
        CategoryDTO categoryDTO_2 = new CategoryDTO();
        categoryDTO_2.setName("Тестовая категория_2");
        CategoryDTO categoryDTO_3 = new CategoryDTO();
        categoryDTO_3.setName("Тестовая категория_3");
        //Сохраняем категории и получаем ID
        HttpEntity<CategoryDTO> entity_k1 = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> response_k1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_k1, CategoryDTO.class);
        assertThat(response_k1.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<CategoryDTO> entity_k2 = new HttpEntity<CategoryDTO>(categoryDTO_2, httpHeaders);
        ResponseEntity<CategoryDTO> response_k2 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_k2, CategoryDTO.class);
        assertThat(response_k2.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<CategoryDTO> entity_k3 = new HttpEntity<CategoryDTO>(categoryDTO_3, httpHeaders);
        ResponseEntity<CategoryDTO> response_k3 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_k3, CategoryDTO.class);
        assertThat(response_k3.getStatusCode(), is(HttpStatus.OK));
        String k1_id = Objects.requireNonNull(response_k1.getBody()).getId();
        String k2_id = Objects.requireNonNull(response_k2.getBody()).getId();
        String k3_id = Objects.requireNonNull(response_k3.getBody()).getId();
        //назначаем: п1 - без категорий, п2 - к1, п3 - к1 и 2, п4 - К3, П5 - К1 и 3
        List<Integer> for_Prod_2 = new ArrayList<>();
        for_Prod_2.add(Integer.parseInt(k1_id));
        productDTO_2.setCategories(for_Prod_2);
        List<Integer> for_Prod_3 = new ArrayList<>();
        for_Prod_3.add(Integer.parseInt(k1_id));
        for_Prod_3.add(Integer.parseInt(k2_id));
        productDTO_3.setCategories(for_Prod_3);
        List<Integer> for_Prod_4 = new ArrayList<>();
        for_Prod_4.add(Integer.parseInt(k3_id));
        productDTO_4.setCategories(for_Prod_4);
        List<Integer> for_Prod_5 = new ArrayList<>();
        for_Prod_5.add(Integer.parseInt(k1_id));
        for_Prod_5.add(Integer.parseInt(k3_id));
        productDTO_5.setCategories(for_Prod_5);
        //сохраняем продкуты в базу
        HttpEntity<ProductDTO> product_1 = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        ResponseEntity<ProductDTO> response1 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, product_1, ProductDTO.class);
        assertThat(response1.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> product_2 = new HttpEntity<ProductDTO>(productDTO_2, httpHeaders);
        ResponseEntity<ProductDTO> response2 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, product_2, ProductDTO.class);
        assertThat(response2.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> product_3 = new HttpEntity<ProductDTO>(productDTO_3, httpHeaders);
        ResponseEntity<ProductDTO> response3 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, product_3, ProductDTO.class);
        assertThat(response3.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> product_4 = new HttpEntity<ProductDTO>(productDTO_4, httpHeaders);
        ResponseEntity<ProductDTO> response4 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, product_4, ProductDTO.class);
        assertThat(response4.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<ProductDTO> product_5 = new HttpEntity<ProductDTO>(productDTO_5, httpHeaders);
        ResponseEntity<ProductDTO> response5 = restTemplate.exchange(urlString + "/products", HttpMethod.POST, product_5, ProductDTO.class);
        assertThat(response5.getStatusCode(), is(HttpStatus.OK));
        //Достаем продукты без параметров
        HttpEntity<CategoryDTO> entityGet = new HttpEntity<CategoryDTO>(httpHeaders);
        ResponseEntity<List<ProductDTO>> responseWithNoParams = restTemplate.exchange(urlString + "/products", HttpMethod.GET,
                entityGet, new ParameterizedTypeReference<List<ProductDTO>>() {});
        assertThat(responseWithNoParams.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseWithNoParams.getBody().size(), is(5));
        assertThat(responseWithNoParams.getBody().get(0).getName(), is(productDTO.getName()));
        assertThat(responseWithNoParams.getBody().get(1).getName(), is(productDTO_2.getName()));
        assertThat(responseWithNoParams.getBody().get(2).getName(), is(productDTO_3.getName()));
        assertThat(responseWithNoParams.getBody().get(3).getName(), is(productDTO_4.getName()));
        assertThat(responseWithNoParams.getBody().get(4).getName(), is(productDTO_5.getName()));
        //Достаем с параметром = категории (только товары с первой категорией, остортированные по имени товара)
        ResponseEntity<List<ProductDTO>> responseListWithParamCategoryAndOrder = restTemplate.exchange(urlString + "/products?categories={categories}", HttpMethod.GET,
                entityGet, new ParameterizedTypeReference<List<ProductDTO>>() {}, k1_id);
        assertThat(responseListWithParamCategoryAndOrder.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseListWithParamCategoryAndOrder.getBody().size(), is(3));
        assertThat(responseListWithParamCategoryAndOrder.getBody().get(0).getName(), is(productDTO_2.getName()));
        assertThat(responseListWithParamCategoryAndOrder.getBody().get(1).getName(), is(productDTO_3.getName()));
        assertThat(responseListWithParamCategoryAndOrder.getBody().get(2).getName(), is(productDTO_5.getName()));
        //Достаем с параметром = категории (только товары с первой категорией, остортированные по имени категорий)
        ResponseEntity<List<ProductDTO>> responseListWithParamCategoryAndOrderCategory = restTemplate.exchange(urlString + "/products?order={order}&categories={categories}",
                HttpMethod.GET, entityGet, new ParameterizedTypeReference<List<ProductDTO>>() {}, "category", k1_id);
        assertThat(responseListWithParamCategoryAndOrderCategory.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseListWithParamCategoryAndOrderCategory.getBody().size(), is(5));
        assertThat(responseListWithParamCategoryAndOrderCategory.getBody().get(0).getName(), is(productDTO_2.getName()));
        assertThat(responseListWithParamCategoryAndOrderCategory.getBody().get(1).getName(), is(productDTO_3.getName()));
        assertThat(responseListWithParamCategoryAndOrderCategory.getBody().get(2).getName(), is(productDTO_5.getName()));
        assertThat(responseListWithParamCategoryAndOrderCategory.getBody().get(3).getName(), is(productDTO_3.getName()));
        assertThat(responseListWithParamCategoryAndOrderCategory.getBody().get(4).getName(), is(productDTO_5.getName()));
        //Достаем с параметром = категории (все товары, остортированные по имени категорий)
        ResponseEntity<List<ProductDTO>> responseListWithOrderCategory = restTemplate.exchange(urlString + "/products?order={order}",
                HttpMethod.GET, entityGet, new ParameterizedTypeReference<List<ProductDTO>>() {}, "category");
        assertThat(responseListWithOrderCategory.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseListWithOrderCategory.getBody().size(), is(7));
        assertThat(responseListWithOrderCategory.getBody().get(0).getName(), is(productDTO.getName()));
        assertThat(responseListWithOrderCategory.getBody().get(1).getName(), is(productDTO_2.getName()));
        assertThat(responseListWithOrderCategory.getBody().get(2).getName(), is(productDTO_3.getName()));
        assertThat(responseListWithOrderCategory.getBody().get(3).getName(), is(productDTO_5.getName()));
        assertThat(responseListWithOrderCategory.getBody().get(4).getName(), is(productDTO_3.getName()));
        assertThat(responseListWithOrderCategory.getBody().get(5).getName(), is(productDTO_4.getName()));
        assertThat(responseListWithOrderCategory.getBody().get(6).getName(), is(productDTO_5.getName()));
    }

    @Test
    public void buyProduct() {
        HttpEntity<ProductDTO> entity = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        ResponseEntity<ProductDTO> responseEntity = restTemplate.exchange(urlString + "/products", HttpMethod.POST, entity, ProductDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getCount(), is("10"));
        UserDTO client = new UserDTO();
        client.setFirstName("Роман");
        client.setLastName("Коваценко");
        client.setLogin("логин7894564");
        client.setPassword("выаыва7865445");
        client.setAddress("аывпвавпп");
        client.setEmail("Kovatsenko@gmail.com");
        client.setPhone("+79873355010");
        ResponseEntity<UserDTO> responseClient = restTemplate.postForEntity(urlString + "/clients", client, UserDTO.class);
        assertThat(responseClient.getStatusCode(), is(HttpStatus.OK));
        httpHeaders = responseClient.getHeaders();
        String set_cookie = httpHeaders.getFirst(HttpHeaders.SET_COOKIE);
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, set_cookie);
        UserDTO cash = new UserDTO();
        cash.setDeposit("500");
        HttpEntity<UserDTO> entityCash = new HttpEntity<UserDTO>(cash, httpHeaders);
        ResponseEntity<UserDTO> responseCash = restTemplate.exchange(urlString + "/deposits", HttpMethod.PUT, entityCash, UserDTO.class);
        assertThat(responseCash.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseCash.getBody().getDeposit(), is("500"));
        productDTO.setCount("5");
        productDTO.setId(responseEntity.getBody().getId());
        HttpEntity<ProductDTO> entityNew = new HttpEntity<ProductDTO>(productDTO, httpHeaders);
        ResponseEntity<ProductDTO> responseBuy = restTemplate.exchange(urlString + "/purchases", HttpMethod.POST, entityNew, ProductDTO.class);
        assertThat(responseBuy.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseBuy.getBody().getCount(), is("5"));
    }

}
