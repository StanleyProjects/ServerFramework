package stan.server.sample;

import stan.server.Connection;
import stan.server.Server;

public class App
{
    static public void main(String[] args)
    {
        new Thread(() -> Server.start(8888, request ->
        {
            System.out.println("request:"
                    + "\n\tt " + request.type()
                    + "\n\tq " + request.query()
                    + "\n\tb " + request.body());
            switch(request.type())
            {
                case GET:
                    switch(request.query())
                    {
                        case "/text":
                            return Connection.responseText(200, "return simple text");
                        case "/json":
                            return Connection.responseJson(200, "{\"message\":\"return simple text\"}");
                    }
                    break;
            }
            return Connection.responseText(404, "Not found");
        })).start();
    }
}