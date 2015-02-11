package weather.data;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

public class ScanEmail {
	public static final int MAX_BATCH_SIZE = 500;
	public static void main(String[] args) throws Throwable{
		System.out.println("logging in");
		Properties props = System.getProperties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.user", "eaottweather");
		props.setProperty("mail.password", "flashflood");
		Session mailSession = Session.getDefaultInstance(props, null);
		
		Store store = mailSession.getStore("imaps");
		store.connect("imap.gmail.com", "eaottweather", "flashflood");
		Folder inbox = store.getFolder("Inbox");
		Folder handled = store.getFolder("Handled");
		inbox.open(Folder.READ_WRITE);
		handled.open(Folder.READ_WRITE);
		System.out.println("logged in");
		int total_messages = inbox.getMessageCount();
		
		Message[] messages = null;
		if (total_messages < MAX_BATCH_SIZE)
			messages = inbox.getMessages();
		else
			messages = inbox.getMessages(1, MAX_BATCH_SIZE);
		
		System.out.println("messages received: " + messages.length);
		
		Set<Message> orders = new HashSet<>();
		Set<String> orderNames = new HashSet<>();
		for (Message m : messages)
		{
			if (!m.getFlags().contains(Flags.Flag.SEEN) &&
					m.getSubject().startsWith("HAS Data Request:"))
			{
				m.setFlag(Flags.Flag.SEEN, true);
				m.setFlag(Flags.Flag.DELETED, true);
				String subj = m.getSubject();
				subj = subj.substring(subj.lastIndexOf("HAS"), subj.indexOf("Completed") - 1);
				orderNames.add(subj);
				orders.add(m);
				System.out.println("processed message " + subj);
			}
		}
		Message[] toMove = new Message[orders.size()];
		toMove = orders.toArray(toMove);
		inbox.copyMessages(toMove, handled);
		
		System.out.println("messages moved");
		
		String htmlBase = "http://www1.ncdc.noaa.gov/pub/has/";
		String startPart = "<tr><td valign=\"top\"><img src=\"/icons/unknown.gif\" alt=\"[   ]\"></td><td><a href=\"";
		
		Set<String> pages = new HashSet<>();
		for (String dir : orderNames)
		{
			String url = htmlBase + dir;
			URLConnection conn = new URL(url).openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String input = null;
			while ((input = in.readLine()) != null)
			{
				if (input.startsWith(startPart))
				{
					String dataPart = input.substring(startPart.length());
					dataPart = dataPart.substring(0, dataPart.indexOf("\""));
					pages.add(url + "/" + dataPart);
				}
			}
			System.out.println("identified files for " + dir);
		}
		
		double i = 0;
		for (String page : pages)
		{
			ReadableByteChannel rbc = Channels.newChannel(new URL(page).openStream());
			String filename = page.substring(page.length() - 19);
			FileOutputStream fos = new FileOutputStream("C:/Users/Evan/Desktop/Thesis_NetCDF/" + filename);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			System.out.printf("%s complete: %.2f%%\n", page, ++i/pages.size() * 100);
		}
	}

}
