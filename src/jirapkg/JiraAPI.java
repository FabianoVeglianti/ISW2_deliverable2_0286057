package jirapkg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bug_tools.Bug;
import date_tools.DateStringParser;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.ZoneId;




public class JiraAPI {
	private static final String RELEASED = "released";
	private static final String FIELDS = "fields";
	private static final String URL = "https://issues.apache.org/jira/rest/api/2/project/";
	private String projectName;

	public JiraAPI(String projectName) {
		this.projectName = projectName;
	}
	
	public List<ReleaseJira> getReleases(){
		// Fills the arraylist with releases dates and orders them
				// Ignores releases with missing dates
				// Adds an extra release called nextRelease
				

				ArrayList<ReleaseJira> releasesJira = new ArrayList<>();
				
				Integer i;
				String projectURL = URL + projectName;
				JSONArray versions = new JSONArray();
				try {
					JSONObject json = readJsonFromUrl(projectURL);
					versions = json.getJSONArray("versions");
				} catch (IOException | JSONException e) {
					e.printStackTrace();
				}

				for (i = 0; i < versions.length(); i++) {
					String name = "";

					try {
						if (versions.getJSONObject(i).has("releaseDate") && versions.getJSONObject(i).getBoolean(RELEASED)) {
							if (versions.getJSONObject(i).has("name"))
								name = versions.getJSONObject(i).get("name").toString();

							ReleaseJira release = new ReleaseJira();
							release.setName(name);
							String strDate = versions.getJSONObject(i).get("releaseDate").toString();
							LocalDate date = LocalDate.parse(strDate);
							LocalDateTime dateTime = date.atStartOfDay();
							release.setReleaseDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
							releasesJira.add(release);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				// order releases by date
				Collections.sort(releasesJira, new Comparator<ReleaseJira>() {
					// @Override
					public int compare(ReleaseJira r1, ReleaseJira r2) {
						return r1.getReleaseDate().compareTo(r2.getReleaseDate());
					}
				});
				
	
				
				int index = 1;
				for (int j = 0; j < releasesJira.size(); j++) {
					releasesJira.get(j).setID(index);
					index = index+1;
				}
				
				return releasesJira;
	}


	private JSONObject readJsonFromUrl(String projectURL) throws IOException, JSONException {
		InputStream is = new URL(projectURL).openStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String jsonText = readAll(rd);
		is.close();
		return new JSONObject(jsonText);
	}

	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public void getTicketsInfo(List<Bug> bugs, List<TicketJira> tickets, ReleaseJira lastRelease){

		JSONObject json = null;
		JSONArray issues = null;
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs
			// >1000
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + projectName
					+ "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
					+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,fixVersions,resolutiondate,versions,created&startAt="
					+ i.toString() + "&maxResults=" + j.toString();

			try {
				json = readJsonFromUrl(url);
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			
			
			issues = json.getJSONArray("issues");

			total = json.getInt("total");

			for (; i < total && i < j; i++) {
				// Iterate through each bug
				String key = null;
				ArrayList<String> versions = new ArrayList<>();
				ArrayList<String> fixVersions = new ArrayList<>();
//ricavare anche le altre informazioni e metterle in ticketJira - la classe ticket Jira mi serve solo come
//classe d'appoggio per i dati che poi userò nella classe bug
				
				JSONObject issue = issues.getJSONObject(i % 1000);
				key = issue.get("key").toString();
				JSONArray versionsInJSON = issue.getJSONObject(FIELDS).getJSONArray("versions");
				JSONArray fixVersionsInJSON =  issue.getJSONObject(FIELDS).getJSONArray("fixVersions");
				
				//get and parse resolution date and creation date
				String resolutionDateString = issue.getJSONObject(FIELDS).getString("resolutiondate");
				Date resolutionDate = DateStringParser.getDateFromString(resolutionDateString);  		
				String creationDateString = issue.getJSONObject(FIELDS).getString("created");
				Date creationDate = DateStringParser.getDateFromString(creationDateString);
						
				for (int k = 0; k<versionsInJSON.length(); k++) {
					JSONObject version = versionsInJSON.getJSONObject(k);
					if(version.getBoolean(RELEASED)) {
						versions.add(version.get("name").toString());
						
						
					}
				}
				for (int s = 0; s<fixVersionsInJSON.length();s++) {
					JSONObject fixVersion = fixVersionsInJSON.getJSONObject(s);
					if(fixVersion.getBoolean(RELEASED)) {
						fixVersions.add(fixVersion.get("name").toString());
					}
				}
				
				if( resolutionDate.before(lastRelease.getReleaseDate()) ) {
					TicketJira ticket = new TicketJira();
					ticket.setTicketID(key);
					ticket.setAffectedVersions(versions);
					ticket.setFixedVersions(fixVersions);
					ticket.setResolutionDate(resolutionDate);
					ticket.setCreationDate(creationDate);
				
					Bug bug = new Bug(key);
					
					tickets.add(ticket);
					bugs.add(bug);
				}
				

			}
		} while (i < total);
	
		TicketJira.reverseArrayList((ArrayList<TicketJira>) tickets);
		Bug.reverseArrayList(bugs);
		
	}
	
	/* Rimuove i dati relativi ai bug per cui OV == FV e contemporaneamente non si conosce l'IV.
	 * Tale rimozione è applicata anche quando i dati di jira sono incoerenti, segue la spiegazione.
	 * 
	 * Rimuove i dati relativi ai bug per cui OV > FV, in tal caso infatti non è possibile applicare proportion.
	 * 
	 * Rimuove i dati relativi ai bug per cui IV >= OV == FV infatti  
	 * -se IV == OV == FV allora i dati sono incoerenti perché IV sicuramente presenta il bug, la FV 
	 * sicuramente non presenta il bug e dunque IV non può essere uguale a FV, ma allora dovrei non 
	 * considerare attentibile l'IV presa da Jira, ma allora siamo nel caso in cui OV == FV e non si conosce
	 * l'IV.
	 * -se IV > OV allora i dati sono incoerenti, dunque siamo nel caso in cui OV == FV e non si conosce l'IV.
	 *
	 * Vengono mantenuti i dati dei bugs per cui OV != FV oppure OV == FV e IV < OV.
	 */
	public void removeUnusableBugs(List<Bug> bugs, List<TicketJira> tickets, List<ReleaseJira> releases) {
		ArrayList<Bug> toRemove = new ArrayList<>();
		Iterator<Bug> iterator = bugs.iterator();
		
		//per ogni bug setto OV e FV, quando OV==FV controllo se la lista delle AV non è vuota e in tal
		//caso se l'IV è precedente all'OV/FV, se si allora mantengo il bug, se no allora elimino il bug.
		
		
		while(iterator.hasNext()) {
			Bug bug = iterator.next();
			
			TicketJira ticket = TicketJira.searchTicketByID((ArrayList<TicketJira>) tickets, bug.getTicketID());

			bug.setFV(ticket.getFixedVersions().get(0));
			bug.setOpeningVersion(ticket, releases, ReleaseJira.getReleaseByName((ArrayList<ReleaseJira>) releases, bug.getFV()));
			
			
			
			
			if ( !(bug.getOV().equalsIgnoreCase(bug.getFV())) ) {
				//se OV != FV
				
				
			} else {
				//se OV == FV
				
				
				if(ticket.hasAV()) {
			
					
					//Jira ha dati sulle AV, ma devo vedere se l'IV è precedente alla OV
					String IV = ticket.getInjectedVersion((ArrayList<ReleaseJira>) releases);
					
					ReleaseJira IVrelease = ReleaseJira.getReleaseByName((ArrayList<ReleaseJira>) releases, IV);
					ReleaseJira OVrelease = ReleaseJira.getReleaseByName((ArrayList<ReleaseJira>) releases, bug.getOV());
					
					if(IVrelease.getReleaseDate().before(OVrelease.getReleaseDate())){
						//se IV < OV=FV allora mantengo il bug
						
						continue;
					
					} else {
						//se IV >= OV=FV allora elimino il bug perché è come se fossi nel caso
						//OV = FV e lista delle AV in jira non presente
						tickets.remove(ticket);
						toRemove.add(bug);
					}
					
				} else {
					//Jira non ha dati sull'AV del bug, quindi elimino il bug
					
					
					tickets.remove(ticket);
					toRemove.add(bug);
				}
						
			}
			
		}
		
		for (Bug bugToRemove: toRemove) {
			bugs.remove(bugToRemove);
		}
		
	}
	
}
