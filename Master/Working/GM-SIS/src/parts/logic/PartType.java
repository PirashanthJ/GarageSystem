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
 *
 * @author Mia
 */
public class PartType {
    
    private String name, description;
    private int stock;
    private Double cost;
    private static final Database DB = Database.getInstance();
    
    /**
     * The constructor for the part type. Gets the information from the database.
     * 
     * @param name The name of the part type 
     */
    public PartType(String name){
        name = Utility.allowApostrophes(name);
        Connection connection = DB.getConnection();
        String query = "SELECT * FROM PartInfo WHERE Name = '" + name + "' ";
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            this.name = rs.getString("Name");
            this.description = rs.getString("Description");
            DecimalFormat df = new DecimalFormat("#.00");
            this.cost = Double.parseDouble(df.format(rs.getDouble("Cost")));
        } catch (SQLException ex) {
            Logger.getLogger(Part.class.getName()).log(Level.SEVERE, null, ex);
            // find better error to throw
            // need to throw error because constructor 
            throw new IllegalArgumentException("Cannot get values to initalize Part");
        }finally{
            Utility.closeStatementAndConnection(statement, connection);
        }
        this.stock = this.getStockLevel();
        
    }
    
    /**
     * Gets the stock level of the part type 
     * 
     * @return an integer value of the stock level of the part type 
     */
    private int getStockLevel(){
        int stock = -1;
                //search in part table for everything with partName and withdraw date null
        Connection conn = DB.getConnection();
        Statement statement = null;
        ResultSet rs;
        /* if withdraw date is null it means the part has not left the inventory yet
        and thus is in stock. */
        String query = "SELECT COUNT(*) AS TotalStock FROM Part WHERE Name='" + Utility.allowApostrophes(this.name) + 
                "' AND WithdrawDate IS NULL AND InstallDate IS NULL";
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            stock = rs.getInt("TotalStock");
        } catch (SQLException ex) {
            Logger.getLogger(PartInventory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }   
        return stock;
    }
        
    /**
     * gets the name of the part type
     * @return name of the part type 
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * get the stock of the part type
     * @return stock of the part type
     */
    public int getStock(){
        return this.stock;
    }
    
    /**
     * gets cost of the part type 
     * @return cost of the part type
     */
    public double getCost(){
        return this.cost;
    }
    
    /**
     * gets the description of the part type
     * @return description of the part type
     */
    public String getDescription(){
        return this.description;
    }
    
}
