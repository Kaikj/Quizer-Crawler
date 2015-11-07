import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Crawler {
	private Database db;
	private LinkedList<String> queue;

	public static void main(String[] args) throws IOException {
		Database db = new Database();
		LinkedList<Thread> threadList = new LinkedList<Thread>();
		while (true) {
			// Remove dead threads for garbage collection
			for (Thread t : threadList) {
				if (!t.isAlive()) {
					threadList.remove(t);
				}
			}

			// Run the crawler on any new seeds
			LinkedList<String> seedURLS = db.getSeedUrls();
			for (String s : seedURLS) {
				// Simple way to do it to avoid extending Thread
				boolean contains = false;
				for (Thread t : threadList) {
					if (t.getName().equals(s)) {
						contains = true;
						break;
					}
				}

				// Spawn a new thread to handle the seed
				if (!contains) {
					Parallel p = new Parallel(s);
					Thread t = new Thread(p, s);
					t.start();
					threadList.add(t);
				}
			}

			// Wait for any one thread to finish
			// Continue on the outer loop to check
			// for new seeds if none of the threads
			// are done or have any error
			for (Thread t : threadList) {
				try {
					t.join(3600000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	static class Parallel implements Runnable {
		String base;
		public Parallel(String url) {
			base = url;
		}

		@Override
		public void run() {
			Crawler c = new Crawler(base);
			c.crawl();
		}
	}

	public Crawler(String base) {
		db = new Database();
		queue = new LinkedList<String>();
		queue.add(base);
	}

	/**
	 * The main powerhouse of the crawler. Everything
	 * including any heuristics is here!
	 */
	public void crawl() {
		// Just keep crawling. No end unless queue is empty
		while (!queue.isEmpty()) {
			// Great! The url to crawl!
			String url = queue.removeFirst();
			Page page;
			try {
				page = new Page(url);
				page.get();
				addToQueue(page.getLinks(), page, queue);

				// We only want real content, not anything
				// from the front page as there are mostly
				// only headlines there
				if (!page.getPath().equals("/")) {
					LinkedList<String> sentences = page.getSentences();
					for (String s : sentences) {
						String new_s = s.trim();
						StringTokenizer st = new StringTokenizer(new_s, " ", false);
						// We do not want a sentence that is too short or blacklisted.
						if (!new_s.isEmpty() && (st.countTokens() > 6)) {
							boolean blacklist = false;
							while(st.hasMoreTokens()){
								String word = st.nextToken();
								blacklist = blacklist || BlackList.isBlacklisted(word);
							}
							if(!blacklist){
								addSentence(new_s, url);
							}
						}
					}

					// We don't want the root page in the list
					// of visited url either. Then we can visit
					// them again for new content
					db.insertVisitedUrl(url);
					Thread.sleep(2000);
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

	/**
	 * Abstraction of checking for duplicate statements
	 * @param sentence The sentence to be added
	 * @param url The url that the sentence came from
	 */
	private void addSentence(String sentence, String url) {
		if (!db.checkIfSentenceExist(sentence)) {
			db.insertSentence(sentence, url);
		}
	}

	/**
	 * Checks for duplicate links and only adds unique links
	 * to the crawling queue
	 * @param links The link that we are adding
	 * @param page The page that the link came from
	 * @param queue The associated queue that we are adding
	 *              the link to
	 */
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
