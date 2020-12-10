package datapkg;

import java.util.ArrayList;
import java.util.Date;

import bug_tools.Diff;
import date_tools.DateComputer;
import measurements.Measure;

public class ClassProject {
	private boolean thisNameIsOld;
	private boolean thisNameIsNew; //mostro solo i file di una release il cui nome � nuovo
	private ClassProject newClassProject;
	private ClassProject oldClassProject;
	private boolean deleted;
	private String thisName;
	private String releaseName;
	private boolean bugginess;
	private Diff diff;
	private Measure measure;
	private Date dateCreation;
	private boolean classTakenFromCommit;
	
	public ClassProject(String name, String releaseName, boolean classTakenFromCommit) {
		setNewClassProject(null);
		setOldClassProject(null);
		this.setThisName(name);
		this.setReleaseName(releaseName);
		this.measure = new Measure();
		this.bugginess = false;
		this.deleted = false;
		this.setClassTakenFromCommit(classTakenFromCommit);
	}
	
	public boolean getDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getThisName() {
		return thisName;
	}

	public void setThisName(String name) {
		this.thisName = name;
	}

	public Diff getDiff() {
		return diff;
	}


	
	public boolean isBugginess() {
		return bugginess;
	}

	public void setBugginess(boolean bugginess) {
		this.bugginess = bugginess;
	}
	
	
	public static ClassProject getClassProjectByName(ArrayList<ClassProject> classes, String name) {
		for (ClassProject classProject: classes) {
			if (classProject.getThisName().equalsIgnoreCase(name)){
				return classProject;
			}
		}
		return null;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}

	public Measure getMeasure() {
		return measure;
	}

	public void setMeasure(Measure measure) {
		this.measure = measure;
	}

	public Date getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	public boolean isThisNameIsOld() {
		return thisNameIsOld;
	}

	public void setThisNameIsOld(boolean thisNameIsOld) {
		this.thisNameIsOld = thisNameIsOld;
	}
	
	public boolean isThisNameIsNew() {
		return thisNameIsNew;
	}

	public void setThisNameIsNew(boolean thisNameIsNew) {
		this.thisNameIsNew = thisNameIsNew;
	}

	public ClassProject getNewClassProject() {
		return newClassProject;
	}

	public void setNewClassProject(ClassProject newClassProject) {
		this.newClassProject = newClassProject;
	}

	public ClassProject getOldClassProject() {
		return oldClassProject;
	}

	public void setOldClassProject(ClassProject oldClassProject) {
		this.oldClassProject = oldClassProject;
	}

	public ClassProject renameThisClass(String newName) {
		if(this.thisNameIsOld == true) {
			return this.getNewClassProject();
		}
		
		ClassProject newClass = new ClassProject(newName, this.releaseName, false);
		newClass.setDateCreation(this.getDateCreation());
		newClass.setBugginess(this.bugginess);
		newClass.setDeleted(false);
		newClass.setThisNameIsNew(true);
		newClass.setThisNameIsOld(false);
		
		newClass.setMeasure(this.measure.copyMeasure());
		newClass.setOldClassProject(this);
		
		
		thisNameIsNew = false;
		thisNameIsOld = true;
		this.setNewClassProject(newClass);
		return newClass;
	}
	
	public void updateClassUsing(ClassProject otherClass) {
		//se la classe � gi� contenuta nell'insieme delle classi di una release allora
		if(this.getDateCreation() == null || otherClass.getDateCreation().before(this.getDateCreation())){
			this.setDateCreation(otherClass.getDateCreation());
		}
		
		this.bugginess = otherClass.bugginess;
		this.oldClassProject = otherClass.oldClassProject;
		
		//devo aggiornare le misure
		Measure measure = this.getMeasure();
		measure.setLocTouched(otherClass.getMeasure().getLocTouched());
		measure.setLocAdded(otherClass.getMeasure().getLocAdded());
		measure.setMaxLocAdded(otherClass.getMeasure().getMaxLocAdded());
		measure.setAvgLocAdded(otherClass.getMeasure().getAvgLocAdded());
		measure.setChurn(otherClass.getMeasure().getChurn());
		measure.setMaxChurn(otherClass.getMeasure().getMaxChurn());
		measure.setAvgChurn(otherClass.getMeasure().getAvgChurn());
		
	}
	
	public ClassProject copyThisClass(String newName) {
		
		
		ClassProject newClass = new ClassProject(newName, this.releaseName, false);
		newClass.setDateCreation(this.getDateCreation());
		newClass.setBugginess(this.bugginess);
		newClass.setDeleted(false);
		newClass.setThisNameIsNew(true);
		newClass.setThisNameIsOld(false);
		newClass.setOldClassProject(this.oldClassProject);
		//mi porto avanti le misure perch� mi porto avanti anche la bugginess
		newClass.setMeasure(this.measure.copyMeasure());
		
		return newClass;
	}
	
	public void resetInterReleasesMetrics() {
		this.getMeasure().resetInterReleasesMetrics();
	}
	
	public void setAge(Date date) {
		if(this.dateCreation == null) {
			this.dateCreation = date;
		}
		//calcola quante settimane separano la data "date" dalla data di creazione della classe 
		double age = DateComputer.getDifferenceInWeeksBetweenDates(this.dateCreation, date);
		
		
		this.measure.setAge(age);
	}

	public boolean isClassTakenFromCommit() {
		return classTakenFromCommit;
	}

	public void setClassTakenFromCommit(boolean classTakenFromCommit) {
		this.classTakenFromCommit = classTakenFromCommit;
	}
	
}