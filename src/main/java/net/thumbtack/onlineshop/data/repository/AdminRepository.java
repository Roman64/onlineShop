package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.Admin;
import net.thumbtack.onlineshop.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    @Nullable
    Admin getAdminByLogin(String login);
    void deleteAdminByLogin(String login);
    Admin getAdminByFirstName(String firstName);
    Admin getAdminById(Integer id);
    Admin getAdminByLoginAndPassword(String login, String password);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("Update Admin a SET a.firstName=:firstName, a.lastName=:lastName, a.patronymic=:patronymic, a.position=:position, a.password=:password WHERE a.id=:id")
    void updateAdmin(@Param("id") Integer id, @Param("firstName") String firstName, @Param("lastName") String lastName,
                      @Param("patronymic") @Nullable String patronymic, @Param("position") String position, @Param("password") String password);

}
