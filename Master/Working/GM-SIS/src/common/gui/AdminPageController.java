/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.gui;

import common.Database;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author ec15072
 */
public class AdminPageController implements Initializable {
    
    @FXML
    private TableView<SystemUser> systemUserTable;
    @FXML
    private TableColumn<SystemUser, String> userFirstName;
    @FXML
    private TableColumn<SystemUser, String> userLastName;
    @FXML
    private TableColumn<SystemUser, String> userLoginID;
    @FXML
    private TableColumn<SystemUser, Double> userHourlyWage;
    @FXML
    private TableColumn<SystemUser, String> userType;
    @FXML
    private TableColumn<SystemUser, Boolean> isUserMechanic;
    @FXML
    private ChoiceBox searchBy;
    @FXML
    private TextField searchByFirstName;
    @FXML
    private TextField searchByLastName;
    @FXML
    private TextField searchByUserLogin;
    // Search methods
    
    private ObservableList<SystemUser> systemUserObsvList;
    private final String searchQuery = "SELECT userID, Firstname, Surname, Login, HourlyWage, IsAdmin, IsMechanic FROM SystemUser";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<String> searchTypes = new ArrayList<>();
        // Add all search types
        searchTypes.add("User's Name");
        searchTypes.add("5-digit ID");
        ObservableList<String> searchByDropDown = FXCollections.observableArrayList(searchTypes);
        searchBy.setItems(searchByDropDown);
        
