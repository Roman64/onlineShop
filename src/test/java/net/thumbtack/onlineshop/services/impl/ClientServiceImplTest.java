package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.model.OperationsWithClients;
import net.thumbtack.onlineshop.data.repository.ClientRepository;
import net.thumbtack.onlineshop.data.repository.OperationsWithClientsRepository;
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
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private OperationsWithClientsRepository operations;
    @Mock
    private AdminService adminService;
    @InjectMocks
    private ClientServiceImpl clientService;

    private UserDTO userDTO = new UserDTO();
    private Client client = new Client();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userDTO.setFirstName("Роман");
        userDTO.setLastName("Коваценко");
        userDTO.setEmail("Kovatsenko@gmail.com");
        userDTO.setPhone("+79873355010");
        userDTO.setAddress("Засратов");
        userDTO.setLogin("Роман123");
        userDTO.setPassword("Роман123");
        userDTO.setErrors(new ArrayList<>());
        client.setId(1);
        client.setLogin(userDTO.getLogin());
        client.setPassword(userDTO.getPassword());
        client.setLastName(userDTO.getLastName());
        client.setFirstName(userDTO.getFirstName());
        client.setDeposit(0);
        client.setPhone(userDTO.getPhone());
        client.setEmail(userDTO.getEmail());
        client.setAddress(userDTO.getAddress());
    }

    @Test
    public void registerClientTest() {
        when(clientRepository.getClientByLogin(userDTO.getLogin())).thenReturn(null);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        UserDTO result = clientService.registerClient(userDTO);
        Assert.assertEquals(clientService.getClientSessions().size(), 1);
        Assert.assertTrue(clientService.getClientSessions().containsValue(client));
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getId(), client.getId().toString());
        Assert.assertTrue(clientService.getClientSessions().containsKey(result.getUuid()));
        Assert.assertEquals(result.getFirstName(), client.getFirstName());
        Assert.assertEquals(result.getLastName(), client.getLastName());
        Assert.assertEquals(result.getEmail(), client.getEmail());
        Assert.assertEquals(result.getPhone(), client.getPhone());
        Assert.assertEquals(result.getAddress(), client.getAddress());
        Assert.assertEquals(result.getDeposit(), "0");
        Assert.assertNull(result.getLogin());
        Assert.assertNull(result.getPassword());
        verify(clientRepository, times(1)).save(any(Client.class));
        clientService.getClientSessions().clear();
    }

    @Test
    public void registerClientFailedTest() {
        when(clientRepository.getClientByLogin(userDTO.getLogin())).thenReturn(new Client());
        UserDTO result = clientService.registerClient(userDTO);
        Assert.assertFalse(result.getErrors().isEmpty());
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Пользователь с таким логином уже существует");
        verify(clientRepository, times(0)).save(any(Client.class));
    }

    @Test
    public void getAllClientsByAdminWithWrongParamsTest() {
        String session = "123";
        List<UserDTO> result = new ArrayList<>();
        when(adminService.isLogin(session)).thenReturn(false);
        result = clientService.getAllClientsByAdmin(session);
        Assert.assertFalse(result.get(0).getErrors().isEmpty());
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Нет доступа");
        when(adminService.isLogin(session)).thenReturn(true);
        when(clientRepository.findAll()).thenReturn(new ArrayList<>());
        result = clientService.getAllClientsByAdmin(session);
        Assert.assertFalse(result.get(0).getErrors().isEmpty());
        Assert.assertEquals(result.get(0).getErrors().get(0).getMessage(), "Список пуст");
    }

    @Test
    public void isCorrectPasswordTest() {
        String session = "123";
        clientService.getClientSessions().put(session, client);
        Assert.assertTrue(clientService.isCorrectPassword(session, client.getPassword()));
        clientService.getClientSessions().remove(session);
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    public void editClientProfileTest() {
        Client clientTest = new Client();
        clientTest.setLogin("MyLogin");
        clientTest.setId(1);
        userDTO.setNewPassword("NewPassword");
        String session = "123";
        clientService.getClientSessions().put(session, clientTest);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        clientService.editClientProfile(session, userDTO);
        Assert.assertEquals(clientTest.getFirstName(), userDTO.getFirstName());
        Assert.assertEquals(clientTest.getLastName(), userDTO.getLastName());
        Assert.assertEquals(clientTest.getLogin(), "MyLogin");
        Assert.assertEquals(clientTest.getId().toString(), "1");
        Assert.assertEquals(clientTest.getPassword(), userDTO.getNewPassword());
        Assert.assertEquals(clientTest.getEmail(), userDTO.getEmail());
        Assert.assertEquals(clientTest.getPhone(), userDTO.getPhone());
        Assert.assertEquals(clientTest.getAddress(), userDTO.getAddress());
        Assert.assertNull(clientTest.getPatronymic());
        UserDTO newEdit = new UserDTO();
        newEdit.setPatronymic("Александрович");
        clientService.editClientProfile(session, newEdit);
        Assert.assertEquals(clientTest.getPatronymic(), newEdit.getPatronymic());
        Assert.assertEquals(clientTest.getFirstName(), userDTO.getFirstName());
        Assert.assertEquals(clientTest.getLastName(), userDTO.getLastName());
        Assert.assertEquals(clientTest.getLogin(), "MyLogin");
        Assert.assertEquals(clientTest.getId().toString(), "1");
        Assert.assertEquals(clientTest.getPassword(), userDTO.getNewPassword());
        Assert.assertEquals(clientTest.getEmail(), userDTO.getEmail());
        Assert.assertEquals(clientTest.getPhone(), userDTO.getPhone());
        Assert.assertEquals(clientTest.getAddress(), userDTO.getAddress());
        clientService.getClientSessions().clear();
        verify(clientRepository, times(2)).save(any(Client.class));
        userDTO.setNewPassword(null);
    }

    @Test
    public void addCashTest() {
        UserDTO cash = new UserDTO();
        cash.setDeposit("200");
        String session = "123";
        when(operations.save(any(OperationsWithClients.class))).thenReturn(null);
        clientService.getClientSessions().put(session, client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        UserDTO result;
        result = clientService.addCash(session, cash);
        Assert.assertTrue(result.getErrors().isEmpty());
        Assert.assertEquals(result.getDeposit(), cash.getDeposit());
        Assert.assertEquals(client.getDeposit().toString(), cash.getDeposit());
        clientService.getClientSessions().clear();
        cash.setDeposit("dfjkgjkdf");
        result = clientService.addCash(session, cash);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Нет доступа");
        clientService.getClientSessions().put(session, client);
        result = clientService.addCash(session, cash);
        Assert.assertEquals(result.getErrors().size(), 1);
        Assert.assertEquals(result.getErrors().get(0).getMessage(), "Введите кол-во денег в верном формате");
        verify(clientRepository, times(1)).save(any(Client.class));
        clientService.getClientSessions().clear();
    }

}
