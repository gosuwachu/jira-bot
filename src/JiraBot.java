import java.util.Iterator;
import java.util.Scanner;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;


public class JiraBot extends PircBot {
	private static final String MESSAGE_SEPARATOR = "------------------------------------------------------------";
	private JiraRestClient jiraClient;
	final NullProgressMonitor pm = new NullProgressMonitor();
	
	public JiraBot(JiraRestClient client) {
		jiraClient = client;
		this.setName("JiraBot");
	}
	
	public void onMessage(String channel, String sender,
            String login, String hostname, String message) {
		String[] tokens = message.split(" ");
		if(tokens.length > 0) {
			if(tokens.length > 1) {
				try {
				Issue issue = jiraClient.getIssueClient().getIssue(tokens[1], pm);
					if(issue != null) {
						if(tokens[0].equals("!summary")) {
							sendMessage(channel, issue.getSummary()); 
						} else if(tokens[0].equals("!status")) {
							sendMessage(channel, issue.getStatus().getName());
						} else if(tokens[0].equals("!url")) {
							sendMessage(channel, issue.getSelf().toString());
						} else if(tokens[0].equals("!desc")) {
							String desc = issue.getField("description").getValue().toString();
							sendLongMessage(channel, desc);
						} else if(tokens[0].equals("!field") && tokens.length > 2) {
							Field field = issue.getField(tokens[2]);
							if(field != null && field.getValue() != null) {
								sendLongMessage(channel, field.getValue().toString());
							} else {
								sendMessage(channel, format(Colors.RED, "Error: ") + tokens[2] + " field not found");
							}
						} else if(tokens[0].equals("!assignee")) {
							sendMessage(channel, issue.getAssignee().getDisplayName());
						} else if(tokens[0].equals("!reporter")) {
							sendMessage(channel, issue.getReporter().getDisplayName());
						} else if(tokens[0].equals("!issue")) {
							String reply = MESSAGE_SEPARATOR;
							reply += format(Colors.BOLD, "Summary: ") + issue.getSummary() + "\n";
							reply += format(Colors.BOLD, "Status: ") + issue.getStatus().getName() + "\n";
							reply += format(Colors.BOLD, "Reporter: ") + issue.getReporter().getDisplayName() 
								+ format(Colors.BOLD, " Assignee: ") + issue.getAssignee().getDisplayName() + "\n";
							reply += format(Colors.BOLD, "Description: ") + issue.getField("description").getValue().toString() + "\n";
							
							Iterator<Comment> iterator = issue.getComments().iterator();
							Comment lastComment = null;
							while(iterator.hasNext()) {
								Comment comment = iterator.next();
								if(lastComment != null && 
										comment.getCreationDate().compareTo(
												lastComment.getCreationDate()) > 0) {
									lastComment = comment;
								}
							}
							if(lastComment != null) {
								reply += format(Colors.BOLD, "Last comment: ") + "(" + lastComment.getAuthor().getDisplayName() + ")" 
									+ lastComment.getBody() + "\n";
							} else {
								reply += "Last comment: no comments yet\n";
							}
							
							reply += MESSAGE_SEPARATOR;
							
							sendLongMessage(channel, reply);
						}
					} else {
						sendMessage(channel, "Error: Issue " + tokens[1] + " not found");
					}
				} catch(RestClientException e) {
					sendMessage(channel, format(Colors.RED, "Error: ") + e.getMessage());
				}
			} else if(tokens[0].equals("!about")) {
				sendMessage(channel, "I am Jira Bot :-)");
			} else if(tokens[0].equals("!welcome")) {
				User[] users = getUsers(channel);
				String reply = "Hi ";
				for(int i = 0; i < users.length; ++i) {
					String[] colors = {
							Colors.GREEN,
							Colors.BLUE,
							Colors.YELLOW,
							Colors.RED,
							Colors.BOLD,
							Colors.OLIVE,
							Colors.REVERSE,
							Colors.PURPLE,
							Colors.UNDERLINE
					};
					
					reply += format(colors[i % colors.length], users[i].getNick());
					if(i < users.length - 1) {
						reply += ", ";
					}
				}
				reply += "! xD";
				sendMessage(channel, reply);
			} else if(tokens[0].equals("!help")) {
				sendMessage(channel, "Commands: !about, !help, !desc, !summary, !issue, !reporter, !assignee, !status, !url, !field");
			}
		}
	}
	
	private String format(final String formatting, final String message) {
		return formatting + message + Colors.NORMAL;
	}
	
	private void sendLongMessage(final String channel, final String message) {
		Scanner scanner = new Scanner(message);
		while(scanner.hasNextLine()) {
			sendMessage(channel, scanner.nextLine());
		}
	}
}
