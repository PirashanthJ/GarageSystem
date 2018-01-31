/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parts.GUI;

import common.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.stage.Stage;
import parts.logic.Utility;
import vehicles.logic.Vehicle;
import vehicles.logic.Warranty;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class VehicleDetailsViewController implements Initializable {

    @FXML
    private Label registrationText;
    @FXML
    private Label mileageText;
    @FXML
    private Label modelText;
    @FXML
    private Label makeText;
    @FXML
    private Label engineSizeText;
    @FXML
    private Label fuelTypeText;
    @FXML
    private Label colourText;
    @FXML
    private Label warrantyExpiryText;
    @FXML
    private Label warrantyCompanyText;
    @FXML
    private Label warrantyCompanyAddressText;
    @FXML
    private Label motText;
    @FXML
    private Label dateOfLastServiceText;
    @FXML
    private Label warrantyExpLabel;
    @FXML
    private Label warrantyCompLabel;
    @FXML
    private Label warrantyCompAddLabel;
    @FXML
    private Label messageNoWarranty;
    @FXML
    private Separator warrantySeparator;
    @FXML
    private Label warrantyStatusText;
    @FXML
    private Label warrantyStatLabel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    /**
     * Initializes the view to display the part information. Handles cases in which the vehicle
     * does not have a warranty or an active warranty and when the vehicle does have an active warranty.
     * Also handles the case in which the date of last service is nu;;. 
     * 
     * @param vehicle 
     */
    public void initializeTextViews(Vehicle vehicle){
        registrationText.setText(vehicle.getRegistration());
        if(vehicle.getMileage() == null){
            mileageText.setText("No Mileage Recorded");
        }else{
            mileageText.setText(String.valueOf(vehicle.getMileage()));
        }
        
        motText.setText(vehicle.getMotRenewal());
        modelText.setText(vehicle.getModel());
        makeText.setText(vehicle.getMake());
        engineSizeText.setText(String.valueOf(vehicle.getEngineSize()));
        fuelTypeText.setText(vehicle.getFuelType());
        colourText.setText(vehicle.getColour());
        String getDateOfLastService = "SELECT DateOfLastService FROM Vehicle WHERE VehicleID=" + vehicle.getID();
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        ResultSet rs = null;
        
        String getWarranty = "SELECT WarrantyID FROM Vehicle WHERE VehicleID=" + vehicle.getID();
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(getWarranty);
            if(rs.getInt("WarrantyID") == 0){
                warrantyExpiryText.setVisible(false);
                warrantyCompanyText.setVisible(false);
                warrantyCompanyAddressText.setVisible(false);
                warrantyExpLabel.setVisible(false);
                warrantyCompLabel.setVisible(false);
                warrantyCompAddLabel.setVisible(false);
                warrantyStatLabel.setVisible(false);
                warrantyStatusText.setVisible(false);
                messageNoWarranty.setVisible(true);
            }else{
                String getWarrantyInfo = "SELECT * FROM Warranty WHERE WarrantyID=" + rs.getInt("WarrantyID");
                rs = statement.executeQuery(getWarrantyInfo);
                warrantyExpiryText.setText(rs.getString("dateOfExpiry"));
                warrantyCompanyText.setText(rs.getString("nameOfCompany"));
                warrantyCompanyAddressText.setText(rs.getString("addressOfCompany"));

                String currentDate = db.getCurrentDate();

                Date current = Utility.convertStringToDate(currentDate, Database.INSIDE_DB_DATE_FORMAT);
                Date end = Utility.convertStringToDate(rs.getString("dateOfExpiry"), Database.INSIDE_DB_DATE_FORMAT);
                // if the current date is after the end warranty date, it is false.
                // if the current date is equal to or before warranty end, it is true.
                if(current.compareTo(end) > 0){
                    warrantyStatusText.setText("Valid");
                }else{
                    warrantyStatusText.setText("Invalid");
                }
            }
            rs = statement.executeQuery(getDateOfLastService);
            if(rs.getString("dateOfLastService") == null){
                dateOfLastServiceText.setText("No Record of Last Service");
            }else{
                dateOfLastServiceText.setText(rs.getString("DateOfLastService"));
            }
        
        } catch (SQLException ex) {
            Logger.getLogger(VehicleDetailsViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Closes the popup 
     * @param event The event that causes this method to run
     */
    @FXML
    private void close(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
}
