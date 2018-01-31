package vehicles.logic;

import common.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Ilias
 * You need to create a vehicle object first to be able to use this correctly
 *
 * Requirements involved: 2, 3
 */
public class Warranty {

    private String company, address, expiry;
    public int wID;

    /**
     * Creates a new warranty and associates it to a vehicle with the given vehicleID
     *
     * @param company The company name
     * @param address The address of the warranty company
     * @param expiry The expiry date of the warranty
     * @param vehicleID The vehicleID of the vehicle that is getting this warranty.
     */
    public Warranty(String company, String address, String expiry, int vehicleID) {

        this.company = company;
        this.address = address;
        this.expiry  = expiry;

        Database DB = Database.getInstance();
        Connection connection = DB.getConnection();

        String dbQuery =    "INSERT INTO Warranty (nameOfCompany, addressOfCompany, dateOfExpiry, inWarranty) " +
                            "VALUES ('"+ company +"', '"+ address +"', '"+ expiry +"', '1')";

        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(dbQuery);

            this.wID = Utility.getLatestID("Warranty"); // Gets the latest created warranty.

            //Uses the latest created warrantyID to associate it with the correct vehicle.
            statement.executeUpdate("UPDATE Vehicle SET WarrantyID = "+wID+" WHERE VehicleID ="+ vehicleID);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }

    /**
     * Edits the warranty information.
     *
     * @param warrantyID The location of the warranty that needs editing
     * @param company The name of the Warranty company
     * @param address The address
     * @param expiry The expiry date of the warranty
     */
    public Warranty(int warrantyID, String company, String address, String expiry) {

        this.company    = company;
        this.address    = address;
        this.expiry     = expiry;
        this.wID        = warrantyID;

        Database DB = Database.getInstance();
        Connection connection = DB.getConnection();

        String dbQuery =    "UPDATE Warranty "
                            +"SET   nameOfCompany     ='"+ company +"',"
                            +"      addressOfCompany  ='"+ address +"',"
                            +"      dateOfExpiry      ='"+ expiry +"'"
                            +"WHERE WarrantyID        ="+ warrantyID;

        Statement statement = null;

        try {
            statement = connection.createStatement();
            statement.executeUpdate(dbQuery);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }

    /**
     * Gets the warranty information from the requested vehicle
     * @param vehicleID the vehicle who's warranty information needs to be accessed
     */
    public Warranty(int vehicleID) {
        Database DB = Database.getInstance();
        Connection connection = DB.getConnection();
        Statement statement = null;

        String dbQuery = "SELECT WarrantyID FROM Vehicle WHERE VehicleID="+ vehicleID;

        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(dbQuery);

            if (rs.next()) {
                this.wID = rs.getInt("WarrantyID");

                // Checks if the vehicle actually has a warranty or not.
                // If it doesn't it gets assigned the temporary default values.
                if (rs.wasNull()) {
                    this.expiry = "1999-01-01";
                } else {
                    dbQuery = "SELECT * FROM Warranty WHERE WarrantyID="+ this.wID;
                    rs = statement.executeQuery(dbQuery);

                    this.company = rs.getString("nameOfCompany");
                    this.address = rs.getString("addressOfCompany");
                    this.expiry  = rs.getString("dateOfExpiry");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }

    /**
     * Checks if the current vehicle is in warranty
     *
     * @param vehicleID current vehicleID
     * @return true: if it is in warranty / false: if it out of warranty
     */
    public static boolean warrantyCheck(int vehicleID) {

        Database DB = Database.getInstance();
        String currentDate = DB.getCurrentDate();

        Warranty w = new Warranty(vehicleID);

        return (currentDate.compareTo(w.expiry) < 0);
    }


    // Getter that returns company name
    public String getCompany() { return this.company; }

    // Getter that returns the company address
    public String getAddress() { return this.address; }

    // Getter that returns the date of expiry.
    public String getExpiry() { return this.expiry; }

}
