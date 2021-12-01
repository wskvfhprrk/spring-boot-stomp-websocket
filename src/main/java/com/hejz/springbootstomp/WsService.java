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

    /**公共信息通道
     *
     * @param message 发送信息
     */
    public void notify(String message){
        ResponseMessage responseMessage=new ResponseMessage(message);
        template.convertAndSend("/topic/message",responseMessage);
    }

    /**
     * 私信通道
     * @param id 客户端id
     * @param message 发送信息
     */
    public void privateNotify(String id,String message){
        ResponseMessage responseMessage=new ResponseMessage(message);
        //注：方法使用ToUser,此处路径不加user
        template.convertAndSendToUser(id,"/topic/privateMessage",responseMessage);
    }
}
