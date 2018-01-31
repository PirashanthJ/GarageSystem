/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diagrep.logic;

/**
 *
 * @author adamlaraqui
 */
import common.Database;
import customers.logic.Bill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import parts.logic.Part;

public class DiagRepair extends Booking {
    
    private final int ID; // The Booking ID represents a Primary Key, therefore doesn't change
    private int mechanicID; // A single mechanic should be assigned to each booking, responsible for all repairs on that vehicle.
    private int repairTime; // We're required to keep record of how many hours are spent on the repair. This helps to formulate cost.
    private boolean vehicleForSPCRepair; // Whether we decided to send the whole vehicle to SPC
    private String fault; // The name of the diagnosed fault. Can only be set after the booking is in progress.
    
    public DiagRepair(int DiagRepID) throws SQLException {
        super(DiagRepID);
        Database db = Database.getInstance();
        Connection connection = db.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT DiagRepID, MechanicID, RepairTime, sendVehicleToSPC, fault FROM `DiagRepairBooking` WHERE `DiagRepID` = " + DiagRepID);
            ResultSet rs = ps.executeQuery();
            this.ID = rs.getInt("DiagRepID");
            this.mechanicID = rs.getInt("MechanicID");
            this.repairTime = rs.getInt("RepairTime");
            this.vehicleForSPCRepair = (rs.getInt("sendVehicleToSPC")==1);
            this.fault = rs.getString("fault");
        } catch (SQLException ex) {
            Logger.getLogger(DiagRepair.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Unable to retrieve booking data");
        }
    }
    
    public void updateBooking() throws SQLException {
        Database db = Database.getInstance();
        Connection connection = db.getConnection();
        String updateQuery = "UPDATE `DiagRepairBooking` SET CostOfService = ROUND(?, 2), StartDate = ?, EndDate = ?, Completed = ?, RepairTime = ?, MechanicID = ?, Paid = ?, sendVehicleToSPC = ?, fault = ? WHERE DiagRepID = ?;";
        PreparedStatement ps = connection.prepareStatement(updateQuery);
        ps.setDouble(1, cost); // Cost
        ps.setString(2, Utility.DateTimeToStr(startDate)); // Start Date
        ps.setString(3, Utility.DateTimeToStr(endDate)); // End Date
        ps.setInt(4, (complete)?1:0); // Completed Status
        ps.setInt(5, repairTime); // Repair Time
        ps.setInt(6, mechanicID); // Mechanic ID [FOREIGN KEY]
        ps.setInt(7, (paid)?1:0); // Paid Status
        ps.setInt(8, (vehicleForSPCRepair)?1:0);
        ps.setString(9, fault);
        ps.setInt(10, ID); // DiagRepair Booking ID
        System.out.print("Updating row... ");
        ps.executeUpdate();
        System.out.println("Success!");
    }
    
    /**
     * Validates and/or sets the assigned mechanic ID.
     * Starts off with validation, whether the booking has started or not.
     * We don't bother whether the provided ID is different or new; we still must (re)validate it.
     * @param mechID UserID of the selected mechanic
     * @throws IllegalArgumentException if booking already in progress
     * @throws SQLException if any SQLite errors
     */
    public void setMechanicID(int mechID) throws IllegalArgumentException, SQLException {
        if(Utility.isMechanicValid(mechID)) {
            if( Utility.isDateInFuture(this.startDate) ) {
                // If booking has not started yet...
                this.mechanicID = mechID;
            } else if( Utility.getEarliestActiveBookingID(this.getVehicleID())!=this.ID ) {
                // If booking has started, but is not the earliest active one (diagnosis/repairs disabled) [Mechanic changeable]
                this.mechanicID = mechID;
            } // Else the booking is active and mechanic will not be deletable from admin panel...
        } else {
            /** Selected Mechanic not (or no longer) valid.
             * This will happen in 2 cases:
             * 1) While Create A Booking view is open, select a mechanic, delete the mechanic from Admin Panel, then click Add
             * 2) While Edit Booking view is open for future booking or inactive (current) booking, with any mechanic selected, delete the mechanic from Admin Panel, then click Update
             */
            throw new IllegalArgumentException("Could not assign selected mechanic to booking. Either the user no longer exists or their mechanic status was revoked.");
        }
    }
    
