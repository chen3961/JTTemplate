#JTTemplate
This is a JSP-like text template engine for java. Unlike the same work on [Velocity](http://http://velocity.apache.org/) and [FreeMaker](http://freemarker.org/) which required to learn new grammar. You can use the java style code directly to describe the logic in text template.

##Usage
    Reader reader = new StringReader("Hello<%if (flag) { %> <%=text%><%} else {%> nothing <%}%>!");   
    Writer writer = new StringWriter();   
    JTTEngineContext context = new JTTEngineContext();   
    context.addBind("flag", true);    
    context.addBind("text", "world");   
    new JTTEngine().evaluate(reader, writer, context);   

  
You can get "Hello world!" from the `writer.toString()`

You can also load the template from file or output the result to another file by using the `java.io.Reader` and `java.io.Writer`
