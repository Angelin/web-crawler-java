package com.app;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {

    private static Map<String, Integer> urlListToBeVisited = new ConcurrentHashMap<String, Integer>();
    private static FileWriter fileWriter;
    private static PrintWriter printWriter;

    public static void visitDomain(String givenStartDomain) {
        try {
            fileWriter = new FileWriter("matched-urls.txt");
            fileWriter.write(""); //emptying the file
            printWriter = new PrintWriter(fileWriter);
            urlListToBeVisited.put(givenStartDomain,0);
            visitAndPrintURLs(givenStartDomain);
            printWriter.close();
            fileWriter.close();
        } catch (IOException ie) {

        }
    }

    /*
    This method will iterative through the urlListToBeVisited, containing the urls to be visited with value 0.
    url visited will be updated with value as 1.
    - parallelStream() is being used for parallel execution
    */
    public static void visitAndPrintURLs(String givenStartDomain) {

        urlListToBeVisited.entrySet().parallelStream().filter(e -> e.getValue() == 0).map(Map.Entry::getKey).forEach(currentUrl -> {
            try {
                String completeUrl = currentUrl;
                completeUrl = createAbsoluteUrl(givenStartDomain,currentUrl);

                URL url = new URL(completeUrl);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                //setting timeout for the connection
                con.setConnectTimeout(3000);
                con.setReadTimeout(3000);
                if(con.getResponseCode() == 200) {
                    grepLinksFromInputStream(givenStartDomain,completeUrl,con);
                }

            } catch (IOException ie) {
                printWriter.printf("\n Exception %s for %s",ie.getMessage(),currentUrl);
            }
        });
        if (urlListToBeVisited.entrySet().parallelStream().filter(e -> e.getValue() == 0).count() > 0) {
            visitAndPrintURLs(givenStartDomain);
        }

    }

    /*
    Iterate through every line of the given url and grep urls from it
     */
    private static void grepLinksFromInputStream(String givenStartDomain,String currentUrl,HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        printWriter.printf("\n root url: %s",currentUrl);
        currentUrl = createAbsoluteUrl(givenStartDomain,currentUrl);
        urlListToBeVisited.put(currentUrl,1);


        while ((inputLine = in.readLine()) != null) {
            String matchedUrl = grepUrl(givenStartDomain,inputLine);

            matchedUrl = createAbsoluteUrl(givenStartDomain,matchedUrl);
            if (null != matchedUrl && urlListToBeVisited.get(matchedUrl) == null) {
                printWriter.printf("\n \t \t sub links: %s",matchedUrl);
                urlListToBeVisited.put(matchedUrl,0);
            }
        }
        in.close();

    }

    private static String createAbsoluteUrl(String givenStartDomain,String matchedUrl) {
        if (null != matchedUrl && matchedUrl.length() > 0 && matchedUrl.substring(0,1).equals("/")) {
            matchedUrl = givenStartDomain + matchedUrl;
        }
        return matchedUrl;
    }

    /**
     * This function will grep absolute url and complete url
     */
    public static String grepUrl(String givenStartDomain,String content) throws IOException {
        String patternStr;
        patternStr = "href=\"((" + givenStartDomain + ")*(/[a-z-0-9]*[^.]/?){0,})\"";
        Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        String matchedUrl = null;
        while (matcher.find()) {
            matchedUrl = matcher.group(1);
        }
        return matchedUrl;
    }
}
