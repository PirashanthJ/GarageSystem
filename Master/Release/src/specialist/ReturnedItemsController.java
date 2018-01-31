/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

import common.Database;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * show list of all returned items by which we mean all completed and repaired parts
 */
public class ReturnedItemsController implements Initializable {
    @FXML
    private Button checkReturnedItems;
    @FXML 
    private ListView showReturned;
    @FXML
    private ChoiceBox chooseSPC;
    private Tab parentTab;
    private boolean isAdmin;
    
    Database db= Database.getInstance();
    Connection connection= db.getConnection();
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        ArrayList<String> store= new ArrayList<String>();
        String insertInto= "select * from SPCDetails";
        try
        {
            Statement statement;
            statement= connection.createStatement();
            ResultSet rs= statement.executeQuery(insertInto);
            ListView<String> list = new ListView<String>();
            
            while(rs.next())
            {
                store.add(rs.getString("SPCName"));
                
            }
            ObservableList<String> items =FXCollections.observableArrayList (store);
            chooseSPC.setItems(items);
           
        }
        
        catch(SQLException err)
        {
            err.getCause().printStackTrace();
            throw new IllegalArgumentException("Cannot show SPC details");
        }
    } 
    @FXML
    public void checkReturned()throws SQLException// show list of all returned items, completed bookings and parts
    {
        ArrayList<String> store= new ArrayList<String>();
        Statement statement;ResultSet rs;
        statement=connection.createStatement();
        if(chooseSPC.getSelectionModel().getSelectedItem()==null){showError("Missing", "Please make sure an SPC has been chosen");return;}
        showReturned.getItems().clear();
        String toCheck="select SPCBooking.SPCBookingID,Part.Name from SPCBooking inner join PartForSPCRepair on SPCBooking.SPCBookingID=PartForSPCRepair.SPCBookingID inner join Part on Part.ID=PartForSPCRepair.PartID inner join SPCDetails on SPCBooking.SPCID=SPCDetails.SPCID where SPCBooking.ReturnStatus='1' and SPCDetails.SPCName='"+chooseSPC.getSelectionModel().getSelectedItem().toString()+"'";
   
        rs= statement.executeQuery(toCheck);
        store.add("Booking ID\t\t\t\t\tPart name\n");
   
        while(rs.next())
        {
            store.add(rs.getInt("SPCBookingID")+"\t\t\t\t\t\t\t"+rs.getString("Name")+"\n");
        }
        ObservableList<String> items2 =FXCollections.observableArrayList (store);
        showReturned.setItems(items2);
    }
    @FXML
    private void mainPage(ActionEvent event) throws IOException 
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("MainPage.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            MainPageController controller= spcloader.<MainPageController>getController();// get the controller and pass the tab to setContent with new fxml file
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ReturnedItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void setParentTab(Tab tab)
    {
        this.parentTab=tab;
    }
    public void setAdmin(boolean admin)
    {
        isAdmin=admin;
    }
    
    private void showError(String title, String content) 
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
            
}