        // Code taken from Java2s.com - Add change listener to ComboBox valueProperty
        searchBy.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldVal, String newVal) {
                if(newVal.equals("User's Name")) {
                    searchByUserLogin.setVisible(false);
                    searchByFirstName.setVisible(true);
                    searchByLastName.setVisible(true);
                } else if(newVal.equals("5-digit ID")) {
                    searchByFirstName.setVisible(false);
                    searchByLastName.setVisible(false);
                    searchByUserLogin.setVisible(true);
                }
            }
        });
        searchBy.setValue("User's Name");
        
        // Code by "Evan Knowles" on StackOverflow - What is the recommended way to make a numeric TextField in JavaFX?
        searchByUserLogin.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                searchByUserLogin.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        systemUserTable.setPlaceholder(new Label("Please select a search option from the toolbar above..."));
    }
    
    @FXML
    private void searchByNameOrID() {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            if(searchBy.getValue().equals("User's Name")) { // Search by name (like matches)
                String query = this.searchQuery + " WHERE Firstname LIKE ? AND Surname LIKE ?";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setString(1, "%"+searchByFirstName.getText().trim()+"%");
                statement.setString(2, "%"+searchByLastName.getText().trim()+"%");
                performSearch(statement);
            } else if(searchBy.getValue().equals("5-digit ID")) { // Search by 5-digit ID (like matches)
                String query = this.searchQuery + " WHERE Login LIKE ?";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setString(1, "%"+searchByUserLogin.getText().trim()+"%");
                performSearch(statement);
            }
        } catch (SQLException ex) {
            showAlert("Unable to search system users", "There was an issue searching for users with this filter. Please try another.");
            Logger.getLogger(AdminPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    protected void showActiveUsers() {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            PreparedStatement statement = con.prepareStatement(this.searchQuery);
            performSearch(statement);
        } catch (SQLException ex) {
            showAlert("Unable to search system users", "There was an issue searching for all users.");
            Logger.getLogger(AdminPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void showAllMechanics() {
        try {
            Database db = Database.getInstance();
            Connection con = db.getConnection();
            String query = this.searchQuery + " WHERE IsMechanic = 1";
            PreparedStatement statement = con.prepareStatement(query);
            performSearch(statement);
        } catch (SQLException ex) {
            showAlert("Unable to search mechanics", "There was an issue searching for all users. Please try a different search");
            Logger.getLogger(AdminPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void performSearch(PreparedStatement statement) {
        try {
            ResultSet systemUsers = statement.executeQuery();
            if(systemUsers.isClosed()) {
                systemUserObsvList = FXCollections.observableArrayList();
                systemUserTable.setItems(systemUserObsvList);
                systemUserTable.setPlaceholder(new Label("No users match this query"));
                return;
            }
            ArrayList<SystemUser> systemUsersArr = new ArrayList<>();
            int ID; String firstname; String surname; String login; double wage; boolean isAdmin; boolean mechanic;
            while(systemUsers.next()) {
                ID = systemUsers.getInt("userID");
                firstname = systemUsers.getString("Firstname");
                surname = systemUsers.getString("Surname");
                login = systemUsers.getString("Login");
                wage = systemUsers.getDouble("HourlyWage");
                isAdmin = (systemUsers.getInt("IsAdmin")==1);
                mechanic = (systemUsers.getInt("IsMechanic")==1);
                systemUsersArr.add(new SystemUser(ID, firstname, surname, login, wage, isAdmin, mechanic));
            }
            systemUserObsvList = FXCollections.observableArrayList(systemUsersArr);
            
            userFirstName.setCellValueFactory(new PropertyValueFactory("firstName"));
            userLastName.setCellValueFactory(new PropertyValueFactory("lastName"));
            userLoginID.setCellValueFactory(new PropertyValueFactory("loginID"));
            userHourlyWage.setCellValueFactory(new PropertyValueFactory("wage"));
            userType.setCellValueFactory(new PropertyValueFactory("userType"));
            isUserMechanic.setCellValueFactory(new PropertyValueFactory("isMechanic"));
            isUserMechanic.setCellFactory(column -> new CheckBoxTableCell());
            
            systemUserTable.setItems(systemUserObsvList);
        } catch (SQLException ex) {
            showAlert("Unable to ]perform search", "There was an issue searching for system users. Please try a different search");
            Logger.getLogger(AdminPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    public void addUser(ActionEvent event)
    {
        try 
        {
            Parent root = FXMLLoader.load(getClass().getResource("addUser.fxml")); 
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            showActiveUsers();
        }
        catch (IOException ex) 
        {
            Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void editUser(ActionEvent event) {
        SystemUser selectedUser = systemUserTable.getSelectionModel().getSelectedItem();
        if(selectedUser != null){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("editUser.fxml"));
                Parent root = loader.load(); 
                EditUserController controller = loader.getController();
                controller.initializeViews(selectedUser);
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setScene(scene);
                stage.showAndWait();
                showActiveUsers();
            } catch (IOException ex) {
                Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            showAlert("No User Selected", "Please select a user to edit.");
        }
    }

    @FXML
    private void deleteUser(ActionEvent event) {
        SystemUser selectedUser = systemUserTable.getSelectionModel().getSelectedItem();
        if(selectedUser != null){
            if(selectedUser.isMechanic()){
                if(selectedUser.isMechanicActive()){
                    showAlert("Mechanic on Booking", selectedUser.getFirstName() + " " + selectedUser.getLastName() 
                                + " is currently on an active diagnosis and repair booking or set on a booking in the future.\n" 
                                + "The booking must be completed and the mechanic must be removed from future bookings before the mechanic can be deleted.");
                    return; // can't delete
                }
            }
            // will delete mechanic from all bookings (s)he was ever on
            String deleteUser = "DELETE FROM SystemUser WHERE userID=" + selectedUser.getID();
           
            boolean yes = showYesNo("Confirm Deletion", "Are you sure you wish to delete " + selectedUser.getFirstName() 
                        + " " + selectedUser.getLastName() + " from the system.");
            if(yes){
                    // used to get the completed bookings the mechanic worked on
                    String getCompletedBookingsForUser = "SELECT DiagRepID FROM DiagRepairBooking WHERE MechanicID=" + selectedUser.getID() + " AND Completed=1";
                    Database db = Database.getInstance();
                    Connection conn = db.getConnection();
                    Statement statement = null;
                try {
                    statement = conn.createStatement();
                    // get completed bookings and store their IDs in an arraylist
                    ResultSet rs = statement.executeQuery(getCompletedBookingsForUser);
                    ArrayList<Integer> completedBookingIDs = new ArrayList<>();
                    while(rs.next() && !rs.isClosed()){
                        completedBookingIDs.add(rs.getInt("DiagRepID"));
                    }
                    
                    // set the mechanicID to null for every completed booking the mechanic worked on
                    // this is so bookings module can check and set some text for the mechanic when viewing past bookings
                    for(int i = 0; i < completedBookingIDs.size(); i++){
                        String updateCompletedBooking = "UPDATE DiagRepairBooking SET MechanicID=null WHERE DiagRepID=" + completedBookingIDs.get(i);
                        statement.executeUpdate(updateCompletedBooking);
                    }
                    // finally mechanic(user) is deleted from the system
                    statement.executeUpdate(deleteUser);
                    
                } catch (SQLException ex) {
                    Logger.getLogger(AdminPageController.class.getName()).log(Level.SEVERE, null, ex);
                }finally{
                    closeStatementAndConnection(statement, conn);
                }
            }// do nothing they say no
            showActiveUsers();
        }else{
            showAlert("No User Selected", "Please select a user to delete.");
        }

    }
    
    private static void closeStatementAndConnection(Statement s, Connection c){
        try {
            if(s != null && !s.isClosed()){
                s.close();
            }else if(c != null && !c.isClosed()){
                c.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(EditUserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static boolean showYesNo(String title, String context){
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType[] buttons = {yes, no};
        Alert alert = new Alert(Alert.AlertType.WARNING, context, buttons);
        alert.setHeaderText(title);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yes){
            return true;
        }else{
            return false;
        }
    }
    
    private static void showAlert(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public class SystemUser {
        private final int ID; // Primary key of table
        private String firstName;
        private String lastName;
        private String userLoginID;
        private double wage;
        private String userType;
        private Boolean isAdmin;
        private Boolean isMechanic;
        
        public SystemUser(int ID, String name, String surname, String login, double wage, boolean isAdmin, boolean isMechanic) {
            this.ID = ID;
            this.firstName = name;
            this.lastName = surname;
            this.userLoginID = login;
            this.wage = wage;
            this.userType = (isAdmin)?"Administrator":"Day-to-day";
            this.isAdmin = isAdmin;
            this.isMechanic = isMechanic;
        }
        
        public int getID() { return this.ID; }
        public String getFirstName() { return this.firstName; }
        public String getLastName() { return this.lastName; }
        public String getLoginID() { return this.userLoginID; }
        public double getWage() { return this.wage; }
        public String getUserType() { return this.userType; }
        public boolean isAdmin() { return this.isAdmin; }
        public boolean isMechanic() { return this.isMechanic; }
        public BooleanProperty isMechanicProperty() { return new SimpleBooleanProperty(this.isMechanic); }
        
        public boolean isMechanicActive(){
            boolean active = true;
            Database db = Database.getInstance();
            // get active or future bookings where the mechanic on the booking is the currently selected user
            String checkForBooking = "SELECT * FROM DiagRepairBooking WHERE Completed = 0 AND MechanicID=" + this.ID; 
            Connection conn = db.getConnection();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(checkForBooking);
                // there are no currently active bookings for this mechanic
                // or the selected user isn't a mechanic
                if(rs.isClosed() || !isMechanic){
                    active = false;
                }
            } catch (SQLException ex) {
                Logger.getLogger(EditUserController.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                closeStatementAndConnection(statement, conn);
            }
            return active;
        }
        
    }
}
