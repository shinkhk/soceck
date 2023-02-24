package aaa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MChatServer {
	
	public static final int PORT = 8002;
	ServerSocket server;
	Vector<ClientThread2> vc;
	String roomlist[] = new String[100];
	String roomUser[] = new String[100];
	
	public MChatServer() {
		try {
			server = new ServerSocket(PORT);
			vc = new Vector<ClientThread2>();
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println("Error in Server");
			System.exit(1);//비정상적인 종료
		}
		System.out.println("****Chat Server 2.0****");
		System.out.println("*클라이언트 접속을 기다리고 있습니다");
		System.out.println("**********************");
		try {
			while(true) {
				Socket sock = server.accept();
				ClientThread2 ct = new ClientThread2(sock);
				ct.start();
				vc.addElement(ct);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println("Error in Socket");
		}
	}
	
	public void sendAllMessage(String msg) {
		for (int i = 0; i < vc.size(); i++) {
			ClientThread2 ct = vc.get(i);
			ct.sendMessage(msg);
		}
	}
	
	public void removeClient(ClientThread2 ct) {
		vc.remove(ct);
	}
	
	//접속된 모든 id 리스트 리턴 ex)aaa;bbb;홍길동;강호동;
	public String getIdList() {
		String list = "";
		for (int i = 0; i < vc.size(); i++) {
			ClientThread2 ct = vc.get(i);
			list+=ct.id+";";
		}
		return list;
	}
	
	//매개변수 id값으로 ClientThread2를 검색
	public ClientThread2 findClient(String id) {
		ClientThread2 ct = null;
		for (int i = 0; i < vc.size(); i++) {
			ct = vc.get(i);
			if(ct.id.equals(id))//매개변수의 id와 ct의 id가 동일 하다면.
				break;
		}
		return ct;
	}
	
	
	
	
	class ClientThread2 extends Thread{
		
		Socket sock;
		BufferedReader in;
		PrintWriter out;
		String id;
		
		public ClientThread2(Socket sock) {
			try {
				this.sock = sock;
				in = new BufferedReader(
						new InputStreamReader(sock.getInputStream()));
				out = new PrintWriter(sock.getOutputStream(),true);
				System.out.println(sock.toString() +" 접속됨...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					String line = in.readLine();
					if(line==null)
						break;
					else
						routine(line);
				}
			} catch (Exception e) {
				removeClient(this);
				System.err.println(sock+"["+ id +"] 끊어짐...");
			}
		}
		
		public void routine(String line) {
			//EX)CHATALL:오늘은 목요일입니다.
			System.out.println("line:" + line);
			int idx = line.indexOf(ChatProtocol2.MODE);
			String cmd = line.substring(0, idx);//CHATALL
			String data = line.substring(idx+1);//오늘은 목요일입니다.
			if(cmd.equals(ChatProtocol2.ENTERROOM)) { // ENTERROOM:방이름:유저명;입장하였습니다 -> 채팅방 입장 메세지, 들어와있는 유저명 관리
				sendAllMessage(line);
				int idx1 = data.indexOf(ChatProtocol2.MODE);
				String Rn = data.substring(0,idx1);
				String str = data.substring(idx1+1);
				int idx2 = str.indexOf(";");
				String Un = str.substring(0,idx2);
				String resetUser = "";
				for(int i = 0;roomUser.length > i; i++) {
					if(roomUser[i] == null) {
						roomUser[i] = Rn+ChatProtocol2.MODE+Un;//방이름:유저명
						break;
					}
				}
				for(int i = 0;roomUser.length > i; i++) {
					if(roomUser[i] != null) {
						resetUser += roomUser[i]+";"; //방이름:유저명;방이름:유저명;방이름:유저명;...;
					}
				}
				System.out.println(resetUser);
				sendAllMessage(ChatProtocol2.ADDUSER+ChatProtocol2.MODE+Rn+ChatProtocol2.MODE+resetUser);
				//ADDUSER:방이름:방이름:유저명;방이름:유저명;방이름:유저명;...;
			}else if(cmd.equals(ChatProtocol2.CHAT)) { // CHAT:방이름:유저명;채팅내용 -> 채팅방에서의 채팅을 관리
				//cmd = CHAT | data = 방이름:유저명;채팅내용
				System.out.println("CHAT진입");
				System.out.println("data = "+ data);
				int idx1 = data.indexOf(ChatProtocol2.MODE);
				String Rn = data.substring(0,idx1); // 방이름
				String str = data.substring(idx1+1); // 유저명;채팅내용
				System.out.println(Rn +" "+ str);
				int idx2 = str.indexOf(";");
				String Un = str.substring(0,idx2);  // 유저명
				String msg = str.substring(idx2+1); // 채팅내용
				System.out.println(msg);
				sendAllMessage(ChatProtocol2.MESSAGE+ChatProtocol2.MODE+Rn+
						ChatProtocol2.MODE+"["+Un+"]"+msg); // MESSAGE:채팅방이름:[id]+채팅내용
				System.out.println(Rn+
						ChatProtocol2.MODE+"["+id+"]:"+msg);
			}else if(cmd.equals(ChatProtocol2.ROOMLIST)) {	// ROOMLIST:유저명;채팅방이름 -> 채팅방을 생성
				System.out.println("first:" + data);
				idx = data.indexOf(';');
				cmd = data.substring(0, idx);//ccc
				data = data.substring(idx+1);//이거 뭐야?
				for(int i = 0;roomlist.length>i;i++) {		// 서버에 채팅방 이름 저장
					if(roomlist[i] == null) {
						roomlist[i] = data;
						break;
					}
				}
				System.out.println(ChatProtocol2.ROOMLIST+ChatProtocol2.MODE+data);
				sendAllMessage(ChatProtocol2.ROOMLIST+ChatProtocol2.MODE+data);//ROOMLIST:이거뭐야
			}else if(cmd.equals(ChatProtocol2.ID)) {		// 새로 들어왔을때 채팅방 리스트 새로고침
				String roomreset = "";
				for(int i = 0;roomlist.length>i;i++) {
					if(roomlist[i] != null) {
						roomreset += roomlist[i]+";";
					}
				}
				System.out.println(roomreset);
				sendAllMessage(ChatProtocol2.RESETLIST+ChatProtocol2.MODE+roomreset);
			}else if(cmd.equals(ChatProtocol2.DELETELIST)) {	// DELETELIST:채팅방이름 -> 방장이 채팅방을 종료했을때 채팅방리스트에서 제거
				for(int i = 0;roomlist.length>i;i++) {
					if(data.equals(roomlist[i])) {
						sendAllMessage(ChatProtocol2.DELETELIST+ChatProtocol2.MODE+roomlist[i]);
						roomlist[i] = null;
					}
				}
			}else if(cmd.equals(ChatProtocol2.DELETUSER)) {
				int idx1 = 0;
				System.out.println("채팅방유저리스트 삭제진입:"+data);
				for(int i = 0;roomUser.length > i; i++) {
					if(roomUser[i] != null) {
						System.out.println(roomUser[i]);
						idx1 = roomUser[i].indexOf(ChatProtocol2.MODE);
						String Rn = roomUser[i].substring(0,idx1);
						System.out.println("삭제중"+data+" "+Rn);
						if(data.equals(Rn)) {
						System.out.println("삭제완료");
						roomUser[i] = null;
						System.out.println(roomUser);
						}
					}
				}
			}
			else if(cmd.equals(ChatProtocol2.EXIT)) { // EXIT:방이름:유저명 -> 유저가 채팅방을 나갔을때 명단에서 삭제
				String resetUser = "";
				int idx1 = data.indexOf(ChatProtocol2.MODE);
				String Rn = data.substring(0,idx1);
				for(int i = 0;roomUser.length > i; i++) {
					if(data.equals(roomUser[i])) {
						roomUser[i] = null;
						for(int j = 0;roomUser.length > j; j++) {
							if(roomUser[j] != null) {
								resetUser += roomUser[j]+";";
							}
						}
						break;
					}
				}
				sendAllMessage(ChatProtocol2.ADDUSER+ChatProtocol2.MODE+Rn+ChatProtocol2.MODE+resetUser);
				sendMessage(ChatProtocol2.EXIT+ChatProtocol2.MODE+Rn);//채팅방 종료시 오브잭트 QR[]배열 삭제 해야함
			}
		}
		
		public void sendMessage(String msg) {
			out.println(msg);
		}
		
	}//--ClientThread2
	
	public static void main(String[] args) {
		new MChatServer();
	}
}

