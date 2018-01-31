/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parts.GUI;

import common.Database;
import customers.logic.Customer;
import diagrep.gui.*;
import diagrep.logic.DiagRepair;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import parts.logic.InstalledPart;
import parts.logic.Utility;
import vehicles.logic.Vehicle;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class ShowPartsInVehicleController implements Initializable {

    @FXML
    private TextField vehicleRegistrationText;
    private Vehicle vehicle;
    private Customer customer;
    private ArrayList<InstalledPart> installedParts;
 
    @FXML
    private TableView<RowForTable> partTable;
    @FXML
    private TableColumn<RowForTable, String> partNameColumn;
    @FXML
    private TableColumn<RowForTable, String> installDateColumn;
    @FXML
    private TableColumn<RowForTable, String> warrantyEndDateColumn;
    
    private Tab parentTab;
    private Tab bookingsTab;
    @FXML
    private Button showButton;
    @FXML
    private Button uninstallButton;
    @FXML
    private Button installButton;
    @FXML
    private Button vehicleButton;
    @FXML
    private Button customerButton;
    @FXML
    private Button viewBookingsButton;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Code by Alexander.Berg (StackOverFlow) - Detect doubleclock on row of TableView JavaFX
        // shows popup with part information on double click
        partTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override 
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    FXMLLoader loader = new FXMLLoader();
                    Pane root = null;
                    try {
                        root = loader.load(getClass().getResource("editInstalledPart.fxml").openStream());
                    } catch (IOException ex) {
                        Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    EditInstalledPartController control = (EditInstalledPartController)loader.getController();
                    control.initializeTextViews(partTable.getSelectionModel().getSelectedItem().getInstalledPart());
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(root);
                    stage.setResizable(false);
                    stage.setScene(scene);
                    stage.setTitle("Edit Installed Part");
                    stage.showAndWait();
                    ArrayList<InstalledPart> installedParts = getInstalledParts(vehicle);
                    List<RowForTable> rows = new ArrayList<>();

                    for(int i = 0; i < installedParts.size(); i++){
                        rows.add(new RowForTable(installedParts.get(i)));
                    }

                    partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
                    installDateColumn.setCellValueFactory(new PropertyValueFactory<>("installDate"));
                    warrantyEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("warrantyEndDate"));
                    partTable.setItems(FXCollections.observableList(rows));
                }
            }
        });
        partTable.setPlaceholder(new Label("No Parts to Display Currently"));
        // Code by ItachiUchiha (StackOverflow) - JavaFX TextField : Automatically transform text to uppercase
        vehicleRegistrationText.textProperty().addListener((ov, oldValue, newValue) -> {
            vehicleRegistrationText.setText(newValue.toUpperCase());
        });
    }    

    /**
     * Takes the user to the part home page
     * @param event The event that causes this method to run
     */
    @FXML
    private void goHome(ActionEvent event) {
        // gets current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("searchForParts.fxml"));
        try {
            // load document I want to go to
            root = loader.load();
            SearchForPartsController controller = loader.getController();
            controller.setTabs(parentTab, bookingsTab);
            parentTab.setContent(root);
            
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Initializes the view with for the vehicle given.
     * @param vehicle The vehicle to 
     */
    public void initializeView(Vehicle vehicle){
        this.vehicle = vehicle;
        this.installedParts = getInstalledParts(vehicle);
        vehicleRegistrationText.setText(vehicle.getRegistration());
        vehicleRegistrationText.setEditable(false);
        showButton.setDisable(true);
        this.customer = getCustomerFromVehicle(vehicle);
        this.installedParts = getInstalledParts(vehicle);
        List<RowForTable> rows = new ArrayList<>();
        
        for(int i = 0; i < installedParts.size(); i++){
            rows.add(new RowForTable(installedParts.get(i)));
        }

        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        installDateColumn.setCellValueFactory(new PropertyValueFactory<>("installDate"));
        warrantyEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("warrantyEndDate"));
        partTable.setItems(FXCollections.observableList(rows));
        uninstallButton.setDisable(false);
        installButton.setDisable(false);
        viewBookingsButton.setDisable(false);
        vehicleButton.setDisable(false);
        customerButton.setDisable(false);
    }
    
    /**
     * Initializes the view if there was no vehicle passed  
     */
    @FXML
    public void initializeView(){
        String registration = vehicleRegistrationText.getText();
        if(registration.contains("'")){
            showAlert("No Vehicle Found", "There is no vehicle in the system with the registration number " + registration);
            return;
        }
        String searchForRegistration = "SELECT VehicleID FROM VehicleInfo WHERE Registration='" + registration + "'";
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null; 
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(searchForRegistration);
            // there is no vehicle matching the registration number
            if(rs.isClosed()){
                showAlert("No Vehicle Found", "There is no vehicle in the system with the registration number " + registration);
            }else{
                partTable.setPlaceholder(new Label("No Parts Installed on Vehicle Currently"));
                initializeView(new Vehicle(rs.getInt("VehicleID")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ShowPartsInVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }
    
    /**
     * Gets the customer from the given vehicle 
     * 
     * @param v The vehicle to get the customer for
     * @return the customer object for the vehicle
     */
    private Customer getCustomerFromVehicle(Vehicle v){
        String query = "SELECT Customer.CustomerID, CustomerType, Firstname, Surname, Address, Postcode, Phone, Email" 
                + " FROM Customer, Vehicle WHERE VehicleID = " + v.getID() + " AND Vehicle.CustomerID=Customer.CustomerID";
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        Customer customer = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            customer = new Customer(rs.getInt("CustomerID"), rs.getString("CustomerType"), 
                    rs.getString("Firstname"), rs.getString("Surname"), rs.getString("Address"),
                    rs.getString("Postcode"), rs.getString("Phone"), rs.getString("Email"));
        } catch (SQLException ex) {
            Logger.getLogger(ShowPartsInVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return customer;
    }
    
    /**
     * Gets parts installed in the vehicle
     * 
     * @param v The vehicle to get the parts for
     * @return An arraylist with the installed parts
     */
    private ArrayList<InstalledPart> getInstalledParts(Vehicle v){
        String query = "SELECT PartID FROM PartInVehicle WHERE VehicleID=" + v.getID();
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        ArrayList<InstalledPart> installedParts = new ArrayList<>();
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()){
                installedParts.add(new InstalledPart(rs.getInt("PartID")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ShowPartsInVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return installedParts;
    }

    /**
     * Shows the vehicle detail screen 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void showVehicleDetails(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("vehicleDetailsView.fxml"));
        Pane root = null;
        try {
            root = loader.load();
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        VehicleDetailsViewController control = (VehicleDetailsViewController)loader.getController();
        control.initializeTextViews(vehicle);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Vehicle Details");
        stage.showAndWait();
    }

    /**
     * Displays a popup of the customer details for the given vehicle 
     * @param event The event that causes this method to run
     */
    @FXML
    private void showCustomerDetails(ActionEvent event) {
            FXMLLoader loader = new FXMLLoader();
            Pane root = null;
            try {
                root = loader.load(getClass().getResource("CustomerDetailsView.fxml").openStream());
            } catch (IOException ex) {
                Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
            }
            CustomerDetailsViewController control = (CustomerDetailsViewController)loader.getController();
            control.initializeTextViews(customer);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node)event.getSource()).getScene().getWindow());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Customer Details");
            stage.showAndWait();
    }

    /**
     * Takes the user to the page to search for installed parts generally 
     * @param event The event that causes this method to run
     */
    @FXML
    private void goToSearchForUsedParts(ActionEvent event) {
        // gets current stage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("advancedSearchForUsedParts.fxml"));
        Parent root;
        try {
            // load document I want to go to
            root = loader.load();
            AdvancedSearchForUsedPartsController controller = loader.getController();
            controller.setTabs(parentTab, bookingsTab);
            parentTab.setContent(root);
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Attempts to uninstall a part, shows a dialog of result 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void uninstallPart(ActionEvent event) {
        RowForTable partSelected = partTable.getSelectionModel().getSelectedItem();
        if(partSelected != null){
            boolean yes = showYesNo("Confirm Uninstall", "Are you sure you wish to uninstall " + partSelected.getPartName() + " from Vehicle: " + vehicle.getRegistration() + "\n" +
                    "It will remove the part from any currently active diagnosis and repair bookings, and that booking's corresponding incomplete SPC bookings.");
            if(yes){
                InstalledPart partInstalled = partSelected.getInstalledPart();
                boolean success = partInstalled.uninstall();
                if(success){
                    // update list of installed parts and update tableview
                    installedParts = getInstalledParts(vehicle);
                    ArrayList<RowForTable> rows = new ArrayList<>();
                    for(int i = 0; i < installedParts.size(); i++){
                        rows.add(new RowForTable(installedParts.get(i)));
                    }
                    partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
                    installDateColumn.setCellValueFactory(new PropertyValueFactory<>("installDate"));
                    warrantyEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("warrantyEndDate"));
                    partTable.setItems(FXCollections.observableList(rows));
                }else{
                    showAlert("Part Cannot Be Uninstalled", "Currently parts on this vehicle cannot be uninstalled, as it is sent to away for an SPC repair.\n" 
                            + "Please ensure the SPC repair is completed and try again.");
                }
            }
        }else{
            showAlert("No Part Selected",
                    "Please select the part you wish to uninstall");
        }
    }
    
    /**
     * Shows a dialog with information for the user
     * @param title The title of the dialog 
     * @param content The content of the dialog
     */
    private static void showAlert(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * An alert asking the user a yes or no question.
     * 
     * @param title The title of the dialog
     * @param context The content of the dialog
     * @return true if the user selected yes, false if other
     */
    private static boolean showYesNo(String title, String context){
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType[] buttons = {yes, no};
        Alert alert = new Alert(AlertType.WARNING, context, buttons);
        alert.setHeaderText(title);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yes){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Provides the user the option to install parts manually without a booking, 
     * go to a currently active booking to install if one exists, or create 
     * a new booking to install parts in 
     * 
     * @param event the even that causes this method to run
     */
    @FXML
    private void addPartToVehicle(ActionEvent event) {
        DiagRepair currentBooking = getCurrentBooking(vehicle);
       
        // there is an active current booking 
        if(currentBooking != null){
            if(currentBooking.getFault().equals("N/A")){
                boolean yes = showYesNo("Caution", "There is a currently active booking in which you can install parts for this vehicle.\n" + 
                    "If you do not install through a booking there will be no way to track the cost of this service.\n" + 
                    "The fault is not yet diagnosed, would you like to view the booking, diagnose the fault, then install a part?");
                if(yes){
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("diagrep/gui/BookingFXML.fxml"));
                    // gets current stage
                    Pane root = null;
                    try {
                        root = loader.load(); 
                        BookingController control = (BookingController)loader.getController();
                        control.setTabs(bookingsTab, parentTab);
                        control.setBookingToEdit(currentBooking);
                        bookingsTab.setContent(root);
                        bookingsTab.getTabPane().getSelectionModel().select(bookingsTab);
                    } catch (IOException ex) {
                        Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                }else{
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("installPartNotThroughBooking.fxml"));
                        Parent root = loader.load();
                        InstallPartNotThroughBookingController control = loader.getController();
                        control.initializeWithVehicle(vehicle, installedParts);
                        Stage stage = new Stage();
                        stage.setResizable(false);
                        stage.initModality(Modality.WINDOW_MODAL);
                        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.setTitle("Install Type");
                        stage.showAndWait();
                    } catch (IOException ex) {
                        Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return;
            }
            boolean yes = showYesNo("Caution", "There is a currently active booking in which you can install parts for this vehicle.\n" + 
                    "If you do not install through a booking there will be no way to track the cost of this service.\n" + 
                    "Would you like to install the new part through that booking?");
            if(yes){
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("PartsForBookingView.fxml"));
                    // load document I want to go to
                    Parent root = loader.load();
                    PartsForBookingViewController controller = loader.getController();
                    controller.initializeWithBooking(currentBooking);
                    controller.setTabs(parentTab, bookingsTab);
                    parentTab.setContent(root);
                    TabPane tabPane = parentTab.getTabPane();
                    ObservableList<Tab> tabs = tabPane.getTabs();
                    for(int i = 0; i < tabs.size(); i++){
                        if(!tabs.get(i).isSelected()){
                            tabs.get(i).setDisable(true);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else if (!yes){ // part installed manually
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("installPartNotThroughBooking.fxml"));
                    Parent root = loader.load();
                    InstallPartNotThroughBookingController control = loader.getController();
                    control.initializeWithVehicle(vehicle, installedParts);
                    Stage stage = new Stage();
                    stage.setResizable(false);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("Install Type");
                    stage.showAndWait();
                } catch (IOException ex) {
                    Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }else{ // give option to create new booking  
            boolean yes = showYesNo("Caution", "You are about to install a part without a booking.\n" +
                    "If you do not install through a booking there will be no way to track the cost of this service.\n" +
                            "Do you wish to be redirected to create a diagnosis and repair booking?");
            if(yes){
                    
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("diagrep/gui/BookingFXML.fxml"));
                // gets current stage
                Pane root = null;
                try {
                    root = loader.load(); 
                    BookingController control = (BookingController)loader.getController();
                    control.setTabs(bookingsTab, parentTab);
                    bookingsTab.setContent(root);
                    bookingsTab.getTabPane().getSelectionModel().select(bookingsTab);
                } catch (IOException ex) {
                    Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }else if(!yes){ // part installed manually
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("installPartNotThroughBooking.fxml"));
                    Pane root = loader.load();
                    InstallPartNotThroughBookingController control = (InstallPartNotThroughBookingController)loader.getController();
                    control.initializeWithVehicle(vehicle, installedParts);
                    Stage stage = new Stage();
                    stage.setResizable(false);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("Install Part");
                    stage.showAndWait();
                } catch (IOException ex) {
                    Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // update list of installed parts and update tableview
        installedParts = getInstalledParts(vehicle);
        ArrayList<RowForTable> rows = new ArrayList<>();
        for(int i = 0; i < installedParts.size(); i++){
            rows.add(new RowForTable(installedParts.get(i)));
        }
        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        installDateColumn.setCellValueFactory(new PropertyValueFactory<>("installDate"));
        warrantyEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("warrantyEndDate"));
        partTable.setItems(FXCollections.observableList(rows));
    }
    
    /**
     * Gets the currently active booking of a vehicle. A booking is currently active 
     * if it is not yet completed, and the start date is after or on the current date 
     * 
     * @param vehicle The vehicle to find the current booking for
     * @return null if there is no current booking, the current booking if there is
     */
    private static DiagRepair getCurrentBooking(Vehicle vehicle){
        Database db = Database.getInstance();
        DiagRepair currentBooking = null;
        // checks for an incomplete booking that is not for a vehicle
        // if it is for a vehicle, mechanics cannot add parts to the booking (this is SPC's job)
        // ASSUMPTION FOR THIS TO WORK: there is only one currently active booking for a vehicle at a time
        // Active Booking: incomplete booking with the earliest start date that has already past. 
        String checkForBooking = "SELECT DiagRepID, MIN(StartDate) FROM DiagRepairBooking WHERE VehicleID=" 
                + vehicle.getID() + " AND Completed = 0 AND sendVehicleToSPC = 0 AND StartDate <= '" + db.getCurrentDateTime() + "'"; 
        Connection conn = db.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(checkForBooking);
            if(!rs.isClosed() && rs.getInt("DiagRepID") != 0){
                int bookingID = rs.getInt("DiagRepID");
                // there is an active booking
                currentBooking = new DiagRepair(bookingID);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ShowPartsInVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return currentBooking;
    }

    /**
     * Shows the past and future bookings for the vehicle 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void viewBookings(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("diagrep/gui/SearchBookingsFXML.fxml"));
        FXMLLoader partLoader = new FXMLLoader(getClass().getResource("searchForParts.fxml"));
        // gets current stage
        Pane root = null;
        Pane partRoot = null;
        try {
            partRoot = partLoader.load();
            SearchForPartsController partControl = partLoader.getController();
            partControl.setTabs(parentTab, bookingsTab);
            parentTab.setContent(partRoot);
            
            root = loader.load(); 
            SearchController control = (SearchController)loader.getController();
            control.viewPastFutureBookings(vehicle.getID());
            control.setTabs(bookingsTab, parentTab);
            bookingsTab.setContent(root);
            bookingsTab.getTabPane().getSelectionModel().select(bookingsTab);

        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public class RowForTable{
        private InstalledPart p;
        
        public RowForTable(InstalledPart p){
            this.p = p;
        }
        
        public String getPartName(){
            return p.getPart().getName();
        }
        
        public String getInstallDate(){
            return p.getInstallDate();
        }
        
        public String getWarrantyEndDate(){
            return p.getWarrantyEndDate();
        }
        
        public InstalledPart getInstalledPart(){
            return p;
        }
    }
    
    /**
     * Sets the parent tab and the bookings tab to display part view properly and 
     * bookings tab where required. 
     * 
     * @param parent The parent tab for the part views
     * @param bookings The tab that contains the booking views
     */
    public void setTabs(Tab parent, Tab bookings){
        parentTab = parent;
        bookingsTab = bookings;
    }
}
