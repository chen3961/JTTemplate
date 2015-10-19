package org.zsource.jtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zsource.jtt.context.JTTEngineContext;
import org.zsource.jtt.exception.JTTException;
import org.zsource.jtt.generator.JTTGenerator;
import org.zsource.jtt.generator.JTTInterpreter;
import org.zsource.jtt.parser.JTTParser;
import org.zsource.jtt.parser.Slice;

import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/**
 * Created by ChenZhe on 2015/9/23.
 */
public class JTTEngine {
  private static final Logger log = LogManager.getLogger(JTTEngine.class);

  public static final String WORKING_DIR = "WORKING_DIR";
  private String workingDir;
  public JTTEngine() {
    workingDir = System.getProperty("java.io.tmpdir");
  }

  public JTTEngine(Properties props) {
    workingDir = props.getProperty(WORKING_DIR);
    if (workingDir == null) {
      workingDir = System.getProperty("java.io.tmpdir");
    }
  }

  public void evaluate(Reader reader, Writer writer, JTTEngineContext context) throws JTTException{
    Slice[] content = new JTTParser().parse(reader);
    String source = new JTTGenerator().genCode(content, context);
    new JTTInterpreter().run(source, writer, context);
  }
}
