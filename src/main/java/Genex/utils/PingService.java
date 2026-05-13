package Genex.utils;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PingService extends ScheduledService<Long> {
    private static final String HOST="1.1.1.1";
    private static final int PORT=53;
    private static final int TIMEOUT=2000;
    private static final int INTERVAL=10;

    public PingService(){
        setPeriod(Duration.seconds(INTERVAL));
    }

    public static long getLatency(String host){
        long start = System.currentTimeMillis();
        try (Socket socket=new Socket()){
            socket.connect(new InetSocketAddress(HOST,PORT),TIMEOUT);
            long ping=System.currentTimeMillis()-start;
            System.out.println("[PingService] Latency to " + host + " : " + ping + " ms");
            return ping;
        }catch (IOException e){
            System.out.println("[PingService] Host unreachable : " + host);
            return -1L;
        }
    }

    @Override
    protected Task<Long> createTask() {
        return new Task<>() {
            @Override
            protected Long call() {
                return getLatency(HOST);
            }
        };
    }
}
