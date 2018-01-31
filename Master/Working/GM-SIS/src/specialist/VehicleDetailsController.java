/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import common.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
/**
 * FXML Controller class
 *
 * @author Pirashanth
 */
public class VehicleDetailsController implements Initializable 
{

    Database db= Database.getInstance();
    Connection connection=db.getConnection();
    /**
     * pop to show vehicle details
     */
    @FXML
    private TextField registration;
    @FXML
    private TextField make;
    @FXML
    private TextField model;
    @FXML
    private TextField engineSize;
    @FXML
    private TextField fuelType;
    @FXML
    private TextField colour;
    @FXML
    private TextField mileage;
    private Tab parentTab;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        // TODO
    }
    @FXML
    public void vehicleDetails(String vehReg)
    {
       Statement statement;ResultSet rs;
       registration.setEditable(false);
       make.setEditable(false);
       model.setEditable(false);
       engineSize.setEditable(false);
       fuelType.setEditable(false);
       colour.setEditable(false);
       mileage.setEditable(false);
       try
       {
           statement= connection.createStatement();
           rs=statement.executeQuery("select VehicleInfo.Registration,VehicleInfo.Colour,Vehicle.CurrentMileage,VehicleInfo.Model,VehicleInfo.Make,VehicleInfo.EngineSize,VehicleInfo.FuelType from Vehicle inner join VehicleInfo on Vehicle.VehicleID=VehicleInfo.VehicleID where VehicleInfo.Registration='"+vehReg+"'");
           registration.setText(rs.getString("Registration"));
           make.setText(rs.getString("Make"));
           model.setText(rs.getString("Model"));
           engineSize.setText(rs.getString("EngineSize"));
           fuelType.setText(rs.getString("FuelType"));
           colour.setText(rs.getString("Colour"));
           mileage.setText(rs.getString("CurrentMileage"));
       }
       catch(NullPointerException e)
       {
            showError("Unable to retrieve", "Cannot get all the details ");
       } 
       catch (IllegalArgumentException e) 
       {
            showError("Validation Error", e.getMessage());
       } 
       catch (SQLException ex) 
       {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(VehicleDetailsController.class.getName()).log(Level.SEVERE, null, ex);
       }
        
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
