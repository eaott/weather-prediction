package weather.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

public class CleanupEmail {
	/**
	 * FRAK WINDOWS
	 * 
	 * So there are 710 emails in Handled. Some of those were not fully processed.
	 * That's about all I have.
	 */
	public static final int MAX_BATCH_SIZE = 100;
	public static void main(String[] args) throws Throwable{
		try(PrintWriter out = new PrintWriter("C:/Users/Evan/GitProjects/weather-prediction/data2/cleanup.txt")) {
			out.println("logging in");
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
			Folder inbox = store.getFolder("Handled");
			Folder handled = store.getFolder("Handled2");
			inbox.open(Folder.READ_WRITE);
			handled.open(Folder.READ_WRITE);
			out.println("logged in");
			System.out.println("logged in");
			int total_messages = inbox.getMessageCount();
			long time = System.currentTimeMillis();
			Message[] messages = null;
			if (total_messages < MAX_BATCH_SIZE)
				messages = inbox.getMessages();
			else
				messages = inbox.getMessages(1, MAX_BATCH_SIZE);
			
			out.println("messages received: " + messages.length);
			System.out.println("messages received: " + messages.length);
			
			double mm = 0;
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
					out.printf("processed message %s: %.2f%%\n",subj, ++mm/messages.length * 100);
					System.out.printf("processed message %s: %.2f%%\n",subj, mm/messages.length * 100);
				}
			}
			Message[] toMove = new Message[orders.size()];
			toMove = orders.toArray(toMove);
			inbox.copyMessages(toMove, handled);
			
			out.println("messages moved");
			System.out.println("messages moved");
			
			String htmlBase = "http://www1.ncdc.noaa.gov/pub/has/";
			String startPart = "<tr><td valign=\"top\"><img src=\"/icons/unknown.gif\" alt=\"[   ]\"></td><td><a href=\"";
			
			File directory = new File("C:/Users/Evan/Desktop/Thesis_NetCDF/");
			String[] currentFiles = directory.list();
			Set<String> currentFileSet = new HashSet<>();
			for (String c : currentFiles)
				currentFileSet.add(c);
			
			Set<String> pages = new HashSet<>();
			double p = 0;
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
						// If I've already downloaded this file, don't do it again.
						String filename = dataPart.substring(dataPart.length() - 19);
						if (currentFileSet.contains(filename))
							continue;
						pages.add(url + "/" + dataPart);
					}
				}
				out.printf("identified files for %s: %.2f%%\n", dir, ++p/orderNames.size() * 100);
				System.out.printf("identified files for %s: %.2f%%\n", dir, p/orderNames.size() * 100);
			}
			out.println("total files to download: " + pages.size());
			System.out.println("total files to download: " + pages.size());
			double i = 0;
			for (String page : pages)
			{
				boolean found = false;
				FileOutputStream fos = null;
				while (! found)
				{
					try {
						ReadableByteChannel rbc = Channels.newChannel(new URL(page).openStream());
						String filename = page.substring(page.length() - 19);
						fos = new FileOutputStream("C:/Users/Evan/Desktop/Thesis_NetCDF/" + filename);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						fos.close();
						out.printf("%s complete: %.2f%%\n", page, ++i/pages.size() * 100);
						System.out.printf("%s complete: %.2f%%\n", page, i/pages.size() * 100);
						found = true;
					}
					catch (java.net.ConnectException e)
					{
						if (fos != null)
							fos.close();
					}
				}
			}
			out.println((System.currentTimeMillis() - time) / 1000 + " seconds to complete.");
			System.out.println((System.currentTimeMillis() - time) / 1000 + " seconds to complete.");
		}
		catch(Throwable t)
		{
			
		}
	}

}
