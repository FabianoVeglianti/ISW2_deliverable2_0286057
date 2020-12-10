package analysis;

import java.io.File;
import java.io.IOException;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.supervised.instance.SMOTE;

public class Analizer {
	
	private String projName; 
	
	public Analizer(String projName) {
		this.projName = projName;
	}
	
	/**
	 * Carica il file
	 * */
	public Instances loadFile() throws IOException {
		CSVLoader loader = new CSVLoader();
    	loader.setSource(new File(projName+".csv"));
    	Instances data = loader.getDataSet();//get instances object
	
    	return data;
	}
	
	/**
	 * Ottiene una copia del dataset
	 * */
	public Instances copyDataset(Instances data) {
		return new Instances(data);
	}
	
	/**
	 * Effettua lo split del dataset in training e test set in funzione dell'indice della release da usare come test set.
	 * */
	private Instances[] getTrainingTestSet(Instances data, int testReleaseIndex) {
		Instances[] trainTest = new Instances[2];
		//mi preparo dei dataset vuoti con la stessa intestazione del dataset di partenza
		Instances trainingSet = new Instances(data,0);
		Instances testSet = new Instances(data,0);
		
		//per ogni istanza se l'id della release è precedente all'id della release da usare come test set allora
		//aggiungo quell'istanza al training set, altrimenti, se è uguale, l'aggiungo al test set
		int index = data.attribute("versionID").index();
		for(Instance instance:data) {
			if((int)instance.value(index)<testReleaseIndex) {
				trainingSet.add(instance);
			}else if((int)instance.value(index)==testReleaseIndex) {
				testSet.add(instance);
			}
		}
		
		trainTest[0] = trainingSet;
		trainTest[1] = testSet;
		return trainTest;
	}
	
	/**
	 * Ottiene il numero delle release - tale numero è il numero della release dell'ultima istanza del dataset
	 * */
	public int getReleasesNumber(Instances data) {
		Instance instance = data.lastInstance();
		int index = data.attribute("versionID").index();
		return (int)instance.value(index);
	}
	
