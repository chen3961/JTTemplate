package org.zsource.jtt.context;

/**
 * Created by ChenZhe on 2015/10/21.
 */
public class JTTSettings {
  public static final String WORKING_DIR = "WORKING_DIR";

  public static final String TIMEOUT_THRESHOLD = "TIMEOUT_THRESHOLD";
  //300s
  public static final String DEFAULT_TIMEOUT_THRESHOLD = "300";

  public static final String OUTPUT_SOURCE_FILE = "OUTPUT_SOURCE_FILE";
  public static final String DEFAULT_OUTPUT_SOUECE_FILE_OPTION = "false";


  public static final String RUNTIME_EXCEPTION_POLICY = "RUNTIME_EXECEPTION_POLICY";

  public enum ExceptionPolicy {
    THROW,
    IGNORE
  }
}
