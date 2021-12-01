package com.hejz.springbootstomp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WsController {
    @Autowired
    private WsService wsService;

    @PostMapping("sendMessage")
    public void sendMessage(String message){
        wsService.notify(message);
    }
}
