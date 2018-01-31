
package common;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import org.sqlite.SQLiteConfig;

public class Database {
    
    private static final Database INSTANCE = new Database();
    private Connection connection = null;
    private static final String DATABASE_NAME = "GM-SIS.db";
    
    public static final String INSIDE_DB_DATE_FORMAT = "yyyy-mm-dd";
    public static final String OUTSIDE_DB_DATE_FORMAT = "dd-mm-yyyy";
    
    private static final String CREATE_CURRENT_DATE_TABLE = "CREATE TABLE IF NOT EXISTS 'CurrentDate' (\n" +
        "   'CurrentDate' datetime\n" +
        ");";
    private static final String CREATE_HOLIDAYS_TABLE = "CREATE TABLE IF NOT EXISTS  'Holidays' (\n" +
        "   'Date' datetime\n" +
        ");";
    
    // Source of information: https://www.gov.uk/bank-holidays
    private static final String INSERT_PUBLIC_HOLIDAYS = "INSERT INTO 'Holidays' (Date) VALUES \n"
            + "('2017-04-14'),\n"
            + "('2017-04-17'),\n"
            + "('2017-05-01'),\n"
            + "('2017-05-29'),\n"
            + "('2017-08-28'),\n"
            + "('2017-12-25'),\n"
            + "('2017-12-26'),\n"
            + "('2018-01-01'),\n"
            + "('2018-03-30'),\n"
            + "('2018-04-02'),\n"
            + "('2018-05-07'),\n"
            + "('2018-05-28'),\n"
            + "('2018-08-27'),\n"
            + "('2018-12-25'),\n"
            + "('2018-12-26')\n"
            + ";";
    
    private static final String CREATE_PART_INFO_TABLE = "CREATE TABLE IF NOT EXISTS 'PartInfo' (\n" +
        "   'Name' varchar PRIMARY KEY UNIQUE NOT NULL,\n" +
        "   'Description' text NOT NULL,\n" +
        "   'Cost' real NOT NULL\n" +
        ");";
    private static final String CREATE_PART_TABLE = "CREATE TABLE IF NOT EXISTS 'Part' (\n" +
        "   'ID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'Name' varchar NOT NULL,\n" +
        "   'ArrivalDate' datetime NOT NULL,\n" +
        "   'WithdrawDate' datetime,\n" +
        "   'InstallDate' datetime,\n" + 
        "   FOREIGN KEY(Name) REFERENCES PartInfo(Name) ON UPDATE CASCADE ON DELETE CASCADE\n" +
        ");";
    
    private static final String CREATE_PART_FOR_REPAIR_TABLE = "CREATE TABLE IF NOT EXISTS 'PartForRepairBooking' (\n" +
        "   'PartID' integer,\n" +
        "   'RepairBookingID' integer,\n" +
        "   'IsForSPC' integer,\n" + 
        "   'RepairOnly' integer,\n" + 
        "   'NewPart' integer,\n" + 
        "   'IsChargedFor' integer,\n" + 
        "   FOREIGN KEY(PartID) REFERENCES Part(ID) ON DELETE CASCADE,\n" +
        "   FOREIGN KEY(RepairBookingID) REFERENCES DiagRepairBooking(DiagRepID) ON DELETE CASCADE\n" +
        ");";
    private static final String CREATE_PART_FOR_SPC_REPAIR_TABLE = "CREATE TABLE IF NOT EXISTS 'PartForSPCRepair' (\n" +
        "   'PartID' integer,\n" +
        "   'SPCBookingID' integer,\n" +
        "   'repairStatus' integer,\n" +
        "   FOREIGN KEY(PartID) REFERENCES Part(ID) ON DELETE CASCADE,\n" +
        "   FOREIGN KEY(SPCBookingID) REFERENCES SPCBooking(SPCBookingID) ON DELETE CASCADE\n" + 
        ");";
    
