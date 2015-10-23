import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
	static Socket s;
	static Database db;
	private static final String STARTING_URL = "www.straitstimes.com";
	private static LinkedList<String> URLtoVisit;
	private static LinkedList<String> URLVisitedBefore;

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		db = new Database();
		URLtoVisit = new LinkedList<String>();
		URLtoVisit.add(STARTING_URL);
		//URLVisitedBefore = db.getVisitedUrls();
		visitURLs();

	}

	private static void visitURLs() throws UnknownHostException, IOException {
		while (!URLtoVisit.isEmpty()) {
			String url = URLtoVisit.getFirst();
			File f;
			f = connect(url, url);//is hostname always == url?
			Document doc = Jsoup.parse(f, null, "");

			// Visit the url, add URLtoVisit, extract sentences from text.
			String text = doc.text();

			Elements links = doc.select("a[href]");
			for (Element link : links) {
				String urlLink = link.attr("abs:href");
				if (!urlLink.equals("")) {
					if(!URLVisitedBefore.contains((urlLink))){
							URLtoVisit.add(urlLink);
							db.insertUrl(urlLink); //update the db of newly visited url.
					}
				}
			}
			insertSentencestoDB(text);
			URLtoVisit.removeFirst();
			f.delete();
			visitURLs();
		}
	}

	public static void insertSentencestoDB(String text) {
		// dump all sentences to db?
		// search for vocab then insert?

	}

	public static File connect(String url, String host)
			throws UnknownHostException, IOException {
		s = new Socket(InetAddress.getByName(url), 80);
		PrintWriter pw = new PrintWriter(s.getOutputStream());
		pw.println("GET / HTTP/1.0");
		pw.println("Host: " + host);
		pw.println("");
		pw.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				s.getInputStream()));
		String t;
		File f = new File("page.html");
		FileWriter fr = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fr);
		while ((t = br.readLine()) != null) {
			bw.write(t);
		}
		bw.close();
		fr.close();
		br.close();
		return f;
	}

}
