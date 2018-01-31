package parts.logic;

import common.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains random helper methods for Part package
 * @author Mia
 */
public class Utility {
    
    /**
     * Capitalizes the first letter of each word in a string
     * Code by tutorialspoint: Java Examples - Make first letter of each word in Uppercase 
     * 
     * @param str String to be capitalized
     * @return String with first letter of each word capitalized
     */
    public static String capitalizeFirstLetterOfWords(String str){
      StringBuffer stringbf = new StringBuffer();
      Matcher m = Pattern.compile(
         "([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(str);
      
      while (m.find()) {
         m.appendReplacement(
            stringbf, m.group(1).toUpperCase() + m.group(2).toLowerCase());
      }
      return m.appendTail(stringbf).toString();
    }
    
    /**
     * Closes the statement and connection objects 
     * 
     * @param s Statement to close 
     * @param c Connection to close
     */
    public static void closeStatementAndConnection(Statement s, Connection c){
        try {
            if(s != null && !s.isClosed()){
                s.close();
            }else if(c != null && !c.isClosed()){
                c.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Adds a year to the date given and returns the date 
     * @param currentDate the current date in the form "yyyy-mm-dd"
     * @return the date after a year in the form "yyyy-mm-dd"
     */
    public static String getDateAfterYear(String currentDate){
        Date current = convertStringToDate(currentDate, Database.INSIDE_DB_DATE_FORMAT);
        Calendar c = Calendar.getInstance();
        c.setTime(current);
        c.add(Calendar.YEAR, 1);
        return convertDateToString(c.getTime(), Database.INSIDE_DB_DATE_FORMAT);
    }
    
    /**
     * converts a date object in the given format to a string in the given format
     * @param date The date object
     * @param format The format of the date object
     * @return A string in the format given
     */
    public static String convertDateToString(Date date, String format){
        Format formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }
    
    /**
     * Escapes apostrophes for inserting/searching in the DB
     * @param stringToInsert String to allow apostrophes for
     * @return String with apostrophes properly escaped
     */
    public static String allowApostrophes(String stringToInsert){
        return stringToInsert.replace("'", "''");
    }
    
    /**
     * Converts a string to a date object
     * @param date The string date
     * @param stringDateFormat The format of the string date
     * @return The date object in the same format
     */
    public static Date convertStringToDate(String date, String stringDateFormat){
        DateFormat df = new SimpleDateFormat(stringDateFormat);
        Date result = null;
        try {
            result = df.parse(date);
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * Takes a string and returns a new string containing the string repeated 
     * a given number of times.
     * 
     * Code by Dominik Sandjaja/Ali Imran (StackOverflow) - Can I multiply string in Java to repeat sequences?
     * @param s
     * @param count
     * @return 
     */
    public static String repeatString(String s,int count){
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < count; i++) {
            r.append(s);
        }
        return r.toString();
    }
}
