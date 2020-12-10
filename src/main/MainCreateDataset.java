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

	private static String[] a ={"SYNCOPE", "syncope-"};
//	private static String[] a = {"BOOKKEEPER", "release-"};

	
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
		ArrayList<ReleaseJira> releases = jirapi.getReleases();
		
		for (ReleaseJira release:releases) {
			System.out.println(release.toString());
		}
		
		//ottieni i tickets
		ArrayList<TicketJira> tickets = new ArrayList<TicketJira>();
		ArrayList<Bug> bugs = new ArrayList<Bug>();
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
		ArrayList<GitRelease> gitReleases = githubapi.getReleases();
		for (GitRelease release: gitReleases) {
			if(release.getName().startsWith(prefix) || release.getName().startsWith(prefix)) {
				release.setName(release.getName().substring(prefix.length()));
			}
		}
		gitReleases = githubapi.fixGitReleaseList(gitReleases, releases);
		
		logger.log(Level.INFO,"Retrieving commits info from Jira...");
		ArrayList<GitCommit> commits = githubapi.getCommits2(gitReleases);
		
		
		ProportionMovingWindow proportion = new ProportionMovingWindow(releases.size(), bugs.size());
		
		//da cancellare
		int count = 0;
		//da cancellare 
		
		logger.log(Level.INFO,"Setting IV, OV and FV for each bug...");
		ArrayList<Bug> bugsWithNoCommit = new ArrayList<Bug>();
		for(Bug bug:bugs) {

			bug.setCommitList(commits);
			
			// se bug.getLastCommit() == null allora significa che non c'è nemmeno un commit per quel bug
			if(bug.getLastCommit()==null) {
				bugsWithNoCommit.add(bug);
				count = count + 1;
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
		
		// da cancellare
		int numBugsBeforeRemotion = bugs.size();
		// da cancellare fine
		
		for(Bug bugWithNoCommit:bugsWithNoCommit) {
			bugs.remove(bugWithNoCommit);
			
		}

		//da cancellare 
		System.out.println("#Bugs prima della rimozione = " + numBugsBeforeRemotion + "."
				+ " Count = " + count + ". #Bugs dopo rimozione = " + bugs.size());
		//da cancellare 
		
		

		

		
		
		
		
		System.out.println("\nReleaseJira");
		for(ReleaseJira releaseJira:releases) {
			System.out.println("ID=" + releaseJira.getID() + " - Nome="+ releaseJira.getName());
		}

		System.out.println("\nReleaseGit");
		for(GitRelease gitRelease:gitReleases) {
			System.out.println("ID=" + gitRelease.getID() + " - Nome="+ gitRelease.getName());
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.log(Level.INFO,"CSV written successfully.\nEnd of the program.");

	}
	
	
	
}
