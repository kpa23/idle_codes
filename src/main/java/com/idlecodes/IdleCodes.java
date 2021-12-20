package com.idlecodes;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.Properties;

public class IdleCodes {
    private static int mode;
    private static boolean debug;

    public static void saveProperties() {
        try (OutputStream out = new FileOutputStream(".\\settings.cfg")) {
            Properties props = new Properties();
            props.setProperty("mode_auto", String.valueOf(mode));
            props.store(out, "User properties.\n" +
                    "Mode:\n" +
                    "  1 for manual copy\\paste mode\n" +
                    "  2 for automatic mode\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadProperties() {
        try (InputStream input = new FileInputStream(".\\settings.cfg")) {
            Properties props = new Properties();
            // load a properties file
            props.load(input);
            // get the property value
            mode = Integer.parseInt(props.getProperty("mode_auto"));
            debug = Boolean.parseBoolean(props.getProperty("debug"));
        } catch (IOException ex) {
            mode = 2;
            saveProperties();
        }
    }

    public static boolean autoCode() throws AWTException, IOException, OCRException.OCRError, OCRException.GameNotFound {
        CodeBot cb = new CodeBot(debug);
        if (cb.findButton()) {
            if (cb.enterCode())
                System.out.println(" (ok)");
            else System.out.println(" (bad code)");
        } else {
            System.in.read();
            return false;
        }
        return true;
    }

    public static void loadJarDll(String name) throws IOException {
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = File.createTempFile(name, "");
        try (FileOutputStream fos = new FileOutputStream(temp); InputStream in = CodeBot.class.getResourceAsStream(name)) {
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        System.load(temp.getAbsolutePath());
    }

    public static void main(String[] args) throws IOException, AWTException, OCRException.OCRError, OCRException.GameNotFound {
        loadProperties();
        loadJarDll("/opencv_java454.dll");
        String url0 = "https://idle-champions.fandom.com/wikia.php?controller=Fandom%5CArticleComments%5CApi%5CArticleCommentsController&method=getComments&namespace=0&title=Combinations";
        String url1 = "https://incendar.com/idlechampions_codes.php";
        String page;
        if (args.length == 0)
            page = "1";
        else
            page = args[0];
        String data0 = Jsoup.connect(url0).ignoreContentType(true).execute().body() + Jsoup.connect(url0 + "&page=" + page).ignoreContentType(true).execute().body();

        Document doc;
        Elements cds;
        doc = Jsoup.parse(Jsoup.connect(url1).ignoreContentType(true).execute().body());
        cds = doc.select("textarea"); // a with href
        String data1 = cds.text();
        Codes c = new Codes();

        String[] matches0 = Pattern.compile("(?<=\\\")([\\w!@#$%^&*.]{4}-?){2,3}([\\w!@#$%^&*.]{4})(?=\\\\)")
                .matcher(data0)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
        String[] matches = Pattern.compile("([\\w!@#$%^&*.]{4}-?){2,3}([\\w!@#$%^&*.]{4})")
                .matcher(data1)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
        HashSet<String> set = new HashSet<>();

        set.addAll(Arrays.asList(matches0));
        set.addAll(Arrays.asList(matches));

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        // try load codes from file
        c.load();
        c.printLastN(20);
        boolean found = false;
        for (String code_s : set) {
            if (c.add(code_s)) {
                if (!found) {
                    System.out.println("New codes:");
                    System.out.println("Code is copied to the clipboard");
                    found = true;
                }
                System.out.print(code_s);
                StringSelection strSel = new StringSelection(code_s + "\n");
                clipboard.setContents(strSel, null);
                if (mode == 2) {
                    if (!autoCode())
                        return;

                } else {
                    System.in.read();
                }
            }
        }
        if (!found) {
            System.out.println("No new codes");
        } else {
            // Save to file
            c.save();
        }
        System.out.println("All codes are saved");
        System.in.read();
    }
}
