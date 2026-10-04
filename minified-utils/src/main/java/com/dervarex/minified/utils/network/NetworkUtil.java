package com.dervarex.minified.utils.network;

import com.dervarex.minified.utils.exceptions.NoConnectionException;
import org.apiguardian.api.API;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

@API(status = API.Status.STABLE)
public class NetworkUtil {
    private static final boolean fakeOffline = false; // used for debug purposes currently
    // a launch asks about 4 times in a row, no need to poke 3 servers every time
    private static final long REMEMBER_NANOS = TimeUnit.SECONDS.toNanos(15);

    private static boolean checked;
    private static long checkedAt;
    private static NoConnectionException lastFailure;

    /**
     * Ensures that the user has a working internet connection
     * <p>
     * Example usage:
     * <pre>{@code
     * try {
     *     NetworkUtil.ensureOnline("Login");
     *     // proceed with network operation
     * } catch (NoConnectionException e) {
     *     // show/print a human readable message
     *     System.out.println("You do not have a network connection");
     * }
     * }</pre>
     * The result is remembered for a few seconds
     * @param action a short description of the action that requires connectivity, e.g. "Login"
     * @throws NoConnectionException if there is no connection
     */
    public static synchronized void ensureOnline(String action) throws NoConnectionException {
        if (checked && System.nanoTime() - checkedAt < REMEMBER_NANOS) {
            if (lastFailure != null) {
                throw withAction(lastFailure, action);
            }
            return;
        }

        NoConnectionException failure = probe(action);
        checked = true;
        checkedAt = System.nanoTime();
        lastFailure = failure;
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * @return null if any of the probes got through, they run until the first one does
     */
    private static NoConnectionException probe(String action) {
        NoConnectionException.Builder builder = new NoConnectionException.Builder().action(action).os(System.getProperty("os.name"));
        boolean dnsOk = probeDns(builder);
        boolean tcpOk = !dnsOk && probeTcp(builder);
        boolean httpOk = !dnsOk && !tcpOk && probeHttp(builder);
        builder.dnsResolved(dnsOk).tcpAny(tcpOk).httpAny(httpOk);
        if (!(dnsOk || tcpOk || httpOk) || fakeOffline) {
            System.out.println("No connectivity for " + action);
            return builder.build();
        }
        return null;
    }

    private static boolean probeDns(NoConnectionException.Builder builder) {
        try {
            long start = System.nanoTime();
            InetAddress addr = InetAddress.getByName("example.com");
            long latency = (System.nanoTime()-start)/1_000_000L;
            builder.addProbe("dns:example.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.DNS, true, latency, "example.com", addr.getHostAddress()));
            return true;
        } catch (Exception e) {
            builder.addProbe("dns:example.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.DNS, false, -1, "example.com", e.getClass().getSimpleName()));
            return false;
        }
    }

    private static boolean probeTcp(NoConnectionException.Builder builder) {
        try (Socket s = new Socket()) {
            long start = System.nanoTime();
            s.connect(new InetSocketAddress("1.1.1.1",53), 1200);
            long latency = (System.nanoTime()-start)/1_000_000L;
            builder.addProbe("tcp:1.1.1.1:53", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.TCP, true, latency, "1.1.1.1:53", "connected"));
            return true;
        } catch (Exception e) {
            builder.addProbe("tcp:1.1.1.1:53", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.TCP, false, -1, "1.1.1.1:53", e.getClass().getSimpleName()));
            return false;
        }
    }

    private static boolean probeHttp(NoConnectionException.Builder builder) {
        try {
            long start = System.nanoTime();
            HttpURLConnection con = (HttpURLConnection)new URL("https://api.mojang.com").openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(1500); con.setReadTimeout(1500);
            int code = con.getResponseCode();
            long latency = (System.nanoTime()-start)/1_000_000L;
            boolean ok = code >=200 && code < 500; // any response proves connectivity
            builder.addProbe("http:api.mojang.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.HTTP, ok, latency, "https://api.mojang.com", "code="+code));
            return ok;
        } catch (IOException e) {
            builder.addProbe("http:api.mojang.com", new NoConnectionException.ProbeResult(NoConnectionException.ProbeResult.Type.HTTP, false, -1, "https://api.mojang.com", e.getClass().getSimpleName()));
            return false;
        }
    }

    private static NoConnectionException withAction(NoConnectionException original, String action) {
        NoConnectionException.Builder builder = new NoConnectionException.Builder()
                .action(action)
                .os(original.getActiveOs())
                .dnsResolved(original.wasDnsResolved())
                .tcpAny(original.wasAnyTcpReachable())
                .httpAny(original.wasAnyHttpReachable())
                .suggestions(original.getSuggestions())
                .netIfaces(original.getNetworkInterfaceSummary())
                .cause(original.getRootCause());
        original.getProbeResults().forEach(builder::addProbe);
        return builder.build();
    }
}
