/*
 * InMemoryCompiler - Compiles Java source code in memory for integration testing
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

/**
 * Utility that compiles Java source code from a String and loads the resulting
 * class into a fresh ClassLoader. Used by integration tests to verify that
 * generated Java actually compiles.
 */
final class InMemoryCompiler {

    private InMemoryCompiler() {}

    /**
     * Compile a single Java source string and return the loaded Class.
     *
     * @param fullyQualifiedClassName e.g. "com.generated.cobol.Hello"
     * @param sourceCode              the complete Java source file content
     * @return the compiled and loaded Class
     * @throws CompilationException if compilation fails
     */
    static Class<?> compile(String fullyQualifiedClassName, String sourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException(
                    "No Java compiler available. Run tests with a JDK, not a JRE.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        InMemoryFileManager fileManager = new InMemoryFileManager(
                compiler.getStandardFileManager(diagnostics, null, null));

        JavaFileObject source = new InMemorySourceFile(fullyQualifiedClassName, sourceCode);

        // Build classpath from the current thread's classloader
        String classpath = System.getProperty("java.class.path");
        List<String> options = List.of("-classpath", classpath);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, options, null, List.of(source));

        boolean success = task.call();
        if (!success) {
            StringBuilder sb = new StringBuilder("Compilation failed:\n");
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                sb.append("  Line ").append(d.getLineNumber())
                  .append(": ").append(d.getMessage(null)).append("\n");
            }
            throw new CompilationException(sb.toString(), diagnostics.getDiagnostics());
        }

        try {
            ClassLoader loader = fileManager.getClassLoader(null);
            return loader.loadClass(fullyQualifiedClassName);
        } catch (ClassNotFoundException e) {
            throw new CompilationException(
                    "Class compiled but could not be loaded: " + fullyQualifiedClassName,
                    List.of());
        }
    }

    // ── Exception ──────────────────────────────────────────────────

    static final class CompilationException extends RuntimeException {
        private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

        CompilationException(String message, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
            super(message);
            this.diagnostics = List.copyOf(diagnostics);
        }

        List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
            return diagnostics;
        }
    }

    // ── In-memory JavaFileObject for source code ───────────────────

    private static final class InMemorySourceFile extends SimpleJavaFileObject {
        private final String code;

        InMemorySourceFile(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    // ── In-memory JavaFileObject for compiled bytecode ─────────────

    private static final class InMemoryClassFile extends SimpleJavaFileObject {
        private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InMemoryClassFile(String className) {
            super(URI.create("mem:///" + className.replace('.', '/') + Kind.CLASS.extension),
                    Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() {
            return bos;
        }

        byte[] getBytes() {
            return bos.toByteArray();
        }
    }

    // ── File manager that stores compiled classes in memory ────────

    private static final class InMemoryFileManager
            extends ForwardingJavaFileManager<StandardJavaFileManager> {

        private final Map<String, InMemoryClassFile> compiled = new HashMap<>();

        InMemoryFileManager(StandardJavaFileManager delegate) {
            super(delegate);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                    JavaFileObject.Kind kind, FileObject sibling)
                throws IOException {
            InMemoryClassFile file = new InMemoryClassFile(className);
            compiled.put(className, file);
            return file;
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return new ClassLoader(getClass().getClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    InMemoryClassFile file = compiled.get(name);
                    if (file == null) throw new ClassNotFoundException(name);
                    byte[] bytes = file.getBytes();
                    return defineClass(name, bytes, 0, bytes.length);
                }
            };
        }
    }
}
