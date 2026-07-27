package com.homefix.servicecatalogservice.repository;

import com.homefix.servicecatalogservice.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByCategoryId(Long categoryId);

    @Query("SELECT s FROM ServiceItem s WHERE " +
           "(:categoryId IS NULL OR s.categoryId = :categoryId) AND " +
           "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ServiceItem> searchServices(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword);
}
