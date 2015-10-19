package org.zsource.jtt.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zsource.jtt.context.JTTEngineContext;
import org.zsource.jtt.exception.JTTException;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ChenZhe on 2015/10/14.
 */
public class JTTInterpreter {
  private static final Logger log = LogManager.getLogger(JTTInterpreter.class);
  public void run(String source, Writer out, JTTEngineContext context) throws JTTException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    JavaFileObject file = new JavaSourceFromString("org.zsource.jtt.instance." + context.getTemplateName(), source);

    Writer addOut = new StringWriter();
    InMemoryJavaClassObject outputInmemory = new InMemoryJavaClassObject(
        "org.zsource.jtt.instance." + context.getTemplateName());
    JavaCompiler.CompilationTask task = compiler.getTask(addOut,
        new InMemoryClassFileManager(compiler.getStandardFileManager(null, null, null),
            outputInmemory),
        diagnostics, null, null, Arrays.asList(file));
    boolean issuccessful = task.call();
    log.info(addOut.toString());
    if (!issuccessful) {
      StringBuilder errorBuilder = new StringBuilder();
      errorBuilder.append("Compile occurred:");
      errorBuilder.append("\n");
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        errorBuilder.append(diagnostic.toString());
      }
      throw new JTTException(errorBuilder.toString());
    }

    try {

      Class targetClass = new MemoryClassLoader(Thread.currentThread().getContextClassLoader()).formInMemoryObject(outputInmemory);

      List<Class> parameterClasses = context.getBindingClass();
      parameterClasses.add(0, Writer.class);
      Method method = targetClass.getMethod("generate", parameterClasses.toArray(new Class[]{}));
      List<Object> parameters = context.getBindingValues();
      parameters.add(0, out);
      method.invoke(targetClass.newInstance(), parameters.toArray());
    }
//    catch (ClassNotFoundException e) {
//      throw new JTTException("Can not load the target class:" + e.getMessage());
//    }
    catch (NoSuchMethodException e) {
      throw new JTTException("No method named with generator:" + e.getMessage());
    }
    catch (InstantiationException e) {
      throw new JTTException("Can not create the instance of the target class:" + e.getMessage());
    }
    catch (IllegalAccessException e) {
      throw new JTTException("Can not create the instance of the target class:" + e.getMessage());
    }
    catch (InvocationTargetException e) {
      throw new JTTException("Can not invoke the generate method:" + e.getMessage());
    }
  }
}

class JavaSourceFromString extends SimpleJavaFileObject {
  final String code;

  JavaSourceFromString(String name, String code) {
    super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
    this.code = code;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }
}

class InMemoryJavaClassObject extends SimpleJavaFileObject {
  private ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
  private String className;

  InMemoryJavaClassObject(String className) {
    super(URI.create("string:///" + className.replace('.',  '/') + Kind.CLASS.extension), Kind.CLASS);
    this.className = className;
  }

  public OutputStream openOutputStream() throws IOException {
    return outputBuffer;
  }

  public byte[] getOutput() {
    return outputBuffer.toByteArray();
  }

  public String getName() {
    return className;
  }

}

class InMemoryClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {
  private InMemoryJavaClassObject classObject;
  InMemoryClassFileManager(JavaFileManager fileManager, InMemoryJavaClassObject classObject) {
    super(fileManager);
    this.classObject = classObject;
  }

  public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
                                          String className,
                                          JavaFileObject.Kind kind, FileObject sibling) throws IOException {
    if (kind == JavaFileObject.Kind.CLASS && className.equals(this.classObject.getName())) {
      return classObject;
    }
    else {
      return super.getJavaFileForOutput(location, className, kind, sibling);
    }
  }
}

class MemoryClassLoader extends ClassLoader {
  public MemoryClassLoader(ClassLoader parent) {
    super(parent);
  }

  Class<?> formInMemoryObject(InMemoryJavaClassObject object) {
    byte[] byteCode = object.getOutput();
    return defineClass(object.getName(), byteCode, 0, byteCode.length);
  }
}
