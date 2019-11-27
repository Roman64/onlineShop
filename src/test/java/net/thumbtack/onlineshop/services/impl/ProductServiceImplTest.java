package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.model.Product;
import net.thumbtack.onlineshop.data.model.SoldProducts;
import net.thumbtack.onlineshop.data.repository.ClientRepository;
import net.thumbtack.onlineshop.data.repository.ProductRepository;
import net.thumbtack.onlineshop.data.repository.SoldProductsRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceImplTest {
    @Mock
    private AdminServiceImpl adminService;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private SoldProductsRepository soldProductsRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ClientServiceImpl clientService;
    @Captor
    private ArgumentCaptor<Product> productArgumentCaptor;
    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTO productDTO = new ProductDTO();
    private Product product = new Product();
    private String session = "123";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        productDTO.setName("Продукт_1");
        productDTO.setPrice("50");
        productDTO.setCount("10");
        productDTO.setErrors(new ArrayList<>());
        product.setName(productDTO.getName());
        product.setPrice(Integer.parseInt(productDTO.getPrice()));
        product.setAmount(Integer.parseInt(productDTO.getCount()));
        product.setId(10);
        product.setCategories(new ArrayList<>());
    }

    @Test
    public void addProductTest() {
        ProductDTO result;
        result = productService.addProductByAdmin(session, productDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        result = productService.addProductByAdmin(session, productDTO);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getName(), product.getName());
        Assert.assertEquals(result.getPrice(), String.valueOf(product.getPrice()));
        Assert.assertEquals(result.getCount(), String.valueOf(product.getAmount()));
        Assert.assertEquals(result.getId(), String.valueOf(product.getId()));
        Assert.assertEquals(result.getCategoriesName().size(), 0);
        productDTO.setPrice(null);
        result = productService.addProductByAdmin(session, productDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Необходимо указать цену товара");
        productDTO.setPrice("-50");
        result = productService.addProductByAdmin(session, productDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Цена товара не может быть <=0");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void editProductByAdminWithWrongParamsTest() {
        ProductDTO result;
        result = productService.editProductByAdmin(session, productDTO, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        productDTO.setPrice("-50");
        result = productService.editProductByAdmin(session, productDTO, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Цена товара не может быть <=0");
        productDTO.setPrice("50");
        result = productService.editProductByAdmin(session, productDTO, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Товара по данному id не найдено");
    }

    @Test
    public void editProductTest() {
        when(adminService.isLogin(session)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDTO result;
        ProductDTO edit = new ProductDTO();
        edit.setPrice("200");
        edit.setName("Продукт Новый");
        result = productService.addProductByAdmin(session, edit);
        verify(productRepository).save(productArgumentCaptor.capture());
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertNull(productArgumentCaptor.getValue().getId());
        Assert.assertEquals(productArgumentCaptor.getValue().getName(), edit.getName());
        Assert.assertEquals(productArgumentCaptor.getValue().getPrice(), Integer.parseInt(edit.getPrice()));
        edit.setPrice("-200");
        result = productService.addProductByAdmin(session, edit);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Цена товара не может быть <=0");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void deleteProductTest() {
        ProductDTO result;
        result = productService.deleteProductByAdmin(session, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        when(productRepository.existsById(anyInt())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyInt());
        result = productService.deleteProductByAdmin(session, product.getId());
        Assert.assertTrue(result.getErrors().isEmpty());
        verify(productRepository, times(1)).deleteById(anyInt());
        when(productRepository.existsById(anyInt())).thenReturn(false);
        result = productService.deleteProductByAdmin(session, 1234);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет продукта с данным id");
        verify(productRepository, times(1)).deleteById(anyInt());
    }

    @Test
    public void getProductByIdTest() {
        ProductDTO result;
        result = productService.getProductById(session, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        when(productRepository.existsById(anyInt())).thenReturn(false);
        result = productService.getProductById(session, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет продукта с данным id");
        when(productRepository.existsById(anyInt())).thenReturn(true);
        when(productRepository.getOne(anyInt())).thenReturn(product);
        result = productService.getProductById(session, 12);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getId(), String.valueOf(product.getId()));
        Assert.assertEquals(result.getName(), product.getName());
        Assert.assertEquals(result.getPrice(), String.valueOf(product.getPrice()));
        Assert.assertEquals(result.getCount(), String.valueOf(product.getAmount()));
        verify(productRepository, times(0)).save(any(Product.class));
    }

    @Test
    public void getAllProductsTest() {
        List<ProductDTO> result;
        result = productService.getAllProducts(session, null, null);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        result = productService.getAllProducts(session, null, null);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Продуктов не найдено");
        ArrayList<Product> products = new ArrayList<>();
        products.add(product);
        when(productRepository.findAll(any(Sort.class))).thenReturn(products);
        result = productService.getAllProducts(session, null, "продукты");
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Ошибка в названии сортировки");
        verify(productRepository, times(0)).save(any(Product.class));
    }

    @Test
    public void buyProductTest() {
        Optional<Product> optional;
        HashMap<String,Client> hashMap = new HashMap<>();
        optional = Optional.of(product);
        Client client = new Client();
        client.setDeposit(500);
        client.setId(100);
        hashMap.put(session, client);
        ProductDTO productToBuy = new ProductDTO();
        productToBuy.setPrice(productDTO.getPrice());
        productToBuy.setName(productDTO.getName());
        productToBuy.setId(product.getId().toString());
        productToBuy.setCount("8");
        when(clientService.isLogin(session)).thenReturn(true);
        when(productRepository.findById(Integer.parseInt(productToBuy.getId()))).thenReturn(optional);
        when(clientService.getClientSessions()).thenReturn(hashMap);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(soldProductsRepository.save(any(SoldProducts.class))).thenReturn(null);
        ProductDTO result;
        result = productService.buyProductByClient(session, productToBuy);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getId(), String.valueOf(product.getId()));
        Assert.assertEquals(result.getName(), product.getName());
        Assert.assertEquals(result.getCount(), "2");
        Assert.assertEquals(result.getPrice(), productToBuy.getPrice());
        productToBuy.setPrice("123");
        result = productService.buyProductByClient(session, productToBuy);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Данные запроса не совпадают с данными о товаре");
        productToBuy.setPrice(String.valueOf(product.getPrice()));
        productToBuy.setName("dgdgf");
        result = productService.buyProductByClient(session, productToBuy);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Данные запроса не совпадают с данными о товаре");
        verify(productRepository, times(1)).save(any(Product.class));
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(soldProductsRepository, times(1)).save(any(SoldProducts.class));
    }
}
