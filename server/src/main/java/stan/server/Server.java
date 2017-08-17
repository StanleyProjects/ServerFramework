package stan.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server
{
    static private Map<Integer, Server> servers = new HashMap<Integer, Server>();

    static public void start(int port, Handler.Worker worker)
    {
        Server server = servers.get(port);
        if(server != null)
        {
            return;
        }
        server = new Server(port, worker);
        servers.put(port, server);
        server.run();
    }
    static public void stop(int port)
    {
        Server server = servers.get(port);
        if(server == null)
        {
            return;
        }
        server.close();
        servers.remove(port);
    }

    private final ServerSocket serverSocket;
    private final Handler.Worker worker;

    private Server(int port, Handler.Worker w)
    {
        worker = w;
        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void run()
    {
        System.out.println("Server started " + serverSocket.getLocalPort());
        while(true)
        {
            new Thread(new Handler(accept(serverSocket), worker)).start();
        }
    }
    private void close()
    {
        try
        {
            serverSocket.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Socket accept(ServerSocket serverSocket)
    {
        try
        {
            return serverSocket.accept();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}