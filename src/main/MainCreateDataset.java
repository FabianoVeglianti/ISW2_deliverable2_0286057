package main;



import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import bug_tools.Bug;
import bug_tools.ProportionMovingWindow;
import file_handler.CsvWriter;
import githubpkg.GitCommit;
import githubpkg.GitHubAPI;
import githubpkg.GitRelease;
import jirapkg.JiraAPI;
import jirapkg.ReleaseJira;
import jirapkg.TicketJira;

public class MainCreateDataset {


private static String[] a = {"BOOKKEEPER", "release-"}; // "SYNCOPE", "syncope-"

	
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(MainCreateDataset.class.getName());
		String projName = a[0];
		String prefix = a[1];
		
		logger.log(Level.INFO,"Initialization repository ...");
		JiraAPI jirapi= new JiraAPI(projName);
		githubpkg.GitHubAPI githubapi = new GitHubAPI(projName);
		CsvWriter csvWriter = new CsvWriter(projName, 0.5);
		githubapi.init();
		
		//ottieni le release
		logger.log(Level.INFO,"Retrieving releases information from Jira...");
		ArrayList<ReleaseJira> releases = (ArrayList<ReleaseJira>) jirapi.getReleases();

		
		//ottieni i tickets
		ArrayList<TicketJira> tickets = new ArrayList<>();
		ArrayList<Bug> bugs = new ArrayList<>();
		logger.log(Level.INFO,"Retrieving tickets info from Jira...");
		jirapi.getTicketsInfo(bugs, tickets, releases.get(releases.size()-1));

		for (TicketJira ticket:tickets) {
			
			ticket.fixData(releases);
			
		}
		//rimuove i bug inutilizzabili, vedere la descrizione del metodo per una spiegazione maggiore
		logger.log(Level.INFO,"Remove unusable bugs...");
		jirapi.removeUnusableBugs(bugs, tickets, releases);
		
		//ottieni i commits
		logger.log(Level.INFO,"Retrieving releases info from Github...");
		ArrayList<GitRelease> gitReleases = (ArrayList<GitRelease>) githubapi.getReleases();
		for (GitRelease release: gitReleases) {
			if(release.getName().startsWith(prefix)) {
				release.setName(release.getName().substring(prefix.length()));
			}
		}
		gitReleases = (ArrayList<GitRelease>) githubapi.fixGitReleaseList(gitReleases, releases);
		
		logger.log(Level.INFO,"Retrieving commits info from Jira...");
		ArrayList<GitCommit> commits = (ArrayList<GitCommit>) githubapi.getCommits(gitReleases);
		
		
		ProportionMovingWindow proportion = new ProportionMovingWindow(releases.size(), bugs.size());
		

		
		logger.log(Level.INFO,"Setting IV, OV and FV for each bug...");
		ArrayList<Bug> bugsWithNoCommit = new ArrayList<>();
		for(Bug bug:bugs) {

			bug.setCommitList(commits);
			
			// se bug.getLastCommit() == null allora significa che non c'è nemmeno un commit per quel bug
			if(bug.getLastCommit()==null) {
				bugsWithNoCommit.add(bug);
			} else {
			
			
				String ticketID = bug.getTicketID();
				TicketJira associatedTicket = TicketJira.searchTicketByID(tickets, ticketID);
				if (associatedTicket == null) {
					//non ci sono tickets dunque il programma termina qui
					logger.log(Level.INFO,"There are no tickets for this project.\nEnd of the program.");
				}
				
				bug.setVersions(associatedTicket, releases, proportion);
			}
		}
		
		
		
		for(Bug bugWithNoCommit:bugsWithNoCommit) {
			bugs.remove(bugWithNoCommit);
			
		}

		
		for (GitRelease release: gitReleases) {
			githubapi.setClassesRelease(release);
		} 

		githubapi.setRevisionsForRelease(gitReleases);
		logger.log(Level.INFO,"Computing measures and bugginess...");
		githubapi.setClassPerReleaseMeasures(gitReleases);
		
		githubapi.setBugginess(bugs, gitReleases);
		logger.log(Level.INFO,"Writing CSV...");
		try {
			csvWriter.writeDatasetCSV(gitReleases);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.log(Level.INFO,"CSV written successfully.\nEnd of the program.");

	}
	
	
	
}
