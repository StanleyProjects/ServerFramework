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
    private final Corrector corrector;

    Handler(Socket s, Worker w, Corrector c)
    {
        socket = s;
        worker = w;
        corrector = c;
    }

    public void run()
    {
        Connection.Response response;
        try
        {
            response = worker.work(request(socket));
        }
        catch(IOException e)
        {
            response = corrector.error(e);
        }
        catch(EmptyRequestException e)
        {
            response = corrector.error(e);
        }
        catch(UnknownRequestTypeException e)
        {
            response = corrector.error(e);
        }
        catch(UnknownRequestQueryException e)
        {
            response = corrector.error(e);
        }
        try
        {
            send(socket, response);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
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
            contentType = Connection.parse(contentTypeValue);
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
        String data = "HTTP/1.1 "+response.code()+" " + codeDescription(response.code()) + "\r\n";
        data += Connection.Content.contentTypeHeaderName + ": " + response.content().type().value + "\r\n";
        data += Connection.Content.contentLengthHeaderName + ": " + response.content().length() + "\r\n";
        data += "\r\n";
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write((data + response.body()).getBytes());
        outputStream.flush();
    }
    private String codeDescription(int code)
    {
        switch(code)
        {
            case 200:
                return "Success";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
        }
        return "Unknown";
    }

    public interface Worker
    {
        Connection.Response work(Connection.Request request);
    }
    public interface Corrector
    {
        Connection.Response error(Exception e);
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