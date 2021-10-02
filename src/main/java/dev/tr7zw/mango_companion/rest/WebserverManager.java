package dev.tr7zw.mango_companion.rest;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import dev.tr7zw.mango_companion.App;
import lombok.Getter;

@Getter
public class WebserverManager {

    private Server server;

    public void init() {
        System.setProperty("wicket.configuration", "DEPLOYMENT");

        server = new Server();

        HttpConfiguration http_config = new HttpConfiguration();
        // http_config.setSecureScheme("https");
        // http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setPort(App.getConfig().getApiPort());
        http.setIdleTimeout(1000 * 60 * 60);

        server.addConnector(http);

        WebAppContext bb = new WebAppContext();
        bb.setServer(server);
        bb.setContextPath("/");
        bb.setWar("src/main/webapp");

        server.setHandler(bb);

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
        server.addEventListener(mBeanContainer);
        server.addBean(mBeanContainer);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }
}