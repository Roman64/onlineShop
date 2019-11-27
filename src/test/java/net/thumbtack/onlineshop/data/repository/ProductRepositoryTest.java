package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.Product;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductRepositoryTest {

    private String name = "product";
    private int price = 10;
    private int amount = 2;

    @Autowired
    private ProductRepository productRepository;


    @After
    public void afterTest() {
        productRepository.deleteAll();
    }

    @Test
    public void addProduct(){
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setAmount(amount);
        Product expected = productRepository.save(product);
        Assert.assertEquals(product, expected);
    }
}
