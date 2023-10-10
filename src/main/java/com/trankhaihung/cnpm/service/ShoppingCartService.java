package com.trankhaihung.cnpm.service;

import com.sun.corba.se.spi.ior.ObjectKey;
import com.trankhaihung.cnpm.entity.*;
import com.trankhaihung.cnpm.entity.enums.Status;
import com.trankhaihung.cnpm.repository.CartItemRepository;
import com.trankhaihung.cnpm.repository.ProductRepository;
import com.trankhaihung.cnpm.entity.CartItem;
import com.trankhaihung.cnpm.entity.Customer;
import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.repository.WareHouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {

    private static final Double SHIPPING_PRICE = 2.0;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WareHouseRepository wareHouseRepository;

    public CartItem getCartItemById(Long id) {
        Optional<CartItem> cartItem = cartItemRepository.findById(id);
        return cartItem.orElse(null);
    }

    public List<CartItem> getCartItemsByCustomer(Customer customer) {
        return cartItemRepository.findByCustomer(customer);
    }

    public CartItem getCartItemByCustomerAndProduct(Customer customer, Product product) {
        return cartItemRepository.findByCustomerAndProduct(customer, product);
    }

    public void upsertCartItem(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }

    public List<CartItem> getCartItems() {
        return cartItemRepository.findAll();
    }

    public List<CartItem> getCartItemsIsPending() {
        return cartItemRepository.findAll()
                .stream().filter(cartItem -> cartItem.getStatus().equals(Status.PENDING))
                .collect(Collectors.toList());
    }

    public List<CartItem> getCartItemsOrderedByCustomer() {
        return cartItemRepository.findAll().stream()
                .filter(cartItem -> Objects.nonNull(cartItem.getCustomer())).collect(Collectors.toList());
    }

    public List<CartItem> getCartItemsByStatus(Status status) {
        if (status == null) {
            return cartItemRepository.findAll();
        }
        return cartItemRepository.findAll().stream()
                .filter(cartItem -> cartItem.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public Integer addProductToCart(Long productId, Integer quantity, Customer customer, RedirectAttributes redirectAttributes) {
        Integer addedQuantity = quantity;

        Product product = productRepository.findById(productId).get();

        CartItem cartItem = cartItemRepository.findByCustomerAndProduct(customer, product);

        if (cartItem != null && cartItem.getStatus().equals(Status.PENDING)) {
            addedQuantity = cartItem.getQuantity() + quantity;
            cartItem.setQuantity(addedQuantity);
        }
        else {
            cartItem = new CartItem();
            cartItem.setQuantity(quantity);
            cartItem.setCustomer(customer);
            cartItem.setProduct(product);
            cartItem.setStatus(Status.PENDING);
        }
        cartItemRepository.save(cartItem);

        return  addedQuantity;
    }

    // co van de
    public void addToCart(Long productId) {
        int quantity = 0;         // note
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            CartItem cartItem = cartItemRepository.findByProduct(product.get());
            if (cartItem != null && cartItem.getStatus().equals(Status.PENDING)) {
                cartItem.setProduct(product.get());
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
            } else {
                cartItem = new CartItem();
                cartItem.setProduct(product.get());
                cartItem.setQuantity(quantity);
                cartItem.setStatus(Status.PENDING);
            }

            Product p = product.get();
            if (Objects.nonNull(p.getWareHouse())) {
                WareHouse wareHouse = p.getWareHouse();
                wareHouse.setAvailableQuantity(wareHouse.getAvailableQuantity() - quantity);
                wareHouseRepository.save(wareHouse);
                productRepository.save(p);
            }

            cartItemRepository.save(cartItem);
        }
    }

    //
    public Double getSubTotalPrice(List<CartItem> cartItems) {
        List<Double> totalItems = new ArrayList<>();
        cartItems.forEach(cartItem -> {
            double totalItem = cartItem.getProduct().getPrice() * cartItem.getQuantity();
            totalItems.add(totalItem);
        });
        return totalItems.stream().mapToDouble(Double::doubleValue).sum();
    }

    public Double getTotalPrice(Double subTotal) {
        return subTotal + SHIPPING_PRICE;
    }



    //
    public Integer getNumberOfItemsInCart() {
        return getCartItems().stream()
                .filter(cartItem -> cartItem.getStatus().equals(Status.PENDING))
                .collect(Collectors.toList()).size();
    }

    public ModelAndView getCartItemsPage() {
        List<CartItem> cartItems = getCartItemsByStatus(Status.PENDING);
        ModelAndView mav = new ModelAndView("phone-cart");
        mav.addObject("cartItems", cartItems);
        Double subTotal = getSubTotalPrice(cartItems);
        mav.addObject("subTotal", subTotal);
        Double totalPrice = getTotalPrice(subTotal);
        mav.addObject("shipping", SHIPPING_PRICE);
        mav.addObject("total", totalPrice);
        Integer numeberOfItemsInCart = getNumberOfItemsInCart();
        mav.addObject("numberOfItem", numeberOfItemsInCart);
        return mav;
    }

    public void deleteById(Long id) {
        cartItemRepository.deleteById(id);
    }


//    public List<CartItem> getCartItemsArePaid() {
//        return cartItemRepository.findAll()
//                .stream().filter(cartItem -> cartItem.getStatus().equals(Status.PAID))
//                .collect(Collectors.toList());
//    }


//    public Double calEarning(CartItem cartItem){
//            Double earning = (cartItem.getProduct().getPrice() - cartItem.getProduct().getOriginalPrice() ) * cartItem.getQuantity();
//            return earning;
//    }

}
