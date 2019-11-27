package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.services.ValidateProductService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ValidateProductServiceImpl implements ValidateProductService {
    @Override
    public void validateProduct(ProductDTO productDTO) {
        if (productDTO.getCount() != null) {
            try {
                Integer.parseInt(productDTO.getCount());
            } catch (NumberFormatException e) {
                productDTO.clearFieldsToGetError();
                productDTO.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT,
                        "В запросе кол-во товара должно быть указано цифрами", "count"));
            }
        }
        if (productDTO.getPrice() != null) {
            try {
                Integer.parseInt(productDTO.getPrice());
            } catch (NumberFormatException e) {
                productDTO.clearFieldsToGetError();
                productDTO.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT,
                        "В запросе цена должна быть указана цифрами", "price"));
            }
        }
    }
}
