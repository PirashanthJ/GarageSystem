package vehicles.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import vehicles.logic.Vehicle;
import vehicles.logic.Warranty;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * @author Ilias.
 * Small Pop-up that allows editing of Warranty.
 */
public class AddEditWarrantyController implements Initializable {

    @FXML
    private TextField companyField, addressField;
    @FXML
    private Label titleLabel;
    @FXML
    private DatePicker dateField;
    @FXML
    private Button done;

    private Vehicle vehicle;
    private String mode;
    private String company, address, expiry;
    private int wID = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Not needed
    }

    /**
     * Gets the vehicle data from the previous window
     * @param v the assigned vehicle object
     * @param mode choice of mode, either edit or add.
     */
    void sendVehicleData(Vehicle v, String mode) {
        this.mode    = mode;
        this.vehicle = v;

        if (this.mode.equals("edit")) {
            titleLabel.setText("Edit Warranty");

            Warranty w = new Warranty(v.getID());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            this.company = w.getCompany();
            this.address = w.getAddress();
            this.expiry  = w.getExpiry();
            this.wID     = w.wID;

            LocalDate date = LocalDate.parse(this.expiry, formatter);

            companyField.setText(this.company);
            addressField.setText(this.address);
            dateField.setValue(date);
        } else {
            titleLabel.setText("Add Warranty");
        }
    }

    /**
     * The method gets called when the Done button gets pressed.
     */
    public void saveWarrantyData() {

        LocalDate date = dateField.getValue();
        this.company   = companyField.getText();
        this.address   = addressField.getText();
        this.expiry    = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (wID != 0){
            new Warranty(this.wID, this.company, this.address, this.expiry);
        } else {
            new Warranty(this.company, this.address, this.expiry, this.vehicle.getID());
        }

        Stage stage = (Stage) done.getScene().getWindow();
        stage.close();
    }
}
