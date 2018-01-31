package vehicles.logic;

import common.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Ilias.
 *
 * This is the main class that handles almost everything that has to do with Vehicles.
 *
 * A big number of requirements from the definition are met in the Database (Things like associations and Template Vehicles)
 */
public class Vehicle {
    private static final Database DB = Database.getInstance();

    // Instance variables. Requirements involved: 1, 2, 3
    private String model, make, registration, colour, fuelType, vehicleType, motRenewal, engineSize, mileage, customerName, lastService;
    private int ID, customerID;

    /**
     * <GET>
     * Initializes a Vehicle object with information from the database.
     * The data can then be accessed with the various Getter methods.
     *
     * Illegal argument exception is being thrown, if there is an SQLite error
     * preventing anything from being created properly.
     *
     * @param vehicleID ID of the Vehicle that is being created
     */
    public Vehicle(int vehicleID) {
        Connection connection = DB.getConnection();
        Statement statement = null;
        this.ID = vehicleID;

        // Selecting all the data from VehicleInfo table that corresponds to this particular Vehicle Object
        String dbQuery =    "SELECT * FROM VehicleInfo INNER JOIN Vehicle " +
                            "ON VehicleInfo.VehicleID = Vehicle.VehicleID " +
                            "WHERE VehicleInfo.VehicleID="+ this.ID;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(dbQuery);

            this.model          = rs.getString("Model");
            this.make           = rs.getString("Make");
            this.registration   = rs.getString("Registration");
            this.colour         = rs.getString("Colour");
            this.fuelType       = rs.getString("FuelType");
            this.vehicleType    = rs.getString("VehicleKind");
            this.motRenewal     = rs.getString("MoTRenewal");
            this.engineSize     = rs.getString("EngineSize");
            this.mileage        = rs.getString("CurrentMileage");
            this.lastService    = rs.getString("DateOfLastService");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection); // Closes all Data feeds.
        }
    }
    

    /**
     * <ADD>
     * Creates a new Vehicle from the parameters.
     * Every time a new Vehicle is created a new row in both Vehicle and VehicleInfo
     * tables gets created and associated with each other.
     *
     * @param model        \
     * @param make          |
     * @param registration  |
     * @param fuelType      |- ALL EXPLAINED IN GETTER METHODS
     * @param colour        |
     * @param motRenewal    |
     * @param engineSize   /
     *
     * Requirements involved: 4, 7
     */
    public Vehicle(String model, String make, String registration,
                   String fuelType, String colour, String motRenewal,
                   String vehicleType, String engineSize) {

        this.model          = model;
        this.make           = make;
        this.registration   = registration;
        this.fuelType       = fuelType;
        this.colour         = colour;
        this.motRenewal     = motRenewal;
        this.vehicleType    = vehicleType;
        this.engineSize     = engineSize;

        Connection connection = DB.getConnection();
        Statement statement = null;

        try {
            statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO 'Vehicle' DEFAULT VALUES");

            this.ID = Utility.getLatestID("Vehicle"); // The variable ID gets updated with the current VehicleID attribute

            statement.executeUpdate("INSERT INTO 'VehicleInfo' ('Model', 'Make', 'Registration', 'Colour', "
                                    + "'EngineSize', 'FuelType', 'VehicleKind', 'MoTRenewal', 'VehicleID') "
                                    + "VALUES ('"+ model +"', '"+ make +"', '"+ registration +"', '"
                                    + colour +"', '"+ engineSize +"', '"+ fuelType +"', '"
                                    + vehicleType +"', '"+ motRenewal +"', '"+ this.ID +"');");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }

    /**
     * <EDIT>
     * This method is used when the user wants to edit the information at a specific vehicle.
     * The information then gets updated in the local class variables and also in the Database.
     *
     * @param vehicleID     \
     * @param model          |
     * @param make           |
     * @param registration   |
     * @param fuelType       |- All explained in getter methods
     * @param colour         |
     * @param motRenewal     |
     * @param vehicleType    |
     * @param engineSize    /
     *
     * Requirement involved: 8
     */
    public Vehicle(int vehicleID, String model, String make, String registration,
                   String fuelType, String colour, String motRenewal,
                   String vehicleType, String engineSize) {

        this.ID             = vehicleID;
        this.model          = model;
        this.make           = make;
        this.registration   = registration;
        this.fuelType       = fuelType;
        this.colour         = colour;
        this.motRenewal     = motRenewal;
        this.vehicleType    = vehicleType;
        this.engineSize     = engineSize;

        Connection connection = DB.getConnection();
        Statement statement = null;

        try {
            statement = connection.createStatement();
            statement.executeUpdate("UPDATE 'VehicleInfo'"
                                        + "SET  'Model'         = '"+ model +"',"
                                        + "     'Make'          = '"+ make +"',"
                                        + "     'Registration'  = '"+ registration +"',"
                                        + "     'Colour'        = '"+ colour +"',"
                                        + "     'EngineSize'    = '"+ engineSize +"',"
                                        + "     'FuelType'      = '"+ fuelType +"',"
                                        + "     'VehicleKind'   = '"+ vehicleType +"',"
                                        + "     'MoTRenewal'    = '"+ motRenewal +"'"
                                        + "WHERE VehicleInfo.VehicleID="+ vehicleID);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }

    /**
     * Adds the requested mileage value to the database, while updating the local class variable too.
     * This method needs to be used by Bookings.
     *
     * @param vehicleID The VehicleID of the vehicle information we want to edit.
     * @param mileage   The Mileage value that needs to be added to the database.
     */
    public void addMileage(int vehicleID, String mileage) {
        Connection connection = DB.getConnection();
        Statement statement = null;
        this.mileage = mileage;

        String query =  "UPDATE Vehicle SET CurrentMileage= '"+mileage+"' WHERE VehicleID="+vehicleID;

        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }

    /**
     * This method deletes the vehicle data from the database by getting the current ID of the vehicle object.
     */
    public void deleteVehicleData() {
        Connection connection = DB.getConnection();
        Statement statement = null;

        String query =  "DELETE FROM Vehicle WHERE VehicleID="+this.ID;

        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }


    /**
     * Gets teh customerID and sets it in the database while updating the local variable too.
     *
     * @param customerID the term to set the correct customer.
     */
    public void setCustomer(int customerID) {
        Connection connection = DB.getConnection();
        Statement statement = null;
        this.customerID = customerID;

        String query =  "UPDATE Vehicle SET CustomerID= '"+this.customerID+"' WHERE VehicleID="+this.ID;

        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
    }

    /**
     * Helps to query the Customer table and get more information about the current customer.
     *
     * @return customer name
     */
    public String getCustomerName() {
        Connection connection = DB.getConnection();
        Statement statement = null;

        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT CustomerID "
                                                    + "FROM Vehicle "
                                                    + "WHERE VehicleID=" +this.ID);

            this.customerID = rs.getInt("CustomerID");

            rs = statement.executeQuery("SELECT FirstName, Surname "
                                        + "FROM Customer "
                                        + "WHERE CustomerID="+customerID);

            // Sets the customer name and surname  in one variable
            customerName = rs.getString("FirstName") + " " + rs.getString("Surname");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Utility.closeDatabaseFeeds(statement, connection);
        }
        return customerName;
    }

    // @return Vehicle model
    public String getModel() { return model; }

    // @return Vehicle mak
    public String getMake() { return make; }

    // @return Vehicle Registration Plate
    public String getRegistration() { return registration; }

    // @return Vehicle colour
    public String getColour() { return this.colour; }

    // @return Vehicle Fuel Type
    public String getFuelType() { return fuelType; }

    // @return Vehicle Kind (Car, Van or Truck)
    public String getVehicleType() { return vehicleType; }

    // @return Vehicle Ministry of Transport test renewal date
    public String getMotRenewal() { return motRenewal; }

    // @return Vehicle Engine Size
    public String getEngineSize() { return engineSize; }

    // @return Vehicle Current Mileage - This is being accessed by Bookings
    public String getMileage() { return mileage; }

    // @return Vehicle Table ID number.
    public int getID() { return ID; }

    // @return Customer ID number associated with vehicle.
    public  int getCustomerID() { return customerID; }

    public String getLastService() { return lastService; }

}