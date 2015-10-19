package org.zsource.jtt.generator;

import com.sun.deploy.util.StringUtils;
import org.zsource.jtt.context.JTTEngineContext;
import org.zsource.jtt.parser.Slice;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Chenzhe on 2015/10/9.
 */
public class JTTGenerator {
  public String genCode(Slice[] slices, JTTEngineContext context) {
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

  private String genImportSegment(JTTEngineContext context) {
    StringBuilder content = new StringBuilder();
    Set<String> imports = context.getImportSet();
    imports.add("java.io.Writer");
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
    content.append("  public void generate(Writer out");
    List<Map.Entry<String,String>> itemList = context.getDeclareItemList();
    for (Map.Entry item : itemList) {
      content.append(", " + item.getValue() + " " + item.getKey());
    }
    content.append(")");
    content.append(" {\n");
    return content.toString();
  }

  private String genBodySegment(Slice[] slices) {
    StringBuilder bodyContent = new StringBuilder();
    bodyContent.append("try {");
    for (Slice sliceItem : slices) {
      String content = sliceItem.getContent();
      if (sliceItem.getType() == Slice.Type.TEXT) {
        String encodedText = content.replaceAll("\"", "\\\"");
        bodyContent.append("\tout.write(\"" + encodedText + "\");");
      }
      else if (sliceItem.getType() == Slice.Type.SCRIPT) {
        bodyContent.append("\t" + content);
      }
      else if (sliceItem.getType() == Slice.Type.EXPRESSION) {
        bodyContent.append("\tout.write(String.valueOf(" + content + "));");
      }
    }
    bodyContent.append("}");
    bodyContent.append("\n");
    bodyContent.append("catch(Exception e) {");
    bodyContent.append("}");
    return bodyContent.toString();
  }
}
