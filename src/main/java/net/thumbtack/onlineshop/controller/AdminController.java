package net.thumbtack.onlineshop.controller;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import net.thumbtack.onlineshop.data.dto.OperationsWithClientsDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.services.AdminService;
import net.thumbtack.onlineshop.services.ValidateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api("Admins")
@RequestMapping("/api")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private ValidateUserService validateUserService;
    @Autowired
    public AdminController(AdminService adminService, ValidateUserService validateUserService){
        this.adminService = adminService;
        this.validateUserService = validateUserService;
    }

    @RequestMapping(path="/admins", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<UserDTO> addAdmin(@RequestBody UserDTO userDTO, HttpServletResponse httpServletResponse) {
        validateUserService.validateAdmin(userDTO);
        UserDTO admin = adminService.registerAdmin(userDTO);
        if (admin.getErrors().isEmpty()) {
            admin.setErrors(null);
            Cookie cookie = new Cookie("JAVASESSIONID", admin.getUuid());
            httpServletResponse.addCookie(cookie);
            admin.setUuid(null);
            return new ResponseEntity<>(admin, HttpStatus.OK);
        } else {
            admin.setUuid(null);
            return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
        }
    }
    @RequestMapping(path="/admins", method = RequestMethod.PUT)
    public ResponseEntity<?> editAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String id,
                                       @RequestBody UserDTO userDTO) {
        if (adminService.isLogin(id)) {
            validateUserService.validateEditAdmin(userDTO);
            if (!adminService.isCorrectPassword(id, userDTO.getOldPassword())){
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
            String admin_id = adminService.editProfile(id, userDTO);
            userDTO.setId(admin_id);
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

    @RequestMapping(path="/admins/history", method = RequestMethod.GET)
    public ResponseEntity<List<OperationsWithClientsDTO>> getHistory (@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                      @RequestParam(name = "clientId", required = false) Integer clientId,
                                                                      @RequestParam(name = "method", required = false) String method) {
        List<OperationsWithClientsDTO> result = adminService.getOperationsHistory(session, clientId, method);
        if (!result.isEmpty() && result.get(0).getErrors() == null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}
