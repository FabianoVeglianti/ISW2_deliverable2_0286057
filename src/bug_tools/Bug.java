package bug_tools;

import java.util.ArrayList;
import java.util.Date;

import date_tools.DateCreator;
import githubpkg.GitCommit;
import jirapkg.ReleaseJira;
import jirapkg.TicketJira;


public class Bug {

	private String ticketID;
	private ArrayList<GitCommit> commitList;
	private String IV;
	private String OV;
	private String FV;
	private ArrayList<String> AV;
	
	public ArrayList<String> getAV(){
		return AV;
	}
	
	public Bug(String ticketID) {
		this.setTicketID(ticketID);
		commitList = new ArrayList<GitCommit>();
		AV = new ArrayList<String>();
	}

	public String getTicketID() {
		return ticketID;
	}
	
	public void setIV(String IV) {
		this.IV = IV;
	}
	
	public void setOV(String OV) {
		this.OV = OV;
	}
	
	public void setFV(String FV) {
		this.FV = FV;
	}
	
	public String getIV() {
		return this.IV;
	}
	
	public String getOV() {
		return this.OV;
	}
	
	public String getFV() {
		return this.FV;
	}

	public void setTicketID(String ticketID) {
		this.ticketID = ticketID;
	}
	
	private void addGitCommitToCommitList(GitCommit commit) {
		commitList.add(commit);
	}
	
	public void setCommitList(ArrayList<GitCommit> commitList) {
		boolean match = false;
		//associa ad un bug la lista dei commit in cui compare il ticket relativo a quel bug
		for(GitCommit commit: commitList) {
		
			String ticketID = this.getTicketID();
			match = commit.hasTicketID(ticketID);
			
			
			if (match) {
				this.addGitCommitToCommitList(commit);
				break;
			} 

		}
		
		if (!match) {
			System.out.println(ticketID + " " + match);
		}
		
		
	}
	
	public static void reverseArrayList(ArrayList<Bug> arraylist) {
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


	public void setOpeningVersion(TicketJira ticket, ArrayList<ReleaseJira> releases, ReleaseJira FV) {
		
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
		
		if(openingVersion.getReleaseDate().after(FV.getReleaseDate())) {
			OV = FV.getName();
		} else {
			OV = openingVersionName;
		}
	}
	
	
	/*
	 * I dati di jira sono ok se IV < OV quando OV = FV, perché IV non può essere uguale a FV
	 * oppure se IV <= OV se OV < FV
	 * */
	private boolean jiraDataOk(ArrayList<ReleaseJira> releases) {
	
		ReleaseJira IVrelease = ReleaseJira.getReleaseByName(releases, IV);
		ReleaseJira OVrelease = ReleaseJira.getReleaseByName(releases, OV);
		ReleaseJira FVrelease = ReleaseJira.getReleaseByName(releases, FV);
		

		if(OV.equalsIgnoreCase(FV)) {
			// OV = FV
			
			if(IVrelease.getReleaseDate().before(OVrelease.getReleaseDate())) {
				//IV < OV = FV
				return true;
			} else {
				//IV = OV = FV oppure IV > OV = FV
				return false;
			}
			
		} else {
			// OV != FV
			
			
			if( OVrelease.getReleaseDate().after(FVrelease.getReleaseDate()) ){
				// OV > FV
				System.out.println("OV: "+ OVrelease.getName() + " FV: " + FVrelease.getName());
				System.out.println("OV: "+ OVrelease.getReleaseDate().toString() + " FV: " + FVrelease.getReleaseDate().toString());
				System.out.println(this.getTicketID() + " ERRORE FATALE");
				System.exit(0);
			}
			
			// OV < FV => true if IV <= OV else false
			if( !(IVrelease.getReleaseDate().after(OVrelease.getReleaseDate())) ){
				//IV <= OV
				return true;
			} else {
				//IV > OV
				return false;
			}
			
		}
	
	}
	
	//l'OV e la FV sono settate, calcola IV partendo dai dati in Jira
	//aggiorna la finestra di proportion
	private void setVersionsFromJiraData(TicketJira ticket, ArrayList<ReleaseJira> releases, ProportionMovingWindow proportion) {
		
		int IVindex = 0;
		int OVindex = 0;
		int FVindex = 0;
		int index = 1;
		for (ReleaseJira release: releases) {
			if(release.getName().equalsIgnoreCase(IV)) {
				IVindex = release.getID();
			}
			if(release.getName().equalsIgnoreCase(OV)) {
				OVindex = release.getID();
			}
			if(release.getName().equalsIgnoreCase(FV)) {
				FVindex = release.getID();
				break;
			}
			index = index + 1;
		}
		
		if (OVindex > FVindex) {
			System.out.println(ticket.getTicketID() + " ERRORE FATALE");
			System.exit(0);
		}
		
		
		for (int i = IVindex; i< FVindex; i++) {
			AV.add(ReleaseJira.getReleaseByID(releases, i).getName());
		}
		
		
		if(OVindex != FVindex) {
			proportion.updateWindow(IVindex, OVindex, FVindex);
		}
		
	}
	
	//l'OV e la FV sono settate: applica proportion per calcolare l' IV
	private void setVersionsUsingProportion(TicketJira ticket, ArrayList<ReleaseJira> releases, ProportionMovingWindow proportion) {
		
		int OVindex = 0;
		int FVindex = 0;
		int index = 1;
		for (ReleaseJira release: releases) {
			if(release.getName().equalsIgnoreCase(OV)) {
				OVindex = index;
			}
			if(release.getName().equalsIgnoreCase(FV)) {
				FVindex = index;
				break;
			}
			index = index + 1;
		}

		
		if (OVindex > FVindex) {
			System.out.println(ticket.getTicketID() + " ERRORE FATALE");
			System.exit(0);
		}
		
		int IVindex = proportion.predictIVindex(OVindex, FVindex);
	
		IV = releases.get(IVindex).getName();
		
		for (int i = IVindex; i< FVindex; i++) {
			AV.add(releases.get(i).getName());
		}
		
	}
	
	public void setVersions(TicketJira ticket, ArrayList<ReleaseJira> releases, ProportionMovingWindow proportion) {
		
		System.out.println(ticket.getTicketID() +" " + OV + " " + FV + " " + ticket.getCreationDate().toString());
		
		if(ticket.hasAV()) {
			//se su jira sono presenti AV
			
			IV = ticket.getInjectedVersion(releases);
			
			boolean result = this.jiraDataOk(releases);
			System.out.println(result);
		
			if(result) {
				//se l'IV è prima della OV, cioè i dati in jira sono coerenti, allora li uso
				this.setVersionsFromJiraData(ticket, releases, proportion);
			
			} else {
				//se l'IV è uguale o successiva alla OV allora devo usare proportion
				this.setVersionsUsingProportion(ticket, releases, proportion);
			
			}
		
		} else {
			//se su jira non sono presenti AV devo usare proportion
			this.setVersionsUsingProportion(ticket, releases, proportion);
			
		}
				
	}
	
	
}
