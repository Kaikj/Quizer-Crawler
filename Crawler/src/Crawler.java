import java.io.IOException;
import java.util.LinkedList;

public class Crawler {
	private Database db;
	private LinkedList<String> urlToVisit;

	public static void main(String[] args) throws IOException {
		Crawler c = new Crawler();
		c.crawl();
	}

	public Crawler() {
		db = new Database();
		urlToVisit = new LinkedList<String>();
		urlToVisit.addAll(db.getSeedUrls());
	}

	public void crawl() {
		// If there is nothing to crawl, restart from the
		// seed url. There might be new seeds
		if (urlToVisit.isEmpty()) {
			urlToVisit.addAll(db.getSeedUrls());
		}

		while (!urlToVisit.isEmpty()) {
			String url = urlToVisit.removeFirst();
			Page page;
			try {
				page = new Page(url);
				page.get();
				addToQueue(page.getLinks(), page);

				// We only want real content, not anything
				// from the front page as there are mostly
				// only headlines there
				if (!page.getPath().equals("/")) {
					LinkedList<String> sentences = page.getSentences();
					for (String s : sentences) {
						addSentence(s, url);
					}

					// We don't want the root page in the list
					// of visited url either. Then we can visit
					// them again for new content
					db.insertVisitedUrl(url);
				}
				Thread.sleep(1000);
			} catch (MalformedURLException e) {
				// If the url is bad, just skip it
				// and carry on. A few missing links won't hurt
				// continue;
			} catch (IOException e) {
				// Parsing failed. Don't care and move on
				// continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		crawl();
	}

	private void addSentence(String sentence, String url) {
		if (!db.checkIfSentenceExist(sentence)) {
			db.insertSentence(sentence, url);
		}
	}

	private void addToQueue(LinkedList<String> links, Page page) {
		for (String link : links) {
			if (urlToVisit.contains(link)) {
				// The queue already contains the link
				// continue;
			} else if (db.checkIfVisited(link)) {
				// We have already visited this link
				// continue;
			} else if (db.checkIfSeed(page.baseURI)) {
				// We only want to search within the domain
				urlToVisit.add(link);
			} else {
				// Nothing to do here
				// continue;
			}
		}
	}
}
