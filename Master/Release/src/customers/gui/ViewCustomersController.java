package customers.gui;

import vehicles.logic.Vehicle;
import customers.logic.Customer;
import common.Database;
import customers.logic.BillRow;
import diagrep.gui.BookingController;
import customers.logic.BookingDetails;
import diagrep.gui.SearchController;
import diagrep.gui.ViewVehicleController;
import diagrep.logic.DiagRepair;
import java.io.IOException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import parts.GUI.PartUsedDetailsController;
import parts.GUI.SearchForPartsController;
import parts.logic.InstalledPart;
import specialist.MainPageController;

/**
 * FXML Controller class
 *
 * @author ec15072
 */
public class ViewCustomersController implements Initializable {

    @FXML
    private TextField searchBox;
    @FXML
    private RadioButton allRadio;
    @FXML
    private RadioButton pastRadio;
    @FXML
    private RadioButton futureRadio;
    @FXML
    private ComboBox searchChoice;
    @FXML
    private ComboBox filterChoice;
    @FXML
    private ToggleButton billsToggle;
    @FXML
    private ToggleButton defaultToggle;
    @FXML
    private TableView<Customer> tableViewCustomers;
    @FXML
    private TableColumn<Customer, String> columnFirstname;
    @FXML
    private TableColumn<Customer, String> columnSurname;
    @FXML
    private TableColumn<Customer, String> columnAddress;
    @FXML
    private TableColumn<Customer, String> columnPostCode;
    @FXML
    private TableColumn<Customer, String> columnPhone;
    @FXML
    private TableColumn<Customer, String> columnEmail;
    @FXML
    private TableColumn<Customer, String> columnCustomerType;
    @FXML
    private Label bookingsText;
    @FXML
    private Label vehiclesText;
    @FXML
    private Label billsText;
    @FXML
    private AnchorPane billsPane;
    @FXML
    private AnchorPane defaultPane;
    @FXML
    private Tab vehiclesTab;
    @FXML
    private Tab bookingsTab;
    @FXML
    private Tab partsTab;
    @FXML
    private Tab spcTab;
    
    //Vehicle Table variables
    @FXML
    private TableView<Vehicle> tableViewVehicles;
    @FXML
    private TableColumn<Vehicle, String> columnRegNo;
    @FXML
    private TableColumn<Vehicle, String> columnVehicleType;
    @FXML
    private TableColumn<Vehicle, String> columnMake;
    @FXML
    private TableColumn<Vehicle, String> columnModel;
    
    @FXML
    private ListView<String> listViewParts;
    @FXML
    private TableView<BookingDetails> tableViewBookings;
    @FXML
    private TableColumn<BookingDetails, String> columnStartDate;
    @FXML
    private TableColumn<BookingDetails, String> columnEndDate;
    @FXML
    private TableColumn<BookingDetails, String> columnBookingRegNo;
    
    //Variables for Bills Table
    @FXML
    private TableView<BillRow> tableViewBills;
    @FXML
    private TableColumn<BillRow, String> columnBillReg;
    @FXML
    private TableColumn<BillRow, String> columnBillStartDate;
    @FXML
    private TableColumn<BillRow, String> columnBillEndDate;
    @FXML
    private TableColumn<BillRow, String> columnWarranty;
    @FXML
    private TableColumn<BillRow, String> columnBill;
    @FXML
    private TableColumn<BillRow, String> columnStatus;
    
    private static final Database DB = Database.getInstance(); 
    private static ArrayList<Customer> customerdata;
    private static ObservableList<Customer> customerobslist;
    private static String currentquery = "SELECT * FROM Customer;";
    private final ToggleGroup togglegroup = new ToggleGroup();
    private final ToggleGroup radiogroup = new ToggleGroup();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> options; 
        options = FXCollections.observableArrayList("First Name", "Surname", "Vehicle Reg No.");
        searchChoice.setItems(options);
        searchChoice.getSelectionModel().selectFirst();  
        
