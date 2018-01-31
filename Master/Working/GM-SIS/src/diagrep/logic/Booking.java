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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import vehicles.logic.Vehicle;



/**
 *
 * @author adamlaraqui
 */
public abstract class Booking {
    
    /**
     * Requirement 1. 
     * These are the common variables that are shared between the two types of booking.
     * Even though the other type of booking (Scheduled Maintenance) isn't being developed
     * within our group, I have structured the classes in a way which would allow simpler
     * integration of this extra module, if needed in future.
     * This meets Requirement 1.
     */
    private final int ID;
    private final int vehicleID; // Each booking can only be assigned to one vehicle!
    //private final Vehicle vehicle; // For Current Mileage and Date Of Last Service (needs setters)
    protected double cost;
    /* Need to check which type to use for cost!!! */
    protected boolean complete;
    protected LocalDateTime startDate;
    protected LocalDateTime endDate;
    protected boolean paid;
    
    public Booking(int BookingID) throws SQLException {
        Database db = Database.getInstance();
        Connection connection = db.getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT VehicleID, CostOfService, Completed, StartDate, EndDate, Paid FROM `DiagRepairBooking` WHERE `DiagRepID` = " + BookingID);
        ResultSet rs = ps.executeQuery();
        this.ID = BookingID;
        this.vehicleID = rs.getInt("VehicleID");
        this.cost = rs.getDouble("CostOfService");
        this.complete = (rs.getInt("Completed")==1);
        this.paid = (rs.getInt("Paid")==1);
        //this.vehicle = new Vehicle(this.vehicleID);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.startDate = LocalDateTime.parse(rs.getString("StartDate"), formatter);
        this.endDate = LocalDateTime.parse(rs.getString("EndDate"), formatter);
    }
    
