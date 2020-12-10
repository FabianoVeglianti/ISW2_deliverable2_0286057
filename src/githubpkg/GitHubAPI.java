package githubpkg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
//import java.util.logging.Logger;
import java.util.TreeSet;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import bug_tools.Bug;
import bug_tools.Diff;
import datapkg.ClassProject;
import jirapkg.ReleaseJira;
import measurements.Measure;



public class GitHubAPI {

//	private static Logger myLogger = Logger.getLogger("InfoLogging");
	private String projNameMin;
	private final String url = "https://github.com/apache/";
	private Git git;
	private String repoLocalPath;
	
	public GitHubAPI(String projectName) {
		this.projNameMin = projectName.toLowerCase();
		this.repoLocalPath = "./"+projNameMin+"Repo";
	}
	
	private boolean isEmpty(Path path) {
	    if (Files.isDirectory(path)) {
	        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
	            return !directory.iterator().hasNext();
	        } catch(IOException e) {
	        	e.printStackTrace();
	        }
	    }
	    return false;
	}
	
	public void init() {
		String uri = url + projNameMin + ".git";
		try {
			
			if (!Files.exists(Paths.get(repoLocalPath)) || this.isEmpty(Paths.get(repoLocalPath))) {
				git = Git.cloneRepository().setURI(uri).setDirectory(new File(repoLocalPath)).call();
			} else {
				git = Git.open(new File(repoLocalPath));
				git.checkout().setName(this.getDefaultBranchName()).call();
				git.pull().call();
			}
		
		} catch (InvalidRemoteException e) {
			System.out.println("e1");
			e.printStackTrace();
		} catch (TransportException e) {
			System.out.println("e2");
			e.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("e3");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getDefaultBranchName() {
		try {
		    List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();
			for (Ref branch: branches) {
				String branchName = branch.getName();
				System.out.println(branchName);
				if (branchName.startsWith("refs/heads/")) {
					int startIndex = "refs/heads/".length();
					System.out.println("Default branch name: " + branchName);
					return branchName.substring(startIndex);
				}
			}
			
	    } catch (GitAPIException e) {
	    	e.printStackTrace();
	    	System.exit(0);
	    }
		return "";
	}
	
	
	public ArrayList<GitCommit> getCommits(){
		ArrayList<GitCommit> commits = new ArrayList<GitCommit>();  
	    Iterable<RevCommit> iterableCommits = null;
	 
	    try {
			git.checkout().setName(this.getDefaultBranchName()).call();
			iterableCommits = git.log().call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		} 

	    for (RevCommit commit: iterableCommits) {
	    	ObjectId parentID;
	    	
	    	if (commit.getParentCount() == 0) {
	    		parentID = null;
	    	} else {
	    		parentID = commit.getParent(0).getId();
	    	}
	    	ObjectId commitID = commit.getId();
	    	Date date = new Date(commit.getCommitTime() *1000L);
	    	String message = commit.getFullMessage();
	    	
	    	GitCommit gitCommit = new  GitCommit(commitID, date, parentID, message);
	    	commits.add(0, gitCommit);
	    }

	    return commits;
	}
	
	public ArrayList<GitCommit> getCommits2(ArrayList<GitRelease> releases){
	ArrayList<GitCommit> commits = new ArrayList<GitCommit>();  
		
	for(int i = 0; i < releases.size(); i ++) {
		if (i==0) {
			commits.addAll(this.getRevisionsBetweenTwoRelease(null, releases.get(i).getReleaseID()));
		} else {
			commits.addAll(this.getRevisionsBetweenTwoRelease(releases.get(i-1).getReleaseID(), releases.get(i).getReleaseID()));
		}
	}
	
	    return commits;
	}
	
	
	public ArrayList<GitRelease> getReleases() {
		
		ArrayList<GitRelease> releases = new ArrayList<GitRelease>();
		
		try {
			//ottiene la lista dei tag
			//https://www.html.it/pag/55386/tag-e-alias-in-git/#:~:text=Sostanzialmente%20in%20Git%20con%20il,versione%20%E2%80%9C1.0%E2%80%9D%20di%20un'
			List<Ref> tagList = git.tagList().call();
		
			//ottiene un walk sul grafo dei commit
			RevWalk walk = new RevWalk(this.git.getRepository());
			
			for (Ref tag: tagList) {
				//i tag sono del formato /ref/tags/releasename
				String tagName = tag.getName();
				String releaseName = tagName.substring("/ref/tags/".length());
				RevCommit commit = null;
				
				try {
					commit = walk.parseCommit(tag.getObjectId());
				} catch (MissingObjectException e) {
					e.printStackTrace();
				} catch (IncorrectObjectTypeException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//da cancellare inizio
				Date date = new Date(commit.getCommitTime() *1000L);
				System.out.println(date.toString() + " " + releaseName);
				//da cancellare fine
				
				GitRelease release = new GitRelease();
				release.setReleaseID(commit.getId());
				release.setName(releaseName);
				release.setDate(date);
				releases.add(release);
			}
			walk.close();
		
			return releases;
		
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		
		return releases;
		
	}
	
	/**La lista delle release in Git è più grande della lista delle release in Jira
	 * dunque per ogni release presa da Jira ci teniamo la corrispondente release presa da Git*/
	public ArrayList<GitRelease> fixGitReleaseList(ArrayList<GitRelease> gitReleases, ArrayList<ReleaseJira> jiraReleases) {
	//	ReleaseJira.reverseArrayList(jiraReleases);
		ArrayList<GitRelease> releases = new ArrayList<GitRelease>();
		
		for (ReleaseJira releaseJira:jiraReleases) {
			
			Iterator<GitRelease> iterator = gitReleases.iterator();
			
			while(iterator.hasNext()) {
				GitRelease releaseGit = iterator.next();
				if(!releaseGit.getName().contains("RC") && !releaseGit.getName().contains("tag") && !releaseGit.getName().contains("SNAPSHOT")
						&& !releaseGit.getName().contains("docker")) {
					int index = releaseGit.getName().indexOf(releaseJira.getName());

					if (index != -1) {
						if((releaseGit.getName().length()==releaseJira.getName().length()) && releaseGit.getName().contains(releaseJira.getName())) {
							releaseGit.setID(releaseJira.getID());
							releases.add(releaseGit);
							iterator.remove();
						}
					}
				}
				
			}
		}
	//	ReleaseJira.reverseArrayList(jiraReleases);
		
		
	//	GitRelease.reverseArrayList(releases);
		return releases;
	}
	
	
	
	/**Data una release setta la lista dei file .java appartenenti a questa release*/
	public void setClassesRelease(GitRelease release){
		
		//aggiungere questo attributo alla classe git release
		//forse anziché stringa è meglio definire una classe in modo da mantenere gli attributi
		//nome, buggy, misure (LOC,age,...)
		
		try {
			RevWalk revWalk = new RevWalk(this.git.getRepository()); 
			TreeWalk treeWalk = new TreeWalk(this.git.getRepository()); 
			
			ObjectId commitId = ObjectId.fromString(release.getReleaseID().getName());
			RevCommit commit = revWalk.parseCommit(commitId);
			ObjectId treeId = commit.getTree();

			treeWalk.reset(treeId);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String path = treeWalk.getPathString();
			    if (path.contains(".java")) {
			    	ClassProject classProject = new ClassProject(path, release.getName(), true);
			    
			    	//setta la size della classe
			    	ObjectId objectId = treeWalk.getObjectId(0);
			    	ObjectReader reader = this.git.getRepository().newObjectReader();
			    	byte[] data = reader.open(objectId).getBytes();
		            String content = new String(data, "utf-8");
		            classProject.getMeasure().setSizeFromContent(content);
		            
		            
		           release.addClassToClassList(classProject);
			    }
			    
			}
			if(release.getClasses().size() == 0) {
				System.out.println("Per la release " + release.getName() + " non si trovano classi.");
			} else {
				System.out.println("Per la release " + release.getName() + " si hanno "+ release.getClasses().size() + " classi.");
			}
			revWalk.dispose();
		} catch (IOException e ) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	private Diff parseDiffEntry(DiffEntry diffEntry, String diffText) {
		
		Diff diff = new Diff(diffEntry.getNewPath(), diffEntry.getOldPath());
		
		int addedLines = 0;
		int deletedLines = 0;
		String pointer = diffText;
		// conto le linee aggiunte "+" e le linee eliminate "-" effettuando il parsing del diffText
		//cerco "@@" mi sposto alla fine della riga e cerco tutti i "+" e i "-" fino alla prossima "@@"
		//o alla fine del file
		int newChangeIndex = pointer.indexOf("@@");
		while (newChangeIndex != -1) {
			
			pointer = pointer.substring(newChangeIndex+"@@".length());
			int indexNewLine = pointer.indexOf("\n");
			pointer = pointer.substring(indexNewLine+1);
			int checkIndex = pointer.indexOf("@@");
			
			while(checkIndex != 0 && pointer.length() > 0) {
				if(pointer.charAt(0) == '+')
					addedLines = addedLines +1;
				else if(pointer.charAt(0) == '-')
					deletedLines = deletedLines +1;
				indexNewLine = pointer.indexOf("\n");
				pointer = pointer.substring(indexNewLine+1);	
			
				checkIndex = pointer.indexOf("@@");
			}
			newChangeIndex = pointer.indexOf("@@");
		}

		
		diff.setAddedLines(addedLines);
		diff.setDeletedLines(deletedLines);	
		
		return diff;
	}
	
	private ArrayList<Diff> getClassChanges(Bug bug) {
		//effettua il diff tra il commit per fixare il bug e il commit precedente, cerca dunque i file .java
		//modificati, per ogni versione nella lista delle AV del bug, imposto a 1 la bugginess delle classi
		//modificate che ricavo dal diff.
		
		GitCommit commit = bug.getLastCommit();
		
		ArrayList<Diff> diffList = new ArrayList<Diff>();
		
		try {
			DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
			diffFormatter.setRepository(git.getRepository());
			diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
			diffFormatter.setDetectRenames(true);
			
			List<DiffEntry> diffEntries = diffFormatter.scan(commit.getParentID(), commit.getCommitID());
			diffFormatter.flush();
			diffFormatter.close();
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    DiffFormatter formatter = new DiffFormatter(outputStream);
		    formatter.setRepository(git.getRepository());
		    formatter.setDiffComparator(RawTextComparator.DEFAULT);
		    formatter.setDetectRenames(true);
			
			for (DiffEntry diffEntry:diffEntries) {
				

				if (diffEntry.getChangeType() == DiffEntry.ChangeType.MODIFY) {

					formatter.format(diffEntry);
			        String diffText = outputStream.toString();
			        
			        if( diffEntry.getOldPath().contains(".java") && diffEntry.getNewPath().contains(".java")) {
			        	Diff diff = parseDiffEntry(diffEntry, diffText);
	
			        	diffList.add(diff);
					}
				} 
		        outputStream.reset();
			}
			
			formatter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return diffList;
			
	
	}
/*	
	public HashMap<Bug, Diff> addDiffForBug(ArrayList<Bug> bugs, ArrayList<GitRelease> releases){
		HashMap<Bug, Diff> bugDiffMap = new HashMap<Bug, Diff>();
		
		for (Bug bug: bugs) {
			
			ArrayList<Diff> diffList = getClassChanges(bug);
			ArrayList<String> AV = bug.getAV();
			
			for (String affectedVersionName : AV) {
				
				GitRelease affectedVersion = GitRelease.getReleaseByName(releases, affectedVersionName);
//				System.out.println("Versione: " + affectedVersionName);
				ArrayList<ClassProject> classes = affectedVersion.getClasses();
						
				for (Diff diff: diffList) {
					String className = diff.getOldPath();
					ClassProject classProject = ClassProject.getClassProjectByName(classes, className);
//					System.out.println("AV: " + affectedVersion.getName() + "\nClasse: " + className);
					
					if (classProject != null) {			
						classProject.setDiff(diff);
						classProject.setBugginess(true);
						classProject.getMeasure().increaseBugFixes();
					}
				}
				
			}
			
		}

		
		
		return bugDiffMap;
	}
*/	
	
	public void setRevisionsForRelease(ArrayList<GitCommit> commits, ArrayList<GitRelease> releases) {
		//aggiunge ad ogni release una lista ordinata di revisioni (commit) tra quella release e 
		//la release precedente
		
		//ordino i commit per data
		Collections.sort(commits, new Comparator<GitCommit>() {
			// @Override
			public int compare(GitCommit c1, GitCommit c2) {
				return c1.getDate().compareTo(c2.getDate());
			}
		});
		
		int index = 0;
		for(GitRelease release:releases) {
			
			ArrayList<GitCommit> revisions = new ArrayList<GitCommit>();
			
			while(commits.get(index).getDate().compareTo(release.getDate())<=0) {
				revisions.add(commits.get(index));
				index = index +1;
			}
			
			release.setRevisions(revisions);
		}
		
		
	}
	
	
	public ArrayList<GitCommit> getRevisionsBetweenTwoRelease(ObjectId startRelease, ObjectId endRelease) {
		
		LogCommand logCommand = this.git.log();

		Iterable<RevCommit> commits = null;
		try {
			if(startRelease == null)
				logCommand = logCommand.add(endRelease);
			else 
				logCommand = logCommand.addRange(startRelease, endRelease);
			commits = logCommand.call();
			
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<GitCommit> commitList = new ArrayList<GitCommit>();
		for (RevCommit commit : commits) {
			
			ObjectId commitID = commit.getId();
	    	Date date = new Date(commit.getCommitTime() *1000L);
	    	String message = commit.getFullMessage();
	    	ObjectId parentID;
	    	
	    	if (commit.getParentCount() == 0)
	    		parentID = null;
	    	else {
	    		parentID = commit.getParent(0).getId();
	    	}
	    	GitCommit gitCommit = new  GitCommit(commitID, date, parentID, message);
	    	commitList.add(0,gitCommit);
		}
		System.out.println("numero revisioni: " + commitList.size());
		return commitList;

	}
	
	
	public void setRevisionsForRelease(ArrayList<GitRelease> releases) {
			
		for(int i = 0; i < releases.size(); i ++) {
			if (i==0) {
				releases.get(i).setRevisions(this.getRevisionsBetweenTwoRelease(null, releases.get(i).getReleaseID()));
			} else {
				releases.get(i).setRevisions(this.getRevisionsBetweenTwoRelease(releases.get(i-1).getReleaseID(), releases.get(i).getReleaseID()));
			}
		}
		
	}
		
	public void setBugginess(ArrayList<Bug> bugs, ArrayList<GitRelease> releases) {
		
		for (Bug bug: bugs) {	
			
			ArrayList<Diff> diffList = getClassChanges(bug);
			ArrayList<String> AV = bug.getAV();
			
			/*
			 * Per ogni diff inizio cercando il nome del file modificato nel diff nell'ultima AV
			 * Se lo trovo mi salvo tutti i nomi assunti dallo stesso file durante la storia del progetto
			 * il motivo è che nelle AVs precedenti all'ultima AV considerata il file potrebbe avere un
			 * nome diverso da quello contenuto nel diff.
			 * Dunque quando vado indietro con le AVs se non trovo la classe cerco i vecchi nomi della
			 * stessa.
			 * */
			for(Diff diff: diffList) {
				String className = diff.getOldPath();
				TreeSet<String> classNameHistory = new TreeSet<String>();
				for(int i = AV.size()-1; i>=0; i--) {
					
					String affectedVersionName = AV.get(i);
					GitRelease affectedVersion = GitRelease.getReleaseByName(releases, affectedVersionName);

					ClassProject classProject = affectedVersion.getClassByName(className);
					
					if (classProject != null) {		
	
						classProject.setBugginess(true);
						classProject.getMeasure().increaseBugFixes();
						ClassProject oldClassProject = classProject.getOldClassProject();
						
						while(oldClassProject != null) {
							if(classNameHistory.contains(oldClassProject.getThisName()) || oldClassProject.getThisName().equalsIgnoreCase(className)){
								break;
							}
							classNameHistory.add(oldClassProject.getThisName());
							oldClassProject = oldClassProject.getOldClassProject();
						}
						
					} 

					
					for(String name: classNameHistory) {
						ClassProject oldClassProject = affectedVersion.getClassByName(name);
						if (oldClassProject != null) {			
							oldClassProject.setBugginess(true);
							oldClassProject.getMeasure().increaseBugFixes();
						}
					}
				}
				
				
				
				
			}

		}
	}
		
		
	private void analyzeDiffEntries(GitCommit revision, List<DiffEntry> diffEntries, GitRelease release) {
		
	
		for(DiffEntry diffEntry:diffEntries) {
			switch(diffEntry.getChangeType()) {
	    	case ADD:
				if(diffEntry.getNewPath().contains(".java")) {
					ClassProject classProject = release.getClassByName(diffEntry.getNewPath());
					if(classProject == null) {
						classProject = new ClassProject(diffEntry.getNewPath(), release.getName(), false);
						classProject.setDateCreation(revision.getDate());
						release.addClassToClassList(classProject);

					} else {
						if(classProject.getDateCreation() == null) {
							classProject.setDateCreation(revision.getDate());
						} else {
							if(revision.getDate().before(classProject.getDateCreation())) {
								classProject.setDateCreation(revision.getDate());
							}
						}
					}
				}
				break;
	    	case DELETE:
				if(diffEntry.getOldPath().contains(".java")) {
					ClassProject classProject = release.getClassByName(diffEntry.getOldPath());
					if(classProject == null) {
			//			System.out.println("Eliminazione di una classe inesistente");
					} else {
						classProject.setDeleted(true);
					}
				}
				break;
	    	case RENAME:
				if(diffEntry.getNewPath().contains(".java") && diffEntry.getOldPath().contains(".java")) {
					ClassProject classProject = release.getClassByName(diffEntry.getOldPath());
					if(classProject == null) {
		//				System.out.println("Rinominazione di una classe inesistente");
					} else {
						ClassProject newClassProject = classProject.renameThisClass(diffEntry.getNewPath());
						release.addClassToClassList(newClassProject);
					}
				}
				break;
	    	case COPY:
	    		if(diffEntry.getNewPath().contains(".java") && diffEntry.getOldPath().contains(".java")) {
					ClassProject classProject = release.getClassByName(diffEntry.getOldPath());
					if(classProject == null) {
		//				System.out.println("Copia di una classe inesistente");
					} else {
						ClassProject newClassProject = classProject.copyThisClass(diffEntry.getNewPath());
						release.addClassToClassList(newClassProject);
					}
				}
				break;
	    	case MODIFY:
	    		if( diffEntry.getOldPath().contains(".java") ) {
		    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    		
		    		DiffFormatter formatter = new DiffFormatter(outputStream);
		            formatter.setRepository(this.git.getRepository());
		            try { 
		            	formatter.format(diffEntry);
		            	String diffText = outputStream.toString();
		
			        	Diff diff = parseDiffEntry(diffEntry, diffText);
			
			        	ClassProject classProject = release.getClassByName(diffEntry.getNewPath());
			        	if(classProject == null) {
			//        		System.out.println("Modifica di una classe inesistente");
			        	}else {
				        	Measure classProjectMeasure = classProject.getMeasure();
				        	classProjectMeasure.setMeasuresPerRelease(diff);
						}
		        
		            } catch(Exception e ) {
		            	e.printStackTrace();
		            } finally {
		            	formatter.close();
		            }
	    		}
		        break;
		        
			default:
				
	    	}
		
		}
	
	}
	
	private void prepareNextReleaseFiles(GitRelease release, GitRelease nextRelease) {
		
		for(ClassProject classProject: release.getClasses().values()) {
			if(classProject.getDeleted() == false) {
				ClassProject classProjectForNextRelease = classProject.copyThisClass(classProject.getThisName());
				classProjectForNextRelease.resetInterReleasesMetrics();
				nextRelease.addOrReplaceClassInClassList(classProjectForNextRelease);
			}
		}
	}
	
	
	public void setClassPerReleaseMeasures(ArrayList<GitRelease> releases) {
		
		
		for(int x = 0; x < releases.size(); x++) {
			GitRelease release = releases.get(x);
			
			
			//setta i valori delle misure "per versione"
			//setta a valori provvisori le misure "storico"
			ArrayList<GitCommit> revisions = release.getRevisions();
			//	System.out.println(release.getDate());
			//per ogni revisione facciamo il diff con la revisione precedente
			for (GitCommit revision: revisions) {
				
				ObjectId revisionId = revision.getCommitID();
				ObjectId prevRevisionId = revision.getParentID();
				
			    DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
				diffFormatter.setRepository(git.getRepository());
				diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
				diffFormatter.setDetectRenames(true);
				
				if(prevRevisionId == null) {
					continue;
				} else {
	
				}
				
				try {
					
					List<DiffEntry> diffEntries = diffFormatter.scan(prevRevisionId, revisionId);
					analyzeDiffEntries(revision, diffEntries, release);
					
				
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				diffFormatter.close();
				
			}
			
			
			release.setAgeOfClasses();
			
			if(x+1 < releases.size()) {
				prepareNextReleaseFiles(release, releases.get(x+1));

			}
		}
		
		
		
	}
	
	

	
}
