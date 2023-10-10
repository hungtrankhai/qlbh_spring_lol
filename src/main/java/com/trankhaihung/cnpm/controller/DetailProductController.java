package com.trankhaihung.cnpm.controller;


import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/detail")
public class DetailProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/detail-product/{id}")
    public String getProductDetail(@PathVariable("id") Long id, Model model){
        Optional<Product> product = productService.getProductById(id);

        Product p = product.get();

        model.addAttribute("product", p);

        if (!product.isPresent()) {
            return "Invalid";
        }
        return "phone-fragment/detail-product";
    }
}
