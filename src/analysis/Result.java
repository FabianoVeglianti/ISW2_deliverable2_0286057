package analysis;

import weka.core.Instance;
import weka.core.Instances;


public class Result {
	
	double TP;
	double FP;
	double TN;
	double FN;
	double precision;
	double recall;
	double auc;
	double kappa;
	
	private String projName;
	private int numTrainingRelease;
	private double percentageTraining;
	private double percentageBuggyInTraining;
	private double percentageBuggyInTesting;
	
	private String classifierName;
	private String featureSelectionName;
	private String resamplingMethodName;
	
	public void setDatasetValues(Instances training, Instances test, int testReleaseIndex) {
		
		this.numTrainingRelease = testReleaseIndex - 1;
		
		int numInstancesTraining = training.numInstances();
		int numInstancesTest = test.numInstances();
		this.percentageTraining = (double)((double)numInstancesTraining/(double)(numInstancesTraining+numInstancesTest));
		
		int numBuggyTraining = 0;
		int numFeatures = training.numAttributes();
		for(Instance instance: training) {
			if( ((String)instance.stringValue(numFeatures-1)).equalsIgnoreCase("true")) {
				numBuggyTraining = numBuggyTraining + 1;
			}
		}
		
		int numBuggyTest = 0;
		for(Instance instance: test) {
			if( ((String)instance.stringValue(numFeatures-1)).equalsIgnoreCase("true")) {
				numBuggyTest = numBuggyTest + 1;
			}
		}
		
		this.percentageBuggyInTraining = (double)numBuggyTraining/(double)numInstancesTraining;
		this.percentageBuggyInTesting = (double)numBuggyTest/(double)numInstancesTest;
	}
	
	
	
	public String getProjName() {
		return projName;
	}

	public void setProjName(String projName) {
		this.projName = projName;
	}

	public int getNumTrainingRelease() {
		return numTrainingRelease;
	}

	public void setNumTrainingRelease(int numTrainingRelease) {
		this.numTrainingRelease = numTrainingRelease;
	}

	public double getPercentageTraining() {
		return percentageTraining;
	}

	public void setPercentageTraining(double percentageTraining) {
		this.percentageTraining = percentageTraining;
	}

	public double getPercentageBuggyInTraining() {
		return percentageBuggyInTraining;
	}

	public void setPercentageBuggyInTraining(double percentageBuggyInTraining) {
		this.percentageBuggyInTraining = percentageBuggyInTraining;
	}

	public double getPercentageBuggyInTesting() {
		return percentageBuggyInTesting;
	}

	public void Buggy(double percentageBuggyInTesting) {
		this.percentageBuggyInTesting = percentageBuggyInTesting;
	}

	public double getTP() {
		return TP;
	}

	public void setTP(double tP) {
		TP = tP;
	}

	public double getFP() {
		return FP;
	}

	public void setFP(double fP) {
		FP = fP;
	}

	public double getTN() {
		return TN;
	}

	public void setTN(double tN) {
		TN = tN;
	}

	public double getFN() {
		return FN;
	}

	public void setFN(double fN) {
		FN = fN;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}

	public double getKappa() {
		return kappa;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}

	public void setValues(double tP, double fP, double tN, double fN, double precision, double recall, double auc,
			double kappa) {
		TP = tP;
		FP = fP;
		TN = tN;
		FN = fN;
		this.precision = precision;
		this.recall = recall;
		this.auc = auc;
		this.kappa = kappa;
	}
	
	public Result(String classifierName, String featureSelectionName, String resamplingMethodName) {
		super();
		this.setClassifierName(classifierName);
		this.setFeatureSelectionName(featureSelectionName);
		this.setResamplingMethodName(resamplingMethodName);
	}
	
	public Result() {
		// TODO Auto-generated constructor stub
	}

	public String toString() {
		String str = "";
		str = str + "TP: " + TP + " - FP: "+ FP + "\nFN: "+ FN + " - TN: " + TN+ "\n"
				+ "precision: " + precision +"\nrecall: " + recall + "\nauc: "+ auc +"\nkappa: "+ kappa;
		return str;
	}



	public String getFeatureSelectionName() {
		return featureSelectionName;
	}



	public void setFeatureSelectionName(String featureSelectionName) {
		this.featureSelectionName = featureSelectionName;
	}



	public String getResamplingMethodName() {
		return resamplingMethodName;
	}



	public void setResamplingMethodName(String resamplingMethodName) {
		this.resamplingMethodName = resamplingMethodName;
	}



	public String getClassifierName() {
		return classifierName;
	}



	public void setClassifierName(String classifierName) {
		this.classifierName = classifierName;
	}
}
