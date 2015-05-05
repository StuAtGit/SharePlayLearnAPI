package com.shareplaylearn.websockets;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
/**
 * Created by stu on 5/5/15.
 */
@ServerEndpoint("gpio")
public class GpioController {
    @OnMessage
    public void OnMessage(Session session, String message) {
        System.out.println("Got message from websocket client: " + message);
        session.getAsyncRemote().sendText("Got your message!");
        //to broadcast (like telling everyone "stu turned the lights on: for( s : sess.getOpenSessions() ) { if s.isOpen()..
    }
}
