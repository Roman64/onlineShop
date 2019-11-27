package net.thumbtack.onlineshop.controller;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.services.ProductService;
import net.thumbtack.onlineshop.services.ValidateProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api("Products")
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ValidateProductService validateProductService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }



    @RequestMapping(path="/products", method = RequestMethod.POST)
    public ResponseEntity<ProductDTO> addProductByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                         @RequestBody ProductDTO productDTO) {
        validateProductService.validateProduct(productDTO);
        ProductDTO result;
        if (productDTO.getErrors() == null) result = productService.addProductByAdmin(session, productDTO);
        else result = productDTO;
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/products/{id}", method = RequestMethod.PUT)
    public ResponseEntity<ProductDTO> editProductByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                         @PathVariable int id,
                                                         @RequestBody ProductDTO productDTO) {
        validateProductService.validateProduct(productDTO);
        ProductDTO result;
        if (productDTO.getErrors() == null) result = productService.editProductByAdmin(session, productDTO, id);
        else result = productDTO;
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/products/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ProductDTO> deleteProductByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                            @PathVariable int id) {
        ProductDTO result = productService.deleteProductByAdmin(session, id);
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/products/{id}", method = RequestMethod.GET)
    public ResponseEntity<ProductDTO> getProductInfo(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                     @PathVariable int id) {
        ProductDTO result = productService.getProductById(session, id);
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ApiOperation("get Products. You can use params: categories and order")
    @RequestMapping(path="/products", method = RequestMethod.GET)
    public ResponseEntity<List<ProductDTO>> getAllProductsInfo(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                         @RequestParam(value = "categories", required = false) List<Integer> categories,
                                                         @RequestParam(value = "order", defaultValue = "product", required = false) String order) {
        List<ProductDTO> result = productService.getAllProducts(session, categories, order);
        if (result.get(0).getErrors() == null || result.get(0).getErrors().isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/purchases", method = RequestMethod.POST)
    public ResponseEntity<ProductDTO> buyProduct(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                     @RequestBody ProductDTO productDTO) {
        validateProductService.validateProduct(productDTO);
        ProductDTO result;
        if (productDTO.getErrors() == null) result = productService.buyProductByClient(session, productDTO);
        else result = productDTO;
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }



}
