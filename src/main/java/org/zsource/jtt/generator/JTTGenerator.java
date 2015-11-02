package org.zsource.jtt.generator;

import org.apache.commons.lang3.StringUtils;
import org.zsource.jtt.context.JTTEngineContext;
import org.zsource.jtt.parser.Slice;

import java.util.*;

/**
 * Created by Chenzhe on 2015/10/9.
 */
public class JTTGenerator {
  public String genCode(Slice[] slices, JTTEngineContext context) {
    appendInternalBinding(slices, context);
    String packageSegment = "package org.zsource.jtt.instance;";
    String importSegment = genImportSegment(context);
    String instanceName = context.getTemplateName();
    String classSegmentStart = "public class " + instanceName + "{";
    String declareSegment = genDeclareSegment(context);
    String functionSegementStart = genMethodSegmentStart(context);
    String bodySegment = genBodySegment(slices);
    String end = "}";
    String full = StringUtils.join(Arrays.asList(new String[]{
        packageSegment, importSegment, classSegmentStart,
        declareSegment, functionSegementStart, bodySegment,
        end, end}), "\n");
    return full;
  }

  private void appendInternalBinding(Slice[] slices, JTTEngineContext context) {
    List<String> textSlice = new ArrayList<String>();
    for (Slice sliceItem : slices) {
      if (sliceItem.getType() == Slice.Type.TEXT) {
        textSlice.add(sliceItem.getContent());
      }
    }
    context.addBind("_textSlice", textSlice.toArray(new String[]{}));
  }

  private String genImportSegment(JTTEngineContext context) {
    StringBuilder content = new StringBuilder();
    Set<String> imports = context.getImportSet();
    imports.add("java.io.Writer");
    imports.add("org.zsource.jtt.exception.JTTRuntimeException");
    for (String importItem : imports) {
      content.append("import " + importItem + ";");
      content.append("\n");
    }
    return content.toString();
  }

  private String genDeclareSegment(JTTEngineContext context) {
    StringBuilder content = new StringBuilder();
    List<Map.Entry<String,String>> itemList = context.getDeclareItemList();
    for (Map.Entry item : itemList) {
      content.append("  private " + item.getValue() + " " + item.getKey() + ";");
      content.append("\n");
    }
    return content.toString();
  }

  private String genMethodSegmentStart(JTTEngineContext context) {
    StringBuilder content = new StringBuilder();
    content.append("  public void generate(Writer _out");
    List<Map.Entry<String,String>> itemList = context.getDeclareItemList();
    for (Map.Entry item : itemList) {
      content.append(", " + item.getValue() + " " + item.getKey());
    }
    content.append(") throws JTTRuntimeException");
    content.append(" {\n");
    return content.toString();
  }

  private String genBodySegment(Slice[] slices) {
    StringBuilder bodyContent = new StringBuilder();
    bodyContent.append("try {");
    int textIndex = 0;
    for (Slice sliceItem : slices) {

      String content = sliceItem.getContent();
      if (sliceItem.getType() == Slice.Type.TEXT) {
        bodyContent.append("\t_out.write(_textSlice[" + String.valueOf(textIndex++) + "]);\n");
      }
      else if (sliceItem.getType() == Slice.Type.SCRIPT) {
        bodyContent.append("\t" + content);
      }
      else if (sliceItem.getType() == Slice.Type.EXPRESSION) {
        bodyContent.append("\t_out.write(String.valueOf(" + content + "));\n");
      }
    }
    bodyContent.append("}");
    bodyContent.append("\n");
    bodyContent.append("catch(Exception e) {");
    bodyContent.append("throw new JTTRuntimeException(e);");
    bodyContent.append("}");
    return bodyContent.toString();
  }
}