    /**
     * Get the name of the assigned mechanic.
     * This is used mainly to inform the user of the GUI which mechanic is supposed to be doing the repairs.
     * @return full name of the assigned mechanic
     */
    public String getMechanicName() {
        try{
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String getMechanicName = "SELECT Firstname, Surname FROM SystemUser WHERE userID = ?";
            PreparedStatement ps = con.prepareStatement(getMechanicName);
            ps.setInt(1, this.mechanicID);
            ResultSet rs = ps.executeQuery();
            return rs.getString("Firstname") + " " + rs.getString("Surname");
        } catch(SQLException ex) {
            System.out.println(ex.getMessage());
            return "User Deleted";
        }
    }
    
    /**
     * Get the hourly rate of the assigned mechanic for the booking.
     * This is used mainly to inform the user of the GUI how much we charge for bookings based on repair time.
     * @return cost of the assigned mechanic
     */
    public double getMechanicRate() {
        try{
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String getMechanicRate = "SELECT HourlyWage FROM SystemUser WHERE userID = ?";
            PreparedStatement ps = con.prepareStatement(getMechanicRate);
            ps.setInt(1, this.mechanicID);
            ResultSet rs = ps.executeQuery();
            return rs.getDouble("HourlyWage");
        } catch(SQLException ex) {
            System.out.println(ex.getMessage());
            return 0;
        }
    }
    
    /**
     * Removes any part from the booking.
     * Given the ID of a part in the booking, this method attempts to cancel the selected repair and remove it from the booking.
     * These are the conditions for each repair type to be cancelled:
     * (1) Add New Part - Cannot be cancelled from the DiagRepair module! Must be un-installed from Parts module
     * (2) Repair Only - No constraints!
     * (3) SPC Repair - The part must not yet have been booked by an SPC Centre, or the SPC booking must be deleted.
     * @param partID ID of the part you wish to remove from the booking
     * @throws SQLException if any SQLite issues arise
     * @throws IllegalArgumentException if any constraint listed above fails
     * @throws diagrep.logic.InProgressException if part is in an active SPC booking
     * @throws IllegalArgumentException if part doesn't exist
     */
    public void removePart(int partID) throws IllegalArgumentException, SQLException, InProgressException {
        PartForRepairBooking part = new PartForRepairBooking(partID, ID);
        part.removeFromBooking(false);
    }
    
    /**
     * Gets the DiagRepair booking ID.
     * @return booking ID
     */
    public int getBookingID() {
        return this.ID;
    }
    
    /**
     * Gets the User ID of the assigned mechanic.
     * @return User ID of the mechanic
     */
    public int getMechanicID() {
        return this.mechanicID;
    }
    
    /**
     * Gets the name of the diagnosed fault.
     * @return name of fault (if diagnosed), "N/A" otherwise
     */
    public String getFault() {
        if(this.fault==null || this.fault.equals("")) {
            return "N/A";
        } else {
            return this.fault;
        }
    }
    
    /**
     * Sets the name of the diagnosed fault.
     * @param fault name of the fault
     */
    public void setFault(String fault) {
        if(fault.equals("")||fault.equals("N/A")) throw new IllegalArgumentException("Invalid Fault Name");
        this.fault = fault;
    }
    
    /**
     * Determines whether the vehicle has been chosen to be repaired at an SPC Centre.
     * It might not already have been sent, but we should nevertheless disallow adding any parts to the booking.
     * @return true if vehicle is selected to be sent to SPC, false otherwise
     */
    public boolean isVehicleForSPC() {
        return this.vehicleForSPCRepair;
    }
    
