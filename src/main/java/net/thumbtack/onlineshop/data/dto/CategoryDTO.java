package net.thumbtack.onlineshop.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.thumbtack.onlineshop.error.UserError;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {
    private String id;
    private String name;
    private String parentId;
    private String parentName;
    private List<UserError> errors;

    public void clearFieldsToGetError() {
        this.id = null;
        this.name = null;
        this.parentId = null;
        this.parentName = null;
        errors = new ArrayList<>();
    }
}
