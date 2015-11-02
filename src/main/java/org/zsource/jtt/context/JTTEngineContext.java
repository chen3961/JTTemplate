package org.zsource.jtt.context;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by ChenZhe on 2015/9/23.
 */
public class JTTEngineContext {
  private static final Logger log = LogManager.getLogger(JTTEngineContext.class);
  private static final int RANDOM_CLASS_NAME_LENGTH = 10;
  private TreeMap<String, BindObject> bindObjectMap;
  private String templateName;
  private Map<String,String> settings = new HashMap<String, String>();

  public JTTEngineContext() {
    bindObjectMap = new TreeMap<String, BindObject>();
    templateName = RandomStringUtils.randomAlphabetic(RANDOM_CLASS_NAME_LENGTH);
    buildDefaultSettings();
  }

  protected void buildDefaultSettings() {
    settings.put(JTTSettings.WORKING_DIR, System.getProperty("java.io.tmpdir"));
    settings.put(JTTSettings.OUTPUT_SOURCE_FILE, JTTSettings.DEFAULT_OUTPUT_SOUECE_FILE_OPTION);
    settings.put(JTTSettings.TIMEOUT_THRESHOLD, JTTSettings.DEFAULT_TIMEOUT_THRESHOLD);
    settings.put(JTTSettings.RUNTIME_EXCEPTION_POLICY, JTTSettings.ExceptionPolicy.THROW.name());
  }

  public void setSetting(String name, String value) {
    if (settings.containsKey(name)) {
      settings.put(name, value);
    }
    else {
      log.warn("Not supported setting " + name);
    }
  }

  public String getSetting(String name) {
    return settings.get(name);
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getTemplateName() {
    return this.templateName;
  }

  public Set<String> getImportSet() {
    Set<String> result = new HashSet<String>();
    for (BindObject object : bindObjectMap.values()) {
      try {
        for (String importName : object.getImportNames()) {
          if (!Class.forName(importName).isPrimitive()) {
            result.add(importName);
          }
        }
      }
      catch (ClassNotFoundException e) {
        log.warn("Can not find the class for " + object.getName() + ", ignore this item");
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

  public void addBind(String name, Object value, GenericTypeReslover typeReslover) {
    bindObjectMap.put(name, new BindObject(name, value, typeReslover));
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
