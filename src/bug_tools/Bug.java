package bug_tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import date_tools.DateCreator;
import githubpkg.GitCommit;
import jirapkg.ReleaseJira;
import jirapkg.TicketJira;


public class Bug {
	private static final String ERRORMESSAGE = " ERRORE FATALE\nIl programma verrà chiuso.";
	private Logger mylogger = Logger.getLogger(Bug.class.getName());

	private String ticketID;
	private ArrayList<GitCommit> commitList;
	private String iv;
	private String ov;
	private String fv;
	private ArrayList<String> av;
	
	public List<String> getAV(){
		return av;
	}
	
	public Bug(String ticketID) {
		this.setTicketID(ticketID);
		commitList = new ArrayList<>();
		av = new ArrayList<>();
	}

	public String getTicketID() {
		return ticketID;
	}
	
	public void setIV(String iv) {
		this.iv = iv;
	}
	
	public void setOV(String ov) {
		this.ov = ov;
	}
	
	public void setFV(String fv) {
		this.fv = fv;
	}
	
	public String getIV() {
		return this.iv;
	}
	
	public String getOV() {
		return this.ov;
	}
	
	public String getFV() {
		return this.fv;
	}

	public void setTicketID(String ticketID) {
		this.ticketID = ticketID;
	}
	
	private void addGitCommitToCommitList(GitCommit commit) {
		commitList.add(commit);
	}
	
	public void setCommitList(List<GitCommit> commitList) {
		boolean match = false;
		//associa ad un bug la lista dei commit in cui compare il ticket relativo a quel bug
		for(GitCommit commit: commitList) {
		
			String myTicketID = this.getTicketID();
			match = commit.hasTicketID(myTicketID);
			
			
			if (match) {
				this.addGitCommitToCommitList(commit);
				break;
			} 

		}

	}
	
	public static void reverseArrayList(List<Bug> arraylist) {
		int size = arraylist.size();
		for (int i = 0; i<size/2; i++) {
			Bug temp = arraylist.get(i);
			arraylist.set(i, arraylist.get(size-1-i));
			arraylist.set(size-1-i, temp);
		}
	}
	
	
	public GitCommit getLastCommit() {
		Date oldDate = DateCreator.getOldDate();
		GitCommit lastCommit = null;
		for (GitCommit commit: commitList) {
			if (commit.getDate().after(oldDate)) {
				oldDate = commit.getDate();
				lastCommit = commit;
			}
		}
		return lastCommit;
	}


	public void setOpeningVersion(TicketJira ticket, List<ReleaseJira> releases, ReleaseJira fv) {
		
		Date creationDate = ticket.getCreationDate();
		
		String openingVersionName = "";
		ReleaseJira openingVersion = null;
		Date futureDate = DateCreator.getFutureDate();
		for (ReleaseJira release: releases) {
			//cond1 considera solo le release (non strettamente) successive alla data di creazione del ticket
			//cond2 permette di aggiornare il risultato alla più vecchia release che soddisfa cond1
			boolean cond1 = creationDate.compareTo(release.getReleaseDate()) <= 0;
			boolean cond2 = release.getReleaseDate().before(futureDate);
			if(cond1 && cond2) {
				openingVersion = release;
				futureDate = release.getReleaseDate();
				openingVersionName = release.getName();
				
			}
			
		}
		try {
			if(openingVersion == null) {
				throw new Exception("Opening Version null");
			} else if(openingVersion.getReleaseDate().after(fv.getReleaseDate())) {
				ov = fv.getName();
			} else {
				ov = openingVersionName;
			}
		} catch (Exception e) {
			mylogger.log(Level.SEVERE, "Opening Version null");
			System.exit(0);
		}
	}
	
	
	/*
	 * I dati di jira sono ok se IV < OV quando OV = FV, perché IV non può essere uguale a FV
	 * oppure se IV <= OV se OV < FV
	 * */
	private boolean jiraDataOk(ArrayList<ReleaseJira> releases) {
	
		ReleaseJira ivRelease = ReleaseJira.getReleaseByName(releases, iv);
		ReleaseJira ovRelease = ReleaseJira.getReleaseByName(releases, ov);
		ReleaseJira fvRelease = ReleaseJira.getReleaseByName(releases, fv);
		

		if(ov.equalsIgnoreCase(fv)) {
			// OV = FV
			
			//true se IV < OV = FV - false se IV = OV = FV oppure IV > OV = FV
			return ivRelease.getReleaseDate().before(ovRelease.getReleaseDate());
			
			
		} else {
			// OV != FV
			
			
			if( ovRelease.getReleaseDate().after(fvRelease.getReleaseDate()) ){
				// OV > FV
				String msg = this.getTicketID() + ERRORMESSAGE;
				mylogger.log(Level.SEVERE, msg);
				System.exit(0);
			}
			
			// OV < FV => true if IV <= OV else false
			return !(ivRelease.getReleaseDate().after(ovRelease.getReleaseDate()));
			
		}
	
	}
	
