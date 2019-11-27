package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.dto.ProductDTO;

import java.util.List;


public interface ProductService {
    ProductDTO addProductByAdmin(String session, ProductDTO productDTO);
    ProductDTO editProductByAdmin(String session, ProductDTO productDTO, Integer id);
    ProductDTO deleteProductByAdmin(String session, Integer id);
    ProductDTO getProductById(String session, Integer id);
    ProductDTO buyProductByClient(String session, ProductDTO productDTO);
    List<ProductDTO> getAllProducts(String session, List<Integer> categories, String order);
}
