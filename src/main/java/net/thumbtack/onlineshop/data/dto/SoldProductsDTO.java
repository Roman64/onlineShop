package net.thumbtack.onlineshop.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.thumbtack.onlineshop.error.UserError;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SoldProductsDTO {
    private String messageForUser;
    private String id;
    private String clientId;
    private String clientFullName;
    private String productId;
    private String productName;
    private String count;
    private List<String> categoriesName;
    private String date;
    private List<UserError> errors;

    public void clearFieldsToGetError() {
        this.id = null;
        this.clientId = null;
        this.clientFullName = null;
        this.productId = null;
        this.productName = null;
        this.count = null;
        this.categoriesName = null;
        this.date = null;
        this.messageForUser = null;
        errors = new ArrayList<>();
    }
}
