package net.thumbtack.onlineshop.controller;


import com.wordnik.swagger.annotations.Api;
import net.thumbtack.onlineshop.data.dto.SoldProductsDTO;
import net.thumbtack.onlineshop.services.SoldProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.joda.LocalDateTimeParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Api("Sold products")
@RequestMapping("/api")
public class SoldProductsController {

    @Autowired
    private SoldProductsService soldProductsService;

    @Autowired
    public SoldProductsController(SoldProductsService soldProductsService){
        this.soldProductsService = soldProductsService;
    }

    @RequestMapping(path="/purchases", method = RequestMethod.GET)
    public ResponseEntity<List<SoldProductsDTO>> getSoldProducts(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                 @RequestParam(name = "products", required = false) List<Integer>  products,
                                                                 @RequestParam(name = "categories", required = false) List<Integer> categories,
                                                                 @RequestParam(name = "date", required = false) String date,
                                                                 @RequestParam(name = "order", defaultValue = "amount") String order){

        List<SoldProductsDTO> result = soldProductsService.getSoldProducts(session, products, categories, date, order);
        if (result.get(0).getErrors() == null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/purchases/client", method = RequestMethod.GET)
    public ResponseEntity<List<SoldProductsDTO>> getSoldProductsByClient(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                 @RequestParam(name = "clientId", required = false) Integer clientId){

        List<SoldProductsDTO> result = soldProductsService.getSoldProductsByClient(session, clientId);
        if (result.get(0).getErrors() == null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/purchases/date", method = RequestMethod.GET)
    public ResponseEntity<List<SoldProductsDTO>> getSoldProductsBetweenDate(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                                            @RequestParam(name = "dateFirst", required = false) String first,
                                                                            @RequestParam(name = "dateSecond", required = false) String second){
        List<SoldProductsDTO> result = soldProductsService.getSoldProductsBetweenDate(session, first, second);
        if (result.get(0).getErrors() == null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}
