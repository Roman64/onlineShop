package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.services.ValidateProductService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ValidateProductServiceImplTest {

    private ValidateProductServiceImpl validateProductService;
    private ProductDTO productDTO;

    @Before
    public void before() {
        validateProductService = new ValidateProductServiceImpl();
        productDTO = new ProductDTO();
        productDTO.setName("Продукт");
        productDTO.setPrice("50");
        productDTO.setCount("100");
    }

    @Test
    public void validProductTest() {
        validateProductService.validateProduct(productDTO);
        Assert.assertNull(productDTO.getErrors());
    }

    @Test
    public void notValidPriceProductTest() {
        productDTO.setPrice("fdgjkhfdsk");
        validateProductService.validateProduct(productDTO);
        Assert.assertEquals(productDTO.getErrors().get(0).getMessage(),"В запросе цена должна быть указана цифрами");
    }

    @Test
    public void notValidCountProductTest() {
        productDTO.setCount("fdgjkhfdsk");
        validateProductService.validateProduct(productDTO);
        Assert.assertEquals(productDTO.getErrors().get(0).getMessage(),"В запросе кол-во товара должно быть указано цифрами");
    }
}
