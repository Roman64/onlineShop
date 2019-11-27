package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.dto.CartWithProductsDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.SoldProductsDTO;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.model.Product;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface CartWithProductsService {
    CartWithProductsDTO addProductToBasket(String session, ProductDTO productDTO);
    CartWithProductsDTO deleteProductFromBasket(String session, Integer id);
    CartWithProductsDTO editProductInBasket(String session, ProductDTO productDTO);
    CartWithProductsDTO getBasket(String session);
    CartWithProductsDTO buyProductsFromBasket(String session, List<ProductDTO> productDTOList);
}
