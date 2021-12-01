# spring-boot-stomp-websocket
spring boot 实现stomp的websocket通信
## 配置
### 1、引用jar包
```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>webjars-locator-core</artifactId>
            <version>0.48</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>sockjs-client</artifactId>
            <version>1.5.1</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>stomp-websocket</artifactId>
            <version>2.3.4</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>bootstrap</artifactId>
            <version>3.3.7</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>jquery</artifactId>
            <version>3.1.1-1</version>
        </dependency>
```
### 2、配置webSocket配置文件WebSocketConfig
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * 注册Stomp端点
     * @param registry
     */
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        //添加端点
        registry.addEndpoint("our-websocket").withSockJS();
    }

    /**
     * 配置消息代理
     * @param registry
     */
    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        //启用简单代理
        registry.enableSimpleBroker("/topic");
        //设置应用程序目标前缀
        registry.setApplicationDestinationPrefixes("ws");
    }
}
```
### 3、编写消息控制器MessageController
```java
/**
 * 消息控制器
 */
@Controller
public class MessageController {

    @MessageMapping("/message")
    @SendTo("/topic/message")
    public ResponseMessage message(final Message message) throws InterruptedException {
        //模拟等待一秒
        Thread.sleep(1000L);
        //把获取到的message中的信息发送给客户端topic
        return new ResponseMessage(HtmlUtils.htmlEscape(message.getContent()));
    }
}
```
### 编写页面index.html
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>websocket</title>
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css" integrity="sha384-HSMxcRTRxnN+Bdg0JdbxYKrThecOKuH5zCYotlSAcp1+c8xmyTe9GYg1l9a69psu" crossorigin="anonymous">
  <script src="webjars/jquery/jquery.min.js"></script>
  <script src="webjars/sockjs-client/sockjs.min.js"></script>
  <script src="webjars/stomp-websocket/stomp.min.js"></script>
  <script src="scripts.js"></script>
</head>
<body>
<div class="container" style="margin-top: 50px">
<!-- 发送信息 -->
  <div class="row">
    <div class="col-md-10">
      <form class="form-inline">
        <div class="form-group">
          <label class="message">消息</label>
          <input type="text" id="sendMessage" class="form-control" placeholder="请在此输入你的消息">
        </div>
        <button id="send" class="btn btn-default" type="button">发送</button>
      </form>
    </div>
  </div>
<!-- 获取到的信息 -->
  <div class="row">
    <div class="col-md-12">
      <table id="message-history" class="table table-striped">
        <thead>
        <tr><th>消息</th></tr>
        </thead>
        <tbody id="showMessage">

        </tbody>
      </table>
    </div>
  </div>
</div>
</body>
</html>
```
### 4、编写js——scripts.js
```js
let stompClient = null;

function showMessage(content) {
    console.log("接到消息："+content)
    $("#showMessage").append("<tr><td>"+content+"</td></tr>")
}

function connect() {
    const socket = new SockJS("our-websocket");
    stompClient=Stomp.over(socket);
    stompClient.connect({},function(frame){
        console.log("连接："+frame);
        stompClient.subscribe("/topic/message",function (message){
            showMessage(JSON.parse(message.body).content)
        })
    })
}

function sendMessage(){
    console.log("发送消息：")
    stompClient.send("ws/message",{},JSON.stringify({"content":$("#sendMessage").val()}))
}

$(document).ready(function(){
    console.log("index页面准备完毕……")
    connect();
    $("#send").click(function (){
        sendMessage();
    })
})
```
## 服务器实现广播通信
### 1、建立WsService
```java
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
```
**注意使用`SimpMessagingTemplate`的`convertAndSend`方法来群发消息**
### 2、指定接口控制器WsController
```java
@RestController
public class WsController {
    @Autowired
    private WsService wsService;

    @PostMapping("sendMessage")
    public void sendMessage(String message){
        wsService.notify(message);
    }
}
```
### 3、使用postman或其它测试工具测试

## 2、实现和单个用户通信
