package com.hejz.springbootstomp;

import com.sun.security.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * 自定义握手——添加各自id等应用
 */
@Slf4j
public class Userhandshakehandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes){
        //编号可以实为客户id或者token值
        final String id= UUID.randomUUID().toString().replaceAll("-","");
        log.info("登陆用户ID:{}",id);
        return new UserPrincipal(id);
    }
}