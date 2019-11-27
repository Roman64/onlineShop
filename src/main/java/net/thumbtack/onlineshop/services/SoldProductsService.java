package net.thumbtack.onlineshop.services;

import net.thumbtack.onlineshop.data.dto.SoldProductsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface SoldProductsService {
    List<SoldProductsDTO> getSoldProducts(String session, List<Integer> products, List<Integer>  categories,
                                          String date, String order);

    List<SoldProductsDTO> getSoldProductsByClient(String session, Integer clientId);
    List<SoldProductsDTO> getSoldProductsBetweenDate(String session, String dateFirst, String dateSecond);
}
