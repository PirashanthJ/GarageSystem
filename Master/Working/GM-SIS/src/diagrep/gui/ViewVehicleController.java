/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diagrep.gui;


import common.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import vehicles.logic.Warranty;


/**
 * FXML Controller class
 *
 * @author adamlaraqui
 */
public class ViewVehicleController implements Initializable {
    
    @FXML
    private Label mileage;
    @FXML
    private Label lastService;
    @FXML
    private Label vehReg;
    @FXML
    private Label warrantyInfo;
    @FXML
    private Label makeModel;
    @FXML
    private Label engineSize;
    @FXML
    private Label fuelType;
    @FXML
    private Label colour;
    @FXML
    private Label kind;
    @FXML
    private Label motrenewal;
    @FXML
    private Button closeButton;
    
    /**
     * Initialises the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
    }
    
    public void setVehicle(int vehicleID) throws SQLException {
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        String getVehicleQuery = "SELECT * FROM Vehicle INNER JOIN VehicleInfo ON Vehicle.VehicleID = VehicleInfo.VehicleID LEFT JOIN Warranty ON Vehicle.WarrantyID = Warranty.WarrantyID WHERE Vehicle.VehicleID = "+vehicleID;
        PreparedStatement ps = con.prepareStatement(getVehicleQuery);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            mileage.setText(rs.getString("CurrentMileage"));
            lastService.setText(rs.getString("DateOfLastService"));
            vehReg.setText(rs.getString("Registration"));
            if(rs.getInt("WarrantyID")==0) {
                warrantyInfo.setText( "Not In Warranty\nVehicle does not have a warranty company..." );
            } else {
                warrantyInfo.setText((Warranty.warrantyCheck(vehicleID)?"In Warranty":"Not In Warranty")
                                    + " (Expiry Date: " + rs.getString("dateOfExpiry") + ")"
                                    + "\nCompany: " + rs.getString("nameOfCompany")
                                    + "\nAddress: " + rs.getString("addressOfCompany") );
            }
            makeModel.setText(rs.getString("Make") + " (" + rs.getString("Model") + ")");
            engineSize.setText(rs.getString("EngineSize"));
            fuelType.setText(rs.getString("FuelType"));
            colour.setText(rs.getString("Colour"));
            kind.setText(rs.getString("VehicleKind"));
            motrenewal.setText(rs.getString("MoTRenewal"));
            
        }
    }
    
    public void close(ActionEvent Event){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
