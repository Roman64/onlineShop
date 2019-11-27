package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.Admin;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AdminRepositoryTest {

    private String firstName = "Roman";
    private String lastName = "Kovatsenko";
    private String patronomic = "Aleksandrovich";
    private String login = "Raven90";
    private String pass = "490003nnn";
    private String post = "administrator";
    private Admin admin;

    @Autowired
    private AdminRepository adminRepository;


    @After
    public void afterTest() {
        adminRepository.deleteAll();
    }

    @Test
    public void addAdminTest(){
        admin = new Admin();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setPatronymic(patronomic);
        admin.setLogin(login);
        admin.setPassword(pass);
        admin.setPosition(post);
        Admin expected = adminRepository.save(admin);
        Assert.assertEquals(admin, expected);
    }

    @Test
    public void getAdminByFirstName() {
        admin = new Admin();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setPatronymic(patronomic);
        admin.setLogin(login);
        admin.setPassword(pass);
        admin.setPosition(post);
        adminRepository.save(admin);
        Admin expected = adminRepository.getAdminByFirstName(firstName);
        Assert.assertEquals(admin, expected);
    }

    @Test
    public void getAdminById() {
        admin = new Admin();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setPatronymic(patronomic);
        admin.setLogin(login);
        admin.setPassword(pass);
        admin.setPosition(post);
        adminRepository.save(admin);
        Admin adminByDB = adminRepository.getAdminByFirstName(firstName);
        Admin expected = adminRepository.getAdminById(adminByDB.getId());
        Assert.assertEquals(admin, expected);
    }
}