    private static final String CREATE_PART_IN_VEHICLE_TABLE = "CREATE TABLE IF NOT EXISTS 'PartInVehicle' (\n" +
        "   'PartID' integer,\n" +
        "   'VehicleID' integer,\n" +
        "   FOREIGN KEY(PartID) REFERENCES Part(ID) ON DELETE CASCADE,\n" +
        "   FOREIGN KEY(VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE\n" + 
        ");";
    
    private static final String CREATE_SYSTEM_USERS_TABLE = "CREATE TABLE IF NOT EXISTS 'SystemUser' (\n" +
        "   'userID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'Surname' varchar,\n" +
        "   'Firstname' varchar,\n" +
        "   'Login' varchar UNIQUE,\n" +
        "   'Password' varchar,\n" +
        "   'HourlyWage' real,\n" +
        "   'IsAdmin' integer,\n" +
        "   'IsMechanic' integer\n" +
        ");";
    
    private static final String CREATE_DIAG_REPAIR_BOOKING_TABLE = "CREATE TABLE IF NOT EXISTS 'DiagRepairBooking' (\n" +
        "   'DiagRepID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'CostOfService' real,\n" +
        "   'StartDate' datetime CHECK (date(StartDate) IS NOT NULL),\n" +
        "   'EndDate' datetime  CHECK (date(EndDate) IS NOT NULL),\n" +
        "   'Completed' integer,\n" +
        "   'VehicleID' integer,\n" +
        "   'RepairTime' integer,\n" +
        "   'MechanicID' integer,\n" +
        "   'Paid' integer,\n" +
        "   'sendVehicleToSPC' integer,\n" +
        "   'fault' text,\n" +
        "   FOREIGN KEY(MechanicID) REFERENCES SystemUser(userID),\n" +
        "   FOREIGN KEY(VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE" +
        ");";
    
    private static final String CREATE_SPC_BOOKING_TABLE = "CREATE TABLE IF NOT EXISTS 'SPCBooking' (\n" +
        "   'SPCBookingID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'SPCID' integer,\n" +
        "   'CostOfService' real,\n" +
        "   'ExpectedDeliveryDate' datetime,\n" +
        "   'ExpectedReturnDate' datetime,\n" +
        "   'DiagRepBookingID' integer,\n" +
        "   'IsBookingForVehicle' integer,\n" +
        "   'ReturnStatus' integer,\n" +
        "   FOREIGN KEY(SPCID) REFERENCES SPCDetails(SPCID) ON DELETE CASCADE,\n" + 
        "   FOREIGN KEY(DiagRepBookingID) REFERENCES DiagRepairBooking(DiagRepID) ON DELETE CASCADE" +
        ");";
    private static final String CREATE_SPC_DETAILS_TABLE = "CREATE TABLE IF NOT EXISTS 'SPCDetails' (\n" +
        "   'SPCName' text,\n" +
        "   'SPCAddress' text,\n" +
        "   'SPCPhoneNo' varchar,\n" +
        "   'SPCEmail' text,\n" +
        "   'SPCID' integer PRIMARY KEY AUTOINCREMENT\n" +
        ");";
    
    private static final String CREATE_VEHICLE_FOR_SPC_REPAIR_TABLE = "CREATE TABLE IF NOT EXISTS 'VehicleForSPCRepair' (\n" +
        "   'VehicleID' integer,\n" +
        "   'SPCBookingID' integer,\n" +
        "   FOREIGN KEY(VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE,\n" + 
        "   FOREIGN KEY(SPCBookingID) REFERENCES SPCBooking(SPCBookingID) ON DELETE CASCADE\n" + 
        ");";

    private static final String CREATE_VEHICLE_TABLE = "CREATE TABLE IF NOT EXISTS 'Vehicle' (\n" +
        "   'VehicleID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'CustomerID' integer,\n" +
        "   'CurrentMileage' text,\n" +
        "   'DateOfLastService' datetime,\n" +
        "   'WarrantyID' integer,\n" +
        "   FOREIGN KEY(CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE,\n" + 
        "   FOREIGN KEY(WarrantyID) REFERENCES Warranty(WarrantyID)\n" +
        ");";

