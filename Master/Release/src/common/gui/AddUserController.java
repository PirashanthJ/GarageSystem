/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.gui;

import common.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
 * @author Pirashanth
 */
public class AddUserController implements Initializable 
{
    Database db=Database.getInstance();
    Connection connection=db.getConnection();
    /**
     * add a user to the system with a unique 5-digit login
     */
    @FXML
    private TextField fName;
    @FXML
    private TextField sName;
    @FXML
    private TextField uName;
    @FXML
    private Label mechanic;
    @FXML 
    private Label admin;
    @FXML
    private Label wage;
    @FXML
    private TextField password;
    @FXML
    private TextField hourlyWage;
    @FXML
    private CheckBox isAdmin;
    @FXML
    private CheckBox isMechanic;
    @FXML
    private Label wageHelpText;
    @FXML
    private Label loginHelpText;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
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
        hourlyWage.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression for decimal with precision of 2
                if (!newValue.matches("(\\d*)|(\\d+\\.\\d{0,2})")) {
                    wageHelpText.setVisible(true);
                    fadeInThenOutWageHelp.play();
                    hourlyWage.setText(oldValue);
                }
            }
        });
        uName.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,5}")) {
                    if(!newValue.equals("")){
                        loginHelpText.setVisible(true);
                        fadeInThenOutLoginHelp.play();
                    }
                    uName.setText(oldValue);
                }
            }
        });
       hourlyWage.setVisible(false);
       wage.setVisible(false);
       
    }
    @FXML
    private void hidehourlyWage()
    { 
        if(isMechanic.isSelected())
        {
            hourlyWage.setVisible(true);
            wage.setVisible(true);
        }
        else
        {
           hourlyWage.setVisible(false);
           wage.setVisible(false);
        }
    }
    @FXML
    private void addUser(ActionEvent event)
    {
        if(fName.getText().isEmpty()||sName.getText().isEmpty()||uName.getText().isEmpty()||password.getText().isEmpty()||isMechanic.isSelected()&&hourlyWage.getText().isEmpty())
        {
            showError("Missing fields","Please check all the fields have been entered");
            return;
        }
        int confirmed=showConfirm("","Add user","Continue adding user?");
        if(confirmed==0){return;}
        try
        {
            DecimalFormat df= new DecimalFormat("0.00");
            String userName=uName.getText().trim();
            int isAdmin=0;
            int isMechanic=0;
            double hourlyWage=0;
            if(String.valueOf(uName.getText()).length()!=5){showError("Login error","Please make sure you have entered a 5-digit login");return;}
            if(this.isAdmin.selectedProperty().getValue()==true){isAdmin=1;}
            if(this.isMechanic.selectedProperty().getValue()==true){isMechanic=1;hourlyWage=Double.parseDouble(this.hourlyWage.getText());hourlyWage=Double.parseDouble(df.format(hourlyWage));}
            Statement statement;
            statement=connection.createStatement();
            ResultSet rs=statement.executeQuery("select Login from SystemUser");
            while(rs.next())
            {
                if(rs.getString("Login").equals(uName.getText().trim()))
                {
                    showError("Login already exists","Please enter another login");
                    return;
                }
            }
            String insertQuery = "INSERT INTO SystemUser('Firstname','Surname','Login','Password','HourlyWage','IsAdmin','IsMechanic') VALUES (?,?,?,?,?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setString(1, fName.getText().trim());
            insertStatement.setString(2, sName.getText().trim());
            insertStatement.setString(3, userName);
            insertStatement.setString(4, password.getText());
            insertStatement.setDouble(5, hourlyWage);
            insertStatement.setInt(6, isAdmin);
            insertStatement.setInt(7, isMechanic);
            insertStatement.executeUpdate();
            
            showAlert("","Successful","User has been added to the system");
            
            // gets the popup window and closes it once part added
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            fName.clear();sName.clear();password.clear();this.hourlyWage.clear();this.isAdmin.setSelected(false);uName.clear();this.isMechanic.setSelected(false);
                                   
        }
        catch(SQLException e)
        {
            showError("Check login","Please make sure the login is a 5 digit number");
            Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, e);
        }
        catch(NumberFormatException e)
        {
            showError("Login error","Please make sure:\n You have entered a 5-digit login\nYou have entered a numerical hourly wage");
            Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, e);
        }
       
    }
    private void showError(String title, String content) 
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String header, String content) 
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }
    private int showConfirm(String title,String header,String content)
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){return 1;}
        else{ return 0;}
    }
}
