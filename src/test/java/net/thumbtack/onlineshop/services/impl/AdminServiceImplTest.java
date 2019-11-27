package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.OperationsWithClientsDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.model.Admin;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.model.OperationsWithClients;
import net.thumbtack.onlineshop.data.repository.AdminRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;
    @InjectMocks
    private AdminServiceImpl adminService;
    private UserDTO userDTO = new UserDTO();
    private Admin admin = new Admin();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userDTO.setFirstName("Роман");
        userDTO.setLastName("Коваценко");
        userDTO.setPosition("админ");
        userDTO.setLogin("Роман123");
        userDTO.setPassword("Роман123");
        userDTO.setErrors(new ArrayList<>());
        admin.setId(1);
        admin.setLogin(userDTO.getLogin());
        admin.setPassword(userDTO.getPassword());
        admin.setLastName(userDTO.getLastName());
        admin.setFirstName(userDTO.getFirstName());
        admin.setPosition(userDTO.getPosition());
    }

    @Test
    public void registerAdminTest() {
        when(adminRepository.getAdminByLogin(userDTO.getLogin())).thenReturn(null);
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);
        UserDTO result = adminService.registerAdmin(userDTO);
        Assert.assertEquals(adminService.getSessions().size(), 1);
        Assert.assertTrue(adminService.getSessions().containsValue(admin));
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getId(), admin.getId().toString());
        Assert.assertTrue(adminService.getSessions().containsKey(result.getUuid()));
        Assert.assertEquals(result.getFirstName(), admin.getFirstName());
        Assert.assertEquals(result.getLastName(), admin.getLastName());
        Assert.assertEquals(result.getPosition(), admin.getPosition());
        Assert.assertNull(result.getLogin());
        Assert.assertNull(result.getPassword());
        when(adminRepository.getAdminByLogin(userDTO.getLogin())).thenReturn(new Admin());
        UserDTO resultWrong = adminService.registerAdmin(userDTO);
        Assert.assertEquals(adminService.getSessions().size(), 1);
        Assert.assertFalse(resultWrong.getErrors().isEmpty());
        Assert.assertEquals(resultWrong.getErrors().get(0).getMessage(), "Пользователь с таким логином уже существует");
        adminService.getSessions().clear();
    }

    @Test
    public void getOperationHistoryWrongTest() {
        String session = "123";
        List<OperationsWithClientsDTO> operations = new ArrayList<>();
        operations = adminService.getOperationsHistory(session, 1, "get");
        Assert.assertFalse(operations.get(0).getErrors().isEmpty());
        Assert.assertEquals(operations.get(0).getErrors().get(0).getMessage(), "Нет доступа");
        adminService.getSessions().put(session, admin);
        operations = adminService.getOperationsHistory(session, 0, null);
        Assert.assertFalse(operations.get(0).getErrors().isEmpty());
        Assert.assertEquals(operations.get(0).getErrors().get(0).getMessage(), "Не удалось найти по данным критериям");
        adminService.getSessions().remove(session);
    }

    @Test
    public void isCorrectPasswordTest() {
        String session = "123";
        adminService.getSessions().put(session, admin);
        Assert.assertTrue(adminService.isCorrectPassword(session, admin.getPassword()));
        adminService.getSessions().remove(session);
        verifyNoMoreInteractions(adminRepository);
    }


    @Test
    public void isLoginTest() {
        String session = "123";
        Assert.assertFalse(adminService.isLogin(session));
        UserDTO result = new UserDTO();
        result.setFirstName("Роман90");
        result.setLastName("Коваценко90");
        result.setPosition("админ90");
        result.setLogin("Роман12390");
        result.setPassword("Роман12390");
        result.setErrors(new ArrayList<>());
        Admin adminDB = new Admin();
        adminDB.setFirstName(result.getFirstName());
        adminDB.setLastName(result.getLastName());
        adminDB.setPosition(result.getPosition());
        adminDB.setPassword(result.getPassword());
        adminDB.setLogin(result.getLogin());
        adminDB.setId(1);
        when(adminRepository.save(any(Admin.class))).thenReturn(adminDB);
        result = adminService.registerAdmin(userDTO);
        Assert.assertTrue(adminService.isLogin(result.getUuid()));
        adminService.getSessions().remove(result.getUuid());
        verify(adminRepository, times(1)).save(any(Admin.class));
    }

    @Test
    public void editProfileTest() {
        Admin adminTest = new Admin();
        adminTest.setLogin("MyLogin");
        adminTest.setId(1);
        userDTO.setNewPassword("NewPassword");
        String session = "123";
        adminService.getSessions().put(session, adminTest);
        doNothing().when(adminRepository).updateAdmin(anyInt(), anyString(), anyString(), any(), anyString(), anyString());
        adminService.editProfile(session, userDTO);
        Assert.assertEquals(adminTest.getFirstName(), userDTO.getFirstName());
        Assert.assertEquals(adminTest.getLastName(), userDTO.getLastName());
        Assert.assertEquals(adminTest.getPosition(), userDTO.getPosition());
        Assert.assertEquals(adminTest.getLogin(), "MyLogin");
        Assert.assertEquals(adminTest.getId().toString(), "1");
        Assert.assertEquals(adminTest.getPassword(), userDTO.getNewPassword());
        Assert.assertNull(adminTest.getPatronymic());
        UserDTO newEdit = new UserDTO();
        newEdit.setPatronymic("Александрович");
        adminService.editProfile(session, newEdit);
        Assert.assertEquals(adminTest.getPatronymic(), newEdit.getPatronymic());
        Assert.assertEquals(adminTest.getFirstName(), userDTO.getFirstName());
        Assert.assertEquals(adminTest.getLastName(), userDTO.getLastName());
        Assert.assertEquals(adminTest.getPosition(), userDTO.getPosition());
        Assert.assertEquals(adminTest.getLogin(), "MyLogin");
        Assert.assertEquals(adminTest.getId().toString(), "1");
        Assert.assertEquals(adminTest.getPassword(), userDTO.getNewPassword());
        adminService.getSessions().clear();
        userDTO.setNewPassword(null);
    }
}
