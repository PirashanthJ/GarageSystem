/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

import common.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 */
public class CustomerDetailsController implements Initializable 
{
    
    Database db= Database.getInstance();
    Connection connection=db.getConnection();
    /**
     * pop to show Customer details
     */
    @FXML
    private TextField fName;
    @FXML
    private TextField sName;
    @FXML
    private TextField address;
    @FXML
    private TextField postCode;
    @FXML
    private TextField phone;
    @FXML
    private TextField email;
    private Tab parentTab;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        // TODO
    }
    @FXML
    public void customerDetails(String vehReg)
    {
       Statement statement;ResultSet rs;
       fName.setEditable(false);
       sName.setEditable(false);
       phone.setEditable(false);
       email.setEditable(false);
       postCode.setEditable(false);
       address.setEditable(false);

       try
       {
           statement= connection.createStatement();
           rs=statement.executeQuery("select Customer.Firstname,Customer.Surname,Customer.Address,Customer.Postcode,Customer.Phone,Customer.Email from Customer inner join Vehicle on Vehicle.CustomerID=Customer.CustomerID inner join VehicleInfo on VehicleInfo.VehicleID=Vehicle.VehicleID where VehicleInfo.Registration='"+vehReg+"'");
           fName.setText(rs.getString("Firstname"));
           sName.setText(rs.getString("Surname"));
           address.setText(rs.getString("Address"));
           postCode.setText(rs.getString("Postcode"));
           phone.setText(rs.getString("Phone"));
           email.setText(rs.getString("Email"));
       }
       catch(NullPointerException e)
       {
            showError("Unable to retrieve", "Cannot get all customer details");
       } 
       catch (IllegalArgumentException e) 
       {
            showError("Validation Error", e.getMessage());
       } 
       catch (SQLException ex) 
       {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(CustomerDetailsController.class.getName()).log(Level.SEVERE, null, ex);
       }
        
    }
    public void setParentTab(Tab tab)
    {
        this.parentTab=tab;
    }
    private void showError(String title, String content) 
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
}
