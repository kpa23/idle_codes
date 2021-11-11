package com.idlecodes;


import org.jsoup.Jsoup;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.Properties;

public class IdleCodes {
    private static int mode;

    public static void saveProperties() {
        try (OutputStream out = new FileOutputStream(".\\settings.cfg")) {
            Properties props = new Properties();
            props.setProperty("mode_auto", String.valueOf(mode));
            props.store(out, """
                    User properties.
                    Mode:
                      1 for manual copy\\paste mode
                      2 for automatic mode""");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadProperties() {
        try (InputStream input = new FileInputStream(".\\settings.cfg")) {
            Properties props = new Properties();
            // load a properties file
            props.load(input);
            // get the property value and print it out
            mode = Integer.parseInt(props.getProperty("mode_auto"));
        } catch (IOException ex) {
            mode = 2;
            saveProperties();
        }
    }

    public static boolean autoCode() throws AWTException, IOException {
        CodeBot cb = new CodeBot();
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

    public static void main(String[] args) throws IOException, AWTException {
        loadProperties();
        String url = "https://idle-champions.fandom.com/wikia.php?controller=Fandom%5CArticleComments%5CApi%5CArticleCommentsController&method=getComments&namespace=0&title=Combinations";
        String page;
        if (args.length == 0)
            page = "1";
        else
            page = args[0];
        String data = Jsoup.connect(url).ignoreContentType(true).execute().body() + Jsoup.connect(url + "&page=" + page).ignoreContentType(true).execute().body();
        Codes c = new Codes();
        // try load codes from file
        c.load();

        c.printLastN(20);
        String[] matches = Pattern.compile("(?<=\\\")([\\w!@#$%^&*.]{4}-?){2,3}([\\w!@#$%^&*.]{4})(?=\\\\)")
                .matcher(data)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();


        boolean found = false;
        for (String code_s : matches) {
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
