/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package diagrep.gui;

import diagrep.logic.DiagRepair;
import common.Database;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author adamlaraqui
 */
public class SearchController implements Initializable {
    private boolean showingDefaultVehicle = false;
    @FXML
    private Label searchByStatus;
    @FXML
    private ChoiceBox searchBy;
    @FXML
    private ToolBar inputRegSearchBar;
    @FXML
    private TextField inputRegSearchTxt;
    @FXML
    private ToolBar inputNameSearchBar;
    @FXML
    private TextField inputFirstNameSearchTxt;
    @FXML
    private TextField inputLastNameSearchTxt;
    @FXML
    private ToolBar templateSearchBar;
    @FXML
    private ChoiceBox<Template> templateChoiceBox;
    @FXML
    private ToolBar hourDayMonthBar;
    @FXML
    private DatePicker dailyPicker;
    @FXML
    private MenuButton hourlyPicker;
    @FXML
    private MenuButton monthlyPicker;
    @FXML
    private TableView<BookingInfo> bookingsList;
    @FXML
    private TableColumn<BookingInfo, Integer> bookingID;
    @FXML
    private TableColumn<BookingInfo, String> vehicleReg;
    @FXML
    private TableColumn<BookingInfo, String> vehicleMan;
    @FXML
    private TableColumn<BookingInfo, String> customerName;
    @FXML
    private TableColumn<BookingInfo, String> startDate;
    @FXML
    private TableColumn<BookingInfo, String> endDate;
    @FXML
    private TableColumn<BookingInfo, String> nextBooking;
    @FXML
    private TableColumn<BookingInfo, Boolean> completed;
    @FXML
    private TableColumn<BookingInfo, Boolean> type;
    @FXML
    private Button editBooking;
    @FXML
    private Button deleteBooking;
    @FXML
    private Button viewCustomerRecord;
    @FXML
    private Button viewVehicleRecord;
    @FXML
    private Button viewPastFutureBookings;
    
    private ObservableList<BookingInfo> bookingList;
    
    private Tab parentTab;
    private Tab partsTab;
    
    private final String searchQuery = "SELECT DiagRepID, StartDate, EndDate, Completed, Registration, Firstname, Surname, Make, Customer.CustomerID, Vehicle.VehicleID, NextDate\n" +
                                    "FROM (\n" +
                                    "	`DiagRepairBooking`\n" + // Get all bookings
                                    "	INNER JOIN `Vehicle` ON DiagRepairBooking.VehicleID = Vehicle.VehicleID\n" + // Link booking to vehicle
                                    "	INNER JOIN `VehicleInfo` ON DiagRepairBooking.VehicleID = VehicleInfo.VehicleID\n" + // Link vehicle information to booking
                                    "	INNER JOIN `Customer` ON Vehicle.CustomerID = Customer.CustomerID\n" + // Link vehicle to customer
                                    ") a LEFT JOIN (\n" + // Tries to get next booking date for vehicle (where start date is on or after current date). If not, then return null.
                                    "	SELECT VehicleID, MIN(StartDate) AS NextDate FROM DiagRepairBooking WHERE datetime(StartDate) >= (SELECT CurrentDate FROM CurrentDate) AND Completed = 0 GROUP BY VehicleID\n" + // Get next booking date for matched vehicles (that are not completed)
                                    ") b ON a.VehicleID = b.VehicleID"; // Match next booking date to each vehicle with a booking
    
    public void setTabs(Tab bookingsTab, Tab partsTab) {
        parentTab = bookingsTab;
        this.partsTab = partsTab;
    }
    
