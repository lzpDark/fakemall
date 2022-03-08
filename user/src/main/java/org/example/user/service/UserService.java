package org.example.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.common.domain.User;
import org.springframework.validation.Errors;

/**
 * @author : lzp
 * @date :
 */
public interface UserService {

    boolean checkUserName(String userName);

    void registerUser(User user, Errors errors, String pwd2, String validStr, String token) throws Exception;

    String loginUser(User user, String validStr, String token) throws Exception;

    String loginState(String ticket);

    void deleteTicket(String ticket);

    String loginUser(User user) throws Exception;

    Integer queryUserType(String ticket) throws Exception;


}
