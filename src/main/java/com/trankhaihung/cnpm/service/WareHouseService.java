package com.trankhaihung.cnpm.service;


import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.entity.WareHouse;
import com.trankhaihung.cnpm.repository.ProductRepository;
import com.trankhaihung.cnpm.repository.WareHouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WareHouseService {


    @Autowired
    private WareHouseRepository wareHouseRepository;

    public List<WareHouse> getProductsInWareHouse() {
        return wareHouseRepository.findAll();
    }

//    public WareHouse getAvailableQuantity(){
//        return wareHouseRepository.getAvailableQuantity();
//    }

}
