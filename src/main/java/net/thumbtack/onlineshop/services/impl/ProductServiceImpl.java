package net.thumbtack.onlineshop.services.impl;

import net.thumbtack.onlineshop.data.model.Client;
import net.thumbtack.onlineshop.data.model.SoldProducts;
import net.thumbtack.onlineshop.data.repository.ClientRepository;
import net.thumbtack.onlineshop.data.repository.SoldProductsRepository;
import net.thumbtack.onlineshop.error.UserError;
import net.thumbtack.onlineshop.error.UserErrorCode;
import net.thumbtack.onlineshop.data.model.Category;
import net.thumbtack.onlineshop.data.model.Product;
import net.thumbtack.onlineshop.data.repository.CategoryRepository;
import net.thumbtack.onlineshop.data.repository.ProductRepository;
import net.thumbtack.onlineshop.data.dto.ProductDTO;
import net.thumbtack.onlineshop.services.AdminService;
import net.thumbtack.onlineshop.services.ClientService;
import net.thumbtack.onlineshop.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AdminService adminService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private SoldProductsRepository soldProductsRepository;


    @Override
    public ProductDTO addProductByAdmin(String session, ProductDTO productDTO) {
        ProductDTO result = new ProductDTO();
        if (!adminService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        if (productDTO.getPrice() == null) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Необходимо указать цену товара", "Price"));
            return result;
        }
        if (Integer.parseInt(productDTO.getPrice()) <= 0) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Цена товара не может быть <=0", "Price"));
            return result;
        }
        Product product = new Product();
        transformDtoToProduct(productDTO, product);
        if (product.getCategories() == null) product.setCategories(new ArrayList<>());
        product = productRepository.save(product);
        result = transformProductToDto(product);
        return result;
    }


    @Override
    public ProductDTO editProductByAdmin(String session, ProductDTO productDTO, Integer id) {
        ProductDTO result = new ProductDTO();
        if (!adminService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        if (productDTO.getPrice() != null && Integer.parseInt(productDTO.getPrice()) <= 0) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Цена товара не может быть <=0", "Price"));
            return result;
        }
        Product product = productRepository.getOne(id);
        if (product == null) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Товара по данному id не найдено", "id"));
            return result;
        }
        transformDtoToProduct(productDTO, product);
        product = productRepository.save(product);
        result = transformProductToDto(product);
        return result;
    }

    @Override
    public ProductDTO deleteProductByAdmin(String session, Integer id) {
        ProductDTO result = new ProductDTO();
        result.clearFieldsToGetError();
        if (!adminService.isLogin(session)) {
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        if (!productRepository.existsById(id)) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Нет продукта с данным id", "ID"));
            return result;
        }
        productRepository.deleteById(id);
        return result;
    }

    @Override
    public ProductDTO getProductById(String session, Integer id) {
        ProductDTO result = new ProductDTO();
        if (!adminService.isLogin(session) && !clientService.isLogin(session)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        if (!productRepository.existsById(id)) {
            result.clearFieldsToGetError();
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Нет продукта с данным id", "ID"));
            return result;
        }
        Product product = productRepository.getOne(id);
        result = transformProductToDto(product);
        return result;
    }

    @Override
    public ProductDTO buyProductByClient(String session, ProductDTO productDTO) {
        ProductDTO result = new ProductDTO();
        result.clearFieldsToGetError();
        if (!clientService.isLogin(session)) {
            result.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            return result;
        }
        Optional<Product> product = productRepository.findById(Integer.parseInt(productDTO.getId()));
        Product productFromDB = new Product();
        if (!product.isPresent()) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Продукт по данному ID не найден", "id"));
            return result;
        } else productFromDB = product.get();
        boolean checkInfo = true;
        if (!productFromDB.getName().equals(productDTO.getName())) checkInfo = false;
        if (productFromDB.getPrice() != (Integer.parseInt(productDTO.getPrice()))) checkInfo = false;
        if (!checkInfo) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ERROR_WITH_PRODUCT, "Данные запроса не совпадают с данными о товаре", "fields"));
            return result;
        }
        if (productDTO.getCount() == null) productDTO.setCount("1");
        Client client = clientService.getClientSessions().get(session);
        int deposit = client.getDeposit();
        int productPrice = productFromDB.getPrice();
        int buyCount = Integer.parseInt(productDTO.getCount());
        if (deposit < (productPrice * buyCount)) {
            String message = String.format("Денег на счету = %d, а необходимо %d", deposit, productPrice*buyCount);
            result.getErrors().add(new UserError(UserErrorCode.WRONG_ClIENT_DEPOSIT_TO_LOW, message, "deposit"));
            return result;
        }
        if (productFromDB.getAmount() < Integer.parseInt(productDTO.getCount())) {
            result.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_COUNT, "Не хватает товара на складе", "count"));
            return result;
        }
        int depositNew = client.getDeposit() - productFromDB.getPrice() * Integer.parseInt(productDTO.getCount());
        client.setDeposit(depositNew);
        clientRepository.save(client);
        int countNew = productFromDB.getAmount() - Integer.parseInt(productDTO.getCount());
        productFromDB.setAmount(countNew);
        productRepository.save(productFromDB);
        SoldProducts soldProducts = new SoldProducts();
        soldProducts.setClientId(client.getId());
        soldProducts.setProductId(productFromDB.getId());
        soldProducts.setAmount(Integer.parseInt(productDTO.getCount()));
        soldProducts.setDate(LocalDateTime.now());
        soldProductsRepository.save(soldProducts);
        result = transformProductToDto(productFromDB);
        result.setCategoriesName(null);
        return result;
    }

    @Override
    public List<ProductDTO> getAllProducts(String session, List<Integer> categories, String order) {
        List<ProductDTO> result = new ArrayList<>();
        if (!adminService.isLogin(session) && !clientService.isLogin(session)) {
            ProductDTO product = new ProductDTO();
            product.clearFieldsToGetError();
            product.getErrors().add(new UserError(UserErrorCode.ACCESS_DENIED, "Нет доступа", "SESSIONID"));
            result.add(product);
            return result;
        }
        if (categories == null) {
            List<Product> list = new ArrayList<>();
            list = productRepository.findAll(Sort.by(Sort.Order.asc("name")));
            for (Product product : list) {
                ProductDTO productDTO = transformProductToDto(product);
                productDTO.setErrors(null);
                result.add(productDTO);
            }
        }
        if (categories != null && categories.isEmpty()) {
            List<Product> list = new ArrayList<>();
            list = productRepository.findAll(Sort.by(Sort.Order.asc("name")));
            for (Product product : list) {
                if (product.getCategories() == null || product.getCategories().isEmpty()) {
                    ProductDTO productDTO = transformProductToDto(product);
                    productDTO.setErrors(null);
                    result.add(productDTO);
                }
            }
        }
        if (categories != null && !categories.isEmpty()) {
            List<Product> list = new ArrayList<>();
            list= productRepository.findAll(Sort.by(Sort.Order.asc("name")));
            for (Product product : list) {
                List<Category> checking = product.getCategories();
                for (Category category : checking) {
                    for (Integer id : categories) {
                        if (category.getId().equals(id)) {
                            ProductDTO productDTO = transformProductToDto(product);
                            productDTO.setErrors(null);
                            result.add(productDTO);
                        }
                        break;
                    }
                }
            }
        }
        if (result.isEmpty()) {
            ProductDTO product = new ProductDTO();
            product.clearFieldsToGetError();
            product.getErrors().add(new UserError(UserErrorCode.WRONG_PRODUCT_ID, "Продуктов не найдено", "categories"));
            result.add(product);
            return result;
        }
        switch (order.toLowerCase().trim()){
            case ("product"):
                return result;
            case ("category"):
                result = transformProductsListToListWithOneCategory(result);
                return result;
            default:
                ProductDTO product = new ProductDTO();
                product.clearFieldsToGetError();
                product.getErrors().add(new UserError(UserErrorCode.WRONG_ORDER_NAME, "Ошибка в названии сортировки", "order"));
                result.clear();
                result.add(product);
                return result;
        }
    }

    private List<ProductDTO> transformProductsListToListWithOneCategory(List<ProductDTO> list) {
        List<ProductDTO> result = new ArrayList<>();
        for (ProductDTO productDTO : list) {
            if (productDTO.getCategoriesName() == null || productDTO.getCategoriesName().isEmpty() || productDTO.getCategoriesName().size() < 2) {
                result.add(productDTO);
            }
            else {
                List<String> categories = productDTO.getCategoriesName();
                for (String category : categories) {
                    ProductDTO product = new ProductDTO();
                    product.setName(productDTO.getName());
                    product.setId(productDTO.getId());
                    product.setCount(productDTO.getCount());
                    product.setPrice(productDTO.getPrice());
                    product.setCategoriesName(new ArrayList<>());
                    product.getCategoriesName().add(category);
                    result.add(product);
                }
            }
        }
        Collections.sort(result, new Comparator<ProductDTO>() {
            @Override
            public int compare(ProductDTO o1, ProductDTO o2) {
                if (o1.getCategoriesName().isEmpty()) return -1;
                if (o2.getCategoriesName().isEmpty()) return 1;
                return o1.getCategoriesName().get(0).compareToIgnoreCase(o2.getCategoriesName().get(0));
            }
        });
        return result;
    }

    private ProductDTO transformProductToDto(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId().toString());
        productDTO.setName(product.getName());
        productDTO.setCount(String.valueOf(product.getAmount()));
        productDTO.setPrice(String.valueOf(product.getPrice()));
        productDTO.setCategoriesName(new ArrayList<>());
        if (product.getCategories() != null || !product.getCategories().isEmpty()) {
            List<Category> list = product.getCategories();
            for (Category category : list) {
                productDTO.getCategoriesName().add(category.getName());
            }
        }
        productDTO.setErrors(new ArrayList<>());
        return productDTO;
    }

    private void transformDtoToProduct(ProductDTO productDTO, Product product) {
        if (productDTO.getName() != null) product.setName(productDTO.getName());
        if (productDTO.getPrice() != null) product.setPrice(Integer.parseInt(productDTO.getPrice()));
        if (productDTO.getCount() != null) product.setAmount(Integer.parseInt(productDTO.getCount()));
        if (product.getAmount() <= 0) product.setAmount(0);
        if (productDTO.getCategories() != null){
            product.setCategories(new ArrayList<>());
            List<Integer> categories = productDTO.getCategories();
            for (Integer category : categories) {
                Optional<Category> categoryOptional= categoryRepository.findById(category);
                if (categoryOptional.isPresent() && !product.getCategories().contains(categoryOptional.get())) {
                    product.getCategories().add(categoryOptional.get());
                }
            }
        }
    }
}
