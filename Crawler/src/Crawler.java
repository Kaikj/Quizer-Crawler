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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
    static Socket s;
    static String TESTSTRING = "posses";

    public static void main(String[] args) throws UnknownHostException, IOException {
        Database database = new Database();

        File f;
        f = connect("www.straitstimes.com", "www.straitstimes.com");
        Document doc = Jsoup.parse(f, null, "");
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
        }
    }

    public static File connect(String url, String host) throws UnknownHostException, IOException {
        s = new Socket(InetAddress.getByName(url), 80);
        PrintWriter pw = new PrintWriter(s.getOutputStream());
        pw.println("GET / HTTP/1.0");
        pw.println("Host: " + host);
        pw.println("");
        pw.flush();
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String t;
        File f = new File("page.html");
        FileWriter fr = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fr);
        while ((t = br.readLine()) != null) {
            //System.out.println(t);
            bw.write(t);
        }
        bw.close();
        fr.close();
        br.close();
        return f;
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
    }
}
