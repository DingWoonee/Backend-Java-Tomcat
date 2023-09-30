package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String requestLine = br.readLine();
            String requestMethod = requestLine.split(" ")[0];
            String requestPath = requestLine.split(" ")[1];
            System.out.println(requestPath);
            if (requestPath.equals("/user/form.html")) {
                byte[] body = Files.readAllBytes(Paths.get("webapp\\user\\form.html"));
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
            if (requestPath.contains("/user/signup?")) {
                String requestQuery = getQuery(requestPath);
                Map<String, String> requestMap = HttpRequestUtils.parseQueryParameter(requestQuery);

                MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                User user = new User(requestMap.get("userId"), requestMap.get("password"), requestMap.get("name"), requestMap.get("email"));
                memoryUserRepository.addUser(user);

                System.out.println(memoryUserRepository.findAll().size());
            }
            byte[] body = Files.readAllBytes(Paths.get("webapp\\index.html"));
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private String getQuery(String requestPath) {
        int index = requestPath.indexOf('?');
        if (index == -1) {
            return "";
        }
        return requestPath.substring(index + 1);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}