
package parts.logic;


import common.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Part object with name, description, cost, and ID.
 * @author Mia
 */
public class Part {
    // instance variables - meets requirements 3, 7
    private final String name, description;
    private final int ID;
    private double cost;
    private boolean installed;
    private static final Database DB = Database.getInstance();
    
    /**
     * Initializes Part object with information from database. Throws illegal argument
     * exception if there is an SQLite error preventing part from being created properly.
     * 
     * @param partIDInDatabase ID of the part to be created
     */
    public Part(int partIDInDatabase){
        Connection connection = DB.getConnection();
        String query = "SELECT Name, ID FROM Part WHERE ID=" + partIDInDatabase;
        
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            this.name = rs.getString("Name");
            this.ID = rs.getInt("ID");
            String getInfo = "SELECT Description, Cost FROM PartInfo WHERE Name='"+ Utility.allowApostrophes(this.name) + "'";
            rs = statement.executeQuery(getInfo);
            this.description = rs.getString("Description");
            DecimalFormat df = new DecimalFormat("#.00");
            this.cost = Double.parseDouble(df.format(rs.getDouble("Cost")));
            
            // checks to see if part is installed by checking whether it is in partInVehicleTable
            String isInstalled = "SELECT * FROM PartInVehicle WHERE PartID=" + partIDInDatabase;
            rs = statement.executeQuery(isInstalled);
            if(!rs.isClosed()){
                installed = true;
            }else{
                installed = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Part.class.getName()).log(Level.SEVERE, null, ex);
            // find better error to throw
            // need to throw error because constructor 
            throw new IllegalArgumentException("Cannot get values to initalize Part");
        }finally{
            Utility.closeStatementAndConnection(statement, connection);
        }
    }
    
    /**
     * Associates part with vehicle after withdrawing it from inventory. Sets the install/withdraw date as current day 
     * @param vehicleID ID of vehicle to install part in
     * @return returns true if installing was successful, false otherwise
     */
    public boolean install(int vehicleID){
        Database db = Database.getInstance();
        Connection connection = db.getConnection();
        boolean success = false;
        String getNumberOfParts = "SELECT count(*) AS partsInVehicle FROM PartInVehicle WHERE VehicleID=" + vehicleID;
        String insertIntoVehicle = "INSERT INTO PartInVehicle VALUES("  + this.ID + ", " + vehicleID + ")";
        String setInstallDate = "UPDATE Part SET InstallDate=date('" + db.getCurrentDate() + "') WHERE ID =" + this.ID;

        Statement statement = null;
        
        try{
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(getNumberOfParts);
            if(rs.getInt("partsInVehicle") < 10){
                int result = statement.executeUpdate(insertIntoVehicle);
                // sets install date only if the part was sucessfully installed
                if(result > 0){
                    statement.executeUpdate(setInstallDate);
                }
                success = true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(Part.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, connection);
        }
        return success;
    }
    
    /**
     * If a part was withdrawn to install on a vehicle during a booking, but was 
     * later removed from the booking before it completed, the part must be put back in the inventory.
     * 
     * Given the way inventory is calculated and parts are withdrawn from it, putting a part back in inventory
     * means setting withdraw and install date for that part back to null. 
     * 
     * IMPORTANT: The part must still be uninstalled from the vehicle and this method should only be called
     * after successfully uninstalling a part
     */
    public void putBackInInventory(){
        String unWithdraw = "UPDATE Part SET WithdrawDate = null, InstallDate = null WHERE ID=" + this.ID;
        Connection conn = DB.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.executeUpdate(unWithdraw);
        } catch (SQLException ex) {
            Logger.getLogger(Part.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
    
    /**
     * Returns the name of the Part instance
     * @return Name of Part
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Returns the description of the Part instance
     * @return Description of Part
     */
    public String getDescription(){
        return this.description;
    }
    
    /**
     * Returns the ID of the Part instance
     * @return ID of Part
     */
    public int getID(){
        return this.ID;
    }
    
    /**
     * Returns the Cost of the Part instance
     * @return Cost of Part
     */
    public double getCost(){
        return this.cost;
    }
    
    /**
     * Check whether or not the part is installed on a vehicle
     * 
     * @return true if the part is installed, false if it is not 
     */
    public boolean isInstalled(){
        return this.installed;
    }
   
}