    public void markAsComplete() throws SQLException, InProgressException {
        if(this.complete) return;
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        String countActiveSPCBookings = "SELECT count(*) FROM SPCBooking WHERE DiagRepBookingID = "+this.ID+" AND ReturnStatus = 0";
        PreparedStatement countActiveBookingsStatement = con.prepareStatement(countActiveSPCBookings);
        ResultSet rs = countActiveBookingsStatement.executeQuery();
        boolean hasActiveSPCBookings = (rs.getInt(1)>0); // If there are active SPC bookings (booked but not complete)
        if(hasActiveSPCBookings) throw new InProgressException("Booking cannot be completed since there are still associated active SPC bookings. Either go to the SPC module to process these repairs, or cancel them from the Diagnostics and Repair booking and try again.");
        /** Now check if there are any part(s)/vehicle that was marked for SPC repair, but not yet booked.
         * We do this using LEFT (OUTER) JOIN, by getting all parts that we marked for SPC, and see if it has a corresponding row in the SPC table(s)
         */
        if(this.vehicleForSPCRepair) {
            // In the above query, we have already checked for any non-completed bookings, so here we just have to check if a vehicle booking was made (for the specific DiagRepair Booking)
            String checkIfSPCVehicleBooked = "SELECT SPCBookingID FROM SPCBooking WHERE IsBookingForVehicle = 1 AND DiagRepBookingID = "+this.ID;
            PreparedStatement getVehicleBookingIDStatement = con.prepareStatement(checkIfSPCVehicleBooked);
            ResultSet SPCVehicleBookingID = getVehicleBookingIDStatement.executeQuery();
            if(SPCVehicleBookingID.isClosed()) {
                throw new InProgressException("Booking cannot be completed since the vehicle was supposed to be repaired at the SPC Centre, but the SPC has made no booking from their side yet.");
            } else {
                this.complete = true;
                this.endDate = Utility.strToDateTime(db.getCurrentDateTime());
                this.updateBooking();
                Bill billForBooking = new Bill(this.ID);
                billForBooking.insertBill();
            }
        } else {
            /** What does this insanely long query do...
             * Well, this seemed to be the most efficient way to check that all parts we wanted to send to SPC were actually booked.
             * Using LEFT JOIN, it will return the Part ID's that we have selected for SPC Repair, which have not been booked by the SPC.
             * There is a nested INNER JOIN to ensure we are focusing on a specific DiagRepair Booking ID when doing this check.
             */
            String getUnbookedSPCParts =
                "SELECT PartForRepairBooking.PartID\n" + // Return all Part IDs
                "FROM PartForRepairBooking\n" + // That we have selected from Diagnostics and Repair
                "LEFT JOIN (\n" +
                "	PartForSPCRepair INNER JOIN SPCBooking\n" +
                "	ON PartForSPCRepair.SPCBookingID = SPCBooking.SPCBookingID\n" +
                "	AND SPCBooking.DiagRepBookingID = ?\n" + // Checking if the Part ID was booked by SPC for the specific DiagRep ID
                ") ON PartForRepairBooking.PartID = PartForSPCRepair.PartID\n" +
                "WHERE PartForRepairBooking.RepairBookingID = ?\n" + // Of course only checking against parts in this booking instance
                "AND PartForRepairBooking.IsForSPC = 1\n" + // That we selected for SPC Repair
                "AND PartForSPCRepair.PartID IS NULL"; // If NULL, means the SPC has not booked the part (due to nature of LEFT JOIN)
            PreparedStatement getUnbookedPartsStatement = con.prepareStatement(getUnbookedSPCParts);
            getUnbookedPartsStatement.setInt(1, ID); // Parts at SPC should be checked for this specific booking ID only.
            getUnbookedPartsStatement.setInt(2, ID); // Only should be checking the parts selected for SPC in THIS booking only.
            ResultSet unbookedPartIDs = getUnbookedPartsStatement.executeQuery();
            if(unbookedPartIDs.isClosed()) {
                this.complete = true;
                this.endDate = Utility.strToDateTime(db.getCurrentDateTime());
                this.updateBooking();
                Bill billForBooking = new Bill(this.ID);
                billForBooking.insertBill();
            } else {
                String log = "";
                Part part;
                while(unbookedPartIDs.next()) {
                    part = new Part(unbookedPartIDs.getInt(1));
                    log += "\nPart Name: "+part.getName();
                }
                throw new InProgressException("Booking cannot be completed since there are parts that have been selected for SPC Repair, which have not yet been booked or processed by the SPC Centre.\n\nThese are the unbooked parts:\n"+log);
            }
        }
    }
    
