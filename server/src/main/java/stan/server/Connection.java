package stan.server;

import java.util.Collections;
import java.util.Map;

public final class Connection
{
    static public Response responseText(int code, String text)
    {
        return response(code, text, content(Content.Type.TEXT, text.length()));
    }
    static public Response responseJson(int code, String json)
    {
        return response(code, json, content(Content.Type.JSON, json.length()));
    }
    static public Response response(int code, Content content)
    {
        return response(code, "", content);
    }
    static public Response response(int code, String body, Content content)
    {
        return response(code, Collections.<String, String>emptyMap(), body, content);
    }
    static public Response response(int code, Map<String, String> headers, Content content)
    {
        return new ResponseData(code, headers, "", content);
    }
    static public Response response(int code, Map<String, String> headers, String body, Content content)
    {
        return new ResponseData(code, headers, body, content);
    }

    static public Request request(Request.Type type, String query, Content content)
    {
        return request(type, query, "", content);
    }
    static public Request request(Request.Type type, String query, String body, Content content)
    {
        return request(type, query, Collections.<String, String>emptyMap(), body, content);
    }
    static public Request request(Request.Type type, String query, Map<String, String> headers, Content content)
    {
        return request(type, query, headers, "", content);
    }
    static public Request request(Request.Type type, String query, Map<String, String> headers, String body, Content content)
    {
        return new RequestData(type, query, headers, body, content);
    }

    static public Content content()
    {
        return content(Content.Type.UNKNOWN, 0);
    }
    static public Content content(Content.Type type)
    {
        return content(type, 0);
    }
    static public Content content(Content.Type type, int length)
    {
        return new ContentData(type, length);
    }
    static public Content.Type parse(String value)
    {
        if(Content.Type.JSON.isIt(value))
        {
            return Content.Type.JSON;
        }
        else if(Content.Type.TEXT.isIt(value))
        {
            return Content.Type.TEXT;
        }
        return Content.Type.UNKNOWN;
    }

    private Connection()
    {
    }

    public interface Response
    {
        int code();
        Map<String, String> headers();
        String body();
        Content content();
    }
    static private class ResponseData
        implements Response
    {
        private final int code;
        private final Map<String, String> headers;
        private final String body;
        private final Content content;

        ResponseData(int cd, Map<String, String> h, String bd, Content cntnt)
        {
            code = cd;
            headers = h;
            body = bd;
            content = cntnt;
        }

        public int code()
        {
            return code;
        }
        public Map<String, String> headers()
        {
            return headers;
        }
        public String body()
        {
            return body;
        }
        public Content content()
        {
            return content;
        }

        public String toString()
        {
            return "{"+code+","+(body != null && !body.isEmpty() ? ","+body+"," : ",")+content+","+headers+"}";
        }
    }

    public interface Request
    {
        Type type();
        String query();
        Map<String, String> headers();
        String body();
        Content content();

        enum Type
        {
            GET,
            POST
        }
    }
    static private class RequestData
        implements Request
    {
        private final Type type;
        private final String query;
        private final Map<String, String> headers;
        private final String body;
        private final Content content;

        private RequestData(Type type, String q, Map<String, String> headers, String body, Content content)
        {
            this.type = type;
            query = q;
            this.headers = headers;
            this.body = body;
            this.content = content;
        }

        public Type type() {
            return type;
        }
        public String query()
        {
            return query;
        }
        public Map<String, String> headers() {
            return headers;
        }
        public String body() {
            return body;
        }
        public Content content() {
            return content;
        }

        public String toString()
        {
            return "{"+type.name()+","+query+(body != null && !body.isEmpty() ? ","+body+"," : ",")+content+","+headers+"}";
        }
    }

    public interface Content
    {
        String contentLengthHeaderName = "content-length";
        String contentTypeHeaderName = "content-type";

        Type type();
        int length();

        enum Type
        {
            UNKNOWN("unknown"),
            JSON("application/json"),
            TEXT("text/plain");

            public final String value;

            Type(String v)
            {
                value = v;
            }

            public boolean isIt(String v)
            {
                return value.equals(v);
            }
        }
    }
    static private class ContentData
        implements Content
    {
        private final Type type;
        private final int length;

        private ContentData(Type t, int l)
        {
            type = t;
            length = l;
        }

        public Type type()
        {
            return type;
        }
        public int length()
        {
            return length;
        }

        public String toString()
        {
            return "{"+type.name()+","+length+"}";
        }
    }
}