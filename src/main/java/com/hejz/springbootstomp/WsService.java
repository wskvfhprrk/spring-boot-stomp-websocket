package com.hejz.springbootstomp;

import com.hejz.springbootstomp.dto.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsService {

    private SimpMessagingTemplate template;

    @Autowired
    public WsService(SimpMessagingTemplate template){
        this.template=template;
    }

    public void notify(String message){
        ResponseMessage responseMessage=new ResponseMessage(message);
        template.convertAndSend("/topic/message",responseMessage);
    }
}
