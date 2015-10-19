package org.zsource.jtt.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhe on 2015/9/23.
 */
public class Slice {
  public enum Type{
    TEXT,
    SCRIPT,
    EXPRESSION
  }
  protected String rawContent;
  protected Type type;
  public Slice(String content, Type type) {
    this.rawContent = content;
    this.type = type;
  }

  public String getContent() {
    return rawContent;
  }

  public Type getType() {
    return type;
  }
}


