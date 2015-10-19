package org.zsource.jtt.parser;

import java.io.StringReader;

import static org.junit.Assert.*;

public class JTTParserTest {

  @org.junit.Before
  public void setUp() throws Exception {

  }

  @org.junit.Test
  public void testParse() throws Exception {
    JTTParser parser = new JTTParser();
    Slice[] slices = parser.parse(new StringReader("abcdefg"));
    assertEquals(slices.length, 1);
    assertEquals(slices[0].getContent(), "abcdefg");

    slices = parser.parse(new StringReader("abcdefg<% hijk %>"));
    assertEquals(slices.length, 2);
    assertEquals(slices[1].getContent(), " hijk ");

    slices = parser.parse(new StringReader("abcdefg<% hijk %>lmn<%=opq%>"));
    assertEquals(slices.length, 4);
    assertEquals(slices[2].getContent(), "lmn");
    assertEquals(slices[2].getType(), Slice.Type.TEXT);
    assertEquals(slices[3].getContent(), "opq");
    assertEquals(slices[3].getType(), Slice.Type.EXPRESSION);

    slices = parser.parse(new StringReader("abcdefg%< hijk %>lmn<%=opq%>"));
    assertEquals(slices.length, 2);
    assertEquals(slices[0].getContent(), "abcdefg%< hijk %>lmn");
    assertEquals(slices[1].getContent(), "opq");
    assertEquals(slices[1].getType(), Slice.Type.EXPRESSION);

    slices = parser.parse(new StringReader("abcdefg<% h<i%j>k %>lmn\n<%=op%q%>"));
    assertEquals(slices.length, 4);
    assertEquals(slices[1].getContent(), " h<i%j>k ");
    assertEquals(slices[3].getContent(), "op%q");

    slices = parser.parse(new StringReader("abcdefg<%%>lmn\n<%=op%q%>>"));
    assertEquals(slices.length, 4);
    assertEquals(slices[1].getContent(), "lmn\n");
    assertEquals(slices[2].getContent(), "op%q");
    assertEquals(slices[3].getContent(), ">");

    slices = parser.parse(new StringReader("abcdefg<%%>lmn\n<%=%>>"));
    assertEquals(slices.length, 3);
    assertEquals(slices[1].getContent(), "lmn\n");
    assertEquals(slices[2].getContent(), ">");
  }
}