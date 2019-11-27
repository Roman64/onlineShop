package net.thumbtack.onlineshop.controller;

import com.wordnik.swagger.annotations.Api;
import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api("Categories")
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @RequestMapping(path="/categories", method = RequestMethod.POST)
    public ResponseEntity<CategoryDTO> addCategoryByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO result = categoryService.addCategoryByAdmin(session, categoryDTO);
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/categories/{id}", method = RequestMethod.GET)
    public ResponseEntity<CategoryDTO> getCategoryByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                          @PathVariable int id) {
        CategoryDTO result = categoryService.getCategoryByAdmin(session, id);
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/categories/{id}", method = RequestMethod.PUT)
    public ResponseEntity<CategoryDTO> editCategoryByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session,
                                                           @RequestBody CategoryDTO categoryDTO,
                                                           @PathVariable int id)  {
        CategoryDTO result = categoryService.editCategoryByAdmin(session, id, categoryDTO);
        if (result.getErrors().isEmpty()) {
            result.setErrors(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path = "/categories/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<CategoryDTO> deleteCategoryByAdmin(@CookieValue(name = "JAVASESSIONID") String session,
                                                             @PathVariable int id) {
        CategoryDTO categoryDTO = categoryService.deleteCategoryByAdmin(session, id);
        if (categoryDTO.getErrors().isEmpty()) {
            categoryDTO.setErrors(null);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        }
        else return new ResponseEntity<>(categoryDTO, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(path="/categories", method = RequestMethod.GET)
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesByAdmin(@CookieValue(name = "JAVASESSIONID", required = true) String session) {
        List<CategoryDTO> result = categoryService.getAllCategoriesByAdmin(session);
        if (result.get(0).getErrors() != null) return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        else return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
