package aaa;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;




public class MChatClient extends JFrame
implements ActionListener, Runnable{
	JButton bt1, bt2, bt3, bt4;
	JTextField tf1, tf2, tf3;
	TextArea area;
	List list;
	Socket sock;
	BufferedReader in;
	PrintWriter out;
	String listTitle = "*******질문 명단*******";
	String id;
	JLabel picture;
	MChatQuestionRoom[] QR = new MChatQuestionRoom[100];
	boolean flag = false;
	
	ImageIcon img=new ImageIcon("./Button_Image/addpay.jpg");
	ImageIcon imgexit=new ImageIcon("./Button_Image/exit.jpg");

	public MChatClient(BufferedReader in, PrintWriter out, String id) {
		setSize(850,700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.id = id;
		this.in = in;
		this.out = out;
		setTitle(this.id + "님 안녕하세요");
		// //////////////////////////////////////////////////////////////////////////////////////////
		JPanel p1 = new JPanel();
		UserMainUIPanel(p1);
		getContentPane().add(p1);
		// /////////////////////////////////////////////////////////////////////////////////////////
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		list = new List();
		list.add(listTitle);
		p2.add(BorderLayout.CENTER, list);
		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(1, 2));
		bt2 = new JButton("질문하기");
		bt2.addActionListener(this);
		bt3 = new JButton("답변하기");
		bt3.addActionListener(this);
		p3.add(bt2);
		p3.add(bt3);
		p2.add(BorderLayout.SOUTH, p3);
		add(BorderLayout.EAST, p2);
		// ///////////////////////////////////////////////////////////////////////////////////////////
		
		new Thread(this).start();
		setVisible(true);
		validate();
	}
	
	public void run() {
		System.out.println("클라이언트 run");
		try {
			while(true) {
				String line = in.readLine();
				if(line==null) {
					System.out.println("종료");
					break;}
				else
					routine(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}//--run
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
			if(obj == bt2) {// 질문하기
			MyDialog md = new MyDialog(this, "질문을 입력하세요", true);
			//Dialog의 창크기
			int width = 300;
			int height = 200;
			//int x = fx+getWidth()/2-width/2;
			//int y = fy+getHeight()/2-height/2;
			md.setSize(width, height);
			md.setLocationRelativeTo(this);
			//md.setBounds(x, y, width, height);
			md.setVisible(true);
		}else if(obj == bt3) { // 답변하기
			if(list.getSelectedItem()!=null) {
			String str = list.getSelectedItem();
			enterRoom(str);
			}
		}else if(obj == tf3) {
			sendMessage(tf3.getText());
			tf3.setText("");
		}
	}
	
	public void routine(String line) {
		System.out.println("클라이언트 line");
		int idx = line.indexOf(ChatProtocol2.MODE);
		String cmd = line.substring(0, idx);
		String data = line.substring(idx+1);
		if(cmd.equals(ChatProtocol2.ROOMLIST)) {
			addRoomList(data);}
		else if(cmd.equals(ChatProtocol2.ENTERROOM)) {	// ENTERROOM:방이름:유저명;님이 입장하였습니다
			int idx1 = data.indexOf(ChatProtocol2.MODE);
			String Rn = data.substring(0, idx1); //방이름
			String str = data.substring(idx1+1); //유저명;님이 입장하였습니다
			int idx2 = str.indexOf(";");
			String Un = str.substring(0, idx2);	//유저명
			String str1 = str.substring(idx2+1); //님이 입장하였습니다
			for(int i = 0; QR.length > i; i++) {
				if(QR[i] != null) {
					if(Rn.equals(QR[i].roomName)){
					QR[i].addText("["+Un+"] " + str1);
					}
				}
				
			}
		}else if(cmd.equals(ChatProtocol2.ADDUSER)) {
			// 방이름:방이름:유저명;방이름:유저명;방이름:유저명;...;
			int idx1 = data.indexOf(ChatProtocol2.MODE);
			String Rn = data.substring(0, idx1); //방이름
			String str = data.substring(idx1+1); //방이름:유저명;방이름:유저명;방이름:유저명;...;
			for(int i = 0; QR.length > i; i++) {
				if(QR[i] != null) {
					if(Rn.equals(QR[i].roomName)){
						QR[i].resetList(str);
						break;
					}
				}

			}
//			String userList[] = new String[100];
//			StringTokenizer st = new StringTokenizer(data, ";");
//			while(st.hasMoreTokens()) {
//				int i = 0;
//				userList[i] = st.nextToken(); // 방이름:방이름:유저명 / 방이름:유저명 / 방이름:유저명/ ... /
//				i++;
//			}
//			for(int i = 0 ;userList.length > i;i++) {
//				int idx2 = userList[i].indexOf(ChatProtocol2.MODE);
//				String Rn1 = userList[i].substring(0,idx2);
//				String Un = userList[i].substring(idx2+1);
//				if(QR[i].roomname.equals(Rn)) {
//					QR[i].resetList(Un);
//				}
//			}

		}else if(cmd.equals(ChatProtocol2.MESSAGE)) { // MESSAGE:방이름:[id]+채팅내용
			System.out.println("메세지진입");
			int idx1 = data.indexOf(ChatProtocol2.MODE);
			String Rn = data.substring(0, idx1); // 방이름
			System.out.println("Rn:"+Rn);
			String msg = data.substring(idx1 + 1);	// [id]:채팅내용
			System.out.println("msg:"+msg);
//			if(Rn.equals(QR[0].roomname)) {
//				QR[0].addText(msg);
//			}
			for(int i = 0; QR.length > i; i++) {
				if(QR[i] != null) {
					if(Rn.equals(QR[i].roomName)){
					System.out.println("채팅한 방번호 = " + i);
					QR[i].addText(msg);
					}
				}

			}
		}else if(cmd.equals(ChatProtocol2.RESETLIST)) {
			System.out.println("리스트리셋");
			System.out.println(data);
			list.removeAll();
			list.add(listTitle);
			StringTokenizer st = new StringTokenizer(data, ";");
			while(st.hasMoreTokens()) {
				list.add(st.nextToken());
			}
		}else if(cmd.equals(ChatProtocol2.DELETELIST)) {
			list.remove(data);
			for(int i = 0; QR.length > i; i++) {
				if(QR[i] != null) {
					if(data.equals(QR[i].roomName)){
					System.out.println("채팅한 방번호 = " + i);
					QR[i].addText("*********OWNER EXIT*********");
					QR[i].addText("Leave the room in 3 seconds");
					sendMessage(ChatProtocol2.DELETUSER+ChatProtocol2.MODE+QR[i].roomName);
					try {
						QR[i].addText("3");
						Thread.sleep(1000);
						QR[i].addText("2");
						Thread.sleep(1000);
						QR[i].addText("1");
						Thread.sleep(1000);
						QR[i].dispose();
						QR[i] = null;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
					}
				}

			}
		}else if(cmd.equals(ChatProtocol2.EXIT)) {	//EXIT:방이름
			for(int i = 0; QR.length > i; i++) {
				if(QR[i] != null) {
					if(data.equals(QR[i].roomName)) {
						QR[i] = null;
						break;
					}
				}
			}
		}
	}//--routine
	
	class MyDialog extends Dialog implements ActionListener{
		Button b1, b2;
		TextField tf;
		TextArea ta;
		public MyDialog(Frame owner, String title, boolean modal) {
			super(owner, title, modal);
			setLayout(new BorderLayout());
			tf = new TextField();
			Panel p = new Panel();
			
			b1 = new Button("확인");
			b2 = new Button("취소");
			
			p.add(b1);
			p.add(b2);
			
			add(p,BorderLayout.SOUTH);
			add(tf,BorderLayout.CENTER);
			b1.addActionListener(this);
			b2.addActionListener(this);
			tf.addActionListener(this);//Enter 이벤트
		}
		
		public MyDialog(Frame owner, String title, boolean modal, String msg) {
			super(owner, title, modal);
			setLayout(new BorderLayout());
			ta = new TextArea(msg);
			Panel p = new Panel();
			
			b2 = new Button("확인");
			
			p.add(b2);
			
			add(p,BorderLayout.SOUTH);
			add(ta,BorderLayout.CENTER);
			b2.addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			if(obj == b1 || obj == tf) {
				String str = tf.getText().trim();
//				sendMessage(ChatProtocol2.ROOMLIST+ChatProtocol2.MODE+id+";"+str);//ROOMRIST:ccc;이거 뭐야?
				
				creatRoom(str);
				dispose();//사라지는 기능
			}else if(obj == b2) {
				dispose();
			}
		}
	}//--MyDialog
	
	public void creatRoom(String roomname) {
//		QR[0] = new MChatQuestionRoom(roomname, in, out, id);
//		System.out.println(QR[0].roomname);
		int orner = 1;
		String msg = "이미 있는 질문 입니다.";
		for(int i = 0; QR.length > i; i++) {
			if(QR[i] != null) {
				if(roomname.equals(QR[i].roomName)) {
					MyDialog md = new MyDialog(this, "질문을 입력하세요", true, msg);
					//Dialog의 창크기
					int width = 200;
					int height = 200;
					//int x = fx+getWidth()/2-width/2;
					//int y = fy+getHeight()/2-height/2;
					md.setSize(width, height);
					md.setLocationRelativeTo(this);
					//md.setBounds(x, y, width, height);
					md.setVisible(true);
					return;
				}
			}
		}
		for(int i = 0; QR.length > i; i++) {
			if(QR[i] == null) {
				System.out.println("만들어진 방번호 = " + i);
				sendMessage(ChatProtocol2.ROOMLIST+ChatProtocol2.MODE+id+";"+roomname);
				QR[i] = new MChatQuestionRoom(roomname, in, out, id, orner);
				QR[i].enterRoom();
				break;
			}
		}
	}
	
	public void enterRoom(String roomname) {
		
		String msg = "이미 열려있는 방입니다.";
		for(int i = 0; QR.length > i; i++) {
			if(QR[i] != null) {
				if(roomname.equals(QR[i].roomName)) {
					MyDialog md = new MyDialog(this, "질문을 입력하세요", true, msg);
					//Dialog의 창크기
					int width = 300;
					int height = 200;
					//int x = fx+getWidth()/2-width/2;
					//int y = fy+getHeight()/2-height/2;
					md.setSize(width, height);
					md.setLocationRelativeTo(this);
					//md.setBounds(x, y, width, height);
					md.setVisible(true);
					return;
				}
			}
		}
		
		for(int i = 0; QR.length > i; i++) {
			if(QR[i] == null) {
				System.out.println("만들어진 방번호 = " + i);
				QR[i] = new MChatQuestionRoom(roomname, in, out, id);
				QR[i].enterRoom();
				break;
			}
		}
	}
	
	public void UserMainUIPanel(JPanel panel)
	{
		panel.setLayout(null);
		
		Font font=new Font("맑은 고딕", Font.PLAIN, 17);
		
		JLabel roomNumber=new JLabel("방번호:");
		roomNumber.setBounds(0,0,100,50);
		roomNumber.setFont(font);
		panel.add(roomNumber);
		
		
		JLabel remaintime=new JLabel("남은 시간:");
		remaintime.setBounds(400,0,100,50);
		remaintime.setFont(font);
		panel.add(remaintime);
		
		//관리자 전화번호 뜨게하기
		
		
		JLabel ManagerPhone = new JLabel("관리자 연락처:");
		ManagerPhone.setBounds(0, 40, 300, 50);
		ManagerPhone.setFont(font);
		panel.add(ManagerPhone);
		
		
		//관리자이메일 뜨게하기
		
		
		JLabel managerEmail=new JLabel("관리자이메일:");
		managerEmail.setBounds(400,40,300,50);
		managerEmail.setFont(font);
		panel.add(managerEmail);
		
		picture = new JLabel();
        picture.setIcon(new ImageIcon("./Button_Image/book.jpg"));
        picture.setBounds(0, 100, 900,365);
        panel.add(picture);
        
        JButton addpay=new JButton(img);
        addpay.setBounds(0,465,350,200);
        panel.add(addpay);
        
        JButton exit=new JButton(imgexit);
        exit.setBounds(350,465,350,200);
        panel.add(exit);
        
        
        
        panel.setBackground(new Color(230,239,255));
        
        //추가결제 기능
        addpay.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				
			}
		});
        
        //퇴실 기능
        exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i = 0;QR.length > i; i++) {
					if(QR[i] != null) {
						if(QR[i].owner == 1) {
							sendMessage(ChatProtocol2.DELETELIST+ChatProtocol2.MODE+QR[i].roomName);
							sendMessage(ChatProtocol2.EXIT+ChatProtocol2.MODE+QR[i].roomName+ChatProtocol2.MODE+id);
						}else {
							sendMessage(ChatProtocol2.EXIT+ChatProtocol2.MODE+QR[i].roomName+ChatProtocol2.MODE+id);
						}
					}
				}
				System.exit(0);
			}
		});
        
		
	}
	
	
	public void addRoomList(String str) {
		list.add(str);
	}
	
	
	public void sendMessage(String msg) {
		out.println(msg);
	}
	
	public static void main(String[] args) {
	}




}