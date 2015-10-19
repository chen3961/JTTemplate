package org.zsource.jtt.context;

/**
 * Created by Chenzhe on 2015/10/10.
 */
public class BindObject {
  private String name;
  private Object value;

  private String className;
  private String classSimpleName;
  private Class objClass;
  private Boolean isArray = false;
  public BindObject(String name, Object value) {
    this.name = name;
    this.value = value;

    if (value != null) {
      isArray = value.getClass().isArray();
      objClass = value.getClass();
      className = value.getClass().getCanonicalName();
      classSimpleName = value.getClass().getSimpleName();
    }
    else {
      isArray = false;
      className = "java.lang.Object";
      classSimpleName = "Object";
      objClass = Object.class;
    }
  }
  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  //Use for import in finial code
  public String getImportName() {
    String itemClassname = className;
    if (isArray) {
      itemClassname = className.substring(0, className.length()-2);
    }

    return itemClassname;
  }

  public String getDeclareName() {
    return classSimpleName;
  }

  public Class getObjClass() {
    return objClass;
  }

}
