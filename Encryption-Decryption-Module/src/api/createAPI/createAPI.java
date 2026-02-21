package api.createAPI;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*; // DB 연결용

public class createAPI {
    private static final String SAVE_DIR = "C:/uploads/"; // 파일 저장 경로

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("API Server 시작 (Port: 5000)...");

            while (true) {
                Socket socket = serverSocket.accept();
                // 각 요청을 독립적인 스레드에서 처리
                new Thread(() -> handleRequest(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            // 1. 파라미터 수신 (파일명, 크기)
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            System.out.println("등록 요청 수신: " + fileName + " (" + fileSize + " bytes)");

            // 2. DB 적재 (JDBC 로직 예시)
            boolean isDbSuccess = saveToDatabase(fileName, fileSize);

            if (isDbSuccess) {
                // 3. 물리 파일 저장
                File saveFile = new File(SAVE_DIR + System.currentTimeMillis() + "_" + fileName);
                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    long totalRead = 0;
                    
                    while (totalRead < fileSize && (read = dis.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        totalRead += read;
                    }
                }
                System.out.println("저장 완료: " + saveFile.getAbsolutePath());
                dos.writeUTF("SUCCESS: " + fileName + " 등록 완료");
            } else {
                dos.writeUTF("ERROR: DB 등록 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean saveToDatabase(String fileName, long fileSize) {
        // 실제 JDBC 코드가 들어갈 자리입니다.
        // INSERT INTO file_table (name, size, reg_date) VALUES (?, ?, NOW())
        System.out.println("[DB] 데이터 적재 중... " + fileName);
        return true; 
    }
}