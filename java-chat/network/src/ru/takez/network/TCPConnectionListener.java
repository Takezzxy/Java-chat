package ru.takez.network;

public interface TCPConnectionListener {

    void onConnectionReady(TCPconnection tcpConnection);
    void onReceiveString(TCPconnection tcpConnection, String value);
    void onDisconnection(TCPconnection tcpConnection);
    void onException(TCPconnection tcpConnection, Exception e);
}
