package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import analysis.Analizer;
import analysis.Result;
import file_handler.CsvWriter;
import weka.core.Instances;

public class MainAnalizeDataset {
	Logger logger = Logger.getLogger(MainAnalizeDataset.class.getName());
	private static final String BOOKKEPER = "BOOKKEEPER"; // "SYNCOPE" 
	private static final String SYNCOPE = "SYNCOPE";
	
	
	private void analize(String projname) {
		Analizer analizer = new Analizer(projname);
		
		
		ArrayList<String> classifiers = new ArrayList<>(Arrays.asList("Random Forest", "Naive Bayes", "IBk"));
		ArrayList<String> resamplingMethods = new ArrayList<>(Arrays.asList("no resample", "Oversampling", "Undersampling", "Smote"));
		ArrayList<String> featureSelectionMethods = new ArrayList<>(Arrays.asList("no feature selection", "Best First"));
		
		
		try {
			//carico il file
			logger.log(Level.INFO,"Load dataset...");
			Instances data = analizer.loadFile();
			
			//elimino le colonne relative al nome della versione e al nome del file
			int index = data.attribute("versionName").index();
			data.deleteAttributeAt(index);
			index = data.attribute("filename").index();
			data.deleteAttributeAt(index);
			
			//recupero l'indice della classe "true"
			Enumeration<Object> values = data.attribute(data.numAttributes()-1).enumerateValues();
			int positiveResultIndex= 0;
			
			index = 0;
			while(values.hasMoreElements()) {
				Object v = values.nextElement();
				
				if (((String)v).equalsIgnoreCase("true")) {
					positiveResultIndex = index;
					break;
				} 
				index = index + 1;
			}
			
			//preparo walk forward
			int releasesNumber = analizer.getReleasesNumber(data);
			ArrayList<Result> resultList = new ArrayList<>();
			
			//per ogni classificatore, per ogni metodo di feature selection, per ogni metodo di balancing, per ogni iterazione di walk forward
			//mi salvo il risultato 
			logger.log(Level.INFO,"Computing results...");
			for(String classifierName: classifiers) {
				for(String featureSelectionName: featureSelectionMethods){
					for(String resamplingMethodName:resamplingMethods) 		{	
						for(int i = 2; i< releasesNumber+1; i++) {
							Instances newData = analizer.copyDataset(data);
							Result result = new Result(classifierName, featureSelectionName, resamplingMethodName);
							Instances[] trainTest = analizer.getTrainingTestSet(newData,i);
							analizer.runWalkForwardIteration(trainTest, result, positiveResultIndex, i);
							resultList.add(result);
						}
					}
				}
			}

			//scrivo i risultati in un csv
			logger.log(Level.INFO,"Writing CSV...");
			CsvWriter writer = new CsvWriter(projname);
			writer.writeResultCSV(resultList);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.log(Level.INFO,"CSV written successfully.\nEnd of the program.");
	}
	
	public static void main(String[] args) {
		MainAnalizeDataset main = new MainAnalizeDataset();
		main.analize(BOOKKEPER);
		main.analize(SYNCOPE);
	}
}
