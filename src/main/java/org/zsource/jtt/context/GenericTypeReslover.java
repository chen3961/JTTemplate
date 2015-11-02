package org.zsource.jtt.context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ChenZhe on 2015/10/26.
 */
public abstract class GenericTypeReslover<T> {
  Type targetType;
  StringBuilder targetClassDeclareName;
  Set<String> classSet;

  protected GenericTypeReslover(){
    ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
    targetType = parameterizedType.getActualTypeArguments()[0];
    targetClassDeclareName = new StringBuilder();
    classSet = new HashSet<String>();
    resloveInternal();
  }

  public Class getTargetClass() {
    return (Class) ((ParameterizedType) targetType).getRawType();
  }

  public String getTargetClassDeclareName() {
    return targetClassDeclareName.toString();
  }

  private void resloveInternal() {
    classSet = new HashSet<String>();
    addTypeInner(classSet, targetType);
  }

  public String[] getTypeClassSet() {
    return classSet.toArray(new String[]{});
  }

  private void addTypeInner(Set<String> classSet, Type type) {
    if (type instanceof ParameterizedType) {
      Type rawType = ((ParameterizedType) type).getRawType();
      addTypeInner(classSet, rawType);
      Type[] subTypes = ((ParameterizedType) type).getActualTypeArguments();
      if (subTypes.length > 0) {
        targetClassDeclareName.append("<");
        for (Type subTypeItem : subTypes) {
          addTypeInner(classSet, subTypeItem);
          targetClassDeclareName.append(",");
        }
        targetClassDeclareName.replace(targetClassDeclareName.length() - 1,
            targetClassDeclareName.length(), ">");
      }
    }
    else if (type instanceof Class){
      Class classItem = (Class) type;
      if (classItem.isArray()) {
        classSet.add(classItem.getCanonicalName().substring(0, classItem.getCanonicalName().length() - 2));
      }
      else if (!classItem.isPrimitive()) {
        classSet.add(classItem.getCanonicalName());
      }
      targetClassDeclareName.append(((Class) type).getSimpleName());
    }
  }
}
