import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Page {
	String host;
	String path;
	String url;
	Socket soc;
	Document doc;

	private static Pattern hostPathPattern = Pattern.compile("[htps]*://([\\w.]*)(/.*)");

	public Page(String url) throws MalformedURLException {
		doc = null;
		Matcher m = hostPathPattern.matcher(url);
		if (m.matches()) {
			System.out.println(m.group(1) + " " + m.group(2));
			host = m.group(1);
			path = m.group(2);
			this.url = url;
		} else {
			throw new MalformedURLException(url);
		}
	}

	public Document get() throws IOException {
		connect();
		sendRequest();
		doc = Jsoup.parse(soc.getInputStream(), null, host);
		return doc;
	}

	/**
	 * Extracts all the links from the page
	 * @return A list of links that can be found
	 * within the page
	 */
	public LinkedList<String> getLinks() {
		LinkedList<String> linkList = new LinkedList<String>();
		Elements links = doc.select("a");
		for (Element link : links) {
			String extractedURL = link.attr("abs:href");
			if (!extractedURL.equals("")) {
				linkList.add(extractedURL);
			}
		}
		return linkList;
	}

	/**
	 * Extracts all the sentences from the page
	 * @return A list of sentences that can be found
	 * within the page
	 */
	public LinkedList<String> getSentences() {
		LinkedList<String> senList = new LinkedList<String>();
		Elements paragraphs = doc.select("p");
		for (Element paragraph : paragraphs) {
			Paragraph p = new Paragraph(paragraph.text());
			while (p.hasNext()) {
				senList.add(p.next());
			}
		}
		return senList;
	}

	public String getPath() {
		return path;
	}

	public String getHost() {
		return host;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * Establishes the connection to the host via sockets.
	 * Uses any available port that the system can find
	 * @throws IOException
	 */
	private void connect() throws IOException {
		soc = new Socket(InetAddress.getByName(host), 0);
	}

	/**
	 * Forms the GET request to the web server and sends it
	 * @throws IOException
	 */
	private void sendRequest() throws IOException {
		PrintWriter pw = new PrintWriter(soc.getOutputStream());
		pw.println("GET " + path + " HTTP/1.1");
		pw.println("Host: " + host);
		pw.println("");
		pw.flush();
		pw.close();
	}
}
