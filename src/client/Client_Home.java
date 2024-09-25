package client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.text.SimpleDateFormat;

import javax.swing.DefaultListModel;
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
import javax.swing.JList;
import java.awt.Component;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class Client_Home extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final int SERVER_PORT = 1234;
	private static final String SERVER_ADDRESS = "192.168.1.2";
    private static final String SERVER_DIR = "MailServer";
	private JPanel contentPane;
	private JTextArea taInfo;
	private JLabel lblEmailTitle;
	private JLabel lblTime;
	private JLabel lblFrom;
	private JLabel lblTo;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client_Home frame = new Client_Home("thaihv.22it@vku.udn.vn", "new_email.txt\npassword.txt");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @param requestParts 
	 * @param email 
	 */
	public Client_Home(String email, String fileNames) {
		String[] requestParts = fileNames.split("\n");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Bạn đang đăng nhập với tài khoản email: " + email);
		setBounds(100, 100, 850, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnStart = new JButton("Thư mới");
		btnStart.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnStart.setBounds(10, 10, 170, 40);
		contentPane.add(btnStart);
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client_Compose main = new Client_Compose(email);
	            main.setLocationRelativeTo(null);
				main.setVisible(true);
			}
		});
		
		DefaultListModel<String> listModel = new DefaultListModel<>();
		for (String part : requestParts) {
		    listModel.addElement(part);
		}
		JList<String> list = new JList<>(listModel);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
		            JList<?> source = (JList<?>) e.getSource();
		            String selected = source.getSelectedValue().toString();
		            
		            File accountDir = new File(SERVER_DIR + "/" + email);
		            File selectedFile = new File(accountDir, selected);
	            	try {
	    	        	BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
	    	        	StringBuilder content = new StringBuilder();
	    	        	String line;
	    	        	int count = 0;
	    	        	while ((line = reader.readLine()) != null) {
	    	        		if (count == 0) lblEmailTitle.setText(line);
	    	        		else if (count == 1) lblTime.setText(line);
	    	        		else if (count == 2) lblFrom.setText(line);
	    	        		else if (count == 3) lblTo.setText(line);
	    	        		else content.append(line).append("\n");
	    	        		count++;
	    	        	}
	    	        	reader.close();
	    	        	
	    	        	logMessage(content.toString());
	            	} catch (IOException e1) {}
		        }
			}
		});
		
		
		JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 60, 170, 393);
		contentPane.add(scrollPane);
		
		JLabel lblTitle = new JLabel("MAIL APPLICATION");
		lblTitle.setForeground(Color.RED);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(445, 10, 163, 13);
		contentPane.add(lblTitle);
		
		JLabel lblHeading = new JLabel("Home");
		lblHeading.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeading.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblHeading.setBounds(445, 30, 163, 15);
		contentPane.add(lblHeading);
		
		taInfo = new JTextArea();
		taInfo.setEditable(false);
		
		JScrollPane scrollPane2 = new JScrollPane(taInfo, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane2.setBounds(190, 162, 636, 291);
		contentPane.add(scrollPane2);
		
		lblEmailTitle = new JLabel("Title:");
		lblEmailTitle.setHorizontalAlignment(SwingConstants.LEFT);
		lblEmailTitle.setForeground(Color.BLACK);
		lblEmailTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblEmailTitle.setBounds(190, 62, 636, 13);
		contentPane.add(lblEmailTitle);
		
		lblTime = new JLabel("Time:");
		lblTime.setHorizontalAlignment(SwingConstants.LEFT);
		lblTime.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTime.setBounds(190, 85, 636, 15);
		contentPane.add(lblTime);
		
		lblFrom = new JLabel("From:");
		lblFrom.setHorizontalAlignment(SwingConstants.LEFT);
		lblFrom.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblFrom.setBounds(190, 110, 636, 15);
		contentPane.add(lblFrom);
		
		lblTo = new JLabel("To:");
		lblTo.setHorizontalAlignment(SwingConstants.LEFT);
		lblTo.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTo.setBounds(190, 137, 636, 15);
		contentPane.add(lblTo);
	}
	
	// Hàm để log thông tin vào JTextArea
	private void logMessage(String message) {
		taInfo.setText(message + "\n");
		//taInfo.append(message + "\n");
	}
}
