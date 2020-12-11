package githubpkg;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import numeric_tools.IntegerTool;

public class GitCommit {

	private ObjectId commitID;
	private Date date;
	private ObjectId parentID;
	private String message;
	
	
	public GitCommit(ObjectId commitID, Date date, ObjectId parentID, String message) {
		this.commitID = commitID;
		this.date = date;
		this.parentID = parentID;
		this.message = message;
	}
	
	public ObjectId getCommitID() {
		return commitID;
	}
	
	public void setCommitID(ObjectId commitID) {
		this.commitID = commitID;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public ObjectId getParentID() {
		return parentID;
	}
	
	public void setParentID(ObjectId parentID) {
		this.parentID = parentID;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public boolean hasTicketID(String ticketID) {
		
		int index = message.indexOf(ticketID);
		if (index == -1) {
			return false;
		}
		
		int checkIndex = index + ticketID.length();
		IntegerTool it = new IntegerTool();
		if (checkIndex < message.length()) {
			return !(it.isNumeric(message.substring(checkIndex, checkIndex + 1)));
		}
		
		return true;
	}
	
}

