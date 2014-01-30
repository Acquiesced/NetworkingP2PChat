import java.util.ArrayList;
import java.io.Serializable;
import java.net.InetAddress;



public class ResponseMessage implements Serializable {
	int status;
	String phrase;
	private ArrayList <DirRecord> allDirRecords;
	private DirRecord dRec;
	
	ResponseMessage(int status, String phrase, ArrayList allDirRecords, DirRecord dRec)
	{
		this.status = status;
		this.phrase = phrase;
		this.allDirRecords = allDirRecords;
		this.dRec = dRec;
		
	}
	
	public ArrayList <DirRecord> getAllDirRecords()
	{
		return this.allDirRecords;
	}
	
	public DirRecord getRecord()
	{
		return this.dRec;
	}
	
}