    private static final String CREATE_VEHICLE_INFO_TABLE = "CREATE TABLE IF NOT EXISTS 'VehicleInfo' (\n" +
        "   'VehicleInfoID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'VehicleID' integer,\n" +
        "   'Model' text,\n" +
        "   'Make' text,\n" +
        "   'Registration' text,\n" +
        "   'Colour' text,\n" +
        "   'EngineSize' integer,\n" +
        "   'FuelType' text,\n" +
        "   'VehicleKind' text,\n" +
        "   'MoTRenewal' datetime,\n" +
        "   FOREIGN KEY(VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE\n" +
        ");";
    
    private static final String CREATE_CUSTOMER_TABLE = "CREATE TABLE IF NOT EXISTS 'Customer' (\n" +
        "   'CustomerID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'CustomerType' varchar,\n" +
        "   'Firstname' text,\n" +
        "   'Surname' text,\n" +
        "   'Address' text,\n" +
        "   'Postcode' varchar,\n" +
        "   'Phone' varchar UNIQUE,\n" +
        "   'Email' text UNIQUE\n" +
        ");";
    private static final String CREATE_WARRANTY_TABLE = "CREATE TABLE IF NOT EXISTS 'Warranty' (\n" +
        "   'WarrantyID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'InWarranty' integer,\n" +
        "   'dateOfExpiry' datetime,\n" +
        "   'nameOfCompany' text,\n" +
        "   'addressOfCompany' text\n" +
        ");";
    
    private static final String CREATE_BILLS_TABLE = "CREATE TABLE IF NOT EXISTS 'Bills' (\n" +
        "   'BillID' integer PRIMARY KEY AUTOINCREMENT,\n" +
        "   'CustomerID' integer,\n" +
        "   'DiagRepID' integer,\n" +
        "   'VehicleID' integer,\n" +
        "   'InWarranty' integer,\n" +
        "   'Bill' real,\n" +
        "   FOREIGN KEY(CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE\n" + 
        ");";

    private static final String CREATE_TEMPLATE_TABLE = "CREATE TABLE IF NOT EXISTS 'Template' (\n" +
            "   'TemplateID' integer PRIMARY KEY AUTOINCREMENT,\n" +
            "   'Make' text,\n" +
            "   'Model' text,\n" +
            "   'EngineSize' text,\n" +
            "   'FuelType' text, \n" +
            "   'VehicleKind' text \n" +
            ");";

    private static final String INSERT_TEMPLATE_TABLE =  "INSERT INTO Template (Make, Model, EngineSize, FuelType, VehicleKind)"
                                                +"VALUES  ('Alfa Romeo',    'Mito',     '2.3L', 'Diesel', 'car'),"
                                                        +"('Ferrari',       'Enzo',     '5.2L', 'Petrol', 'car'),"
                                                        +"('Toyota',        'Yaris',    '1.2L', 'Diesel', 'car'),"
                                                        +"('Mercedes',      'E Class',  '2.0L', 'Petrol', 'car'),"
                                                        +"('McLaren',       'P1',       '4.5L', 'Hydrogen', 'car'),"
                                                        +"('Citroen',       'C3',       '2.4L', 'Petrol', 'car'),"
                                                        +"('Fiat',          'Punto',    '1.5L', 'Diesel', 'car'),"
                                                        +"('Honda',         'Civic',    '3.0L', 'Diesel', 'car'),"
                                                        +"('Porsche',       '911 GT3',  '6.2L', 'Petrol', 'car'),"
                                                        +"('Bugatti',       'Veyron',   '6.3L', 'Petrol', 'car'),"
                                                        +"('Ford',          'Galaxy',   '2.4L', 'Diesel', 'van'),"
                                                        +"('Fiat',          'Fiorino',  '3.0L', 'Diesel', 'van'),"
                                                        +"('Honda',         'Combo',    '2.3L', 'Petrol', 'van'),"
                                                        +"('GMC',           'Safari',   '4.2L', 'Diesel', 'van'),"
                                                        +"('Pontiac',       'Montana',  '4.0L', 'Petrol', 'van'),"
                                                        +"('Chevrolet',    'Silverado', '6.2L', 'Diesel', 'truck'),"
                                                        +"('Isuzu',         'D-Max',    '4.2L', 'Petrol', 'truck'),"
                                                        +"('Nissan',        'Navara',   '5.3L', 'Diesel', 'truck'),"
                                                        +"('Toyota',        'Hilux',    '6.4L', 'Diesel', 'truck'),"
                                                        +"('Volkswagen',    'Amarok',   '5.2L', 'Petrol', 'truck');";

