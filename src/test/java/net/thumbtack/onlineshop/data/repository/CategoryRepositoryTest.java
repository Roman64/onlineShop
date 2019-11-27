package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.Category;
import net.thumbtack.onlineshop.data.model.Product;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryRepositoryTest {

    private String name = "category";


    @Autowired
    private CategoryRepository categoryRepository;


    @After
    public void afterTest() {
        categoryRepository.deleteAll();
    }

    @Test
    public void addCategory(){
        Category category = new Category();
        category.setName(name);
        Category expected = categoryRepository.save(category);
        Assert.assertEquals(category, expected);
    }

    @Test
    public void addCategoryWithParent(){
        Category category = new Category();
        category.setName(name);
        Category categorySub = new Category();
        categorySub.setName("new");
        categorySub.setParent(category);
        Category expected = categoryRepository.save(categorySub);
        Assert.assertEquals(categorySub, expected);
    }
}
