/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

/**
 *
 * @author Pirashanth
 * class to insert SPC details into database
 */
import java.util.*;
import common.Database;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javax.swing.JOptionPane;



public final class SPC 
{
    private IntegerProperty id;
    private StringProperty name;
    private StringProperty address ; // Won't change for duration of booking (only after complete)
    private StringProperty email; // Determines whether this part was already in the booking
    private StringProperty number;
    
    public SPC(String SPCName, String SPCAddress, String SPCPhoneNo, String SPCEmail)// when do no know the spcID to add a new SPC to the database
    {
        name=new SimpleStringProperty(SPCName);
        address=new SimpleStringProperty(SPCAddress);
        email=new SimpleStringProperty(SPCEmail);
        if(SPCPhoneNo.matches(".*\\d+.*")){number=new SimpleStringProperty(SPCPhoneNo);}
        else{showError("Database Error", "Invalid phone number\nPlease try again");throw new NumberFormatException();}
        id=new SimpleIntegerProperty(updateDatabase(SPCName,SPCAddress,SPCPhoneNo,SPCEmail));
    }
    public SPC(Integer id,String SPCName, String SPCAddress, String SPCPhoneNo, String SPCEmail)// passing ID aswell when showing list of all existing spc's
    {
        name=new SimpleStringProperty(SPCName);
        address=new SimpleStringProperty(SPCAddress);
        email=new SimpleStringProperty(SPCEmail);
        if(SPCPhoneNo.matches(".*\\d+.*")){number=new SimpleStringProperty(SPCPhoneNo);}
        else{showError("Database Error", "Invalid phone number\nPlease try again");throw new NumberFormatException();}
        this.id= new SimpleIntegerProperty(id);
    }
    public SPC()
    {
       
    }
    public int SPCID()
    {
        return this.id.getValue();
    }
    public int updateDatabase(String SPCName, String SPCAddress, String SPCPhoneNo, String SPCEmail)//add SPC details to the database
    {
        Database db= Database.getInstance();
        Connection connection= db.getConnection();
        String insertInto= ("insert into SPCDetails('SPCName','SPCAddress','SPCPhoneNo','SPCEmail') values('"+SPCName+"','"+SPCAddress+"','"+SPCPhoneNo+"','"+SPCEmail+"')");
       
        Statement statement= null;
        
        
        try
        {    
            String key[] = {"SPCID"}; //put the name of the primary key column
            PreparedStatement ps = connection.prepareStatement(insertInto, key);
 
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            long generatedKey = rs.getLong(1);
            return (int)generatedKey;// return the spc ID
            
        }
        
        catch(SQLException err)
        {
          showError("Database Error", err.getMessage());
          Logger.getLogger(SPC.class.getName()).log(Level.SEVERE, null, err);
          return 0;
        }
    }
    public void deleteBooking(int partID)// delete a booking if a part is removed
    {
        Database db= Database.getInstance();
        Connection connection= db.getConnection();
        Statement statement= null;
       
        try
        {    statement=connection.createStatement();
             ResultSet rs2=statement.executeQuery("select count(*) as total from PartForSPCRepair where PartID="+partID);// check if part is involved in a SPC booking
             if(rs2.getInt("total")>0)// if so then set booking as complete..
             {

                ResultSet rs=statement.executeQuery("select PartForSPCRepair.SPCBookingID,SPCBooking.DiagRepBookingID,SPCBooking.SPCID from PartForSPCRepair inner join SPCBooking on PartForSPCRepair.SPCBookingID=SPCBooking.SPCBookingID where PartID="+partID);
                while(rs.next())
                {
                    int bookingID=rs.getInt("SPCBookingID");
                    int diagID=rs.getInt("DiagRepBookingID");
                    int spcID=rs.getInt("SPCID");
                    statement.executeUpdate("delete from PartForSPCRepair where SPCBookingID="+bookingID+" and PartID="+partID);//delete from part for spc 
                    statement.executeUpdate("delete from SPCBooking where SPCBookingID="+bookingID+" and DiagRepBookingID="+diagID+" and SPCID="+spcID);// then delete the booking
                }

             }
        }  
        catch(SQLException err)
        {
          showError("Database Error", err.getMessage());
          Logger.getLogger(SPC.class.getName()).log(Level.SEVERE, null, err);
        }
        
    }
    public StringProperty nameProperty() { return this.name; }
    public StringProperty addressProperty() { return this.address; }
    public StringProperty emailProperty() { return this.email; }
    public StringProperty numberProperty() { return this.number; }
    public IntegerProperty idProperty() { return this.id; }
    
    private void showError(String title, String content) 
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
