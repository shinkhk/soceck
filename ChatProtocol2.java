package aaa;

public class ChatProtocol2 {

	//ID, CHAT, CHATALL , MESSAGE, CHATLIST
	
	//(C->S) ID:aaa
	public static final String ID = "ID";
	
	//(C->S) CHAT:받는아이디;메세지 ex)CHAT:bbb;밥먹자
	//(S->C) CHAT:보내는아이디;메세지 ex)CHAT:aaa;밥먹자
	public static final String CHAT = "CHAT";
	
	//(C->S) CHATALL:메세지
	//(S->C) CHATALL:[보내는아이디]메세지
	public static final String CHATALL = "CHATALL";
	
	//(C->S) MESSAGE:받는아이디;쪽지내용 ex)MESSAGE:bbb;밥먹자
	//(S->C) MESSAGE:보내는아이디;쪽지내용 ex)MESSAGE:aaa;밥먹자
	public static final String MESSAGE = "MESSAGE";
	
	//(S->C) CHATLIST:aaa;bbb;ccc;홍길동;
	public static final String CHATLIST = "CHATLIST";
	
	public static final String MODE = ":";
	
	public static final String ROOMLIST = "ROOMLIST";
	
	public static final String RESETLIST = "RESETLIST";

	public static final String DELETELIST = "DELETELIST";

	public static final String ENTERROOM = "ENTERROOM";
	
	public static final String ADDUSER = "ADDUSER";
	
	public static final String EXIT = "EXIT";
	
	public static final String DELETUSER = "DELETUSER";
}

