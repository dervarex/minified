package com.dervarex.minified.utils.network;

import com.dervarex.minified.utils.exceptions.NoConnectionException;

import java.io.IOException;
import java.net.*;

public class NetworkUtil {
    private static final boolean fakeOffline = false; // used for debug purposes currently
    /**
     * Ensures that the user has a working internet connection
     * <p>
     * Example usage:
     * <pre>{@code
     * try {
     *     NetworkUtil.ensureOnline("Login");
     *     // proceed with network operation
     * } catch (NoConnectionException e) {
     *     // show user-friendly message
     *     System.out.println("You do not have a network connection");
     * }
     * }</pre>
     * @param action a short description of the action that requires connectivity, e.g. "Login"
     * @throws NoConnectionException if there is no connection
     */
    public static void ensureOnline(String action) throws NoConnectionException {
        long start; long dnsLatency=-1, tcpLatency=-1, httpLatency=-1;
        boolean dnsOk=false, tcpOk=false, httpOk=false;
        NoConnectionException.Builder builder = new NoConnectionException.Builder().action(action).os(System.getProperty("os.name"));
        // DNS Probe
        try {
            start = System.nanoTime();
            InetAddress addr = InetAddress.getByName("example.com");
            dnsLatency = (System.nanoTime()-start)/1_000_000L;
            dnsOk = addr != null;
            builder.addProbe("dns:example.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.DNS, dnsOk, dnsLatency, "example.com", addr.getHostAddress()));
        } catch (Exception e) {
            builder.addProbe("dns:example.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.DNS, false, -1, "example.com", e.getClass().getSimpleName()));
        }
        // TCP Probe (1.1.1.1:53)
        try (Socket s = new Socket()) {
            start = System.nanoTime();
            s.connect(new InetSocketAddress("1.1.1.1",53), 1200);
            tcpLatency = (System.nanoTime()-start)/1_000_000L;
            tcpOk = true;
            builder.addProbe("tcp:1.1.1.1:53", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.TCP, true, tcpLatency, "1.1.1.1:53", "connected"));
        } catch (Exception e) {
            builder.addProbe("tcp:1.1.1.1:53", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.TCP, false, -1, "1.1.1.1:53", e.getClass().getSimpleName()));
        }
        // HTTP Probe (fast HEAD)
        try {
            start = System.nanoTime();
            HttpURLConnection con = (HttpURLConnection)new URL("https://api.mojang.com").openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(1500); con.setReadTimeout(1500);
            int code = con.getResponseCode();
            httpLatency = (System.nanoTime()-start)/1_000_000L;
            httpOk = code >=200 && code < 500; // any response proves connectivity
            builder.addProbe("http:api.mojang.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.HTTP, httpOk, httpLatency, "https://api.mojang.com", "code="+code));
        } catch (IOException e) {
            builder.addProbe("http:api.mojang.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.HTTP, false, -1, "https://api.mojang.com", e.getClass().getSimpleName()));
        }
        builder.dnsResolved(dnsOk).tcpAny(tcpOk).httpAny(httpOk);
        if (!(dnsOk || tcpOk || httpOk) || fakeOffline) {
            System.out.println("No connectivity for " + action);
            throw builder.build();
        }
    }
}