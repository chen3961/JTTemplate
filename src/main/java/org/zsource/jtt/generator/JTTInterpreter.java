package org.zsource.jtt.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zsource.jtt.context.JTTEngineContext;
import org.zsource.jtt.context.JTTSettings;
import org.zsource.jtt.exception.JTTException;
import org.zsource.jtt.exception.JTTRuntimeException;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ChenZhe on 2015/10/14.
 */
public class JTTInterpreter {
  private static final Logger log = LogManager.getLogger(JTTInterpreter.class);
  public void run(final String source, final Writer out, final JTTEngineContext context) throws JTTException {
    if (context.getSetting(JTTSettings.OUTPUT_SOURCE_FILE).equals("true")) {
      String outputDir = context.getSetting(JTTSettings.WORKING_DIR);
      try {
        Files.write(Paths.get(outputDir, context.getTemplateName()), source.getBytes(Charset.forName("utf-8")),
            StandardOpenOption.CREATE);
      } catch (IOException e) {
        throw new JTTException("Can not output the generated source file to " + outputDir + e.getMessage());
      }
    }
    Future handler = null;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      handler = executor.submit(new Callable<Object>() {
        @Override
        public Object call() throws Exception {
          JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
          if (compiler == null) {
            throw new JTTException("Compiler can not find. Please add tools.jar under JDK into the classpath");
          }
          DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

          JavaFileObject file = new JavaSourceFromString("org.zsource.jtt.instance." + context.getTemplateName(), source);
          Writer addOut = new StringWriter();
          InMemoryJavaClassObject outputInmemory = new InMemoryJavaClassObject(
              "org.zsource.jtt.instance." + context.getTemplateName());
          Map<String, InMemoryJavaClassObject> subClassObjects = new HashMap<String, InMemoryJavaClassObject>();
          subClassObjects.put(outputInmemory.getName(), outputInmemory);
          JavaCompiler.CompilationTask task = compiler.getTask(addOut,
              new InMemoryClassFileManager(compiler.getStandardFileManager(null, null, null),
                  outputInmemory, subClassObjects),
              diagnostics, null, null, Arrays.asList(file));
          boolean issuccessful = task.call();
          log.info("Compiler output:" + addOut.toString());
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
            ClassLoader memoryClassLoader = new MemoryClassLoader(Thread.currentThread().getContextClassLoader(),
                subClassObjects);
            Thread.currentThread().setContextClassLoader(memoryClassLoader);
            Class targetClass = memoryClassLoader.loadClass(outputInmemory.getName());

            List<Class> parameterClasses = context.getBindingClass();
            parameterClasses.add(0, Writer.class);
            Method method = targetClass.getMethod("generate", parameterClasses.toArray(new Class[]{}));
            List<Object> parameters = context.getBindingValues();
            parameters.add(0, out);
            method.invoke(targetClass.newInstance(), parameters.toArray());
          } catch (NoSuchMethodException e) {
            throw new JTTException("No method named with generator:" + e.getMessage());
          } catch (InstantiationException e) {
            throw new JTTException("Can not create the instance of the target class:" + e.getMessage());
          } catch (IllegalAccessException e) {
            throw new JTTException("Can not create the instance of the target class:" + e.getMessage());
          } catch (InvocationTargetException e) {
            throw new JTTException("Can not invoke the generate method:" + e.getMessage());
          } catch (Exception e) {
            if (e instanceof JTTRuntimeException) {
              JTTSettings.ExceptionPolicy policy = JTTSettings.ExceptionPolicy.valueOf(
                  context.getSetting(JTTSettings.RUNTIME_EXCEPTION_POLICY));
              if (policy == JTTSettings.ExceptionPolicy.THROW) {
                throw new JTTException(e.getMessage());
              }
            }
          }
          return null;
        }
      });
      handler.get(Long.parseLong(context.getSetting(JTTSettings.TIMEOUT_THRESHOLD)), TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      throw new JTTException("Evaluate template timeout in " + context.getSetting(JTTSettings.DEFAULT_TIMEOUT_THRESHOLD)
          + "seconds, please extend the timeout threshold or check your template");
    } catch (InterruptedException e) {
      throw new JTTException("Evaluate process has been interrupted, " + e.getMessage());
    } catch (ExecutionException e) {
      throw new JTTException("Evaluate encounter error, " + e.getMessage());
    }
    finally {
      if (handler != null && !handler.isDone() && !handler.isCancelled()) {
        handler.cancel(true);
      }
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
  private Map<String, InMemoryJavaClassObject> subClassItems;
  InMemoryClassFileManager(JavaFileManager fileManager, InMemoryJavaClassObject classObject,
                           Map<String, InMemoryJavaClassObject> subClassItems) {
    super(fileManager);
    this.classObject = classObject;
    this.subClassItems = subClassItems;
  }

  public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
                                          String className,
                                          JavaFileObject.Kind kind, FileObject sibling) throws IOException {
    if (kind == JavaFileObject.Kind.CLASS && className.equals(this.classObject.getName())) {
      return classObject;
    }
    else if (kind == JavaFileObject.Kind.CLASS && className.startsWith(this.classObject.getName())) {
      InMemoryJavaClassObject subItem = new InMemoryJavaClassObject(className);
      subClassItems.put(className, subItem);
      return subItem;
    }
    else {
      return super.getJavaFileForOutput(location, className, kind, sibling);
    }
  }
}

class MemoryClassLoader extends ClassLoader {
  final private Map<String, InMemoryJavaClassObject> inMemoryJavaClassObjects;
  public MemoryClassLoader(ClassLoader parent, Map<String, InMemoryJavaClassObject> memoryClasses) {
    super(parent);
    inMemoryJavaClassObjects = memoryClasses;
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    if (inMemoryJavaClassObjects.containsKey(name)) {
      return formInMemoryObject(inMemoryJavaClassObjects.get(name));
    }
    return super.loadClass(name);
  }

  Class<?> formInMemoryObject(InMemoryJavaClassObject object) {
    byte[] byteCode = object.getOutput();
    return defineClass(object.getName(), byteCode, 0, byteCode.length);
  }


}
