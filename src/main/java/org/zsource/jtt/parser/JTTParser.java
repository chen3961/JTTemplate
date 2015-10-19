package org.zsource.jtt.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zsource.jtt.exception.JTTException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhe on 2015/9/23.
 */
public class JTTParser {
  private static final Logger log = LogManager.getLogger(JTTParser.class);

  public enum STATE {
    TEXT,
    NON_TEXT_CHECK,
    EXP_CHECK,
    SCRIPT_COMMIT,
    SCRIPT_END_CHECK,
    EXP_COMMIT,
    EXP_END_CHECK
  }

  public Slice[] parse(Reader reader) throws JTTException {
    List<Slice> slices = new ArrayList<Slice>();

    StringBuilder currentContent = new StringBuilder();
    //init state
    STATE currentState = STATE.TEXT;
    try {
      int intval = reader.read();
      while (intval != -1) {
        char ch = (char)intval;
        currentContent.append(ch);
        switch (currentState) {
          case TEXT:
            if (ch == '<') {
              currentState = STATE.NON_TEXT_CHECK;
            }
            break;
          case NON_TEXT_CHECK:
            if (ch == '%') {
              currentState = STATE.EXP_CHECK;
            }
            else {
              currentState = STATE.TEXT;
            }
            break;
          case EXP_CHECK:
            putPreviousSlice(currentContent, slices, Slice.Type.TEXT, 3);
            //confirm the next char is start of the expression
            if (ch == '=') {
              currentState = STATE.EXP_COMMIT;
              //remove all characters, expression start from next char
              currentContent.delete(0, currentContent.length());
            }
            else {
              //confirm the current char is start of the script
              //the script end directly
              if (ch == '%') {
                currentState = STATE.SCRIPT_END_CHECK;
              }
              else {
                currentState = STATE.SCRIPT_COMMIT;
              }
              //add the current char, the script start from current char
              currentContent.delete(0, currentContent.length() - 1);
            }
            break;
          case SCRIPT_COMMIT:
            if (ch == '%') {
              currentState = STATE.SCRIPT_END_CHECK;
            }
            break;
          case EXP_COMMIT:
            if (ch == '%') {
              currentState = STATE.EXP_END_CHECK;
            }
            break;
          case SCRIPT_END_CHECK:
            if (ch == '>') {
              putPreviousSlice(currentContent, slices, Slice.Type.SCRIPT, 2);
              currentContent.delete(0, currentContent.length());
              currentState = STATE.TEXT;
            }
            else {
              currentState = STATE.SCRIPT_COMMIT;
            }
            break;
          case EXP_END_CHECK:
            if (ch == '>') {
              putPreviousSlice(currentContent, slices, Slice.Type.EXPRESSION, 2);
              currentContent.delete(0, currentContent.length());
              currentState = STATE.TEXT;
            }
            else {
              currentState = STATE.EXP_COMMIT;
            }
        }
        intval = reader.read();
      }
      if (currentContent.length() > 0) {
        putPreviousSlice(currentContent, slices, Slice.Type.EXPRESSION, 0);
      }
    }
    catch (IOException e) {
      log.error("Can not read the template source", e);
      throw new JTTException("Can not read the template source:" + e.getMessage());
    }
    return slices.toArray(new Slice[]{});
  }

  private void putPreviousSlice(StringBuilder currentContent, List<Slice> sliceHolder, Slice.Type type, int offset) {
    String textContent = currentContent.substring(0, currentContent.length() - offset);
    if (textContent.length() > 0) {
      sliceHolder.add(new Slice(textContent, type));
    }
  }
}
