package org.zsource.jtt;

import org.junit.Test;
import org.zsource.jtt.context.JTTEngineContext;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

public class JTTEngineTest {

  @Test
  public void testSimpleExpression() throws Exception {
    Reader reader = new StringReader("test<%=2+2%>");
    Writer writer = new StringWriter();
    new JTTEngine().evaluate(reader, writer, new JTTEngineContext());
    assertEquals(writer.toString(), "test4");
  }

  @Test
   public void testSimpleScript() throws Exception {
    Reader reader = new StringReader("test-<%if (flag) {%>true<%}else{%>false<%}%>");
    Writer writer = new StringWriter();
    JTTEngineContext context = new JTTEngineContext();
    context.addBind("flag", true);
    new JTTEngine().evaluate(reader, writer, context);
    assertEquals(writer.toString(), "test-true");
  }

  @Test
  public void testArrayLoop() throws Exception {
    Reader reader = new StringReader("test-<%for (int i : val) {%><%=i%><%}%>");
    Writer writer = new StringWriter();
    JTTEngineContext context = new JTTEngineContext();
    context.addBind("val", new int[]{1,2,3,4});
    new JTTEngine().evaluate(reader, writer, context);
    assertEquals(writer.toString(), "test-1234");
  }

  @Test
  public void testUsingLocalObject() throws Exception {
    Reader reader = new StringReader("test-name=<%=val.upper()%>  value+3=<%=val.add(3)%>");
    Writer writer = new StringWriter();
    JTTEngineContext context = new JTTEngineContext();
    context.addBind("val", new TestValueObject("test", 1));
    new JTTEngine().evaluate(reader, writer, context);
    assertEquals(writer.toString(), "test-name=TEST  value+3=4");
  }
}