	/**
	 * Esegue un'iterazione di Walk Forward
	 * */
	public void runWalkForwardIteration(Instances data, Result result, int positiveResultIndex, int iterationIndex) throws Exception {
		
		//ottiene lo split training test
		Instances[] trainTest = getTrainingTestSet(data,iterationIndex);
		Instances trainingSet = trainTest[0];
		Instances testSet = trainTest[1];
		
		//rimuove dal dataset la feature relativa all'id della release
		int index = trainingSet.attribute("versionID").index();
		trainingSet.deleteAttributeAt(index);
		testSet.deleteAttributeAt(index);
		
		//setta la feature da predirre
		int numAttr = trainingSet.numAttributes();
		trainingSet.setClassIndex(numAttr - 1);
		testSet.setClassIndex(numAttr - 1);
		
		//otteniamo gli oggetti da utilizzare in questa iterazione
		AbstractClassifier classifier = null;
		AttributeSelection featureSelection = null;
		Filter resamplingMethod = null;
		
		//otteniamo il classificatore
		switch(result.getClassifierName()) {
		case "Random Forest":
			System.out.println("Random Forest");
			classifier = new RandomForest();
			break;
		case "Naive Bayes":
			System.out.println("Naive Bayes");
			classifier = new NaiveBayes();
			break;
		case "IBk":
			System.out.println("IBk");
			classifier = new IBk();
			break;
		default:
		}
		
		//applichiamo il metodo di balancing
		switch(result.getResamplingMethodName()) {
		case "Undersampling":
			resamplingMethod = new SpreadSubsample();
			
			resamplingMethod.setInputFormat(trainingSet);
			
			String[] opts = new String[]{ "-M", "1.0"};
			resamplingMethod.setOptions(opts);
			
			trainingSet = Filter.useFilter(trainingSet, resamplingMethod);
	
			break;
		case "Oversampling":
			resamplingMethod = new Resample();

			resamplingMethod.setInputFormat(trainingSet);
			
			//mi calcolo la percentuale della classe maggioritaria
			int trainingSetSize = trainingSet.size();
			int numInstancesTrue = 0;
			int buggyIndex = trainingSet.classIndex();
			for(Instance instance:trainingSet) {
				if(instance.stringValue(buggyIndex).equalsIgnoreCase("true")) {
					numInstancesTrue = numInstancesTrue + 1;
				}
			}
			double percentageTrue = (double)(numInstancesTrue)/(double)(trainingSetSize)*100.0;
			double percentageMajorityClass = 0;
			if(percentageTrue > 50) {
				percentageMajorityClass = percentageTrue;
			} else {
				percentageMajorityClass = 100 - percentageTrue;
			}
			String doublePercentageMajorityClassString = String.valueOf(percentageMajorityClass*2);
			
			opts = new String[]{ "-B", "1.0", "-Z", doublePercentageMajorityClassString};
			resamplingMethod.setOptions(opts);
			
			trainingSet = Filter.useFilter(trainingSet, resamplingMethod);

			break;
		case "Smote":
			resamplingMethod = new SMOTE();
			
			double parameter = 0;
			numInstancesTrue = 0;
			int numInstancesFalse = 0;
			for (Instance instance: trainingSet) {
				if(instance.stringValue(trainingSet.classIndex()).equalsIgnoreCase("true")) {
					numInstancesTrue = numInstancesTrue +1;
				} else if (instance.stringValue(trainingSet.classIndex()).equalsIgnoreCase("false")) {
					numInstancesFalse = numInstancesFalse +1;
				}
			}
			if(numInstancesTrue < numInstancesFalse) {
				parameter = (double)((double)numInstancesFalse-(double)numInstancesTrue)/(double)numInstancesTrue*100.0;
			} else {
				parameter = (double)((double)numInstancesTrue-(double)numInstancesFalse)/(double)numInstancesFalse*100.0;
			}

			opts = new String[] {"-P", String.valueOf(parameter)};
			resamplingMethod.setOptions(opts);
			resamplingMethod.setInputFormat(trainingSet);
			trainingSet = Filter.useFilter(trainingSet, resamplingMethod);
			break;
		case "No resampling":
			break;
		default:
			break;
		}
		
		//otteniamo il metodo di feature selection
		switch(result.getFeatureSelectionName()) {
		case "No feature selection":
			break;
		case "Best First":
			//create AttributeSelection object
			featureSelection = new AttributeSelection();
			//create evaluator and search algorithm objects
			CfsSubsetEval eval = new CfsSubsetEval();
			GreedyStepwise search = new GreedyStepwise();
			//set the algorithm to search backward
			search.setSearchBackwards(true);
			//set the filter to use the evaluator and search algorithm
			featureSelection.setEvaluator(eval);
			featureSelection.setSearch(search);
			//specify the dataset
			featureSelection.setInputFormat(trainingSet);
			//apply
			trainingSet = Filter.useFilter(trainingSet, featureSelection);
			testSet = Filter.useFilter(testSet, featureSelection);
			int numAttrFiltered = trainingSet.numAttributes();
			trainingSet.setClassIndex(numAttrFiltered - 1);
			testSet.setClassIndex(numAttrFiltered - 1);
		}
		
		//salva le informazioni relative al numero di release in training e alla percentuale di bugginess nel training e nel test set
		result.setDatasetValues(trainingSet, testSet, iterationIndex);
	
		//addestro il classificatore ed effettuo la predizione
		classifier.buildClassifier(trainingSet);
		Evaluation eval = new Evaluation(testSet);
		eval.evaluateModel(classifier, testSet);
		
		//raccolgo i risultati
		double TP = eval.numTruePositives(positiveResultIndex);
		double FP = eval.numFalsePositives(positiveResultIndex);
		double TN = eval.numTrueNegatives(positiveResultIndex);
		double FN = eval.numFalseNegatives(positiveResultIndex);
		double precision = eval.precision(positiveResultIndex);
		double recall = eval.recall(positiveResultIndex);
		double auc = eval.areaUnderROC(positiveResultIndex);
		double kappa = eval.kappa();

		//salvo i risultati nell'oggetto result
		result.setValues(TP, FP, TN, FN, precision, recall, auc, kappa);
		System.out.println("Iterazione " + iterationIndex + "\nClassificatore "+ result.getClassifierName()+ "\n"
				+ "Resampling " + result.getResamplingMethodName()+ "\nFeature selection " + result.getFeatureSelectionName());
		System.out.println(result.toString()+"\n\n");

	}
	
/*
	private void temp() {
		
		Instances data;
		try {
			data = this.loadFile();
			int index = data.attribute("versionName").index();
			data.deleteAttributeAt(index);
			index = data.attribute("filename").index();
			data.deleteAttributeAt(index);
			
			Enumeration<Object> values = data.attribute(data.numAttributes()-1).enumerateValues();
			int positiveResultIndex = 0;
			index = 0;
			while(values.hasMoreElements()) {
				Object v = values.nextElement();
				System.out.println(v);
				
				if (((String)v).equalsIgnoreCase("true")) {
					positiveResultIndex = index;
				} else if (((String)v).equalsIgnoreCase("false")) {
					positiveResultIndex = index;
				}
				index = index + 1;
			}
			int releasesNumber = this.getReleasesNumber(data);
			ArrayList<Result> resultList = new ArrayList<Result>();
			for(int i = 2; i< releasesNumber+1; i++) {
				Instances newData = this.copyDataset(data);
				Result result = new Result("Random Forest", "no feature selection", "no resampling");
				Instances[] trainTest = getTrainingTestSet(newData,i);
				
				result.setDatasetValues(trainTest, i);
				
				Instances trainingSet = trainTest[0];
				Instances testSet = trainTest[1];
				System.out.println("train number" + trainingSet.numInstances() + " test number" + testSet.numInstances());
				
				index = trainingSet.attribute("versionID").index();
				trainingSet.deleteAttributeAt(index);
				testSet.deleteAttributeAt(index);
				
				int numAttr = trainingSet.numAttributes();
				trainingSet.setClassIndex(numAttr - 1);
				testSet.setClassIndex(numAttr - 1);
				
				//otteniamo gli oggetti da utilizzare in questa iterazione
				IBk classifier = new IBk();

			

				classifier.buildClassifier(trainingSet);
				Evaluation eval = new Evaluation(testSet);
				eval.evaluateModel(classifier, testSet);
				
				double TP = eval.numTruePositives(positiveResultIndex);
				double FP = eval.numFalsePositives(positiveResultIndex);
				double TN = eval.numTrueNegatives(positiveResultIndex);
				double FN = eval.numFalseNegatives(positiveResultIndex);
				double precision = eval.precision(positiveResultIndex);
				double recall = eval.recall(positiveResultIndex);
				double auc = eval.areaUnderROC(positiveResultIndex);
				double kappa = eval.kappa();

				
				result.setValues(TP, FP, TN, FN, precision, recall, auc, kappa);
				System.out.println("Iterazione " + i + "\nClassificatore "+ result.getClassifierName()+ "\n"
						+ "Resampling " + result.getResamplingMethodName()+ "\nFeature selection " + result.getFeatureSelectionName());
				System.out.println(result.toString()+"\n\n");
				resultList.add(result);
			}
			CsvWriter writer = new CsvWriter("BOOKKEEPER");
			writer.writeResultCSV(resultList);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
		Analizer analizer = new Analizer("BOOKKEEPER");
		analizer.temp();
	}
	*/
	
	
	
}
