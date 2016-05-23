package org.fenixsoft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IcyFenix on 2016-05-21.
 */
public class RegexEmailUtil {

    public static String getEmail(String line) {
        Pattern pattern = Pattern.compile("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}");
        Matcher matcher = pattern.matcher(line.toLowerCase());
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        File file = new File("c:/test.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        int lines = 0;
        int matches = 0;
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            lines++;
            String email = getEmail(line);
            if (email != null) {
                System.out.println(lines + ": '" + email + "'");
                matches++;
            }
        }
        // output of summary
        if (matches == 0) {
            System.out.println("No matches in " + lines + " lines");
        } else {
            System.out.println("\n" + matches + " matches in " + lines + " lines");
        }
    }

}
