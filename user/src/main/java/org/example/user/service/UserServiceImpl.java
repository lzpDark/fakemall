package org.example.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.domain.User;
import org.example.common.utils.TimeUtils;
import org.example.user.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.sql.Time;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author : lzp
 * @date :
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserDao userDao;

    @Autowired
    private StringRedisTemplate template = null;

    @Autowired
    ObjectMapper mapper;

    @Override
    public boolean checkUserName(String userName) {
        User user = userDao.selectUser(userName);
        return user != null;
    }

    @Override
    public void registerUser(User user, Errors errors, String pwd2, String validStr, String token) throws Exception {

        if (!user.getUserPassword().equals(pwd2)) {
            throw new Exception("password err");
        }

        if (StringUtils.isEmpty(validStr)) {
            throw new Exception("valid code cannot be empty");
        }

        if (!validStr.equalsIgnoreCase(template.opsForValue().get(token))) {
            throw new Exception("valid code err");
        }

        List<FieldError> fieldErrors = errors.getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            throw new Exception(fieldErrors.get(0).getDefaultMessage());
        }

        user.setUserId(UUID.randomUUID().toString());

        try {
            userDao.insertUser(user);
            // remove valid code token
            template.delete(TimeUtils.getBareKey(token));
            template.delete(token);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("user already exist");
        }

    }

    @Override
    public String loginUser(User user, String validStr, String token) throws Exception {
        String validCode = template.opsForValue().get(token);
        if(validCode == null || !validCode.equalsIgnoreCase(validStr)) {
            throw new Exception("valid code err");
        }

        User userInDb = userDao.selectUser(user.getUserName());
        if (userInDb == null) {
            return null;
        }

        String ticket = "EM_TICKET_" + userInDb.getUserName();
        String value = mapper.writeValueAsString(userInDb);
        template.opsForHash().put(ticket, "", value);
        template.opsForHash().put(ticket, "timestamp", TimeUtils.getTimestamp());
        template.expire(ticket, 30, TimeUnit.SECONDS);
        template.delete(token);
        return ticket + template.opsForHash().get(ticket, "timestamp");
    }

    @Override
    public String loginState(String ticket) {
        String key = TimeUtils.getBareKey(ticket);
        String timestamp = TimeUtils.getTimePart(ticket);
        if (timestamp != null && timestamp.equals(template.opsForHash().get(key, "timestamp"))) {
            return (String)template.opsForHash().get(key, "data");
        }else{
            return null;
        }
    }

    @Override
    public void deleteTicket(String ticket) {
        String key = TimeUtils.getBareKey(ticket);
        template.delete(key);
    }

    @Override
    public String loginUser(User user) throws Exception {
        String key = "AUTO_LOGIN_" + user.getUserName();
        String value = String.valueOf(System.currentTimeMillis());
        template.opsForList().leftPush(key, value);
        template.expire(key, 30, TimeUnit.SECONDS);

        List<String> lst = template.opsForList().range(key, 0, -1);
        if(null == lst || lst.isEmpty()) {
            return null;
        }

        if(lst.size() > 1) {
            String oldValue = template.opsForList().rightPop(key);
            if((null == oldValue || Long.parseLong(value) - Long.parseLong(oldValue) < 500000)) {
                return null;
            }
        }

        User userInDb = userDao.selectUser(user.getUserName());
        if (null == userInDb) {
            throw new Exception("info err");
        }

        String ticket = "EM_TICKET_" + userInDb.getUserName();
        String data = mapper.writeValueAsString(userInDb);

        template.opsForHash().put(ticket, "data", data);
        template.opsForHash().put(ticket, "timestamp", TimeUtils.getTimestamp());
        template.expire(ticket, 30, TimeUnit.SECONDS);

        return ticket + template.opsForHash().get(ticket, "timestamp");
    }

    @Override
    public Integer queryUserType(String ticket) throws Exception {
        ticket = TimeUtils.getBareKey(ticket);
        String data = (String)template.opsForHash().get(ticket, "data");
        if(null == data) {
            return null;
        }
        try {
            User user = mapper.readValue(data, User.class);
            return user.getUserType();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new Exception("data format err");
        }
    }
}
