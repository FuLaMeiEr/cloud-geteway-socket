package com.example.demo;

import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName
 * @Description
 * @Author WangHaiQiang
 * @Date Created in 14:36 2020/4/14
 **/
@ServerEndpoint("/websocket/{username}")
public class WebsocketConfig {

    private String username;

    private Session session;

    private static Map<String, WebsocketConfig> clients
            = new ConcurrentHashMap<String, WebsocketConfig>();


    /**
     * 连接成功调用的函数
     *
     * @param username 用户名
     * @param session  和客户端的会话
     */
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        this.username = username;
        this.session = session;

        clients.put(username, this);
        sendMessageAll(username + "已连接！当前在线人数：" + clients.size());
        System.out.println(username + "已连接!");
    }

    /**
     * 连接关闭时执行的函数
     *
     * @throws IOException
     */
    @OnClose
    public void onClose() throws IOException {
        clients.remove(username);
        sendMessageAll(username + "断开连接！当前在线人数：" + clients.size());
    }

    /**
     * 接收到消息时执行的函数
     *
     * @param message 接受的消息内容
     * @throws IOException
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        JSONObject jsonTo = new JSONObject(message);
        if (!jsonTo.get("To").equals("All")) {
            message = this.username + "对你说：" + jsonTo.get("Message");
            ;
            sendMessageTo(message, jsonTo.get("To").toString());
        } else {
            message = this.username + "说：" + jsonTo.get("Message");
            sendMessageAll(message);
        }
    }

    /**
     * 发生错误执行的函数
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 向目标用户发送消息
     *
     * @param message 消息内容
     * @param to      目标用户
     * @throws IOException
     */
    private void sendMessageTo(String message, String to) throws IOException {
        for (WebsocketConfig item : clients.values()) {
            if (item.username.equals(to)) {
                item.session.getAsyncRemote().sendText(message);
            }
        }
    }

    /**
     * 向所有人发送消息
     *
     * @param message 消息内容
     */
    private void sendMessageAll(String message) {
        for (WebsocketConfig item : clients.values()) {
            item.session.getAsyncRemote().sendText(message);
        }
    }

}