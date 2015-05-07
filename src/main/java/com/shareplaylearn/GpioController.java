package com.shareplaylearn;

import com.shareplaylearn.utilities.Exceptions;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
/**
 * Created by stu on 5/5/15.
 */
@ServerEndpoint("/gpio")
public class GpioController {
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void OnMessage(Session session, String message) {
        System.out.println("Got message from websocket client: " + message);
        session.getAsyncRemote().sendText("Got your message!");
        //to broadcast (like telling everyone "stu turned the lights on: for( s : sess.getOpenSessions() ) { if s.isOpen()..
    }

    @OnClose
    public void onClose() {

    }

    @OnError
    public void OnError(Throwable t) {
        System.out.println(Exceptions.asString(t));
    }
}
