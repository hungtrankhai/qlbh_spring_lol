package com.trankhaihung.cnpm.service;

import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.entity.WareHouse;
import com.trankhaihung.cnpm.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService{

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getProducts() {

        return productRepository.getAvailableProducts();
    }

    public List<Product> getAllProducts() {

        return productRepository.getAllProduct();
    }

    public Page<Product> getProductsInWarehouse(int pageNumber) {
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        return productRepository.findAll(pageable);
    }


    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product upsert(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }


}
