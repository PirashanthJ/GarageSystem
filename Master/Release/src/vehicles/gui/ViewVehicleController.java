package vehicles.gui;

import common.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vehicles.logic.Vehicle;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Ilias.
 * The main GUI controller for the vehicle module.
 */
public class ViewVehicleController implements Initializable {

    @FXML
    public RadioButton car, van, truck, all, hidden;
    @FXML
    public Button addVehicle, editVehicle, deleteVehicle, search;
    @FXML
    public TableView<Vehicle> vehicles;
    @FXML
    public TableColumn<Vehicle, String> make, model, reg, colour, engine, fuel, type, mot, lastBooking, customerName;
    @FXML
    public TextField searchBox;

    private final ToggleGroup groupSearch = new ToggleGroup(); // sets the correct ToggleGroup.

    private static final Database DB = Database.getInstance();

    private String query = "SELECT VehicleID FROM VehicleInfo";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setVehicleViewing();
    }

    private void setVehicleViewing() {

        car.setToggleGroup(groupSearch);
        van.setToggleGroup(groupSearch);
        truck.setToggleGroup(groupSearch);
        all.setToggleGroup(groupSearch);
        hidden.setToggleGroup(groupSearch);

        groupSearch.selectToggle(all);

        // Action listener for when somebody presses the Search button.
        search.setOnAction(e ->{
            String currentQuery = searchBox.getText();
            outputVehicleData("SELECT * FROM VehicleInfo "
                                    +"WHERE Make LIKE '%"+currentQuery+"%' "
                                    +"OR Registration LIKE '%"+currentQuery+"%'");
            if (!(currentQuery.equals(""))) {
                groupSearch.selectToggle(hidden); // Make it hidden only when the user actually wants to search
            }
        });

        // Action Listener for toggling between different options. Helps meet Definition Requirement
        groupSearch.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {

            String sql;
            if (groupSearch.getSelectedToggle() == all) {
                sql = query;
                outputVehicleData(sql);
            } else if (groupSearch.getSelectedToggle() == car) {
                sql = "SELECT VehicleID FROM VehicleInfo WHERE VehicleKind = 'car';";
                outputVehicleData(sql);
            } else if (groupSearch.getSelectedToggle() == van) {
                sql = "SELECT VehicleID FROM VehicleInfo WHERE VehicleKind = 'van';";
                outputVehicleData(sql);
            } else if (groupSearch.getSelectedToggle() == truck){
                sql = "SELECT VehicleID FROM VehicleInfo WHERE VehicleKind = 'truck';";
                outputVehicleData(sql);
            }

        });

        // Shows selected vehicle information.
        vehicles.setRowFactory( tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    showVehicleInfo();
                }
            });
            return row ;
        });

        outputVehicleData(query);
    }

    /**
     * This Method outputs the requested by the query data into the table as a list.
     * @param query String that includes the query that will be sent to the database
     *              (Default value gives all the VehicleInfo data)
     */
    private void outputVehicleData(String query) {
        Connection connection = DB.getConnection();
        ArrayList<Vehicle> vehicleData = new ArrayList<>();

        try {
            ResultSet rs = connection.createStatement().executeQuery(query);
            while (rs.next()){
                vehicleData.add(new Vehicle(rs.getInt("VehicleID")));
            }
        } catch (SQLException e) {
            System.err.println("Error: "+ e);
        }

        make.setCellValueFactory(new PropertyValueFactory<>("make"));
        model.setCellValueFactory(new PropertyValueFactory<>("model"));
        reg.setCellValueFactory(new PropertyValueFactory<>("registration"));
        colour.setCellValueFactory(new PropertyValueFactory<>("colour"));
        fuel.setCellValueFactory(new PropertyValueFactory<>("fuelType"));
        type.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
        mot.setCellValueFactory(new PropertyValueFactory<>("motRenewal"));
        engine.setCellValueFactory(new PropertyValueFactory<>("engineSize"));
        customerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        lastBooking.setCellValueFactory(new PropertyValueFactory<>("lastService"));


        ObservableList<Vehicle> vehicleObservableList = FXCollections.observableArrayList(vehicleData);
        vehicles.setItems(vehicleObservableList);
    }

    // Refreshes the whole table and shows the vehicles that meet the requirements of the query.
    private void refreshTable() {
        outputVehicleData(query);
    }

    // Calls the add vehicle menu.
    public void addVehicle() {
        try {

            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getResource("AddVehicle.fxml").openStream());
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
            refreshTable();

        } catch(IOException e) {
            System.err.println("Error: "+e);
        }
    }

    // Calls the edit vehicle menu by getting the data from the selected vehicle.
    public void editVehicle() {
        try {

            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getResource("EditVehicle.fxml").openStream());
            EditVehicleController controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            Vehicle selectedVehicle = vehicles.getSelectionModel().getSelectedItem();
            controller.sendVehicleData(selectedVehicle);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
            refreshTable();

        } catch(IOException e) {
            System.err.println("Error: "+e);
        } catch(NullPointerException e) {

            Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
            errorAlert.setTitle("Unable to Edit Vehicle Information");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Please Select Vehicle");
            errorAlert.showAndWait();
        }
    }

    // Shows the current vehicle's information.
    private void showVehicleInfo() {
        try {

            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getResource("ViewVehicleInfo.fxml").openStream());
            ViewVehicleInfoController controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            Vehicle selectedVehicle = vehicles.getSelectionModel().getSelectedItem();
            controller.sendVehicleData(selectedVehicle);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
            refreshTable();

        } catch(IOException e) {
            System.err.println("Error: "+e);
        }
    }

    // Deletes the selected vehicle but first asks for confirmation from user.
    public void deleteFunction() {
        try {

            Vehicle selectedVehicle = vehicles.getSelectionModel().getSelectedItem();

            if (!(selectedVehicle.equals(null))) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Confirmation");
                alert.setHeaderText("Deleting selected vehicle.");
                alert.setContentText("Are you sure you want to do this?");


                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    selectedVehicle.deleteVehicleData();
                    refreshTable();
                }
            }

        } catch(NullPointerException e) {
            Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
            errorAlert.setTitle("Unable to Delete Vehicle Information");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Please Select Vehicle");
            errorAlert.showAndWait();
        }

    }
}


