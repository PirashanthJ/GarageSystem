/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parts.logic;

import common.Database;
import diagrep.logic.DiagRepair;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used in PartsForBookingView
 * @author Mia
 */
public class PartInBooking {
    
    Part part;
    int bookingID;
    boolean installed, repair, SPC, addNew, delete, chargedFor;
    
    /**
     * Constructor for part in booking, gets the part from the database. 
     * 
     * @param partID The id of the part 
     * @param bookingID The id of the booking
     */
    public PartInBooking(int partID, int bookingID){
        part = new Part(partID);
        this.bookingID = bookingID;
        delete = false;
        String query = "SELECT * FROM PartForRepairBooking WHERE PartID=" + partID + " AND RepairBookingID= " + bookingID;
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            if(rs.getInt("IsForSPC") == 1){
                SPC = true;
            }else{
                SPC = false;
            }
            if(rs.getInt("RepairOnly") == 1){
                repair = true;
            }else{
                repair = false;
            }
            if(rs.getInt("NewPart") == 1){
                addNew = true;
            }else{
                addNew = false;
            }
            if(rs.getInt("IsChargedFor") == 1){
                chargedFor = true;
            }else{
                chargedFor = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartInBooking.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Cannot get values to initalize Part in Booking");
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
    
    /**
     * Removes this part from a diagnosis and repair booking. 
     * 
     * 1) If it is a part that was added and installed in this booking, it is removed from the booking, it's cost deducted (if it was charged for), 
     * the part is uninstalled, and then put back in inventory
     * 2) If it is a part that is being repaired, simply remove it from the booking.
     * 3) If it is a part that was sent to SPC and completed, simply remove from the diagrep booking
     * 4) If it is a part that was sent to SPC and incomplete, remove the part from both bookings and delete the SPC booking
     * IMPORTANT - because this part is associated to a diagrep booking, it will not be associated to a vehicle SPC booking. 
     * Therefore, there will only be one part per SPC booking, meaning the queries for interacting with SPC bookings will be slightly different. 
     */
    public void removeFromBooking(){
        String removeFromBooking = "DELETE FROM PartForRepairBooking WHERE PartID=" + this.part.getID() + " AND RepairBookingID=" + this.bookingID;
        String getSPCBooking = "SELECT SPCBooking.SPCBookingID, ReturnStatus FROM SPCBooking, PartForSPCRepair WHERE DiagRepBookingID=" + this.bookingID + 
                " AND PartID=" + this.part.getID();
        
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        ResultSet rs;
        try {
            statement = conn.createStatement();
            // cases 3 and 4
            if(SPC){
                rs = statement.executeQuery(getSPCBooking);
                // case 4
                if(!rs.isClosed() && rs.getInt("ReturnStatus") != 1){ // if closed, the SPC booking wasn't made yet so nothing to remove
                    String removeFromSPC = "DELETE FROM PartForSPCRepair WHERE PartID=" + this.part.getID() + " AND SPCBookingID=" + rs.getInt("SPCBookingID");
                    String deleteSPCBooking = "DELETE FROM SPCBooking WHERE SPCBookingID=" + rs.getInt("SPCBookingID");
                    statement.executeUpdate(removeFromSPC);
                    statement.executeUpdate(deleteSPCBooking);
                }//else doing nothing is case 3
            }
            // case 1
            if(addNew){
                DiagRepair booking = new DiagRepair(bookingID);
                if(chargedFor){
                    System.out.println(booking.getBookingID());
                    booking.decrementCost(this.part.getCost());
                    System.out.println(this.part.getCost());
                }
                String removePartFromVehicle = "DELETE FROM PartInVehicle WHERE PartID=" + this.part.getID();
                statement.executeUpdate(removePartFromVehicle);
                this.part.putBackInInventory();
            }
            // all cases
            statement.executeUpdate(removeFromBooking);
        } catch (SQLException ex) {
            Logger.getLogger(PartInBooking.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
        
    /**
     * The part object for this part 
     * 
     * @return The part object
     */
    public Part getPart(){
        return part;
    }
    
    /**
     * Whether or not this part is being repaired in the booking 
     * 
     * @return true if for repair, false otherwise
     */
    public boolean getRepair(){
        return repair;
    }
    
    /**
     * Whether or not this part is sent/being sent to SPC
     * 
     * @return true if for SPC, false otherwise
     */
    public boolean getSPC(){
        return SPC;
    }
    
    /**
     * Whether or not this part should be deleted from the booking 
     * 
     * @return true if it is supposed to be deleted, false if otherwise 
     */
    public boolean getDelete(){
        return delete;
    }
    
    /**
     * Whether or not the part should 
     * 
     * @return true if adding new, false if otherwise
     */
    public boolean getAddNew(){
        return addNew;
    }
}
