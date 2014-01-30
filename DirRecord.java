import java.io.Serializable;
import java.net.InetAddress;

public class DirRecord implements Serializable{
	
	String userNickName; //nickname of user
	String chatroomOwnerNickName; //nickname of the host you are connected to
	String hostName; //hostname of client
	int hostPort;
	double popularity;
	boolean server=false;
	
	DirRecord(String userNickName,String hostName, int hostPort, String chatroomOwnerNickName)
	{
		this.userNickName = userNickName;
		this.hostName = hostName;
		this.chatroomOwnerNickName = chatroomOwnerNickName;
		this.hostPort = hostPort;
	}
	
	public double calculatePopularity(double totalUsersInRoom,double totalUsersOnline)
	{
		if (totalUsersInRoom==0)
		{
			this.popularity=0;
			
		}
		else
		{
			//System.out.println("INSIDE");
			//System.out.println(totalUsersInRoom);
			//System.out.println(totalUsersOnline);
			this.popularity = totalUsersInRoom/totalUsersOnline;
			//System.out.println("CALCING P " + this.popularity);
		}
		
		return this.popularity;
	}
	
	public String getHostName()
	{
		return this.hostName;
	}
	
	
	public String getUserNickName()
	{
		return this.userNickName;
	}
	
	public String getChatroomOwnerName()
	{
		return this.chatroomOwnerNickName;
	}
	
	public void setChatroomOwnerName(String chatroomName)
	{
		this.chatroomOwnerNickName=chatroomName;
	}
	
	public void setServerStatus(boolean status)
	{
		this.server = status;
		
	}
	
	public boolean getServerStatus()
	{
		return this.server;
		
	}
	
	public void setHostPort(int hostPort)
	{
		this.hostPort = hostPort;
	}
	
	public int getHostPort()
	{
		return this.hostPort;
	}
	
	public double getPopularity()
	{
		return this.popularity;
	}
	
}
