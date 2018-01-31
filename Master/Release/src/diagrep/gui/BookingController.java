/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package diagrep.gui;

import diagrep.logic.DiagRepair;
import diagrep.logic.Utility;
import diagrep.logic.InProgressException;
import common.Database;
import diagrep.logic.BillForDiagRepair;
import diagrep.logic.PartForRepairBooking;
import java.io.IOException;
import javafx.event.ActionEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import parts.GUI.PartsForBookingViewController;
import parts.logic.InstalledPart;

/**
 * FXML Controller class
 *
 * @author adamlaraqui
 */
public class BookingController implements Initializable {
    private DiagRepair booking; // If we're editing a booking, this will store the instance of it!
    private boolean editMode = false;
    private boolean bookingInProgress = false; // If Start Date has passed, but booking not complete
    private boolean bookingComplete = false; // If booking has been marked as completed
    private BillForDiagRepair bookingBill = null;
    private Mechanic selectedMechanic;
    private int vehicleID = -1; // Needed when creating a booking.
    // This can either be passed from the Customer Accounts module, Parts module, or set it ourselves
    private LocalDate currentDate;
    private ArrayList<LocalDate> holidays;
    
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private Button addBooking;
    @FXML
    private Button updateBooking;
    @FXML
    private TextField cost;
    @FXML
    private CheckBox complete;
    @FXML
    private CheckBox paid;
    @FXML
    private TextField vehicleReg;
    @FXML
    private Button vehicleRegCheck;
    @FXML
    private Button vehicleRegReset;
    @FXML
    private TextField currMileage;
    @FXML
    private TextField diagnosedFault;
    @FXML
    private Button sendVehicleToSPC;
    @FXML
    private Button addEditParts;
    @FXML
    private TextArea vehicleInfo;
    @FXML
    private TextField hourSelection;
    @FXML
    private TextField minuteSelection;
    @FXML
    private TextField endHourSelection;
    @FXML
    private TextField endMinuteSelection;
    @FXML
    private Accordion accordion;
    @FXML
    private TitledPane vehicleSelectPane;
    @FXML
    private TitledPane mechanicSelectPane;
    @FXML
    private TitledPane diagnosisSelectPane;
    @FXML
    private Label vehicleStatus;
    @FXML
    private Label mechanicStatus;
    @FXML
    private Label diagnosisStatus;
    @FXML
    private Pane SentOffToParts;
    @FXML
    private TableView<Mechanic> mechanicSelectList;
    @FXML
    private TableColumn<Mechanic, Integer> mechanicID;
    @FXML
    private TableColumn<Mechanic, String> mechanicName;
    @FXML
    private TableColumn<Mechanic, String> mechanicLastName;
    @FXML
    private TableColumn<Mechanic, Integer> mechanicWage;
    @FXML
    private TableView<PartInfo> diagnosisPartsList;
    @FXML
    private TableColumn<PartInfo, String> partName;
    @FXML
    private TableColumn<PartInfo, Boolean> partInstalled;
    @FXML
    private TableColumn<PartInfo, Boolean> repairPartCheck;
    @FXML
    private TableColumn<PartInfo, Boolean> addNewPartCheck;
    @FXML
    private TableColumn<PartInfo, Boolean> forSPCCheck;
    @FXML
    private Pane costingPane;
    @FXML
    private Pane completedPane;
    @FXML
    private Label completedCost;
    @FXML
    private Label completedRepairTime;
    @FXML
    private Label bookingCost;
    @FXML
    private Label mechanicHourlyRate;
    @FXML
    private Label mechanicRepairTime;
    @FXML
    private Label costMechanic;
    @FXML
    private Slider repairTimeSlider;
    private Tab parentTab;
    private Tab partsTab;
    
    public void setTabs(Tab bookingsTab, Tab partsTab) {
        parentTab = bookingsTab;
        this.partsTab = partsTab;
    }
    
