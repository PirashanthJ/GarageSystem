/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.gui;

import common.Database;
import common.gui.AdminPageController.SystemUser;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class EditUserController implements Initializable {

    @FXML
    private TextField firstName;
    @FXML
    private TextField surname;
    @FXML
    private TextField login;
    @FXML
    private TextField password;
    @FXML
    private CheckBox admin;
    @FXML
    private CheckBox mechanic;
    @FXML
    private Label wageTitle;
    @FXML
    private TextField wageText;
    private SystemUser user;
    private final int NUMBERS_IN_USER_ID = 5;
    @FXML
    private Label wageHelpText;
    @FXML
    private Label loginHelpText;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        FadeTransition fadeInWage = new FadeTransition(Duration.millis(3000), wageHelpText);
        fadeInWage.setFromValue(0.0);
        fadeInWage.setToValue(1.0);
        FadeTransition fadeOutWage = new FadeTransition(Duration.millis(3000), wageHelpText);
        fadeOutWage.setFromValue(1.0);
        fadeOutWage.setToValue(0.0);
        SequentialTransition fadeInThenOutWageHelp = new SequentialTransition(wageHelpText, fadeInWage, fadeOutWage);
        
        FadeTransition fadeInLogin = new FadeTransition(Duration.millis(3000), loginHelpText);
        fadeInLogin.setFromValue(0.0);
        fadeInLogin.setToValue(1.0);
        FadeTransition fadeOutLogin = new FadeTransition(Duration.millis(3000), loginHelpText);
        fadeOutLogin.setFromValue(1.0);
        fadeOutLogin.setToValue(0.0);
        SequentialTransition fadeInThenOutLoginHelp = new SequentialTransition(loginHelpText, fadeInLogin, fadeOutLogin);
        // Code by Evan Knowles (StackOverflow) What is the recommended way to make a numeric TextField in JavaFX?
        // force the field to only allow decimals
        wageText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression for decimal with precision of 2
                if (!newValue.matches("(\\d*)|(\\d+\\.\\d{0,2})")) {
                    wageHelpText.setVisible(true);
                    fadeInThenOutWageHelp.play();
                    wageText.setText(oldValue);
                }
            }
        });
        login.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,5}")) {
                    if(!newValue.equals("")){
                        loginHelpText.setVisible(true);
                        fadeInThenOutLoginHelp.play();
                    }
                    login.setText(oldValue);
                }
            }
        });
        mechanic.selectedProperty().addListener(new ChangeListener<Boolean>() {
        public void changed(ObservableValue<? extends Boolean> ov,
            Boolean old_val, Boolean new_val) {
                if(new_val){ // mechanic now selected
                    wageText.setVisible(true);
                    wageText.setText(String.valueOf(user.getWage()));
                    wageTitle.setVisible(true);
                }else{
                    wageText.setVisible(false);
                    wageTitle.setVisible(false);
                }
            }
        });
    }    
    
    public void initializeViews(SystemUser user){
        this.user = user;
        firstName.setText(user.getFirstName());
        surname.setText(user.getLastName());
        login.setText(user.getLoginID());
        if(user.isAdmin()){
            admin.setSelected(true);
        }else{
            admin.setSelected(false);
        }
        if(user.isMechanic()){
            mechanic.setSelected(true);
            wageTitle.setVisible(true);
            wageText.setVisible(true);
            wageText.setText(String.valueOf(user.getWage()));
        }else{
            mechanic.setSelected(false);
            wageTitle.setVisible(false);
            wageText.setVisible(false);
        }
    }

    @FXML
    private void updateUser(ActionEvent event) {

        Database db = Database.getInstance();
        boolean mechanicSelected = false;
        Connection conn = db.getConnection();
        Statement statement = null;
        try {
            if(login.getText().length() != NUMBERS_IN_USER_ID){
                showAlert("Improper Length", "Please insert a 5-digit number for the login.");
                return;
            }
            if(!checkLoginUnique(login.getText())){
                showAlert("Login Not Unique", "The login you have entered is not unique.");
                return;
            }
            
            if(mechanic.isSelected()){
                    mechanicSelected = true;
                    updateWage();
            }else{
                /* mechanic is not selected, must check if user was previously mechanic.
                 * If user was previously mechanic moving to not mechanic, must check if it is
                 * on a currently active booking or future booking. Cannot edit if they are (or delete).
                 */
                if(user.isMechanic()){// trying to move from mechanic to not mechanic
                    if(user.isMechanicActive()){ // active so you can not change
                        showAlert("Mechanic on Booking", user.getFirstName() + " " + user.getLastName() 
                                + " is currently on an active diagnosis and repair booking or set on a booking in the future.\n" 
                                + "The booking must be completed or the mechanic must be removed from future bookings before the mechanic's status can be changed.");
                        mechanic.setSelected(true);
                        wageText.setVisible(true);
                        wageTitle.setVisible(true);
                        wageText.setText(String.valueOf(user.getWage()));
                        return; // cant continue
                    }
                }
            }
            statement = conn.createStatement();
            if(admin.isSelected()){
                String updateAdmin = "UPDATE SystemUser Set IsAdmin=1 WHERE userID=" + user.getID();
                statement.executeUpdate(updateAdmin);
            }else{
                if(user.isAdmin()){
                    // the admin is moving from admin to not admin so we must check that there is at least one other admin
                    String checkOtherAdmins = "SELECT count(*) AS numberOfAdmins FROM SystemUser WHERE IsAdmin=1";
                    ResultSet rs = statement.executeQuery(checkOtherAdmins);
                    if(rs.getInt("numberOfAdmins") > 1){// greater than 1 because at least this admin will be a result
                        String updateAdmin = "UPDATE SystemUser Set IsAdmin=0 WHERE userID=" + user.getID();
                        statement.executeUpdate(updateAdmin);
                    }else{
                        showAlert("Only Admin in System", user.getFirstName() + " " + user.getLastName() 
                                + " is the only admin in the system. " 
                                + "Please add at least one admin before changing the status of this admin or the system will be left permanently without one.\n");
                        admin.setSelected(true);
                        return;
                    }
                }
            }
            
            String update = "UPDATE SystemUser SET Firstname='" + allowApostrophes(firstName.getText()) + "', Surname='" + allowApostrophes(surname.getText()) + "', Login='" + 
                    login.getText() + "' WHERE userID=" + user.getID();

            
            statement.executeUpdate(update);
            
            
            if(mechanicSelected){
                String updateAdmin = "UPDATE SystemUser Set IsMechanic=1 WHERE userID=" + user.getID();
                statement.executeUpdate(updateAdmin);
            }else{
                String updateAdmin = "UPDATE SystemUser Set IsMechanic=0 WHERE userID=" + user.getID();
                statement.executeUpdate(updateAdmin);
            }

            // if password is not empty they edited it and changed the password
            if(!password.getText().equals("")){
                updatePassword();
            }
            showAlert("Updated User", "The user " + firstName.getText() + " " + surname.getText() 
                    + " has been successfully updated.");
            // gets the popup window and closes it once part added
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (SQLException ex) {
            Logger.getLogger(EditUserController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{

        }   
    }
    
    private boolean checkLoginUnique(String login){
        boolean unique = true;
        String checkLogin = "SELECT Login, userID FROM SystemUser WHERE Login='" + login + "'";
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(checkLogin);
            // there is a user with this login so it is potentally not unique
            if(rs.next()){
                // if the login does not match the login for the current user it is not unique 
                if(rs.getInt("userID") != user.getID()){
                    unique = false;
                } // if the login is for the current user, it is unique
            }
        } catch (SQLException ex) {
            Logger.getLogger(EditUserController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            closeStatementAndConnection(statement, conn);
        }
        return unique;
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
    
    private static String allowApostrophes(String stringToInsert){
        return stringToInsert.replace("'", "''");
    }
    
    private static void showAlert(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void updatePassword() throws SQLException {
        String updatePassword = "UPDATE SystemUser SET Password=? WHERE userID=?";
        PreparedStatement ps = null;
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        try {
            ps = conn.prepareStatement(updatePassword);
            ps.setString(1, password.getText());
            ps.setInt(2, user.getID());
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(EditUserController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(ps != null && !ps.isClosed()){
                ps.close();
            }
            if(conn != null && !conn.isClosed()){
                ps.close();
            }
        }
    }

    private void updateWage() {
        String updateWage = "UPDATE SystemUser Set HourlyWage= " + wageText.getText() + " WHERE userID=" + user.getID();
        Statement statement = null;
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        try {
            statement = conn.createStatement();
            statement.executeUpdate(updateWage);
        } catch (SQLException ex) {
            Logger.getLogger(EditUserController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            closeStatementAndConnection(statement, conn);
        }
    }
}
