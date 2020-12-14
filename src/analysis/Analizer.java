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
	private static final String VERSIONID = "versionID";
	
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
    
    	return loader.getDataSet();//get instances object
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
	public Instances[] getTrainingTestSet(Instances data, int testReleaseIndex) {
		Instances[] trainTest = new Instances[2];
		//mi preparo dei dataset vuoti con la stessa intestazione del dataset di partenza
		Instances trainingSet = new Instances(data,0);
		Instances testSet = new Instances(data,0);
		
		//per ogni istanza se l'id della release è precedente all'id della release da usare come test set allora
		//aggiungo quell'istanza al training set, altrimenti, se è uguale, l'aggiungo al test set
		int index = data.attribute(VERSIONID).index();
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
		int index = data.attribute(VERSIONID).index();
		return (int)instance.value(index);
	}
	
	private int getNumInstancesTrue(Instances dataset) {
		int numInstancesTrue = 0;
		int buggyIndex = dataset.classIndex();
		for(Instance instance:dataset) {
			if(instance.stringValue(buggyIndex).equalsIgnoreCase("true")) {
				numInstancesTrue = numInstancesTrue + 1;
			}
		}
		return numInstancesTrue;
	}
	
	/**
	 * Esegue un'iterazione di Walk Forward
	 * */
	public void runWalkForwardIteration(Instances[] trainTest, Result result, int positiveResultIndex, int iterationIndex) {
		
		//ottiene lo split training test
		Instances trainingSet = trainTest[0];
		Instances testSet = trainTest[1];
		
		//rimuove dal dataset la feature relativa all'id della release
		int index = trainingSet.attribute(VERSIONID).index();
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
			classifier = new RandomForest();
			break;
		case "Naive Bayes":
			classifier = new NaiveBayes();
			break;
		case "IBk":
			classifier = new IBk();
			break;
		default:
		}
		
		//applichiamo il metodo di balancing
		try {
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
				int numInstancesTrue = getNumInstancesTrue(trainingSet);
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
				numInstancesTrue = getNumInstancesTrue(trainingSet);
				int numInstancesFalse = trainingSet.numInstances()-numInstancesTrue;
				if(numInstancesTrue < numInstancesFalse && numInstancesTrue != 0) {
					parameter = ((double)numInstancesFalse-(double)numInstancesTrue)/(double)numInstancesTrue*100.0;
				} else if (numInstancesTrue >= numInstancesFalse && numInstancesFalse != 0){
					parameter = ((double)numInstancesTrue-(double)numInstancesFalse)/(double)numInstancesFalse*100.0;
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
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
		//otteniamo il metodo di feature selection
			if(result.getFeatureSelectionName().equalsIgnoreCase("Best First")) {
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
			if(classifier != null)
				classifier.buildClassifier(trainingSet);
			
			Evaluation eval = new Evaluation(testSet);
			eval.evaluateModel(classifier, testSet);

			
			//salvo i risultati nell'oggetto result
			result.setValues(eval, positiveResultIndex);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
