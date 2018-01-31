package parts.logic;

import common.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import vehicles.logic.Vehicle;
/**
 * Class to create objects with information about parts that are installed.
 * Includes vehicle owner's first and last name because fields not accessible 
 * through vehicle object, and they are necessary when displaying information 
 * about the installed parts.
 * 
 * TODO change when first/last name can be accessed through vehicle
 * 
 * 
 * @author Mia
 */
public class InstalledPart{
   
    private Vehicle vehicle = null;
    private String ownerFirstName, ownerLastName;
    private String installDate; 
    private String warrantyEndDate;
    private Part part = null;
    
    /**
     * Installed part controller. Gets the installed part details from the database 
     * @param partID The id of the part to populate 
     */
    public InstalledPart(int partID){
        String query = "SELECT * FROM PartInVehicle, Part WHERE PartID=" + partID + " AND Part.ID=" + partID;
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            this.vehicle = new Vehicle(rs.getInt("VehicleID"));
            this.part = new Part(rs.getInt("PartID"));
            this.installDate = rs.getString("InstallDate");
            this.warrantyEndDate = Utility.getDateAfterYear(installDate);
            String getCustomerName = "SELECT Firstname, Surname FROM Vehicle, Customer WHERE VehicleID=" + this.vehicle.getID() 
                    + " AND Vehicle.CustomerID=Customer.CustomerID";
            rs = statement.executeQuery(getCustomerName);
            this.ownerFirstName = rs.getString("Firstname");
            this.ownerLastName = rs.getString("Surname");
        } catch (SQLException ex) {
            Logger.getLogger(InstalledPart.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Cannot get values to initialize Installed");
        }
    }
    
    /**
     * Returns part installed in vehicle 
     * 
     * @return Part installed in vehicle 
     */
    public Part getPart(){
        return this.part;
    }
    
    /**
     * Returns vehicle that part is installed in 
     *
     * @return Vehicle part installed in
     */
    public Vehicle getVehicle(){
        return this.vehicle;
    }
    
    /**
     * Returns date of part installation 
     * 
     * @return date in 'yyyy-mm-dd' form
     */
    public String getInstallDate(){
        return this.installDate;
    }
    
    /**
     * Checks if the part is in warranty. 
     * 
     * @return True if in warranty, false if not
     */
    public boolean checkWarranty(){
        Database db = Database.getInstance();
        String currentDate = db.getCurrentDate();
        
        Date current = Utility.convertStringToDate(currentDate, Database.INSIDE_DB_DATE_FORMAT);
        Date end = Utility.convertStringToDate(this.warrantyEndDate, Database.INSIDE_DB_DATE_FORMAT);
        // if the current date is after the end warranty date, it is false.
        // if the current date is equal to or before warranty end, it is true.
        if(current.compareTo(end) > 0){
            return false;
        }else{
            return true;
        }
    }
    
    /**
     * Gets the first name of the owner of the vehicle in which this part is installed
     * 
     * @return The owner's first name
     */
    public String getOwnerFirstName(){
        return this.ownerFirstName;
    }
    
    /**
     * Gets the last name of the owner of the vehicle in which this part is installed
     * 
     * @return The owner's last name 
     */
    public String getOwnerLastName(){
        return this.ownerLastName;
    }
    
    /**
     * Returns the warranty end date 
     * 
     * @return Warranty end date in form 'yyyy-mm-dd' 
     */
    public String getWarrantyEndDate(){
        return this.warrantyEndDate;
    }
    
    /**
     * Uninstalls part from vehicle. Contains logic to delete the part from necessary SPC/Diagnosis and Repair Bookings.
     * 
     * 1) If no active diagnosis and repair booking, simply uninstall
     * ************************ALL ELSE ASSUME ACTIVE DIAGNOSIS AND REPAIR BOOKING**********************
     * ***************If active diagnosis and repair booking, check if it is for vehicle****************
     * 2) If it is for vehicle, cannot uninstall parts because it is not at our shop and would cause conflicts with SPC module
     * 3) If there is a completed SPC booking, remove the part from the diagrep booking and leave SPC alone (or errors will arise with diagrep) 
     *    and SPC module wants SPC booking left to calculate bill
     * 4) If incomplete SPC booking, delete part from both bookings, and delete the SPC booking
     */
    public boolean uninstall(){
        boolean uninstalled = false;
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        
        // query tested and working properly
        String getActiveBooking = "SELECT DiagRepID, MIN(StartDate), sendVehicleToSPC FROM DiagRepairBooking WHERE VehicleID=" 
                + vehicle.getID() + " AND Completed = 0 AND StartDate <= '" + db.getCurrentDateTime() + "'";
       
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(getActiveBooking);
            String removePartFromVehicle = "DELETE FROM PartInVehicle WHERE PartID = " + this.part.getID() + " AND VehicleID=" + this.vehicle.getID();
            
            /* no result, so part is not in active diagnosis and repair bookings because there are no active bookings
             * part just deleted from installed part because we want to keep historical data about bookings */
            if(rs.isClosed() || rs.getInt("DiagRepID") == 0){// CASE 1
                if(rs.getInt("DiagRepID") ==0){System.out.println("WHY");}
                //System.out.println("There is no active diagrep booking");
                //System.out.println("The part is being removed from the vehicle and bookings are untouched");
                statement.executeUpdate(removePartFromVehicle);
                uninstalled = true;
            }else{// CASES 2-4
                System.out.println("There is an active diagrep booking");
                if(rs.getInt("sendVehicleToSPC") == 1){ //CASE 2
                    /*  If the vehicle is being sent to SPC, one cannot uninstall parts.
                    The vehicle is not at our shop, so it doesn't make sense to uninstall parts, 
                    and it prevents potential errors with deleting an SPC booking (we don't want this to happen
                    because then calculating the final bill is problematic)
                    */
                    System.out.println("The vehicle is being sent to SPC in this booking");
                }else{ // CASES 3-4 or not in SPC booking at all
                    String isPartInBooking = "SELECT * FROM PartForRepairBooking WHERE PartID=" + this.part.getID() + " AND RepairBookingID=" + rs.getInt("DiagRepID");
                    rs = statement.executeQuery(isPartInBooking);
                    // the part is in the booking
                    if(!rs.isClosed()){
                        PartInBooking partInBooking = new PartInBooking(rs.getInt("PartID"), rs.getInt("RepairBookingID"));
                        partInBooking.removeFromBooking();
                        statement.executeUpdate(removePartFromVehicle);
                        uninstalled = true;
                    
                    }else{ // the part is not in the booking and can simply be uninstalled
                        //System.out.println("The part is not in this booking");
                        statement.executeUpdate(removePartFromVehicle);
                        uninstalled = true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InstalledPart.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return uninstalled;
    }
}