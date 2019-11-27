package net.thumbtack.onlineshop.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.thumbtack.onlineshop.error.UserError;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductDTO {
    private String id;
    private String name;
    private String price;
    private String count;
    private List<Integer> categories;
    private List<String> categoriesName;
    private List<UserError> errors;

    public void clearFieldsToGetError() {
        this.id = null;
        this.name = null;
        this.price = null;
        this.count = null;
        this.categories = null;
        this.categoriesName = null;
        errors = new ArrayList<>();
    }
}
