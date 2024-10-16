package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import utils.ServerUtils;

public class Server {
	private static final int SERVER_PORT = ServerUtils.getServerPort();
	private static final String SERVER_DIR = ServerUtils.getServerDir();
    
    public static void main(String[] args) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);

        // Tạo thư mục gốc nếu chưa tồn tại
        File serverDirectory = new File(SERVER_DIR);
        if (!serverDirectory.exists()) {
            serverDirectory.mkdir();
        }
        
        System.out.println("Mail Server UDP đang khởi chạy...");
        
        while (true) {
        	// Nhận yêu cầu từ client
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String clientRequest = new String(receivePacket.getData(), 0, receivePacket.getLength());
            InetAddress clientIP = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            System.out.println("Received: " + clientRequest);

            String[] requestParts = clientRequest.split(" ", 3);
            String command = requestParts[0];
            
            switch (command) {
	            case "CREATE_ACCOUNT":
	                createAccount(requestParts[1], requestParts[2], clientIP, clientPort, serverSocket);
	                break;
	            case "SEND_EMAIL":
	                //String[] emailParts = requestParts[1].split(" ", 2);
	                //sendEmail(emailParts[0], emailParts[1], clientIP, clientPort, serverSocket);
	                break;
	            case "LOGIN":
	                login(requestParts[1], requestParts[2], clientIP, clientPort, serverSocket);
	                break;
	            default:
	                System.out.println("Không tìm thấy lệnh: " + command);
            }
        }
	}
    
    private static void createAccount(String accountName, String password, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
        File accountDir = new File(SERVER_DIR + "/" + accountName);
        if (!accountDir.exists()) {
            accountDir.mkdir();

            // Tạo file new_email.txt
            File newEmailFile = new File(accountDir, "new_email.txt");
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(newEmailFile));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String formattedDate = now.format(formatter);
            writer1.write("Welcome to Mail!\n");
            writer1.write(formattedDate + "\n");
            writer1.write("admin@gmail.com\n");
            writer1.write(accountName + "\n");
            writer1.write("Thank you for using this service. We hope that you will feel comfortable.");
            writer1.close();
            
            // Tạo file password.txt
            File passwordFile = new File(accountDir, "password.txt");
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(passwordFile));
            writer2.write("This is your password!\n");
            writer2.write(formattedDate + "\n");
            writer2.write("admin@gmail.com\n");
            writer2.write(accountName + "\n");
            writer2.write(password);
            writer2.close();

            String response = "SUCCESS Tài khoản " + accountName + " đã tạo thành công.";
            sendResponse(response, clientIP, clientPort, serverSocket);
        } else {
            sendResponse("ERROR Tài khoản đã tồn tại.", clientIP, clientPort, serverSocket);
        }
    }
    
    private static void login(String accountName, String password, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
        File accountDir = new File(SERVER_DIR + "/" + accountName);
        if (accountDir.exists()) {
        	File passwordFile = new File(accountDir, "password.txt");
        	try {
	        	BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
	        	String pass = "";
	        	String line;
	        	while ((line = reader.readLine()) != null) {
	        		pass = line;
	        	}
	        	reader.close();

	        	if (pass.toString().equals(password)) {
	        		 StringBuilder fileNames = new StringBuilder();
	                 for (File file : accountDir.listFiles()) {
	                     fileNames.append(file.getName()).append("\n");
	                 }
	                 sendResponse("SUCCESS " + fileNames.toString(), clientIP, clientPort, serverSocket);
	        	} else {
	                sendResponse("ERROR Sai mật khẩu.", clientIP, clientPort, serverSocket);
	        	}
        	} catch (Exception e) {}
        } else {
            sendResponse("ERROR Tài khoản không tồn tại.", clientIP, clientPort, serverSocket);
        }
    }
    
    private static void sendResponse(String response, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
        byte[] sendData = response.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
        serverSocket.send(sendPacket);
    }
}
