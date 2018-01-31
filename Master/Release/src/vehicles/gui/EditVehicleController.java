package vehicles.gui;

import common.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vehicles.logic.Vehicle;
import vehicles.logic.Warranty;

import java.io.IOException;
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
 * Menu for editing the current vehicle's information like warranty, customer or manufacturer
 */
public class EditVehicleController implements Initializable{

    @FXML
    private Button save, cancel, warrantyChoice;
    @FXML
    private TextField modelField,  makeField, regNumField, colourField, fuelTypeField, engSizeField;
    @FXML
    private DatePicker dateChoice;
    @FXML
    private ChoiceBox<String> customerChoice, typeChoice;

    private Vehicle vehicle;
    private String model, make, registration, colour, fuelType, engineSize, type, mot, customerName;
    private static final Database DB = Database.getInstance();
    private ArrayList<Integer> customerID = new ArrayList<>();
    private int vehicleID;
    private int currentCustomerID = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        customerChoice();

        // Listener that checks if the user chose a different customer.
        customerChoice.valueProperty().addListener((ov, oldVal, newVal) ->
                currentCustomerID = customerID.get(customerChoice.getSelectionModel().getSelectedIndex()));

        typeChoice.setItems(FXCollections.observableArrayList("car", "van", "truck"));

    }

    /**
     * Method that gets the data from the other Window
     * @param v teh vehicle object that gets passed.
     */
    void sendVehicleData(Vehicle v) {

        vehicle = v;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        model           = vehicle.getModel();
        make            = vehicle.getMake();
        registration    = vehicle.getRegistration();
        colour          = vehicle.getColour();
        fuelType        = vehicle.getFuelType();
        engineSize      = vehicle.getEngineSize();
        type            = vehicle.getVehicleType();
        vehicleID       = vehicle.getID();
        customerName    = vehicle.getCustomerName();

        LocalDate date = LocalDate.parse(vehicle.getMotRenewal(), formatter); // Gets MoT Value

        modelField.setText(model);
        makeField.setText(make);
        regNumField.setText(registration);
        colourField.setText(colour);
        fuelTypeField.setText(fuelType);
        engSizeField.setText(engineSize);
        dateChoice.setValue(date);
        typeChoice.getSelectionModel().select(type);
        customerChoice.getSelectionModel().select(customerName);

        if (Warranty.warrantyCheck(vehicleID)) {
            warrantyChoice.setText("Edit Warranty");
        } else {
            warrantyChoice.setText("Add Warranty");
        }
    }

    /**
     * Method that edits the Vehicle data.
     */
    public void editVehicle(){

        LocalDate date = dateChoice.getValue();

        model = modelField.getText();
        make = makeField.getText();
        registration = regNumField.getText().toUpperCase();
        colour = colourField.getText();
        fuelType = fuelTypeField.getText();
        engineSize = engSizeField.getText();
        type = typeChoice.getSelectionModel().getSelectedItem();
        mot = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Vehicle v = new Vehicle(vehicleID, model, make, registration, fuelType, colour, mot, type, engineSize);
        if (currentCustomerID != -1) {
            v.setCustomer(currentCustomerID);
        }

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Vehicle was successfully edited");
        successAlert.showAndWait();

        Stage stage = (Stage) save.getScene().getWindow();
        stage.close();
    }

    /**
     * Loads up all the current customer names to the drop-down menu.
     */
    private void customerChoice() {

        Connection connection = DB.getConnection();
        ArrayList<String> customerData = new ArrayList<>();
        customerID = new ArrayList<>();

        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT CustomerID, Firstname, Surname FROM Customer");
            while (rs.next()) {

                customerData.add(rs.getString("Firstname") + " " + rs.getString("Surname"));
                customerID.add(rs.getInt("CustomerID"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e);
        }

        ObservableList<String> customerObservableList = FXCollections.observableArrayList(customerData);
        customerChoice.setItems(customerObservableList);
    }

    /**
     * Helps handle the ADD/EDIT warranty button.
     * If the vehicle doesn't have a warranty or if the warranty has expired then the option is Add Warranty.
     * if the vehicle has a warranty then the Button becomes Edit Warranty.
     */
    public void warrantyButtonHandler() {
        try {

            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getResource("AddEditWarranty.fxml").openStream());
            AddEditWarrantyController controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            if (warrantyChoice.getText().equals("Edit Warranty")) {
                controller.sendVehicleData(vehicle, "edit");
            } else {
                warrantyChoice.setText("Edit Warranty");
                controller.sendVehicleData(vehicle, "add");
            }
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();

        } catch(IOException e) {
            System.err.println("Error: " + e);
        }
    }

    // Helps handle the cancel button.
    public void cancel() {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }
}
