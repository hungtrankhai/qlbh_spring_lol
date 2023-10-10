package com.trankhaihung.cnpm.controller;

import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.entity.WareHouse;
import com.trankhaihung.cnpm.service.ProductService;
import com.trankhaihung.cnpm.service.ShoppingCartService;
import com.trankhaihung.cnpm.service.WareHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class PhoneController {
    @Autowired
    private ProductService productService;

    @Autowired
    private WareHouseService wareHouseService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/")
    public ModelAndView index(Model model){
        ModelAndView mav = new ModelAndView("index");
        List<Product> products = productService.getAllProducts();
        mav.addObject("products", products);

//        List<WareHouse> wareHouses = (List<WareHouse>) wareHouseService.getAvailableQuantity();
//        mav.addObject("wareHouses",wareHouses);

        Integer numeberOfItemsInCart = shoppingCartService.getNumberOfItemsInCart();
        mav.addObject("numberOfItem", numeberOfItemsInCart);

        return mav;
    }
}
