package org.zsource.jtt;

/**
 * Created by ChenZhe on 2015/10/16.
 */
public class TestValueObject {
  private String name;
  private int value;

  public TestValueObject (String name, int value) {
    this.name = name;
    this.value = value;
  }
  public String upper() {
    return name.toUpperCase();
  }
  public int add(int toadd) {
    return value + toadd;
  }
}
