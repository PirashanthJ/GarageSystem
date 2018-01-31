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
import parts.logic.Part;

/**
 * This class will only be accessed by DiagRepair.
 * @author adamlaraqui
 */
public class PartForRepairBooking {
    
    private final int BookingID;
    private final Part PART;
    private boolean inBooking;
    private boolean repairOnly;
    private boolean newPart;
    private boolean isForSPC;
    private boolean repairChanged = false;
    
    // TO DO
    // If ordering a new part, we need to add it's cost to the booking
    // If repairing a part ourselves, we need to add to add our fixed fee
    // If sending a part to SPC, we need to charge a fee (for delivery costs)
    
    // We should only allow adding a new part type to booking if it's not already part of it
    // If a Brake with ID 7 (installed) is in in booking, don't allow user to add/reserve new Brake with ID 8
    
    /**
     * Get instance of the PartForRepairBooking object.
     * No default actions are applied using this constructor.
     * You can access the part, repair type and whether it's in the booking.
     * @param partID ID of the part in the booking
     * @param bookingID ID of the booking which you are interested in
     * @throws java.sql.SQLException if any database errors
     * @throws IllegalArgumentException if the given part is not in the given booking (or either doesn't exist)
     */
    public PartForRepairBooking(int partID, int bookingID) throws SQLException, IllegalArgumentException {
        /**
         * CHECK EXISTING STATUS OF PART IN BOOKING.
         * Here, we check if part is already in some form of repair in THIS booking
         * This is what's used to set the instance variables, since they accurately represent the current DB data
         */
        this.PART = new Part(partID); // We will possibly need cost of the part etc
        this.BookingID = bookingID;
        ResultSet getStatusResult = checkPartInBooking(partID, bookingID);
        if(getStatusResult.next()) {
            System.out.println("Part ID "+partID+" was found in DB for this booking\nPart Name: "+this.PART.getName());
            // If the Part ID was found in the booking, then set instance variables
            // (since this would be an accurate represenatation of the stored data)
            // We don't know yet if we can change the booking to what the new selection is (from params)
            this.inBooking = true;
            this.repairOnly = (getStatusResult.getInt("RepairOnly")==1);
            this.newPart = (getStatusResult.getInt("NewPart")==1);
            this.isForSPC = (getStatusResult.getInt("IsForSPC")==1);
        } else {
            this.inBooking = false;
            this.repairOnly = false;
            this.newPart = false;
            this.isForSPC = false;
            System.out.println("Part ID " + partID + " not found\nPart Name: " + this.PART.getName());
        }
    }
    
    private void updateDB() throws SQLException {
        /**
         * [IF NOT IN BOOKING YET] Will insert part into the Part-Booking table
         * [IF IN BOOKING, AND CHANGED] Will update the part in the Part-Booking table
         * [IF IN BOOKING, AND NO REPAIRS] Will delete the part from the Part-Booking table
         */
        if(this.inBooking) { // If part already associated to the booking
            if(!this.repairOnly && !this.newPart && !this.isForSPC) {
                // If part no longer has any repair type selected
                this.deletePartFromBooking();
            } else if (this.repairChanged) {
                // If repair solution was changed
                this.updatePartInBooking();
            } else {
                // If there were no changes to the booking
                System.out.println("No Changes");
            }
        } else {
            this.addPartToBooking();
        }
        this.repairChanged = false;
        System.out.println("Constructor Fully Executed for Part ID: "+PART.getID());
    }
    
    /**
     * Gets the result set of an INNER JOIN query to get all repair statuses.
     * @param partID ID of the Part
     * @param bookingID ID of the DiagRepair booking
     * @return ResultSet of the search query
     * @throws SQLException 
     */
    private ResultSet checkPartInBooking(int partID, int bookingID) throws SQLException {
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        String searchQuery = "SELECT RepairOnly, NewPart, IsForSPC FROM PartForRepairBooking WHERE PartForRepairBooking.PartID = ? AND PartForRepairBooking.RepairBookingID = ?";
        PreparedStatement getStatusQuery = con.prepareStatement(searchQuery);
        getStatusQuery.setInt(1, partID);
        getStatusQuery.setInt(2, bookingID);
        return getStatusQuery.executeQuery();
    }
    
