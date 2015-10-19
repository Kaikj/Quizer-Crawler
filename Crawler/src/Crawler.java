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

public class Crawler {
	static Socket s;

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		File f;
		f = connect("www.straitstimes.com", "www.straitstimes.com");
		
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
