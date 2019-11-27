package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.SoldProductsDTO;
import net.thumbtack.onlineshop.data.dto.UserDTO;
import net.thumbtack.onlineshop.data.model.Category;
import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.model.Product;
import net.thumbtack.onlineshop.data.model.SoldProducts;
import net.thumbtack.onlineshop.data.repository.ClientRepository;
import net.thumbtack.onlineshop.data.repository.ProductRepository;
import net.thumbtack.onlineshop.data.repository.SoldProductsRepository;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.services.AdminService;
import net.thumbtack.onlineshop.services.SoldProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SoldProductsServiceImpl implements SoldProductsService {

    @Autowired
    private AdminService adminService;
    @Autowired
    private SoldProductsRepository soldProductsRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ClientRepository clientRepository;

    @Override
    public List<SoldProductsDTO> getSoldProducts(String session, List<Integer> products, List<Integer>  categories,
                                                 String date, String order) {
        List<SoldProductsDTO> result = new ArrayList<>();
        if (!adminService.isLogin(session)) {
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.clearFieldsToGetError();
            soldProductsDTO.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            result.add(soldProductsDTO);
            return result;
        }
        if (date != null && !isDateAccess(date)){
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.clearFieldsToGetError();
            soldProductsDTO.getErrors().add(new UserError(UserErrorCode.WRONG_DATE, "Введите дату в формате yyyy-MM-dd", "date"));
            result.add(soldProductsDTO);
            return result;
        }
        List<SoldProducts> soldProductsList = null;
        if (products != null) {
            soldProductsList = soldProductsRepository.findByProductIdIsIn(products, Sort.by(order));
        }
        if (soldProductsList == null) {
            Pageable pageable = PageRequest.of(0, 500, Sort.by(order));
            Page<SoldProducts> page = soldProductsRepository.findAll(pageable);
            soldProductsList = new ArrayList<>(page.getContent());
            while (page.hasNext()) {
                Page<SoldProducts> nextPage = soldProductsRepository.findAll(page.nextPageable());
                soldProductsList.addAll(nextPage.getContent());
                page = nextPage;
            }
        }
        if (categories != null) {
            List<SoldProducts> toDelete = new ArrayList<>();
            for (SoldProducts soldProducts : soldProductsList) {
                Optional<Product> product = productRepository.findById(soldProducts.getProductId());
                if (product.isPresent()) {
                    List<Category> list = product.get().getCategories();
                    boolean isContains = false;
                    for (Category category : list) {
                        for (Integer integer : categories) {
                            if (integer.equals(category.getId())) {
                                isContains = true;
                                break;
                            }
                        }
                        if (isContains) break;
                    }
                    if (!isContains) toDelete.add(soldProducts);
                } else toDelete.add(soldProducts);
            }
            soldProductsList.removeAll(toDelete);
        }
        if (date != null) {
            String[] dateSort = date.split("-");
            List<SoldProducts> toDelete = new ArrayList<>();
            for (SoldProducts soldProducts : soldProductsList) {
                if (soldProducts.getDate().getYear() != Integer.parseInt(dateSort[0]) ||
                    soldProducts.getDate().getMonth().getValue() != Integer.parseInt(dateSort[1]) ||
                    soldProducts.getDate().getDayOfMonth() != Integer.parseInt(dateSort[2])) toDelete.add(soldProducts);
            }
            soldProductsList.removeAll(toDelete);
        }
        result.addAll(transformToDtoList(soldProductsList));
        return result;
    }

    @Override
    public List<SoldProductsDTO> getSoldProductsByClient(String session, Integer clientId) {
        List<SoldProductsDTO> result = new ArrayList<>();
        if (!adminService.isLogin(session)) {
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.clearFieldsToGetError();
            soldProductsDTO.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            result.add(soldProductsDTO);
            return result;
        }
        List<SoldProducts> soldProductsList = null;
        Pageable pageable = PageRequest.of(0, 500, Sort.by("date"));
        Page<SoldProducts> page = soldProductsRepository.findAllByClientId(clientId, pageable);
        soldProductsList = new ArrayList<>(page.getContent());
        while (page.hasNext()) {
            Page<SoldProducts> nextPage = soldProductsRepository.findAllByClientId(clientId, page.nextPageable());
            soldProductsList.addAll(nextPage.getContent());
            page = nextPage;
        }
        if (soldProductsList.isEmpty()) {
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.clearFieldsToGetError();
            soldProductsDTO.getErrors().add(new UserError(UserErrorCode.CLIENTS_NOT_FOUND, "Нет товаров с таким id клиента", "clientId"));
            result.add(soldProductsDTO);
            return result;
        }
        result.addAll(transformToDtoList(soldProductsList));
        for (SoldProductsDTO soldProductsDTO : result) {
            soldProductsDTO.setClientFullName(null);
        }
        return result;
    }

    @Override
    public List<SoldProductsDTO> getSoldProductsBetweenDate(String session, String dateFirst, String dateSecond) {
        List<SoldProductsDTO> result = new ArrayList<>();
        if (!adminService.isLogin(session)) {
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.clearFieldsToGetError();
            soldProductsDTO.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            result.add(soldProductsDTO);
            return result;
        }
        if (!isDateAccess(dateFirst) || !isDateAccess(dateSecond)){
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.clearFieldsToGetError();
            soldProductsDTO.getErrors().add(new UserError(UserErrorCode.WRONG_DATE, "Введите дату в формате yyyy-MM-dd", "date"));
            result.add(soldProductsDTO);
            return result;
        }
        String[] date_1 = dateFirst.split("-");
        String[] date_2 = dateSecond.split("-");
        LocalDateTime first = LocalDateTime.of(Integer.parseInt(date_1[0]), Integer.parseInt(date_1[1]), Integer.parseInt(date_1[2]), 0, 0);
        LocalDateTime second = LocalDateTime.of(Integer.parseInt(date_2[0]), Integer.parseInt(date_2[1]), Integer.parseInt(date_2[2]), 0, 0);

        List<SoldProducts> soldProductsList = soldProductsRepository.findByDateBetween(first, second);

        if (soldProductsList.isEmpty()) {
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.clearFieldsToGetError();
            soldProductsDTO.getErrors().add(new UserError(UserErrorCode.WRONG_DATE, "По таким датам заказы не найдены", "date"));
            result.add(soldProductsDTO);
            return result;
        }
        result.addAll(transformToDtoList(soldProductsList));
        return result;
    }

    private List<SoldProductsDTO> transformToDtoList(List<SoldProducts> soldProductsList) {
        int sum = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");
        List<SoldProductsDTO> result = new ArrayList<>();
        for (SoldProducts soldProducts : soldProductsList) {
            Optional<Product> product = productRepository.findById(soldProducts.getProductId());
            if (product.isPresent()) sum += product.get().getPrice() * soldProducts.getAmount();
        }
        for (SoldProducts soldProducts : soldProductsList) {
            Optional<Product> product = productRepository.findById(soldProducts.getProductId());
            SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
            soldProductsDTO.setCount(String.valueOf(soldProducts.getAmount()));
            soldProductsDTO.setDate(soldProducts.getDate().format(formatter));
            if (product.isPresent()) {
                soldProductsDTO.setProductName(product.get().getName());
                List<Category> categories = product.get().getCategories();
                soldProductsDTO.setCategoriesName(new ArrayList<>());
                for (Category category : categories) {
                    soldProductsDTO.getCategoriesName().add(category.getName());
                }
            } else soldProductsDTO.setProductName("Удаленный продукт");
            Optional<Client> client = clientRepository.findById(soldProducts.getClientId());
            if (client.isPresent()) soldProductsDTO.setClientFullName(client.get().getFirstName() + " " + client.get().getLastName());
            else  soldProductsDTO.setClientFullName("Удаленный клиент");
            result.add(soldProductsDTO);
        }
        SoldProductsDTO soldProductsDTO = new SoldProductsDTO();
        soldProductsDTO.setMessageForUser(String.format("Итого сделано покупок: %d, на сумму: %d", result.size(), sum));
        result.add(soldProductsDTO);
        return result;
    }

    private boolean isDateAccess(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        String regexForLogin = "[0-9]{4}-(0[1-9]|1[012])-(0[1-9]|1[0-9]|2[0-9]|3[01])";
        Pattern pattern = Pattern.compile(regexForLogin);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }
}
