# spring-boot-stomp-websocket
spring boot 实现stomp的websocket通信
## 学习视频来源
[youtube](https://www.youtube.com/watch?v=XY5CUuE6VOk)
## 一、配置
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
## 二、服务器实现广播通信
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
### 3、使用postman或其它测试工具测试（略）
## 三、实现和单个用户通信
### 1、区分用户在websocket进行所握手时给每个用户建立一个id可以区分客户端，建立Userhandshakehandler类
```java
@Slf4j
public class Userhandshakehandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        //编号可以实为客户id或者token值
        final String id= UUID.randomUUID().toString().replaceAll("-","");
        log.info("登陆用户ID:{}",id);
        return new UserPrincipal(id);
    }
}
```
### 2、在websocket配置类中添加自定义握手hendler——WebSocketConfig:
```java
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        //添加端点
        registry.addEndpoint("our-websocket")
                //添加自定义握手
                .setHandshakeHandler(new Userhandshakehandler())
                .withSockJS();
    }
```
### 3、在MessageController中添加私信通道：
```java
    @MessageMapping("/privateMessage")
    @SendToUser("/topic/privateMessage")
    public ResponseMessage privateMessage(final Principal principal, final Message message) throws InterruptedException {
        //模拟等待一秒
        Thread.sleep(1000L);
        //把获取到的message中的信息发送给客户端topic
        return new ResponseMessage("用户："+principal.getName()+"发送的信息："+HtmlUtils.htmlEscape(message.getContent()));
    }
```

### 4、修改页面和js，添加私信功能——自己仅接收到自己的信息
```html
<!-- 发送私信息 -->
  <div class="row">
    <div class="col-md-10">
      <form class="form-inline">
        <div class="form-group">
          <label class="message">私信</label>
          <input type="text" id="sendPrivateMessage" class="form-control" placeholder="请在此输入你的消息">
        </div>
        <button id="sendPrivate" class="btn btn-default" type="button">发送私信</button>
      </form>
    </div>
  </div>
```
```js
function connect() {
    const socket = new SockJS("our-websocket");
    stompClient=Stomp.over(socket);
    stompClient.connect({},function(frame){
        console.log("连接："+frame);
        //公共信息通过
        stompClient.subscribe("/topic/message",function (message){
            showMessage(JSON.parse(message.body).content)
        })
        //私信通过——前加user
        stompClient.subscribe("/user/topic/privateMessage",function (message){
            showMessage(JSON.parse(message.body).content)
        })
    })
}
function sendPrivateMessage(){
    console.log("发送私消息：")
    stompClient.send("ws/privateMessage",{},JSON.stringify({"content":$("#sendPrivateMessage").val()}))
}
```
**注：私信连接路径前前加`/user/`** 
### 5、添加服务器主动向客户端推送私信
#### 1）在WsService中添加私信服务方法：
```java
        public void privateNotify(String id,String message){
        ResponseMessage responseMessage=new ResponseMessage(message);
        //注：方法使用Touser,此处路径不加user
        template.convertAndSendToUser(id,"/topic/privateMessage",responseMessage);
    }
```
#### 2)WsController调用私信方法，开启接口：
```java
    @PostMapping("sendPrivateMessage")
    public void sendMessage(String id,String message){
        wsService.privateNotify(id,message);
    }
```
## 四、重构代码——用直接用于生产的方法Notificationservice（详见代码）和重构接口（详见代码）
## 五、未实现的功能及设想：
### 1、考虑到客户端有时候由于断网原因未能够时时连接服务器，需要客户端也服务器间进行心跳——客户端轮询经过私信给服务器，服务器应该及时反馈给客户端一个影响字段，如果客户端收到即表示正常，如果收不到表示服务器websocket连接中断，立即发送重新连接。同时，为了记录服务器连接客户端实时情况，及时发现连接不上服务器的客户端，采用每次心跳记录的方法是非常占资源的，应该改为缓存记录加上RabbitMQ的缓存消息队列，来发管理服务器连接状态比较合理，详情见——ClientStatus.puml
### 2、发私信的id使用userId或者token都可以，根据业务需要，不用来回转就可以了，否则需要缓存。
### 3、websocket连接数使用nio——netty框架，单体tomcat数量可以上万，如果配置[WebSocket轻松单台服务器5w并发jmeter实测](https://blog.csdn.net/weixin_34379433/article/details/91365773)