package TCPSocket;

import java.awt.GridLayout;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ServerCode {

	public static int protNo = 3333;
	public static int fileProt = 3344;
	private static Map<Integer, MySocket> clients;
	private static Map<Integer,MySocket> fileRecList;
	private static ServerSocket serverS;
	private static ServerSocket serverF;
	private static FileOutputStream messageHistory;
	
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
//	Server setting 
		serverS = new ServerSocket(protNo);
		serverF = new ServerSocket(fileProt);
		System.out.println("The Server is start:"+serverS);
//	file I/O setting
		Scanner scanner = new Scanner(System.in);
		clients = Collections.synchronizedMap(new HashMap<>());
		fileRecList = Collections.synchronizedMap(new HashMap<>());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
		String nowDir = new File("").getAbsolutePath();
		File addFile = new File("History");
		if(!addFile.exists()||!addFile.isDirectory()){
			addFile.mkdir();
		}
		File todayHis = new File(addFile.getAbsolutePath()+"/"+"his"+new File(sdf.format(new Date()))+".txt");
		if(!todayHis.exists()){
			todayHis.createNewFile();
		}
		messageHistory = new FileOutputStream(todayHis);
		
		try {
				Thread serDo = new MessageClient();
				Thread addDo = new AddClient();
				Thread fileRec = new ManageFileReq();
				Thread fileAct = new AddFileReq();
				serDo.start();
				addDo.start();
				fileRec.start();
				fileAct.start();
				
				try {
					serDo.join();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				try {
					addDo.join();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				try {
					fileRec.join();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				try {
					fileAct.join();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally {
			System.out.println("Close the Server socket and the io.");
			messageHistory.close();
			Iterator iterator = clients.entrySet().iterator();
//			±éÀúÉ¾³ýsocket
			while(iterator.hasNext()){
				MySocket socketClose = (MySocket) iterator.next();
				socketClose.close();
			}
			
		}

	}
	
	public static class ManageFileReq extends Thread{
		
		public boolean flag = true;
		
		@Override
		public void run(){
			while(flag){
				if(!fileRecList.isEmpty()){
					Iterator iterator = fileRecList.entrySet().iterator();
					while(iterator.hasNext()){
						Map.Entry entry = (Entry) iterator.next();
						MySocket socketNow = (MySocket) entry.getValue();
						try {
							if(socketNow.in.ready()){
								String nowString  =socketNow.in.readLine();
								if(nowString.startsWith("pop")){
									if(nowString.startsWith("popfor")){
										sendFile(socketNow, nowString);
									}else{
										showFile(socketNow);
									}
								}else if(nowString.startsWith("push")){
									pushFile(nowString, socketNow);
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		public void pushFile(String str,MySocket nowSocket){
			String[] pushString = str.split("\\$");
			
			File nowFile = new File("ClientFile");
			if(nowFile.exists()&&nowFile.isDirectory()){
			}else{
				nowFile.mkdir();
			}
			
			try {
				String nowDir = new File("").getAbsolutePath();
				File addFile = new File(nowDir+"/"+nowFile.getName()+"/"+pushString[1]);
				addFile.createNewFile();
				FileWriter fw = new FileWriter(addFile);
				int nums = Integer.parseInt(pushString[2]);
				for(int i = 0;i < nums;i++){
					fw.write(nowSocket.in.readLine()+"\n");
				}
				System.out.println("file input successful");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void showFile(MySocket socket){
			File clientFile = new File("ClientFile");
			File history = new File("History");
			File[] cfsChild = clientFile.listFiles();
			File[] hfsChild = history.listFiles();
			socket.out.println("popl$"+(cfsChild.length+hfsChild.length)+"$"+hfsChild.length);
			System.out.println("show to client\n"+"popl$"+(cfsChild.length+hfsChild.length)+"$"+hfsChild.length);
			for(File file : hfsChild){
				socket.out.println(file.getName());
			}
			for(File file : cfsChild){
				socket.out.println(file.getName());
			}
		}
		
		public void sendFile(MySocket socket,String str){
			if("error".equals(str)){
				return;
			}
			String[] nowString = str.split("\\$");
			File abs = new File("");
			File nowFile = new File(abs.getAbsolutePath()+"/"+nowString[1]+"/"+nowString[2]);
			try {
				BufferedReader fs = new BufferedReader(new FileReader(nowFile));
				int fileLength = 0;
				String printString = "";
				while((printString = fs.readLine())!=null){
					fileLength++;
				}
				fs.close();
				socket.out.println("popfor$"+nowString[2]+"$"+fileLength);
				fs = new BufferedReader(new FileReader(nowFile));				
				while((printString = fs.readLine())!=null){
					socket.out.println(printString);
				}
				fs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static class AddFileReq extends Thread{
		
		public boolean flag = true;
		
		@Override
		public void run(){
			while(flag){
				try {
					Socket socket = serverF.accept();
					MySocket nowSocket =new MySocket(socket);
					fileRecList.put(nowSocket.hashCode(), nowSocket);
					System.out.println("file socket receive success: "+nowSocket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class MySocket{
		
		public BufferedReader in;
		public PrintWriter out;
		public Socket socket;
		
		public MySocket(Socket netSocket) {
			// TODO Auto-generated constructor stub
			socket = netSocket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		wait for develop
		public void close(){
			try {
				in.close();
				out.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static class AddClient extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				try {
					Socket socket = serverS.accept();//×èÈû
					clients.put(socket.hashCode(), new MySocket(socket));
					System.out.println("Accept the Client:"+socket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//´Ë´¦×èÈû
				
			}
		}
		
	}
	
	public static class MessageClient extends Thread{
		
		@Override
		public void run() {
			while(true){
				if(!clients.isEmpty()){
					Iterator iterator = clients.entrySet().iterator();
					while(iterator.hasNext()){
						Map.Entry entry = (Entry) iterator.next();
						MySocket nowSocket = (MySocket) entry.getValue();
						try {
							if(nowSocket.in.ready()){
								String getString = nowSocket.in.readLine();
								System.out.println(getString);
								messageHistory.write((getString+"\n").getBytes());
								System.out.println(getString);
								printAll(getString);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		public void printAll(String str){
			String[] printString = str.split("\\$");
			Iterator iterator = clients.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry entry = (Entry) iterator.next();
				MySocket nowSocket = (MySocket) entry.getValue();
				System.out.println(printString[0]+" "+printString[1]+"\n"+printString[2]);
				nowSocket.out.println(printString[0]+" "+printString[1]+"\n"+printString[2]);
			}
		}
	}
	

}
