package org.example.user.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author : lzp
 * @date :
 */
@Component
@Aspect
public class LoginAspect {

    @Autowired
    private StringRedisTemplate template = null;


    @AfterReturning(value = "execution(* org.example.user.service.UserService*.loginState(..))", returning = "result")
    public void extendTime(JoinPoint jp, Object result){

        if(StringUtils.isEmpty(result)) {
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        Object[] args = jp.getArgs();
        String[] names = ((CodeSignature) jp.getSignature()).getParameterNames();
        for (int i = 0; i<names.length; i++) {
            map.put(names[i], args[i]);
        }
        String ticket = (String)map.get("ticket");
        template.expire(ticket, 30, TimeUnit.SECONDS);
    }

}
