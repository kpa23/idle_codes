package com.idlecodes;


import org.jsoup.Jsoup;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;


public class IdleCodes {

    public static class Code implements Serializable {
        public String str; // Code
        public LocalDateTime date; // Date added

        // Constructor
        public Code(String str) {
            this.str = str;
            this.date = LocalDateTime.now();
        }

        // Compare objects to find out if we already saved one
        @Override
        public boolean equals(Object object) {
            if (object != null && object instanceof Code thing) {
                thing = (Code) object;
                if (str == null) {
                    return (thing.str == null);
                } else {
                    return str.equals(thing.str);
                }
            }
            return false;
        }

        // Generating hash of an object
        @Override
        public int hashCode() {
            return str.hashCode();
        }
    }

    // Collection of Codes.
    public static class Codes implements Serializable {
        String filename = "codes.db";
        private List<Code> list;

        // Adding new code to the list
        public boolean add(String s) {
            Code tmp = new Code(s);
            if (!this.list.contains(tmp)) { // Check if we don't have this code in the list
                this.list.add(tmp);
                return true;
            }
            return false;
        }

        // Save list to file
        public void save() {
            try (FileOutputStream fos = new FileOutputStream(this.filename);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                // Delete all codes that are older than 60 days
                list.removeIf(b -> ChronoUnit.DAYS.between(LocalDateTime.now(), b.date) > 30);
                oos.writeObject(this.list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Read list from file
        @SuppressWarnings("unchecked")
        public void load() {
            try (FileInputStream fis = new FileInputStream(this.filename);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                this.list = (List<Code>) ois.readObject();
            } catch (Exception e) {
                this.list = new ArrayList<>();
            }
        }
    }


    public static void main(String[] args) throws IOException, AWTException {
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

        System.out.println("------Last 20 codes saved------");
        int j = c.list.size() - Math.min(c.list.size(), 20);
        for (int i = j; i < c.list.size(); i++) {
            System.out.println(c.list.get(i).str + " : " + c.list.get(i).date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
        }
        System.out.println("-------------------------------");

        String[] matches = Pattern.compile("(?<=\\\")([\\w!@#$%^&*.]{4}-?){2,3}([\\w!@#$%^&*.]{4})(?=\\\\)")
                .matcher(data)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        new Scanner(System.in);
        // TODO Wait until game is running
        boolean found = false;
        for (String code_s : matches) {
            if (c.add(code_s)) {
                if (!found) {
                    System.out.println("New codes:");
                    System.out.println("Code is copied to clipboard");
                    found = true;
                }
                System.out.print(code_s);
                StringSelection strSel = new StringSelection(code_s + "\n");
                clipboard.setContents(strSel, null);
                CodeBot cb = new CodeBot();
                if (cb.findButton()) {
                    if (cb.enterCode())
                        System.out.println(" (ok)");

                    else System.out.println(" (bad code)");
                } else {
                    System.in.read();
                    return;
                }
            }
        }
        if (!found) {
            System.out.println("No new codes");
        }
        // Save to file
        c.save();
        System.out.println("All codes are saved");
        System.in.read();

    }
}
