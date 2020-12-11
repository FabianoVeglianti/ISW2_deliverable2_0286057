package file_handler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import analysis.Result;
import datapkg.ClassProject;
import githubpkg.GitRelease;
import measurements.Measure;

public class CsvWriter {

	private String projName;
	private double percentageToPrint;
	
	public CsvWriter(String projName) {
		this.projName = projName;
	}
	
	public CsvWriter(String projName, double percentageToPrint) {
		this.projName = projName;
		this.percentageToPrint = percentageToPrint;
	}
	
	public void writeDatasetCSV(List<GitRelease> releases) throws IOException {
		String filename = projName + ".csv";
		
		FileWriter fw = new FileWriter(filename, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		
		pw.println("versionID,"
				+ "versionName,"
				+ "filename,"
				+ "size,"
				+ "locTouched,"
				+ "numRevision,"
				+ "numBugFix,"
				+ "locAdded,"
				+ "maxLocAdded,"
				+ "avgLocAdded,"
				+ "churn,"
				+ "maxChurn,"
				+ "avgChurn,"
				+ "age,"
				+ "buggy");
		
		for (int i = 0; i < releases.size()*percentageToPrint; i++) {
	
			GitRelease release = releases.get(i);
			int count = 0;
			for(ClassProject classProject: release.getClasses().values()) {
				String str = "";
				
				if(classProject.isClassTakenFromCommit()) {
					count = count +1;
					Measure measure = classProject.getMeasure();
					str = str + release.getID() + "," + release.getName() + ",";
					str = str + classProject.getThisName() + "," + measure.getSize() + "," + measure.getLocTouched() + "," + 
					measure.getNumberRevisions() + "," + measure.getNumberBugFixes() + "," +
					measure.getLocAdded() + "," + measure.getMaxLocAdded() + "," + measure.getAvgLocAdded() + "," +
					measure.getChurn() + "," + measure.getMaxChurn() + "," + measure.getAvgChurn() + "," +
					measure.getAge() + "," + classProject.isBugginess();
					pw.println(str);
						
				}
				
			}
	
			
		}
		pw.flush();
		pw.close();
	
	}
	
	public void writeResultCSV(List<Result> results) throws IOException {
		String filename = projName + "Result.csv";
		
		FileWriter fw = new FileWriter(filename, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		
		pw.println("Progetto,"
				+ "#Training Release,"
				+ "%Training,"
				+ "%Buggy in training,"
				+ "%Buggy in test,"
				+ "classifier,"
				+ "balancing,"
				+ "feature selection,"
				+ "TP,"
				+ "FP,"
				+ "TN,"
				+ "FN,"
				+ "precision,"
				+ "recall,"
				+ "Area Under ROC,"
				+ "Kappa");
	
		for (Result result:results) {
			String str = projName + "," +
					result.getNumTrainingRelease() + "," +
					String.format(Locale.US, "%.2f", result.getPercentageTraining()) + "," +
					String.format(Locale.US, "%.2f", result.getPercentageBuggyInTraining()) + "," +
					String.format(Locale.US, "%.2f", result.getPercentageBuggyInTesting()) + "," +
					result.getClassifierName()+ "," +
					result.getResamplingMethodName()+ "," +
					result.getFeatureSelectionName()+ "," +
					result.getTP()+ "," +
					result.getFP()+ "," +
					result.getTN()+ "," +
					result.getFN()+ "," +
					String.format(Locale.US, "%.2f", result.getPrecision())+ "," +
					String.format(Locale.US, "%.2f", result.getRecall())+ "," +
					String.format(Locale.US, "%.2f", result.getAuc())+ "," +
					String.format(Locale.US, "%.2f", result.getKappa());
			pw.println(str);
		}
	
		pw.flush();
		pw.close();
	}
	
	
}
