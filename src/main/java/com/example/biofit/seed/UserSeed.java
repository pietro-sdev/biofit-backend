package com.example.biofit.seed;

import com.example.biofit.model.User;
import com.example.biofit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UserSeed {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            seedUsers();
        }
    }

    private void seedUsers() {
        User user1 = new User();
        user1.setPassword(bCryptPasswordEncoder.encode("admin1234"));
        user1.setEmail("admin@admin.com");
        userRepository.save(user1);
    }
}
