package net.thumbtack.onlineshop.services.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.model.*;
import net.thumbtack.onlineshop.data.repository.*;
import net.thumbtack.onlineshop.services.AdminService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientServiceImpl clientService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private CartWithProductsRepository withProductsRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private SoldProductsRepository soldProductsRepository;
    @Mock
    private OperationsWithClientsRepository operations;
    @Mock
    private AdminServiceImpl adminService;
    @InjectMocks
    private UserServiceImpl userService;

    private String sessionAdmin = "1";
    private String sessionClient = "2";
    private Admin admin;
    private Client client;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        admin = new Admin();
        admin.setId(11);
        admin.setLogin("login");
        admin.setPassword("password");
        admin.setPosition("position");
        admin.setFirstName("Roman");
        admin.setLastName("Kovatsenko");

        client = new Client();
        client.setId(12);
        client.setLogin("login");
        client.setPassword("password");
        client.setFirstName("Roman");
        client.setLastName("Kovatsenko");
        client.setDeposit(0);
        client.setAddress("adress");
        client.setEmail("email");
        client.setPhone("phone");
    }

    @Test
    public void loginAdminTest() {
        HashMap<String, Admin> sessions = new HashMap<>();
        when(adminRepository.getAdminByLoginAndPassword(anyString(), anyString())).thenReturn(admin);
        when(adminService.getSessions()).thenReturn(sessions);
        UserDTO result;
        result = userService.login("123", "123");
        Assert.assertEquals(sessions.size(), 1);
        Assert.assertTrue(sessions.containsValue(admin));
        Assert.assertEquals(result.getId(), admin.getId().toString());
        Assert.assertEquals(result.getFirstName(), admin.getFirstName());
        Assert.assertEquals(result.getLastName(), admin.getLastName());
        Assert.assertNull(result.getPatronymic());
        Assert.assertNull(result.getLogin());
        Assert.assertNull(result.getPassword());
        Assert.assertEquals(result.getPosition(), admin.getPosition());
        Assert.assertNotNull(result.getUuid());
        verifyNoMoreInteractions(clientService);
    }

    @Test
    public void clearBaseTest() {
        Mockito.doNothing().when(adminRepository).deleteAll();
        Mockito.doNothing().when(categoryRepository).deleteAll();
        Mockito.doNothing().when(clientRepository).deleteAll();
        Mockito.doNothing().when(withProductsRepository).deleteAll();
        Mockito.doNothing().when(purchaseRepository).deleteAll();
        Mockito.doNothing().when(productRepository).deleteAll();
        Mockito.doNothing().when(soldProductsRepository).deleteAll();
        Mockito.doNothing().when(operations).deleteAll();
        Mockito.doNothing().when(adminRepository).flush();
        Mockito.doNothing().when(categoryRepository).flush();
        Mockito.doNothing().when(clientRepository).flush();
        Mockito.doNothing().when(withProductsRepository).flush();
        Mockito.doNothing().when(purchaseRepository).flush();
        Mockito.doNothing().when(productRepository).flush();
        Mockito.doNothing().when(operations).flush();
        userService.clearBase();
        verify(productRepository, times(1)).deleteAll();
        verify(categoryRepository, times(1)).deleteAll();
        verify(clientRepository, times(1)).deleteAll();
        verify(withProductsRepository, times(1)).deleteAll();
        verify(purchaseRepository, times(1)).deleteAll();
        verify(productRepository, times(1)).deleteAll();
        verify(soldProductsRepository, times(1)).deleteAll();
        verify(operations, times(1)).deleteAll();
    }

    @Test
    public void loginClientTest() {
        HashMap<String, Client> sessions = new HashMap<>();
        when(adminRepository.getAdminByLoginAndPassword(anyString(), anyString())).thenReturn(null);
        when(clientRepository.getClientByLoginAndPassword(anyString(), anyString())).thenReturn(client);
        when(clientService.getClientSessions()).thenReturn(sessions);
        UserDTO result;
        result = userService.login("123", "123");
        Assert.assertEquals(sessions.size(), 1);
        Assert.assertTrue(sessions.containsValue(client));
        Assert.assertEquals(result.getId(), client.getId().toString());
        Assert.assertEquals(result.getFirstName(), client.getFirstName());
        Assert.assertEquals(result.getLastName(), client.getLastName());
        Assert.assertNull(result.getPatronymic());
        Assert.assertNull(result.getLogin());
        Assert.assertNull(result.getPassword());
        Assert.assertEquals(result.getAddress(), client.getAddress());
        Assert.assertEquals(result.getPhone(), client.getPhone());
        Assert.assertEquals(result.getDeposit(), client.getDeposit().toString());
        Assert.assertNotNull(result.getUuid());
        verifyNoMoreInteractions(adminService);
        when(clientRepository.getClientByLoginAndPassword(anyString(), anyString())).thenReturn(null);
        sessions.clear();
        result = userService.login("123", "123");
        Assert.assertEquals(sessions.size(), 0);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Неверные данные");
    }

    @Test
    public void logoutAdminTest() {
        HashMap<String, Admin> sessions = new HashMap<>();
        sessions.put(sessionAdmin, admin);
        when(adminService.getSessions()).thenReturn(sessions);
        UserDTO result;
        result = userService.logout(sessionAdmin);
        Assert.assertEquals(sessions.size(), 0);
    }

    @Test
    public void logoutClientTest() {
        HashMap<String, Client> sessions = new HashMap<>();
        sessions.put(sessionClient, client);
        when(clientService.getClientSessions()).thenReturn(sessions);
        UserDTO result;
        result = userService.logout(sessionClient);
        Assert.assertEquals(sessions.size(), 0);
        result = userService.logout(sessionAdmin);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Данной сессии не существует");
    }

    @Test
    public void getUserInfoWrongTest() {
        HashMap<String, Admin> sessionsAdmin = new HashMap<>();
        HashMap<String, Client> sessionsClient = new HashMap<>();
        when(adminService.getSessions()).thenReturn(sessionsAdmin);
        when(clientService.getClientSessions()).thenReturn(sessionsClient);
        UserDTO result;
        result = userService.getUserInfo(sessionClient);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Данной сессии не существует");
    }

    @Test
    public void getUserInfoAdminTest() {
        HashMap<String, Admin> sessionsAdmin = new HashMap<>();
        sessionsAdmin.put(sessionAdmin, admin);
        HashMap<String, Client> sessionsClient = new HashMap<>();
        when(adminService.getSessions()).thenReturn(sessionsAdmin);
        when(clientService.getClientSessions()).thenReturn(sessionsClient);
        UserDTO result;
        result = userService.getUserInfo(sessionAdmin);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getId(), admin.getId().toString());
        Assert.assertEquals(result.getFirstName(), admin.getFirstName());
        Assert.assertEquals(result.getLastName(), admin.getLastName());
        Assert.assertNull(result.getPatronymic());
        Assert.assertNull(result.getLogin());
        Assert.assertNull(result.getPassword());
        Assert.assertEquals(result.getPosition(), admin.getPosition());
    }

    @Test
    public void getUserInfoClientTest() {
        HashMap<String, Admin> sessionsAdmin = new HashMap<>();
        HashMap<String, Client> sessionsClient = new HashMap<>();
        sessionsClient.put(sessionClient, client);
        when(adminService.getSessions()).thenReturn(sessionsAdmin);
        when(clientService.getClientSessions()).thenReturn(sessionsClient);
        UserDTO result;
        result = userService.getUserInfo(sessionClient);
        Assert.assertEquals(result.getId(), client.getId().toString());
        Assert.assertEquals(result.getFirstName(), client.getFirstName());
        Assert.assertEquals(result.getLastName(), client.getLastName());
        Assert.assertNull(result.getPatronymic());
        Assert.assertNull(result.getLogin());
        Assert.assertNull(result.getPassword());
        Assert.assertEquals(result.getAddress(), client.getAddress());
        Assert.assertEquals(result.getPhone(), client.getPhone());
        Assert.assertEquals(result.getDeposit(), client.getDeposit().toString());
    }
}
