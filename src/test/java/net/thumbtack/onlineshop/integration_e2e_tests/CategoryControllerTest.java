package net.thumbtack.onlineshop.integration_e2e_tests;

import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.model.Category;
import net.thumbtack.onlineshop.data.repository.CategoryRepository;
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
public class CategoryControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TestRestTemplate restTemplate;
    private CategoryDTO categoryDTO = new CategoryDTO();
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
    public void addCategory() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getId(), is(notNullValue()));
        assertThat(responseEntity.getBody().getName(), is(categoryDTO.getName()));
        assertThat(responseEntity.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void addCategoryWithWrongParam() {
        categoryDTO.setName("");
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getErrors().get(0).getMessage(), is("Нельзя создать категорию с пустым именем"));
    }

    @Test
    public void addCategoryTwice() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        ResponseEntity<CategoryDTO> responseWrong = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseWrong.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseWrong.getBody()).getErrors(), is(notNullValue()));
        assertThat(responseWrong.getBody().getErrors().get(0).getMessage(), is("Такая категория уже есть"));
    }

    @Test
    public void addCategoryWithParent() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("подкатегория");
        categoryDTO.setParentId(Objects.requireNonNull(responseEntity.getBody()).getId());
        ResponseEntity<CategoryDTO> responseSubCategory = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseSubCategory.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseSubCategory.getBody()).getName(), is(categoryDTO.getName()));
        assertThat(responseSubCategory.getBody().getErrors(), is(nullValue()));
        assertThat(responseSubCategory.getBody().getParentName(), is("Тестовая категория_1"));
    }

    @Test
    public void addCategoryWithWrongParent() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("родительская категория");
        categoryDTO.setParentId("11");
        ResponseEntity<CategoryDTO> responseSubCategory = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseSubCategory.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseSubCategory.getBody()).getErrors(), is(notNullValue()));
        assertThat(responseSubCategory.getBody().getErrors().get(0).getMessage(), is("Нет такой родительской категории"));
    }

    @Test
    public void getCategory() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getId(), is(notNullValue()));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", responseEntity.getBody().getId());
        ResponseEntity<CategoryDTO> responseGet = restTemplate.exchange(urlString + "/categories/{id}", HttpMethod.GET, entity, CategoryDTO.class, params);
        assertThat(responseGet.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseGet.getBody()).getId(), is(notNullValue()));
        assertThat(responseGet.getBody().getName(), is(categoryDTO.getName()));
        assertThat(responseGet.getBody().getErrors(), is(nullValue()));
    }

    @Test
    public void getCategoryWithWrongId() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getId(), is(notNullValue()));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", "777");
        ResponseEntity<CategoryDTO> responseGet = restTemplate.exchange(urlString + "/categories/{id}", HttpMethod.GET, entity, CategoryDTO.class, params);
        assertThat(responseGet.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseGet.getBody()).getErrors(), is(notNullValue()));
        assertThat(responseGet.getBody().getErrors().get(0).getMessage(), is(  "По данному id записей нет"));
    }

    @Test
    public void editCategoryWithParent() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("категория2");
        ResponseEntity<CategoryDTO> responseEntity2 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity2.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("подкатегория");
        categoryDTO.setParentId(Objects.requireNonNull(responseEntity1.getBody()).getId());
        ResponseEntity<CategoryDTO> responseSubEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseSubEntity.getStatusCode(), is(HttpStatus.OK));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", Objects.requireNonNull(responseSubEntity.getBody()).getId());
        categoryDTO.setName("измененная подкатегория");
        categoryDTO.setParentId(Objects.requireNonNull(responseEntity2.getBody()).getId());
        ResponseEntity<CategoryDTO> responseEdit = restTemplate.exchange(urlString + "/categories/{id}", HttpMethod.PUT, entity, CategoryDTO.class, params);
        assertThat(responseEdit.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEdit.getBody()).getName(), is("измененная подкатегория"));
        assertThat(responseEdit.getBody().getParentName(), is("категория2"));
        categoryDTO.setName("измененная подкатегория2");
        categoryDTO.setParentId(null);
        ResponseEntity<CategoryDTO> responseEdit_2 = restTemplate.exchange(urlString + "/categories/{id}", HttpMethod.PUT, entity, CategoryDTO.class, params);
        assertThat(responseEdit_2.getStatusCode(), is(HttpStatus.OK));
        assertThat(Objects.requireNonNull(responseEdit_2.getBody()).getName(), is("измененная подкатегория2"));
        assertThat(responseEdit_2.getBody().getParentName(), is(nullValue()));
    }

    @Test
    public void wrongEditCategoryWithParent() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("подкатегория1");
        categoryDTO.setParentId(Objects.requireNonNull(responseEntity.getBody()).getId());
        ResponseEntity<CategoryDTO> responseSub1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseSub1.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("подкатегория2");
        categoryDTO.setParentId(Objects.requireNonNull(responseEntity.getBody()).getId());
        ResponseEntity<CategoryDTO> responseSub2 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseSub2.getStatusCode(), is(HttpStatus.OK));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", Objects.requireNonNull(responseSub1.getBody()).getId());
        categoryDTO.setName("измененная подкатегория");
        categoryDTO.setParentId(Objects.requireNonNull(responseSub2.getBody()).getId());
        ResponseEntity<CategoryDTO> responseEdit = restTemplate.exchange(urlString + "/categories/{id}", HttpMethod.PUT, entity, CategoryDTO.class, params);
        assertThat(responseEdit.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(Objects.requireNonNull(responseEdit.getBody()).getErrors(), is(notNullValue()));
        assertThat(responseEdit.getBody().getErrors().get(0).getMessage(), is("Подкатегория не может стать категорией"));
    }

    @Test
    public void deleteCategories() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("подкатегория1");
        categoryDTO.setParentId(Objects.requireNonNull(responseEntity.getBody()).getId());
        ResponseEntity<CategoryDTO> responseSub1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseSub1.getStatusCode(), is(HttpStatus.OK));
        categoryDTO.setName("подкатегория2");
        categoryDTO.setParentId(Objects.requireNonNull(responseEntity.getBody()).getId());
        ResponseEntity<CategoryDTO> responseSub2 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseSub2.getStatusCode(), is(HttpStatus.OK));
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", Objects.requireNonNull(responseEntity.getBody()).getId());
        ResponseEntity<CategoryDTO> responseEdit = restTemplate.exchange(urlString + "/categories/{id}", HttpMethod.DELETE, entity, CategoryDTO.class, params);
        assertThat(responseSub2.getStatusCode(), is(HttpStatus.OK));
        Optional<Category> categorySub_1 = categoryRepository.findById(Integer.valueOf(Objects.requireNonNull(responseSub1.getBody()).getId()));
        Optional<Category> categorySub_2 = categoryRepository.findById(Integer.valueOf(Objects.requireNonNull(responseSub2.getBody()).getId()));
        Assert.assertFalse(categorySub_1.isPresent());
        Assert.assertFalse(categorySub_2.isPresent());
    }

    @Test
    public void getAllCategories() {
        HttpEntity<CategoryDTO> entity = new HttpEntity<CategoryDTO>(categoryDTO, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity, CategoryDTO.class);
        assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));
        CategoryDTO categoryDTO2 = new CategoryDTO();
        categoryDTO2.setName("aaaaaaaa");
        HttpEntity<CategoryDTO> entity2 = new HttpEntity<CategoryDTO>(categoryDTO2, httpHeaders);
        ResponseEntity<CategoryDTO> responseEntity2 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity2, CategoryDTO.class);
        assertThat(responseEntity2.getStatusCode(), is(HttpStatus.OK));
        CategoryDTO categoryDTO_sub1 = new CategoryDTO();
        categoryDTO_sub1.setName("подкатегория1");
        categoryDTO_sub1.setParentId(Objects.requireNonNull(responseEntity1.getBody()).getId());
        HttpEntity<CategoryDTO> entity_sub1 = new HttpEntity<CategoryDTO>(categoryDTO_sub1, httpHeaders);
        ResponseEntity<CategoryDTO> responseSub1 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_sub1, CategoryDTO.class);
        assertThat(responseSub1.getStatusCode(), is(HttpStatus.OK));
        CategoryDTO categoryDTO_sub2 = new CategoryDTO();
        categoryDTO_sub2.setName("подкатегория2");
        categoryDTO_sub2.setParentId(Objects.requireNonNull(responseEntity1.getBody()).getId());
        HttpEntity<CategoryDTO> entity_sub2 = new HttpEntity<CategoryDTO>(categoryDTO_sub2, httpHeaders);
        ResponseEntity<CategoryDTO> responseSub2 = restTemplate.exchange(urlString + "/categories", HttpMethod.POST, entity_sub2, CategoryDTO.class);
        assertThat(responseSub2.getStatusCode(), is(HttpStatus.OK));
        HttpEntity<List<CategoryDTO>> entity_get = new HttpEntity<List<CategoryDTO>>(httpHeaders);
        ResponseEntity<List<CategoryDTO>> responseGet = restTemplate.exchange(urlString + "/categories", HttpMethod.GET, entity_get, new ParameterizedTypeReference<List<CategoryDTO>>(){});
        assertThat(responseGet.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseGet.getBody().get(0), is(notNullValue()));
        categoryDTO.setId(responseEntity1.getBody().getId());
        categoryDTO2.setId(responseEntity2.getBody().getId());
        categoryDTO_sub1.setId(responseSub1.getBody().getId());
        categoryDTO_sub1.setParentName(responseEntity1.getBody().getName());
        categoryDTO_sub2.setId(responseSub2.getBody().getId());
        categoryDTO_sub2.setParentName(responseEntity1.getBody().getName());
        assertThat(responseGet.getBody().get(0), is(categoryDTO2));
        assertThat(responseGet.getBody().get(1), is(categoryDTO));
        assertThat(responseGet.getBody().get(2), is(categoryDTO_sub1));
        assertThat(responseGet.getBody().get(3), is(categoryDTO_sub2));
    }
}
