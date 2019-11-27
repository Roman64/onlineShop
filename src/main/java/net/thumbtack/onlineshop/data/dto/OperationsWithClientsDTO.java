package net.thumbtack.onlineshop.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.thumbtack.onlineshop.error.UserError;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationsWithClientsDTO {

    private String id;
    private String date;
    private String session;
    private String clientId;
    private String method;
    private List<UserError> errors;

    public void clearFieldsToGetError() {
        this.id = null;
        this.date = null;
        this.session = null;
        this.clientId = null;
        this.method = null;
        errors = new ArrayList<>();
    }
}
