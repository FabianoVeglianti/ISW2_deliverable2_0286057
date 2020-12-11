package jirapkg;

import java.util.Date;
import java.util.List;

public class ReleaseJira {
	private int id;
	private String name;
	private Date releaseDate;
	
	public int getID() {
		return id;
	}
	
	public void setID(int iD) {
		id = iD;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	public String toString() {
		Integer integerID = getID();
		
		return "ID = " + integerID.toString() + "; name = " + getName() + "; release date = " + getReleaseDate().toString() + ".";
	}
	
	public static ReleaseJira getReleaseByName(List<ReleaseJira> releases, String name) {
		for (ReleaseJira release:releases) {
			if (release.getName().equalsIgnoreCase(name)) {
				return release;
			}
		}
		
		return null;
	}
	
	public static ReleaseJira getReleaseByID(List<ReleaseJira> releases, int id) {
		for (ReleaseJira release:releases) {
			if (release.getID() == id) {
				return release;
			}
		}
		
		return null;
	}
	
	public static void reverseArrayList(List<ReleaseJira> arraylist) {
		int size = arraylist.size();
		for (int i = 0; i<size/2; i++) {
			ReleaseJira temp = arraylist.get(i);
			arraylist.set(i, arraylist.get(size-1-i));
			arraylist.set(size-1-i, temp);
		}
	}
}
