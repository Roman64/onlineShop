package net.thumbtack.onlineshop.data.repository;

import net.thumbtack.onlineshop.data.model.SoldProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface SoldProductsRepository extends PagingAndSortingRepository<SoldProducts, Integer> {
    ArrayList<SoldProducts> findByProductIdIsIn(List<Integer> ids, Sort sort);
    Page<SoldProducts> findAll(Pageable pageable);
    Page<SoldProducts> findAllByClientId(Integer clientId, Pageable pageable);
    ArrayList<SoldProducts> findByDateBetween(LocalDateTime dateTime, LocalDateTime dateTimeNext);
}
