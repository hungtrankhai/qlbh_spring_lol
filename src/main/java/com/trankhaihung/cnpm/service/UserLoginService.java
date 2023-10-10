package com.trankhaihung.cnpm.service;

import com.trankhaihung.cnpm.entity.UserLogin;
import com.trankhaihung.cnpm.entity.enums.AuthProvider;
import com.trankhaihung.cnpm.repository.UserLoginRepository;
import com.trankhaihung.cnpm.security.oauth2.userInfo.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserLoginService {
    @Autowired
    private UserLoginRepository userLoginRepository;

    public void processOAuthPostLogin(CustomOAuth2User user) {
        UserLogin existUser = userLoginRepository.getUserLoginByUsername(user.getEmail());

        Map<String, Object> attributes = user.getAttributes();

        String first_name = (String) attributes.get("first_name");
        String last_name = (String) attributes.get("last_name");
        String pictureUrl = (String) ((LinkedHashMap)(((LinkedHashMap) attributes.get("picture"))).get("data")).get("url");

        if (existUser == null) {
            userLoginRepository.deleteAll();
            UserLogin userLogin = new UserLogin();
            userLogin.setUsername(user.getEmail());
            userLogin.setFirstName(first_name);
            userLogin.setLastName(last_name);
            userLogin.setProvider(AuthProvider.FACEBOOK);
            userLogin.setImageUrl(pictureUrl);
            userLoginRepository.save(userLogin);
        }

        existUser.setLastName(last_name);
        existUser.setFirstName(first_name);
        existUser.setImageUrl(pictureUrl);
        userLoginRepository.save(existUser);
    }

    public List<UserLogin> getUserLogins() {
        return userLoginRepository.findAll();
    }
}
