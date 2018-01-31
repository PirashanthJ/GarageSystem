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
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * get list of outstanding parts
 */
public class OutstandingController implements Initializable 
{
    @FXML
    private Button checkOutstanding;
    @FXML 
    private ListView showOutstanding;
    @FXML
    private ChoiceBox chooseSPC;
    @FXML
    private Button showDetails;
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
        showDetails.setDisable(true);
        
        
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
           showError("Database Error", err.getMessage());
           Logger.getLogger(OutstandingController.class.getName()).log(Level.SEVERE, null, err);
        }
    } 
    @FXML
    public void checkOutstanding()throws SQLException// get list of all part outastanding at all SPC's
    {
        ArrayList<String> store= new ArrayList<String>();
        Statement statement;ResultSet rs;
        statement=connection.createStatement();
        showDetails.setDisable(true);
        showOutstanding.getItems().clear();
        String toCheck="select SPCDetails.SPCName,SPCBooking.SPCBookingID,Part.Name,VehicleInfo.Registration from SPCBooking inner join PartForSPCRepair on SPCBooking.SPCBookingID=PartForSPCRepair.SPCBookingID inner join DiagRepairBooking on SPCBooking.DiagRepBookingID=DiagRepairBooking.DiagRepID inner join VehicleInfo on VehicleInfo.VehicleID= DiagRepairBooking.VehicleID inner join SPCDetails on SPCBooking.SPCID=SPCDetails.SPCID inner join Part on PartForSPCRepair.PartID =Part.ID where SPCBooking.ReturnStatus='0'";
   
        rs= statement.executeQuery(toCheck);
        store.add("SPC Centre\t\t\t\t\t\t\t Booking ID\t\t\t\t\t\tPart name\t\t\t\t\t\tVehicle Registration\n");
   
        while(rs.next())
        {
            store.add(rs.getString("SPCName")+"\t\t\t\t\t\t\t"+rs.getInt("SPCBookingID")+"\t\t\t\t\t\t\t"+rs.getString("Name")+"\t\t\t\t\t\t\t"+rs.getString("Registration")+"\n");
        }
        ObservableList<String> items2 =FXCollections.observableArrayList (store);
        showOutstanding.setItems(items2);
    }
    @FXML
    public void checkCentre()throws SQLException// get list of parts outstanding by chosen SPC centre
    {
        ArrayList<String> store= new ArrayList<String>();
        if(chooseSPC.getSelectionModel().isEmpty()){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-An SPC has been chosen");return;}
        showDetails.setDisable(false);
        Statement statement;ResultSet rs;
        statement=connection.createStatement();
        showOutstanding.getItems().clear();
  
        rs=statement.executeQuery("select SPCID from SPCDetails where SPCName='"+chooseSPC.getSelectionModel().getSelectedItem().toString()+"'");
        int id=rs.getInt("SPCID");

        String toCheck="select SPCBooking.SPCBookingID,Part.ID,Part.Name from SPCBooking inner join PartForSPCRepair on PartForSPCRepair.SPCBookingID=SPCBooking.SPCBookingID inner join Part on Part.ID=PartForSPCRepair.PartID where SPCBooking.SPCID='"+id+"' and SPCBooking.ReturnStatus=0";
   
        rs= statement.executeQuery(toCheck);
        store.add("PartID\t\tBooking ID\t\t\t\t\t\tName\n");
   
        while(rs.next())
        {
                store.add(rs.getInt("ID")+"\t\t\t"+rs.getInt("SPCBookingID")+"\t\t\t\t\t\t\t\t"+rs.getString("Name")+"\n");
        }
        ObservableList<String> items2 =FXCollections.observableArrayList (store);
        showOutstanding.setItems(items2);
    }
    @FXML
    public void showPartDetails(ActionEvent event)
    {
        if(showOutstanding.getSelectionModel().isEmpty()||showOutstanding.getSelectionModel().getSelectedIndex()==0){showError("Missing ", "Please make sure a part has been chosen");return;}
        Object selected=showOutstanding.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        String partID=text.substring(0, index);
        try 
        {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("partDetails.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                PartDetailsController controller= fxmlLoader.<PartDetailsController>getController();
                controller.partDetails(partID);
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
        } 
        catch(Exception e) 
        {
                Logger.getLogger(DeletePartController.class.getName()).log(Level.SEVERE, null, e);
        }
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
            Logger.getLogger(OutstandingController.class.getName()).log(Level.SEVERE, null, ex);
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
        alert =  new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
            
}
