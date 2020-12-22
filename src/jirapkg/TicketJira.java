package jirapkg;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import date_tools.DateCreator;

/**
 * Classe per mantenere le informazioni di interesse dei ticket presi da Jira
 * */
public class TicketJira {
	private Logger mylogger = Logger.getLogger(TicketJira.class.getName());
	private String ticketID;
	private ArrayList<String> affectedVersions;
	private ArrayList<String> fixedVersions;
	private Date creationDate;
	private Date resolutionDate;

	
	public String getTicketID() {
		return ticketID;
	}
	public void setTicketID(String ticketID) {
		this.ticketID = ticketID;
	}
	
	public List<String> getAffectedVersions() {
		return affectedVersions;
	}
	public void setAffectedVersions(List<String> affectedVersions) {
		this.affectedVersions = (ArrayList<String>) affectedVersions;
	}
	
	public List<String> getFixedVersions() {
		return fixedVersions;
	}
	public void setFixedVersions(List<String> fixedVersions) {
		this.fixedVersions = (ArrayList<String>) fixedVersions;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getResolutionDate() {
		return resolutionDate;
	}
	public void setResolutionDate(Date resolutionDate) {
		this.resolutionDate = resolutionDate;
	}
	

	public boolean hasAV() {
		return !this.affectedVersions.isEmpty();
	}
	
	public String toString() {
		String result;
		result = "TicketID: " + this.ticketID + "\n";
		result = result + "CreationDate: " + this.creationDate.toString() + "\n";
		result = result + "ResolutionDate: " + this.resolutionDate.toString() + "\n";
		result = result + "affectedVersions:\n";
		for (String affectedVersion:this.affectedVersions) {
			result = result.concat("\t" + affectedVersion + "\n");
		}
		result = result + "fixedVersions:\n";
		for (int i = 0; i < fixedVersions.size(); i++) {
			String fixedVersion = fixedVersions.get(i);
			if (i < fixedVersions.size()-1) {
				result = result.concat("\t" + fixedVersion + "\n");
			} else {
				result = result.concat("\t" + fixedVersion);
			}
		}
		return result;
	}
	
	public static void reverseArrayList(List<TicketJira> arraylist) {
		int size = arraylist.size();
		for (int i = 0; i<size/2; i++) {
			TicketJira temp = arraylist.get(i);
			arraylist.set(i, arraylist.get(size-1-i));
			arraylist.set(size-1-i, temp);
		}
	}
	
	/**
	 * Data la lista delle release, ritorna la release meno recente che sia contenuta nella variabile affectedVersions.
	 * */
	public String getInjectedVersion(List<ReleaseJira> releases) {
		HashMap<String, Date> mapVersionameDate= new HashMap<>();
		
		/*
		 * Per ogni affected version (release) ottenuta da Jira mi salvo la corrispondente data nella mappa
		 * */
		Iterator<String> iterator = affectedVersions.iterator();
		
		
		while(iterator.hasNext()) {
		String version = iterator.next();
			try {
				
				//vediamo se la  version considerata è presente nella lista delle release
				int index = -1;
				boolean contained = false;
				for (int i = 0; i < releases.size(); i++) {
					if (version.equalsIgnoreCase(releases.get(i).getName())) {
						index = i;
						contained = true;
						break;
					}
				}
				
				
				if(!contained) {
					//se non è presente significa che non conosco quella release
					throw new UnknownReleaseException("Release " + version + " non trovata!\n"
							+ "Questa release sarà ignorata.");
				} else {
					//se è presente mi salvo la sua data di rilascio nella mappa
					mapVersionameDate.put(version, releases.get(index).getReleaseDate());
				}
				
			} catch (UnknownReleaseException e) {
				
				/*
				 * Se la release è sconosciuta allora è successo qualcosa di inaspettato
				 * tipicamente un errore umano nell'inserimento dei dati.
				 */
				e.printStackTrace();
				mylogger.log(Level.WARNING, "I risultati ottenuti potrebbero non essere esatti!");

				 
			}
		}
		
		//prendo la versione più vecchia contenuta in mapVersionameDate
		String injectedVersion = "";
		Date oldDate = DateCreator.getOldDate();
		for(Entry<String,Date> mapVersionameDateEntry: mapVersionameDate.entrySet()) {
			String key = mapVersionameDateEntry.getKey();
			Date versionDate = mapVersionameDateEntry.getValue();
			if(versionDate.after(oldDate)) {
				oldDate = versionDate;
				injectedVersion = key;
			}
		}
		
		return injectedVersion;
		
		
	}
	
	/**
	 * Data la lista delle release, aggiunge la prima versione successiva (in ordine cronologico) alla data di risoluzione del ticket.
	 * */
	private void fixDataFixVersionEmpty(List<ReleaseJira> releases) {
		if(resolutionDate.compareTo(releases.get(0).getReleaseDate()) <=0) {
			fixedVersions.add(releases.get(0).getName());
		} else {
		 
			for (int i = 0; i<releases.size()-1; i++) {
				ReleaseJira prevRelease = releases.get(i);
				ReleaseJira nextRelease = releases.get(i+1);
				
				if (resolutionDate.after(prevRelease.getReleaseDate()) && (resolutionDate.compareTo(nextRelease.getReleaseDate()) <=0) ) {
					fixedVersions.add(nextRelease.getName());
				}
			}
		}
	}
	
	
	private void createMapFixedVersionNameDate(List<ReleaseJira> releases, Map<String,Date> mapVersionameDate, String fixedVersionName) {
		try {
			
			//vediamo se la fixed version considerata è presente nella lista delle release

			ReleaseJira releaseJira = ReleaseJira.getReleaseByName(releases, fixedVersionName);
					
			if(releaseJira == null) {
				//se non è presente significa che non conosco quella release
				throw new UnknownReleaseException("Release " + fixedVersionName + " non trovata!");
			} else {
				//se è presente mi salvo la sua data di rilascio nella mappa
				mapVersionameDate.put(fixedVersionName, releaseJira.getReleaseDate());
			}
			
		} catch (UnknownReleaseException e) {
			
			/*
			 * Se la release è sconosciuta allora uso resolutionDate per determinare la FV
			 * Quindi svuoto l'arraylist fixedVersions e cerco la prima release successiva a 
			 * resolutionDate
			 * In fase di ottenimento dei dati si sono già scartati i bug per cui resolutionDate
			 * è successiva all'ultima release
			 * */
			for (int i = 0; i < fixedVersions.size(); i++) {
				fixedVersions.remove(0);
			}
			
			for (int i = 0; i<releases.size()-1; i++) {
				ReleaseJira prevRelease = releases.get(i);
				ReleaseJira nextRelease = releases.get(i+1);
				
				if (resolutionDate.after(prevRelease.getReleaseDate()) && (resolutionDate.compareTo(nextRelease.getReleaseDate()) <=0) ) {
					fixedVersions.add(nextRelease.getName());
					return;
				}
			}
		}
	}
	
	/**
	 * Alcuni dati sono incompleti o incoerenti: ad esempio potrebbero esserci 0 o più di 2 FV
	 * a partire dalla data di risoluzione del ticket si correggono questi dati
	 * nel primo caso (numero di fixed versions = 0) si setta come FV la prima release successiva alla data
	 * di risoluzione del ticket
	 * nel secondo caso (numero di fixed versions >=2) si setta come FV la più vecchia tra queste release
	 * */
	public void fixData(List<ReleaseJira> releases) {

		
		if (fixedVersions.isEmpty()) {
			
			fixDataFixVersionEmpty(releases);
		} else {
			
			HashMap<String, Date> mapVersionameDate= new HashMap<>();
			
			/*
			 * Per ogni fixed version (release) ottenuta da Jira mi salvo la corrispondente data nella mappa
			 * */
			Iterator<String> iterator = fixedVersions.iterator();
		
			while(iterator.hasNext()) {
				String fixedVersionName = iterator.next();
				createMapFixedVersionNameDate(releases, mapVersionameDate, fixedVersionName);
				
			}
			
			//prendo la versione più vecchia contenuta in mapVersionameDate
			String fixedVersion = "";
			Date oldDate = DateCreator.getOldDate();
			for(Entry<String,Date> mapVersionameDateEntry: mapVersionameDate.entrySet()) {
				String key = mapVersionameDateEntry.getKey();
				Date versionDate = mapVersionameDate.get(key);
				if(versionDate.after(oldDate)) {
					oldDate = versionDate;
					fixedVersion = key;
				}
			}
			
			//svuoto la lista delle fixed versions
			for (int i = 0; i < fixedVersions.size(); i++) {
				fixedVersions.remove(0);
			}
			
			//metto nella lista l'unica fixed version, cioè la più vecchia tra quelle che erano presenti
			fixedVersions.add(fixedVersion);
	
		}
	}
	
	public static TicketJira searchTicketByID(List<TicketJira> tickets, String ticketID) {
		if (!tickets.isEmpty()) {
			
			for (TicketJira ticket:tickets) {
				if(ticket.getTicketID().equalsIgnoreCase(ticketID)) {
					return ticket;
				}
			}
			
			
		}
		
		return null;
		
	}
	
}
