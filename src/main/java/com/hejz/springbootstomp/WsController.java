package com.hejz.springbootstomp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WsController {
    @Autowired
    private Notificationservice notificationservice;
//    private WsService wsService;

    @PostMapping("sendMessage")
    public void sendMessage(String message){
//        wsService.notify(message);
        notificationservice.gloubNotificationservice(message);
    }
    @PostMapping("sendPrivateMessage")
    public void sendMessage(String id,String message){
//        wsService.privateNotify(id,message);
        notificationservice.privateNotificationservice(id,message);

    }
}
