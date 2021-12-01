package com.hejz.springbootstomp;

import com.hejz.springbootstomp.dto.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 服务器端websoket——可直接用于生产
 */
@Service
public class Notificationservice {

    private final SimpMessagingTemplate template;

    @Autowired
    private Notificationservice(SimpMessagingTemplate template){
        this.template=template;
    }

    public void gloubNotificationservice(String message){
        ResponseMessage responseMessage=new ResponseMessage(message);
        template.convertAndSend("/topic/message",responseMessage);
    }
    public void privateNotificationservice(String id,String message){
        ResponseMessage responseMessage=new ResponseMessage(message);
        template.convertAndSendToUser(id,"/topic/message",responseMessage);
    }
}