    public class BookingInfo {
        private final IntegerProperty BookingID;
        private final StringProperty VehicleReg;
        private final StringProperty VehicleManufacturer;
        private final StringProperty customerName;
        private final StringProperty startDate;
        private final StringProperty endDate;
        private final StringProperty nextBookingDate;
        private final BooleanProperty complete;
        private final StringProperty type;
        private final int CustomerID;
        private final int VehicleID;
        public BookingInfo(int bookingID, String vehReg, String vehMan, String custName, String startDate, String endDate, String nextBooking, Boolean isComplete, String type, int custID, int vehID) {
            this.BookingID = new SimpleIntegerProperty(bookingID);
            this.VehicleReg = new SimpleStringProperty(vehReg);
            this.VehicleManufacturer = new SimpleStringProperty(vehMan);
            this.customerName = new SimpleStringProperty(custName);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
            this.nextBookingDate = new SimpleStringProperty(nextBooking);
            this.complete = new SimpleBooleanProperty(isComplete);
            this.type = new SimpleStringProperty(type);
            this.CustomerID = custID;
            this.VehicleID = vehID;
        }
        public IntegerProperty BookingIDProperty() { return BookingID; }
        public StringProperty VehRegProperty() { return VehicleReg; }
        public StringProperty VehManProperty() { return VehicleManufacturer; }
        public StringProperty CustNameProperty() { return customerName; }
        public StringProperty StartDateProperty() { return startDate; }
        public StringProperty EndDateProperty() { return endDate; }
        public StringProperty NextBookingProperty() { return nextBookingDate; }
        public BooleanProperty CompleteProperty() { return complete; }
        public StringProperty TypeProperty() { return type; }
        public int getCustomerID() { return CustomerID; }
        public int getVehicleID() { return VehicleID; }
    }
    
    class Template {
        int id; String make; String model; String enginesize; String fueltype; String vehicletype;
        Template(int id, String make, String model, String enginesize, String fueltype, String vehicletype) { this.id = id; this.make = make; this.model = model; this.enginesize = enginesize; this.fueltype = fueltype; this.vehicletype = vehicletype; }
        @Override public String toString() { return "Make (Model): "+make+" ("+model+")"; } // What to show on the list
    }
    
    /**
     * Initialises the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<String> searchTypes = new ArrayList<>();
        // Add all search types
        searchTypes.add("Vehicle Reg");
        searchTypes.add("Customer Name");
        searchTypes.add("Vehicle Template");
        searchTypes.add("Hour/Day/Month");
        ObservableList<String> searchByDropDown = FXCollections.observableArrayList(searchTypes);
        searchBy.setItems(searchByDropDown);
        searchBy.setValue("Vehicle Reg");
        // Code taken from Java2s.com - Add change listener to ComboBox valueProperty
        searchBy.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                inputRegSearchBar.setVisible(false);
                templateSearchBar.setVisible(false);
                inputNameSearchBar.setVisible(false);
                hourDayMonthBar.setVisible(false);
                if(newVal.equals("Customer Name")) {
                    inputNameSearchBar.setVisible(true);
                } else if(newVal.equals("Vehicle Reg")) {
                    inputRegSearchBar.setVisible(true);
                } else if(newVal.equals("Vehicle Template")) {
                    templateSearchBar.setVisible(true);
                } else if(newVal.equals("Hour/Day/Month")) {
                    hourDayMonthBar.setVisible(true);
                }
            }
        });
        
        // Code adapted from "Steven" on StackOverflow: JFXtras - How to add Change Listener to CalendarTextField?
        // and following the ChangeListener structure above...
        dailyPicker.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue ov, LocalDate oldValue, LocalDate newValue) {
                if(newValue!=null) {
                    searchByStatus.setText("Searching bookings that start on "+newValue);
                    searchBookings(" WHERE date(StartDate) = date('"+newValue+"')");
                }
            }
        });
        
        hourlyPicker.getItems().setAll(
                FXCollections.observableArrayList(createHourlyItem("09:00"), createHourlyItem("10:00"), createHourlyItem("11:00"),
                        createHourlyItem("12:00"), createHourlyItem("13:00"), createHourlyItem("14:00"), createHourlyItem("15:00"),
                        createHourlyItem("16:00"), createHourlyItem("17:00"))
        );
        
        monthlyPicker.getItems().setAll(
                FXCollections.observableArrayList(createMonthlyItem("January"), createMonthlyItem("February"),
                        createMonthlyItem("March"), createMonthlyItem("April"), createMonthlyItem("May"),
                        createMonthlyItem("June"), createMonthlyItem("July"), createMonthlyItem("August"),
                        createMonthlyItem("September"), createMonthlyItem("October"), createMonthlyItem("November"), createMonthlyItem("December"))
        );
        
        // Code inspired by "jewelsea" on GitHubGist - Example JavaFX ChoiceBox control backed by Database IDs
        ObservableList<Template> choices = FXCollections.observableArrayList();
        //choices.add(new Template(null, "No selection"));
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String getTemplateQuery = "SELECT * FROM `Template`";
            PreparedStatement st = con.prepareStatement(getTemplateQuery);
            ResultSet rs = st.executeQuery();
            while(rs.next()) {
                choices.add(new Template(rs.getInt("TemplateID"), rs.getString("Make"), rs.getString("Model"), rs.getString("EngineSize"), rs.getString("FuelType"), rs.getString("VehicleKind") ) );
            }
        } catch (SQLException ex) {
            showError("Diagnostics And Repair Error", "Vehicle Template Search will not be functional until the new table is added to the DB...");
            //Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
        templateChoiceBox.setItems(choices);
        
    }
    
    /**
     * For Requirement 9 and 10 in Parts and DiagRepair.
     * For a given vehicle ID, will populate a list of all past and future bookings.
     * Customer Name and Type of Booking, will also be accessible.
     * @param vehicleID ID of the vehicle to populate past/future bookings for
     */
    public void viewPastFutureBookings(int vehicleID) {
        this.showingDefaultVehicle = true;
        searchBookings(" WHERE Vehicle.VehicleID = "+vehicleID);
    }
    