        ObservableList<String> filteroptions; 
        filteroptions = FXCollections.observableArrayList("All Customers", "Private", "Business");
        filterChoice.setItems(filteroptions);
        filterChoice.getSelectionModel().selectFirst(); 
        
        listViewParts.setPlaceholder(new Label("Select Vehicle"));
        bookingsText.setText("Select a Customer");
        vehiclesText.setText("Select a Customer");
        billsText.setText("Select a Customer");
        
        billsToggle.setToggleGroup(togglegroup);
        defaultToggle.setToggleGroup(togglegroup);
        defaultToggle.setSelected(true);
        defaultToggle.setDisable(true);
        
        allRadio.setToggleGroup(radiogroup);
        allRadio.setUserData("All");
        allRadio.setSelected(true);
        pastRadio.setToggleGroup(radiogroup);
        pastRadio.setUserData("Past");
        futureRadio.setToggleGroup(radiogroup);
        futureRadio.setUserData("Future");
        
        tableViewCustomers.setRowFactory( tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
                    int customerid = tableViewCustomers.getSelectionModel().getSelectedItem().getCustomerID();
                    displayVehicle(customerid);
                    displayBookingDetails(customerid);
                    displayBills(customerid);
                    listViewParts.setItems(null);
                }
            });
            return row ;
        });

        tableViewVehicles.setRowFactory( tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
                    int vehicleid = tableViewVehicles.getSelectionModel().getSelectedItem().getID();
                    displayPartDetails(vehicleid);
                }
            });
            return row ;
        });
        
        tableViewCustomers.setOnKeyPressed(Event::consume);
        tableViewBookings.setOnKeyPressed(Event::consume);
        tableViewVehicles.setOnKeyPressed(Event::consume);
        tableViewBills.setOnKeyPressed(Event::consume);
        listViewParts.setOnKeyPressed(Event::consume);
        //The lines above disable movement with keys on table views
        
        displayCustomerDetails("SELECT * FROM Customer;");
    }    
   
    //Gets the query to search customer accounts based on the combo box option
    private String getQuery(String selection){
        String selectedFilter = filterChoice.getSelectionModel().getSelectedItem().toString();
        switch (selection) {
            default:
                if (selectedFilter.equals("All Customers")){
                    return "SELECT * FROM Customer WHERE Firstname LIKE ? ;";
                } else {
                    return "SELECT * FROM Customer WHERE Firstname LIKE ? AND CustomerType = '"+selectedFilter+"';";
                }
            case "Surname":
                if (selectedFilter.equals("All Customers")){
                    return "SELECT * FROM Customer  WHERE Surname LIKE ? ;";
                } else {
                    return "SELECT * FROM Customer  WHERE Surname LIKE ? AND CustomerType = '"+selectedFilter+"';";
                }
            case "Vehicle Reg No.":
                if (selectedFilter.equals("All Customers")){
                    return "Select Customer.* "
                            + "FROM Customer "
                            + "INNER JOIN Vehicle "
                            + "ON Customer.CustomerID = Vehicle.CustomerID "
                            + "INNER JOIN VehicleInfo ON Vehicle.VehicleID = VehicleInfo.VehicleID "
                            + "WHERE VehicleInfo.Registration  "
                            + "LIKE ? ;";
                } else {
                    return "Select Customer.* "
                            + "FROM Customer "
                            + "INNER JOIN Vehicle "
                            + "ON Customer.CustomerID = Vehicle.CustomerID "
                            + "INNER JOIN VehicleInfo ON Vehicle.VehicleID = VehicleInfo.VehicleID "
                            + "WHERE VehicleInfo.Registration  "
                            + "LIKE ? AND Customer.CustomerType = '"+selectedFilter+"';";
                }
        }  
    }
    
    //displays the customer information in the customers tableview
    private void displayCustomerDetails(String query) {
        try {
            Connection conn = DB.getConnection();
            customerdata = new ArrayList();
            PreparedStatement ps = conn.prepareStatement(query);
            if (!query.equals("SELECT * FROM Customer;"))
                ps.setString(1, "%" + searchBox.getText() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                customerdata.add(new Customer(rs.getInt("CustomerID"),
                                              rs.getString("CustomerType"),
                                              rs.getString("Firstname"),
                                              rs.getString("Surname"),
                                              rs.getString("Address"),
                                              rs.getString("Postcode"),
                                              rs.getString("Phone"),
                                              rs.getString("Email")));
            }
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }

        columnCustomerType.setCellValueFactory(new PropertyValueFactory<>("customertype"));
        columnFirstname.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        columnPostCode.setCellValueFactory(new PropertyValueFactory<>("postcode"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        columnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableViewCustomers.setItems(null); 
        customerobslist = FXCollections.observableArrayList(customerdata);
        tableViewCustomers.setItems(customerobslist);
    }
    
    //helper method to execute a query based on string input
    private void executeQuery(String query) {
        try {
            Connection conn = DB.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }
    }
    
    //helper method to refresh the tableview
    private void refreshTable(String query) {
        displayCustomerDetails(query);
    }
    
    //method to delete Customer Account
    public void deleteCustomer(ActionEvent event){
        try {
            Customer customer;
            String customerid;
            String deletequery;
            String customername;
            Alert alert;

            customer = tableViewCustomers.getSelectionModel().getSelectedItem();
            customerid = Integer.toString(customer.getCustomerID());
            customername = customer.getFirstname()+" "+customer.getSurname();
            deletequery = "DELETE FROM Customer WHERE CustomerID='"+ customerid +"';";

            alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Delete Confirmation");
            alert.setHeaderText("Deleting " + customername);
            alert.setContentText("Deleting a Customer will also delete all associated Vehicles, "
                    + "Bookings, Bills and Parts used per Vehicle "
                    + "Are you ok with this?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
               executeQuery(deletequery);
               viewAllCustomers(new ActionEvent());
            } 
         
        //Refreshes tabs from the Main Frame
        FXMLLoader vloader = new FXMLLoader(getClass().getClassLoader().getResource("vehicles/gui/ViewVehicle.fxml"));
        FXMLLoader bloader = new FXMLLoader(getClass().getClassLoader().getResource("diagrep/gui/SearchBookingsFXML.fxml"));
        FXMLLoader ploader = new FXMLLoader(getClass().getClassLoader().getResource("parts/GUI/searchForParts.fxml"));
        FXMLLoader spcloader = new FXMLLoader(getClass().getClassLoader().getResource("specialist/MainPage.fxml"));
        
        vehiclesTab.setContent(vloader.load());
        bookingsTab.setContent(bloader.load());
        partsTab.setContent(ploader.load());
        spcTab.setContent(spcloader.load());
        //by setting the tab content again
        
        MainPageController controller= spcloader.<MainPageController>getController();// get the controller and pass the tab to setContent with new fxml file
        controller.setParentTab(spcTab);
        SearchForPartsController partController = (SearchForPartsController) ploader.getController();
        partController.setTabs(partsTab, bookingsTab);
        SearchController bookingController = (SearchController) bloader.getController();
        bookingController.setTabs(bookingsTab, partsTab);

            
        } catch (NullPointerException ex) {
            infoAlert("Unable to Delete Customer", "No Customer Selected");
        } catch (IOException ex) {
            Logger.getLogger(ViewCustomersController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Method to add a customer
    public void addCustomer(ActionEvent event){
        try {
                FXMLLoader loader = new FXMLLoader();
                Pane root = loader.load(getClass().getResource("AddEditCustomers.fxml").openStream()); 
                AddEditCustomersController controller = (AddEditCustomersController)loader.getController();
                controller.setTitle("Add Customer");
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(root.getScene().getWindow());
                stage.setResizable(false);
                stage.setScene(scene);
                stage.showAndWait();
                refreshTable("SELECT * FROM Customer;");
            } catch(IOException ex) {
                System.err.println("Error: "+ex);
            }
    }
    
    //Method to edit a customer 
    public void editCustomer(ActionEvent event){
        try {
                FXMLLoader loader = new FXMLLoader();
                Pane root = loader.load(getClass().getResource("AddEditCustomers.fxml").openStream()); 
                AddEditCustomersController controller = (AddEditCustomersController)loader.getController();
                controller.setTitle("Edit Customer");
                Customer selectedcustomer = tableViewCustomers.getSelectionModel().getSelectedItem();
                controller.sendCustomerData(selectedcustomer);
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setScene(scene);
                stage.showAndWait();
                refreshTable("SELECT * FROM Customer;");
            } catch(IOException ex) {
                System.err.println("Error: "+ex);
            } catch(NullPointerException ex){
                System.err.println(ex);
                infoAlert("Unable to Edit Customer", "No Customer Selected");
            }
    }
    
    //Method that is called when View All button is pressed
    public void viewAllCustomers(ActionEvent Event){
        tableViewVehicles.setItems(null);
        tableViewBookings.setItems(null);
        listViewParts.setItems(null);
        tableViewBills.setItems(null);
        listViewParts.setPlaceholder(new Label("Select Vehicle"));
        bookingsText.setText("Select a Customer");
        vehiclesText.setText("Select a Customer");
        billsText.setText("Select a Customer");
        displayCustomerDetails("SELECT * FROM Customer;");
    }
    
    //Displays vehicles in vehicles table view
    private void displayVehicle(int customerid) {
        ArrayList<Vehicle> vehicledata = new ArrayList();
        try {
            Connection conn = DB.getConnection();
            String vehiclequery = "Select Vehicle.VehicleID FROM Vehicle "
                                + "INNER JOIN VehicleInfo "
                                + "WHERE Vehicle.VehicleID = VehicleInfo.VehicleID "
                                + "AND Vehicle.CustomerID = '"+customerid+"';";
            ResultSet rs = conn.createStatement().executeQuery(vehiclequery);
            while (rs.next()) {
                vehicledata.add(new Vehicle(rs.getInt("VehicleID")));
            }
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }

        columnRegNo.setCellValueFactory(new PropertyValueFactory<>("registration"));
        columnVehicleType.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
        columnMake.setCellValueFactory(new PropertyValueFactory<>("make"));
        columnModel.setCellValueFactory(new PropertyValueFactory<>("model"));

        tableViewVehicles.setItems(null); 
        ObservableList<Vehicle> vehicleobslist = FXCollections.observableArrayList(vehicledata);
        tableViewVehicles.setItems(vehicleobslist);
        
        if (vehicleobslist.isEmpty()){
            vehiclesText.setText("No Vehicles found for Customer");
        }
    }
   
    //displays Booking details in table view
    private void displayBookingDetails(int customerid) {
        ArrayList<BookingDetails> bookingsdata = new ArrayList();
        String curSelection = radiogroup.getSelectedToggle().getUserData().toString();
        String bookingquery = "SELECT Registration, StartDate, EndDate "
                        + "FROM DiagRepairBooking "
                        + "INNER JOIN Vehicle ON DiagRepairBooking.VehicleID = Vehicle.VehicleID "
                        + "INNER JOIN VehicleInfo ON Vehicle.VehicleID = VehicleInfo.VehicleID "
                        + "AND Vehicle.CustomerID = '"+customerid+"';";
        try {
            Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(bookingquery);
            ResultSet rs = ps.executeQuery();

            if (curSelection.equals("Future")){
                while (rs.next()) {
                    String startdate = rs.getString("StartDate");
                    if (isFutureDate(startdate))
                        bookingsdata.add(new BookingDetails(rs.getString("Registration"), startdate, rs.getString("EndDate")));
                }
            } else if (curSelection.equals("Past")) {
                while (rs.next()) {
                    String startdate = rs.getString("StartDate");
                    if (!isFutureDate(startdate))
                        bookingsdata.add(new BookingDetails(rs.getString("Registration"), startdate, rs.getString("EndDate")));
                }
            } else {
                while (rs.next()) {
                    bookingsdata.add(new BookingDetails(rs.getString("Registration"), rs.getString("StartDate"), rs.getString("EndDate")));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }

        columnBookingRegNo.setCellValueFactory(new PropertyValueFactory<>("VehReg"));
        columnStartDate.setCellValueFactory(new PropertyValueFactory<>("StartDate")); 
        columnEndDate.setCellValueFactory(new PropertyValueFactory<>("EndDate"));

        tableViewBookings.setItems(null); 
        ObservableList<BookingDetails> bookingsobslist = FXCollections.observableArrayList(bookingsdata);
        tableViewBookings.setItems(bookingsobslist);
        if (bookingsobslist.isEmpty()){
            bookingsText.setText("No Bookings found for Customer");
        }
    }
    
    //Runs query when radio button is pressed
    public void radioButtonPressed(ActionEvent Event){
        int customerid = tableViewCustomers.getSelectionModel().getSelectedItem().getCustomerID();
        displayBookingDetails(customerid);
    }
    
    //displays parts in listview
    private void displayPartDetails(int vehicleid){
        ArrayList<String> partdata = new ArrayList();
        try {
            Connection conn = DB.getConnection();
            String partnamequery = "SELECT Part.Name FROM Part "
                                 + "INNER JOIN PartInVehicle "
                                 + "WHERE PartInVehicle.PartID = Part.ID "
                                 + "AND PartInVehicle.VehicleID='"+vehicleid+"';";
            ResultSet rs = conn.createStatement().executeQuery(partnamequery);
            while (rs.next()) {
                partdata.add(rs.getString("Name"));
            }
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }
        listViewParts.setItems(null); 
        ObservableList<String> partsobslist = FXCollections.observableArrayList(partdata);
        if (!partsobslist.isEmpty()){
            listViewParts.setItems(partsobslist);
        }
        else {
            listViewParts.setPlaceholder(new Label("No Parts Found"));
        }
    }
    
    //Method to initialise a booking when "Make Booking" button is clicked
    public void makeBooking(ActionEvent Event) throws IOException{
        if (tableViewVehicles.getSelectionModel().getSelectedItem() != null){
            int vehicleid = tableViewVehicles.getSelectionModel().getSelectedItem().getID();
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("diagrep/gui/BookingFXML.fxml")); 
            bookingsTab.setContent(loader.load());
            BookingController controller = (BookingController)loader.getController();
            controller.setVehicleToAdd(vehicleid);
            controller.setTabs(bookingsTab, partsTab);
            bookingsTab.getTabPane().getSelectionModel().select(bookingsTab);
        }
        else {
            infoAlert("Unable to Make Booking", "No Vehicle Selected");
        }
        
    }
    
    //Shows bill records in table view
    public void displayBills(int customerid){
        ArrayList<BillRow> billsData = new ArrayList();
        try {
            Connection conn = DB.getConnection();
            String billsquery = "SELECT VehicleInfo.Registration, DiagRepairBooking.StartDate, " +
                                "DiagRepairBooking.EndDate, Bills.InWarranty, Bills.Bill, " +
                                "DiagRepairBooking.Paid, DiagRepairBooking.DiagRepID FROM Bills  \n" +
                                "INNER JOIN DiagRepairBooking ON Bills.DiagRepID = DiagRepairBooking.DiagRepID \n" +
                                "INNER JOIN Vehicle ON DiagRepairBooking.VehicleID = Vehicle.VehicleID \n" +
                                "INNER JOIN VehicleInfo ON Vehicle.VehicleID = VehicleInfo .VehicleID\n" +
                                "WHERE Vehicle.CustomerID = '"+customerid+"';";
            ResultSet rs = conn.createStatement().executeQuery(billsquery);
            while (rs.next()) { 
                billsData.add(new BillRow(rs.getString(1), 
                                          rs.getString(2), 
                                          rs.getString(3), 
                                          rs.getInt(4), 
                                          rs.getDouble(5), 
                                          rs.getInt(6), 
                                          rs.getInt(7),
                                          customerid));
            }
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }

        columnBillReg.setCellValueFactory(new PropertyValueFactory<>("VehicleReg"));
        columnBillStartDate.setCellValueFactory(new PropertyValueFactory<>("StartDate"));
        columnBillEndDate.setCellValueFactory(new PropertyValueFactory<>("EndDate"));
        columnWarranty.setCellValueFactory(new PropertyValueFactory<>("Warranty"));
        columnBill.setCellValueFactory(new PropertyValueFactory<>("Bill"));
        columnStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));

        tableViewBills.setItems(null); 
        ObservableList<BillRow> billsobslist = FXCollections.observableArrayList(billsData);
        tableViewBills.setItems(billsobslist);
        if (billsobslist.isEmpty()){
            billsText.setText("No Bills found for Customer");
        }
    }
    
    //Changes bill status to Paid 
    public void setPaid(ActionEvent Event){
        Connection connection =  DB.getConnection();
        try {
            BillRow billrow = tableViewBills.getSelectionModel().getSelectedItem();
            String warranty = billrow.WarrantyProperty().getValue();
            String status = billrow.StatusProperty().getValue();
            int diagrepid = billrow.getDiagRepID();
            int customerid = billrow.getCustomerID();
            if(warranty.equals("Valid"))
            {
                errorAlert("Unable to Settle Bill", 
                "Customers do not settle accounts for vehicles with valid warranties.");
            }
            else if(status.equals("Paid")){
                errorAlert("Unable to Settle Bill", 
                "Bill has already been settled.");
            }
            else {
                try {
                    PreparedStatement ps;
                    ps = connection.prepareStatement("UPDATE DiagRepairBooking SET Paid = 1 WHERE DiagRepID = "+diagrepid+";");
                    ps.executeUpdate();
                    displayBills(customerid);
                    billrow.setStatus("Paid");
                    infoAlert("Success", "Bill was successfully settled as Paid.");
                } catch (SQLException ex) {
                    Logger.getLogger(DiagRepair.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        } catch (NullPointerException ex) {
                errorAlert("Error", "Please select a Bill");
        }
    }
    
    //Changes a bill to status outstanding
    public void setOutstanding(ActionEvent Event){
        Connection connection =  DB.getConnection();
        try {
            BillRow billrow = tableViewBills.getSelectionModel().getSelectedItem();
            String warranty = billrow.WarrantyProperty().getValue();
            String status = billrow.StatusProperty().getValue();
            int diagrepid = billrow.getDiagRepID();
            int customerid = billrow.getCustomerID();
            if(warranty.equals("Valid"))
            {
                errorAlert("Unable to Settle Bill", 
                "Customers do not settle accounts for vehicles with valid warranties.");
            }
            else if(status.equals("Outstanding")){
                errorAlert("Unable to change Bill", 
                "Bill is already Outstanding.");
            }
            else {
                try {
                    PreparedStatement ps;
                    ps = connection.prepareStatement("UPDATE DiagRepairBooking SET Paid = 0 WHERE DiagRepID = "+diagrepid+";");
                    ps.executeUpdate();
                    displayBills(customerid);
                    billrow.setStatus("Outstanding");
                    infoAlert("Success", "Bill was successfully changed to Outstanding.");
                } catch (SQLException ex) {
                    Logger.getLogger(DiagRepair.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        } catch (NullPointerException ex) {
                errorAlert("Error", "Please select a Bill");
        }
    }
     
    //Iniates access to vehicle record
    public void viewVehicleRecord(ActionEvent Event){
        try {
            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getClassLoader().getResource("diagrep/gui/ViewVehicleInfo.fxml").openStream());
            ViewVehicleController controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            Vehicle selectedVehicle = tableViewVehicles.getSelectionModel().getSelectedItem();
            controller.setVehicle(selectedVehicle.getID());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();
        } catch(IOException e) {
            System.err.println("Error: "+e);
        } catch(NullPointerException ex){
            errorAlert("Can't retrieve Vehicle Info", "Please select a Vehicle");
        } catch (SQLException ex) {
            Logger.getLogger(ViewCustomersController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Iniates access to part in vehicle record
    public void viewPartRecord(ActionEvent Event){
        try {
            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getClassLoader().getResource("parts/GUI/partUsedDetails.fxml").openStream());
            PartUsedDetailsController controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            String selectedpartname = listViewParts.getSelectionModel().getSelectedItem();
            int selectedvehid = tableViewVehicles.getSelectionModel().getSelectedItem().getID();
            InstalledPart ipart = getInstalledPart(selectedpartname, selectedvehid);
            controller.initializeTextViews(ipart);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(root.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();
        } catch(IOException e) {
            System.err.println("Error: "+e);
        } catch(NullPointerException ex){
            errorAlert("Can't retrieve Part Info", "Please select a part");
        }
    }
    
    private InstalledPart getInstalledPart(String partname, int vehicleid){  
    try {
            Connection conn = DB.getConnection();
            String partsquery = "SELECT PartInVehicle.PartID FROM PART "
                              + "INNER JOIN PartInVehicle ON Part.ID = PartInVehicle.PartID "
                              + "WHERE Name = ? AND PartInVehicle.VehicleID = ?;";
            PreparedStatement ps = conn.prepareStatement(partsquery);
            ps.setString(1, partname);
            ps.setInt(2, vehicleid);
            ResultSet rs = ps.executeQuery();
            return new InstalledPart(rs.getInt(1));
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        }
        return null;
    }
    
    //Displays customer accounts based on key releases in search bar
    public void keyReleaseListener(KeyEvent event) {
        currentquery = getQuery(searchChoice.getSelectionModel().getSelectedItem().toString());
        displayCustomerDetails(currentquery);
    }
    
    //method to switch between Bills and Default view
    public void toggleBills(ActionEvent event){

        if (togglegroup.getSelectedToggle() == billsToggle){
            billsToggle.setDisable(true);
            defaultToggle.setDisable(false);
            defaultPane.setVisible(false);
            billsPane.setVisible(true);
        }
        else {
            defaultToggle.setDisable(true);
            billsToggle.setDisable(false);
            billsPane.setVisible(false);
            defaultPane.setVisible(true);
        }
    }
    
    //method to set the tabs of the miainframe Customer module interacts with
    public void setTabs(Tab vtab, Tab btab, Tab ptab, Tab spctab){
        vehiclesTab = vtab;
        bookingsTab = btab;
        partsTab = ptab;
        spcTab = spctab;
    }
    
    //method that prints error message
    private void errorAlert(String title, String content){
        Alert erroralert = new Alert(Alert.AlertType.ERROR);
        erroralert.setTitle(title);
        erroralert.setHeaderText(null);
        erroralert.setContentText(content);
        erroralert.showAndWait();
    }
    
    //method to print info message
    private void infoAlert(String title, String content){
        Alert successalert = new Alert(Alert.AlertType.INFORMATION);
        successalert.setTitle(title);
        successalert.setHeaderText(null);
        successalert.setContentText(content);
        successalert.showAndWait();
    }
    
    public boolean isFutureDate(String bookingStartDate){
        try {
            String currentDate = DB.getCurrentDate();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            
            Date date1 = format.parse(currentDate);
            Date date2 = format.parse(bookingStartDate);
            
            if (date1.compareTo(date2) <= 0) {
                return true;
            }
        } catch (ParseException ex) {
            Logger.getLogger(ViewCustomersController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}