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

function sendMessage(){
    console.log("发送消息：")
    stompClient.send("ws/message",{},JSON.stringify({"content":$("#sendMessage").val()}))
}

function sendPrivateMessage(){
    console.log("发送私消息：")
    stompClient.send("ws/privateMessage",{},JSON.stringify({"content":$("#sendPrivateMessage").val()}))
}
$(document).ready(function(){
    console.log("index页面准备完毕……")
    connect();
    $("#send").click(function (){
        sendMessage();
    })
    $("#sendPrivate").click(function (){
        sendPrivateMessage();
    })
})