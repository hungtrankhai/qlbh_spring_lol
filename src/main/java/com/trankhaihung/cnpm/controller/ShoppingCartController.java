package com.trankhaihung.cnpm.controller;

import com.trankhaihung.cnpm.dto.CartResponseDTO;
import com.trankhaihung.cnpm.dto.CustomerDTO;
import com.trankhaihung.cnpm.entity.CartItem;
import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.entity.WareHouse;
import com.trankhaihung.cnpm.entity.enums.Status;
import com.trankhaihung.cnpm.repository.WareHouseRepository;
import com.trankhaihung.cnpm.service.CustomerService;
import com.trankhaihung.cnpm.service.ProductService;
import com.trankhaihung.cnpm.service.ShoppingCartService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/cart")
public class ShoppingCartController {
    private static final Double SHIPPING_PRICE = 2.0;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private WareHouseRepository wareHouseRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/detail")
    public ModelAndView getCartItems(CustomerDTO customerDTO) {
        return shoppingCartService.getCartItemsPage();
    }

    @GetMapping("/add-to-cart/{id}")
    public ModelAndView addToCart(@PathVariable("id") Long productId, CustomerDTO customerDTO) {
        shoppingCartService.addToCart(productId);
        return shoppingCartService.getCartItemsPage();
    }

    @GetMapping("/delete-cart-item/{id}")
    public ModelAndView deleteCartItem(@PathVariable("id") Long cartItemId, CustomerDTO customerDTO) throws NotFoundException {
        CartItem cartItem = shoppingCartService.getCartItemById(cartItemId);
        if (cartItem == null) {
            throw new NotFoundException("CartItem not found");
        }
        shoppingCartService.deleteById(cartItemId);
        return shoppingCartService.getCartItemsPage();
    }

//    @GetMapping("/contact")
//    public ModelAndView showFormContact(CustomerDTO customerDTO) {
//        ModelAndView mav = new ModelAndView("cycle-contact");
//        List<CartItem> cartItems = shoppingCartService.getCartItemsByStatus(Status.PENDING);
//        mav.addObject("cartItems", cartItems);
//        return mav;
//    }

    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    @PostMapping("/update-quantity/{id}/{quantity}")
    @ResponseBody
    public ResponseEntity<CartResponseDTO> updateQuantity(@PathVariable("id") Long id, @PathVariable("quantity") Integer quantity, RedirectAttributes redirectAttributes) {
        CartItem cartItem = shoppingCartService.getCartItemById(id);

        //check
        if (cartItem.getProduct().getAvailableQuantity() < quantity){
            throw new RuntimeException("Số lượng sản phẩm ko hợp lệ! ");
//            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng ko hợp lệ !!!");
        }

        cartItem.setQuantity(quantity);
        shoppingCartService.upsertCartItem(cartItem);

        Product product = cartItem.getProduct();
        WareHouse wareHouse = product.getWareHouse();

        if (Objects.nonNull(wareHouse)) {
            wareHouse.setAvailableQuantity(wareHouse.getAvailableQuantity() - quantity);
            wareHouseRepository.save(wareHouse);
            productService.upsert(product);
        }

        Double subPrice = shoppingCartService.getSubTotalPrice(shoppingCartService.getCartItemsIsPending());
        CartResponseDTO cartResponse = new CartResponseDTO();
        cartResponse.setSubTotal(subPrice);
        cartResponse.setShipPrice(SHIPPING_PRICE);

        return ResponseEntity.ok(cartResponse);
    }
}