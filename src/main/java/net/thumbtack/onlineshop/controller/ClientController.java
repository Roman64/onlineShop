package net.thumbtack.onlineshop.controller;

import com.wordnik.swagger.annotations.Api;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.services.ClientService;
import net.thumbtack.onlineshop.services.ValidateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api("Clients")
@RequestMapping("/api")
public class ClientController {

   @Autowired
   private ClientService clientService;
   @Autowired
    private ValidateUserService validateUserService;

   @Autowired
   public ClientController(ClientService clientService, ValidateUserService validateUserService){
       this.clientService = clientService;
       this.validateUserService = validateUserService;
   }

    @RequestMapping(path="/clients", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<UserDTO>> getAllClientsByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session) {
        List<UserDTO> result = clientService.getAllClientsByAdmin(session);
        if (!result.get(0).getErrors().isEmpty()) return new ResponseEntity<List<UserDTO>>(result, HttpStatus.BAD_REQUEST);
        for (UserDTO userDTO : result) {
                userDTO.setErrors(null);
        }
        return new ResponseEntity<List<UserDTO>>(result, HttpStatus.OK);
    }

    @RequestMapping(path="/clients", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<UserDTO> addClient(@RequestBody UserDTO userDTO, HttpServletResponse httpServletResponse) {
        validateUserService.validateClient(userDTO);
        UserDTO client = clientService.registerClient(userDTO);
        if (client.getErrors().isEmpty()) {
            client.setErrors(null);
            Cookie cookie = new Cookie("JAVASESSIONID", client.getUuid());
            httpServletResponse.addCookie(cookie);
            client.setUuid(null);
            return new ResponseEntity<>(client, HttpStatus.OK);
        } else {
            client.setUuid(null);
            return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path="/clients", method = RequestMethod.PUT)
    public ResponseEntity<UserDTO> editClient(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                       @RequestBody UserDTO userDTO) {
        if (clientService.isLogin(session)) {
            validateUserService.validateEditClient(userDTO);
            if (!clientService.isCorrectPassword(session, userDTO.getOldPassword())){
                userDTO.clearFieldsToGetError();
                userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_PASSWORD, "Пароли не совпадают", "oldPassword"));
            }
        } else {
            userDTO.clearFieldsToGetError();
            userDTO.getErrors().add(new UserError(UserErrorCode.INVALID_SESSION,"Вы не залогинены", "LOGIN"));
            return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
        }
        if (userDTO.getErrors().isEmpty()) {
            userDTO.setErrors(null);
            String client_id = clientService.editClientProfile(session, userDTO);
            userDTO.setId(client_id);
            userDTO.setOldPassword(null);
            userDTO.setNewPassword(null);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
        else {
            userDTO.setOldPassword(null);
            userDTO.setNewPassword(null);
            return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path="/deposits", method = RequestMethod.PUT)
    public ResponseEntity<UserDTO> addCash(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                        @RequestBody UserDTO userDTO) {
        UserDTO result = clientService.addCash(session, userDTO);
        if (result.getErrors() == null || result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/deposits", method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getCash(@CookieValue(name = "JAVASESSIONID", required = true) String session) {
        UserDTO result = clientService.getCash(session);
        if (result.getErrors() == null || result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}
