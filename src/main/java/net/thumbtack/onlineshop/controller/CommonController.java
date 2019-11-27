package net.thumbtack.onlineshop.controller;

import com.wordnik.swagger.annotations.Api;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.services.UserService;
import net.thumbtack.onlineshop.services.ValidateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@Api("For all Users")
@RequestMapping("/api")
public class CommonController {

    @Autowired
    private UserService userService;
    @Autowired
    private ValidateUserService validateUserService;

    @Autowired
    public CommonController(UserService userService, ValidateUserService validateUserService){
        this.userService = userService;
        this.validateUserService = validateUserService;
    }


    @RequestMapping(path="/sessions", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO userDTO, HttpServletResponse httpServletResponse) {
        UserDTO updatedUserDto = userService.login(userDTO.getLogin(), userDTO.getPassword());
        if (updatedUserDto.getErrors().isEmpty()) {
            Cookie cookie = new Cookie("JAVASESSIONID", updatedUserDto.getUuid());
            httpServletResponse.addCookie(cookie);
            updatedUserDto.setUuid(null);
            updatedUserDto.setErrors(null);
            return new ResponseEntity<>(updatedUserDto, HttpStatus.OK);
        }
        else {
            updatedUserDto.setUuid(null);
            return new ResponseEntity<>(updatedUserDto, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/sessions", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<UserDTO> logout(@CookieValue(name = "JAVASESSIONID") String session) {
        UserDTO userDTO = userService.logout(session);
        if (userDTO.getErrors() == null || userDTO.getErrors().isEmpty()) {
            userDTO.setErrors(null);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
        else return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path = "/accounts", method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getUserInfo(@CookieValue(value = "JAVASESSIONID") String session) {
        UserDTO userDTO = userService.getUserInfo(session);
        if (userDTO.getErrors().isEmpty()) {
            userDTO.setErrors(null);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
        else return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path = "/debug/clear", method = RequestMethod.POST)
    public String clearBase (@RequestBody String clear, HttpServletResponse httpServletResponse) {
        return userService.clearBase();
    }

    @RequestMapping(path = "/settings", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getServerSettings(@CookieValue(name = "JAVASESSIONID", defaultValue = "default") String session) {
        return validateUserService.getServerSettings(session);
    }


}
