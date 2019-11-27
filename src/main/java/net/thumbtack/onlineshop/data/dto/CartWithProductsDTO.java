package net.thumbtack.onlineshop.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.thumbtack.onlineshop.error.UserError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartWithProductsDTO {

    private List<ProductDTO> bought;
    private List<ProductDTO> remaining;
    private List<UserError> errors;

    public void clearFieldsToGetError() {
        this.bought = null;
        this.remaining = null;
        errors = new ArrayList<>();
    }

}
