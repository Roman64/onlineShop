package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.CartWithProductsDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.model.*;
import net.thumbtack.onlineshop.data.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CartWithProductsServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartWithProductsRepository cartWithProductsRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private SoldProductsRepository soldProductsRepository;
    @Mock
    private ClientServiceImpl clientService;
    @Mock
    private AdminServiceImpl adminService;
    @InjectMocks
    private CartWithProductsServiceImpl cartWithProductsService;

    private ProductDTO productDTO;
    private Product product;
    private Client client;
    private String session = "123";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        productDTO = new ProductDTO();
        product = new Product();
        productDTO.setName("Продукт");
        productDTO.setPrice("50");
        productDTO.setCount("10");
        productDTO.setId("10");
        product.setName(productDTO.getName());
        product.setPrice(Integer.parseInt(productDTO.getPrice()));
        product.setAmount(Integer.parseInt(productDTO.getCount()));
        product.setId(10);
        client = new Client();
        client.setId(1);
        client.setLogin("Логин123");
        client.setPassword("Пароль777");
        client.setLastName("Коваценко");
        client.setFirstName("Роман");
        client.setDeposit(0);
        client.setPhone("+79873355010");
        client.setEmail("Kovatsenko@gmail.com");
        client.setAddress("Засратов");
    }

    @Test
    public void addProductToBasketTest() {
        HashMap<String, Client> sessions = new HashMap<>();
        sessions.put(session, client);
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        when(clientService.getClientSessions()).thenReturn(sessions);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        CartWithProductsDTO result;
        result = cartWithProductsService.addProductToBasket(session, productDTO);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getRemaining().size(), 1);
        Assert.assertEquals(result.getRemaining().get(0).getId(), String.valueOf(10));
        Assert.assertEquals(result.getRemaining().get(0).getName(), product.getName());
        Assert.assertEquals(result.getRemaining().get(0).getPrice(), productDTO.getPrice());
        Assert.assertEquals(result.getRemaining().get(0).getCount(), productDTO.getCount());
        Assert.assertEquals(client.getCartWithProducts().getPurchase().get(0).getProduct().getName(), product.getName());
        Assert.assertEquals(client.getCartWithProducts().getPurchase().get(0).getQuantity().toString(), productDTO.getCount());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    public void addProductWithWrongLoginParamsToBasketTest() {
        when(clientService.isLogin(session)).thenReturn(false);
        CartWithProductsDTO result;
        result = cartWithProductsService.addProductToBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void addProductWithWrongIdToBasketTest() {
        productDTO.setId("sdfjhskfh");
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        CartWithProductsDTO result;
        result = cartWithProductsService.addProductToBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Неверный формат ID");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void addWrongProductToBasketTest() {
        Optional<Product> optional = Optional.empty();
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        CartWithProductsDTO result;
        result = cartWithProductsService.addProductToBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Товара по данному id не найдено");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void addProductWithWrongNameToBasketTest() {
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        CartWithProductsDTO result;
        productDTO.setName("Продукт Неопознанный");
        result = cartWithProductsService.addProductToBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Названия товаров не совпадают");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void addProductWithWrongPriceToBasketTest() {
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        CartWithProductsDTO result;
        productDTO.setPrice("350");
        result = cartWithProductsService.addProductToBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Цены товаров не совпадают");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void deleteProductFromBasketTest() {
        HashMap<String, Client> sessions = new HashMap<>();
        sessions.put(session, client);
        CartWithProducts cartWithProducts = new CartWithProducts();
        cartWithProducts.setPurchase(new ArrayList<>());
        cartWithProducts.getPurchase().add(new Purchase());
        cartWithProducts.getPurchase().get(0).setQuantity(10);
        cartWithProducts.getPurchase().get(0).setProduct(product);
        client.setCartWithProducts(cartWithProducts);
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        when(clientService.getClientSessions()).thenReturn(sessions);
        when(cartWithProductsRepository.getOne(any())).thenReturn(cartWithProducts);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        CartWithProductsDTO result;
        result = cartWithProductsService.deleteProductFromBasket(session, product.getId());
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertTrue(client.getCartWithProducts().getPurchase().isEmpty());
        verify(purchaseRepository, times(1)).delete(any(Purchase.class));
        verify(cartWithProductsRepository, times(1)).save(any(CartWithProducts.class));
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    public void deleteProductWithWrongSessionFromBasketTest() {
        when(clientService.isLogin(session)).thenReturn(false);
        CartWithProductsDTO result;
        result = cartWithProductsService.deleteProductFromBasket(session, product.getId());
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        verifyNoMoreInteractions(clientRepository);
        verifyNoMoreInteractions(cartWithProductsRepository);
        verifyNoMoreInteractions(purchaseRepository);
    }

    @Test
    public void deleteProductWithWrongIdFromBasketTest() {
        Optional<Product> optional = Optional.empty();
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        CartWithProductsDTO result;
        result = cartWithProductsService.deleteProductFromBasket(session, product.getId());
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет товара с таким ID");
        verifyNoMoreInteractions(clientRepository);
        verifyNoMoreInteractions(cartWithProductsRepository);
        verifyNoMoreInteractions(purchaseRepository);
    }

    @Test
    public void editProductWithWrongLoginParamsInBasketTest() {
        when(clientService.isLogin(session)).thenReturn(false);
        CartWithProductsDTO result;
        result = cartWithProductsService.editProductInBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void editProductWithWrongIdInBasketTest() {
        productDTO.setId("sdfjhskfh");
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        CartWithProductsDTO result;
        result = cartWithProductsService.editProductInBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Неверный формат ID");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void editWrongProductInBasketTest() {
        Optional<Product> optional = Optional.empty();
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        CartWithProductsDTO result;
        result = cartWithProductsService.editProductInBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Товара по данному id не найдено");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void editProductWithWrongNameInBasketTest() {
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        CartWithProductsDTO result;
        productDTO.setName("Продукт Неопознанный");
        result = cartWithProductsService.editProductInBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Названия товаров не совпадают");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void editProductWithWrongPriceInBasketTest() {
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        CartWithProductsDTO result;
        productDTO.setPrice("350");
        result = cartWithProductsService.editProductInBasket(session, productDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Цены товаров не совпадают");
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void editProductInBasketTest() {
        HashMap<String, Client> sessions = new HashMap<>();
        sessions.put(session, client);
        CartWithProducts cartWithProducts = new CartWithProducts();
        cartWithProducts.setPurchase(new ArrayList<>());
        cartWithProducts.getPurchase().add(new Purchase());
        cartWithProducts.getPurchase().get(0).setQuantity(10);
        cartWithProducts.getPurchase().get(0).setProduct(product);
        client.setCartWithProducts(cartWithProducts);
        Optional<Product> optional = Optional.of(product);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(anyInt())).thenReturn(optional);
        when(clientService.getClientSessions()).thenReturn(sessions);
        CartWithProductsDTO result;
        productDTO.setCount("100");
        result = cartWithProductsService.editProductInBasket(session, productDTO);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getRemaining().size(), 1);
        Assert.assertEquals(result.getRemaining().get(0).getId(), String.valueOf(10));
        Assert.assertEquals(result.getRemaining().get(0).getName(), product.getName());
        Assert.assertEquals(result.getRemaining().get(0).getPrice(), productDTO.getPrice());
        Assert.assertEquals(result.getRemaining().get(0).getCount(), productDTO.getCount());
        Assert.assertEquals(client.getCartWithProducts().getPurchase().get(0).getProduct().getName(), product.getName());
        Assert.assertEquals(client.getCartWithProducts().getPurchase().get(0).getQuantity().toString(), productDTO.getCount());
        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    @Test
    public void buyProductsWithWrongSessionFromBasketTest() {
        when(clientService.isLogin(session)).thenReturn(false);
        CartWithProductsDTO result;
        result = cartWithProductsService.buyProductsFromBasket(session, new ArrayList<>());
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        verifyNoMoreInteractions(clientRepository);
        verifyNoMoreInteractions(cartWithProductsRepository);
        verifyNoMoreInteractions(purchaseRepository);
    }

    @Test
    public void getBasketWithWrongSessionTest() {
        CartWithProductsDTO result;
        result = cartWithProductsService.getBasket(session);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        verifyNoMoreInteractions(clientRepository);
        verifyNoMoreInteractions(cartWithProductsRepository);
        verifyNoMoreInteractions(purchaseRepository);
    }

    @Test
    public void buyProductsFromBasketWithNoCashTest() {
        HashMap<String, Client> sessions = new HashMap<>();
        sessions.put(session, client);
        CartWithProducts cartWithProducts = new CartWithProducts();
        cartWithProducts.setPurchase(new ArrayList<>());
        cartWithProducts.getPurchase().add(new Purchase());
        cartWithProducts.getPurchase().get(0).setQuantity(10);
        cartWithProducts.getPurchase().get(0).setProduct(product);
        client.setCartWithProducts(cartWithProducts);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.existsById(anyInt())).thenReturn(true);
        when(clientService.getClientSessions()).thenReturn(sessions);
        CartWithProductsDTO result;
        List<ProductDTO> buyList = new ArrayList<>();
        buyList.add(productDTO);
        result = cartWithProductsService.buyProductsFromBasket(session, buyList);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Не хватает денег на счету");
        verifyNoMoreInteractions(clientRepository);
        verifyNoMoreInteractions(purchaseRepository);
        verifyNoMoreInteractions(cartWithProductsRepository);
    }

    @Test
    public void buyProductsFromBasketTest() {
        HashMap<String, Client> sessions = new HashMap<>();
        sessions.put(session, client);
        CartWithProducts cartWithProducts = new CartWithProducts();
        cartWithProducts.setPurchase(new ArrayList<>());
        cartWithProducts.getPurchase().add(new Purchase());
        cartWithProducts.getPurchase().get(0).setQuantity(20);
        cartWithProducts.getPurchase().get(0).setProduct(product);
        client.setDeposit(1000);
        client.setCartWithProducts(cartWithProducts);
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.existsById(anyInt())).thenReturn(true);
        when(productRepository.getOne(anyInt())).thenReturn(product);
        when(clientService.getClientSessions()).thenReturn(sessions);
        when(cartWithProductsRepository.getOne(any())).thenReturn(cartWithProducts);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        CartWithProductsDTO result;
        List<ProductDTO> buyList = new ArrayList<>();
        buyList.add(productDTO);
        result = cartWithProductsService.buyProductsFromBasket(session, buyList);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getBought().size(),1);
        Assert.assertEquals(result.getBought().get(0).getCount(),"10");
        Assert.assertEquals(result.getRemaining().size(),1);
        Assert.assertEquals(result.getRemaining().get(0).getCount(),"10");
        Assert.assertEquals(client.getCartWithProducts().getPurchase().get(0).getQuantity().toString(), "10");
        Assert.assertEquals(client.getDeposit().toString(), "500");
        verify(purchaseRepository, times(1)).delete(any(Purchase.class));
        verify(cartWithProductsRepository, times(1)).save(any(CartWithProducts.class));
        verify(clientRepository, times(1)).save(any(Client.class));
    }
}
