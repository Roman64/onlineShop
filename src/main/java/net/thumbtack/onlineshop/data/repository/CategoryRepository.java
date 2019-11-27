package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category getCategoryByName(String name);
    Category getCategoryByParentId(Integer id);
    Category getCategoryByParent(Category category);
    List<Category> findByParent(Category category);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("Update Category c SET c.name=:name, c.parent=:parentId WHERE c.id=:id")
    void updateCategory(@Param("id") Integer id, @Param("name") String name, @Param("parentId") Integer parentId);
}
