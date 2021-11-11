package com.idlecodes;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// Collection of Codes.
public class Codes implements Serializable {
    String filename = "codes.db";
    private List<Code> list;

    public void printLastN(int n) {
        System.out.println("------Last 20 codes saved------");
        int j = this.list.size() - Math.min(this.list.size(), n);
        for (int i = j; i < this.list.size(); i++) {
            System.out.println(this.list.get(i).str + " : " + this.list.get(i).date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
        }
        System.out.println("-------------------------------");
    }

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
}