    /**
     * Marks the booking for sending a vehicle to the SPC Centre rather than repairing any of the parts.
     * This method deals with both selecting for SPC, and cancelling from SPC.
     * A vehicle cannot be sent to the SPC if the Garage has already processed any repairs/new parts for the vehicle.
     * @param send - whether to send the vehicle to SPC, or cancel it
     * @param force - whether or not to force the decision, or warn of a conflicting SPC booking
     * @throws SQLException - if any SQLite errors
     * @throws InProgressException - if there is an active SPC Vehicle booking
     */
    public void sendVehicleToSPC(boolean send, boolean force) throws SQLException, InProgressException {
        if(send && !this.vehicleForSPCRepair) {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String countPartsInBooking = "SELECT count(*) FROM PartForRepairBooking WHERE RepairBookingID = "+this.ID;
            PreparedStatement ps = con.prepareStatement(countPartsInBooking);
            ResultSet rs = ps.executeQuery();
            if(rs.getInt(1)>0) {
                // If there are already parts in the booking, don't allow sending the vehicle to SPC
                throw new InProgressException("The vehicle cannot be sent to the SPC Centre while the Garage is processing repairs or has already added parts to the booking. You must cancel/remove all associated parts before you can do this.");
            } else {
                String countPartsInVehicle = "SELECT count(*) FROM PartInVehicle WHERE VehicleID = "+this.getVehicleID();
                PreparedStatement ps3 = con.prepareStatement(countPartsInVehicle);
                ResultSet rs3 = ps3.executeQuery();
                if(rs3.getInt(1)>0) {
                    // Only allow sending the vehicle if it has at least one installed part
                    this.vehicleForSPCRepair = true;
                    this.updateBooking();
                } else {
                    // If vehicle doesn't have any installed parts
                    throw new IllegalArgumentException("This vehicle doesn't have any installed parts, therefore the SPC cannot process any repairs for the vehicle. You can only install new parts from the Garage.");
                }
            }
        } else if(!send && this.vehicleForSPCRepair) {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            // Try to find the vehicle SPC Booking ID and Completion Status (if at all booked)
            String getSPCBookingID = "SELECT SPCBookingID, ReturnStatus FROM SPCBooking WHERE DiagRepBookingID = ? AND IsBookingForVehicle = 1";
            PreparedStatement ps = con.prepareStatement(getSPCBookingID);
            ps.setInt(1, this.ID);
            ResultSet rs = ps.executeQuery();
            if(rs.isClosed()) {
                // If SPC did not yet book the Vehicle Repair, no deletion needed
                this.vehicleForSPCRepair = false;
                this.updateBooking();
            } else if(rs.getInt("ReturnStatus")==1) {
                // From this case onwards, the booking has already been made by the SPC
                // In this case, the booking is already complete
                // ASSUMPTION BY PRASHANTH - SPC BOOKING IS NOT DELETED (CUSTOMER STILL BILLED)
                this.vehicleForSPCRepair = false;
                this.updateBooking();
            } else if(!force) {
                // If SPC booked the vehicle, throw an initial warning
                throw new InProgressException("This vehicle has already been booked by the SPC Centre and is in progress. Cancelling this will DELETE the associated SPC booking. Are you sure you want to do this?");
            } else {
                // If user has accepted the notice, then delete the SPC booking
                String updateQuery = "DELETE FROM `SPCBooking` WHERE SPCBookingID = ?;";
                PreparedStatement ps2 = con.prepareStatement(updateQuery);
                ps2.setInt(1, rs.getInt(1)); // SPC Booking ID
                ps2.executeUpdate();
                this.vehicleForSPCRepair = false;
                this.updateBooking();
            }
        }
    }
    
    /**
     * Gets the repair time of hours spent by the mechanic.
     * @return integer - number of hours spent on repairs
     */
    public int getRepairTime() {
        return this.repairTime;
    }
    
    /**
     * Sets the repair time of hours spent by the mechanic.
     * Cannot be decremented, only incremented for obvious reasons.
     * @param hours
     */
    public void setRepairTime(int hours) {
        this.repairTime = Math.max(this.repairTime, hours);
    }
    
    public IntegerProperty BookingIDProperty() {
        return new SimpleIntegerProperty(ID);
    }
    
    /*public static void main(String[] args) throws SQLException {
        DiagRepair booking = new DiagRepair(1);
        try {
            booking.decrementCost(1.50);
        } catch(IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }*/
}
