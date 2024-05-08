package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public class WebCrawler {

    private Queue<String> urlQueue = new LinkedList<>();
    String initialUrl;
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    int count = 0;

    public WebCrawler(String initialUrl) {
        this.initialUrl = initialUrl;
        urlQueue.add(initialUrl);
    }

    public void visitLinksRecursively(int numberOfVisits, String folderPath) {
        try {
            String link = urlQueue.remove();
            count++;
            numberOfVisits--;
            File file = new File(folderPath + "/" + count + ".html");
            System.out.println("Visiting URL " + link);
            URL url = new URL(link);
            InputStream inputStream = url.openStream();
            downloadFile(inputStream, file);
            String rawHTML = readFile(file);
            childUrls(rawHTML, numberOfVisits);

            if (!urlQueue.isEmpty() || (numberOfVisits > 0)) {
                visitLinksRecursively(numberOfVisits, folderPath);
            }

        } catch (IOException e) {
            System.out.println("Wrong URL, so skipped");
        }
    }

    private static void downloadFile(InputStream reader, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = reader.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }

    private String readFile(File file) {
        try (FileReader fileReader = new FileReader(file)) {
            StringBuilder content = new StringBuilder();
            int nextChar;
            while ((nextChar = fileReader.read()) != -1) {
                content.append((char) nextChar);
            }

            return String.valueOf(content);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int childUrls(String rawHtml, int numberOfVisits) {
        Document document = Jsoup.parse(rawHtml);
        Elements availableLinksOnPage = document.select("a[href]");
        for (Element ele : availableLinksOnPage) {
            String url = ele.attr("abs:href");

            if (urlQueue.size() > numberOfVisits) {
                break;
            }
            if (url != null && url.startsWith(initialUrl) && !url.endsWith(".zip")) {
                urlQueue.add(url);
            }
        }
        return numberOfVisits;
    }

}
