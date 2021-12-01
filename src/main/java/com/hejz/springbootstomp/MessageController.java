package com.hejz.springbootstomp;


import com.hejz.springbootstomp.dto.Message;
import com.hejz.springbootstomp.dto.ResponseMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

/**
 * 消息控制器
 */
@Controller
public class MessageController {

    /**
     * 公共信息
     * @param message
     * @return
     * @throws InterruptedException
     */
    @MessageMapping("/message")
    @SendTo("/topic/message")
    public ResponseMessage message(final Message message) throws InterruptedException {
        //模拟等待一秒
        Thread.sleep(1000L);
        //把获取到的message中的信息发送给客户端topic
        return new ResponseMessage(HtmlUtils.htmlEscape(message.getContent()));
    }

    /**
     * 私信——访问服务器返回信息
     * @param message
     * @return
     * @throws InterruptedException
     */
    @MessageMapping("/privateMessage")
    @SendToUser("/topic/privateMessage")
    public ResponseMessage privateMessage(final Principal principal, final Message message) throws InterruptedException {
        //模拟等待一秒
        Thread.sleep(1000L);
        //把获取到的message中的信息发送给客户端topic
        return new ResponseMessage("用户："+principal.getName()+"发送的信息："+HtmlUtils.htmlEscape(message.getContent()));
    }
}
