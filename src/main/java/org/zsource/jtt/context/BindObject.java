package org.zsource.jtt.context;

/**
 * Created by Chenzhe on 2015/10/10.
 */
public class BindObject {
  private String name;
  private Object value;

  private String[] className;
  private String classSimpleName;
  private Class objClass;
  private Boolean isArray = false;
  public BindObject(String name, Object value) {
    this.name = name;
    this.value = value;

    if (value != null) {
      isArray = value.getClass().isArray();
      objClass = value.getClass();
      if (isArray) {
        className = new String[]{value.getClass().getCanonicalName().substring(0,
            value.getClass().getCanonicalName().length() - 2)};
      } else {
        className = new String[]{value.getClass().getCanonicalName()};
      }

      classSimpleName = value.getClass().getSimpleName();
    }
    else {
      isArray = false;
      className = new String[]{"java.lang.Object"};
      classSimpleName = "Object";
      objClass = Object.class;
    }
  }

  public BindObject(String name, Object value, GenericTypeReslover genericTypeReslover) {
    this(name, value);
    className = genericTypeReslover.getTypeClassSet();
    classSimpleName = genericTypeReslover.getTargetClassDeclareName();
    objClass = genericTypeReslover.getTargetClass();
  }
  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  //Use for import in finial code
  public String[] getImportNames() {
    return className;
  }

  public String getDeclareName() {
    return classSimpleName;
  }

  public Class getObjClass() {
    return objClass;
  }

}
