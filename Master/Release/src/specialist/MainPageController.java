/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

import common.Database;
import common.gui.LoginController;
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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import parts.GUI.SearchForPartsController;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 */
public class MainPageController implements Initializable 
{
    Database db= Database.getInstance();
    Connection connection= db.getConnection();
    
    @FXML
    private Button makeBooking;
    @FXML
    private Button centreDetails;
    @FXML
    private Button checkOutstanding;
    @FXML
    private Button searchRepair;
    @FXML
    private Button showAllVehicles;
    @FXML
    private Button deletePart;
    @FXML
    private Button returnedItems;
    private Tab parentTab;
    private boolean isAdmin;
    private FXMLLoader loginLoader;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
       centreDetails.setDisable(true);
    }    
    public void centreDetails(ActionEvent event) throws IOException
    {
        // gets current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("centreDetails.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            CentreDetailsController controller= spcloader.<CentreDetailsController>getController();// get the controller and pass the tab to setContent with new fxml file
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } catch (IOException ex) {
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
           
    }
    public void makeBooking(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("Booking.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            BookingController controller= spcloader.<BookingController>getController();// get the controller and pass the tab to setContent with new fxml file
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    
    }
    public void searchRepair(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("searchRepair.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            SearchRepairController controller= spcloader.<SearchRepairController>getController();// get the controller and pass the tab to setContent with new fxml file
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    public void checkOutsanding(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("outstanding.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            OutstandingController controller= spcloader.<OutstandingController>getController();
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void showAllVehicles(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("allVehicles.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            AllVehiclesController controller= spcloader.<AllVehiclesController>getController();
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        }
        catch (IOException ex) 
        {  
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    public void repairsVehicle(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("repairsVehicle.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            RepairsVehicleController controller= spcloader.<RepairsVehicleController>getController();
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void processRepair(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader= new FXMLLoader(getClass().getResource("processRepair.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            ProcessRepairController controller= spcloader.<ProcessRepairController>getController();
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) {
            
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void deletePart(ActionEvent event) throws IOException
    {

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("deletePart.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            DeletePartController controller= spcloader.<DeletePartController>getController();
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);

        } 
        catch (IOException ex) 
        {
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void returnedItem(ActionEvent event) throws IOException
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("returnedItems.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            ReturnedItemsController controller= spcloader.<ReturnedItemsController>getController();
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    public void setParentTab(Tab tab)
    {
        this.parentTab=tab;
    }
    public void setAdmin(boolean isAdmin)
    {
        this.isAdmin=isAdmin;
        if(isAdmin==false){centreDetails.setDisable(true);}
        else{centreDetails.setDisable(false);}
    }
}
