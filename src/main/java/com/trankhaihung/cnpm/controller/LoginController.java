package com.trankhaihung.cnpm.controller;

import com.trankhaihung.cnpm.dto.UserRegisterDTO;
import com.trankhaihung.cnpm.entity.Role;
import com.trankhaihung.cnpm.entity.User;
import com.trankhaihung.cnpm.entity.UserLogin;
import com.trankhaihung.cnpm.entity.enums.RoleName;
import com.trankhaihung.cnpm.repository.UserRepository;
import com.trankhaihung.cnpm.security.JwtTokenProvider;
import com.trankhaihung.cnpm.service.AuthenticationService;
import com.trankhaihung.cnpm.service.UserLoginService;
import com.trankhaihung.cnpm.util.CommonConstant;
import com.trankhaihung.cnpm.util.CookieUtils;
import com.trankhaihung.cnpm.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Controller
public class LoginController {
    private static final String JWT_TOKEN_NAME = "jwtToken";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private ObjectUtils objectUtils;

    @GetMapping("/login-page")
    public String login() {
        return "login-page";
    }

//    @GetMapping("/")
//    public ModelAndView index(Model model){
//        ModelAndView mav = new ModelAndView("index");
//        List<UserLogin> userLogins = userLoginService.getUserLogins();
//
//        Optional<UserLogin> userLogin = userLogins.stream().findFirst();
//
//        userLogin.ifPresent(user -> mav.addObject("userLogin", user.getFirstName().concat(CommonConstant.EMPTY_STRING)
//                                                                                    .concat(user.getLastName())));
//
//        return mav;
//    }

    @GetMapping("/test")
    public ModelAndView testlogin() {
        ModelAndView mav = new ModelAndView("test");
        List<UserLogin> userLogins = userLoginService.getUserLogins();

        Optional<UserLogin> userLogin = userLogins.stream().findFirst();

        userLogin.ifPresent(user -> mav.addObject("userLogin", user.getFirstName().concat(CommonConstant.EMPTY_STRING)
                .concat(user.getLastName())));

        return mav;
    }

    @PostMapping("/login")
    @Transactional
    public String doLogin(@RequestParam("email") String email,
                          @RequestParam("password") String password, HttpServletResponse response, HttpServletRequest request,
                          RedirectAttributes redirectAttributes){
        Optional<User> users = userRepository.findByEmail(email);

        if(!users.isPresent() || !passwordEncoder.matches(password ,users.get().getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid username password !!!");
            return "redirect:/login-page";
        }

        Authentication authentication = authenticationService.setAuthentication(users.get(), request);

        String jwtToken = tokenProvider.generateToken(authentication);
        CookieUtils.addCookie(response, JWT_TOKEN_NAME, jwtToken);

        if (authenticationService.getAuthentication() != null) {
            return "redirect:/management/product-list/1";
        }

        return "redirect:/login-page";
    }

    @GetMapping("/doLogout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationService.getAuthentication();

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            CookieUtils.deleteCookie(request, response, JWT_TOKEN_NAME);
            return "redirect:/login-page?logout";
        }
        return "redirect:/management/product-list?logout-failed";
    }

    @PostMapping("/register")
    @Transactional
    public String doRegister(@ModelAttribute("userRegister") UserRegisterDTO userRegisterDTO, RedirectAttributes redirectAttributes){
        if (userRegisterDTO.getEmail().isEmpty() || userRegisterDTO.getPassword().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please input email and password !!!");
            return "redirect:/register-page";
        }
        if(userRepository.existsByUsername(userRegisterDTO.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username is exist !!!");
            return "redirect:/register-page";
        }

        if (!userRegisterDTO.getConfirmPassword().equals(userRegisterDTO.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be match !!!");
            return "redirect:/register-page";
        }

        // Creating user's account
        User user = new User(userRegisterDTO.getName(), userRegisterDTO.getUsername(),
                userRegisterDTO.getEmail(), userRegisterDTO.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = new Role();
        role.setName(RoleName.ROLE_ADMIN);
        user.getRoles().add(role);

        userRepository.save(user);

        return "redirect:/login-page";
    }

    @GetMapping("/register-page")
    public ModelAndView viewRegisterPage() {
        ModelAndView mav = new ModelAndView("register-admin");
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        mav.addObject("userRegister", userRegisterDTO);
        return mav;
    }

    @GetMapping("/403")
    public ModelAndView accessDenied() {
        ModelAndView mav = new ModelAndView("403");
        User loginedUser = authenticationService.getLoginedUser();
        String userInfo = objectUtils.mapUserToString(loginedUser);
        String message = "Hi " + loginedUser.getName()
                + "<br> You do not have permission to access this page!";
        mav.addObject("userInfo", userInfo);
        mav.addObject("message", message);
        return mav;
    }
}
