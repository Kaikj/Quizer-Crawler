import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

public class Crawler {
	private Database db;
	private HashMap<String, LinkedList<String>> urlQueue;

	public static void main(String[] args) throws IOException {
		Crawler c = new Crawler();
		c.crawl();
	}

	public Crawler() {
		db = new Database();
		urlQueue = new HashMap<String, LinkedList<String>>();
		LinkedList<String> urls = db.getSeedUrls();
		for (String s : urls) {
			LinkedList<String> ll = new LinkedList<String>();
			ll.add(s);
			urlQueue.put(s, ll);
		}
	}

	public void crawl() {
		// Just keep crawling. No end unless system interrupt
		while (true) {
			for (Map.Entry<String, LinkedList<String>> entry : urlQueue.entrySet()) {
				String seed = entry.getKey();
				LinkedList<String> urlToVisit = entry.getValue();
				// If there is nothing to crawl, restart from the
				// seed url. There might be new seeds
				if (urlToVisit.isEmpty()) {
					urlToVisit.add(seed);
					// Check for new seeds
					LinkedList<String> urls = db.getSeedUrls();
					for (String s: urls) {
						if (!urlQueue.containsKey(s)) {
							LinkedList<String> ll = new LinkedList<String>();
							ll.add(s);
							urlQueue.put(s, ll);
						}
					}
				}
				String url = urlToVisit.removeFirst();
				Page page;
				try {
					page = new Page(url);
					page.get();
					addToQueue(page.getLinks(), page, urlToVisit);

					// We only want real content, not anything
					// from the front page as there are mostly
					// only headlines there
					if (!page.getPath().equals("/")) {
						LinkedList<String> sentences = page.getSentences();
						for (String s : sentences) {
							String new_s = s.trim();
							StringTokenizer st = new StringTokenizer(new_s, " ", false);
							// We do not want a sentence that is too short
							if (!new_s.isEmpty() && (st.countTokens() > 6)) {
								addSentence(new_s, url);
							}
						}

						// We don't want the root page in the list
						// of visited url either. Then we can visit
						// them again for new content
						db.insertVisitedUrl(url);
					}
				} catch (MalformedURLException e) {
					// If the url is bad, just skip it
					// and carry on. A few missing links won't hurt
					// continue;
				} catch (IOException e) {
					// Parsing failed. Don't care and move on
					// continue;
				}
			}
		}
	}

	private void addSentence(String sentence, String url) {
		if (!db.checkIfSentenceExist(sentence)) {
			db.insertSentence(sentence, url);
		}
	}

	private void addToQueue(LinkedList<String> links, Page page, LinkedList<String> queue) {
		for (String link : links) {
			if ((!queue.contains(link)) &&
					(!db.checkIfVisited(link)) &&
					(db.checkIfSeed(page.baseURI))) {
				// We only want to search within the domain
				queue.add(link);
			}
		}
	}
}
