package measurements;

import bug_tools.Diff;

public class Measure {
	private int size;
	private int locTouched; //per versione
	private int numberRevisions; //storico
	private int numberBugFixes; //storico
	private int locAdded; //per versione
	private int maxLocAdded; //per versione
	private int avgLocAdded; //per versione
	private int churn; //per versione
	private int maxChurn; //per versione
	private int avgChurn; //per versione
	private double age; //storico
	
	public Measure() {
		size = 0;
		locTouched = 0;
		numberRevisions = 0;
		numberBugFixes = 0;
		locAdded = 0;
		maxLocAdded = 0;
		avgLocAdded = 0;
		churn = 0;
		maxChurn = 0;
		avgChurn = 0;
		setAge(0);
	}

	
	public void resetInterReleasesMetrics() {
		locTouched = 0;
		locAdded = 0;
		maxLocAdded = 0;
		avgLocAdded = 0;
		churn = 0;
		maxChurn = 0;
		avgChurn = 0;
	}
	
	public void increaseBugFixes() {
		this.numberBugFixes = this.numberBugFixes +1;
	}
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getLocTouched() {
		return locTouched;
	}
	public void setLocTouched(int locTouched) {
		this.locTouched = locTouched;
	}
	
	public int getNumberRevisions() {
		return numberRevisions;
	}
	public void setNumberRevisions(int numberRevisions) {
		this.numberRevisions = numberRevisions;
	}
	
	public int getNumberBugFixes() {
		return numberBugFixes;
	}
	public void setNumberBugFixes(int numberBugFixes) {
		this.numberBugFixes = numberBugFixes;
	}
	
	public int getLocAdded() {
		return locAdded;
	}
	public void setLocAdded(int locAdded) {
		this.locAdded = locAdded;
	}
	
	public int getMaxLocAdded() {
		return maxLocAdded;
	}
	public void setMaxLocAdded(int maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}
	
	public int getAvgLocAdded() {
		return avgLocAdded;
	}
	public void setAvgLocAdded(int avgLocAdded) {
		this.avgLocAdded = avgLocAdded;
	}
	
	public int getChurn() {
		return churn;
	}
	public void setChurn(int churn) {
		this.churn = churn;
	}
	
	public int getMaxChurn() {
		return maxChurn;
	}
	public void setMaxChurn(int maxChurn) {
		this.maxChurn = maxChurn;
	}
	
	public int getAvgChurn() {
		return avgChurn;
	}
	public void setAvgChurn(int avgChurn) {
		this.avgChurn = avgChurn;
	}
	
	public double getAge() {
		return age;
	}
	public void setAge(double age) {
		this.age = age;
	}
	
	public void setSizeFromContent(String content) {
		/* 
		 * dato il contenuto di una classe calcola le linee di codice
		 * (la metrica utilizzata è la metrica LOC - commenti e string)
		 */
		int startLine = 0;
		int endLine = content.indexOf("\n");
		String line = null;
		while(endLine != -1) {
			line = content.substring(startLine, endLine);
			
			line = line.trim();
			
			if( !(line.startsWith("/*") || line.startsWith("*") || line.startsWith("//") || line.startsWith("*/") || line.equalsIgnoreCase(""))) {
				size = size + 1;
			}
			startLine = endLine + 1;
			if(startLine < content.length()) {
				line = content.substring(startLine);
				
				if(line.indexOf("\n")!= -1) {
					endLine = endLine + 1 + line.indexOf("\n");
				} else {
					endLine = endLine + 1;
				}
				
			} else {
				endLine = -1;
			}
			
		}
		
	}
	
	public Measure copyMeasure() {
		Measure newMeasure = new Measure();
		newMeasure.setLocTouched(this.locTouched);
		newMeasure.setNumberRevisions(this.numberRevisions);
		newMeasure.setNumberBugFixes(this.numberBugFixes);
		newMeasure.setLocAdded(this.locAdded);
		newMeasure.setMaxLocAdded(this.maxLocAdded);
		newMeasure.setAvgLocAdded(this.avgLocAdded);
		newMeasure.setChurn(this.churn);
		newMeasure.setMaxChurn(this.maxChurn);
		newMeasure.setAvgChurn(this.avgChurn);
		newMeasure.setAge(this.getAge());
		return newMeasure;
	}
	

	public void setMeasuresPerRelease(Diff diff) {
		locTouched = locTouched + diff.getAddedLines() + diff.getDeletedLines();
		locAdded = locAdded + diff.getAddedLines();
		if(diff.getAddedLines() > maxLocAdded)
			maxLocAdded = diff.getAddedLines();
		avgLocAdded = ((avgLocAdded * numberRevisions)+diff.getAddedLines())/(numberRevisions + 1);
		churn = churn + diff.getAddedLines() - diff.getDeletedLines();
		if(diff.getAddedLines() - diff.getDeletedLines() > maxChurn) {
			maxChurn = diff.getAddedLines() - diff.getDeletedLines();
		}
		avgChurn = ((avgChurn * numberRevisions)+diff.getAddedLines() - diff.getDeletedLines())/(numberRevisions + 1); 
	
		numberRevisions = numberRevisions + 1;
	}

	
	
}