package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.SoldProductsDTO;
import net.thumbtack.onlineshop.data.model.Product;
import net.thumbtack.onlineshop.data.repository.ClientRepository;
import net.thumbtack.onlineshop.data.repository.ProductRepository;
import net.thumbtack.onlineshop.data.repository.SoldProductsRepository;
import net.thumbtack.onlineshop.services.AdminService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoldProductsServiceImplTest {

    @Mock
    private AdminService adminService;
    @Mock
    private SoldProductsRepository soldProductsRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private SoldProductsServiceImpl soldProductsService;

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
    public void getSoldProductsTest() {
        List<SoldProductsDTO> result;
        result = soldProductsService.getSoldProducts(session, null, null, null, null);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        result = soldProductsService.getSoldProducts(session, null, null, "2019.10.12", null);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Введите дату в формате yyyy-MM-dd");
        when(soldProductsRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        result = soldProductsService.getSoldProducts(session, null, null, "2019-10-12", "amount");
        Assert.assertEquals(result.get(0).getMessageForUser(), "Итого сделано покупок: 0, на сумму: 0");
        Assert.assertNull(result.get(0).getId());
        Assert.assertNull(result.get(0).getErrors());
    }

    @Test
    public void getSoldProductsByClientTest() {
        List<SoldProductsDTO> result;
        result = soldProductsService.getSoldProductsByClient(session, 11);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        when(soldProductsRepository.findAllByClientId(anyInt(), any(Pageable.class))).thenReturn(Page.empty());
        result = soldProductsService.getSoldProductsByClient(session, 11);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Нет товаров с таким id клиента");
    }

    @Test
    public void getSoldProductsBetweenDateTest() {
        List<SoldProductsDTO> result;
        result = soldProductsService.getSoldProductsBetweenDate(session, null, null);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        result = soldProductsService.getSoldProductsBetweenDate(session, null, null);
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Введите дату в формате yyyy-MM-dd");
        when(soldProductsRepository.findByDateBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        result = soldProductsService.getSoldProductsBetweenDate(session, "2019-12-10", "2019-12-10");
        Assert.assertEquals(result.get(0).getErrors().size(), 1);
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "По таким датам заказы не найдены");
    }
}
