package vehicles.gui;

import common.Database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import vehicles.logic.Vehicle;
import vehicles.logic.Warranty;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author Ilias.
 */
public class AddVehicleController implements Initializable{

    @FXML
    private Button save, cancel;
    @FXML
    private ChoiceBox<String> templateChoiceBox, customerChoiceBox, typeChoice;
    @FXML
    private TextField modelField, makeField, regNumField, colourField, fuelTypeField, engSizeField, addressField, companyField;
    @FXML
    private DatePicker motField, expiryField;
    @FXML
    private CheckBox inWarrantyCheck;

    private String model, make, registration, colour, fuelType, engineSize, type, mot, company, address, expiry;
    private String templateModel, templateMake, templateFuel, templateEngine, templateType;
    private static final Database DB = Database.getInstance();
    private ArrayList<Integer> customerID = new ArrayList<>();
    private boolean inWarranty = false;
    private int id = -1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        templateChoice();
        customerChoice();

        typeChoice.setItems(FXCollections.observableArrayList("car", "van", "truck"));
        typeChoice.getSelectionModel().selectFirst();

        // Helps set the desired customer for the vehicle
        customerChoiceBox.valueProperty().addListener((ov, oldVal, newVal) ->
                id = customerID.get(customerChoiceBox.getSelectionModel().getSelectedIndex()));

        // Action Listener that helps handle the creation of a vehicle from the Template table.
        templateChoiceBox.valueProperty().addListener((ov, oldVal, newVal) -> {
                searchTemplate(templateChoiceBox.getSelectionModel().getSelectedItem());

                modelField.setText(templateModel);
                makeField.setText(templateMake);
                fuelTypeField.setText(templateFuel);
                engSizeField.setText(templateEngine);
                typeChoice.getSelectionModel().select(templateType);
            });

        // This works like a toggle and helps add the warranty info to a vehicle.
        inWarrantyCheck.selectedProperty().addListener((ov, old_val, new_val) -> {
                if (inWarranty) {
                    addressField.setDisable(true);
                    companyField.setDisable(true);
                    expiryField.setDisable(true);

                    inWarranty = false;
                } else {
                    addressField.setDisable(false);
                    companyField.setDisable(false);
                    expiryField.setDisable(false);

                    inWarranty = true;
                }
        });
    }

    /**
     * The method that gets called when the save button is pressed.
     */
    public void saveVehicle() {

        LocalDate date1 = motField.getValue();

        model           = modelField.getText();
        make            = makeField.getText();
        registration    = regNumField.getText().toUpperCase();
        colour          = colourField.getText();
        fuelType        = fuelTypeField.getText();
        engineSize      = engSizeField.getText();
        type            = typeChoice.getSelectionModel().getSelectedItem();
        mot             = date1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (id != -1) { // Only goes in if Customer was selected.

            Vehicle v = new Vehicle(model, make, registration, fuelType, colour, mot, type, engineSize);
            v.setCustomer(id);

            if (inWarranty) {
                LocalDate date2 = expiryField.getValue();

                company = companyField.getText();
                address = addressField.getText();
                expiry  = date2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                new Warranty(company, address, expiry, v.getID());
            }
        } else {

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("No Customer added!");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Customer information needs to be chosen");
            errorAlert.showAndWait();
            return;
        }

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Vehicle was successfully created");
        successAlert.showAndWait();

        Stage stage = (Stage) save.getScene().getWindow();
        stage.close();
    }

    // functionality for the cancel button
    public void cancel(){
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Collects the information for the drop down template menu.
     */
    private void templateChoice() {

        Connection connection = DB.getConnection();
        ArrayList<String> templateData = new ArrayList<>();

        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM Template");
            while (rs.next()) {
                templateData.add(rs.getString("Make") + " " + rs.getString("Model"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e);
        }

        ObservableList<String> templateObservableList = FXCollections.observableArrayList(templateData);
        templateChoiceBox.setItems(templateObservableList);
    }

    /**
     * Assigns the chosen template vehicle data to the textFields
     * @param model car model information.
     */
    private void searchTemplate(String model) {

        Connection connection = DB.getConnection();
        String[] name =  model.split(" ",2);
        try {

            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM Template "
                                                                        +"WHERE Model='"+name[1]+"'");

            this.templateMake   = rs.getString("Make");
            this.templateModel  = rs.getString("Model");
            this.templateFuel   = rs.getString("FuelType");
            this.templateEngine = rs.getString("EngineSize");
            this.templateType   = rs.getString("VehicleKind");

        } catch (SQLException e) {
            System.err.println("Error: " + e);
        }
    }

    /**
     * Generates the data for the dropdown menu.
     */
    private void customerChoice() {
        Connection connection = DB.getConnection();
        ArrayList<String> customerData = new ArrayList<>();
        customerID = new ArrayList<>();

        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT CustomerID, Firstname, Surname "
                                                                    + "FROM Customer");
            while (rs.next()) {

                customerData.add(rs.getString("Firstname") + " " + rs.getString("Surname"));
                customerID.add(rs.getInt("CustomerID"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e);
        }

        ObservableList<String> customerObservableList = FXCollections.observableArrayList(customerData);
        customerChoiceBox.setItems(customerObservableList);
    }
}
