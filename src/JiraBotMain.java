import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.TrustingSSLSocketFactory;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

public class JiraBotMain {
	
	public static void main(String[] args) throws Exception {
		
		String address = "", username = "", password = "", channel = "", server = "";
		int port = 6667;
		if(args.length == 6) {
			try {
				address = args[0];
				username = args[1];
				password = args[2];
				channel = args[3];
				server = args[4];
				port = Integer.parseInt(args[5]);
				
				init(address, username, password, channel, server, port);
			} catch(Exception e) {
				System.out.println(e.getMessage());
				printHelp();
			}
		} else {
			printHelp();
		}       
    }

	private static void printHelp() {
		System.out.println("Arguments: jira_address jira_username jira_pass irc_channel irc_server irc_port");
	}

	private static void init(String address, String username, String password,
			String channel, String server, int port) throws URISyntaxException,
			IOException, IrcException, NickAlreadyInUseException, SSLException {
		final JiraRestClient restClient = setupJiraClient(new URI(address), username, password);
		
        JiraBot bot = new JiraBot(restClient);
        bot.setVerbose(true);
        bot.connect(server, port, new TrustingSSLSocketFactory());
        bot.joinChannel(channel);
	}

	private static JiraRestClient setupJiraClient(final URI address, final String username, final String password) throws URISyntaxException {
		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(address, username, password);
		return restClient;
	}
}
