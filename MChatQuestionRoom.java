package aaa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MChatQuestionRoom extends JFrame
implements ActionListener{
	 
	String roomName;
	List userList;
	JButton bt1,bt2,bt3;
	JTextField tf;
	TextArea ta;
	String id;
	BufferedReader in;
	PrintWriter out;
	Socket sock;
	int owner;
	
	
	public MChatQuestionRoom(String roomname,BufferedReader in, PrintWriter out, String id) {	// 답변할 사람의 채팅방
		System.out.println("방생성");
		setSize(450, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.roomName = roomname;
		this.in = in;
		this.out = out;
		this.id = id;
		setTitle(this.roomName);
		// //////////////////////////////////////////////////////////////////////////////////////////
		ta = new TextArea();
		ta.setBackground(new Color(230,239,255));
		ta.setForeground(Color.BLACK);
		ta.setEditable(false);
		add(BorderLayout.CENTER, ta);
		// /////////////////////////////////////////////////////////////////////////////////////////
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		userList = new List();
		p2.add(BorderLayout.CENTER, userList);
		JPanel p3 = new JPanel();
		bt2 = new JButton("나가기");
		bt2.addActionListener(this);
		p3.add(bt2);
		p2.add(BorderLayout.SOUTH, p3);
		add(BorderLayout.EAST, p2);
		// ///////////////////////////////////////////////////////////////////////////////////////////
		JPanel p4 = new JPanel();
		tf = new JTextField("", 30);
		bt1 = new JButton("보내기");
		p4.add(tf);
		p4.add(bt1);
		add(BorderLayout.SOUTH, p4);
		bt1.addActionListener(this);
		tf.addActionListener(this);
		setVisible(true);
		validate();
	}
	
	public MChatQuestionRoom(String roomname,BufferedReader in, PrintWriter out, String id, int owner) { // 질문 한사람의 채팅방
		System.out.println("방생성");
		setSize(450, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.roomName = roomname;
		this.in = in;
		this.out = out;
		this.id = id;
		this.owner = owner;
		setTitle(this.roomName);
		// //////////////////////////////////////////////////////////////////////////////////////////
		ta = new TextArea();
		ta.setBackground(new Color(230,239,255));
		ta.setForeground(Color.BLACK);
		ta.setEditable(false);
		add(BorderLayout.CENTER, ta);
		// /////////////////////////////////////////////////////////////////////////////////////////
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		userList = new List();
		p2.add(BorderLayout.CENTER, userList);
		JPanel p3 = new JPanel();
		bt3 = new JButton("질문 종료");
		bt3.addActionListener(this);
		p3.add(bt3);
		p2.add(BorderLayout.SOUTH, p3);
		add(BorderLayout.EAST, p2);
		// ///////////////////////////////////////////////////////////////////////////////////////////
		JPanel p4 = new JPanel();
		tf = new JTextField("", 30);
		bt1 = new JButton("보내기");
		p4.add(tf);
		p4.add(bt1);
		add(BorderLayout.SOUTH, p4);
		bt1.addActionListener(this);
		tf.addActionListener(this);
		setVisible(true);
		validate();
	}
	
	
//	public MChatQuestionRoom(String roomname, String msg) {
//		if(roomname == this.roomname) {
//			ta.append(msg);
//		}
//	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj == bt2) {	//나가기
			sendMessage(ChatProtocol2.EXIT+ChatProtocol2.MODE+roomName+ChatProtocol2.MODE+id);
			dispose();
		}else if(obj == bt1 || obj == tf) {
			String msg = tf.getText().trim();
			if(msg.length() != 0) {
				sendMessage(ChatProtocol2.CHAT+ChatProtocol2.MODE+roomName+ChatProtocol2.MODE+id+";"+msg); // -> CHAT:방이름:aaa;안녕하세요
				tf.setText("");
				tf.requestFocus();
			}
		}else if(obj == bt3) {
			sendMessage(ChatProtocol2.DELETELIST+ChatProtocol2.MODE+roomName);
			sendMessage(ChatProtocol2.EXIT+ChatProtocol2.MODE+roomName+ChatProtocol2.MODE+id);
			dispose();
		}
	}
	
	
	public void routine(String line) {
		System.out.println("룸 line");
		int idx = line.indexOf(ChatProtocol2.MODE);
		String cmd = line.substring(0, idx);
		String data = line.substring(idx+1);
		if(cmd == roomName) {
			ta.append(data+"\n");
		}
	}//--routine
	
	public void enterRoom() {
		String msg =  id + ";Enter room";
		sendMessage(ChatProtocol2.ENTERROOM+ChatProtocol2.MODE+roomName+ChatProtocol2.MODE+msg); // ENTERROOM:방이름:유저명;님이 입장하였습니다
	}
	
	public void resetList(String str) {
		String addList = "";
		userList.removeAll();
		StringTokenizer st = new StringTokenizer(str, ";");	//방이름:유저명;방이름:유저명;방이름:유저명;...;
		while(st.hasMoreTokens()) {
			addList = st.nextToken();
			System.out.println("addlist"+addList);
			int idx = addList.indexOf(ChatProtocol2.MODE);
			String rn = addList.substring(0, idx);
			String un = addList.substring(idx+1);
			System.out.println(rn+" "+un);
			if(rn.equals(roomName)) {
				userList.add(un);
			}
		
		}
	}
	
	public void addText(String msg) {
			ta.append(msg+"\n");
	}
	
	
	public void sendMessage(String msg) {
		out.println(msg);
	}
	
	
	public static void main(String[] args) {

	}



}// 