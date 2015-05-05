package com.shareplaylearn;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(new ApplicationConfig()));
        servletHandler.addServlet(servletHolder, "/api/*");
        jettyServer.setHandler(servletHandler);

        jettyServer.setDumpAfterStart(true);
        jettyServer.setDumpBeforeStop(true);

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
        int timeout = 30000;
        int port = 8080;

        StandaloneServer standaloneServer = new StandaloneServer(port);
        Thread serverThread = new Thread(standaloneServer);
        serverThread.start();
        serverThread.join(timeout);
        standaloneServer.stop();
    }
}
