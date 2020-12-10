package bug_tools;

public class Diff {
	private String newPath;
	private String oldPath;
	private int addedLines;
	private int deletedLines;
	
	public Diff(String newPath, String oldPath) {
		this.newPath = newPath;
		this.oldPath = oldPath;
		addedLines = 0;
		deletedLines = 0;
	}
	
	public String getNewPath() {
		return newPath;
	}
	
	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}
	
	public String getOldPath() {
		return oldPath;
	}
	
	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}
	
	public int getAddedLines() {
		return addedLines;
	}
	
	public void setAddedLines(int addedLines) {
		this.addedLines = addedLines;
	}
	
	public int getDeletedLines() {
		return deletedLines;
	}
	
	public void setDeletedLines(int deletedLines) {
		this.deletedLines = deletedLines;
	}
	
	public String toString() {
		return "New Path = " + newPath + "\nOldPath = " + oldPath + "\n"
				+ "AddedLines = " + addedLines + " and DeletedLines = " + deletedLines; 
	}
	
	
}
