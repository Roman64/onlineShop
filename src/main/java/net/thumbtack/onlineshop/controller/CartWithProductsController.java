package net.thumbtack.onlineshop.controller;

import com.wordnik.swagger.annotations.Api;
import net.thumbtack.onlineshop.data.dto.CartWithProductsDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.services.CartWithProductsService;
import net.thumbtack.onlineshop.services.ValidateProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


@RestController
@Api("Cart with products")
@RequestMapping("/api")
public class CartWithProductsController {

    @Autowired
    private CartWithProductsService cartWithProductsService;
    @Autowired
    private ValidateProductService validateProductService;

    @Autowired
    public CartWithProductsController(CartWithProductsService cartWithProductsService, ValidateProductService validateProductService){
        this.cartWithProductsService = cartWithProductsService;
        this.validateProductService = validateProductService;
    }

    @RequestMapping(path="/baskets", method = RequestMethod.POST)
    public ResponseEntity<CartWithProductsDTO> addProductToBasket(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                  @RequestBody ProductDTO productDTO) {
        validateProductService.validateProduct(productDTO);
        CartWithProductsDTO result;
        if (productDTO.getErrors() == null) result = cartWithProductsService.addProductToBasket(session, productDTO);
        else {
            result = new CartWithProductsDTO();
            result.setErrors(productDTO.getErrors());
        }
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/baskets/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<CartWithProductsDTO> deleteProductFromBasket(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                  @PathVariable int id) {
        CartWithProductsDTO result = cartWithProductsService.deleteProductFromBasket(session, id);
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/baskets", method = RequestMethod.PUT)
    public ResponseEntity<CartWithProductsDTO> editProductInBasket(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                  @RequestBody ProductDTO productDTO) {
        validateProductService.validateProduct(productDTO);
        CartWithProductsDTO result;
        if (productDTO.getErrors() == null) result = cartWithProductsService.editProductInBasket(session, productDTO);
        else {
            result = new CartWithProductsDTO();
            result.setErrors(productDTO.getErrors());
        }
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/baskets", method = RequestMethod.GET)
    public ResponseEntity<CartWithProductsDTO> getProductsFromBasket(@CookieValue(name = "JAVASESSIONID", required = true) String session) {
        CartWithProductsDTO result = cartWithProductsService.getBasket(session);
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/purchases/baskets", method = RequestMethod.POST)
    public ResponseEntity<CartWithProductsDTO> buyProductsFromBasket(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                   @RequestBody List<ProductDTO> productDTO) {
        CartWithProductsDTO result = new CartWithProductsDTO();
        result.setErrors(new ArrayList<>());
        for (ProductDTO dto : productDTO) {
            validateProductService.validateProduct(dto);
        }
        boolean isValid = true;
        for (ProductDTO dto : productDTO) {
            if (dto.getErrors() != null) {
                isValid = false;
                result.getErrors().add(dto.getErrors().get(0));
            }
        }
        if (isValid) result = cartWithProductsService.buyProductsFromBasket(session, productDTO);

        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

}
