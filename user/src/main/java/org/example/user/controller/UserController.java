package org.example.user.controller;

import org.example.common.domain.User;
import org.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author : lzp
 * @date :
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/checkUserName")
    public Object checkUserName(@RequestParam("userName") String userName) {
        boolean b = userService.checkUserName(userName);
        return "" + b;
    }

    @PostMapping("/register")
    public Object registerUser(@Valid User user,
                               @RequestParam("pwd2") String pwd2,
                               @RequestParam("validStr") String validStr,
                               @RequestParam("token") String token,
                               Errors errors) {

        try {
            userService.registerUser(user, errors, pwd2, validStr, token);
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @PostMapping("/login")
    public Object login(User user,
                        @RequestParam() String validStr,
                        @RequestParam("token") String token) {

        try {
            String ticket = userService.loginUser(user, validStr, token);
            if (StringUtils.isEmpty(ticket)) {
                return "no such user";
            } else{
                // todo set cookie
                return "ok";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    @PostMapping("/autologin")
    public Object autoLogin(User user,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        try {
            String ticket = userService.loginUser(user);
            if (StringUtils.isEmpty(ticket)) {
                return "err";
            }else{
                // todo set cookie
                return "ok";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    @GetMapping("/loginState/{ticket}")
    public Object getLoginState(@PathVariable("ticket") String ticket) {
        String user = userService.loginState(ticket);
        if (null == user) {
            // todo delete cookie
            return "not login";
        }else{
            return "ok";
        }
    }


    @GetMapping("/logout")
    public Object logout(@CookieValue("EM_TICKET") String ticket) {
        try {
            userService.deleteTicket(ticket);
            // todo delete cookie
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("/userType")
    public Integer getUserType(@RequestParam("ticket") String ticket) {
        try {
            return userService.queryUserType(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