    @FXML
    private void viewPastFutureBookings() {
        BookingInfo booking = bookingsList.getSelectionModel().getSelectedItem(); // Retrieves selected booking
        if(booking == null) {
            showError("Select a vehicle first", "You must select a specific booking/vehicle row to see the past and future booking dates for the associated vehicle.");
        } else {
            viewPastFutureBookings(booking.VehicleID);
        }
    }
    
    public void searchByTemplate() {
        Template temp = templateChoiceBox.getSelectionModel().selectedItemProperty().getValue();
        if(temp == null) {
            showError("No Template Selected", "Please select a vehicle template in order to run this search...");
        } else {
            try {
                Database db = Database.getInstance();
                Connection con = db.getConnection();
                String query = this.searchQuery + " WHERE VehicleInfo.Make LIKE ? AND VehicleInfo.Model LIKE ? AND EngineSize LIKE ? AND FuelType LIKE ? AND VehicleKind LIKE ?";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setString(1, "%"+temp.make.trim()+"%");
                statement.setString(2, "%"+temp.model.trim()+"%");
                statement.setString(3, "%"+temp.enginesize.trim()+"%");
                statement.setString(4, "%"+temp.fueltype.trim()+"%");
                statement.setString(5, "%"+temp.vehicletype.trim()+"%");
                searchBookings(statement);
                searchByStatus.setText("Searching bookings with Vehicle Template: " + temp.make.trim() + " (" + temp.model.trim() + ")");
            } catch (SQLException ex) {
                showError("Unable to search bookings", "There was an issue when trying to search bookings using this vehicle template. Please try another vehicle template.");
                Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private MenuItem createHourlyItem(String hour) {
        MenuItem hourlyItem = new MenuItem(hour);
        hourlyItem.setOnAction( clickHour( Integer.parseInt( hour.substring(0, 2) ) ) );
        return hourlyItem;
    }
    
    private MenuItem createMonthlyItem(String month) {
        MenuItem monthlyItem = new MenuItem(month);
        String month_num = "01";
        switch(month) {
            case "January": month_num = "01"; break;
            case "February": month_num = "02"; break;
            case "March": month_num = "03"; break;
            case "April": month_num = "04"; break;
            case "May": month_num = "05"; break;
            case "June": month_num = "06"; break;
            case "July": month_num = "07"; break;
            case "August": month_num = "08"; break;
            case "September": month_num = "09"; break;
            case "October": month_num = "10"; break;
            case "November": month_num = "11"; break;
            case "December": month_num = "12"; break;
            default: break;
        }
        monthlyItem.setOnAction( clickMonth( month_num ) );
        return monthlyItem;
    }
    
    // Code adapted from Java2s.com - Menu item event handler
    private EventHandler<ActionEvent> clickHour(int hour) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Line below from Vlad on Stackoverflow - How to format a number to a 2 char string?
                String startdatetime;
                String enddatetime;
                String starttime = String.format("%02d", hour) + ":00";
                String endtime = String.format("%02d", hour + 1) + ":00";
                if(dailyPicker.getValue()==null) { // Use Database's "Current Date" if no date picked
                    Database db = Database.getInstance();
                    startdatetime = db.getCurrentDate() + " " + starttime;
                    enddatetime = db.getCurrentDate() + " " + endtime;
                } else { // Use the date from DatePicker if selected
                    startdatetime = dailyPicker.getValue().toString() + " " + starttime;
                    enddatetime = dailyPicker.getValue().toString() + " " + endtime;
                }
                searchByStatus.setText("Searching bookings that start between " + startdatetime + " and " + enddatetime);
                // Code from "RichardTheKiwi" on StackOverflow - How to select rows by date in sqlite
                searchBookings(" WHERE StartDate >= '" + startdatetime + "' AND StartDate < '" + enddatetime + "'");
            }
        };
    }
    
