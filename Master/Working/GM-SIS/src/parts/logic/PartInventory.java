package parts.logic;

import common.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mia
 */
public class PartInventory {
    private static PartInventory instance;
    private static final Database DB = Database.getInstance();
    
    private PartInventory(){
        // prevents public creation of partInventories
    }
    
    /**
     * gets the instance of part inventory 
     * 
     * @return part inventory instance 
     */
    public static PartInventory getInstance(){
        if(instance == null){
            instance = new PartInventory();
        }
        return instance;
    }
    
    /**
     * Finds and returns parts that are installed on vehicle given by full or partial 
     * vehicle registration number. 
     * 
     * Meets requirement 8. Along with {@link #searchForPart(java.lang.String, java.lang.String)} meets requirement 12
     * @param registrationNo The partial or full registration number of a vehicle whose parts
     * need to be found
     * @return ArrayList containing Part instances that are in vehicles with registration
     * numbers similar to or equal to the given registration number.
     */
    public List<InstalledPart> searchForPart(String registrationNo){
        List<InstalledPart> parts = new ArrayList<>();
        Connection conn = DB.getConnection();
        Statement statement = null;
        
        String query = "SELECT PartInVehicle.PartID FROM PartInVehicle, VehicleInfo WHERE VehicleInfo.Registration LIKE '%" + registrationNo + "%'" 
                + " AND PartInVehicle.VehicleID=VehicleInfo.VehicleID";
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()){
                parts.add(new InstalledPart(rs.getInt("PartID")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return parts;
    }
    
    /**
     * Searches for an installed part by customer name 
     * Along with {@link #searchForPart(int)} meets requirement 12
     * 
     * @param lastName
     * @param firstName 
     * @return  
     */
    public List<InstalledPart> searchForPart(String firstName, String lastName){
        List<InstalledPart> parts = new ArrayList<>();
        Connection conn = DB.getConnection();
        Statement statement = null;
        ResultSet rs = null;
        firstName = Utility.allowApostrophes(firstName);
        lastName = Utility.allowApostrophes(lastName);
        // gets all customers with names similar to the ones being searched for
        String getCustomerID = "";
        if(!firstName.equals("") && !lastName.equals("")){
            getCustomerID = "SELECT CustomerID FROM Customer WHERE " + 
                "FirstName LIKE '%" + firstName + "%' OR Surname LIKE '% " + lastName + "%'";
        }else if(!firstName.equals("")){
            getCustomerID = "SELECT CustomerID FROM Customer WHERE " + 
                "FirstName LIKE '%" + firstName + "%'";
        }else{
            getCustomerID = "SELECT CustomerID FROM Customer WHERE " + 
                "Surname LIKE '%" + lastName + "%'";
        }
        
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(getCustomerID);
            ArrayList<Integer> customerIDs = new ArrayList<>();
            while(!rs.isClosed() && rs.next()){
                customerIDs.add(rs.getInt("CustomerID"));
            }
            
            ArrayList<Integer> vehicleIDs = new ArrayList<>();
            // gets the vehicle(s) for each customer
            for(int i = 0; i < customerIDs.size(); i++){
                String getVehicleID = "SELECT VehicleID FROM Vehicle WHERE Vehicle.CustomerID = " + customerIDs.get(i);
                rs = statement.executeQuery(getVehicleID);
                while(!rs.isClosed() && rs.next()){
                    vehicleIDs.add(rs.getInt("VehicleID"));
                }
            }
           
            // gets the installed part(s) for each vehicle
            for(int i = 0; i < vehicleIDs.size(); i++){
                String query = "SELECT PartID FROM PartInVehicle WHERE VehicleID = " + vehicleIDs.get(i);
                rs = statement.executeQuery(query);
                while(!rs.isClosed() && rs.next()){
                    parts.add(new InstalledPart(rs.getInt("PartID")));
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return parts;
    }
    
    /**
     * Searches for a part by name
     * @param name the name (full or partial) of the part to search for 
     * 
     * @return An arraylist of all part types with similar names
     */
    public ArrayList<PartType> searchForPartByName(String name){
        name = Utility.allowApostrophes(name);
        ArrayList<PartType> parts = new ArrayList<>();
        Connection conn = DB.getConnection();
        Statement statement = null;
        ResultSet rs = null;
        String query = "";
        if(!name.trim().equals("")){
            query = "SELECT Name FROM PartInfo WHERE Name LIKE '%" + name + "%'";
        }else{
            return parts;
        }
       
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while(rs.next()){
                parts.add(new PartType(rs.getString("Name")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return parts;
    }
    
    /**
     * Gets the stock level for a specific type of part. 
     * 
     * meets requirement 4 
     * 
     * @param partName Name of part to check stock of
     * @return The number of the type of part in stock
     */
    public int getStockLevel(String partName){
        int totalStock = 0;
        partName = Utility.allowApostrophes(partName);
        //search in part table for everything with partName and withdraw date null
        Connection conn = DB.getConnection();
        Statement statement = null;
        ResultSet rs;
        /* if withdraw date is null it means the part has not left the inventory yet
        and thus is in stock. */
        String query = "SELECT COUNT(*) AS TotalStock FROM Part WHERE Name='" + partName + 
                "' AND WithdrawDate IS NULL AND InstallDate IS NULL";
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            totalStock = rs.getInt("TotalStock");
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }   
        return totalStock;
    }
    
    /**
     * adds more stock of an existing part type to the inventory 
     * 
     * @param name the name of the part to add
     * @param quantity the quantity to add
     */
    public void addExistingPartTypeToInventory(String name, int quantity){
        /* ensures insertion of part/unique checks case insensitive by standardizing
        case*/
        name = Utility.capitalizeFirstLetterOfWords(name);
        name = Utility.allowApostrophes(name);
        String currentDate = DB.getCurrentDate();
        // done via transaction to improve performance
        // code by Jamie Cook (StackOverflow) - Is it possible to insert multiple rows at a time in an SQLite database
        String rowsToInsert = Utility.repeatString("INSERT INTO Part('Name', 'ArrivalDate') VALUES('" + name + "', date('" + currentDate + "'));\n", quantity);
        String insert = "BEGIN TRANSACTION;\n" + rowsToInsert + "COMMIT;";
        Connection conn = DB.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.executeUpdate(insert);
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
    
    /**
     * Inserts a new Part type (part with a new name) into the inventory. 
     * 
     * @param name Name of part
     * @param description Description of part
     * @param cost Cost of part
     * @param quantity Number of parts to add
     */
    public boolean addNewPartTypeToInventory(String name, String description, double cost, int quantity){
        /* convert name to standard format to ensure insertion and checks to see that name is
        unique is case insensitive */
        name = Utility.capitalizeFirstLetterOfWords(name);
        name = Utility.allowApostrophes(name);
        description = Utility.allowApostrophes(description);
        
        if(existsInSystem(name)){
            return false;
        }else{
            String insertInfo = "INSERT INTO 'PartInfo'('Name', 'Description', 'Cost') " 
                + "VALUES('" + name + "', '" + description + "', " + cost + ")";
            Connection conn = DB.getConnection();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                statement.executeUpdate(insertInfo);
            } catch (SQLException ex) {
                // TODO Better Error Handling
                Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                Utility.closeStatementAndConnection(statement, conn);
            }
            // after inserting new type into partinfo table insert instance of part into part table
            addExistingPartTypeToInventory(name, quantity);
            return true;
        }
    }
    
    /**
     * Gets recently added parts 
     * Along with {@link #getRecentWithdraws(java.util.Date, int)} meets requirement 5
     * 
     * @param fromDate the date to search from 
     * @param toDate the date to search to 
     * @return returns the recent arrivals 
     */
    public ArrayList<Part> getRecentAdditions(String fromDate, String toDate){
        ArrayList<Part> recentlyAddedParts = new ArrayList<>();
            
        Connection conn = DB.getConnection();
        Statement statement = null;
        ResultSet rs;
        String query = "SELECT ID FROM Part WHERE ArrivalDate BETWEEN date('" 
                + fromDate + "') AND date('" + toDate + "')";
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while(rs.next()){
                recentlyAddedParts.add(new Part(rs.getInt("ID")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return recentlyAddedParts;
    }
    
    /**
     * Gets recently withdrawn parts 
     * Along with {@link #getRecentAdditions(java.util.Date, int)} meets requirement 5
     * 
     * @param fromDate
     * @param toDate
     * @return the recent withdrawals
     */
    public ArrayList<Part> getRecentWithdraws(String fromDate, String toDate){
        ArrayList<Part> recentlyRemovedParts = new ArrayList<>();

        Connection conn = DB.getConnection();
        Statement statement = null;
        ResultSet rs;
        String query = "SELECT ID FROM Part WHERE WithdrawDate BETWEEN date('" 
                + fromDate + "') AND date('" + toDate + "')";
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while(rs.next()){
                recentlyRemovedParts.add(new Part(rs.getInt("ID")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return recentlyRemovedParts;
    }
    
    /**
     * Checks to see if the part type exists in the system already 
     * 
     * @param partName The name of the part to check
     * @return True if it exists, false if it doesn't
     */
    public boolean existsInSystem(String partName){
        Database db = Database.getInstance();
        String query = "SELECT Name From PartInfo WHERE Name='" + partName + "'";
        Connection conn = db.getConnection();
        Boolean exists = false;
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            // if this part type does not exist this query will be empty
            // that means that the result set will be cloed
            if(!rs.isClosed()){
                exists = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Part.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return exists;
    }
    
    
    /**
     * Withdraws a given part from the inventory. Sets the withdraw date equal to the current date
     * 
     * @param partName The name of the part to withdraw
     * @return the ID of the part withdrawn, if there is no stock -1 returned.
     */
    public int withdraw(String partName){
        int partID = -1;
        partName = Utility.allowApostrophes(partName);
        // returns all partID's with given name that arent yet withdrawn from the inventory
        String query = "SELECT ID FROM Part WHERE (WithdrawDate IS NULL AND InstallDate IS NULL) AND Name='" + 
                partName + "'";
        Connection conn = DB.getConnection();
        Statement statement = null;
        ResultSet rs;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            // if the result set is closed, then there is no stock for this item, so -1 returned
            if(!rs.isClosed()){
                // arbitrarily withdraw the first available part
                partID = rs.getInt("ID");
                String setWithdrawDate = "UPDATE Part SET WithdrawDate=date('" + DB.getCurrentDate() + "') WHERE ID=" + partID;
                statement.executeUpdate(setWithdrawDate);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return partID;
    }

    /**
    * Updates the name of the part to the new name given.
    * 
    * IMPORTANT - Updates the name for all parts with same name
    * 
    * @param oldName The name of the part type originally.
    * @param newName The new name of the part type.
    */
    public void changePartTypeName(String oldName, String newName){
        oldName = Utility.capitalizeFirstLetterOfWords(oldName);
        oldName = Utility.allowApostrophes(oldName);
        newName = Utility.capitalizeFirstLetterOfWords(newName);
        newName = Utility.allowApostrophes(newName);
        Connection conn = DB.getConnection();
        Statement statement = null;
        // will automatically update foreign key in Part table because of ON UPDATE CASCADE
        String updateName = "UPDATE PartInfo SET Name='" + newName + "' WHERE Name='" + oldName + "'";
        try {
            statement = conn.createStatement();
            statement.executeUpdate(updateName);
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
    
    /**
     * updates the description of a part type
     * @param name the name of the part type to update
     * @param newDescription the new description of the part type
     */
    public void changePartTypeDescription(String name, String newDescription){
        name = Utility.capitalizeFirstLetterOfWords(name);
        name = Utility.allowApostrophes(name);
        newDescription = Utility.allowApostrophes(newDescription);
        Connection conn = DB.getConnection();
        Statement statement = null;
        // will automatically update foreign key in Part table because of ON UPDATE CASCADE
        String updateName = "UPDATE PartInfo SET Description='" + newDescription + "' WHERE Name='" + name + "'";
        try {
            statement = conn.createStatement();
            statement.executeUpdate(updateName);
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
    
    /**
     * updates the cost of a part type 
     * 
     * @param name the name of the part type to update 
     * 
     * @param cost the new cost of the part type
     */
    public void changePartTypeCost(String name, double cost){
        name = Utility.capitalizeFirstLetterOfWords(name);
        name = Utility.allowApostrophes(name);
        Connection conn = DB.getConnection();
        Statement statement = null;
        // will automatically update foreign key in Part table because of ON UPDATE CASCADE
        String updateName = "UPDATE PartInfo SET Cost=" + cost + " WHERE Name='" + name + "'";
        try {
            statement = conn.createStatement();
            statement.executeUpdate(updateName);
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
    
    /**
     * Gets all of the part types in the inventory 
     * 
     * @return an arraylist of all of the part types in inventory 
     */
    public List<PartType> getAllPartTypes(){
        String query = "SELECT Name FROM PartInfo GROUP BY Name";
        Connection conn = DB.getConnection();
        Statement statement = null;
        List<PartType> allPartTypes = new ArrayList<>();
        List<Integer> partTypeIDs = new ArrayList<>();
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()){
                allPartTypes.add(new PartType(rs.getString("Name")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
 
        return allPartTypes;
    }
    
    
    /**
     * Deletes a quantity of parts in inventory 
     * 
     * @param name The name of the part type to delete stock of 
     * 
     * @param quantityToDelete The quantity of stock to delete
     */
    public void deleteQuantity(String name, int quantityToDelete){
        name = Utility.allowApostrophes(name);
        String query = "SELECT ID FROM Part WHERE WithdrawDate IS NULL AND InstallDate IS NULL AND Name='" + name + "'";
        Connection conn = DB.getConnection();
        Statement statement = null;
        String deletePart = "";
        try {
            statement = conn.createStatement();
            ArrayList<Integer> partIDs = new ArrayList<>();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()){
                partIDs.add(rs.getInt("ID"));
            }
            
            String rowsToDelete = "";
            
            // if tthe number of parts to delete is greater than the number of parts left in stock
            // just delete the number of parts left in stock
            if(partIDs.size() <= quantityToDelete){
                for(int i = 0; i < partIDs.size(); i++){
                    rowsToDelete += "Delete FROM Part WHERE ID= " + partIDs.get(i) + ";\n";
                }
            }else{ // if quantity to delete less than remaning parts, delete quantity
                for(int i = 0; i < quantityToDelete; i++){
                    rowsToDelete += "Delete FROM Part WHERE ID= " + partIDs.get(i) + ";\n";
                }
            }
            
            String delete = "BEGIN TRANSACTION;\n" + rowsToDelete + "COMMIT;";
            statement.executeUpdate(delete);

        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }        
    }
    
}