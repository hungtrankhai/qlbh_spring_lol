package com.trankhaihung.cnpm.controller;

import com.trankhaihung.cnpm.dto.CartResponseDTO;
import com.trankhaihung.cnpm.dto.ProductDTO;
import com.trankhaihung.cnpm.entity.CartItem;
import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.entity.WareHouse;
import com.trankhaihung.cnpm.entity.enums.Status;
import com.trankhaihung.cnpm.repository.WareHouseRepository;
import com.trankhaihung.cnpm.service.AuthenticationService;
import com.trankhaihung.cnpm.service.ProductService;
import com.trankhaihung.cnpm.service.ShoppingCartService;
import com.trankhaihung.cnpm.util.FileUploadUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/management")
public class ManagementController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private WareHouseRepository wareHouseRepository;

//    private Status pending_status = Status.PENDING;

    @GetMapping("/product-list/{pageNumber}")
    public String getProducts(Model model, @PathVariable("pageNumber") int pageNumber) {
        boolean authenticated = authenticationService.isAuthenticated();

        if(!authenticated) {
            return "redirect:/login-page";
        }
        Page<Product> productPage = productService.getProductsInWarehouse(pageNumber);
        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTOS = new ArrayList<>();

        for (Product product : products) {
            ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
            if (Objects.nonNull(product.getWareHouse())) {
                productDTO.setQuantity(product.getWareHouse().getQuantity());
                productDTO.setAvailableQuantity(product.getWareHouse().getAvailableQuantity());
            }
            productDTOS.add(productDTO);
        }
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("products", productDTOS);
        return "product/list-product";
    }

    @GetMapping("/create-product")
    public String showCreateProductForm(Product product) {
        if(!authenticationService.isAuthenticated()) {
            return "redirect:/login-page";
        }
        return "product/create-product";
    }

    @PostMapping("/submit-create-product")
    public String createProduct(@Valid Product product, @RequestParam("image") MultipartFile multipartFile, BindingResult result) throws IOException {
        if(!authenticationService.isAuthenticated()) {
            return "redirect:/login-page";
        }
        if (result.hasErrors()) {
            return "product/create-product";
        }
        String productImage = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        product.setImageName(productImage);

        String uploadDir = "product-photos/";
        FileUploadUtils.saveFile(uploadDir, productImage, multipartFile);

        Product upsert = productService.upsert(product);

        WareHouse wareHouse = new WareHouse();
        wareHouse.setProduct(upsert);
        wareHouse.setAvailableQuantity(product.getAvailableQuantity());
        wareHouse.setQuantity(product.getQuantity());
        wareHouseRepository.save(wareHouse);

        return "redirect:/management/product-list/1";
    }

    @GetMapping("/show-update-product/{id}")
    public String showUpdateProductForm(@PathVariable("id") Long id, Model model) {
        if(!authenticationService.isAuthenticated()) {
            return "redirect:/login-page";
        }
        Optional<Product> product = productService.getProductById(id);

        if (!product.isPresent()) {
            return "Invalid";
        }

        Product p = product.get();
        if (Objects.nonNull(p.getWareHouse())) {
            p.setQuantity(p.getWareHouse().getQuantity());
            p.setAvailableQuantity(p.getWareHouse().getAvailableQuantity());
        }

        model.addAttribute("product", p);
        return "product/update-product";
    }

    @PostMapping("/update-product/{id}")
    public String updateProduct(@PathVariable("id") Long id, @Valid Product product, @RequestParam("image") MultipartFile multipartFile, BindingResult result) throws IOException {
        if(!authenticationService.isAuthenticated()) {
            return "redirect:/login-page";
        }
        if (result.hasErrors()) {
            return "update-product";
        }

        Optional<Product> p = productService.getProductById(id);

        if (Objects.nonNull(p)) {
            String productImage = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            if(!StringUtils.isEmpty(productImage)) {
                product.setImageName(productImage);
                String uploadDir = "product-photos/";
                FileUploadUtils.saveFile(uploadDir, productImage, multipartFile);

                if (Objects.nonNull(product.getWareHouse()) && p.isPresent()) {
                    WareHouse wareHouse = product.getWareHouse();
                    wareHouse.setProduct(p.get());
                    wareHouse.setQuantity(product.getQuantity());
                    wareHouse.setAvailableQuantity(product.getAvailableQuantity());
                    wareHouseRepository.save(wareHouse);
                }
                productService.upsert(product);
                return "redirect:/management/product-list/1";
            }

            if (Objects.nonNull(product.getWareHouse()) && p.isPresent()) {
                WareHouse wareHouse = product.getWareHouse();
                wareHouse.setProduct(p.get());
                wareHouse.setQuantity(product.getQuantity());
                wareHouse.setAvailableQuantity(product.getAvailableQuantity());
                wareHouseRepository.save(wareHouse);
            }
            productService.upsert(product);
            return "redirect:/management/product-list/1";
        }
        return "Invalid";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        if(!authenticationService.isAuthenticated()) {
            return "redirect:/login-page";
        }
        Optional<Product> product = productService.getProductById(id);

        if (!product.isPresent()) {
            return "Invalid";
        }

        productService.deleteProduct(product.get());
        return "redirect:/management/product-list/1";
    }

    @GetMapping("/orders")
    public ModelAndView getOrders() {
        if(!authenticationService.isAuthenticated()) {
            return new ModelAndView("redirect:/login-page");
        }
        ModelAndView mav = new ModelAndView("product/order-list");

        List<CartItem> cartItems = shoppingCartService.getCartItemsOrderedByCustomer();
        mav.addObject("service", shoppingCartService);
        mav.addObject("cartItems", cartItems);

        return mav;
    }


    @GetMapping("/pending-orders")
    public ModelAndView getPendingOrders(){
        if(!authenticationService.isAuthenticated()) {
            return new ModelAndView("redirect:/login-page");
        }
        ModelAndView mav = new ModelAndView("product/pending-order-list");
        List<CartItem> cartItems = shoppingCartService.getCartItemsByStatus(Status.PENDING);
        mav.addObject("service", shoppingCartService);
        mav.addObject("cartItems", cartItems);
        return mav;
    }


    @GetMapping("/revenue")
    public ModelAndView getRevenue(){
        if(!authenticationService.isAuthenticated()) {
            return new ModelAndView("redirect:/login-page");
        }

        ModelAndView mav = new ModelAndView("product/revenue");
        List<CartItem> cartItemsArePaid = shoppingCartService.getCartItemsByStatus(Status.PAID);
        Double totalRevenue = 0.0;

        for (CartItem c: cartItemsArePaid) {
            totalRevenue += c.getRevenue();
        }

        mav.addObject("totalRevenue",totalRevenue);
        mav.addObject("service", shoppingCartService);
        mav.addObject("cartItems", cartItemsArePaid);
        return mav;
    }





    @GetMapping("/earning")
    public ModelAndView getEarning(){
        if(!authenticationService.isAuthenticated()) {
            return new ModelAndView("redirect:/login-page");
        }

        ModelAndView mav = new ModelAndView("product/earning");
        List<CartItem> cartItemsArePaid = shoppingCartService.getCartItemsByStatus(Status.PAID);
        Double totalEarning = 0.0;

        for (CartItem c: cartItemsArePaid) {
            totalEarning += c.getEarning();
        }

        mav.addObject("totalEarning",totalEarning);
        mav.addObject("service", shoppingCartService);
        mav.addObject("cartItems", cartItemsArePaid);
        return mav;
    }



}