/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package diagrep.logic;

import common.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adamlaraqui
 */
public class Utility {
    
    /**
     * Concatenates the date and time into SQLite-ready format.
     * Validation is also done here to ensure the date and time occur within operating hours.
     * This fulfils requirement 4!
     * 
     * Opening times:
     * Monday to Friday (0900 - 1730) [Cannot start at 17:30]
     * Saturday (0900 - 1200) [Cannot start at 12:00]
     * 
     * @param date - The date in SQLlite format (yyyy-mm-dd)
     * @param hour - Hour of the working day
     * @param minute - Minute value within the given hour
     * @param isStart - Whether we're validating a start time or end time
     * @return LocalDateTime object that represents the date, hour and minute parameters
     */
    public static LocalDateTime constructDateTimeObj(LocalDate date, int hour, int minute, boolean isStart) throws IllegalArgumentException {
        int dayOfWeek = date.getDayOfWeek().getValue();
        /* Day of week:
         * 1 = Monday
         * 2 = Tuesday
         * ...
         * 7 = Sunday
         */
        switch (dayOfWeek) {
            case 7: // Check if the date is a Sunday [Garage Closed] (Requirement 4)
                throw new IllegalArgumentException("Invalid Date - The garage is not open on Sundays");
            case 6: // If the date is on a Saturday [Early Closing Time] (Requirement 4)
                if(!isStart && hour==12 && minute==0) break; // If they want to end booking at closing time, then it's valid. Else use other validator.
                if(hour<9 || hour>11) throw new IllegalArgumentException("Invalid Time - Saturday Opening Times are 9AM - 12PM"); // Can't have start time from 12PM anyway...
                break;
            default: // If the date is a normal working day [Mon-Fri] (Requirement 4)
                if(!isStart && hour==17 && minute==30) break; // If they want to end booking at closing time, then it's valid. Else use other validator.
                if(hour<9 || hour>17) throw new IllegalArgumentException("Invalid Time - Weekday Opening Times are 9AM - 5:30PM");
                if(hour==17 && minute>=30) throw new IllegalArgumentException("Invalid Time - Weekday Closing Time is 5:30PM"); // Can't have a start time at the garage closing time...
                break;
        }
        if(minute<0 || minute>59) throw new IllegalArgumentException("Invalid Time - Accepted minute values are 0-59"); // Ensures that minute value is within accepted bounds, else can cause an error on insertion
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // Line below from Vlad on Stackoverflow - How to format a number to a 2 char string?
        String timeStr = String.format("%02d", hour) + ":" + String.format("%02d", minute); // Since the time 0X is shown as X in an integer format
        String dateTimeStr = dateStr + " " + timeStr;
        return strToDateTime(dateTimeStr);
    }
    
    /**
     * Compares two dates; checks whether the first date occurs before the second.
     * @param date1 The first/earlier date
     * @param date2 The second/later date
     * @return boolean expressing whether first date occurs before the second date
     */
    public static boolean isDate1BeforeDate2(LocalDateTime date1, LocalDateTime date2) {
        int minsDiff = (int) java.time.Duration.between(date1, date2).toMinutes();
        if(minsDiff>0)
            return true; // If the dates are in chronological order
        else
            return false; // If the dates are in reverse order
    }
    
    /**
     * Checks whether the given date is in the future, according to the CurrentDate table.
     * This fulfils requirement 6!
     * @param futureDateTime The date to check if in the future
     * @return true if the date is in the future; false otherwise
     * @throws java.sql.SQLException
     */
    public static boolean isDateInFuture(LocalDateTime futureDateTime) throws SQLException {
        Database db = Database.getInstance();
        String datetimestr = db.getCurrentDateTime();
        return isDate1BeforeDate2(strToDateTime(datetimestr), futureDateTime);
    }
    
    /**
     * Checks whether the given date is a public or bank holiday.
     * This fulfils requirement 5!
     * @param date The date to check if a public holiday
     * @return true if the date is a public holiday; false otherwise
     * @throws java.sql.SQLException
     */
    public static boolean isPublicHoliday(LocalDateTime date) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateToCheck = date.format(formatter);
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM Holidays WHERE Date = '"+dateToCheck+"';");
        return rs.getInt(1)>0; // If the date is found at least once, then it's a public holiday
    }
    
    /**
     * Converts a DateTime from String format to LocalDateTime format
     * @param dateTimeStr The date and time in String format (yyyy-MM-dd HH:mm)
     * @return LocalDateTime object that represents the same date and time that was given as a parameter
     */
    public static LocalDateTime strToDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        formatter = formatter.withLocale(Locale.ENGLISH);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
    
    /**
     * Converts a DateTime from LocalDateTime format to String format
     * @param dateTime The date and time in LocalDateTime format
     * @return LocalDateTime String that represents the same date and time that was given as a parameter
     */
    public static String DateTimeToStr(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }
    
    /**
     * For a given Vehicle ID, will check the Booking ID of the earliest active booking for that vehicle.
     * This is a foolproof way of ensuring that even if bookings overrun on our schedule, only the earliest booking can allow diagnosis and repairs at any time.
     * This validation is deemed essential by the Parts and SPC modules, as a way of preventing errors in their functionality.
     * This method should only be used by the booking controller, UPON verifying that Start Date is ON OR BEFORE Current Date.
     * @param vehicleID Vehicle ID to check earliest active booking for
     * @return -1 if no bookings found or SQLite error, else the ID of the earliest active booking
     */
    public static int getEarliestActiveBookingID(int vehicleID) {
        int bookingID = -1;
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            // NOTE: If this method is called, it means the vehicle already has at least one active booking...
            String earliestBookingQuery =
                    "SELECT DiagRepID, MIN(StartDate) as EarliestDate\n" + // Get Booking ID of the earliest active booking
                    "FROM DiagRepairBooking\n" +
                    "WHERE VehicleID = ?\n" + // For a specific vehicle
                    "AND Completed = 0"; // That's not yet completed (active)
            PreparedStatement getEarliestBookingIDStatement = con.prepareStatement(earliestBookingQuery);
            getEarliestBookingIDStatement.setInt(1, vehicleID);
            ResultSet earliestBookingID = getEarliestBookingIDStatement.executeQuery();
            if(!earliestBookingID.isClosed()) {
                bookingID = earliestBookingID.getInt(1); // Found earliest booking
            } // Else, no active bookings found (should not happen)
        } catch (SQLException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bookingID;
    }
    
    /**
     * HELPER METHOD - Checks whether given User ID is a mechanic
     * @param userID - User ID (primary key) of system user to check
     * @return true if user is a mechanic, false if either not an existing user or not a mechanic
     */
    public static boolean isMechanicValid(int userID) {
        boolean isValid = false;
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String getMechanicRecord = "SELECT IsMechanic FROM SystemUser WHERE userID = ?";
            PreparedStatement getMechanicStatement = con.prepareStatement(getMechanicRecord);
            getMechanicStatement.setInt(1, userID);
            ResultSet mechanicRecord = getMechanicStatement.executeQuery();
            if( !mechanicRecord.isClosed() && mechanicRecord.getInt("IsMechanic") == 1 ) {
                isValid = true;
            }
        } catch (SQLException ex) {
            //Logger.getLogger(DiagRepair.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isValid;
    }
}