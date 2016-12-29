package com.uz.simpletodolist.core;
import com.android.volley.toolbox.HurlStack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * @author hendrawd on 6/29/16
 */
public class ProxiedHurlStack extends HurlStack {

    private static final String PROXY_ADDRESS = "192.168.1.10";
    private static final int PROXY_PORT = 8888;//change with the port of the proxy
    private static final boolean ENABLED = false;

    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved(PROXY_ADDRESS, PROXY_PORT));
        if(ENABLED)
            return (HttpURLConnection) url.openConnection(proxy);
        else
            return (HttpURLConnection) url.openConnection();
    }
}