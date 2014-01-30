import java.io.Serializable;
import java.net.InetAddress;



public class Message implements Serializable{
	
	DirRecord dirRecord;
	String message;
	InetAddress host; //Used for be server to know where to send the ACK
	
	Message(String message, DirRecord dirRecord,InetAddress host)
	{
		this.message = message;
		this.dirRecord = dirRecord;
		this.host = host;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public DirRecord getRecord()
	{
		return this.dirRecord;
	}
	
}