	//l'OV e la FV sono settate, calcola IV partendo dai dati in Jira
	//aggiorna la finestra di proportion
	private void setVersionsFromJiraData(TicketJira ticket, ArrayList<ReleaseJira> releases, ProportionMovingWindow proportion) {
		
		int ivIndex = 0;
		int ovIndex = 0;
		int fvIndex = 0;
		int index = 1;
		for (ReleaseJira release: releases) {
			if(release.getName().equalsIgnoreCase(iv)) {
				ivIndex = release.getID();
			}
			if(release.getName().equalsIgnoreCase(ov)) {
				ovIndex = release.getID();
			}
			if(release.getName().equalsIgnoreCase(fv)) {
				fvIndex = release.getID();
				break;
			}
			index = index + 1;
		}
		
		if (ovIndex > fvIndex) {
			String msg = ticket.getTicketID() + ERRORMESSAGE;
			mylogger.log(Level.SEVERE, msg);
			System.exit(0);
		}
		
		
		for (int i = ivIndex; i< fvIndex; i++) {
			av.add(ReleaseJira.getReleaseByID(releases, i).getName());
		}
		
		
		if(ovIndex != fvIndex) {
			proportion.updateWindow(ivIndex, ovIndex, fvIndex);
		}
		
	}
	
	//l'OV e la FV sono settate: applica proportion per calcolare l' IV
	private void setVersionsUsingProportion(TicketJira ticket, ArrayList<ReleaseJira> releases, ProportionMovingWindow proportion) {
		
		int ovIndex = 0;
		int fvIndex = 0;
		int index = 1;
		for (ReleaseJira release: releases) {
			if(release.getName().equalsIgnoreCase(ov)) {
				ovIndex = index;
			}
			if(release.getName().equalsIgnoreCase(fv)) {
				fvIndex = index;
				break;
			}
			index = index + 1;
		}

		
		if (ovIndex > fvIndex) {
			String msg = ticket.getTicketID() + ERRORMESSAGE;
			mylogger.log(Level.SEVERE, msg);
			System.exit(0);
		}
		
		int ivIndex = proportion.predictIVindex(ovIndex, fvIndex);
	
		iv = releases.get(ivIndex).getName();
		
		for (int i = ivIndex; i< fvIndex; i++) {
			av.add(releases.get(i).getName());
		}
		
	}
	
	public void setVersions(TicketJira ticket, List<ReleaseJira> releases, ProportionMovingWindow proportion) {
		
		
		if(ticket.hasAV()) {
			//se su jira sono presenti AV
			
			iv = ticket.getInjectedVersion((ArrayList<ReleaseJira>) releases);
			
			boolean result = this.jiraDataOk((ArrayList<ReleaseJira>) releases);

		
			if(result) {
				//se l'IV è prima della OV, cioè i dati in jira sono coerenti, allora li uso
				this.setVersionsFromJiraData(ticket, (ArrayList<ReleaseJira>) releases, proportion);
			
			} else {
				//se l'IV è uguale o successiva alla OV allora devo usare proportion
				this.setVersionsUsingProportion(ticket, (ArrayList<ReleaseJira>) releases, proportion);
			
			}
		
		} else {
			//se su jira non sono presenti AV devo usare proportion
			this.setVersionsUsingProportion(ticket, (ArrayList<ReleaseJira>) releases, proportion);
			
		}
				
	}
	
	
}
