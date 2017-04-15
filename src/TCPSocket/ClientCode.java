package TCPSocket;

import java.awt.Button;
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
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.annotation.processing.Filer;
import javax.swing.JFrame;
import javax.swing.JPanel;

import TCPSocket.ServerCode.MySocket;

public class ClientCode {

	static final String localLocation = "/Users/97building/";
	
	static String clientName = "";
	public static int protNo = 3333;
	static boolean flags = true;
	static String localFileName;
	static File localFile;
	
	static BufferedWriter myFileOut;
	static BufferedReader myFileIn;
	static boolean readFile = true;
	
	protected static Socket socket;
	private static PrintWriter out;
	private static BufferedReader in; 
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		System.out.println("print your name:");
		clientName = new Scanner(System.in).nextLine();
		
		localFileName = localLocation+clientName+"/";
		
		localFile = new File(localFileName);
		if(localFile.exists()&&localFile.isDirectory()){
		}else{
			localFile.mkdir();
		}


		InetAddress address = InetAddress.getByName("localhost");
		socket = new Socket(address, protNo);
		try {
			System.out.println("client socket = "+socket);
			
			Thread readT = new ReadThread();
			Thread writeT = new WriteThread();
			
			readT.start();
			writeT.start();
			try {
				readT.join();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			try {
				writeT.join();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		finally {
			System.out.println("close the Client socket and the io");
			socket.close();
			myFileIn.close();
			myFileOut.close();
		}
		
	}
	
	public static class ReadThread extends Thread{
		private static boolean flag = true;
		
		public ReadThread(){
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void run(){
			while(flag){
				try {
					if(readFile){
						if(in.ready()){
							System.out.println("dodo");
							String nowString = in.readLine();
							System.out.println(nowString);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			close it **pop
		}
	}

	public static class WriteThread extends Thread{
		static boolean flag = true;
		public WriteThread(){
			try {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run(){
			Scanner scanner = new Scanner(System.in);
			while(flag){
				try {
				String now = scanner.nextLine();
				if("***".equals(now)){
					flags = false;
				}else if(now.startsWith("**push")){
					String[] pushNow = now.split("\\$");
					myFileIn = new BufferedReader(new FileReader(new File(localFileName+pushNow[1])));
					int lines = 0;
					while(myFileIn.readLine()!=null){
						lines++;
					}
					out.println("push$"+pushNow[1]+"$"+lines);
					
					myFileIn.close();
					myFileIn = new BufferedReader(new FileReader(new File(localFileName+pushNow[1])));
					String pushString = "";
					while((pushString = myFileIn.readLine())!=null){
						out.println(pushString);
					} 
				}else if(now.startsWith("**pop")){
					readFile = false;
					out.println("pop");
					Thread getFile = new GetFileThread();
					getFile.start();
					try {
						getFile.join();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					readFile = true;
				}else{
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
					StringBuilder printNow = new StringBuilder();
					printNow.append(sdf.format(new Date())+"$");
					printNow.append(clientName+"$");
					printNow.append(now);
					out.println(printNow);
				}
//			close it
			out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	}
	public class CloseSocket{
		public CloseSocket(WriteThread wt,ReadThread rt){
		}
	}
	public static class ServerFrame{
		private JFrame jFrame;
		private JPanel mainPanel,screenP,inputP,submitP,settingP;
		private TextArea screenT,inputT;
		private Button sendB;
		
		public ServerFrame(){
			jFrame = new JFrame("PP群客户端v1.0");
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridLayout(4, 2));
			
					
		}
	}
	
	public static class GetFileThread extends Thread{
		
		public GetFileThread(){
		}
		
		@Override
		public void run(){
			try {
				String getString = in.readLine();
				String[] nowString = getString.split("\\$");
				int length = Integer.parseInt(nowString[1]);
				int hisLen = Integer.parseInt(nowString[2]);
				String[] fileNames = new String[length];
				System.out.println("chose the file you want as number:");
				
				for(int i = 0;i < length;i++){
					if(i== 0){
						System.out.println("聊天记录：");
					}
					if(i==hisLen){
						System.out.println("群聊文件：");
					}
					fileNames[i] = in.readLine();
					System.out.println(i+":"+fileNames[i]);
				}
				
				int choose = new Scanner(System.in).nextInt();//阻塞
				if(choose>=0&&choose<length){
					if(choose >= hisLen){
						out.println("popfor$ClientFile$"+fileNames[choose]);
					}else{
						out.println("popfor$History$"+fileNames[choose]);	
					}
				}else{
					System.out.println("没有该文件，输入有误");
					out.println("error");

					return;
				}
				String receiveString = in.readLine();
				String[] fileBuffer = receiveString.split("\\$");
				File nowFile = new File(localLocation+clientName+"/"+fileBuffer[1]);
				nowFile.createNewFile();
				int lines = Integer.parseInt(fileBuffer[2]);
				FileWriter nfw = new FileWriter(nowFile);
				for(int i = 0;i < lines;i++){
					nfw.write(in.readLine()+"\n");
				}
				nfw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
