package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientRepositoryTest {

    private String firstName = "Roman";
    private String lastName = "Kovatsenko";
    private String login = "Raven90";
    private String pass = "450000";
    private String phone = "89873355010";
    private String email = "Kovatsenko@gmail.com";
    private Integer deposit = 0;
    private String address = "Electronnaya";

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CartWithProductsRepository cartWithProductsRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @Before
    public void beforeTest(){
        Client client = new Client();
        client.setAddress(address);
        client.setDeposit(deposit);
        client.setEmail(email);
        client.setPhone(phone);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setLogin(login);
        client.setPassword(pass);
        clientRepository.save(client);
    }

    @After
    public void afterTest() {
        categoryRepository.deleteAll();
        cartWithProductsRepository.deleteAll();
        purchaseRepository.deleteAll();
        clientRepository.deleteAll();
        productRepository.deleteAll();
    }


    @Test
    public void addNewClient() {
        Client client = new Client();
        client.setAddress("Electronnaya");
        client.setDeposit(0);
        client.setEmail("Kovatsenko@gmail.com");
        client.setPhone("89873355010");
        client.setFirstName("Roman");
        client.setLastName("Kovatsenko");
        client.setLogin("Raven90");
        client.setPassword("777777");
        Client expected = clientRepository.save(client);
        Assert.assertEquals(client, expected);
    }

    @Test
    public void getClient() {
        Client client = clientRepository.getClientByLoginAndPassword(login, pass);
        Assert.assertEquals(client.getFirstName(), firstName);
        Assert.assertEquals(client.getLastName(), lastName);
        Assert.assertEquals(client.getLogin(), login);
        Assert.assertEquals(client.getPassword(), pass);
        Assert.assertEquals(java.util.Optional.ofNullable(client.getDeposit()), Optional.of(0));
        Assert.assertEquals(client.getAddress(), address);
        Assert.assertEquals(client.getPhone(), phone);
        Assert.assertEquals(client.getEmail(), email);
        Assert.assertEquals(client.getPatronymic(), null);
    }

    @Test
    public void addClientWithCartProducts() {
        Client client = new Client();
        client.setAddress(RandomStringUtils.random(10));
        client.setDeposit(50);
        client.setEmail("Kovatsenko@gmail.com");
        client.setPhone(RandomStringUtils.random(10));
        String firstName = "Elena";
        client.setFirstName(firstName);
        client.setLastName(RandomStringUtils.random(10));
        client.setLogin(RandomStringUtils.random(10));
        client.setPassword(RandomStringUtils.random(10));

        Category category = new Category();
        category.setName("Категория_тест");
        categoryRepository.save(category);
        List<Category> categories = new ArrayList<>();
        categories.add(category);

        Product product = new Product();
        product.setAmount(10);
        product.setName("КРУТЫЕ БОТИНКИ");
        product.setPrice(2);
        product.setCategories(categories);
        productRepository.save(product);

        CartWithProducts cartProduct = new CartWithProducts();
        client.setCartWithProducts(cartProduct);
        Purchase purchase = new Purchase();
        purchase.setProduct(product);
        purchase.setQuantity(2);
        purchase.setCartWithProducts(cartProduct);
        cartProduct.setPurchase(new ArrayList<>());
        cartProduct.getPurchase().add(purchase);

        Client expected = clientRepository.save(client);
        Assert.assertEquals(client, expected);
        Client clientFromDB = clientRepository.getClientByFirstName("Elena");
        Assert.assertNotNull(clientFromDB);
        Assert.assertNotNull(clientFromDB.getCartWithProducts().getPurchase());
        Assert.assertEquals(clientFromDB.getCartWithProducts().getPurchase().get(0), purchase);
    }
}

