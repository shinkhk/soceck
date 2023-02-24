package aaa;

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;

public class LoginMChat extends JFrame implements ActionListener{

	TextField idTf;
	Label logo, idl, pwl;
	Button logBtn;
	Socket sock;
	BufferedReader in;
	PrintWriter out;
	String id;
	String host = "113.198.238.111";
	int port = 8002;
	String title = "MChat0.1";
	
//	113.198.238.109
//	113.198.238.111
	public LoginMChat() {
		setSize(450, 400);
		getContentPane().setBackground(new Color(100, 200, 100));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setTitle(title);
		logo = new Label(title);
		logo.setFont(new Font("Dialog", Font.BOLD, 50));
		idl = new Label("ID");
		idTf = new TextField("aaa");
		logBtn = new Button("로그인");
		logo.setBounds(100, 50, 250, 100);
		idl.setBounds(150, 200, 50, 20);
		idTf.setBounds(200, 200, 100, 20);
		logBtn.setBounds(150, 260, 150, 40);
		logBtn.addActionListener(this);
		add(logo);
		add(idl);
		add(idTf);
		add(logBtn);
		setVisible(true);
		validate();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Object obj = e.getSource();
			if(obj==logBtn) {
				if(sock==null) {
					connect();
				}
				id = idTf.getText().trim();
				out.println(ChatProtocol2.ID+ChatProtocol2.MODE+
						id);
				dispose();
				new MChatClient(in, out, id);
			}//--if1
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	
	public void connect() {
		try {
			sock = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true/* auto flush */);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// --connect
	
	
	
	public static void main(String[] args) {
		new LoginMChat();
	}



}
