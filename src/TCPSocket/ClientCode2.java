package TCPSocket;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.annotation.processing.Filer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.soap.Text;

import TCPSocket.ServerCode.MySocket;

public class ClientCode2 {

	static final String localLocation = "/Users/97building/";
	
	static String clientName = "";
	public static int protNo = 3333;
	public static int fileRecProt = 3344;
	static boolean flags = true;
	static String localFileName;
	static File localFile;
	
	static BufferedWriter myFileOut;
	static BufferedReader myFileIn;
	static boolean readFile = true;
	
	public static Socket socket;
	public static MySocket fileSocket;
	public static PrintWriter out;
	public static BufferedReader in; 
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ServerFrame nowFrame = new ServerFrame();
		nowFrame.start();
	}
	
	public static class ShowFrame extends JFrame{
		
		protected JFrame jFrameS;
		protected JLabel jTitle;
		protected JPanel mainPanel;
		protected TextArea screenT;
		protected Button close;
		
		public ShowFrame(String putString,String fileName){
			init(putString,fileName);
		}
		
		public void init(String putString,String fileName){
			jFrameS = new JFrame("查看");
			jFrameS.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrameS.setSize(600, 200);
			
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridLayout(3, 1));
			
			jTitle = new JLabel(fileName+"文件详情");
			screenT = new TextArea();
			screenT.setText(putString);
			
			close = new Button("关闭");
			close.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					jFrameS.dispose();
				}
			});
			
			
			mainPanel.add(jTitle);
			mainPanel.add(screenT);
			mainPanel.add(close);
			
			jFrameS.add(mainPanel);
			jFrameS.setResizable(false);
	        jFrameS.setVisible(true);
		}
	}

	public static class ServerFrame extends JFrame{
		protected JFrame jFrame;
		protected JLabel jTitle,jToolsInfo,jInputHint;
		protected JPanel mainPanel,screenP,inputP,submitP,settingP;
		protected TextArea screenT,inputT;
		protected Button pushB,getHistoryB,emptyScreenB,sendB;
		
		public ServerFrame(){
			init();
		}
		
		public void start() throws IOException{

			clientName=JOptionPane.showInputDialog("请输入昵称:");
			jInputHint.setText(clientName+"泥嚎，快来说点什么");
			localFileName = localLocation+clientName+"/";			
			localFile = new File(localFileName);
			if(localFile.exists()&&localFile.isDirectory()){
			}else{
				localFile.mkdir();
			}


			InetAddress address = InetAddress.getByName("localhost");
			InetAddress fileAddress = InetAddress.getByName("localhost");
			fileSocket = new MySocket(new Socket(fileAddress, fileRecProt));
			socket = new Socket(address, protNo);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
			try {
				System.out.println("client socket = "+socket);
				Thread readT = new ReadThread(screenT);				
				readT.start();
				FileThread fileT = new FileThread();
				fileT.start();
				sendB.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						String nowString = inputT.getText();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
						StringBuilder printNow = new StringBuilder();
						printNow.append(sdf.format(new Date())+"$");
						printNow.append(clientName+"$");
						printNow.append(nowString);
						out.println(printNow);
						System.out.println(printNow);
						inputT.setText("");
						// TODO Auto-generated method stub
						
					}
				});
				
				emptyScreenB.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						screenT.setText("");
						// TODO Auto-generated method stub
						
					}
				});
				
				getHistoryB.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						fileSocket.out.println("pop");
						System.out.println("send to the server");
					}
				});
				
				pushB.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						String fileName=JOptionPane.showInputDialog("请输入文件名:");
						try {
							int lines = 0;
							myFileIn = new BufferedReader(new FileReader(new File(localFileName+fileName)));
							while(myFileIn.readLine()!=null){
								lines++;
							}
							fileSocket.out.println("push$"+fileName+"$"+lines);
							myFileIn.close();
							myFileIn = new BufferedReader(new FileReader(new File(localFileName+fileName)));
							String pushString = "";
							while((pushString = myFileIn.readLine())!=null){
								fileSocket.out.println(pushString);
							}
							System.out.println("push success");
							JOptionPane.showMessageDialog(null, "发送成功", "发送文件成功！", JOptionPane.INFORMATION_MESSAGE); 
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
				
				
				try {
					readT.join();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				try {
					fileT.join();
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
				fileSocket.close();
				myFileIn.close();
				myFileOut.close();
			}
		}

		public void init(){
			jFrame = new JFrame("PP群客户端v1.0");
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setSize(600, 400);
			
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridLayout(2, 2));
			
//			显示屏
			screenP = new JPanel();
			screenP.setLayout(new GridLayout(1, 1));
			screenT = new TextArea();
			screenT.setText("");
			screenP.add(screenT);
			
//			工具按钮栏
			settingP = new JPanel();
			settingP.setLayout(new GridLayout(4, 1));
			jToolsInfo = new JLabel("文件工具");
			getHistoryB = new Button("下载文件");
			emptyScreenB = new Button("清空屏幕");
			pushB = new Button("上传文件");
			settingP.add(jToolsInfo);
			settingP.add(getHistoryB);
			settingP.add(emptyScreenB);
			settingP.add(pushB);
			
//			输入框
			inputP = new JPanel();
			inputP.setLayout(new GridLayout(2, 1));
			jInputHint = new JLabel("说点什么");
			inputT = new TextArea();
			inputT.setText("");
			inputP.add(jInputHint);
			inputP.add(inputT);
			
//			发送按钮栏
			submitP = new JPanel();
			sendB = new Button("发送");
			submitP.add(sendB);
			
			jFrame.add(mainPanel);
			mainPanel.add(screenP);
			mainPanel.add(settingP);
			mainPanel.add(inputP);
			mainPanel.add(submitP);
			jFrame.setResizable(false);
	        jFrame.setVisible(true);
	        

		}
		
		public static class FileThread extends Thread{
			
			private boolean flag = true;
			private FileWriter nfw;
			private int lines = 0;
			private String[] fileName;
			private int fileHisNum = 0;
			private int fileNum = 0;
			@Override
			public void run(){
				while(flag){
					try {
						if(fileSocket.in.ready()){
							String nowString = fileSocket.in.readLine();
							if(nowString.startsWith("popfor")){
								String[] fileBuffer = nowString.split("\\$");
								File nowFile = new File(localLocation+clientName+"/"+fileBuffer[1]);
								nowFile.createNewFile();
								lines = Integer.parseInt(fileBuffer[2]);
								nfw = new FileWriter(nowFile);
								StringBuilder screenText = new StringBuilder();
								String get = "";
								for(int i = 0;i < lines;i++){
									get = fileSocket.in.readLine();
									screenText.append(get+"\n");
									nfw.write(get+"\n");
								}
								nfw.close();
								ShowFrame printFrame = new ShowFrame(screenText.toString(), fileBuffer[1]);
//								JOptionPane.showMessageDialog(null, "接收成功", "成功接收目标文件", JOptionPane.INFORMATION_MESSAGE);
							}else{
								String[] fileInfo = nowString.split("\\$");
								fileNum = Integer.parseInt(fileInfo[1]);
								fileHisNum = Integer.parseInt(fileInfo[2]);
								fileName = new String[fileNum];
								StringBuilder infoList = new StringBuilder();
								for(int i = 0;i < fileNum;i++){
									if(i== 0){
										infoList.append("聊天记录：\n");
									}
									if(i==fileHisNum){
										infoList.append("群聊文件：\n");
									}
									fileName[i] = fileSocket.in.readLine();
									infoList.append(i+":"+fileName[i]+"\n");
								}
								infoList.append("chose the file you want as number:\n");
								
								int choose =Integer.parseInt(JOptionPane.showInputDialog(infoList+"\n选择文件编号:"));//阻塞
								if(choose>=0&&choose<fileNum){
									if(choose >= fileHisNum){
										fileSocket.out.println("popfor$ClientFile$"+fileName[choose]);
									}else{
										fileSocket.out.println("popfor$History$"+fileName[choose]);	
									}
								}else{
									JOptionPane.showMessageDialog(null, "请求失败", "没有该文件，输入有误！", JOptionPane.ERROR);
									fileSocket.out.println("error");
								}
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
				}
			}	
			}
		
		public static class ReadThread extends Thread{
			private static boolean flag = true;
			private TextArea screen;
			public ReadThread(TextArea ta) {
				// TODO Auto-generated constructor stub
				screen = ta;
			}
			@Override
			public void run(){
				while(true){
						try {
							if(in.ready()){
								String nowString = in.readLine();
								screen.append(nowString+"\n");
								System.out.println(nowString);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
//				close it **pop
			}//run over
		}
	}
}
