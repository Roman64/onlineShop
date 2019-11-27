package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.data.model.Category;
import net.thumbtack.onlineshop.data.repository.CategoryRepository;
import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.services.AdminService;
import net.thumbtack.onlineshop.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private AdminService adminService;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public CategoryDTO addCategoryByAdmin(String session, CategoryDTO categoryDTO) {
        CategoryDTO result = new CategoryDTO();
        if (!adminService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        Category category = categoryRepository.getCategoryByName(categoryDTO.getName());
        if (category != null) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_CATEGORY_NAME, "Такая категория уже есть", "name"));
            return result;
        }
        if (categoryDTO.getName() == null || categoryDTO.getName().equals("")) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_CATEGORY_NAME, "Нельзя создать категорию с пустым именем", "name"));
            return result;
        }
        Category dao = new Category();
        dao.setName(categoryDTO.getName());
        if (categoryDTO.getParentId() != null) {
            int id = Integer.parseInt(categoryDTO.getParentId());
            Optional<Category> categoryParent = categoryRepository.findById(id);
            if (!categoryParent.isPresent()) {
                result.clearFieldsToGetError();
                result.getErrors().add(new UserError(UserErrorCode.WRONG_CATEGORY_NAME, "Нет такой родительской категории", "parent_id"));
                return result;
            }
            dao.setParent(categoryParent.get());
        }
        result = transformCategoryToDto(categoryRepository.save(dao));
        return result;
    }

    @Override
    public CategoryDTO getCategoryByAdmin(String session, Integer id) {
        CategoryDTO result = new CategoryDTO();
        if (!adminService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        if (categoryRepository.existsById(id)) {
            Category category = categoryRepository.getOne(id);
            return transformCategoryToDto(category);
        } else {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_GATEGORY_ID, "По данному id записей нет", "id"));
            return result;
        }
    }

    @Override
    public CategoryDTO editCategoryByAdmin(String session, Integer id, CategoryDTO categoryDTO) {
        CategoryDTO result = new CategoryDTO();
        if (!adminService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        if (categoryDTO.getName() == null && categoryDTO.getParentId() == null) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_CATEGORY_NAME, "Поля для запроса пустые", "id"));
            return result;
        }
        if (categoryDTO.getName() == null || categoryDTO.getName().equals("")) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_CATEGORY_NAME, "Имя категории не может быть пустым", "id"));
            return result;
        }
        if (!categoryRepository.existsById(id)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.CATEGORY_NOT_FOUND, "Нет категории с таким ID", "id"));
            return result;
        }
        Category category = categoryRepository.getOne(id);
        Optional<Category> parent = Optional.empty();
        if (categoryDTO.getParentId() != null) parent = categoryRepository.findById(Integer.parseInt(categoryDTO.getParentId()));

        if (categoryDTO.getParentId() != null) {
            if (!categoryRepository.findByParent(category).isEmpty()) {
                result.clearFieldsToGetError();
                result.getErrors().add(new UserError(UserErrorCode.WRONG_CANT_EDIT_PARENT_GATEGORY, "Данный запрос только для подкатегорий", "parent_id"));
                return result;
            }
            if (parent.isPresent() && parent.get().getParent() != null) {
                result.clearFieldsToGetError();
                result.getErrors().add(new UserError(UserErrorCode.WRONG_CANT_EDIT_PARENT_GATEGORY, "Подкатегория не может стать категорией", "parent_id"));
                return result;
            }
        }

        if (categoryDTO.getParentId() != null && !parent.isPresent()) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_CANT_EDIT_PARENT_GATEGORY, "Не существует такой родительской категории", "parent_id"));
            return result;
        }
        if (categoryDTO.getName() != null) category.setName(categoryDTO.getName());
        if (categoryDTO.getParentId() != null) category.setParent(categoryRepository.getOne(Integer.parseInt(categoryDTO.getParentId())));
        else category.setParent(null);
        categoryRepository.save(category);
        result = transformCategoryToDto(category);
        return result;
    }

    @Override
    public CategoryDTO deleteCategoryByAdmin(String session, Integer id) {
        CategoryDTO result = new CategoryDTO();
        if (!adminService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        if (!categoryRepository.existsById(id)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.CATEGORY_NOT_FOUND, "Нет категории с таким ID", "id"));
            return result;
        }
        Category category = categoryRepository.getOne(id);
        List<Category> categories = categoryRepository.findByParent(category);
        for (Category categorySub : categories) {
            categoryRepository.delete(categorySub);
        }
        categoryRepository.deleteById(id);
        return result;
    }

    @Override
    public List<CategoryDTO> getAllCategoriesByAdmin(String session) {
        List<CategoryDTO> result = new ArrayList<>();
        if (!adminService.isLogin(session)) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.clearFieldsToGetError();
            categoryDTO.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            result.add(categoryDTO);
            return result;
        }
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Order.asc("name")));
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getParent() != null) continue;
            List<Category> categories_sub = categoryRepository.findByParent(categories.get(i));
            if (categories_sub != null && !categories_sub.isEmpty()) {
                CategoryDTO categoryDTO = transformCategoryToDto(categories.get(i));
                categoryDTO.setErrors(null);
                result.add(categoryDTO);
                for (Category categorySub : categories_sub) {
                    CategoryDTO categoryDTO_sub = transformCategoryToDto(categorySub);
                    categories.remove(categorySub);
                    categoryDTO_sub.setErrors(null);
                    result.add(categoryDTO_sub);
                }
            } else {
                CategoryDTO categoryDTO = transformCategoryToDto(categories.get(i));
                categoryDTO.setErrors(null);
                result.add(categoryDTO);
            }
        }
        return result;
    }

    private CategoryDTO transformCategoryToDto(Category category){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(String.valueOf(category.getId()));
        categoryDTO.setName(category.getName());
        if (category.getParent() != null) {
            categoryDTO.setParentId(String.valueOf(category.getParent().getId()));
            categoryDTO.setParentName(category.getParent().getName());
        }
        categoryDTO.setErrors(new ArrayList<>());
        return categoryDTO;
    }
}