    /**
     * Removes a part from the booking.
     * @throws SQLException if any SQLite errors occur on deletion
     */
    private void deletePartFromBooking() throws SQLException {
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        PreparedStatement updateStatement;
        updateStatement = con.prepareStatement("DELETE FROM `PartForRepairBooking` WHERE PartID = ? AND RepairBookingID = ?");
        updateStatement.setInt(1, this.PART.getID()); // Part ID
        updateStatement.setInt(2, this.BookingID); // Booking ID
        System.out.println("Part In Booking : TO BE DELETED");
        this.inBooking = false;
        updateStatement.executeUpdate();
    }
    
    /**
     * Updates a part in the booking.
     * @throws SQLException if any SQLite errors occur on update
     */
    private void updatePartInBooking() throws SQLException {
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        PreparedStatement updateStatement;
        updateStatement = con.prepareStatement("UPDATE `PartForRepairBooking` SET IsForSPC = ?, RepairOnly = ?, NewPart = ?, IsChargedFor = ? WHERE PartID = ? AND RepairBookingID = ?");
        updateStatement.setInt(1, ((this.isForSPC)?1:0)); // Is for SPC?
        updateStatement.setInt(2, ((this.repairOnly)?1:0)); // Is for repair only?
        updateStatement.setInt(3, ((this.newPart)?1:0)); // Has been added?
        updateStatement.setInt(4, 0); // Anything updated from DiagRepair is not charged for directly
        updateStatement.setInt(5, this.PART.getID()); // Part ID
        updateStatement.setInt(6, this.BookingID); // Booking ID
        System.out.println("Part In Booking : TO BE UPDATED");
        updateStatement.executeUpdate();
    }
    
    /**
     * Adds a unique part type to the booking.
     * @throws SQLException if any SQLite errors occur on insertion
     */
    private void addPartToBooking() throws SQLException {
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        PreparedStatement updateStatement;
        updateStatement = con.prepareStatement("INSERT INTO `PartForRepairBooking` (PartID, RepairBookingID, IsForSPC, RepairOnly, NewPart, IsChargedFor) VALUES (?,?,?,?,?,?)");
        updateStatement.setInt(1, this.PART.getID()); // Part ID
        updateStatement.setInt(2, this.BookingID); // Part ID
        updateStatement.setInt(3, ((this.isForSPC)?1:0)); // Is for SPC?
        updateStatement.setInt(4, ((this.repairOnly)?1:0)); // Is for repair only?
        updateStatement.setInt(5, ((this.newPart)?1:0)); // Part added?
        updateStatement.setInt(5, 0); // Anything added from DiagRepair is not charged for directly
        this.inBooking = true;
        System.out.println("Part NOT In Booking : TO BE INSERTED");
        updateStatement.executeUpdate();
    }
    
    /**
     * Marks the part in booking for "SPC Repair".
     * @throws java.sql.SQLException
     * @throws IllegalArgumentException if trying to repair a part that was already installed
     */
    public void setSPCRepair() throws SQLException, IllegalArgumentException {
        if(!this.inBooking) {
            this.isForSPC = true;
            this.updateDB();
        } else if(this.repairOnly) { // If in booking, and was for repair only
            this.repairOnly = false;
            this.isForSPC = true;
            this.repairChanged = true;
            this.updateDB();
        } else if(this.newPart) { // If in booking, and new part was installed
            throw new IllegalArgumentException("This part was already installed as new within this booking. There's no need for an SPC Repair.");
        } else if(!this.isForSPC) { // If in booking, but no repairs were chosen on DB
            this.isForSPC = true;
            this.repairChanged = true;
            this.updateDB();
        } // Else, no change
    }
    
