package org.zsource.jtt.context;

import org.junit.Test;

import static org.junit.Assert.*;

public class BindObjectTest {

  @Test
  public void testGetClassName() throws Exception {
    BindObject object = new BindObject("name", new Integer(1));
    assertEquals(object.getName(), "name");
    assertEquals(object.getValue(), new Integer(1));
    assertEquals(object.getImportName(), "java.lang.Integer");
    assertEquals(object.getDeclareName(), "Integer");

    object = new BindObject("name", new Integer[]{1, 2});
    assertEquals(object.getImportName(), "java.lang.Integer");
    assertEquals(object.getDeclareName(), "Integer[]");

    object = new BindObject("name", null);
    assertEquals(object.getImportName(), "java.lang.Object");
    assertEquals(object.getDeclareName(), "Object");

    int[] v = new int[] {1,2};
    object = new BindObject("name", v);
    assertEquals(object.getImportName(), "int");
    assertEquals(object.getDeclareName(), "int[]");
  }
}