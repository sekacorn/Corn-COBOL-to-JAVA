/*
 * CornServer - Lightweight HTTP API for the CORN demo UI
 * Uses JDK built-in com.sun.net.httpserver — zero extra dependencies.
 * Author: Sekacorn
 * Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sekacorn.corn.codegen.GeneratedClass;
import com.sekacorn.corn.codegen.JavaCodeGenerator;
import com.sekacorn.corn.ir.DataDivision;
import com.sekacorn.corn.ir.Paragraph;
import com.sekacorn.corn.ir.Program;
import com.sekacorn.corn.ir.ProcedureDivision;
import com.sekacorn.corn.ir.SourceMetadata;
import com.sekacorn.corn.parser.CobolSourceParser;
import com.sekacorn.corn.parser.ParseError;
import com.sekacorn.corn.parser.ParseResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class CornServer {

    private static final int DEFAULT_PORT = 8085;
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final int port;
    private final Path staticDir;

    public CornServer(int port, Path staticDir) {
        this.port = port;
        this.staticDir = staticDir.toAbsolutePath().normalize();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // API endpoints
        server.createContext("/api/translate", this::handleTranslate);
        server.createContext("/api/analyze", this::handleAnalyze);
        server.createContext("/api/health", this::handleHealth);

        // Static files (demo-ui)
        server.createContext("/", this::handleStatic);

        server.start();
        System.out.println("===========================================");
        System.out.println("  CORN Demo Server");
        System.out.println("  http://localhost:" + port);
        System.out.println("  Static files: " + staticDir.toAbsolutePath());
        System.out.println("  Endpoints:");
        System.out.println("    POST /api/translate   (COBOL -> Java)");
        System.out.println("    POST /api/analyze     (COBOL -> metrics)");
        System.out.println("    GET  /api/health");
        System.out.println("===========================================");
    }

    // ---------------------------------------------------------------
    // POST /api/translate
    // Body: { "source": "<cobol>", "packageName": "com.generated.cobol" }
    // Returns: { "java": "<source>", "className": "...", "errors": [...], "success": true }
    // ---------------------------------------------------------------
    private void handleTranslate(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        if (!requirePost(ex)) return;

        try {
            Map<String, Object> body = readJsonBody(ex);
            String source = (String) body.getOrDefault("source", "");
            String pkg = (String) body.getOrDefault("packageName", "com.generated.cobol");

            if (source.isBlank()) {
                sendJson(ex, 400, Map.of("success", false, "error", "No COBOL source provided"));
                return;
            }

            // Parse
            ParseResult result = CobolSourceParser.parseString(
                    source, "demo-input.cbl", SourceMetadata.CobolDialect.ANSI_85);

            List<Map<String, Object>> diagnostics = formatErrors(result.errors());

            if (result.program() == null) {
                sendJson(ex, 200, Map.of(
                        "success", false,
                        "java", "",
                        "error", "Parse failed — no program IR produced",
                        "diagnostics", diagnostics
                ));
                return;
            }

            // Generate Java
            JavaCodeGenerator gen = new JavaCodeGenerator().withPackage(pkg);
            GeneratedClass generated = gen.generate(result.program());
            String javaSource = generated.render();

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("java", javaSource);
            resp.put("className", generated.getClassName());
            resp.put("fileName", generated.getFileName());
            resp.put("programId", result.program().getProgramId());
            resp.put("diagnostics", diagnostics);
            resp.put("hasErrors", result.hasErrors());

            sendJson(ex, 200, resp);
        } catch (Exception e) {
            sendJson(ex, 500, Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    // ---------------------------------------------------------------
    // POST /api/analyze
    // Body: { "source": "<cobol>" }
    // Returns: metrics, diagnostics, division breakdown, features
    // ---------------------------------------------------------------
    private void handleAnalyze(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        if (!requirePost(ex)) return;

        try {
            Map<String, Object> body = readJsonBody(ex);
            String source = (String) body.getOrDefault("source", "");

            if (source.isBlank()) {
                sendJson(ex, 400, Map.of("success", false, "error", "No COBOL source provided"));
                return;
            }

            ParseResult result = CobolSourceParser.parseString(
                    source, "demo-input.cbl", SourceMetadata.CobolDialect.ANSI_85);

            List<Map<String, Object>> diagnostics = formatErrors(result.errors());

            String[] lines = source.split("\n");
            int totalLines = lines.length;

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("diagnostics", diagnostics);
            resp.put("totalLines", totalLines);
            resp.put("hasErrors", result.hasErrors());

            if (result.program() != null) {
                Program p = result.program();
                resp.put("programId", p.getProgramId());

                // Count paragraphs
                int paragraphCount = 0;
                int statementCount = 0;
                if (p.getProcedure() != null) {
                    ProcedureDivision proc = p.getProcedure();
                    for (Paragraph para : proc.getParagraphs()) {
                        paragraphCount++;
                        statementCount += para.getStatements().size();
                    }
                    for (ProcedureDivision.Section sec : proc.getSections()) {
                        for (Paragraph para : sec.getParagraphs()) {
                            paragraphCount++;
                            statementCount += para.getStatements().size();
                        }
                    }
                }

                // Count data items
                int dataItemCount = 0;
                int copybookCount = 0;
                if (p.getData() != null) {
                    DataDivision data = p.getData();
                    dataItemCount += data.getWorkingStorage().size();
                    dataItemCount += data.getLinkageSection().size();
                    dataItemCount += data.getLocalStorage().size();
                    for (DataDivision.FileSection fs : data.getFileSection()) {
                        dataItemCount += fs.getRecords().size();
                    }
                }

                // Complexity heuristic
                String complexity;
                if (statementCount <= 10) complexity = "Low";
                else if (statementCount <= 30) complexity = "Medium";
                else complexity = "High";

                resp.put("paragraphs", paragraphCount);
                resp.put("statements", statementCount);
                resp.put("dataItems", dataItemCount);
                resp.put("copybooks", copybookCount);
                resp.put("complexity", complexity);

                // Division breakdown (approximate by line scanning)
                int idLines = 0, envLines = 0, dataLines = 0, procLines = 0;
                String currentDiv = "";
                for (String line : lines) {
                    String trimmed = line.trim().toUpperCase();
                    if (trimmed.contains("IDENTIFICATION DIVISION")) currentDiv = "ID";
                    else if (trimmed.contains("ENVIRONMENT DIVISION")) currentDiv = "ENV";
                    else if (trimmed.contains("DATA DIVISION")) currentDiv = "DATA";
                    else if (trimmed.contains("PROCEDURE DIVISION")) currentDiv = "PROC";

                    switch (currentDiv) {
                        case "ID" -> idLines++;
                        case "ENV" -> envLines++;
                        case "DATA" -> dataLines++;
                        case "PROC" -> procLines++;
                    }
                }
                int nonEmpty = Math.max(1, idLines + envLines + dataLines + procLines);
                resp.put("divisionBreakdown", Map.of(
                        "identification", pct(idLines, nonEmpty),
                        "environment", pct(envLines, nonEmpty),
                        "data", pct(dataLines, nonEmpty),
                        "procedure", pct(procLines, nonEmpty)
                ));

                // Feature detection by scanning source
                List<String> features = new ArrayList<>();
                String upper = source.toUpperCase();
                if (upper.contains("COMPUTE")) features.add("COMPUTE");
                if (upper.contains("PERFORM")) features.add("PERFORM");
                if (upper.contains("DISPLAY")) features.add("DISPLAY");
                if (upper.contains("MOVE")) features.add("MOVE");
                if (upper.matches("(?s).*\\bADD\\b.*") || upper.matches("(?s).*\\bSUBTRACT\\b.*")
                        || upper.matches("(?s).*\\bMULTIPLY\\b.*") || upper.matches("(?s).*\\bDIVIDE\\b.*")) features.add("Arithmetic");
                if (upper.matches("(?s).*\\bIF\\b.*")) features.add("IF/ELSE");
                if (upper.matches("(?s).*\\bEVALUATE\\b.*")) features.add("EVALUATE");
                if (upper.matches("(?s).*\\bSTRING\\b.*") || upper.matches("(?s).*\\bUNSTRING\\b.*")) features.add("STRING/UNSTRING");
                if (upper.matches("(?s).*\\bINSPECT\\b.*")) features.add("INSPECT");
                if (upper.matches("(?s).*\\bOPEN\\b.*") || upper.matches("(?s).*\\bREAD\\b.*") || upper.matches("(?s).*\\bWRITE\\b.*")) features.add("File I/O");
                if (upper.matches("(?s).*\\bCALL\\b.*")) features.add("CALL");
                if (upper.contains("COPY ")) features.add("COPY");
                if (upper.contains("ACCEPT")) features.add("ACCEPT");
                if (upper.contains("SEARCH")) features.add("SEARCH");
                if (upper.contains("GO TO")) features.add("GO TO");
                if (upper.contains("STOP RUN")) features.add("STOP RUN");
                if (upper.contains("EXEC SQL")) features.add("EXEC SQL");
                if (upper.contains("EXEC CICS")) features.add("EXEC CICS");
                resp.put("features", features);
            }

            sendJson(ex, 200, resp);
        } catch (Exception e) {
            sendJson(ex, 500, Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    // ---------------------------------------------------------------
    // GET /api/health
    // ---------------------------------------------------------------
    private void handleHealth(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        sendJson(ex, 200, Map.of(
                "status", "ok",
                "version", "2.0.0",
                "engine", "CORN COBOL-to-Java"
        ));
    }

    // ---------------------------------------------------------------
    // Static file serving for demo-ui
    // ---------------------------------------------------------------
    private void handleStatic(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        // Prevent path traversal
        Path resolved = staticDir.resolve(path.substring(1)).normalize();
        if (!resolved.startsWith(staticDir)) {
            sendText(ex, 403, "Forbidden");
            return;
        }

        if (!Files.isRegularFile(resolved)) {
            sendText(ex, 404, "Not found: " + path);
            return;
        }

        String contentType = guessContentType(path);
        byte[] data = Files.readAllBytes(resolved);
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private List<Map<String, Object>> formatErrors(List<ParseError> errors) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ParseError err : errors) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("message", err.message());
            m.put("severity", err.severity().name().toLowerCase());
            if (err.location() != null) {
                m.put("line", err.location().getStartLine());
                m.put("column", err.location().getStartColumn());
            }
            list.add(m);
        }
        return list;
    }

    private int pct(int part, int total) {
        return Math.round((float) part / total * 100);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJsonBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            byte[] bytes = is.readAllBytes();
            return JSON.readValue(bytes, Map.class);
        }
    }

    private void sendJson(HttpExchange ex, int status, Object obj) throws IOException {
        byte[] data = JSON.writeValueAsBytes(obj);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    private void sendText(HttpExchange ex, int status, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    private boolean handleCors(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private boolean requirePost(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, Map.of("error", "POST required"));
            return false;
        }
        return true;
    }

    private String guessContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".json")) return "application/json; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        Path staticDir = Path.of("demo-ui");

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port", "-p" -> {
                    if (i + 1 < args.length) port = Integer.parseInt(args[++i]);
                }
                case "--static", "-s" -> {
                    if (i + 1 < args.length) staticDir = Path.of(args[++i]);
                }
            }
        }

        if (!Files.isDirectory(staticDir)) {
            System.err.println("Static directory not found: " + staticDir.toAbsolutePath());
            System.err.println("Run from project root, or use --static <path-to-demo-ui>");
            System.exit(1);
        }

        new CornServer(port, staticDir).start();
    }
}
