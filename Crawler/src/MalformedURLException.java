public class MalformedURLException extends Exception {
	public MalformedURLException(String url) {
		super("Malformed URL detected: " + url);
	}
}
