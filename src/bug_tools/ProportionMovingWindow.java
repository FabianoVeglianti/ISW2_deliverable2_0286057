package bug_tools;

public class ProportionMovingWindow {
	boolean isWindowFull;
	private int windowSize;
	private double[] windowP;
	private int index;
	//dato che il 51% dei difetti non ha AV o ha AV non affidabile mantenere la media delle P nella
	//finestra aggiornata significherebbe farlo mediamente 1 difetto su 2 e sprecare memoria
	//di contro non mantenerlo significa calcolarlo mediamente 1 volta su 2, ma non sprecare memoria,
	//quindi non lo mantengo
	
	//da cancellare inizio
	private int contatore = 0;
	
	public int getContatore() {
		return this.contatore;
	}
	
	public void setContatore(int num) {
		this.contatore = num;
	}
	//da cancellare fine
	
	public ProportionMovingWindow(int nReleases,int nBugFix) {
		windowSize = (int)(nBugFix * 0.01);

		windowP = new double[windowSize];
		index = 0;
		isWindowFull = false;
		
		//la maggior parte dei difetti dorme per il 19% delle releases, dunque imposto come valore iniziale
		//di P nReleases*0.19
		windowP[index] = (double)nReleases * 0.19;

		index = -1;
		
	}
	
	
	/**
	 * Aggiorna la finestra: scorre avanti di una posizione e sostituisce il valore contenuto in questa 
	 * posizione con il valore di P per il bug corrente
	 * */
	public void updateWindow(int ivIndex, int ovIndex, int fvIndex) {
		index = (index+1)%windowSize;
		if (fvIndex - ovIndex != 0) {
			double currentP = ((double)(fvIndex - ivIndex))/((double)(fvIndex - ovIndex));
			windowP[index] = currentP;
		} else {
			double currentP = 1;
			windowP[index] = currentP;
		}
		
		if(index==windowSize-1) {
			isWindowFull = true;
		}
		
	}
	
	/**
	 * Restituisce l'indice della IV predetto a partire dall'indice della OV e della FV
	 * */
	public int predictIVindex(int ovIndex, int fvIndex) {
		double p = 0;
		
		if (isWindowFull) {
			for (int i = 0; i < windowSize; i++) {
				p = p + windowP[i];
			}
			p = p/(windowSize);
		} else {
			for (int i = 0; i < index + 1; i++) {
				p = p + windowP[i];
			}
			p = p/(index + 2);
		}

		double doublePredictedIVindex = (double)fvIndex - p * (double)(fvIndex - ovIndex);
		int predictedIVindex = (int) Math.round(doublePredictedIVindex);

		
		if(predictedIVindex < 1) {
			//se la predizione mi porterebbe prima della prima release allora impongo che il bug esiste dalla
			//prima release
			predictedIVindex = 1;
		}
		return predictedIVindex;
	}
}
