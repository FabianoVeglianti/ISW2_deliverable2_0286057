package datapkg;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import bug_tools.Diff;
import date_tools.DateComputer;
import measurements.Measure;

public class ClassProject {
	private boolean thisNameIsOld;
	private boolean thisNameIsNew; //mostro solo i file di una release il cui nome è nuovo
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
	private Set<String> bugInClassHistory;
	
	public ClassProject(String name, String releaseName, boolean classTakenFromCommit) {
		setNewClassProject(null);
		setOldClassProject(null);
		this.setThisName(name);
		this.setReleaseName(releaseName);
		this.measure = new Measure();
		this.bugginess = false;
		this.deleted = false;
		this.setClassTakenFromCommit(classTakenFromCommit);
		this.bugInClassHistory = new TreeSet<>();
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
	
	
	public static ClassProject getClassProjectByName(List<ClassProject> classes, String name) {
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

	/**
	 * Dato il nuovo nome della classe, crea e restituisce un oggetto ClassProject che rappresenta la stessa classe con nuovo nome
	 * le misure della nuova classe sono uguali a quelle della vecchia e tutti i bug che affliggevano la vecchia affliggono la nuova.
	 * La classe su cui viene chiamato il metodo smette di esistere.
	 * */
	public ClassProject renameThisClass(String newName) {
		if(this.thisNameIsOld) {
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
		newClass.getBugInClass().addAll(this.getBugInClass());
		
		thisNameIsNew = false;
		thisNameIsOld = true;
		this.setDeleted(true);
		this.setNewClassProject(newClass);
		return newClass;
	}
	
	/**
	 * Aggiorna i dati di una classe esistente utilizzando i dati della classe passata come parametro
	 * */
	public void updateClassUsing(ClassProject otherClass) {
		//se la classe è già contenuta nell'insieme delle classi di una release allora
		if(this.getDateCreation() == null || otherClass.getDateCreation().before(this.getDateCreation())){
			this.setDateCreation(otherClass.getDateCreation());
		}
		
		this.bugginess = otherClass.bugginess;
		this.oldClassProject = otherClass.oldClassProject;
		
		//devo aggiornare le misure
		this.getMeasure().setLocTouched(otherClass.getMeasure().getLocTouched());
		this.getMeasure().setLocAdded(otherClass.getMeasure().getLocAdded());
		this.getMeasure().setMaxLocAdded(otherClass.getMeasure().getMaxLocAdded());
		this.getMeasure().setAvgLocAdded(otherClass.getMeasure().getAvgLocAdded());
		this.getMeasure().setChurn(otherClass.getMeasure().getChurn());
		this.getMeasure().setMaxChurn(otherClass.getMeasure().getMaxChurn());
		this.getMeasure().setAvgChurn(otherClass.getMeasure().getAvgChurn());
		
	}
	
	/**
	 * Dato il nuovo nome della classe, crea e restituisce un oggetto ClassProject che rappresenta la stessa classe con nuovo nome
	 * le misure della nuova classe sono uguali a quelle della vecchia e tutti i bug che affliggevano la vecchia affliggono la nuova.
	 * La classe su cui viene chiamato il metodo NON smette di esistere.
	 * */
	public ClassProject copyThisClass(String newName) {
		
		
		ClassProject newClass = new ClassProject(newName, this.releaseName, false);
		newClass.setDateCreation(this.getDateCreation());
		newClass.setBugginess(this.isBugginess());
		newClass.setDeleted(false);
		newClass.setThisNameIsNew(true);
		newClass.setThisNameIsOld(false);
		newClass.setOldClassProject(this.oldClassProject);
		newClass.getBugInClass().addAll(this.getBugInClass());

		//mi porto avanti le misure perché mi porto avanti anche la bugginess
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

	public Set<String> getBugInClass() {
		return bugInClassHistory;
	}

	public void setBugInClass(Set<String> bugInClass) {
		this.bugInClassHistory = bugInClass;
	}
	
}
