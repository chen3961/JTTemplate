package org.zsource.jtt.context;

import com.sun.deploy.util.StringUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by ChenZhe on 2015/9/23.
 */
public class JTTEngineContext {
  private TreeMap<String, BindObject> bindObjectMap;
  private String templateName;
  private String classPath;

  public JTTEngineContext() {
    bindObjectMap = new TreeMap<String, BindObject>();
    templateName = "MyTemplate";
    buildRuntimeClasspath();
  }

  private void buildRuntimeClasspath() {
    List<String> paths = new ArrayList<String>();
    URLClassLoader loader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
    for (URL item : loader.getURLs()) {
      paths.add(item.getFile());
    }
    classPath = StringUtils.join(paths, ":");
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public void setClassPath(String classPath) {
    this.classPath = classPath;
  }

  public String getClassPath() {
    return this.classPath;
  }

  public String getTemplateName() {
    return this.templateName;
  }

  public Set<String> getImportSet() {
    Set<String> result = new HashSet<String>();
    for (BindObject object : bindObjectMap.values()) {
      try {
        if (!Class.forName(object.getImportName()).isPrimitive()) {
          result.add(object.getImportName());
        }
      }
      catch (ClassNotFoundException e) {
        //should not happened
      }
    }
    return result;
  }

  public List<Map.Entry<String, String>> getDeclareItemList() {
    List<Map.Entry<String,String>> result = new ArrayList<Map.Entry<String, String>>();
    Iterator<String> keyIt = bindObjectMap.navigableKeySet().iterator();
    while(keyIt.hasNext()) {
      String key = keyIt.next();
      result.add(new AbstractMap.SimpleEntry<String,String>(key, bindObjectMap.get(key).getDeclareName()));
    }
    return result;
  }

  public List<Class> getBindingClass() {
    List<Class> result = new ArrayList<Class>();
    Iterator<String> keyIt = bindObjectMap.navigableKeySet().iterator();
    while(keyIt.hasNext()) {
      String key = keyIt.next();
      result.add(bindObjectMap.get(key).getObjClass());
    }
    return result;
  }

  public List<Object> getBindingValues() {
    List<Object> result = new ArrayList<Object>();
    Iterator<String> keyIt = bindObjectMap.navigableKeySet().iterator();
    while(keyIt.hasNext()) {
      String key = keyIt.next();
      result.add(bindObjectMap.get(key).getValue());
    }
    return result;
  }

  public void addBind(String name, Object value) {
    bindObjectMap.put(name, new BindObject(name, value));
  }

  public void addBind(String name, int value) {
    bindObjectMap.put(name, new BindObject(name, new Integer(value)));
  }

  public void addBind(String name, float value) {
    bindObjectMap.put(name, new BindObject(name, new Float(value)));
  }

  public void addBind(String name, boolean value) {
    bindObjectMap.put(name, new BindObject(name, new Boolean(value)));
  }

  public void addBind(String name, double value) {
    bindObjectMap.put(name, new BindObject(name, new Double(value)));
  }

  public void addBind(String name, char value) {
    bindObjectMap.put(name, new BindObject(name, new Character(value)));
  }

  public void addBind(String name, byte value) {
    bindObjectMap.put(name, new BindObject(name, new Byte(value)));
  }
}
