package stan.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Handler
        implements Runnable
{
    private final Socket socket;
    private final Worker worker;

    Handler(Socket s, Worker w)
    {
        socket = s;
        worker = w;
    }

    public void run()
    {
        try
        {
            send(socket, worker.work(request(socket)));
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
        catch(EmptyRequestException e)
        {
            System.out.println("empty request");
        }
        catch(UnknownRequestTypeException e)
        {
            System.out.println("empty request");
        }
        catch(UnknownRequestQueryException e)
        {
            System.out.println("empty request");
        }
        try
        {
            socket.close();
        }
        catch(IOException e)
        {
        }
    }
    private Connection.Request request(Socket socket)
            throws IOException, EmptyRequestException, UnknownRequestTypeException, UnknownRequestQueryException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Map<String, String> headers = new HashMap<String, String>();
        String firstHeader = bufferedReader.readLine();
        if(firstHeader == null || firstHeader.isEmpty())
        {
            throw new EmptyRequestException();
        }
        String[] split = firstHeader.split(" ");
        if(split.length == 0)
        {
            throw new UnknownRequestTypeException();
        }
        Connection.Request.Type requestType;
        try
        {
            requestType = Connection.Request.Type.valueOf(split[0]);
        }
        catch(IllegalArgumentException e)
        {
            throw new UnknownRequestTypeException();
        }
        if(split.length == 1)
        {
            throw new UnknownRequestQueryException();
        }
        String query = split[1];
        if(query.isEmpty())
        {
            throw new UnknownRequestQueryException();
        }
        boolean withBody = firstHeader.startsWith(Connection.Request.Type.POST.name());
        int contentLength = 0;
        String contentTypeValue = null;
        String header;
        while((header = bufferedReader.readLine()) != null)
        {
            if(header.isEmpty())
            {
                break;
            }
            else
            {
                if(header.toLowerCase().startsWith(Connection.Content.contentLengthHeaderName))
                {
                    contentLength = Integer.parseInt(header.substring((Connection.Content.contentLengthHeaderName + ": ").length()));
                    System.out.println("Content length: " + contentLength);
                }
                else if(header.toLowerCase().startsWith(Connection.Content.contentTypeHeaderName))
                {
                    contentTypeValue = header.substring((Connection.Content.contentTypeHeaderName + ": ").length());
                    System.out.println("Content type: " + contentTypeValue);
                }
                String[] h = header.split(": ");
                if(h.length < 2)
                {
                    continue;
                }
                headers.put(h[0], header.substring((h[0]+": ").length()));
            }
        }
        if(headers.isEmpty())
        {
            throw new EmptyRequestException();
        }
        String requestBody = null;
        if(withBody)
        {
            StringBuilder body = new StringBuilder();
            for(int i = 0; i < contentLength; i++)
            {
                body.append((char) bufferedReader.read());
            }
            requestBody = body.toString();
            System.out.println("Body: " + requestBody + "\n\tsize " + requestBody.length());
        }
        System.out.println(headers);
        Connection.Content.Type contentType;
        if(contentTypeValue != null)
        {
            try
            {
                contentType = Connection.Content.Type.valueOf(contentTypeValue);
            }
            catch(IllegalArgumentException e)
            {
                contentType = Connection.Content.Type.UNKNOWN;
            }
        }
        else
        {
            contentType = Connection.Content.Type.UNKNOWN;
        }
        return withBody
                ? Connection.request(requestType, query, headers, requestBody, Connection.content(contentType, contentLength))
                : Connection.request(requestType, query, headers, Connection.content(contentType));
    }
    private void send(Socket socket, Connection.Response response)
            throws IOException
    {
        String data = "HTTP/1.1 "+response.code()+" none\r\n";
        data += Connection.Content.contentTypeHeaderName + ": " + response.content().type().value + "\r\n";
        data += Connection.Content.contentLengthHeaderName + ": " + response.content().length() + "\r\n";
        data += "\r\n";
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write((data + response.body()).getBytes());
        outputStream.flush();
    }

    public interface Worker
    {
        Connection.Response work(Connection.Request request);
    }

    private class EmptyRequestException
            extends Exception
    {
    }
    private class UnknownRequestTypeException
            extends Exception
    {
    }
    private class UnknownRequestQueryException
            extends Exception
    {
    }
}