package com.trankhaihung.cnpm.repository;

import com.trankhaihung.cnpm.entity.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, Long> {

//    @Query(value = "SELECT ware_house.available_quantity FROM alandung.ware_house"+
//            "JOIN alandung.product ON ware_house.product_id = product.id",nativeQuery = true)
//    WareHouse getAvailableQuantity();



}
