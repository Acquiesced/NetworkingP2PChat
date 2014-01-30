import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

import javax.swing.text.html.HTMLDocument.Iterator;

public class Server {
	public static ArrayList<DirRecord> allDirRecords;

	public static int portNumber = 40010;

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		allDirRecords = new ArrayList<DirRecord>();

		try {

			ServerSocket serverSocket = new ServerSocket(portNumber);
			Socket clientSocket = serverSocket.accept();

			int x = 0;
			for (x = 0; x < 8; x++) {
				clientSocket = serverSocket.accept();
				new serverThread(clientSocket).start();
			}

		} catch (IOException e) {
			System.out
					.println("Exception caught when trying to listen on port "
							+ portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		} finally {

		}

	}

	private static class serverThread extends Thread {

		private Socket clientSocket;

		serverThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void run() {
			try {
				OutputStream os;
				ObjectOutputStream oos;
				InputStream is;
				ObjectInputStream ois;

				os = clientSocket.getOutputStream();
				oos = new ObjectOutputStream(os);
				is = clientSocket.getInputStream();
				ois = new ObjectInputStream(is);

				DirRecord dRecord;
				Message m;
				while (true) {
					try {
						m = (Message) ois.readObject();

						if (m != null) {
							System.out.println("Message: " + m.message);

							if (m.message.equals("Online")) {

								// Check if name is in use
								boolean nameInUse = false;
								for (int x = 0; x < allDirRecords.size(); x++) {
									DirRecord currentRecord = allDirRecords
											.get(x);
									if (currentRecord.getUserNickName().equals(
											m.getRecord().getUserNickName())) {
										nameInUse = true;
									}
								}

								if (nameInUse == false) {
									// If user is online, add the dirRecord from
									// the
									// message to allDirRecords
									allDirRecords.add(m.getRecord());

									// Send ACK
									sendResponseMsgOK(oos, m.getRecord());
									// break;//Remove this later, i only added
									// this
									// so it wouldn't take extra CPU
									System.out.println("User "
											+ m.dirRecord.userNickName
											+ " is online.");

									// Calculate Popularity if there is server
									int numOfClients = 0;
									int x = 0;
									for (x = 0; x < allDirRecords.size(); x++) {
										DirRecord currentRecord1 = allDirRecords
												.get(x);

										if (currentRecord1.getServerStatus() == true) {
											String tempChatOwner = currentRecord1
													.getUserNickName();

											for (x = 0; x < allDirRecords
													.size(); x++) {
												DirRecord currentRecord2 = allDirRecords
														.get(x);
												if (currentRecord2
														.getChatroomOwnerName() != null
														&& currentRecord2
																.getChatroomOwnerName()
																.equals(tempChatOwner)) {
													numOfClients++;
												}
											}

											currentRecord1.calculatePopularity(
													numOfClients,
													allDirRecords.size());
											displayAllRecordInfo();

										}
									}

								} else {
									sendResponseMsgError(oos, m.getRecord());
								}

							} else if (m.message.equals("Offline")) {
								if (allDirRecords.size() > 0) {
									// for each dirRecord in allDirRecords, find
									// the same hostname and remove it
									boolean deletedRecord = false;
									for (int x = 0; x < allDirRecords.size(); x++) {
										DirRecord currentRecord = allDirRecords
												.get(x);
										String nameToSearch = m.dirRecord.userNickName;
										String currentNameInRecord = currentRecord.userNickName;
										if (currentNameInRecord
												.equals(nameToSearch)) {

											allDirRecords.remove(x);
											System.out.println("User "
													+ currentNameInRecord
													+ " is offline.");

											deletedRecord = true;
											sendResponseMsgOK(oos,
													m.getRecord());

											// calculate popularity when going
											// offline
											int numOfClients = 0;
											int y = 0;
											for (y = 0; y < allDirRecords
													.size(); y++) {
												DirRecord currentRecord1 = allDirRecords
														.get(y);

												if (currentRecord1
														.getServerStatus() == true) {
													String tempChatOwner = currentRecord1
															.getUserNickName();

													for (y = 0; y < allDirRecords
															.size(); y++) {
														DirRecord currentRecord2 = allDirRecords
																.get(y);
														if (currentRecord2
																.getChatroomOwnerName() != null
																&& currentRecord2
																		.getChatroomOwnerName()
																		.equals(tempChatOwner)) {
															numOfClients++;
														}
													}

													currentRecord1
															.calculatePopularity(
																	numOfClients,
																	allDirRecords
																			.size());
													displayAllRecordInfo();

												}
											}

											break;

										}

									}
									// Send error if it wasnt user found
									if (deletedRecord == false) {

										System.out
												.println("Record not found(Shouldnt happen)");
										sendResponseMsgError(oos, m.getRecord());
									}
								}
							} else if (m.message.equals("Join")) {

								boolean found = false;
								String chatRoomNickNameToSearch = m.getRecord()
										.getChatroomOwnerName();
								String userNickName = m.getRecord()
										.getUserNickName();

								int x = 0;
								for (x = 0; x < allDirRecords.size(); x++) {
									DirRecord currentRecord = allDirRecords
											.get(x);
									String currentNameInRecord = currentRecord.userNickName;
									boolean currentServerStatus = currentRecord
											.getServerStatus();

									// If the chatroom you are trying to join is
									// avaialable then
									if (currentNameInRecord
											.equals(chatRoomNickNameToSearch)
											&& currentServerStatus == true) {
										// Add the chatroom name to the current
										// record
										for (x = 0; x < allDirRecords.size(); x++) {
											DirRecord recordToUpdate = allDirRecords
													.get(x);
											if (recordToUpdate
													.getUserNickName().equals(
															userNickName)) {
												recordToUpdate
														.setChatroomOwnerName(chatRoomNickNameToSearch);
												sendResponseMsgOK(oos,
														m.getRecord());
												found = true;
												System.out
														.println(userNickName
																+ " has joined chatroom owned by "
																+ chatRoomNickNameToSearch);

												// calculate number of clients
												// in the server that was joined
												int numOfClients = 0;
												for (x = 0; x < allDirRecords
														.size(); x++) {
													DirRecord currentRecord1 = allDirRecords
															.get(x);

													if ((currentRecord1
															.getChatroomOwnerName() != null && currentRecord1
															.getChatroomOwnerName()
															.equals(chatRoomNickNameToSearch))) {
														numOfClients++;
													}
												}

												for (x = 0; x < allDirRecords
														.size(); x++) {
													DirRecord currentRecord1 = allDirRecords
															.get(x);

													if (chatRoomNickNameToSearch
															.equals(currentRecord1
																	.getUserNickName())) {
														currentRecord1
																.calculatePopularity(
																		numOfClients,
																		allDirRecords
																				.size());
														break;
													}
												}

												displayAllRecordInfo();

												break;
											}
										}
										break;
									}

								}
								if (found == false) {
									sendResponseMsgError(oos, m.getRecord());
									System.out
											.println(userNickName
													+ " has failed to join chatroom owned by "
													+ chatRoomNickNameToSearch
													+ ".  Chatroom does not exist.");
								}

							} else if (m.message.equals("Query")) {

								sendResponseMsgOK(oos, m.getRecord());

							} else if (m.message.equals("Exit")) {

								String userNickName = m.getRecord()
										.getUserNickName();

								int x = 0;
								for (x = 0; x < allDirRecords.size(); x++) {
									DirRecord currentRecord = allDirRecords
											.get(x);
									if (currentRecord.getUserNickName().equals(
											userNickName)
											&& currentRecord
													.getChatroomOwnerName() != null) {
										String chatRoomNickName = currentRecord
												.getChatroomOwnerName();
										currentRecord
												.setChatroomOwnerName(null);
										sendResponseMsgOK(oos, m.getRecord());
										// found=true;
										System.out
												.println(userNickName
														+ " has left chatroom owned by "
														+ chatRoomNickName);

										// count number of clients in chatroom
										// when leaving
										int numOfClients = 0;
										for (x = 0; x < allDirRecords.size(); x++) {
											DirRecord currentRecord1 = allDirRecords
													.get(x);

											if (currentRecord1
													.getChatroomOwnerName() != null
													&& currentRecord1
															.getChatroomOwnerName()
															.equals(chatRoomNickName)) {
												numOfClients++;
											}

										}

										// Find and update chatroom popularity
										for (x = 0; x < allDirRecords.size(); x++) {
											DirRecord currentRecord1 = allDirRecords
													.get(x);

											if (currentRecord1
													.getUserNickName().equals(
															chatRoomNickName)) {
												currentRecord1
														.calculatePopularity(
																numOfClients,
																allDirRecords
																		.size());
												break;
											}

										}

										displayAllRecordInfo();
										break;
									}
								}

							} else if (m.message.equals("Create Server")) {
								String userNickName = m.getRecord()
										.getUserNickName();
								int x = 0;
								for (x = 0; x < allDirRecords.size(); x++) {
									DirRecord currentRecord = allDirRecords
											.get(x);
									if (currentRecord.getUserNickName().equals(
											userNickName)) {

										currentRecord.setServerStatus(true);
										currentRecord.setHostPort(m.getRecord()
												.getHostPort());
										sendResponseMsgOK(oos, m.getRecord());
										System.out.println(userNickName
												+ " created a chatroom");
										break;
									}
								}

							} else if (m.message.equals("Remove Server")) {
								String userNickName = m.getRecord()
										.getUserNickName();
								int x = 0;
								for (x = 0; x < allDirRecords.size(); x++) {
									DirRecord currentRecord = allDirRecords
											.get(x);
									if (currentRecord.getUserNickName().equals(
											userNickName)) {

										currentRecord.setServerStatus(false);
										currentRecord.setHostPort(m.getRecord()
												.getHostPort());
										sendResponseMsgOK(oos, m.getRecord());
										System.out.println(userNickName
												+ " created a chatroom");
										break;
									}
								}

							}

							else {
								sendResponseMsgError(oos, m.getRecord());

							}
						} else {
							sendResponseMsgError(oos, m.getRecord());

						}

					} catch (IOException e) {
						System.out
								.println("Exception caught when trying to listen on port "
										+ portNumber
										+ " or listening for a connection");
						System.out.println(e.getMessage());
						e.printStackTrace();
					} catch (Exception e) {

						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void sendResponseMsgOK(ObjectOutputStream oos, DirRecord dRec)
			throws IOException {
		oos.reset();
		ResponseMessage rm = new ResponseMessage(200, "OK", allDirRecords, dRec);
		oos.writeObject(rm);
	}

	public static void sendResponseMsgError(ObjectOutputStream oos,
			DirRecord dRec) throws IOException {
		oos.reset();
		ResponseMessage rm = new ResponseMessage(400, "" + "Error", null, dRec);
		oos.writeObject(rm);
	}

	// Displays user name, server name, server status, popularity (used for
	// debugging)
	public static void displayAllRecordInfo() {
		int x = 0;
		System.out
				.println("Displaying Record: NickName, ServerOwnerNickName, ServerStatus.");
		for (x = 0; x < allDirRecords.size(); x++) {
			DirRecord currentRecord = allDirRecords.get(x);
			System.out.println(currentRecord.getUserNickName() + " "
					+ currentRecord.getChatroomOwnerName() + " "
					+ currentRecord.getServerStatus() + " "+currentRecord.getPopularity());

		}
		

	
	    }
	//MTU and Datagrams
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
                serverThread st = new serverThread(socket);
                Thread thread = new Thread(st);
                thread.run();
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