    public String getVehicleReg() {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String getQuery = "SELECT Registration FROM VehicleInfo WHERE VehicleID = ?";
            PreparedStatement ps = con.prepareStatement(getQuery);
            ps.setInt(1, vehicleID);
            ResultSet rs = ps.executeQuery();
            return rs.getString("Registration");
        } catch (SQLException ex) {
            Logger.getLogger(Booking.class.getName()).log(Level.SEVERE, null, ex);
            return "Unknown";
        }
    }
    
    public int getVehicleMileage() {
        // When Ilias fixed the Vehicle constructor, I can switch to using the getter method from Vehicle
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String getQuery = "SELECT CurrentMileage FROM Vehicle WHERE VehicleID = ?";
            PreparedStatement ps = con.prepareStatement(getQuery);
            ps.setInt(1, vehicleID);
            ResultSet rs = ps.executeQuery();
            return rs.getInt("CurrentMileage");
        } catch (SQLException ex) {
            Logger.getLogger(Booking.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    
    public String getCustomerName() {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String getQuery = "SELECT Firstname, Surname FROM Customer INNER JOIN Vehicle ON Customer.CustomerID = Vehicle.CustomerID WHERE Vehicle.VehicleID = ?";
            PreparedStatement ps = con.prepareStatement(getQuery);
            ps.setInt(1, vehicleID);
            ResultSet rs = ps.executeQuery();
            return rs.getString("Firstname") + " " + rs.getString("Surname");
        } catch (SQLException ex) {
            Logger.getLogger(Booking.class.getName()).log(Level.SEVERE, null, ex);
            return "Unknown";
        }
    }
    
    public void setStartAndEndDate(LocalDateTime startDate, LocalDateTime endDate) throws IllegalArgumentException, SQLException {
        // The reason we use one method for both dates, is so that we won't get accidental errors.
        // For example, the StartDate might first be updated to after the old EndDate, but EndDate may also have changed to a later date.
        // If no change...
        if(this.startDate.equals(startDate)&&this.endDate.equals(endDate)) return;
        // If booking already in progress
        // UPDATE - Further check if this booking is the earliest active booking for the vehicle
        // If it is, then DO NOT allow Start Date/Time to change
        // If it is not, then it means we are restricting access to Diagnosis/Repair, hence can allow extension of Start Date/Time
        if( !this.startDate.equals(startDate) && !Utility.isDateInFuture(this.startDate) && (Utility.getEarliestActiveBookingID(this.vehicleID) == this.ID ) ) throw new IllegalArgumentException("Start Date cannot be changed since the booking is already in progress and is the earliest active booking for the vehicle.");
        // If trying to change start date to before the current date
        if(!this.startDate.equals(startDate) && !Utility.isDateInFuture(startDate)) throw new IllegalArgumentException("Start Date cannot be changed to a date on or before the current date, according to the 'Current Date' table.");
        // If trying to change end date to before start date
        if(!Utility.isDate1BeforeDate2(startDate, endDate)) throw new IllegalArgumentException("Start Date cannot be changed to a date after the End Date.");
        // If new start date is public holiday
        if(!this.startDate.equals(startDate) && Utility.isPublicHoliday(startDate)) throw new IllegalArgumentException("The new Start Date you selected is a public holiday.");
        // If new end date is a public holiday
        if(!this.endDate.equals(endDate) && Utility.isPublicHoliday(endDate)) throw new IllegalArgumentException("The new End Date you selected is a public holiday.");
        // Check if the date range overlaps with any INCOMPLETE bookings for the SAME VEHICLE
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        
        /** Logic adapted from "Charles Bretana" on StackOverflow - Determine Whether Two Date Ranges Overlap.
         * Rather than checking each booking for the vehicle against the new date range, we use the following query.
         * We search for any NON-COMPLETED bookings for THIS VEHICLE, which do NOT SATISFY this logic.
         * We also of course, exclude the original date range of this booking from the query, else it could potentially detect conflicts with itself (original and desired version).
         * The main logic ensures that any 2 dates DO NOT overlap, based on De Morgan's laws.
         */
        String getOverlappingBookings = 
            "SELECT DiagRepID, StartDate, EndDate FROM DiagRepairBooking WHERE\n" +
            "VehicleID = ? AND Completed = 0 AND NOT DiagRepID = ?\n" + // Make search specific to this vehicle, and excluding the booking's existing date range
            "AND NOT (\n" +
            "	StartDate >= ?\n" + // Check that StartDate of each booking occurs after/on EndDate of the new date range
            "	OR EndDate <= ?\n" + // Check that EndDate of each booking occurs before/on StartDate of the new date range
            ")";
        PreparedStatement overlappingBookingsStatement = con.prepareStatement(getOverlappingBookings);
        overlappingBookingsStatement.setInt(1, this.vehicleID); // Vehicle ID
        overlappingBookingsStatement.setInt(2, this.ID); // DiagRep Booking ID
        overlappingBookingsStatement.setString( 3, Utility.DateTimeToStr(endDate) ); // New intended end date/time
        overlappingBookingsStatement.setString( 4, Utility.DateTimeToStr(startDate) ); // New intended start date/time
        ResultSet overlappingBookingIDs = overlappingBookingsStatement.executeQuery();
        if(!overlappingBookingIDs.isClosed()) {
            // Inform user of the booking ID(s) that conflicts with this new start/end date, for this particular vehicle.
            String listOfConflictIDs = "";
            while(overlappingBookingIDs.next()) {
                listOfConflictIDs += "\n\nBooking ID: "+overlappingBookingIDs.getInt("DiagRepID") + "\nStart Date: "+overlappingBookingIDs.getString("StartDate") + "\nEnd Date: " + overlappingBookingIDs.getString("EndDate");
            }
            throw new IllegalArgumentException("The new Start and End Date you selected overlap with another active booking for this vehicle.\n\nBooking ID(s) which conflict:"+listOfConflictIDs);
        } else {
            // No date range conflicts, thus validation is all complete
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
    
    public boolean isComplete() {
        return this.complete;
    }
    
    public boolean isPaid() {
        return this.paid;
    }
    
    public int getVehicleID() {
        return vehicleID;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public double getCost() {
        return cost;
    }
    
    public StringProperty StartDateProperty() {
        return new SimpleStringProperty(Utility.DateTimeToStr(startDate));
    }
    
    public StringProperty EndDateProperty() {
        return new SimpleStringProperty(Utility.DateTimeToStr(endDate));
    }
    
    public StringProperty VehicleRegProperty() {
        return new SimpleStringProperty(this.getVehicleReg());
    }
    
    public StringProperty CustomerNameProperty() {
        return new SimpleStringProperty(this.getCustomerName());
    }
    
    /**
     * ONLY FOR DIAGREPAIR MODULE - Overrides cost to given value regardless of what it was before.
     * To increment cost or decrement cost, use the methods incrementCost() and decrementCost() respectively.
     * @param amount to set cost to
     */
    public void setCost(double amount) throws IllegalArgumentException {
        if(amount<0) throw new IllegalArgumentException("Price cannot be negative.");
        this.cost = amount;
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String updateQuery = "UPDATE `DiagRepairBooking` SET CostOfService = ROUND(?, 2) WHERE DiagRepID = ?;";
            PreparedStatement ps = con.prepareStatement(updateQuery);
            ps.setDouble(1, this.cost);
            ps.setInt(2, ID);
            ps.executeUpdate();
        } catch(SQLException ex) {
            throw new IllegalArgumentException("Unable to update booking cost."+this.cost);
        }
    }
    
    /**
     * Increments cost by a given amount for the booking - updates it immediately.
     * @param amount to increment by
     */
    public void incrementCost(double amount) throws IllegalArgumentException {
        if(amount<0) throw new IllegalArgumentException("Cannot increment cost by negative amount.");
        this.setCost(this.cost + amount);
    }
    
    /**
     * Decrements cost by a given amount for the booking - updates it immediately.
     * @param amount to decrement by (provide the positive value)
     */
    public void decrementCost(double amount) throws IllegalArgumentException {
        if(amount<0) throw new IllegalArgumentException("Cannot decrement cost by negative amount.");
        this.setCost( Math.max(0, this.cost - amount) );
    }
}