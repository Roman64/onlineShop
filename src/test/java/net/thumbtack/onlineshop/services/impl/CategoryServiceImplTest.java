package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.CategoryDTO;
import net.thumbtack.onlineshop.data.model.Category;
import net.thumbtack.onlineshop.data.model.Product;
import net.thumbtack.onlineshop.data.repository.CategoryRepository;
import net.thumbtack.onlineshop.services.AdminService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AdminService adminService;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category = new Category();
    private Category categoryParent = new Category();
    private Product product = new Product();
    private String session = "123";
    private CategoryDTO categoryDTO = new CategoryDTO();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        product.setId(1);
        product.setName("продукт");
        product.setAmount(10);
        product.setPrice(50);
        product.setCategories(new ArrayList<>());
        product.getCategories().add(category);
        category.setId(10);
        category.setName("Подкатегория");
        category.setProducts(new HashSet<>());
        category.getProducts().add(product);
        category.setParent(categoryParent);
        categoryParent.setId(11);
        categoryParent.setName("Родительская категория");
        categoryDTO.setName(category.getName());
        categoryDTO.setParentId(categoryParent.getId().toString());
    }

    @Test
    public void addCategoryWithParentTest() {
        Optional<Category> optional = Optional.of(categoryParent);
        when(adminService.isLogin(session)).thenReturn(true);
        when(categoryRepository.getCategoryByName(anyString())).thenReturn(null);
        when(categoryRepository.findById(anyInt())).thenReturn(optional);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDTO result;
        result = categoryService.addCategoryByAdmin(session, categoryDTO);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getId(), category.getId().toString());
        Assert.assertEquals(result.getName(), category.getName());
        Assert.assertEquals(result.getParentId(), categoryParent.getId().toString());
        Assert.assertEquals(result.getParentName(), categoryParent.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void addCategoryTest() {
        category.setParent(null);
        when(adminService.isLogin(session)).thenReturn(true);
        when(categoryRepository.getCategoryByName(anyString())).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDTO result;
        categoryDTO.setParentId(null);
        result = categoryService.addCategoryByAdmin(session, categoryDTO);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getId(), category.getId().toString());
        Assert.assertEquals(result.getName(), category.getName());
        Assert.assertNull(result.getParentId());
        Assert.assertNull(result.getParentName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void addCategoryFailedTest() {
        CategoryDTO result;
        result = categoryService.addCategoryByAdmin(session, categoryDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        verifyNoMoreInteractions(categoryRepository);
        when(categoryRepository.getCategoryByName(anyString())).thenReturn(new Category());
        when(adminService.isLogin(session)).thenReturn(true);
        result = categoryService.addCategoryByAdmin(session, categoryDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Такая категория уже есть");
        Optional<Category> parent = Optional.empty();
        when(categoryRepository.findById(Integer.parseInt(categoryDTO.getParentId()))).thenReturn(parent);
        when(categoryRepository.getCategoryByName(anyString())).thenReturn(null);
        result = categoryService.addCategoryByAdmin(session, categoryDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет такой родительской категории");
    }

    @Test
    public void addCategoryWithWrongParamsTest() {
        category.setParent(null);
        when(adminService.isLogin(session)).thenReturn(true);
        when(categoryRepository.getCategoryByName(any())).thenReturn(null);
        CategoryDTO result;
        categoryDTO.setParentId(null);
        categoryDTO.setName(null);
        result = categoryService.addCategoryByAdmin(session, categoryDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нельзя создать категорию с пустым именем");
        categoryDTO.setName("");
        result = categoryService.addCategoryByAdmin(session, categoryDTO);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нельзя создать категорию с пустым именем");
        verify(categoryRepository, times(0)).save(any(Category.class));
    }

    @Test
    public void getCategoryByAdminFailedTest() {
        CategoryDTO result;
        result = categoryService.getCategoryByAdmin(session, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        when(categoryRepository.existsById(anyInt())).thenReturn(false);
        when(adminService.isLogin(session)).thenReturn(true);
        result = categoryService.getCategoryByAdmin(session, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "По данному id записей нет");
    }

    @Test
    public void editCategoryToNoParentTest() {
        CategoryDTO result;
        result = categoryService.getCategoryByAdmin(session, 12);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        when(categoryRepository.getOne(anyInt())).thenReturn(category);
        when(categoryRepository.existsById(anyInt())).thenReturn(true);
        categoryDTO.setName("Отредактированная категория");
        categoryDTO.setParentId(null);
        result = categoryService.editCategoryByAdmin(session, category.getId(), categoryDTO);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getName(),categoryDTO.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void editParentCategoryTest() {
        Optional<Category> optional = Optional.of(category);
        Optional<Category> optionalWrong = Optional.empty();
        when(adminService.isLogin(session)).thenReturn(true);
        when(categoryRepository.getOne(anyInt())).thenReturn(categoryParent);
        when(categoryRepository.findById(anyInt())).thenReturn(optionalWrong);
        when(categoryRepository.existsById(anyInt())).thenReturn(true);
        CategoryDTO result;
        categoryDTO.setName("Отредактированная категория");
        categoryDTO.setParentId(category.getId().toString());
        result = categoryService.editCategoryByAdmin(session, category.getId(), categoryDTO);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Не существует такой родительской категории");
        when(categoryRepository.findById(anyInt())).thenReturn(optional);
        result = categoryService.editCategoryByAdmin(session, category.getId(), categoryDTO);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Подкатегория не может стать категорией");
        when(categoryRepository.existsById(anyInt())).thenReturn(false);
        result = categoryService.editCategoryByAdmin(session, category.getId(), categoryDTO);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет категории с таким ID");
        verify(categoryRepository, times(0)).save(any(Category.class));
    }

    @Test
    public void getAllCategoriesByAdminWithWrongSession(){
        List<CategoryDTO> result = new ArrayList<>();
        result = categoryService.getAllCategoriesByAdmin(session);
        Assert.assertFalse(result.get(0).getErrors().isEmpty());
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Нет доступа");
    }

    @Test
    public void editCategoryIsParentTest() {
        Optional<Category> optional = Optional.of(categoryParent);
        when(adminService.isLogin(session)).thenReturn(true);
        when(categoryRepository.getOne(anyInt())).thenReturn(category);
        when(categoryRepository.findById(anyInt())).thenReturn(optional);
        when(categoryRepository.existsById(anyInt())).thenReturn(true);
        List<Category> list = new ArrayList<>();
        list.add(category);
        when(categoryRepository.findByParent(any(Category.class))).thenReturn(list);
        CategoryDTO result;
        categoryDTO.setName("Отредактированная категория");
        categoryDTO.setParentId(category.getId().toString());
        result = categoryService.editCategoryByAdmin(session, category.getId(), categoryDTO);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Данный запрос только для подкатегорий");
        verify(categoryRepository, times(0)).save(any(Category.class));
    }

    @Test
    public void editCategoryWithWrongParamsTest() {
        Optional<Category> optional = Optional.of(categoryParent);
        when(adminService.isLogin(session)).thenReturn(true);
        CategoryDTO result;
        categoryDTO.setName(null);
        categoryDTO.setParentId(null);
        result = categoryService.editCategoryByAdmin(session, category.getId(), categoryDTO);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Поля для запроса пустые");
        categoryDTO.setParentId(category.getId().toString());
        result = categoryService.editCategoryByAdmin(session, category.getId(), categoryDTO);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Имя категории не может быть пустым");
        verify(categoryRepository, times(0)).save(any(Category.class));
    }

    @Test
    public void deleteCategoryTest() {
        when(adminService.isLogin(session)).thenReturn(true);
        when(categoryRepository.existsById(anyInt())).thenReturn(true);
        CategoryDTO result;
        result = categoryService.deleteCategoryByAdmin(session, category.getId());
        verify(categoryRepository, times(1)).deleteById(anyInt());
        when(categoryRepository.existsById(anyInt())).thenReturn(false);
        result = categoryService.deleteCategoryByAdmin(session, category.getId());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет категории с таким ID");
        verify(categoryRepository, times(1)).deleteById(anyInt());
        when(adminService.isLogin(session)).thenReturn(false);
        result = categoryService.deleteCategoryByAdmin(session, category.getId());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        verify(categoryRepository, times(1)).deleteById(anyInt());
    }


}
