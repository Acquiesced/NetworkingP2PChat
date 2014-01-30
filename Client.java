import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;


public class Client {
	private static int local_port = 40012;
	private static Vector<PrintWriter> writers = new Vector<PrintWriter>();

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		Scanner input = new Scanner(System.in);
		String serverHostName = "Victor-PC";
		//Input a host name
		System.out.println("Input Host Name of DirServer:");
		serverHostName =  input.next();
		int portNumber = 40010;
		String userNickName = null;
		ResponseMessage rm = null;
		InetAddress IP = InetAddress.getLocalHost(); // client ip
		
		boolean selectedOption = false;
		boolean online = false;
		boolean joined = false;
		boolean isChatroom = false;

		Socket echoSocket = new Socket(serverHostName, portNumber);

		OutputStream os = echoSocket.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		InputStream is = echoSocket.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is);

		while (true) {

			switch (menu()) {

			// online
			case 1:
				try {

					if (online == false) {
						System.out.println("Input NickName");
						userNickName = input.next();
						DirRecord dRecord = new DirRecord(userNickName,
								IP.getHostName(), 0, null);
						Message m = new Message("Online", dRecord, IP);
						oos.reset();
						oos.writeObject(m);
						// oos.flush();
						rm = (ResponseMessage) ois.readObject();
						System.out.println(rm.phrase + " " + rm.status);
						if (rm.phrase.equals("OK")) {
							online = true;
						}
					} else {
						System.out.println("You are already online.");
					}

				} catch (Exception e) {
					e.printStackTrace();

				}
				break;

			// offline
			case 2:
				try {

					if (online == true) {
						if (userNickName != null) {
							// Send message with nickname and server will remove
							// dirRecord from server

							DirRecord dRecord = new DirRecord(userNickName,
									IP.getHostName(), 0, null);
							Message m = new Message("Offline", dRecord, IP);
							oos.reset();
							oos.writeObject(m);

							rm = (ResponseMessage) ois.readObject();
							System.out.println(rm.phrase + "" + rm.status);
							if (rm.phrase.equals("OK")) {
								online = false;
								joined = false;
								isChatroom = false;
								System.out.println("You are now offline.");
							} else {
								System.out.println("Unable to go offline.");
							}
						}
					} else {
						System.out.println("You are not online.");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			// join
			case 3:
				// edit record with chatroom owner nickname
				try {
					if (online == true && joined == false) {
						String chatroomOwnerNickName;
						System.out.println("Input Chat Room Owner NickName");
						chatroomOwnerNickName = input.next();

						DirRecord dRecord = new DirRecord(userNickName,
								IP.getHostName(), 0, chatroomOwnerNickName);
						Message m = new Message("Join", dRecord, IP);
						oos.reset();
						oos.writeObject(m);

						rm = (ResponseMessage) ois.readObject();
						System.out.println(rm.phrase + "" + rm.status);
						//get the message and get local port

						if (rm.phrase.equals("OK")) {
							DirRecord clientChatroomRecord = rm.getRecord();
							String hostNameOfChatroom = clientChatroomRecord.getChatroomOwnerName();
							ArrayList<DirRecord> allDirRecords = new ArrayList<DirRecord>();
							allDirRecords = rm.getAllDirRecords();
							
							int x =0;
							for (x = 0; x < allDirRecords.size(); x++) {
								DirRecord currentRecord = allDirRecords.get(x);
								if (currentRecord.getUserNickName().equals(hostNameOfChatroom))
								{
									String chatroomHostName = currentRecord.getHostName();
									int chatroomPort = currentRecord.getHostPort();
									
//									System.out.println("HOSTNAME"+chatroomHostName);
//									System.out.println("PORT"+chatroomPort);
									
									joined = true;
									System.out
											.println("You have joined a chatroom owned by '"
													+ chatroomOwnerNickName + "'.");
									connectToChatRoom(chatroomHostName, chatroomPort);

									//Send message exit to leave the chatroom
									try {
										if (joined == true) {
											// Send Exit message
											 dRecord = new DirRecord(userNickName,
													IP.getHostName(), 0, null);
											 m = new Message("Exit", dRecord, IP);
											oos.reset();
											oos.writeObject(m);

											// Get response message
											rm = (ResponseMessage) ois.readObject();
											System.out.println(rm.phrase + "" + rm.status);
											if (rm.phrase.equals("OK")) {
												System.out.println("You have exited the chatroom.");
												joined = false;

											} else {
												System.out.println("Failed to exit the chatroom.");
											}

										} else {
											System.out.println("You are not in a chatroom.");
										}
									} catch (Exception e) {
										e.printStackTrace();

									}
									
									
									
								}

							}
							
							
							
						} else {
							System.out
									.println("Failed to join chatroom owned by '"
											+ chatroomOwnerNickName + "'.");
						}
					} else if (online == false) {
						System.out
								.println("You are not online to join a chatroom.");
					} else if (joined == true) {
						System.out
								.println("You have already joined a chatroom.");
					}

				}

				catch (Exception e) {
					e.printStackTrace();
				}
				break;

			// query
			case 4:
				// get array list from reponse message
				try {

					Message m = new Message("Query", null, IP);
					oos.reset();
					oos.writeObject(m);
					rm = (ResponseMessage) ois.readObject();
					System.out.println("Displaying Host Names");
					displayQueryHostnames(rm);
					System.out.println(rm.phrase + " " + rm.status);

				} catch (Exception e) {

					e.printStackTrace();

				}
				break;

			// create server
			case 5:
				// if not exists - create chatroom record like in case 1
				// edit boolean server to true

				try {
					if (online == true && joined == false) {
						DirRecord dRecord = new DirRecord(userNickName,
								IP.getHostName(), local_port, null);
						Message m = new Message("Create Server", dRecord, IP);
						oos.reset();
						oos.writeObject(m);

						rm = (ResponseMessage) ois.readObject();
						System.out.println(rm.phrase + "" + rm.status);

						if (rm.phrase.equals("OK")) {
							isChatroom = true;
							System.out.println("You created a chatoom.");
							runServer();
							
							
							// Set serverStatus to false on record when not a chatroom anymore
							//isChatroom = false;
							
							//Send message exit to leave the chatroom
							try {
								if (isChatroom == true) {
									// Send Exit message
									 dRecord = new DirRecord(userNickName,
											IP.getHostName(), 0, null);
									 m = new Message("Remove Server", dRecord, IP);
									oos.reset();
									oos.writeObject(m);

									// Get response message
									rm = (ResponseMessage) ois.readObject();
									System.out.println(rm.phrase + "" + rm.status);
									if (rm.phrase.equals("OK")) {
										System.out.println("You have stopped hosting the chatroom.");
										isChatroom = false;

									} else {
										System.out.println("Failed to exit the chatroom.");
									}

								} else {
									System.out.println("You are not hosting a chatroom.");
								}
							} catch (Exception e) {
								e.printStackTrace();

							}
							
						} else {
							System.out.println("Failed to create a chatoom.");
						}
					} else if (online == false) {
						System.out.println("You are not online.");
					} else if (joined == true) {
						System.out.println("You are currently in a chatroom.");
					}

				} catch (Exception e) {
					e.printStackTrace();

				}
				break;

			default:
				System.out.println("Please Select a correct input");
				continue;
			}

		}

	}

	public static int menu() {
		System.out.println("");
		System.out.println("Input a number:");
		System.out.println("1. Online");
		System.out.println("2. Offline");
		System.out.println("3. Join");
		System.out.println("4. Query");
		//System.out.println("5. Exit Chatroom");
		System.out.println("5. Create Server");

		Scanner input = new Scanner(System.in);
		int selectedOption = input.nextInt();

		return selectedOption;
	}

	public static void displayQueryHostnames(ResponseMessage rm) {
		ArrayList<DirRecord> allDirRecords;
		allDirRecords = rm.getAllDirRecords();

		for (int x = 0; x < allDirRecords.size(); x++) {
			DirRecord currentRecord = allDirRecords.get(x);
			String currentNameInRecord = currentRecord.userNickName;
			if (currentRecord.getServerStatus() == false) {
				System.out.print("User: " + currentNameInRecord);
				String chatroomHost="";
				if (currentRecord.getChatroomOwnerName()!=null)
				{
					 chatroomHost = currentRecord.getChatroomOwnerName();
					 System.out.print(" in Chatoom hosted by: " + chatroomHost);
				}
				System.out.println();
				
				
			} else {
				System.out.println("Server: " + currentNameInRecord + " Popularity: " +currentRecord.getPopularity() );
			}
		}
		
		
	}

	public static void runServer() throws IOException {

	
		ServerSocket chatServer = null;
		try {
			System.out.println("Waiting for clients on port: "
					+ local_port);
			chatServer = new ServerSocket(local_port); 
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + local_port);
			System.err.println(e);
			
		}


		Socket client = null;
		try {
			int x = 0;
			for (x = 0; x < 8; x++) {
				client = chatServer.accept();
				new clientThread(client, chatServer).start();
				System.out.println("Peer connection Estahblished");
			}

		} catch (IOException e) {

		}

		try {
			chatServer.setSoTimeout(20000);
		} catch (SocketException se) {

		}

	}

	//Connects to a existing chatroom
	public static void connectToChatRoom(String cHost, int cPort) throws IOException {

		Socket joinedClient =null;

		BufferedReader in;
		PrintWriter out = null;

	
		try {
			joinedClient = new Socket(cHost, cPort);
		} catch (IOException e) {
			System.out.println("Could not connect to peer ");
			return;
		}

		try { 
			joinedClient.setSoTimeout(30000);
		} catch (SocketException se) {
			System.err.println("Unable to set socket connection timeout ");
		}


		try {
			out = new PrintWriter(joinedClient.getOutputStream(), true);
			in = new BufferedReader(
					new InputStreamReader(joinedClient.getInputStream()));
		} catch (IOException e) {
			System.err.println(e);
			return;
		}

	
		BufferedReader standardIn = new BufferedReader(new InputStreamReader(
				System.in));

		
		System.out.println("Input '/exit' to disconnect or quit the chatroom\nInput a message to send data to chatroom:");

		String inMessage2;
		String outMessage2;


		while (true) {
			try {
				if (in.ready()) {
					inMessage2 = in.readLine(); 
					if (inMessage2.equals("Dics_From_Chat")) {
						System.out.println("A user has disconnected.");

					} else {
					
						System.out.println(inMessage2);
					}
				} else if (standardIn.ready()) {
					outMessage2 = standardIn.readLine(); 

					if (outMessage2.equals("/exit"))
					{
						System.out.println("Disconnecting from chatroom.");
						out.println("Dics_From_Chat");
					

						break;
					} else { 
						out.println(outMessage2);
					}

				}

			} catch (SocketException SE) { // Disconnect clients
				System.out.println("Connection timed out, standby ");
				joinedClient.close();
				
			}
		}

	}

	private static class clientThread extends Thread {

		private Socket client;
		private ServerSocket chatServer;

		clientThread(Socket clientSocket, ServerSocket chatServer) {
			this.client = clientSocket;
			this.chatServer = chatServer;
		}

		public void run() {
			
			BufferedReader in;
			PrintWriter out = null;
			//create streams to peers
			try {
				in = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				out = new PrintWriter(client.getOutputStream(), true);
				writers.add(out);

			} catch (IOException e) {
				System.err.println(e);
				return;
			}


			BufferedReader stdIn = new BufferedReader(new InputStreamReader(
					System.in));

			
			System.out.println("Input '/exit' to disconnect or quit the chatroom\nInput a message to send data to chatroom:");

			String inMessage1;
			String outMessage1;



			while (true) {
				try {
					if (in.ready()) {
						inMessage1 = in.readLine();
						if (inMessage1.equals("/exit")) {
							
						} else {
							System.out.println("From chatroom" + ": " + inMessage1);
							for (PrintWriter writer : writers) {
								writer.println("From chatroom" + ": " + inMessage1);
							}
						}
					} else if (stdIn.ready()) {
						outMessage1 = stdIn.readLine();

						if (outMessage1.equals("/exit")) {
							System.out.println("Disconnecting from chatroom.");
			
							chatServer.close();
				
						} else {
					
							for (PrintWriter writer : writers) {
								writer.println("From chatroom" + ": " + outMessage1);
							}
						}

					}
				} catch (SocketException SE) {
					System.out.println("Connection timed out ... ");
				
				} catch (IOException e) {
				
					e.printStackTrace();
				}
			}
		}
	}
	
	//MTU 80
    public static void send() {
        while (true) {
        	Socket socket = null;
            byte[] readInput = new byte[80];
            DatagramPacket packett = null;
            byte[] m = null;
            byte[] input = null;
            try {
                input = new byte[80];
                packett = new DatagramPacket(input, input.length);
                m = getPacket(packett, input);
                          } catch (Exception e) {
              
            }
        }
	}
    
	private static byte[] getPacket(DatagramPacket packett, byte[] input) {
		 byte[] inputM = null;
		 inputM = packett.getData();
		 return inputM;
	}
}