    private Database() {
        // must do this so foreign keys work 
        // Code by Cherry (StackOverflow): How do you enforce foreign key constraints in SQLite through Java?
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        try {
            // this is when the database is created
            if(!databaseExists()){
                connection = DriverManager.getConnection("jdbc:sqlite:" + Database.DATABASE_NAME, config.toProperties());
                initializeDatabase();
            }else{
                connection = DriverManager.getConnection("jdbc:sqlite:" + Database.DATABASE_NAME, config.toProperties());
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Connection failed!", ex);
        }
    }
    
    /**
     * Creates tables in database for system. 
     * 
     * IMPORTANT - does not close connection because when this method is called
     * someone is getting a database instance for the first time. Because they 
     * are getting a database instance they want it to be open to work with the DB.
     * 
     * It is good practice to close the connection when you are done working with
     * the DB.
     */
    private void initializeDatabase(){
        try {
            Statement statement = connection.createStatement();
            // time out after 10 seconds - we can change if too short
            statement.setQueryTimeout(10);
            
            // create tables
            statement.executeUpdate(Database.CREATE_CURRENT_DATE_TABLE);
            statement.executeUpdate(Database.CREATE_HOLIDAYS_TABLE);
            statement.executeUpdate(Database.INSERT_PUBLIC_HOLIDAYS); // Inserts public holidays for 2017 - 2018
            statement.executeUpdate(Database.CREATE_PART_TABLE);
            statement.executeUpdate(Database.CREATE_PART_INFO_TABLE);
            statement.executeUpdate(Database.CREATE_SYSTEM_USERS_TABLE);
            statement.executeUpdate(Database.CREATE_DIAG_REPAIR_BOOKING_TABLE);
            statement.executeUpdate(Database.CREATE_SPC_BOOKING_TABLE);
            statement.executeUpdate(Database.CREATE_SPC_DETAILS_TABLE);
            statement.executeUpdate(Database.CREATE_WARRANTY_TABLE);
            statement.executeUpdate(Database.CREATE_VEHICLE_TABLE);
            statement.executeUpdate(Database.CREATE_VEHICLE_INFO_TABLE);
            statement.executeUpdate(Database.CREATE_VEHICLE_FOR_SPC_REPAIR_TABLE);
            statement.executeUpdate(Database.CREATE_CUSTOMER_TABLE);
            statement.executeUpdate(Database.CREATE_PART_FOR_REPAIR_TABLE);
            statement.executeUpdate(Database.CREATE_PART_FOR_SPC_REPAIR_TABLE);
            statement.executeUpdate(Database.CREATE_PART_IN_VEHICLE_TABLE);
            statement.executeUpdate(Database.CREATE_BILLS_TABLE);
            statement.executeUpdate(Database.CREATE_TEMPLATE_TABLE);
            statement.executeUpdate(Database.INSERT_TEMPLATE_TABLE);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 

    private static boolean databaseExists(){
        File f = new File(Database.DATABASE_NAME);
        return f.exists();
    }
    
    public static Database getInstance() {
        return INSTANCE;
    }

    public Connection getConnection(){
        return this.connection;
    }

    public void test_database(){
		
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			statement.executeUpdate("drop table if exists `test`");
			statement.executeUpdate("create table `test` (`id` integer, `name` string)");
			statement.executeUpdate("insert into `test` values('1', 'test 1')");
			statement.executeUpdate("insert into `test` values('2', 'test 2')");
                        statement.executeUpdate("insert into `Customer` values(null, 'Private', 'martin', 'neil', '156 Valentine Road, London', 'E11 12J', '07462863983', 'SD@qmul.ac.uk')");
                        //statement.executeUpdate("insert into CurrentDate values('2017-03-21')");
                        statement.executeUpdate("insert into Warranty ('InWarranty')values('0')");
                        statement.executeUpdate("insert into Vehicle('CustomerID','WarrantyID','CurrentMileage') values('2','2','14000')");
                        statement.executeUpdate("insert into VehicleInfo ('Model','Registration','VehicleID')values('yessss','lambo','1')");
                        //statement.executeUpdate("insert into SystemUser('Firstname','IsAdmin','Login','Password') values ('Rahi','1','12345','12345')");
                        //statement.executeUpdate("insert into DiagRepairBooking('StartDate','EndDate','VehicleID','MechanicID','sendVehicleToSPC','Completed','fault') values('2017-03-11 13:00','2017-03-16 14:00','1','1','1','0','sparks')");
                        //statement.executeUpdate("insert into DiagRepairBooking('StartDate','EndDate','VehicleID','MechanicID','sendVehicleToSPC','Completed','fault') values('2017-03-11 13:00','2017-03-16 14:00','1','1','0','0','sparks')");
                        //statement.executeUpdate("insert into DiagRepairBooking('StartDate','EndDate','VehicleID','MechanicID','sendVehicleToSPC','Completed','fault') values('2017-03-11 13:00','2017-03-16 14:00','1','1','0','0','sparks')");
                        //statement.executeUpdate("insert into PartInfo values('brakes','****','36.45')");
                       //statement.executeUpdate("insert into PartInfo values('plugs','****','45.25')");
                        //statement.executeUpdate("insert into Part('Name','ArrivalDate','InstallDate') values ('brakes','2015-01-01','2016-01-01')");
                        //statement.executeUpdate("insert into Part('Name','ArrivalDate','InstallDate') values ('plugs','2016-01-01','2016-01-01')");
                        statement.executeUpdate("insert into PartInVehicle('PartID','VehicleID') values ('3','2')");
                        statement.executeUpdate("insert into PartInVehicle('PartID','VehicleID') values ('8','2')");
                       //statement.executeUpdate("insert into PartForRepairBooking values('1','1','1','0','0')");
                       //statement.executeUpdate("insert into PartForRepairBooking values('2','1','1','0','0')");
                        
			
			//ResultSet rs = statement.executeQuery("select * from `test`");
                        ResultSet rs = statement.executeQuery("select * from `Customer`");

                        
			System.out.println("id	type firstname surname");
			while(rs.next()){
				System.out.println(rs.getInt("CustomerID")+"	"+rs.getString("CustomerType")+"	"+rs.getString("Firstname")+"	"+rs.getString("Surname"));
			}
		}
		catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
		finally {
			if (connection != null){
				try{
					connection.close();
				}
				catch(SQLException ex){
					System.err.println(ex.getMessage());
				}
			}
		}
	}
    
    /**
     * Returns the current date in the system.
     * 
     * @return The current date
     */
    public String getCurrentDate(){
        String currentDate = null;
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT date(CurrentDate) FROM CurrentDate";
            ResultSet rs = statement.executeQuery(query);
            currentDate = rs.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currentDate;
    }
    
    /**
     * Returns the current date and time (from DB).
     * In some instances, to search, edit or create DiagRepair bookings, the date and time is needed.
     * @return The current date and time
     */
    public String getCurrentDateTime() {
        String currentDateTime = null;
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT CurrentDate FROM CurrentDate";
            ResultSet rs = statement.executeQuery(query);
            currentDateTime = rs.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currentDateTime;
    }
    
}
