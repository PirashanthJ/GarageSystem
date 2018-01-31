package customers.logic;

import common.Database;
import diagrep.logic.DiagRepair;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import vehicles.logic.Warranty;

/**
 *
 * @author ec15072
 * Bill Class to insert bill into database
 */
public class Bill {
    
    private int customerid;
    private int diagrepid;
    private int  vehicleid;
    private BigDecimal bill;
    
    public Bill(int drid) throws IllegalArgumentException{
        diagrepid = drid;
        Database db = Database.getInstance();
        Connection connection = db.getConnection();
        ResultSet rs;
        try {
            PreparedStatement ps;
            ps = connection.prepareStatement("SELECT Vehicle.CustomerID,  DiagRepairBooking.VehicleID  " +
                 "FROM  Vehicle \n" +
                 "INNER JOIN DiagRepairBooking ON Vehicle.VehicleID  = DiagRepairBooking.VehicleID\n" +
                 "WHERE DiagRepairBooking.DiagRepID = '"+diagrepid+"';");
            rs = ps.executeQuery();
            this.customerid = rs.getInt(1);
            this.vehicleid = rs.getInt(2);
            ps = connection.prepareStatement("Select CostOfService FROM DiagRepairBooking "
                                           + "WHERE DiagRepID = '"+diagrepid+"';");
            rs = ps.executeQuery();
            BigDecimal diagrepcost = rs.getBigDecimal(1);
            ps = connection.prepareStatement("Select SUM(SPCBooking.CostOfService) FROM SPCBooking "
                                           + "WHERE DiagRepBookingID = '"+diagrepid+"';");
            rs = ps.executeQuery();
            BigDecimal spccost = rs.getBigDecimal(1);
            this.bill = calculateBill(diagrepcost, spccost);
            
        } catch (SQLException ex) {
            Logger.getLogger(DiagRepair.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Unable to create Bill");
        }        
    }
    
    //method to sert a bill record into the database
    public void insertBill(){
        Database db = Database.getInstance();
        Connection connection = db.getConnection();
        ResultSet rs;
        try {
            PreparedStatement ps;
            ps = connection.prepareStatement("INSERT INTO Bills (customerid, diagrepid, vehicleid, InWarranty, bill) " +
            "VALUES(?, ?, ?, ?, ?);");
            ps.setInt(1, customerid);
            ps.setInt(2, diagrepid);
            ps.setInt(3, vehicleid);
            boolean isInWarranty = Warranty.warrantyCheck(vehicleid);
            if (isInWarranty)
                ps.setInt(4, 1);
            else
                ps.setInt(4, 0);
            ps.setBigDecimal(5, bill);
            ps.executeUpdate();
            if (isInWarranty){
                    ps = connection.prepareStatement("UPDATE DiagRepairBooking SET Paid = 1 WHERE DiagRepID = "+diagrepid+";");
                    ps.executeUpdate();
            }
        } catch (SQLException ex) {
            System.err.println(ex);
            Logger.getLogger(DiagRepair.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
    //Method to calculate the total bill from a diagrepbooking
    public static BigDecimal calculateBill(BigDecimal diagrepcost, BigDecimal spccost){
        BigDecimal totalcost = new BigDecimal(0.00);
        if (diagrepcost == null && spccost == null)
            return totalcost;
        else if (spccost == null)
            totalcost = diagrepcost;
        else if (diagrepcost == null)
            totalcost = spccost;
        else {
            totalcost = totalcost.add(diagrepcost);
            totalcost = totalcost.add(spccost);
        }
        totalcost = totalcost.setScale(2, BigDecimal.ROUND_HALF_UP);
        return totalcost;
    }

    public int getCustomerid() {
        return customerid;
    }

    public int getDiagrepid() {
        return diagrepid;
    }

    public int getVehicleid() {
        return vehicleid;
    }
    
    public BigDecimal getBill(){
        return bill;
    }
}