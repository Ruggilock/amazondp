package servicedemo.demo.service.impl;


import org.springframework.stereotype.Service;
import servicedemo.demo.service.AuthService;


@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public String loginStudent(String email, String password) {

        if (email.compareTo("jen")==0 && password.compareTo("jen")==0) {
            return email;
        } else {
            return "fail";
        }
    }


}
