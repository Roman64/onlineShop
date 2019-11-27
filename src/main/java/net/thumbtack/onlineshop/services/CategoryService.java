package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.data.model.Category;

import java.util.List;


public interface CategoryService {

    CategoryDTO addCategoryByAdmin(String session, CategoryDTO categoryDTO);
    CategoryDTO getCategoryByAdmin(String session, Integer id);
    CategoryDTO editCategoryByAdmin(String session, Integer id, CategoryDTO categoryDTO);
    CategoryDTO deleteCategoryByAdmin(String session, Integer id);
    List<CategoryDTO> getAllCategoriesByAdmin(String session);
}
