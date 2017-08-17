package stan.server.sample;

import stan.server.Connection;
import stan.server.Handler;
import stan.server.Server;

public class App
{
    static public void main(String[] args)
    {
        new Thread(() -> Server.start(8888, new Handler.Worker()
        {
            public Connection.Response work(Connection.Request request)
            {
                System.out.println("request:"
                        + "\n\tt " + request.type()
                        + "\n\tq " + request.query()
                        + "\n\tb " + request.body());
                return Connection.response(404, Connection.content());
            }
        })).start();
    }
}