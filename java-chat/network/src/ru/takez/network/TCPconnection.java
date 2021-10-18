package ru.takez.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPconnection {

    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPconnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPconnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPconnection.this);
                    while (!rxThread.isInterrupted()) {
                        eventListener.onReceiveString(TCPconnection.this, in.readLine());
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPconnection.this, e);
                } finally {
                    eventListener.onDisconnection(TCPconnection.this);
                }

            }
        });
        rxThread.start();

    }

    public synchronized void sendString(String value) {
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPconnection.this, e);
            disconnection();
        }
    }

    public synchronized void disconnection() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPconnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
