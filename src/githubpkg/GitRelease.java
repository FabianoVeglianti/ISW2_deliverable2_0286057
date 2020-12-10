package githubpkg;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.jgit.lib.ObjectId;

import datapkg.ClassProject;



public class GitRelease {
	private int ID;
	private ObjectId releaseID; //id del commit relativo alla release
	private String name;
	private HashMap<String, ClassProject> classes;
	private ArrayList<GitCommit> revisions;
	private Date date;
	
	public GitRelease() {
		classes = new HashMap<String, ClassProject>();
	}
	
	public int getID() {
		return ID;
	}
	
	public void setID(int ID) {
		this.ID = ID;
	}
	
	public ObjectId getReleaseID() {
		return releaseID;
	}
	
	public void setReleaseID(ObjectId releaseID) {
		this.releaseID = releaseID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	 
	public HashMap<String, ClassProject> getClasses(){
		return this.classes;
	}
	
	public void addOrReplaceClassInClassList(ClassProject classProject) {
		ClassProject existsClass = classes.get(classProject.getThisName());
		if(existsClass == null) {
			classes.put(classProject.getThisName(), classProject);
		} else {
			int size = existsClass.getMeasure().getSize();
			boolean classTakenFromCommit = existsClass.isClassTakenFromCommit();
			classProject.getMeasure().setSize(size);;
			classProject.setClassTakenFromCommit(classTakenFromCommit);
			this.classes.remove(existsClass.getThisName());
			this.classes.put(classProject.getThisName(), classProject);
		}
	}
	
	public void addClassToClassList(ClassProject classProject) {
		//aggiungo la classe se non c'era, altrimenti devo aggiornare le variabili di istanza classe esistente
		ClassProject existsClass = classes.get(classProject.getThisName());
		
		if(existsClass == null) {
			classes.put(classProject.getThisName(), classProject);
			return;
		} else {
			existsClass.updateClassUsing(classProject);
			return;
		}
	}
	
	public static void reverseArrayList(ArrayList<GitRelease> arraylist) {
		int size = arraylist.size();
		for (int i = 0; i<size/2; i++) {
			GitRelease temp = arraylist.get(i);
			arraylist.set(i, arraylist.get(size-1-i));
			arraylist.set(size-1-i, temp);
		}
	}
	
	public static GitRelease getReleaseByName(ArrayList<GitRelease> releases, String name) {
		for (GitRelease release: releases) {
			if(release.getName().equalsIgnoreCase(name)) {
				return release;
			}
		}
		return null;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public ArrayList<GitCommit> getRevisions() {
		return revisions;
	}

	public void setRevisions(ArrayList<GitCommit> revisions) {
		this.revisions = revisions;
	}
	
	public ClassProject getClassByName(String className) {

		return this.classes.get(className);
	
	}
	
	public void setAgeOfClasses() {
		for(ClassProject classProject: classes.values()) {
			classProject.setAge(this.getDate());
		}
	}
	
	
	
}
