package net.thumbtack.onlineshop.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.thumbtack.onlineshop.error.UserError;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private String id;
    private String uuid;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String position;
    private String login;
    private String password;
    private String oldPassword;
    private String newPassword;
    private String email;
    private String address;
    private String phone;
    private String deposit;
    private String userType;
    private List<UserError> errors;

    public void clearFieldsToGetError() {
        this.id = null;
        this.uuid = null;
        this.firstName = null;
        this.lastName = null;
        this.patronymic = null;
        this.position = null;
        this.login = null;
        this.password = null;
        this.oldPassword = null;
        this.newPassword= null;
        this.email = null;
        this.address = null;
        this.phone = null;
        this.deposit = null;
        this.userType = null;
        errors = new ArrayList<>();
    }
}