    public void setBookingToEdit(DiagRepair booking) {
        this.booking = booking;
        this.editMode = true;
        try {
            this.bookingComplete = booking.isComplete();
            if(!this.bookingComplete) this.bookingInProgress = ( !Utility.isDateInFuture(booking.getStartDate()) );
            if(this.bookingInProgress) {
                // There can be many bookings which should be "active" at the same time per vehicle, in case they overrun (despite there being validation for this in terms of schedule)
                // We should only ensure that the earliest (active) booking can involve diagnosis + repairs
                int earliestActiveBookingID = Utility.getEarliestActiveBookingID(booking.getVehicleID());
                if(earliestActiveBookingID != -1 && earliestActiveBookingID != booking.getBookingID()) {
                    // If the booking we wanted to edit is not the earliest active booking for the vehicle
                    int confirmCode = getConfirmation("Booking Conflict!", "Booking Conflict for Vehicle.\nGo to currently active booking?", "Diagnosis and Repairs are unable to take place for this booking since this vehicle already has an active booking which started prior to this one.\n\n"
                            + "Even though this booking is due to start, you will only be able to make changes to Start/End Dates and the assigned Mechanic, until all earlier active bookings for this vehicle are completed.\n\n"
                            + "Would you like to edit the earliest active booking for the vehicle instead?\n\n"
                            + "Yes - Go to earliest active booking (ID "+earliestActiveBookingID+")\n"
                            + "No - Ammend booking anyway (ID "+this.booking.getBookingID()+")");
                    if(confirmCode == 1) {
                        this.booking = new DiagRepair(earliestActiveBookingID); // If accepted, will open booking view for the earliest active vehicle booking instead
                        this.bookingBill = new BillForDiagRepair(booking.getMechanicRate()); // Get bill instance, since the booking is active
                    } else if(confirmCode == 0) {
                        this.bookingInProgress = false; // If they clicked No, they can only change the Start/End Dates and Mechanic (which is what this boolean will enforce)
                        // Setting this makes the controller believe it to be an incomplete booking which has not yet started
                    } else {
                        // They clicked the (X) button on the dialog
                        throw new IllegalArgumentException("User exited out of the dialog");
                    }
                } else {
                    // If booking is the earliest active booking for the vehicle, make a Bill instance
                    this.bookingBill = new BillForDiagRepair(booking.getMechanicRate());
                }
            }
        } catch (SQLException ex) {
            this.bookingInProgress = true; // If error occurred, assume in progress, for testing functionality
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Edit Mode - Editing Booking ID: "+booking.getBookingID());
        conditionalRender();
    }
    
    @FXML
    public void setVehicleToAdd(int vehicleID) {
        this.vehicleID = vehicleID;
        this.checkVehicle(vehicleID); // TEMP Solution For Customer Accounts
        // TO DO
    }
    
    // Code adapted from "Jens-Peter Haack" on Stackoverflow: How to populate a tableview that contain CheckBox in JavaFX
    public class PartInfo {
        private IntegerProperty partID;
        private final StringProperty name;
        private final BooleanProperty installed; // Won't change for duration of booking (only after complete)
        private BooleanProperty inBooking; // Determines whether this part was already in the booking
        private BooleanProperty repairOnly;
        private BooleanProperty newPart;
        private BooleanProperty isForSPC;
        // For parts in the booking, or installed parts not in the booking
        public PartInfo(int partID, String name, Boolean installed, Boolean inBooking, Boolean repairOnly, Boolean newPart, Boolean isForSPC) {
            this.partID = new SimpleIntegerProperty(partID);
            this.name = new SimpleStringProperty(name);
            this.installed = new SimpleBooleanProperty(installed);
            this.inBooking = new SimpleBooleanProperty(inBooking);
            this.repairOnly = new SimpleBooleanProperty(repairOnly);
            this.newPart = new SimpleBooleanProperty(newPart);
            this.isForSPC = new SimpleBooleanProperty(isForSPC);
        }
        public StringProperty NameProperty() { return name; }
        public BooleanProperty InstalledProperty() { return installed; }
        public BooleanProperty RepairOnlyProperty() { return repairOnly; }
        public BooleanProperty NewPartProperty() { return newPart; }
        public BooleanProperty IsForSPCProperty() { return isForSPC; }
    }
    
    public class Mechanic {
        private final IntegerProperty ID;
        private final StringProperty FIRSTNAME;
        private final StringProperty SURNAME;
        private final DoubleProperty WAGE;
        public Mechanic(int ID, String firstName, String surName, double wage) {
            this.ID = new SimpleIntegerProperty(ID);
            this.FIRSTNAME = new SimpleStringProperty(firstName);
            this.SURNAME = new SimpleStringProperty(surName);
            this.WAGE = new SimpleDoubleProperty(wage);
        }
        public IntegerProperty IDProperty() { return this.ID; }
        public StringProperty FirstNameProperty() { return this.FIRSTNAME; }
        public StringProperty SurNameProperty() { return this.SURNAME; }
        public DoubleProperty WageProperty() { return this.WAGE; }
    }
    
    /**
     * Initialises the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /** DATE PICKER VALIDATION.
         * This is more so to make it easier to select dates, however it can never be foolproof due to the nature oh how users input time.
         * All Sunday dates are disabled by default.
         * All Holiday dates (2017 - 2018) are disabled by default.
         * All Start Dates that occur before the Current Date are disabled.
         * All End Dates that occur before the selected Start Date are disabled.
         */
        Database db = Database.getInstance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.currentDate = LocalDate.parse(db.getCurrentDate(), formatter);
        this.holidays = this.getHolidays();
        
        // Code from Oracle - JavaFX: Working with JavaFX UI Components
        // Example 26-6 Implementing a Day Cell Factory to Disable Some Days
        final Callback<DatePicker, DateCell> startDayCellFactory =
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item.isBefore(currentDate)) {
                                setDisable(true);
                                setStyle("-fx-background-color: #C0C0C0;");
                            } else if(item.getDayOfWeek() == DayOfWeek.SUNDAY) {
                                setDisable(true);
                                setStyle("-fx-background-color: #C0C0C0;");
                            } else if(holidays.contains(item)) {
                                setDisable(true);
                                setStyle("-fx-background-color: #C0C0C0;");
                            }
                        }
                    };
                }
            };
        startDate.setDayCellFactory(startDayCellFactory);
        
        // Code from Oracle - JavaFX: Working with JavaFX UI Components
        // Example 26-6 Implementing a Day Cell Factory to Disable Some Days
        final Callback<DatePicker, DateCell> endDayCellFactory =
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item.isBefore(startDate.getValue())) {
                                setDisable(true);
                                setStyle("-fx-background-color: #C0C0C0;");
                            } else if(item.getDayOfWeek() == DayOfWeek.SUNDAY) {
                                setDisable(true);
                                setStyle("-fx-background-color: #C0C0C0;");
                            } else if(holidays.contains(item)) {
                                setDisable(true);
                                setStyle("-fx-background-color: #C0C0C0;");
                            }
                        }
                    };
                }
            };
        endDate.setDayCellFactory(endDayCellFactory);
        
        vehicleReg.textProperty().addListener((ov, oldValue, newValue) -> {
            vehicleReg.setText(newValue.toUpperCase());
        });
        
        // Code by "Evan Knowles" on StackOverflow - What is the recommended way to make a numeric TextField in JavaFX?
        currMileage.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    currMileage.setText(newValue.replaceAll("[^\\d]", "")); // Only allows integer values
                }
            }
        });
        
        // Code by "Evan Knowles" on StackOverflow - What is the recommended way to make a numeric TextField in JavaFX?
        cost.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Code by "Jimmy" on StackOverflow - Simple regular expression for decimal with precision of 2
                if (!newValue.matches("(\\d*)|(\\d+\\.\\d{0,2})")) {
                    cost.setText(oldValue);
                }
            }
        });
        hourSelection.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    hourSelection.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        minuteSelection.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    minuteSelection.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        endHourSelection.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    endHourSelection.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        endMinuteSelection.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    endMinuteSelection.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        
        conditionalRender();
    }
    
    /** CONDITIONAL RENDERING.
     * If creating, all fields except Billing details should be enabled
     * If updating, all fields except "Select a vehicle" and "Assign a mechanic" should be enabled.
     */
    private void conditionalRender() {
        
        if(!editMode) {
            /**
             * IF CREATING A NEW BOOKING.
             */
            if(this.vehicleID != -1) {
                checkVehicle(this.vehicleID);
            } else {
                vehicleStatus.setText("Vehicle Not Selected");
                vehicleStatus.setTextFill(Color.RED);
            }
            populateMechanicView();
            accordion.setExpandedPane(vehicleSelectPane);
            diagnosisSelectPane.setCollapsible(false);
            diagnosisSelectPane.setText("Diagnosis and Repair (Disabled until Start Date)");
            mechanicStatus.setTextFill(Color.RED); // By default, mechanic will not be set
            diagnosisStatus.setTextFill(Color.GREEN);
            mechanicStatus.setText("Mechanic Not Assigned");
            diagnosisStatus.setText("Diagnosis Not Applicable");
            addBooking.setDisable(false);
            updateBooking.setDisable(true);
            repairTimeSlider.setDisable(true);
            startDate.setValue(LocalDate.parse(Database.getInstance().getCurrentDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            /**
             * IF EDITING AN EXISTING BOOKING.
             */
            vehicleSelectPane.setCollapsible(false);
            vehicleStatus.setTextFill(Color.GREEN);
            mechanicStatus.setTextFill(Color.GREEN);
            startDate.setValue(booking.getStartDate().toLocalDate()); // Converts LocalDateTime to LocalDate
            hourSelection.setText(String.format("%02d", booking.getStartDate().getHour()));
            minuteSelection.setText(String.format("%02d", booking.getStartDate().getMinute()));
            endDate.setValue(booking.getEndDate().toLocalDate()); // Converts LocalDateTime to LocalDate
            endHourSelection.setText(String.format("%02d", booking.getEndDate().getHour()));
            endMinuteSelection.setText(String.format("%02d", booking.getEndDate().getMinute()));
            currMileage.setText(Integer.toString(booking.getVehicleMileage()));
            cost.setText( String.format( "%.2f", booking.getCost() ) );
            paid.setSelected(booking.isPaid());
            
            vehicleStatus.setText("Vehicle Selected");
            mechanicStatus.setText("Mechanic Assigned");
            vehicleSelectPane.setText("Vehicle Selected (Registration: "+booking.getVehicleReg()+")");
            addBooking.setDisable(true);
            updateBooking.setDisable(false);
            if(this.bookingInProgress || this.bookingComplete) { // If editing a booking, but Start Date has passed/approached
                
                /** Load Up Repair Time Slider.
                 * Make the smallest value whatever the current repair time is.
                 * This prevents the user from decreasing the number of hours worked, which can't be taken back in reality. */
                resetSlider();
                
                /** GET ALL PARTS ALREADY IN BOOKING, AND OTHER INSTALLED PARTS WHICH MAY STILL BE ADDED. */
                accordion.setExpandedPane(diagnosisSelectPane);
                try {
                    if(booking.isVehicleForSPC()) {
                        addEditParts.setDisable(true);
                        repairTimeSlider.setDisable(true);
                        sendVehicleToSPC.setTextFill(Color.RED);
                        sendVehicleToSPC.setText("Cancel SPC Vehicle Repair");
                    } else {
                        repairTimeSlider.setDisable(false);
                        sendVehicleToSPC.setTextFill(Color.GREEN);
                        sendVehicleToSPC.setText("Send Vehicle To SPC Centre"); 
                    }
                    refreshPartsViewAndBill();
                } catch (SQLException ex) {
                    showError("Rendering Error", "There was an issue populating the Diagnosis and Repairs TableView...");
                    Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(booking.getFault().equals("N/A")) {
                    diagnosisStatus.setTextFill(Color.RED);
                    diagnosisStatus.setText("Diagnosis Not Recorded");
                } else {
                    diagnosedFault.setText(this.booking.getFault());
                    diagnosisStatus.setTextFill(Color.GREEN);
                    diagnosisStatus.setText("Diagnosis Recorded");
                }

                /** Mechanic is not editable after Start Date.
                 * This helps to fulfil Requirement 2, by not allowing the user to modify the assigned mechanic after repairs have potentially begun. */
                mechanicSelectPane.setCollapsible(false);
                mechanicSelectPane.setText("Mechanic Assigned (Name: "+booking.getMechanicName()+")");

                startDate.setDisable(true);
                minuteSelection.setDisable(true);
                hourSelection.setDisable(true);
                
                if(this.bookingInProgress) {
                    /** IF BOOKING IS IN PROGRESS (HAS STARTED BUT NOT FINISHED). */
                    complete.setDisable(false);
                    complete.setSelected(false);
                    currMileage.setDisable(true); // Current Mileage should only be editable if booking completed
                    cost.setDisable(false);
                    if(booking.isVehicleForSPC()) repairTimeSlider.setDisable(true);
                    else repairTimeSlider.setDisable(false);
                    repairTimeSlider.valueProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                            if(old_val.intValue() != new_val.intValue())
                                refreshBill();
                        }
                    });
                    costingPane.setDisable(false);
                    diagnosisSelectPane.setText("Diagnosis and Repair");
                } else if(this.bookingComplete) {
                    /** IF BOOKING IS COMPLETE. */
                    diagnosedFault.setEditable(false);
                    endDate.setDisable(true);
                    endHourSelection.setDisable(true);
                    endMinuteSelection.setDisable(true);
                    costingPane.setVisible(false);
                    completedPane.setVisible(true);
                    completedCost.setText("£"+String.format( "%.2f", booking.getCost()));
                    completedRepairTime.setText(booking.getRepairTime()+" hour(s)");
                    repairPartCheck.setEditable(false);
                    forSPCCheck.setEditable(false);
                    sendVehicleToSPC.setDisable(true);
                    addEditParts.setDisable(true);
                    repairTimeSlider.setDisable(true);
                    cost.setDisable(true);
                    currMileage.setDisable(false);
                    complete.setDisable(true);
                    complete.setSelected(true);
                    diagnosisSelectPane.setText("Diagnosis and Repair (Uneditable since booking complete)");
                }
            } else { // If editing a booking, but start date has NOT yet passed
                currMileage.setDisable(true);
                populateMechanicView(); // Mechanic can STILL be edited
                mechanicSelectList.getSelectionModel().select(this.selectedMechanic);
                mechanicSelected();
                mechanicSelectPane.setText("Change Assigned Mechanic (Name: "+booking.getMechanicName()+")");
                vehicleSelectPane.setCollapsible(true);
                accordion.setExpandedPane(mechanicSelectPane);
                mechanicSelectPane.setCollapsible(false);
                diagnosisStatus.setTextFill(Color.GREEN);
                diagnosisStatus.setText("Diagnosis Not Applicable");
                diagnosisSelectPane.setCollapsible(false);
                vehicleSelectPane.setCollapsible(false);
                diagnosisSelectPane.setText("Diagnosis and Repair (Disabled until Start Date)");
            }
        }
    }
    
    @FXML
    private void addEditParts() {
        try {
            if(!syncPartsInBooking())
                return;
            if(cost.getText().equals("")) throw new IllegalArgumentException("Cost cannot be empty while the booking is in progress.");
            LocalDateTime startDateTime = booking.getStartDate();
            if(endHourSelection.getText().equals("")||endMinuteSelection.getText().equals("")) throw new IllegalArgumentException("You must provide values for the estimated end time.");
            LocalDateTime endDateTime = Utility.constructDateTimeObj( endDate.getValue(), Integer.parseInt(endHourSelection.getText()), Integer.parseInt(endMinuteSelection.getText()), false );
            booking.setStartAndEndDate(startDateTime, endDateTime); // Setter with Validation
            if(diagnosedFault.getText().equals("N/A") || diagnosedFault.getText().equals("")) throw new IllegalArgumentException("You are required to diagnose the reported fault and enter it on the Diagnosis and Repair tab, before updating the booking.");
            else this.booking.setFault(diagnosedFault.getText());
            diagnosisStatus.setTextFill(Color.GREEN);
            diagnosisStatus.setText("Diagnosis Recorded");
            booking.setCost(Double.parseDouble(cost.getText())); // Set cost to what was entered
            int oldRepairTime = booking.getRepairTime();
            int newRepairTime = (int)repairTimeSlider.getValue();
            if(newRepairTime>oldRepairTime) {
                booking.incrementCost(bookingBill.netMechanicCost(newRepairTime-oldRepairTime)); // Then add the extra labour costs on top
                booking.setRepairTime(newRepairTime);
            }
            resetSlider();
            this.refreshPartsViewAndBill();
            
            booking.updateBooking();
            
            diagnosisPartsList.setVisible(false);
            SentOffToParts.setVisible(true);
            updateBooking.setDisable(true);
            showAlert("Opening Parts module...", "Redirecting...", "In order to install new parts on the vehicle, or cancel any reserved parts, you need to access this booking from the parts module. As soon as you're done, click Update Booking from that view, then you'll be brought back here...");
            // Method from "Krzysztof Szewczyk" on StackOverflow - How to create a modal window in JavaFX 2.1
            // Open parts tab
            FXMLLoader loader = new FXMLLoader();
            Pane root = null;
            root = loader.load(getClass().getResource("/parts/GUI/PartsForBookingView.fxml").openStream());
            PartsForBookingViewController partsController = (PartsForBookingViewController)loader.getController();
            partsController.initializeWithBooking(booking);
            partsController.setTabs(partsTab, parentTab);
            partsTab.setContent(root);
            partsTab.getTabPane().getSelectionModel().select(partsTab);
            // Disable all tabs except parts
            TabPane pane = partsTab.getTabPane();
            ObservableList<Tab> obsvlist = pane.getTabs();
            for (Tab tab : obsvlist) {
                if(!tab.equals(partsTab)) {
                    tab.setDisable(true);
                }
            }
            // Reset my tab to search view
            FXMLLoader searchloader = new FXMLLoader();
            Pane searchRoot = searchloader.load(getClass().getResource("/diagrep/gui/SearchBookingsFXML.fxml").openStream());
            SearchController bookingSearchController = (SearchController)searchloader.getController();
            bookingSearchController.setTabs(parentTab, partsTab);
            parentTab.setContent(searchRoot);
        } catch (IOException ex) {
            showError("Unable to open view", ex.getMessage());
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            showError("Unable to update parts in booking", ex.getMessage());
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            showError("Diagnosis Required", ex.getMessage());
        }
    }
    
    @FXML
    private void addBooking(ActionEvent evt) throws IOException {
        
        try {
            /** GET INPUT DATA.
             * Retrieves input data. If any input field not set, will throw a NullPointerException
             */
            LocalDate startdate = startDate.getValue();
            LocalDate enddate = endDate.getValue();
            if(hourSelection.getText().equals("")||minuteSelection.getText().equals(""))
                throw new NullPointerException();
            int hour = Integer.parseInt(hourSelection.getText());
            int min = Integer.parseInt(minuteSelection.getText());
            int endhour = Integer.parseInt(endHourSelection.getText());
            int endmin = Integer.parseInt(endMinuteSelection.getText());
            int mechID = selectedMechanic.ID.get();
            String mechanicFullName = selectedMechanic.FIRSTNAME.get() + " " + selectedMechanic.SURNAME.get();
            int mileage;
            if(currMileage.getText().equals("")) throw new NullPointerException();
            else mileage = Integer.parseInt(currMileage.getText());
            if(vehicleID == -1) throw new NullPointerException();
            
            /** VALIDATE INPUT DATA.
             * Ensures all the data is valid, with checks such as:
             * Time and Date are valid (since we manually concatenate them)
             * Start Date is before End Date (and Start Date is before 'Current' Date)
             * Dates comply with the working hours/days of the Garage (exc. public holidays)
             * NEW! Ensures there are no booking conflicts
             */
            LocalDateTime startDateTime = Utility.constructDateTimeObj(startdate, hour, min, true);
            LocalDateTime endDateTime = Utility.constructDateTimeObj(enddate, endhour, endmin, false); // Time is defaulted to 11:30
            if(!Utility.isDate1BeforeDate2(startDateTime, endDateTime)) {
                // If Start Date is NOT before End Date
                showError("Validation Error", "Start Date cannot occur after End Date");
                return;
            } else if(!Utility.isDateInFuture(startDateTime)) {
                // If Start Date is not in the future (according to Current Date table)
                showError("Validation Error", "The Start Date and Time you selected has already passed, according to  the 'CurrentDate'");
                return;
            } else if(Utility.isPublicHoliday(startDateTime)||Utility.isPublicHoliday(endDateTime)) {
                // If Start Date or End Date is a public or bank holiday according to our records
                showError("Validation Error", "At least one of the dates you entered are a public holiday, therefore the Garage will not be open.");
                return;
            } else if(!Utility.isMechanicValid(mechID)) {
                /** MECHANIC VALIDITY CHECK - ADDED 25 MARCH.
                 * Ensures that the mechanic was not deleted or had their mechanic status revoked while the booking tab was rendered. */
                showError("Mechanic Not Available", "Could not assign selected mechanic to booking. Either the user no longer exists or their mechanic status was revoked. Please select another and try again.");
                this.populateMechanicView();
                return;
            } else {
                // Now, since the dates are valid, we further check that this booking doesn't overlap with any other bookings for THIS vehicle
                Database db = Database.getInstance();
                Connection con = db.getConnection();
                /** Logic adapted from "Charles Bretana" on StackOverflow - Determine Whether Two Date Ranges Overlap.
                 * Rather than checking each booking for the vehicle against the new date range, we use the following query.
                 * We search for any NON-COMPLETED bookings for THIS VEHICLE, which do NOT SATISFY this logic.
                 * The main logic ensures that any 2 dates DO NOT overlap, based on De Morgan's laws.
                 */
                String getOverlappingBookings = 
                    "SELECT DiagRepID, StartDate, EndDate FROM DiagRepairBooking WHERE\n" +
                    "VehicleID = ? AND Completed = 0\n" + // Make search specific to this vehicle
                    "AND NOT (\n" +
                    "	StartDate >= ?\n" + // Check that StartDate of each booking occurs after/on EndDate of the new date range
                    "	OR EndDate <= ?\n" + // Check that EndDate of each booking occurs before/on StartDate of the new date range
                    ")";
                PreparedStatement overlappingBookingsStatement = con.prepareStatement(getOverlappingBookings);
                overlappingBookingsStatement.setInt(1, this.vehicleID); // Vehicle ID
                overlappingBookingsStatement.setString( 2, Utility.DateTimeToStr(endDateTime) ); // New intended end date/time
                overlappingBookingsStatement.setString( 3, Utility.DateTimeToStr(startDateTime) ); // New intended start date/time
                ResultSet overlappingBookingIDs = overlappingBookingsStatement.executeQuery();
                if(!overlappingBookingIDs.isClosed()) {
                    // Inform user of the booking ID(s) that conflicts with this new start/end date, for this particular vehicle.
                    String listOfConflictIDs = "";
                    while(overlappingBookingIDs.next()) {
                        listOfConflictIDs += "\n\nBooking ID: "+overlappingBookingIDs.getInt("DiagRepID") + "\nStart Date: "+overlappingBookingIDs.getString("StartDate") + "\nEnd Date: " + overlappingBookingIDs.getString("EndDate");
                    }
                    throw new IllegalArgumentException("The Start and End Date you selected overlap with another active booking for this vehicle.\n\nBooking ID(s) which conflict:"+listOfConflictIDs);
                } // Else, continue to create booking
            }
            
            /** CREATE BOOKING.
             * Using all input fields, and after validation, we now insert the data into the database
             */
            Database db = Database.getInstance();
            Connection connection = db.getConnection();
            String insertQuery = "INSERT INTO `DiagRepairBooking` (CostOfService, StartDate, EndDate, Completed, VehicleID, RepairTime, MechanicID, Paid, sendVehicleToSPC) VALUES (?,?,?,?,?,?,?,?,?);";
            String key[] = {"DiagRepID"}; //put the name of the primary key column
            PreparedStatement ps = connection.prepareStatement(insertQuery, key);
            ps.setDouble(1, 0); // Cost (default to 0 when making a new booking? Or booking/admin fee?)
            ps.setString(2, Utility.DateTimeToStr(startDateTime)); // Start Date
            ps.setString(3, Utility.DateTimeToStr(endDateTime)); // End Date
            ps.setInt(4, 0); // Completed Status
            ps.setInt(5, vehicleID); // Vehicle ID [FOREIGN KEY]
            ps.setInt(6, 0); // Repair Time (default to 0)
            ps.setInt(7, mechID); // Mechanic ID [FOREIGN KEY]
            ps.setInt(8, 0); // Paid Status (default to unpaid)
            ps.setInt(9, 0); // SendVehicleToSPC - Default to false
            System.out.print("Inserting row... ");
            ps.executeUpdate();
            System.out.println("Successful!");
            ResultSet rs = ps.getGeneratedKeys();
            // Code by atripathi (StackOverflow): Primary key from inserted row jdbc?
            long generatedKey = rs.getLong(1);
            int DiagRepID = (int)generatedKey;
            System.out.println("DiagRepair Booking ID: "+DiagRepID);
            
            /** UPDATE VEHICLE MILEAGE.
             * Requirement 7 - My interpretation is that I have to update this directly on the vehicle record.
             * Sadly, I cannot rely on the Vehicle class for getters and setters due to lack of progress of that module.
             * Therefore, I am updating the attribute directly.
             */
            String mileageQuery = "UPDATE `Vehicle` SET CurrentMileage = ? WHERE VehicleID = ?;";
            ps = connection.prepareStatement(mileageQuery);
            ps.setInt(1, mileage);
            ps.setInt(2, vehicleID);
            ps.executeUpdate();
            
            showAlert("Booking Confirmation", "Booking Created!", "This booking has successfully been added to the database!\n\nBooking ID: "+DiagRepID+"\nStart Date: "+Utility.DateTimeToStr(startDateTime)+"\nEnd Date: "+Utility.DateTimeToStr(endDateTime)+"\nAssigned Mechanic: "+mechanicFullName);
            // Re-render booking page into edit mode
            setBookingToEdit(new DiagRepair(DiagRepID));
            conditionalRender();
        } catch (NullPointerException e) {
            showError("Missing Input Fields", "Some required details were not entered...\nPlease check the following:\n\n- Start Date\n- Start Time\n- Est. End Date\n- Current Mileage\n- Vehicle Selection\n- Mechanic Assignment");
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
            //Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, e);
        } catch (SQLException ex) {
            showError("Database Error", ex.getMessage());
            //Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Maybe reset fields upon success, or refresh?
    }
    
    @FXML
    private void updateBooking() {
        try {
            /** GET INPUT DATA.
            * Retrieves input data. If any input field not set, will throw a NullPointerException
            */
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;
            int mileage;
            
            if(bookingInProgress) { // If Start Date passed (but not complete)
                if(cost.getText().equals("")) throw new IllegalArgumentException("Cost cannot be empty while the booking is in progress.");
                startDateTime = booking.getStartDate();
                if(endHourSelection.getText().equals("")||endMinuteSelection.getText().equals("")) throw new IllegalArgumentException("You must provide values for the end time.");
                endDateTime = Utility.constructDateTimeObj( endDate.getValue(), Integer.parseInt(endHourSelection.getText()), Integer.parseInt(endMinuteSelection.getText()), false );
                booking.setStartAndEndDate(startDateTime, endDateTime); // Setter with Validation
                if(diagnosedFault.getText().equals("N/A") || diagnosedFault.getText().equals("")) throw new IllegalArgumentException("You are required to diagnose the reported fault and enter it on the Diagnosis and Repair tab, before updating the booking.");
                else this.booking.setFault(diagnosedFault.getText());
                diagnosisStatus.setTextFill(Color.GREEN);
                diagnosisStatus.setText("Diagnosis Recorded");
                //booking.setRepairTime((int)repairTimeSlider.getValue());
                booking.setCost(Double.parseDouble(cost.getText())); // Set cost to what was entered
                int oldRepairTime = booking.getRepairTime();
                int newRepairTime = (int)repairTimeSlider.getValue();
                if(newRepairTime>oldRepairTime) {
                    booking.incrementCost(bookingBill.netMechanicCost(newRepairTime-oldRepairTime)); // Then add the extra labour costs on top
                    booking.setRepairTime(newRepairTime);
                }
                refreshBill();
                resetSlider();
            } else if(bookingComplete) { // If booking completed (all done)
                startDateTime = booking.getStartDate();
                endDateTime = booking.getEndDate();
            } else { // If booking hasn't even begun yet (everything pretty much changeable)
                int hour = Integer.parseInt(hourSelection.getText());
                int min = Integer.parseInt(minuteSelection.getText());
                startDateTime = Utility.constructDateTimeObj(startDate.getValue(), hour, min, true);
                if(endHourSelection.getText().equals("")||endMinuteSelection.getText().equals("")) throw new IllegalArgumentException("You must provide values for the end time.");
                endDateTime = Utility.constructDateTimeObj( endDate.getValue(), Integer.parseInt(endHourSelection.getText()), Integer.parseInt(endMinuteSelection.getText()), false );
                booking.setStartAndEndDate(startDateTime, endDateTime); // Setter with Validation
            }
            
            /**
             * MECHANIC VALIDATION - ADDED 24 MARCH.
             * Mechanics can now be deleted while assigned to (future) bookings, or their mechanic status can be revoked.
             * Therefore, it's important that we validate the mechanic's status upon EVERY update of a booking.
             * If a booking is complete, we allow the mechanic to be deleted, since we never update the DiagRep table upon completion.
             */
            if(!this.bookingComplete) { // If booking not complete
                // In case the assigned Mechanic was removed from system or mechanic status revoked, user is required to select a new mechanic
                // UPDATE - We now ALWAYS revalidate whilst the booking is NOT complete
                if(this.bookingInProgress) {
                    // If the booking is in progress, it means the mechanic select tab is not rendered, so just revalidate the booking's Mechanic ID
                    // REVALIDATE EXISITING MECHANIC ID
                    booking.setMechanicID( booking.getMechanicID() );
                } else {
                    // If booking has not started yet (or is not active yet), then validate and set booking's mechanic ID to the new (or unchanged) selection
                    // VALIDATE SELECTED MECHANIC ID
                    booking.setMechanicID( selectedMechanic.ID.get() );
                }
            }
            
            /** UPDATE VEHICLE MILEAGE.
             * Only can be updated if booking completed.
             * Test Case 10.
             */
            if(this.bookingComplete) { // If booking already complete
                if(currMileage.getText().equals("")) throw new NullPointerException();
                else mileage = Integer.parseInt(currMileage.getText());
                Database db = Database.getInstance();
                Connection connection = db.getConnection();
                String mileageQuery = "UPDATE `Vehicle` SET CurrentMileage = ? WHERE VehicleID = ?;";
                PreparedStatement ps = connection.prepareStatement(mileageQuery);
                ps.setInt(1, mileage);
                ps.setInt(2, this.booking.getVehicleID());
                ps.executeUpdate();
                showAlert("Update Successful", "Booking Updated!", "This booking has successfully been updated on the database!\n\nBooking ID: "+booking.getBookingID()+"\nDate Started: "+Utility.DateTimeToStr(startDateTime)+"\nDate Ended: "+Utility.DateTimeToStr(endDateTime)+"\nAssigned Mechanic: "+booking.getMechanicName());
                return;
            } else if(complete.isSelected()) { // User NOW wants to mark booking as complete
                booking.updateBooking(); // Update overview details anyway to store updates
                if(!syncPartsInBooking()) {
                    // It's important all repair changes are synchronised successfully before performing the markAsComplete() method
                    System.out.println("Updated");
                    showError("Unable to complete booking", "Please adjust any repair selections you made to resolve the problem, then try again updating the booking to complete.");
                    return;
                }
                booking.markAsComplete();
                Database db = Database.getInstance();
                Connection connection = db.getConnection();
                String lastServiceDateQuery = "UPDATE `Vehicle` SET DateOfLastService = ? WHERE VehicleID = ?;";
                PreparedStatement ps = connection.prepareStatement(lastServiceDateQuery);
                ps.setString(1, db.getCurrentDate()); // Get current date from database
                ps.setInt(2, this.booking.getVehicleID());
                ps.executeUpdate(); // Sets date of last service for the vehicle
                showAlert("Booking Complete!", "This booking is now complete!", "Further repairs can no longer be carried out. The End Date and Time for the booking has been set to the current one for obvious reasons.\n\nConfirmed End Date: "+Utility.DateTimeToStr(booking.getEndDate()));
                this.bookingInProgress = false;
                this.bookingComplete = true;
                this.bookingBill = null;
                conditionalRender();
            } else { // Booking not started yet or in progress
                booking.updateBooking();
                if(!syncPartsInBooking())
                    return;
            }
            showAlert("Update Successful", "Booking Updated!", "This booking has successfully been updated on the database!\n\nBooking ID: "+booking.getBookingID()+"\nStart Date: "+Utility.DateTimeToStr(booking.getStartDate())+"\nEnd Date: "+Utility.DateTimeToStr(booking.getEndDate())+"\nAssigned Mechanic: "+booking.getMechanicName());
            this.refreshPartsViewAndBill();
        } catch (NullPointerException e) {
            showError("Missing Input Fields", "Some Required Details Were Not Entered - Please check the following:\n- Start Date\n- Start Time\n- Est. End Date\n- Current Mileage\n- Mechanic Assignment");
            //System.out.println(e.getMessage());
            //Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalArgumentException ex) { // Picks up all possible validation errors
            showError("Validation Error", ex.getMessage());
            //Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            showError("Database Error", ex.getMessage());
            //Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InProgressException ex) {
            showError("Could Not Complete Booking", ex.getMessage());
            complete.setSelected(false);
            //Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /** SYNCHRONISE PARTS IN VIEW WITH PARTS IN BOOKING.
     * This section will try to implement all selections made on the TableView.
     * Where an SPC cancellation is involved, will prompt the user to override it and delete
     * If true is returned, there were no issues, else if false, then there were changes that could not be implemented - user should check them.
     * @return true if synchronisation successful, false if any errors
     */
    private boolean syncPartsInBooking() throws SQLException {
        PartForRepairBooking partForRepairBooking;
        String updateLog = "";
        if(booking.isVehicleForSPC())
            return true;
        int confirmCode;
        for (PartInfo row : diagnosisPartsList.getItems()) { // For all parts in the TableView
            try {
                int count = 0;
                if(row.repairOnly.get()) count++;
                if(row.newPart.get()) count++;
                if(row.isForSPC.get()) count++;
                if(count>1) throw new IllegalArgumentException("A part can only be given up to ONE repair solution.");
                if(count==0 && row.inBooking.get()) {
                    // Nothing selected, but in booking (possibly means cancellations)
                    partForRepairBooking = new PartForRepairBooking(row.partID.get(), booking.getBookingID());
                    try { // Attempt 1 : No forcing
                        partForRepairBooking.removeFromBooking(false);
                    } catch(InProgressException ex) { // If SPC booking in progress, warn user
                        confirmCode = getConfirmation("Warning!", "Part: "+row.name.get(), ex.getMessage());
                        if(confirmCode == 1) {
                            partForRepairBooking.removeFromBooking(true); // If user accepted, delete SPC booking too
                            showAlert("SPC Booking Cancelled", "Cancellation Successful", "The part has been removed from the SPC Centre's system and is no longer part of this booking.");
                        }
                    }
                } else if(row.isForSPC.get()) {
                    // Whether or not in booking, if user wants to send part to SPC
                    partForRepairBooking = new PartForRepairBooking(row.partID.get(), booking.getBookingID());
                    partForRepairBooking.setSPCRepair();
                } else if(row.repairOnly.get()) {
                    // If user wants to only repair the part
                    partForRepairBooking = new PartForRepairBooking(row.partID.get(), booking.getBookingID());
                    try { // Attempt 1 : No forcing
                        partForRepairBooking.setRepairOnly(false);
                    } catch(InProgressException ex) { // If SPC booking in progress, warn user
                        confirmCode = getConfirmation("Warning!", "Part: "+row.name.get(), ex.getMessage());
                        if(confirmCode == 1) {
                            partForRepairBooking.setRepairOnly(true); // If user accepted, delete SPC booking too
                            showAlert("SPC Booking Cancelled", "Operation Successful", "The part has been removed from the SPC Centre's system, and has been selected for in-Garage repair only.");
                        }
                    }
                } // New Part checkbox is disabled for users, thus no handler needed
            } catch(IllegalArgumentException | InProgressException ex) {
                // FAILED
                updateLog += "\n\nPart: "+row.name.get()+"\nError: "+ex.getMessage();
            }
        }
        if(!updateLog.equals("")) {
            showError("Some Updates Failed", "Some changes you made to the repair selections were not updated. Please see the details below:"+updateLog);
            return false;
        } else {
            return true;
        }
    }
    
    public void resetVehicle() {
        vehicleID = -1;
        vehicleReg.setText("");
        vehicleReg.setDisable(false);
        vehicleRegCheck.setDisable(false);
        vehicleRegReset.setDisable(true);
        currMileage.setText("");
        vehicleInfo.setText("Please enter a full vehicle registration to create a booking for, then click 'Check' to find the vehicle on our database.");
        vehicleStatus.setText("Vehicle Not Selected");
        vehicleStatus.setTextFill(Color.RED);
    }
    
    public void checkVehicle() {
        try {
            String enteredVehicleReg = vehicleReg.getText();
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT Vehicle.VehicleID, Vehicle.CurrentMileage, Vehicle.DateOfLastService, VehicleInfo.Registration, Customer.Firstname, Customer.Surname FROM Vehicle INNER JOIN VehicleInfo ON Vehicle.VehicleID = VehicleInfo.VehicleID INNER JOIN Customer ON Vehicle.CustomerID = Customer.CustomerID WHERE VehicleInfo.Registration = ?");
            statement.setString(1, enteredVehicleReg);
            checkVehicle(statement);
        } catch (SQLException ex) {
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void checkVehicle(int vehicleID) {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT Vehicle.VehicleID, Vehicle.CurrentMileage, Vehicle.DateOfLastService, VehicleInfo.Registration, Customer.Firstname, Customer.Surname FROM Vehicle INNER JOIN VehicleInfo ON Vehicle.VehicleID = VehicleInfo.VehicleID INNER JOIN Customer ON Vehicle.CustomerID = Customer.CustomerID WHERE Vehicle.VehicleID = ?");
            statement.setInt(1, vehicleID);
            checkVehicle(statement);
        } catch (SQLException ex) {
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void checkVehicle(PreparedStatement ps) {
        try {
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                if(currMileage.getText().equals("") && rs.getString("CurrentMileage")!=null) {
                    currMileage.setText( rs.getString("CurrentMileage") );
                }
                vehicleReg.setText( rs.getString("Registration") );
                vehicleInfo.setText( "Vehicle ID "+rs.getInt("VehicleID")+" Found On Database!\nLast Service Date: " + rs.getString("DateOfLastService") + "\nCustomer Name: " + rs.getString("Firstname") + " " + rs.getString("Surname") );
                vehicleID = rs.getInt("VehicleID");
                vehicleRegCheck.setDisable(true);
                vehicleRegReset.setDisable(false);
                vehicleReg.setDisable(true);
                vehicleStatus.setText("Vehicle Selected");
                vehicleStatus.setTextFill(Color.GREEN);
            } else {
                vehicleInfo.setText("Unable to find vehicle on the database...");
                vehicleRegCheck.setDisable(false);
                vehicleRegReset.setDisable(true);
                vehicleStatus.setText("Vehicle Not Selected");
                vehicleStatus.setTextFill(Color.RED);
                vehicleID = -1;
            }
        } catch (SQLException ex) {
            vehicleInfo.setText("Unable to find vehicle on the database...");
            vehicleStatus.setText("Vehicle Not Selected");
            vehicleStatus.setTextFill(Color.RED);
            vehicleID = -1;
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /** Re-populates the list of parts in the booking, and recalculates the estimated bill.
     * If vehicle was sent to SPC centre, will not 
     * @throws SQLException 
     */
    public void refreshPartsViewAndBill() throws SQLException {
        // Populate TableView of parts for the "Repairs and Diagnosis" tab
        if(editMode) {
            updateBooking.setDisable(false);
            if(bookingBill!=null) bookingBill.resetBill(); // Reset bill
            diagnosisPartsList.setItems(null); // Reset TableView
            
            /**
             * If vehicle was selected for SPC repair.
             * Refresh bill and check status of repair
             */
            if( booking.isVehicleForSPC() ) { // If vehicle was sent/marked for SPC repair
                if(this.bookingComplete) {
                    diagnosisPartsList.setPlaceholder(new Label("Vehicle was sent to the SPC Centre..."));
                    refreshBill();
                    return;
                }
                String checkVehicleRepairStatus = "SELECT ReturnStatus FROM SPCBooking WHERE IsBookingForVehicle = 1 AND DiagRepBookingID = ?";
                Database db = Database.getInstance();
                Connection con = db.getConnection();
                PreparedStatement getRepairStatusStatement = con.prepareStatement(checkVehicleRepairStatus);
                getRepairStatusStatement.setInt(1, this.booking.getBookingID());
                ResultSet SPCVehicleStatus = getRepairStatusStatement.executeQuery();
                if(SPCVehicleStatus.isClosed()) {
                    diagnosisPartsList.setPlaceholder( new Label("SPC Vehicle Repair Status >>> NOT YET BOOKED") );
                } else if(SPCVehicleStatus.getInt(1) == 1) {
                    diagnosisPartsList.setPlaceholder( new Label("SPC Vehicle Repair Status >>> COMPLETED") );
                } else {
                    diagnosisPartsList.setPlaceholder( new Label("SPC Vehicle Repair Status >>> IN PROGRESS") );
                }
                refreshBill();
                return;
            }
            
            ArrayList<PartInfo> partsList = new ArrayList<>(); // Parts to show in the tableview
            ArrayList<String> partNamesInBooking = new ArrayList<>(); // Names of all parts already in the booking
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            
            // TO DO - REVERSE THE FUNCTION (ID > Name), so that we only bill parts that are still installed within the booking
            // However this should never happen... If Parts uninstalls a part, it should delete it from all active bookings

            /**
             * Step 1 - Retrieves all installed parts on the vehicle
             * This makes it much more easier and efficient to check (saves a ton of SQL Queries).
             */
            HashMap<String, Integer> installedPartsMap = new HashMap<>();
            String installedPartsQuery = "SELECT Name, PartID FROM PartInVehicle INNER JOIN Part ON PartInVehicle.PartID=Part.ID WHERE PartInVehicle.VehicleID = ?;";
            PreparedStatement getInstalledParts = con.prepareStatement(installedPartsQuery);
            getInstalledParts.setInt(1, this.booking.getVehicleID());
            ResultSet rs0 = getInstalledParts.executeQuery();
            while(rs0.next()) {
                // Up to 10 parts max
                installedPartsMap.put(rs0.getString("Name"), rs0.getInt("PartID"));
            }

            String nameOfPart;
            int partID;
            Boolean isForSPC;
            Boolean newPart;
            Boolean repairOnly;
            Boolean partInVehicle;
            Boolean bookingEmpty = true;
            String excludePartStr = "";

            /**
             * Step 2 - Retrieves all parts which are already associated to the booking
             * FINAL VERSION.
             * While retrieving each part, we build an SQL exclusion sub-query, so it's straightforward
             * to determine which parts in our inventory are not yet part of the booking (installed or not)
             */
            String partsInBookingQuery = "SELECT PartID, Name, RepairOnly, NewPart, IsForSPC FROM PartForRepairBooking INNER JOIN Part ON PartForRepairBooking.PartID = Part.ID WHERE PartForRepairBooking.RepairBookingID = ?;";
            PreparedStatement getPartsInBooking = con.prepareStatement(partsInBookingQuery);
            getPartsInBooking.setInt(1, this.booking.getBookingID());
            ResultSet rs = getPartsInBooking.executeQuery();

            while(rs.next()) {
                nameOfPart = rs.getString("Name");
                partID = rs.getInt("PartID");
                isForSPC = (rs.getInt("IsForSPC")==1);
                repairOnly = (rs.getInt("RepairOnly")==1);
                newPart = (rs.getInt("NewPart")==1);
                bookingEmpty = false;

                partInVehicle = (installedPartsMap.get(nameOfPart)!=null); // Checks if this part is installed
                // The only reason it wouldn't be is if a new part was installed, but removed from a completed booking
                try {
                    if( bookingBill!=null && (newPart || repairOnly) ) {
                        InstalledPart installedPart = new InstalledPart(partID);
                        if(newPart) bookingBill.addInstalledPart(installedPart);
                        else if(repairOnly) bookingBill.addRepairedPart(installedPart);
                    }
                } catch(IllegalArgumentException ex) {
                    showError("Bill Error", ex.getMessage());
                }
                PartInfo partForBooking = new PartInfo(partID, nameOfPart, partInVehicle, true, repairOnly, newPart, isForSPC);
                partNamesInBooking.add(nameOfPart); // We are constructing an array of part types to exclude from the otherPartsQuery
                partsList.add(partForBooking); //
                System.out.println("[IN BOOKING] Added part "+nameOfPart+" with ID "+partID);
            }

            /**
             * Step 3 - Retrieves all other part types that are installed in the vehicle, but not yet part of the booking.
             * This excludes all parts already in booking, using the exclusion sub-query
             */
            if(partNamesInBooking.isEmpty()) {
                // No parts in the booking
                excludePartStr = " WHERE ";
            } else {
                // 1 or more parts in the booking
                excludePartStr = " WHERE NOT (Part.Name = ?";
                for(int i = 1; i < partNamesInBooking.size(); i++) {
                    excludePartStr += " OR Part.Name = ?";
                }
                excludePartStr += ") AND ";
            }
            // Build query
            String otherPartsQuery = "SELECT Name, PartID FROM Part INNER JOIN PartInVehicle ON Part.ID = PartInVehicle.PartID" + excludePartStr + "PartInVehicle.VehicleID = " + booking.getVehicleID() + ";";
            // Prepare the statement
            PreparedStatement getOtherPartsNotInBooking = con.prepareStatement(otherPartsQuery);
            // Set each of the part names in the prepared statement (if any)
            for(int i = 1; i <= partNamesInBooking.size(); i++) {
                getOtherPartsNotInBooking.setString(i, partNamesInBooking.get(i-1));
            }
            ResultSet rs2 = getOtherPartsNotInBooking.executeQuery();
            PartInfo otherPart;
            while(rs2.next()) {
                // Check if part has been installed in vehicle in past...
                nameOfPart = rs2.getString("Name");
                if(installedPartsMap.get(nameOfPart)!=null) { // Part is installed, thus has an ID
                    //System.out.println("installed, not in book");
                    otherPart = new PartInfo(installedPartsMap.get(nameOfPart), nameOfPart, true, false, false, false, false);
                    partsList.add(otherPart);
                    System.out.println("[NOT in booking] Added part "+nameOfPart+" with ID "+rs2.getInt("PartID"));
                }
            }

            /**
             * Step 4 - Bind all CheckBoxes on TableView to associated PartInfo properties.
             * This uses the CheckBoxTableCell class, and what it does is displays the properties
             * of each PartInfo object automatically. Selecting or deselecting a CheckBox will
             * change the object's associated Boolean property automatically too.
             */
            // Code adapted from "Jens-Peter Haack" on Stackoverflow: How to populate a tableview that contain CheckBox in JavaFX
            partName.setCellValueFactory(new PropertyValueFactory<>("Name"));
            partInstalled.setCellValueFactory(new PropertyValueFactory<>("Installed"));
            partInstalled.setCellFactory(column -> new CheckBoxTableCell());
            repairPartCheck.setCellValueFactory(new PropertyValueFactory<>("RepairOnly"));
            repairPartCheck.setCellFactory(column -> new CheckBoxTableCell());
            addNewPartCheck.setCellValueFactory(new PropertyValueFactory<>("NewPart"));
            addNewPartCheck.setCellFactory(column -> new CheckBoxTableCell());
            forSPCCheck.setCellValueFactory(new PropertyValueFactory<>("IsForSPC"));
            forSPCCheck.setCellFactory(column -> new CheckBoxTableCell());
            ObservableList<PartInfo> partInfoList = FXCollections.observableArrayList(partsList);
            diagnosisPartsList.setItems(partInfoList);
            if(bookingBill!=null) refreshBill();
        }
        SentOffToParts.setVisible(false);
        diagnosisPartsList.setVisible(true);
    }
    
    public void sendVehicleToSPC() {
        try {
            if(!syncPartsInBooking())
                return;
            if(cost.getText().equals("")) throw new IllegalArgumentException("Cost cannot be empty while the booking is in progress.");
            LocalDateTime startDateTime = booking.getStartDate();
            if(endHourSelection.getText().equals("")||endMinuteSelection.getText().equals("")) throw new IllegalArgumentException("You must provide values for the end time.");
            LocalDateTime endDateTime = Utility.constructDateTimeObj( endDate.getValue(), Integer.parseInt(endHourSelection.getText()), Integer.parseInt(endMinuteSelection.getText()), false );
            booking.setStartAndEndDate(startDateTime, endDateTime); // Setter with Validation
            if(diagnosedFault.getText().equals("N/A") || diagnosedFault.getText().equals("")) throw new IllegalArgumentException("You are required to diagnose the reported fault and enter it on the Diagnosis and Repair tab, before updating the booking.");
            else this.booking.setFault(diagnosedFault.getText());
            diagnosisStatus.setTextFill(Color.GREEN);
            diagnosisStatus.setText("Diagnosis Recorded");
            booking.setCost(Double.parseDouble(cost.getText())); // Set cost to what was entered
            int oldRepairTime = booking.getRepairTime();
            int newRepairTime = (int)repairTimeSlider.getValue();
            if(newRepairTime>oldRepairTime) {
                booking.incrementCost(bookingBill.netMechanicCost(newRepairTime-oldRepairTime)); // Then add the extra labour costs on top
                booking.setRepairTime(newRepairTime);
                refreshBill();
            }
            resetSlider();
            booking.updateBooking();
            
            if(!booking.isVehicleForSPC()) {
                this.refreshPartsViewAndBill();
                booking.sendVehicleToSPC(true, false);
                // Added for SPC Vehicle Repair
                showAlert("Action Successful", "Vehicle selected for SPC Repair", "This vehicle has been selected for SPC Repair and is ready to be booked from the SPC Centre. If you made any other changes to the booking, please click \"Update Booking\".");
                sendVehicleToSPC.setTextFill(Color.RED);
                sendVehicleToSPC.setText("Cancel SPC Vehicle Repair");
                diagnosisPartsList.setItems(null);
                diagnosisPartsList.setPlaceholder(new Label("Vehicle was selected to be sent to the SPC Centre...\n\nThis area will be updated to inform you of the progress of the repair..."));
                repairTimeSlider.setDisable(true);
                addEditParts.setDisable(true);
            } else {
                try {
                    booking.sendVehicleToSPC(false, false);
                } catch (InProgressException ex) {
                    // Vehicle already booked by SPC. Ask to cancel...
                    int confirmCode = getConfirmation("Warning!", "Vehicle already booked by the SPC", ex.getMessage());
                    if(confirmCode == 1) {
                        booking.sendVehicleToSPC(false, true);
                    } else {
                        return;
                    }
                }
                // Removed from SPC Vehicle Repair
                showAlert("Cancellation Successful", "Vehicle no longer selected for SPC repair", "This vehicle is no longer selected for SPC repair. If the SPC booking was already completed, then it will not be deleted and the customer will still be charged for the work done. Parts in the vehicle can now be repaired within the Garage. You can still send individual parts to the SPC Centre.");
                sendVehicleToSPC.setTextFill(Color.GREEN);
                sendVehicleToSPC.setText("Send Vehicle To SPC Centre");
                repairTimeSlider.setDisable(false);
                diagnosisPartsList.setPlaceholder(new Label("There are no installed parts in the vehicle..."));
                refreshPartsViewAndBill();
                addEditParts.setDisable(false);
            }
        } catch(InProgressException | SQLException | IllegalArgumentException ex) {
            showError("Cannot complete action", ex.getMessage());
        }
    }
    
    @FXML
    private void goBack(ActionEvent event) {
        /*if(!getConfirmation("Discard changes?", "Unsaved changes will be lost", "Are you sure you want to go back?"))
            return;*/
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchBookingsFXML.fxml"));
            Parent root = loader.load();
            SearchController controller = (SearchController)loader.getController();
            controller.setTabs(parentTab, partsTab);
            parentTab.setContent(root);
        } catch (IOException ex) {
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void viewBillBreakdown(ActionEvent event) {
        try {
            // Method from "Krzysztof Szewczyk" on StackOverflow - How to create a modal window in JavaFX 2.1
            FXMLLoader loader = new FXMLLoader();
            Pane root = null;
            root = loader.load(getClass().getResource("ViewBookingBill.fxml").openStream());
            ViewBookingBillController controller = (ViewBookingBillController)loader.getController();
            bookingBill.setHoursWorked((int)repairTimeSlider.getValue());
            controller.setBill(bookingBill);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner((Stage) ((Node) event.getSource()).getScene().getWindow());
            //stage.initOwner()
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Booking Cost Breakdown");
            stage.showAndWait();
        } catch (IOException | IllegalArgumentException ex) {
            showError("Unable to view bill", ex.getMessage());
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void refreshBill() {
        if(bookingBill==null) return; // Booking must be active (after Start Date)
        bookingBill.setCost(booking.getCost());
        cost.setText( String.format( "%.2f", bookingBill.getCost() ) );
        bookingCost.setText( "£"+String.format( "%.2f", booking.getCost() ) );
        mechanicHourlyRate.setText("£"+String.format( "%.2f", bookingBill.mechanicRate())+"/hour");
        mechanicRepairTime.setText((int)repairTimeSlider.getValue() + " hour(s)");
        costMechanic.setText( "£"+String.format( "%.2f", bookingBill.netMechanicCost( (int)repairTimeSlider.getValue() ) ) );
    }
    
    public void populateMechanicView() {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            Statement st = con.createStatement();
            String getMechanicQuery = "SELECT UserID, Firstname, Surname, HourlyWage FROM SystemUser WHERE IsMechanic = 1;";
            ResultSet rs = st.executeQuery(getMechanicQuery);
            ArrayList<Mechanic> mechanicList = new ArrayList<>();
            while(rs.next()) {
                Mechanic mechToAdd = new Mechanic(rs.getInt("UserID"), rs.getString("Firstname"), rs.getString("Surname"), rs.getDouble("HourlyWage"));
                mechanicList.add( mechToAdd );
                if(editMode && booking.getMechanicID()==rs.getInt("UserID")) this.selectedMechanic = mechToAdd;
            }
            mechanicID.setCellValueFactory(new PropertyValueFactory<>("ID"));
            mechanicName.setCellValueFactory(new PropertyValueFactory<>("FirstName"));
            mechanicLastName.setCellValueFactory(new PropertyValueFactory<>("SurName"));
            mechanicWage.setCellValueFactory(new PropertyValueFactory<>("Wage"));
            ObservableList<Mechanic> mechanicObservableList = FXCollections.observableArrayList(mechanicList);
            mechanicSelectList.setItems(mechanicObservableList);
        } catch (SQLException ex) {
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void resetSlider() {
        repairTimeSlider.setMin(booking.getRepairTime());
        repairTimeSlider.setMax(booking.getRepairTime() + 10);
        repairTimeSlider.setValue(booking.getRepairTime());
    }
    
    @FXML
    private void goToMechanicSelect() {
        accordion.setExpandedPane(mechanicSelectPane);
    }
    
    public void mechanicSelected() {
        if(mechanicSelectList.getSelectionModel().getSelectedItem()!=null) {
            this.selectedMechanic = mechanicSelectList.getSelectionModel().getSelectedItem();
            mechanicHourlyRate.setText("£"+String.format( "%.2f", selectedMechanic.WAGE.get() )+"/hour");
            mechanicStatus.setTextFill(Color.GREEN);
            mechanicStatus.setText("Mechanic Assigned");
        } else {
            this.selectedMechanic = null;
            mechanicHourlyRate.setText("£.../hour");
            mechanicStatus.setTextFill(Color.RED);
            mechanicStatus.setText("Mechanic Not Assigned");
        }
    }
    
    private ArrayList<LocalDate> getHolidays() {
        ArrayList<LocalDate> holidayDates = new ArrayList<>();
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            PreparedStatement getHolidayDatesStatement = con.prepareStatement("SELECT * FROM Holidays");
            ResultSet holidayDatesSet = getHolidayDatesStatement.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while(holidayDatesSet.next()) {
                holidayDates.add(LocalDate.parse(holidayDatesSet.getString("Date"), formatter));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return holidayDates;
    }
    
    public int getConfirmation(String title, String header, String desc) {
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType[] buttons = {yes, no};
        Alert dialog = new Alert(AlertType.WARNING, desc, buttons);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.get() == no) {
            return 0;
        } else if(result.get() == yes) {
            return 1;
        } else {
            return 2;
        }
    }
    
    // Code from javacodegeeks.com - JavaFX Dialog Example
    private void showError(String title, String desc) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        String s = desc;
        alert.setContentText(s);
        alert.showAndWait();
    }
    
    // Code from javacodegeeks.com - JavaFX Dialog Example
    private void showAlert(String title, String header, String desc) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        String s = desc;
        alert.setContentText(s);
        alert.showAndWait();
    }
}