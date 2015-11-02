package org.zsource.jtt;

import org.junit.Test;
import org.zsource.jtt.context.GenericTypeReslover;
import org.zsource.jtt.context.JTTEngineContext;
import org.zsource.jtt.context.JTTSettings;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

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

  @Test
  public void testHelloworld() throws Exception {
    Reader reader = new StringReader("Hello <%if (flag) {%><%=target%><%}%>!");
    Writer writer = new StringWriter();
    JTTEngineContext context = new JTTEngineContext();
    context.addBind("flag", true);
    context.addBind("target", "world");
    new JTTEngine().evaluate(reader, writer, context);
    assertEquals(writer.toString(), "Hello world!");
  }

  @Test
  public void testFromFile() throws Exception {
    Reader reader = new InputStreamReader(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("simpletemplate.jtt"));
    BufferedReader resultToCheckReader = new BufferedReader( new InputStreamReader(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("simpletemplateresult.txt")));

    String resultFileName = System.getProperty("java.io.tmpdir") + "/template.result";
    Writer writer = new FileWriter(new File(resultFileName));
    JTTEngineContext context = new JTTEngineContext();
    Map<String, String> values = new HashMap<String,String>();
    values.put("k1","v1");
    values.put("k2","v2");
    values.put("k3","v3");
    context.addBind("values", values, new GenericTypeReslover<Map<String,String>>() {});
    new JTTEngine().evaluate(reader, writer, context);
    BufferedReader resultReader = new BufferedReader( new FileReader(new File(resultFileName)));
    String lineContent;
    while ((lineContent = resultReader.readLine())!= null) {
      assertTrue(lineContent.equals(resultToCheckReader.readLine()));
    }
    if (resultReader.readLine()!= null) {
      assertFalse(true);
    }
    assertTrue(true);
  }

  @Test
  public void testInnerFunction() throws Exception {
    Reader reader = new InputStreamReader(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("innerfunction.jtt"));
    BufferedReader resultToCheckReader = new BufferedReader(new InputStreamReader(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("innerfunctionresult.txt")));

    String resultFileName = System.getProperty("java.io.tmpdir") + "/template.result";
    Writer writer = new FileWriter(new File(resultFileName));
    JTTEngineContext context = new JTTEngineContext();
    context.setSetting(JTTSettings.OUTPUT_SOURCE_FILE, "true");
    context.addBind("input", "test");
    new JTTEngine().evaluate(reader, writer, context);
    BufferedReader resultReader = new BufferedReader(new FileReader(new File(resultFileName)));
    String lineContent;
    while ((lineContent = resultReader.readLine()) != null) {
      assertTrue(lineContent.equals(resultToCheckReader.readLine()));
    }
    if (resultReader.readLine() != null) {
      assertFalse(true);
    }
    assertTrue(true);
  }

}