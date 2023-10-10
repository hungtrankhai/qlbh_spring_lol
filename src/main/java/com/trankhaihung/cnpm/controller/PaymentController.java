package com.trankhaihung.cnpm.controller;

import com.trankhaihung.cnpm.dto.CustomerDTO;
import com.trankhaihung.cnpm.entity.CartItem;
import com.trankhaihung.cnpm.entity.Customer;
import com.trankhaihung.cnpm.entity.enums.Status;
import com.trankhaihung.cnpm.repository.CustomerRepository;
import com.trankhaihung.cnpm.service.PayPalService;
import com.trankhaihung.cnpm.service.ShoppingCartService;
import com.trankhaihung.cnpm.util.CommonConstant;
import com.trankhaihung.cnpm.util.PayPalPaymentIntent;
import com.trankhaihung.cnpm.util.PayPalPaymentMethod;
import com.trankhaihung.cnpm.util.PayPalUtils;
import com.trankhaihung.cnpm.util.mapper.CustomerMapper;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
public class PaymentController {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String URL_PAYPAL_SUCCESS = "pay/success";
    private static final String URL_PAYPAL_CANCEL = "pay/cancel";
    private static final Double SHIPPING_PRICE = 2.0;
    private static Customer customer;

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @PostMapping("/pay")
    public String pay(HttpServletRequest request, @Valid CustomerDTO customerDTO) {
        customer = customerMapper.toEntity(customerDTO);
        customerRepository.save(PaymentController.customer);
        List<CartItem> cartItems = shoppingCartService.getCartItemsByStatus(Status.PENDING);
        Double totalPrice = shoppingCartService.getSubTotalPrice(cartItems);
        Double total = totalPrice + SHIPPING_PRICE;
        String cancelUrl = PayPalUtils.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
        String successUrl = PayPalUtils.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
        String paymentDescription = CommonConstant.PAYMENT_DESCRIPTION + totalPrice
                + CommonConstant.ONE_SPACE + CommonConstant.CURRENCY;
        try {
            Payment payment = payPalService.createPayment(
                    total,
                    CommonConstant.CURRENCY,
                    PayPalPaymentMethod.PAYPAL,
                    PayPalPaymentIntent.SALE,
                    paymentDescription,
                    cancelUrl,
                    successUrl);
            for(Links links : payment.getLinks()){
                if(links.getRel().equals("approval_url")){
                    return "redirect:" + links.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            LOGGER.error(e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("pay/cancel")
    public String cancelPay(){
        List<CartItem> cartItems = shoppingCartService.getCartItemsByStatus(Status.PENDING);
        cartItems.forEach(cartItem -> {
            cartItem.setCustomer(customer);
            cartItem.setStatus(Status.UNPAID);
            shoppingCartService.upsertCartItem(cartItem);
        });
        return "cancel";
    }

    @GetMapping("pay/success")
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            if(payment.getState().equals("approved")){
                List<CartItem> cartItems = shoppingCartService.getCartItemsByStatus(Status.PENDING);
                cartItems.forEach(cartItem -> {
                    cartItem.setCustomer(customer);
                    cartItem.setStatus(Status.PAID);
                    shoppingCartService.upsertCartItem(cartItem);
                });
                return "success";
            }
        } catch (PayPalRESTException e) {
            LOGGER.error(e.getMessage());
        }
        return "redirect:/";
    }
}
