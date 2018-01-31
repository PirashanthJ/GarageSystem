package common.gui;

import common.Database;
import customers.gui.ViewCustomersController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import parts.GUI.SearchForPartsController;
import diagrep.gui.SearchController;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import specialist.MainPageController;

public class MainFrameController implements Initializable {
    
    private FXMLLoader customersloader;
    private FXMLLoader vehiclesloader;
    private FXMLLoader partsloader;
    private FXMLLoader bookingsloader;
    private FXMLLoader spcloader;
    private FXMLLoader adminpageloader;
    
    @FXML
    private Tab customersTab;
    @FXML
    private Tab partsTab;
    @FXML
    private Tab vehiclesTab;
    @FXML
    private Tab bookingsTab;
    @FXML
    private Tab adminTab;
    @FXML
    private Tab spcTab;
    @FXML
    private Text loggedInUser;
    @FXML
    private TextField hourSelection;
    @FXML
    private TextField minuteSelection;
    @FXML
    private DatePicker dateSelection;
    
    private LocalDateTime currentDateTime;
    
    private Stage mystage;
    @FXML
    private Button logoutButton;
    @FXML
    private Label currentDateLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        customersloader= new FXMLLoader(getClass().getClassLoader().getResource("customers/gui/ViewCustomers.fxml"));
        vehiclesloader = new FXMLLoader(getClass().getClassLoader().getResource("vehicles/gui/ViewVehicle.fxml"));
        bookingsloader = new FXMLLoader(getClass().getClassLoader().getResource("diagrep/gui/SearchBookingsFXML.fxml"));
        partsloader = new FXMLLoader(getClass().getClassLoader().getResource("parts/GUI/searchForParts.fxml"));
        spcloader = new FXMLLoader(getClass().getClassLoader().getResource("specialist/MainPage.fxml"));
        adminpageloader = new FXMLLoader(getClass().getClassLoader().getResource("common/gui/AdminPage.fxml"));
        
        try {
            customersTab.setContent(customersloader.load());
            vehiclesTab.setContent(vehiclesloader.load());
            bookingsTab.setContent(bookingsloader.load());
            partsTab.setContent(partsloader.load());
            spcTab.setContent(spcloader.load());
            adminTab.setContent(adminpageloader.load());
            MainPageController controller= spcloader.<MainPageController>getController();// get the controller and pass the tab to setContent with new fxml file
            controller.setParentTab(spcTab);
            SearchForPartsController partController = (SearchForPartsController) partsloader.getController();
            partController.setTabs(partsTab, bookingsTab);
            SearchController bookingController = (SearchController) bookingsloader.getController();
            bookingController.setTabs(bookingsTab, partsTab);
            ViewCustomersController customerController = (ViewCustomersController) customersloader.getController();
            customerController.setTabs(vehiclesTab, bookingsTab, partsTab, spcTab);
        } catch (IOException ex) {
            Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Code by "Evan Knowles" on StackOverflow - What is the recommended way to make a numeric TextField in JavaFX?
        hourSelection.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                hourSelection.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        minuteSelection.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                minuteSelection.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            Statement statement = con.createStatement();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String currentDateTimeStr;
            
            String checkIfDateExists = "SELECT CurrentDate FROM CurrentDate";
            ResultSet date = statement.executeQuery(checkIfDateExists);
            
            if(date.isClosed()) {
                // Set Date and Time for first use
                currentDateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String insertDateQuery = "INSERT INTO CurrentDate (CurrentDate) VALUES ('"+currentDateTimeStr+"')";
                statement.executeUpdate(insertDateQuery);
            } else if(!usingNewFormat(date.getString(1))) {
                // For anyone in our group still using the old format (yyyy-MM-dd), will update automatically
                currentDateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String updateDate = "UPDATE CurrentDate SET CurrentDate = '"+currentDateTimeStr+"'";
                statement.executeUpdate(updateDate);
            } else {
                // Retrieve Date and Time from DB
                currentDateTimeStr = date.getString("CurrentDate");
            }
            LocalDateTime dateTime = LocalDateTime.parse(currentDateTimeStr, formatter);
            currentDateTime = dateTime;
            dateSelection.setValue(currentDateTime.toLocalDate());
            hourSelection.setText(String.format("%02d", dateTime.getHour()));
            minuteSelection.setText(String.format("%02d", dateTime.getMinute()));
            currentDateLabel.setText(db.getCurrentDateTime()); 
            
            // Code from Oracle - JavaFX: Working with JavaFX UI Components
            // Example 26-6 Implementing a Day Cell Factory to Disable Some Days
            final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item.isBefore(currentDateTime.toLocalDate())) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #C0C0C0;");
                                }
                            }
                        };
                    }
                };
            //dateSelection.setDayCellFactory(dayCellFactory);
        } catch (SQLException ex) {
            showAlert("Error", "Unable to get Date and Time", ex.getMessage());
            Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setUsername(String user){
        loggedInUser.setText("Logged in as: " + user);
    }
    
    public void isAdmin(boolean isAdmin)
    {
        if (isAdmin)
        {
            adminTab.setDisable(false);
        }else{
            adminTab.setDisable(true);
        }
        MainPageController controller= spcloader.<MainPageController>getController();// get the controller and pass the tab to setContent with new fxml file
        controller.setAdmin(isAdmin);
    }
    
    public void setStage(Stage stage){
        mystage = stage;
    }
    @FXML
    public void logout(ActionEvent Event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
            mystage.close();
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private boolean usingNewFormat(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            format.parse(date);
            return true;
        } catch(ParseException e){
            return false;
        }
    }
    
    @FXML
    public void setDateTime() {
        if(hourSelection.getText().equals("")||minuteSelection.getText().equals("")) {
            showAlert("Error", "Invalid Input", "You must provide a value for hour and minute.");
            return;
        }
        int hour = Integer.parseInt(hourSelection.getText());
        int min = Integer.parseInt(minuteSelection.getText());
        if(hour<0||hour>23) {
            showAlert("Error", "Invalid Hour Format", "Hour must be between 0 and 23");return;
        } else if(min<0||min>59) {
            showAlert("Error", "Invalid Hour Format", "Minute must be between 0 and 59");
        } else {
            try {
                Database db = Database.getInstance();
                Connection con = db.getConnection();
                Statement statement = con.createStatement();
                String newDateTimeStr = dateSelection.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" "+String.format("%02d", hour)+":"+String.format("%02d", min);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime newDateTime = LocalDateTime.parse(newDateTimeStr, formatter);
                int minsDiff = (int) java.time.Duration.between(currentDateTime, newDateTime).toMinutes();
                if(true) {
                    String updateDate = "UPDATE CurrentDate SET CurrentDate = '"+newDateTimeStr+"'";
                    statement.executeUpdate(updateDate);
                    currentDateTime = newDateTime;
                    showAlert("Update Successful", "Date and Time Updated", "The new date and time is:\n\n"+newDateTimeStr);
                    currentDateLabel.setText(db.getCurrentDateTime());
                } else if(minsDiff == 0) {
                    showAlert("No Changes", "Date and Time Unchanged", "The current date and time is:\n\n"+newDateTimeStr);
                } else {
                    showAlert("ERROR", "Date and Time NOT Updated", "The new date and time you selected occurs before the current one. Please only select a future date and time to change too.");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Database Error", "Unable to update Date and Time");
                //Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // Code from javacodegeeks.com - JavaFX Dialog Example
    private void showAlert(String title, String header, String desc) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        String s = desc;
        alert.setContentText(s);
        alert.show();
    }
}
