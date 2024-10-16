package server;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import utils.ServerUtils;

public class Server_Run extends JFrame {

	private static final long serialVersionUID = ServerUtils.getSerialversionuid();
	private static final int SERVER_PORT = ServerUtils.getServerPort();
	private static final String SERVER_ADDRESS = ServerUtils.getServerAddress();
	private static final String SERVER_DIR = ServerUtils.getServerDir();
	private JPanel contentPane;
	private JTextField tfPort;
	private JTextArea taInfo;
	private DatagramSocket serverSocket; // Biến socket toàn cục để dừng server khi cần
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server_Run frame = new Server_Run();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// Hàm để khởi động server
	private void startServer() {
		Thread serverThread = new Thread(() -> {
			try {
				// Khởi tạo socket server UDP
				InetAddress serverIP = InetAddress.getByName(SERVER_ADDRESS);
				serverSocket = new DatagramSocket(SERVER_PORT, serverIP);

		        // Tạo thư mục gốc nếu chưa tồn tại
		        File serverDirectory = new File(SERVER_DIR);
		        if (!serverDirectory.exists()) {
		            serverDirectory.mkdir();
		        }
		        
		        logMessage("Mail Server UDP đang khởi chạy trên port " + SERVER_PORT + "...");
		        
		        while (!serverSocket.isClosed()) {
		        	// Nhận yêu cầu từ client
		            byte[] receiveData = new byte[1024];
		            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		            serverSocket.receive(receivePacket);

		            String clientRequest = new String(receivePacket.getData(), 0, receivePacket.getLength());
		            InetAddress clientIP = receivePacket.getAddress();
		            int clientPort = receivePacket.getPort();

		            logMessage("Received: " + clientRequest);

		            String[] requestParts = clientRequest.split(" ", 3);
		            String command = requestParts[0];
		            
		            switch (command) {
			            case "CREATE_ACCOUNT":
			                createAccount(requestParts[1], requestParts[2], clientIP, clientPort, serverSocket);
			                break;
			            case "SEND_EMAIL":
			            	requestParts = clientRequest.split(" ", 2);
			            	requestParts = requestParts[1].split("\n", 5);
			            	//System.out.println(requestParts[0] + " " + requestParts[1] + " " + requestParts[2] + " " + requestParts[3] + " " + requestParts[4]);
			            	sendMessage(requestParts[0], requestParts[1], requestParts[2], requestParts[3], requestParts[4], clientIP, clientPort, serverSocket);
			                break;
			            case "LOGIN":
			                login(requestParts[1], requestParts[2], clientIP, clientPort, serverSocket);
			                break;
			            case "GET_MAILS":
			            	getMailList(requestParts[1], clientIP, clientPort, serverSocket);
			            	break;
			            case "LOGOUT":
			            	requestParts = clientRequest.split(" ", 2);
			            	logMessage(requestParts[1] + " đã thoát");
			            	break;
			            default:
			                logMessage("Không tìm thấy lệnh: " + command);
		            }
		        }
			} catch (IOException e) {
				logMessage("Lỗi khi khởi chạy server: " + e.getMessage());
			}
		});
		
		serverThread.start(); // Bắt đầu server trong luồng riêng
	}
	
	private static void createAccount(String accountName, String password, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
        File accountDir = new File(SERVER_DIR + "/" + accountName);
        if (!accountDir.exists()) {
            accountDir.mkdir();

            // Tạo file new_email.txt
            File newEmailFile = new File(accountDir, "new_email.txt");
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(newEmailFile));
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//            LocalDateTime now = LocalDateTime.now();
//            String formattedDate = now.format(formatter);
//            writer1.write("Welcome to Mail!\n");
//            writer1.write(formattedDate + "\n");
//            writer1.write("admin@gmail.com\n");
//            writer1.write(accountName + "\n");
            writer1.write("Thank you for using this service. We hope that you will feel comfortable.");
            writer1.close();
            
            // Tạo file password.txt
            File passwordFile = new File(accountDir, "password.txt");
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(passwordFile));
//            writer2.write("This is your password!\n");
//            writer2.write(formattedDate + "\n");
//            writer2.write("admin@gmail.com\n");
//            writer2.write(accountName + "\n");
            writer2.write(password);
            writer2.close();

