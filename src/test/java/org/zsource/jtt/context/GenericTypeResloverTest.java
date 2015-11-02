package org.zsource.jtt.context;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericTypeResloverTest {
  @Test
  public void testGetGenericeType() {
    GenericTypeReslover<Map<String,String>> testObj= new GenericTypeReslover<Map<String, String>>(){};
    String[] result = testObj.getTypeClassSet();
    List<String> resultList = Arrays.asList(result);
    assertTrue(resultList.contains("java.util.Map"));
    assertTrue(resultList.contains("java.lang.String"));
    assertEquals("Map<String,String>", testObj.getTargetClassDeclareName());

    GenericTypeReslover<Map<List<Integer>, String>> testObj2=
        new GenericTypeReslover<Map<List<Integer>, String>>(){};
    result = testObj2.getTypeClassSet();
    resultList = Arrays.asList(result);
    assertTrue(resultList.contains("java.util.Map"));
    assertTrue(resultList.contains("java.lang.String"));
    assertTrue(resultList.contains("java.lang.Integer"));
    assertTrue(resultList.contains("java.util.List"));

    assertEquals("Map<List<Integer>,String>", testObj2.getTargetClassDeclareName());
    assertTrue(result.length > 0);

    GenericTypeReslover<Map<String, String[]>> testObj3=
        new GenericTypeReslover<Map<String, String[]>>(){};
    result = testObj3.getTypeClassSet();
    resultList = Arrays.asList(result);
    assertTrue(resultList.contains("java.util.Map"));
    assertTrue(resultList.contains("java.lang.String"));
    assertEquals(result.length, 2);
    assertEquals("Map<String,String[]>", testObj3.getTargetClassDeclareName());
    assertTrue(result.length > 0);
  }

}