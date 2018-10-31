package servicedemo.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import servicedemo.demo.service.AuthService;
import servicedemo.demo.view.request.LoginView;

import javax.validation.Valid;

@RestController
public class Auth {

    @Autowired
    private AuthService authService;

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String loginStudent(@Valid @RequestBody LoginView request) {
        return authService.loginStudent(request.getEmail(), request.getPassword());
    }
}
