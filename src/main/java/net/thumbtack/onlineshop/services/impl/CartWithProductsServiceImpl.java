package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.dto.CartWithProductsDTO;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.data.model.*;
import net.thumbtack.onlineshop.data.repository.*;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.services.AdminService;
import net.thumbtack.onlineshop.services.CartWithProductsService;
import net.thumbtack.onlineshop.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static java.lang.Integer.*;

@Service
public class CartWithProductsServiceImpl implements CartWithProductsService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartWithProductsRepository cartWithProductsRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private SoldProductsRepository soldProductsRepository;
    @Autowired
    private ClientService clientService;
    @Autowired
    private AdminService adminService;



    @Override
    public CartWithProductsDTO addProductToBasket(String session, ProductDTO productDTO) {
        CartWithProductsDTO result = new CartWithProductsDTO();
        result.clearFieldsToGetError();
        if (!clientService.isLogin(session)) {
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        int id = 0;
        try {
            id = parseInt(productDTO.getId());
        } catch (NumberFormatException e) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Неверный формат ID", "id"));
            return result;
        }
        Optional<Product> product = productRepository.findById(id);
        if (!product.isPresent()) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Товара по данному id не найдено", "id"));
            return result;
        }
        Product productDB = product.get();
        if (!productDTO.getName().equals(productDB.getName())) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Названия товаров не совпадают", "name"));
            return result;
        }
        if ( productDB.getPrice() != parseInt(productDTO.getPrice())) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Цены товаров не совпадают", "price"));
            return result;
        }
        Client clientDB = clientService.getClientSessions().get(session);
        if (clientDB.getCartWithProducts() == null) {
            CartWithProducts cartWithProducts = new CartWithProducts();
            clientDB.setCartWithProducts(cartWithProducts);
        }
        if (clientDB.getCartWithProducts().getPurchase() == null) {
            clientDB.getCartWithProducts().setPurchase(new ArrayList<>());
        }
        List<Purchase> purchases = clientDB.getCartWithProducts().getPurchase();
        boolean finded = false;
        for (Purchase purchase : purchases) {
            if (purchase.getProduct().getId().equals(productDB.getId())) {
                int count = purchase.getQuantity() + parseInt(productDTO.getCount());
                purchase.setQuantity(count);
                finded = true;
                break;
            }
        }
        if (!finded) {
            Purchase purchase = new Purchase();
            purchase.setProduct(productDB);
            purchase.setQuantity(parseInt(productDTO.getCount()));
            purchase.setCartWithProducts(clientDB.getCartWithProducts());
            clientDB.getCartWithProducts().getPurchase().add(purchase);
        }
        clientDB  = clientRepository.save(clientDB);
        clientService.getClientSessions().put(session, clientDB);
        result.setRemaining(transformPurchasesToProductDtoList(clientDB.getCartWithProducts().getPurchase()));
        return result;
    }

    @Override
    public CartWithProductsDTO deleteProductFromBasket(String session, Integer id) {
        CartWithProductsDTO result = new CartWithProductsDTO();
        result.clearFieldsToGetError();
        if (!clientService.isLogin(session)) {
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        Optional<Product> product = productRepository.findById(id);
        if (!product.isPresent()) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Нет товара с таким ID", "id"));
            return result;
        }
        Client client = clientService.getClientSessions().get(session);
        List<Purchase> purchases = client.getCartWithProducts().getPurchase();
        List<Purchase> newPurchases = new ArrayList<>();
        for (Purchase purchase : purchases) {
            if (purchase.getProduct().getId().equals(id)) {
                CartWithProducts cartWithProducts = cartWithProductsRepository.getOne(client.getCartWithProducts().getId());
                purchaseRepository.delete(purchase);
                cartWithProductsRepository.save(cartWithProducts);
            } else {
                newPurchases.add(purchase);
            }
        }
        client.getCartWithProducts().setPurchase(newPurchases);
        clientRepository.save(client);
        clientRepository.flush();
        clientService.getClientSessions().put(session,client);
        return result;
    }

    private List<ProductDTO> transformPurchasesToProductDtoList(List<Purchase> purchase) {
        List<ProductDTO> result = new ArrayList<>();
        for (Purchase purchaseFromList : purchase) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(purchaseFromList.getProduct().getId().toString());
            productDTO.setPrice(String.valueOf(purchaseFromList.getProduct().getPrice()));
            productDTO.setName(purchaseFromList.getProduct().getName());
            productDTO.setCount(purchaseFromList.getQuantity().toString());
            result.add(productDTO);
        }
        return result;
    }



    @Override
    public CartWithProductsDTO editProductInBasket(String session, ProductDTO productDTO) {
        CartWithProductsDTO result = new CartWithProductsDTO();
        result.clearFieldsToGetError();
        if (!clientService.isLogin(session)) {
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        int id = 0;
        try {
            id = parseInt(productDTO.getId());
        } catch (NumberFormatException e) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Неверный формат ID", "id"));
            return result;
        }
        Optional<Product> product = productRepository.findById(id);
        if (!product.isPresent()) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Товара по данному id не найдено", "id"));
            return result;
        }
        Product productDB = product.get();
        if (!productDTO.getName().equals(productDB.getName())) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Названия товаров не совпадают", "name"));
            return result;
        }
        if ( productDB.getPrice() != parseInt(productDTO.getPrice())) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Цены товаров не совпадают", "price"));
            return result;
        }
        Client client = clientService.getClientSessions().get(session);
        List<Purchase> purchases = client.getCartWithProducts().getPurchase();
        for (Purchase purchase : purchases) {
            if (purchase.getProduct().getId().equals(product.get().getId())) {
                purchase.setQuantity(parseInt(productDTO.getCount()));
                purchaseRepository.save(purchase);
            }
        }
        result.setRemaining(transformPurchasesToProductDtoList(client.getCartWithProducts().getPurchase()));
        return result;
    }

    @Override
    public CartWithProductsDTO getBasket(String session) {
        CartWithProductsDTO result = new CartWithProductsDTO();
        result.clearFieldsToGetError();
        if (!clientService.isLogin(session)) {
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        Client client = clientService.getClientSessions().get(session);
        result.setRemaining(transformPurchasesToProductDtoList(client.getCartWithProducts().getPurchase()));
        return result;
    }

    @Override
    public CartWithProductsDTO buyProductsFromBasket(String session, List<ProductDTO> productDTOList) {
        CartWithProductsDTO result = new CartWithProductsDTO();
        result.clearFieldsToGetError();
        if (!clientService.isLogin(session)) {
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        Client client = clientService.getClientSessions().get(session);
        List<Purchase> purchases = client.getCartWithProducts().getPurchase();
        List<ProductDTO> needToBuy = new ArrayList<>();
        for (ProductDTO productDTO : productDTOList) {
            for (Purchase purchase : purchases) {
                if (parseInt(productDTO.getId()) == purchase.getProduct().getId() && productRepository.existsById(purchase.getProduct().getId())) {
                    if (productDTO.getCount() == null || parseInt(productDTO.getCount()) > purchase.getQuantity()) {
                        productDTO.setCount(purchase.getQuantity().toString());
                    }
                    if (productDTO.getName().equals(purchase.getProduct().getName()) &&
                            (parseInt(productDTO.getPrice()) == purchase.getProduct().getPrice()) &&
                            (parseInt(productDTO.getCount()) <= purchase.getProduct().getAmount())){
                        needToBuy.add(productDTO);
                    }
                }
            }
        }
        int needCash = 0;
        int haveCash = client.getDeposit();
        for (ProductDTO productDTO : needToBuy) {
            needCash += parseInt(productDTO.getPrice()) * parseInt(productDTO.getCount());
        }
        if (needCash > haveCash) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ClIENT_DEPOSIT_TO_LOW, "Не хватает денег на счету", "deposit"));
            return result;
        }
        List<Purchase> purchaseListNew = new ArrayList<>();
        boolean isAdd = false;
        for (Purchase purchase : purchases) {
            for (ProductDTO productDTO : needToBuy) {
                if (parseInt(productDTO.getId()) == purchase.getProduct().getId()) {
                    SoldProducts soldProducts = new SoldProducts();
                    soldProducts.setClientId(client.getId());
                    soldProducts.setProductId(purchase.getProduct().getId());
                    soldProducts.setAmount(Integer.parseInt(productDTO.getCount()));
                    soldProducts.setDate(LocalDateTime.now());
                    soldProductsRepository.save(soldProducts);
                    int newCount = purchase.getQuantity() - parseInt(productDTO.getCount());
                    if (newCount != 0) {
                        purchase.setQuantity(newCount);
                        purchaseListNew.add(purchase);
                        isAdd = true;
                    } else isAdd = true;

                }
            }
            if (!isAdd) {
                purchaseListNew.add(purchase);
            } else isAdd = false;
            CartWithProducts cartWithProducts = cartWithProductsRepository.getOne(client.getCartWithProducts().getId());
            purchaseRepository.delete(purchase);
            cartWithProductsRepository.save(cartWithProducts);
        }
        client.getCartWithProducts().setPurchase(purchaseListNew);
        int newCash = haveCash - needCash;
        client.setDeposit(newCash);
        client = clientRepository.save(client);
        clientRepository.flush();
        clientService.getClientSessions().put(session, client);
        result.setBought(new ArrayList<>());
        for (ProductDTO productDTO : needToBuy) {
            result.getBought().add(productDTO);
            Product product = productRepository.getOne(parseInt(productDTO.getId()));
            int newCount = product.getAmount() - parseInt(productDTO.getCount());
            product.setAmount(newCount);
            productRepository.save(product);
        }
        result.setRemaining(transformPurchasesToProductDtoList(client.getCartWithProducts().getPurchase()));
        return result;
    }
}