            String response = "SUCCESS Tài khoản " + accountName + " đã tạo thành công.";
            sendResponse(response, clientIP, clientPort, serverSocket);
        } else {
            sendResponse("ERROR Tài khoản đã tồn tại.", clientIP, clientPort, serverSocket);
        }
    }
	
	private void sendMessage(String emailTitle, String formattedDate, String email, String emailReceived, String content, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
		File accountDir1 = new File(SERVER_DIR + "/" + emailReceived);
//		File accountDir2 = new File(SERVER_DIR + "/" + email);
        if (accountDir1.exists()) {
            // Tạo file email_currentTime.txt
        	Long currentTime = System.currentTimeMillis();
            File newEmailFile = new File(accountDir1, "email_" + currentTime + ".txt");
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(newEmailFile));
//            writer1.write(emailTitle + "\n");
//            writer1.write(formattedDate + "\n");
//            writer1.write(email + "\n");
//            writer1.write(emailReceived + "\n");
            writer1.write(content);
            writer1.close();

//            newEmailFile = new File(accountDir2, "email_" + currentTime + ".txt");
//            BufferedWriter writer2 = new BufferedWriter(new FileWriter(newEmailFile));
//            writer2.write(emailTitle + "\n");
//            writer2.write(formattedDate + "\n");
//            writer2.write(email + "\n");
//            writer2.write(emailReceived + "\n");
//            writer2.write(content);
//            writer2.close();
            
            String response = "SUCCESS Thư đã được gửi thành công từ " + email + " đến " + emailReceived + ".";
            sendResponse(response, clientIP, clientPort, serverSocket);
        } else {
            sendResponse("ERROR Tài khoản không tồn tại.", clientIP, clientPort, serverSocket);
        }
	}
    
    private void login(String accountName, String password, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
        File accountDir = new File(SERVER_DIR + "/" + accountName);
        if (accountDir.exists()) {
        	File passwordFile = new File(accountDir, "password.txt");
        	try {
	        	BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
	        	String pass = "";
	        	String line;
//	        	int count = 0;
	        	while ((line = reader.readLine()) != null) {
	        		pass = line;
//	        		if (count >= 4) pass = line;
//	        		count++;
	        	}
	        	reader.close();
			    
	        	if (pass.equals(password)) {
//	        		 StringBuilder fileNames = new StringBuilder();
//	                 for (File file : accountDir.listFiles()) {
//	                     fileNames.append(file.getName()).append("\n");
//	                 }
	                 logMessage(accountName + " đã kết nối");
	                 sendResponse("SUCCESS ", clientIP, clientPort, serverSocket);
	        	} else {
	                sendResponse("ERROR Sai mật khẩu.", clientIP, clientPort, serverSocket);
	        	}
        	} catch (Exception e) {}
        } else {
            sendResponse("ERROR Tài khoản không tồn tại.", clientIP, clientPort, serverSocket);
        }
    }
    
    private void getMailList(String accountName, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
    	File accountDir = new File(SERVER_DIR + "/" + accountName);
    	if (accountDir.exists()) {
    		StringBuilder fileNames = new StringBuilder();
            for (File file : accountDir.listFiles()) {
                fileNames.append(file.getName()).append("\n");
            }
            sendResponse("SUCCESS " + fileNames.toString(), clientIP, clientPort, serverSocket);
    	} else {
            sendResponse("ERROR Tài khoản không tồn tại.", clientIP, clientPort, serverSocket);
        }
    }
    
    private static void sendResponse(String response, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
        byte[] sendData = response.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
        serverSocket.send(sendPacket);
    }

	/**
	 * Create the frame.
	 */
	public Server_Run() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Máy chủ (Server)");
		setBounds(100, 100, 700, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(80, 426, 25, 13);
		contentPane.add(lblPort);
		
		tfPort = new JTextField();
		tfPort.setText("1234");
		tfPort.setBounds(115, 423, 96, 19);
		contentPane.add(tfPort);
		tfPort.setColumns(10);
		
		JButton btnStart = new JButton("Khởi động máy chủ");
		btnStart.setBounds(234, 422, 143, 21);
		contentPane.add(btnStart);
		
		JButton btnStop = new JButton("Dừng máy chủ");
		btnStop.setEnabled(false);
		btnStop.setBounds(387, 422, 143, 21);
		contentPane.add(btnStop);
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!tfPort.getText().equals("")) {
					btnStart.setEnabled(false);
					btnStop.setEnabled(true);
					startServer();  // Khởi chạy server trong luồng riêng
				} else {
					JOptionPane.showMessageDialog(null, "Port không thể bỏ trống", "Thông báo", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();  // Dừng server
					logMessage("Server đã dừng.");
					btnStart.setEnabled(true);
					btnStop.setEnabled(false);
				}
			}
		});
		
		taInfo = new JTextArea();
		taInfo.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(taInfo, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(48, 81, 587, 304);
		contentPane.add(scrollPane);
		
		JLabel lblTitle = new JLabel("MAIL APPLICATION");
		lblTitle.setForeground(Color.RED);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(251, 24, 163, 13);
		contentPane.add(lblTitle);
		
		JLabel lblRole = new JLabel("(Server)");
		lblRole.setHorizontalAlignment(SwingConstants.CENTER);
		lblRole.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRole.setBounds(251, 47, 163, 15);
		contentPane.add(lblRole);
	}
	
	// Hàm để log thông tin vào JTextArea
	private void logMessage(String message) {
		java.util.Date date = new java.util.Date();
		taInfo.append(sdf.format(date) + ": "+ message + "\n");
	}
}
