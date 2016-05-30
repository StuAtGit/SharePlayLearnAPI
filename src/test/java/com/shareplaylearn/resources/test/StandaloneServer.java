/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.shareplaylearn.resources.test;

import com.shareplaylearn.SharePlayLearnApi;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Created by stu on 4/21/15.
 * TODO: Add ability to run as a stand-alone applicaition using jetty + jersey
 * For now, this is mainly to enable junit tests of server/client interaction -
 * still plan on deploying as a war in tomcat.
 * We need Angular unit tests as well.
 */
public class StandaloneServer
    implements  Runnable {

    private int port;
    private Server jettyServer;

    public StandaloneServer( int port ) {
        this.port = port;
    }

    public void stop() throws Exception {
        this.jettyServer.stop();
    }

    @Override
    public void run() {
        this.jettyServer = new Server(this.port);
        ServletContextHandler jerseyHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

        ServletHolder servletHolder = new ServletHolder(new ServletContainer(new SharePlayLearnApi()));
        jerseyHandler.addServlet(servletHolder, "/api/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(jerseyHandler);

        jettyServer.setHandler(handlers);
        jettyServer.setDumpAfterStart(true);

        try {
            jettyServer.start();
            jettyServer.join();
        } catch (InterruptedException e ) {
            System.out.println("Jetty server interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error running or stopping Jetty server");
            e.printStackTrace();
        }
    }

    public static void main( String[] args ) throws Exception {
        //eventually parse these from arguments
        int timeout = 40000;
        int port = 8080;

        StandaloneServer standaloneServer = new StandaloneServer(port);
        Thread serverThread = new Thread(standaloneServer);
        serverThread.start();
        serverThread.join(timeout);
        standaloneServer.stop();
    }
}
