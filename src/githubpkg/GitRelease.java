package githubpkg;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;

import datapkg.ClassProject;



public class GitRelease {
	private int id;
	private ObjectId releaseID; //id del commit relativo alla release
	private String name;
	private HashMap<String, ClassProject> classes;
	private ArrayList<GitCommit> revisions;
	private Date date;
	
	public GitRelease() {
		classes = new HashMap<>();
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
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
	 
	public Map<String, ClassProject> getClasses(){
		return this.classes;
	}
	
	public void addOrReplaceClassInClassList(ClassProject classProject) {
		ClassProject existsClass = classes.get(classProject.getThisName());
		if(existsClass == null) {
			classes.put(classProject.getThisName(), classProject);
		} else {
			int size = existsClass.getMeasure().getSize();
			boolean classTakenFromCommit = existsClass.isClassTakenFromCommit();
			boolean bugginess = existsClass.isBugginess();
			classProject.getMeasure().setSize(size);
			classProject.setClassTakenFromCommit(classTakenFromCommit);
			classProject.setBugginess(bugginess);
			
			for(String ticket: existsClass.getBugInClass()) {
				classProject.getBugInClass().add(ticket);
			}
			
			classProject.getBugInClass().addAll(existsClass.getBugInClass());
			this.classes.remove(existsClass.getThisName());
			this.classes.put(classProject.getThisName(), classProject);
		}
	}
	
	public void addClassToClassList(ClassProject classProject) {
		//aggiungo la classe se non c'era, altrimenti devo aggiornare le variabili di istanza classe esistente
		ClassProject existsClass = classes.get(classProject.getThisName());
		
		if(existsClass == null) {
			classes.put(classProject.getThisName(), classProject);
		} else {
			existsClass.updateClassUsing(classProject);
		}
	}
	
	public static void reverseArrayList(List<GitRelease> arraylist) {
		int size = arraylist.size();
		for (int i = 0; i<size/2; i++) {
			GitRelease temp = arraylist.get(i);
			arraylist.set(i, arraylist.get(size-1-i));
			arraylist.set(size-1-i, temp);
		}
	}
	
	public static GitRelease getReleaseByName(List<GitRelease> releases, String name) {
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

	public List<GitCommit> getRevisions() {
		return revisions;
	}

	public void setRevisions(List<GitCommit> revisions) {
		this.revisions = (ArrayList<GitCommit>) revisions;
	}
	
	public ClassProject getClassByName(String className) {

		return this.classes.get(className);
	
	}
	
	public void setAgeOfClasses() {
		for(ClassProject classProject: classes.values()) {
			classProject.setAge(this.getDate());
		}
	}

	public void setNumBugOfClasses() {
		for(ClassProject classProject: classes.values()) {
			classProject.getMeasure().setNumberBugFixes(classProject.getBugInClass().size());
		}
		
	}
	
	
	
}