    // Code adapted from Java2s.com - Menu item event handler
    private EventHandler<ActionEvent> clickMonth(String month) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                searchByStatus.setText("Searching bookings that start in the month of "+month);
                // Code by "ikegami" on StackOverflow - SQLite SELECT date for a specific month
                searchBookings(" WHERE strftime('%m', StartDate) = '"+month+"'");
            }
        };
    }
    
    public void checkSelection() {
        if(bookingsList.getSelectionModel().getSelectedItem()==null) {
            editBooking.setDisable(true);
            viewCustomerRecord.setDisable(true);
            viewVehicleRecord.setDisable(true);
            deleteBooking.setDisable(true);
            viewPastFutureBookings.setDisable(true);
        } else {
            editBooking.setDisable(false);
            viewCustomerRecord.setDisable(false);
            viewVehicleRecord.setDisable(false);
            deleteBooking.setDisable(false);
            viewPastFutureBookings.setDisable(false);
        }
    }
    
    public void viewAllBookings() {
        searchByStatus.setText("Searching ALL bookings");
        searchBookings("");
    }
    
    public void viewFutureBookings() {
        Database db = Database.getInstance();
        String current =  db.getCurrentDateTime();
        searchByStatus.setText("Searching all FUTURE bookings");
        showAlert("Future Bookings", "Showing Future Bookings", "The 'Current Date' and 'Local Time' is\n\n" + current + "\n\nBookings will be shown which start after this date and time.");
        searchBookings(" WHERE datetime(StartDate) > datetime('"+current+"')");
    }
    
    public void searchByVehicleReg() {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String query = this.searchQuery + " WHERE VehicleInfo.Registration LIKE ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, "%"+inputRegSearchTxt.getText().trim()+"%");
            searchBookings(statement);
            searchByStatus.setText("Searching bookings with Vehicle Registration like '"+inputRegSearchTxt.getText().trim()+"'");
        } catch (SQLException ex) {
            showError("Unable to search bookings", "There was an issue when trying to search bookings using this vehicle registration. Please try another vehicle registration.");
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //searchBookings(" WHERE VehicleInfo.Registration LIKE '%"+inputRegSearchTxt.getText()+"%'");
    }
    
    public void searchByCustomerName() {
        try {
            ArrayList<BookingInfo> bookings = new ArrayList<>();
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String query = this.searchQuery + " WHERE Customer.Firstname LIKE ? AND Customer.Surname LIKE ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, "%"+inputFirstNameSearchTxt.getText().trim()+"%"); // TO DO
            statement.setString(2, "%"+inputLastNameSearchTxt.getText().trim()+"%"); // TO DO
            searchBookings(statement);
            searchByStatus.setText("Searching bookings with Customer Name like '"+(inputFirstNameSearchTxt.getText() + " " + inputLastNameSearchTxt.getText()).trim()+"'");
        } catch (SQLException ex) {
            showError("Unable to search bookings", "There was an issue when trying to search bookings using this vehicle registration. Please try another vehicle registration.");
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //searchBookings(" WHERE Customer.Firstname LIKE '%"+inputFirstNameSearchTxt.getText()+"%' AND Customer.Surname LIKE '%"+inputLastNameSearchTxt.getText()+"%'");
    }
    
    public void searchBookings(PreparedStatement readyStatement) {
        try {
            ArrayList<BookingInfo> bookings = new ArrayList<>();
            ResultSet rs = readyStatement.executeQuery();
            String registration = "";
            while(rs.next()) {
                registration = rs.getString("Registration");
                BookingInfo booking = new BookingInfo(rs.getInt("DiagRepID"), registration, rs.getString("Make"), rs.getString("Firstname")+" "+rs.getString("Surname"), rs.getString("StartDate"), rs.getString("EndDate"), rs.getString("NextDate"), (rs.getInt("Completed")==1), "DiagRepair", rs.getInt("Customer.CustomerID"), rs.getInt("Vehicle.VehicleID"));
                bookings.add(booking);
            }
            if(this.showingDefaultVehicle) {
                searchByStatus.setText("Showing all past and future booking dates for Vehicle Registration '"+registration+"'");
                this.showingDefaultVehicle = false;
            }
            bookingID.setCellValueFactory(new PropertyValueFactory("BookingID"));
            startDate.setCellValueFactory(new PropertyValueFactory("StartDate"));
            endDate.setCellValueFactory(new PropertyValueFactory("EndDate"));
            vehicleReg.setCellValueFactory(new PropertyValueFactory("VehReg"));
            vehicleMan.setCellValueFactory(new PropertyValueFactory("VehMan"));
            customerName.setCellValueFactory(new PropertyValueFactory("CustName"));
            nextBooking.setCellValueFactory(new PropertyValueFactory("NextBooking"));
            completed.setCellValueFactory(new PropertyValueFactory<>("Complete"));
            completed.setCellFactory(column -> new CheckBoxTableCell());
            type.setCellValueFactory(new PropertyValueFactory<>("Type"));
            bookingList = FXCollections.observableArrayList(bookings);
            editBooking.setDisable(true);
            viewCustomerRecord.setDisable(true);
            viewVehicleRecord.setDisable(true);
            deleteBooking.setDisable(true);
            viewPastFutureBookings.setDisable(true);
            bookingsList.setItems(bookingList);
        } catch (SQLException ex) {
            showError("Unable to search bookings", "There was an issue when trying to search bookings with the filter you selected. Please try again, or try another way to search.");
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void searchBookings(String refinedQuery) {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String query = this.searchQuery + refinedQuery;
            PreparedStatement statement = con.prepareStatement(query);
            searchBookings(statement);
        } catch (SQLException ex) {
            showError("Unable to search bookings", "There was an issue when trying to search bookings with the filter you selected. Please try again, or try another way to search.");
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void editBooking(ActionEvent event) throws IOException, SQLException {
        BookingInfo booking = bookingsList.getSelectionModel().getSelectedItem(); // Retrieves selected booking
        if(booking==null) {
            showError("Please select a booking to edit first", "Please select a booking from the search view so you can edit it.");
            return;
        }
        DiagRepair bookingObject = new DiagRepair(booking.BookingID.get());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookingFXML.fxml"));
        Parent root = loader.load();
        BookingController controller = (BookingController)loader.getController();
        controller.setBookingToEdit(bookingObject);
        controller.setTabs(parentTab, partsTab);
        parentTab.setContent(root);
    }
    
    public void deleteBooking() {
        try {
            BookingInfo booking = bookingsList.getSelectionModel().getSelectedItem(); // Retrieves selected booking
            if(booking == null) {
                showError("Select a booking to delete", "You must select a booking first in order to delete it.");
            } else if(getConfirmation("Delete A Booking", "Delete Booking ID "+booking.BookingID.get(), "You are about to delete this booking. This will delete all associated SPC bookings, whether Vehicle or Part(s). Any parts that were installed will not be removed from the vehicle; this can optionally be done from the parts module afterwards.\n\nAre you sure you want to delete this booking?")) {
                DiagRepair bookingObject = new DiagRepair(booking.BookingID.get());
                Database db = Database.getInstance();
                Connection con = db.getConnection();
                String deleteDiagRepBooking = "DELETE FROM DiagRepairBooking WHERE DiagRepID = " + bookingObject.getBookingID();
                PreparedStatement deleteBookingStatement = con.prepareStatement(deleteDiagRepBooking);
                deleteBookingStatement.executeUpdate();
                showAlert("Deletion Successful", "Booking Deleted!", "This booking has now been removed from the system. Any associated SPC bookings are also removed.");
                bookingList.remove(booking);
                bookingsList.refresh();
                checkSelection();
            }
        } catch (SQLException ex) {
            showError("Unable to delete booking", ex.getMessage());
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addBooking(ActionEvent event) {
        try {
            // Method from "Krzysztof Szewczyk" on StackOverflow - How to create a modal window in JavaFX 2.1
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource("BookingFXML.fxml").openStream());
            BookingController controller = (BookingController)loader.getController();
            if(bookingsList.getSelectionModel().getSelectedItem()!=null)
                controller.setVehicleToAdd(bookingsList.getSelectionModel().getSelectedItem().VehicleID);
            controller.setTabs(parentTab, partsTab);
            parentTab.setContent(root);
        } catch (IOException ex) {
            showError("Unable to open booking view", ex.getMessage());
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void viewCustomerRecord(ActionEvent event) {
        try {
            // Method from "Krzysztof Szewczyk" on StackOverflow - How to create a modal window in JavaFX 2.1
            FXMLLoader loader = new FXMLLoader();
            Pane root = null;
            root = loader.load(getClass().getResource("ViewCustomerInfo.fxml").openStream());
            ViewCustomerController controller = (ViewCustomerController)loader.getController();
            controller.setCustomer(bookingsList.getSelectionModel().getSelectedItem().CustomerID);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner((Stage) ((Node) event.getSource()).getScene().getWindow());
            stage.setResizable(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Customer Record");
            stage.showAndWait();
        } catch (IOException | SQLException ex) {
            showError("Unable to get Customer Record", ex.getMessage());
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void viewVehicleRecord(ActionEvent event) {
        try {
            // Method from "Krzysztof Szewczyk" on StackOverflow - How to create a modal window in JavaFX 2.1
            FXMLLoader loader = new FXMLLoader();
            Pane root = null;
            root = loader.load(getClass().getResource("ViewVehicleInfo.fxml").openStream());
            ViewVehicleController controller = (ViewVehicleController)loader.getController();
            controller.setVehicle(bookingsList.getSelectionModel().getSelectedItem().VehicleID);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner((Stage) ((Node) event.getSource()).getScene().getWindow());
            stage.setResizable(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Vehicle Record");
            stage.showAndWait();
        } catch (IOException | SQLException ex) {
            showError("Unable to get Vehicle Record", ex.getMessage());
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean getConfirmation(String title, String header, String desc) {
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType[] buttons = {yes, no};
        Alert dialog = new Alert(Alert.AlertType.WARNING, desc, buttons);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        Optional<ButtonType> result = dialog.showAndWait();
        return result.get() == yes;
    }
    
    // Code from javacodegeeks.com - JavaFX Dialog Example
    private void showError(String title, String desc) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        String s = desc;
        alert.setContentText(s);
        alert.showAndWait();
    }
    
    // Code from javacodegeeks.com - JavaFX Dialog Example
    private void showAlert(String title, String header, String desc) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        String s = desc;
        alert.setContentText(s);
        alert.showAndWait();
    }
}