    /**
     * Marks the part in booking for "Repair Only".
     * @param force whether to force the decision
     * @throws java.sql.SQLException
     * @throws diagrep.logic.InProgressException if trying to cancel an SPC booking (without forcing)
     * @throws IllegalArgumentException if trying to cancel a part that was already installed during the booking
     */
    public void setRepairOnly(boolean force) throws SQLException, InProgressException, IllegalArgumentException {
        if(!this.inBooking) {
            this.repairOnly = true;
            this.updateDB();
        } else if(this.isForSPC) { // If in booking, and was for SPC
            try {
                this.cancelSPC(force);
                this.isForSPC = false;
                this.repairOnly = true;
                this.repairChanged = true;
                this.updateDB();
            } catch(InProgressException ex) { // If action wasn't forced, and part was already booked by SPC
                throw new InProgressException("You can't repair this part in the Garage since it has already been booked by the SPC Centre. Forcing this change will delete the associated SPC booking. Do you want to proceed?");
            }
        } else if(this.newPart) { // If in booking, and new part was installed
            throw new IllegalArgumentException("This part was already installed as new within this booking. There's no need to repair it in the same booking.");
        } else if(!this.repairOnly) { // If in booking, but no repairs were chosen on DB
            this.repairOnly = true;
            this.repairChanged = true;
            this.updateDB();
        } // Else, no change
    }
    
    public void removeFromBooking(boolean force) throws InProgressException, SQLException {
        if(!this.inBooking) return;
        if(this.newPart) throw new IllegalArgumentException("Since this part has already been installed as part of this booking, it cannot be removed unless it is uninstalled.");
        this.repairOnly = false;
        if(this.isForSPC)
            try {
                this.cancelSPC(force);
            } catch (InProgressException ex) {
                throw new InProgressException("Part could not be removed from booking since it has already been booked by the SPC Centre. Forcing this change will delete the associated SPC booking. Do you want to proceed?");
            }
        this.isForSPC = false;
        this.updateDB();
    }
    
    private void cancelSPC(boolean force) throws SQLException, InProgressException {
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        String checkIfSPCPartBooked = "SELECT SPCBooking.SPCBookingID, SPCBooking.ReturnStatus "
                                    + "FROM PartForSPCRepair "
                                    + "INNER JOIN SPCBooking "
                                    + "ON PartForSPCRepair.SPCBookingID = SPCBooking.SPCBookingID "
                                    + "WHERE PartForSPCRepair.PartID = ? " // For the specific part ID
                                    + "AND SPCBooking.DiagRepBookingID = ?" // In the specific DiagRep Booking
                                    + "AND SPCBooking.IsBookingForVehicle = 0"; // Which is for a single part, not vehicle
        PreparedStatement SPCBookingsCount = con.prepareStatement(checkIfSPCPartBooked);
        SPCBookingsCount.setInt(1, this.PART.getID());
        SPCBookingsCount.setInt(2, this.BookingID);
        ResultSet partsBookedWithSPC = SPCBookingsCount.executeQuery();
        Boolean partBookedWithSPC = (!partsBookedWithSPC.isClosed());
        if(partBookedWithSPC) {
            // If the part is on the SPC's system (booked)
            boolean SPCBookingComplete = ( partsBookedWithSPC.getInt("ReturnStatus")==1 );
            if(!SPCBookingComplete && !force) {
                // Not complete yet; inform user
                throw new InProgressException();
            } else if(!SPCBookingComplete && force) {
                // Delete incomplete SPC booking (decision was forced)
                String deleteBookingFromSPC = "DELETE FROM SPCBooking WHERE SPCBookingID = ? AND ReturnStatus = 0"; // Check again that it is still incomplete
                PreparedStatement deleteSPCBooking = con.prepareStatement(deleteBookingFromSPC);
                deleteSPCBooking.setInt(1, partsBookedWithSPC.getInt("SPCBookingID"));
                deleteSPCBooking.executeUpdate();
                System.out.println("Deleted SPC Booking");
            } // Else, booking already complete [Customer still billed + historical reference]
        } // Else, part not yet booked with SPC [No action needed]
    }
    
    /**
     * Determines if the part is for repair only.
     * @return true if for repair only, false otherwise
     */
    public boolean isForRepair() {
        return this.repairOnly;
    }
   
    /**
     * Determines if the part has been installed during the booking.
     * Parts can only be installed/un-installed from the parts module.
     * @return true if installed in THIS booking, false otherwise
     */
    public boolean isNewPart() {
        return this.newPart;
    }

    /**
     * Determines if the part has been selected for SPC repair.
     * It does not imply that the SPC has already booked it.
     * @return 
     */
    public boolean isForSPC() {
        return this.isForSPC;
    }
    
    /**
     * Returns the selected part which exists in the booking.
     * @return 
     */
    public Part getPartInBooking() {
        return this.PART;
    }
}
