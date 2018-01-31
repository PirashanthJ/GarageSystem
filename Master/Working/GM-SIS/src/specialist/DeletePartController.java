/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

import common.Database;
import diagrep.logic.*;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * delete part from list of parts still outstanding
 */
public class DeletePartController implements Initializable 
{
    Database db= Database.getInstance();
    Connection connection= db.getConnection();
    
    @FXML
    private ChoiceBox chooseSPC;
    @FXML
    private ListView allParts;
    private Tab parentTab;
    private boolean isAdmin;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        spcNames();
    }   
    @FXML
    private void spcNames()// get names of all SPC's
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
            showError("Database Error", err.getMessage());
            Logger.getLogger(DeletePartController.class.getName()).log(Level.SEVERE, null, err);
        }
        
    }
    @FXML
    private void deletePart()
    {
        if(allParts.getSelectionModel().isEmpty()||allParts.getSelectionModel().getSelectedIndex()==0){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A part been chosen");return;}
        //method to get the spcBooking ID from the list view
        Object selected=allParts.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        int spcBooking=Integer.parseInt(text.substring(0, index));
        //show confirmation to delete part
        Alert alert;
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Deleting part: " +text.substring(index,text.length()).replaceAll("\\s+",""));
        alert.setContentText("Continue deleting?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK){return;}
        
        Statement statement;Statement statement2;
        try
        {
            statement=connection.createStatement();
            statement2=connection.createStatement();
            ResultSet rs=statement2.executeQuery("select SPCBooking.DiagRepBookingID,PartForSPCRepair.PartID from SPCBooking inner join PartForSPCRepair on PartForSPCRepair.SPCBookingID=SPCBooking.SPCBookingID where SPCBooking.SPCBookingID='"+spcBooking+"'");
            statement.executeUpdate("delete from PartForSPCRepair where SPCBookingID='"+spcBooking+"'");
            statement.executeUpdate("delete from SPCBooking where SPCBookingID='"+spcBooking+"'");
            DiagRepair diag= new DiagRepair(rs.getInt("DiagRepBookingID"));
            diag.removePart(rs.getInt("PartID"));
            showAlert("Part deleted", "Deleted", "This part has successfully removed from the repair list");
            checkAllParts();
            
        }
        catch(NullPointerException e)
        {
            showError("Missing Input Fields", "Some Required Details Were Not Entered - Please check the following:\n- Expected Return Date\n- Expected Delivery Date\n- DiagID\n- SPC centre\n- vehicle/part added");

        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(DeletePartController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(InProgressException ex)
        {
                 Logger.getLogger(DeletePartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void checkAllParts()throws SQLException//get list of all parts from chosen SPC
    {
        ArrayList<String> store= new ArrayList<String>();
        Statement statement;ResultSet rs;
        statement=connection.createStatement();
        if(chooseSPC.getSelectionModel().isEmpty()){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-An SPC has been chosen");return;}
        
        rs=statement.executeQuery("select SPCID from SPCDetails where SPCName='"+chooseSPC.getSelectionModel().getSelectedItem().toString()+"'");
        int id=rs.getInt("SPCID");

        allParts.getItems().clear();
        String toCheck="select SPCBooking.SPCBookingID,Part.Name from SPCBooking inner join PartForSPCRepair on SPCBooking.SPCBookingID=PartForSPCRepair.SPCBookingID inner join Part on Part.ID=PartForSPCRepair.PartID where SPCBooking.SPCID='"+id+"' and SPCBooking.ReturnStatus='0' and SPCBooking.IsBookingForVehicle='0'";
   
        rs= statement.executeQuery(toCheck);
        store.add("SPC BookingID\t\t\t\t\tPart name\n");
   
        while(rs.next())
        {
            store.add(rs.getInt("SPCBookingID")+"\t\t\t\t\t\t\t\t"+rs.getString("Name")+"\n");
        }
        ObservableList<String> items2 =FXCollections.observableArrayList (store);
        allParts.setItems(items2);
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
            Logger.getLogger(DeletePartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    public void showPartDetails(ActionEvent event)
    {
        if(allParts.getSelectionModel().isEmpty()||allParts.getSelectionModel().getSelectedIndex()==0){showError("Missing ", "Please make sure a part has been chosen");return;}
        Object selected=allParts.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        int spcBookingID=Integer.parseInt(text.substring(0, index));
        String partID;
        try 
        {
                Statement statement= connection.createStatement();
                ResultSet rs=statement.executeQuery("select PartID from PartForSPCRepair where SPCBookingID="+spcBookingID);
                partID=rs.getString("PartID");
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
    private void showAlert(String title, String header, String content) 
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }
   
    
}
