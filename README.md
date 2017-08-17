# ServerFramework
Framework for create http server with java

<img src="media/icon.png" width="128" height="128" />

#### Run [Server](https://github.com/StanleyProjects/ServerFramework/tree/master/server/src/main/java/stan/server/Server.java) on 8888 port with [Worker](https://github.com/StanleyProjects/ServerFramework/tree/master/server/src/main/java/stan/server/Handler.java)

```java
Server.start(8888, new Handler.Worker()
{
    public Connection.Response work(Connection.Request request)
    {
        System.out.println("request:"
                + "\n\tt " + request.type()
                + "\n\tq " + request.query()
                + "\n\tb " + request.body());
        return Connection.response(404, Connection.content());
    }
});
```

#### Also see [sample](https://github.com/StanleyProjects/ServerFramework/tree/master/sample/src/main/java/stan/server/sample/App.java)