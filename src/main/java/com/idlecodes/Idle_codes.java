package com.idlecodes;

import org.jsoup.*;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Idle_codes {
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
            if (object != null && object instanceof Code) {
                Code thing = (Code) object;
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
        public List<Code> list;

        // Adding new code to the list
        public void add(String s) {
            Code tmp = new Code(s);
            if (!this.list.contains(tmp)) { // Check if we don't have this code in the list
                this.list.add(tmp);
            }
        }

        // Save list to file
        public void save() throws IOException {
            FileOutputStream fos = new FileOutputStream(this.filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (int i = 0; i < list.size(); i++) {
                Code x = list.get(i);
                // Delete all codes that are older than 60 days
                if (ChronoUnit.DAYS.between(LocalDateTime.now(), x.date) > 60) {
                    list.remove(i);
                }
            }
            oos.writeObject(this.list);
            oos.close();
        }

        // Read list from file
        public void load() throws IOException, ClassNotFoundException {
            FileInputStream fis = new FileInputStream(this.filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.list = (List<Code>) ois.readObject();
            ois.close();
        }
    }

    public static void main(String[] args) throws IOException {
        String url = "https://idle-champions.fandom.com/wikia.php?controller=Fandom%5CArticleComments%5CApi%5CArticleCommentsController&method=getComments&namespace=0&title=Combinations";

        try {
            String data = Jsoup.connect(url).ignoreContentType(true).execute().body();
            Codes c = new Codes();
            // try load codes from file
            try {
                c.load();
            } catch (Exception e) {
                c.list = new ArrayList<>();
            }
            System.out.println("------Last 20 codes saved------");
            int j = c.list.size() - Math.min(c.list.size(), 20);
            for (int i = j; i < c.list.size(); i++) {
                System.out.println(c.list.get(i).str + " : " + c.list.get(i).date.format(DateTimeFormatter.ofPattern("YYYY.MM.dd HH:mm")));
            }
            System.out.println("-------------------------------");

            Pattern pattern = Pattern.compile("(?<=\")([\\w\\d!@#$%^&*.]{4}-?){2,3}([\\w\\d!@#$%^&*.]{4})(?=\\\\)");
            Matcher m = pattern.matcher(data);
            if (m.find()) {
                System.out.println("New codes:");
                System.out.println("Code is copied to clipboard, press Enter after you use it in the game");
            }
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            Scanner s = new Scanner(System.in);
            while (m.find()) {
                String code_s = m.group();
                c.add(code_s);
                System.out.print(code_s);
                StringSelection strSel = new StringSelection(code_s + "\n");
                clipboard.setContents(strSel, null);
                try {
                    s.nextLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Save to file
            c.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.in.read();
    }
}
