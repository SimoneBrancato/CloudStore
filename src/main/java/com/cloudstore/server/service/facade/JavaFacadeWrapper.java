package com.cloudstore.server.service.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.security.SecurityContext;
import com.cloudstore.server.model.dto.auth.AuthenticationResult;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


public class JavaFacadeWrapper {
    
    // Mapper for JSON serialization/deserialization
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    // Facade instance to handle business logic
    private static final CloudStoreFacade FACADE;

    // Port number for the HTTP server
    private static final int PORT = 9999;
    
    // Static initializer to set up the facade and JSON mapper
    static {
        MAPPER.registerModule(new JavaTimeModule());
        try {
            System.err.println("Initializing CloudStoreFacade...");
            FACADE = new CloudStoreFacade();
            System.err.println("CloudStoreFacade initialized successfully");
        } catch (ServiceException e) {
            System.err.println("Failed to initialize facade: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize facade", e);
        }
    }
    
    /**
     * Main method to start the HTTP server and listen for incoming requests.
     * The server will handle requests to invoke methods on the CloudStoreFacade.
    **/
    public static void main(String[] args) throws Exception {
        System.err.println("HTTP Server starting on port " + PORT);
        
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new FacadeHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        System.err.println("HTTP Server started on http://localhost:" + PORT);
        System.err.println("Waiting for requests...");
    }
    
    static class FacadeHandler implements HttpHandler {
        
        /** 
         * Handles incoming HTTP requests.
         * @param exchange The HttpExchange object containing request and response information.
         * @throws IOException If an I/O error occurs while handling the request.
        **/
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "{\"status\": \"ok\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            
            try {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                String requestBody = sb.toString();
                System.err.println("Received request: " + requestBody);
                
                Map<String, Object> request = MAPPER.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
                String methodName = (String) request.get("method");
                List<?> argsList = (List<?>) request.getOrDefault("args", new ArrayList<>());
                Object[] argsArray = argsList.toArray();
                
                String token = null;
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    try {
                        AuthenticationResult session = FACADE.getSessionFromToken(token);
                        SecurityContext.set(session);
                    } catch (Exception e) {
                        System.err.println("Invalid token: " + e.getMessage());
                        sendErrorResponse(exchange, "Unauthorized: " + e.getMessage(), 401);
                        return;
                    }
                }
                
                System.err.println("Invoking: " + methodName + " with args: " + Arrays.toString(argsArray));
                Object result = invokeMethod(methodName, argsArray, token);
                System.err.println("Result: " + result);
                
                Map<String, Object> response = new HashMap<>();
                response.put("ok", true);
                response.put("data", result);
                
                String responseJson = MAPPER.writeValueAsString(response);
                System.err.println("Sending response: " + responseJson);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseJson.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseJson.getBytes());
                os.close();
                
            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
                e.printStackTrace();
                
                sendErrorResponse(exchange, e.getMessage(), 500);
            } finally {
                SecurityContext.clear();
            }
        }
        
        /** 
         * Sends an error response with the specified message and HTTP status code.
         * @param exchange The HttpExchange object to send the response through.
         * @param message The error message to include in the response body.
         * @param statusCode The HTTP status code to set for the response.
         * @throws IOException If an I/O error occurs while sending the response.
        **/
        private void sendErrorResponse(HttpExchange exchange, String message, int statusCode) throws IOException {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("error", message);
            String errorJson = MAPPER.writeValueAsString(errorResponse);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, errorJson.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(errorJson.getBytes());
            os.close();
        }
        
        /** 
         * Invokes the specified method on the CloudStoreFacade with the given arguments.
         * @param methodName The name of the method to invoke.
         * @param args The arguments to pass to the method.
         * @param token The authentication token (may be null).
         * @return The result of the method invocation.
         * @throws Exception If an error occurs while invoking the method.
        **/
        private static Object invokeMethod(String methodName, Object[] args, String token) throws Exception {
            Method[] methods = CloudStoreFacade.class.getMethods();
            Method targetMethod = null;
            
            // First, try to find a method that matches args.length
            for (Method m : methods) {
                if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                    targetMethod = m;
                    break;
                }
            }
            
            // If not found and we have a token, try to find a method that expects token as first param
            if (targetMethod == null && token != null) {
                for (Method m : methods) {
                    if (m.getName().equals(methodName) && m.getParameterCount() == args.length + 1) {
                        Class<?>[] paramTypes = m.getParameterTypes();
                        if (paramTypes.length > 0 && paramTypes[0].equals(String.class)) {
                            targetMethod = m;
                            break;
                        }
                    }
                }
                
                // If found, prepend token to args
                if (targetMethod != null) {
                    Object[] newArgs = new Object[args.length + 1];
                    newArgs[0] = token;
                    System.arraycopy(args, 0, newArgs, 1, args.length);
                    args = newArgs;
                }
            }
            
            if (targetMethod == null) {
                throw new IllegalArgumentException("Method not found: " + methodName + " with " + args.length + " parameters");
            }
            
            Class<?>[] paramTypes = targetMethod.getParameterTypes();
            Object[] convertedArgs = new Object[paramTypes.length];
            
            for (int i = 0; i < paramTypes.length; i++) {
                if (i < args.length && args[i] != null) {
                    convertedArgs[i] = MAPPER.convertValue(args[i], paramTypes[i]);
                }
            }
            
            return targetMethod.invoke(FACADE, convertedArgs);
        }
    }
}