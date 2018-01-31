package vehicles.gui;

import common.Database;
import customers.logic.BookingDetails;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import vehicles.logic.Vehicle;
import vehicles.logic.Warranty;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author Ilias.
 */
public class ViewVehicleInfoController implements Initializable{

    @FXML
    public TableView<BookingDetails> bookings;
    @FXML
    public TableColumn<BookingDetails, String> columnStartDate, columnEndDate;
    @FXML
    public Label vehicleInfo, customerInfo;
    @FXML
    public Label expiry;
    @FXML
    public Label company;
    @FXML
    public Label address;
    @FXML
    public TitledPane warrantyPane;
    @FXML
    public Button done;

    private Vehicle vehicle;
    private String model, make, registration, customerName;
    private int vehicleID;
    private static final Database DB = Database.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resourcesRL){
        // Not needed
    }

    /**
     * Gets the vehicle information from the previous Windows
     * @param v vehicle object
     */
    void sendVehicleData(Vehicle v){

        vehicle = v;

        this.model = vehicle.getModel();
        this.make = vehicle.getMake();
        this.registration = vehicle.getRegistration();
        this.customerName = vehicle.getCustomerName();
        this.vehicleID = vehicle.getID();

        setView();
    }

    /**
     * Prepares all the components of the Windows. Making the Warranty information disabled or not.
     */
    private void setView() {

        vehicleInfo.setText(make + " " + model + " - " + registration);
        customerInfo.setText(customerName);

        if (Warranty.warrantyCheck(vehicleID)) {
            warrantyPane.setDisable(false);

            Warranty warranty = new Warranty(vehicleID);

            company.setText(warranty.getCompany());
            address.setText(warranty.getAddress());
            expiry.setText(warranty.getExpiry());
        } else {
            warrantyPane.setDisable(true);
        }

        displayBookingDetails();
    }

    /**
     * Displays a table with all past and future booking details.
     */
    private void displayBookingDetails() {
        ArrayList<BookingDetails> bookingsData = new  ArrayList<>();

        try {

            Connection conn = DB.getConnection();
            String bookingQuery = "SELECT StartDate, EndDate "
                                + "FROM DiagRepairBooking "
                                + "WHERE DiagRepairBooking.VehicleID = '"+vehicleID+"';";
            ResultSet rs = conn.createStatement().executeQuery(bookingQuery);
            while (rs.next()) {
                bookingsData.add( new BookingDetails(vehicle.getRegistration(), rs.getString("StartDate"), rs.getString("EndDate")));
            }
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }

        columnStartDate.setCellValueFactory(new PropertyValueFactory<>("StartDate"));
        columnEndDate.setCellValueFactory(new PropertyValueFactory<>("EndDate"));


        bookings.setItems(null);
        ObservableList<BookingDetails> bookingsObsList = FXCollections.observableArrayList(bookingsData);
        bookings.setItems(bookingsObsList);
    }

    // Goes to previous window after done gets pressed.
    public void cancel() {
        Stage stage = (Stage) done.getScene().getWindow();
        stage.close();
    }
}
