/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

import common.Database;
import java.io.IOException;
import static java.lang.Math.round;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 */
public class ProcessRepairController implements Initializable 
{

    @FXML
    private ChoiceBox spcBooking;
    @FXML
    private ListView partToRepair;
    @FXML
    private CheckBox part;
    @FXML
    private CheckBox vehicle;
    private Tab parentTab;
    private boolean isAdmin;
    
    Database db= Database.getInstance();
    Connection connection= db.getConnection();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        part.setDisable(true);vehicle.setDisable(true);
        getID();
    }
    private void getID()// get all SPCBooking ID to choose repair from
    {
        try
        {
            Statement statement;
            statement=connection.createStatement();
            ResultSet rs;
            rs=statement.executeQuery("select SPCBooking.SPCBookingID,VehicleInfo.Registration from SPCBooking inner join DiagRepairBooking on DiagRepairBooking.DiagRepID=SPCBooking.DiagRepBookingID inner join VehicleInfo on VehicleInfo.VehicleID=DiagRepairBooking.VehicleID where ReturnStatus='0'");
            ArrayList<String> store= new ArrayList<>();
            while(rs.next())
            {
                store.add(Integer.toString(rs.getInt("SPCBookingID"))+"\nVehicle Registration: "+rs.getString("Registration"));
            }
            ObservableList<String> items =FXCollections.observableArrayList (store);
            spcBooking.setItems(items);
            
        }
        catch(NullPointerException e)
        {
            showError("Unable to get bookings", "no bookings");
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(ProcessRepairController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
    
    @FXML
    private void showParts()// show list of parts ready for repair
    {
        if(spcBooking.getSelectionModel().isEmpty()){showError("Missing", "Please check the following:\n-An SPC BookingID has been chosen");return;}
        
        ArrayList<String> store= new ArrayList<>();
        Object chosenID=spcBooking.getSelectionModel().getSelectedItem();
        String []arr= chosenID.toString().split("\n");
        int spcBookingID=Integer.parseInt(arr[0]);
      
        try
        {
            Statement statement;
            statement=connection.createStatement();ResultSet rs;
            rs=statement.executeQuery("select Part.ID,PartForSPCRepair.repairStatus,SPCBooking.IsBookingForVehicle,PartInfo.Name,PartInfo.Description from PartInfo,Part,PartForSPCRepair,SPCBooking where PartForSPCRepair.PartID=Part.ID and PartForSPCRepair.SPCBookingID='"+spcBookingID+"' and PartInfo.Name=Part.Name and SPCBooking.SPCBookingID='"+spcBookingID+"' and SPCBooking.ReturnStatus='0'");
            if(rs.getInt("IsBookingForVehicle")==1){vehicle.selectedProperty().set(true);part.selectedProperty().set(false);}else{part.selectedProperty().set(true);vehicle.selectedProperty().set(false);}
            store.add("PartID\t\t\tName\t\t\t\tRepairStatus");
            while(rs.next())
            {
              String returnStatus;
              if(rs.getInt("repairStatus")==0){returnStatus="false";}else{returnStatus="true";}
              store.add(rs.getInt("ID")+"\t\t\t\t"+rs.getString("Name")+"\t\t\t\t"+returnStatus+"\n");//PartForSPCRepair add a completed boolean
            }
            ObservableList<String> items =FXCollections.observableArrayList (store);
            partToRepair.getItems().clear();
            partToRepair.setItems(items);
            
        }
        catch(NullPointerException e)
        {
            showError("Unable to show parts", "not possible to show parts");
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(ProcessRepairController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    private void repairPart()// the selected part will be repaired if not repaired already and if all parts repaired then sent back
    {
        Statement statement;
        if(partToRepair.getSelectionModel().isEmpty()||partToRepair.getSelectionModel().getSelectedIndex()==0){showError("Missing", "Please check the following:\n-A Part has been chosen");return;}
        int confirmed=showConfirm("","Repair part","Are you sure this part been repaired?");
        if(confirmed==0){return;}
        
        Object chosenID=spcBooking.getSelectionModel().getSelectedItem();
        String []arr= chosenID.toString().split("\n");
        int spcBookingID;      
        try{spcBookingID=Integer.parseInt(arr[0]);}catch(NullPointerException e){showError("Cannot retrieve details", "Sorry something went wrong");return;}
        Object selected=partToRepair.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        int partID=Integer.parseInt(text.substring(0, index));
        ResultSet rs;
        try
        {
            DecimalFormat df= new DecimalFormat("0.00");
            statement=connection.createStatement();
            rs=statement.executeQuery("select repairStatus from PartForSPCRepair where PartID='"+partID+"' and SPCBookingID='"+spcBookingID+"'");
            if(rs.getInt("repairStatus")==1){showError("Already Repaired", "The part has already been repaired");return;}
            statement.executeUpdate("update PartForSPCRepair set repairStatus='1' where PartID='"+partID+"' and SPCBookingID='"+spcBookingID+"'");
            partToRepair.getItems().remove(partToRepair.getSelectionModel().getSelectedIndex());
            rs=statement.executeQuery("select repairStatus from PartForSPCRepair where SPCBookingID='"+spcBookingID+"'");
            while(rs.next())
            {
                if(rs.getInt("repairStatus")==0)
                {
                    showAlert("Part repaired", "Part repaired", "This part has successfully been repaired");
                    return;
                }
            }
            //calculate costs of all repairs and insert to database
            double totalCosts=Double.parseDouble(df.format(0.2*calculateRepairCosts(spcBookingID)));
            statement.executeUpdate("update SPCBooking set ReturnStatus='1',CostOfService="+totalCosts+" where SPCBookingID='"+spcBookingID+"'");
            showAlert("Booking completed", "All parts repaired", "Part/Vehicle will be sent back\nTotal repair cost: "+totalCosts);
            showAlert("Part repaired", "Part repaired", "This part has successfully been repaired");
            getID();// updater ID's
        }
        catch(NullPointerException e)
        {
            showError("Unable to repair", "Something went wrong, unable to repair part");

        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(ProcessRepairController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private double calculateRepairCosts(int bookingID)// calcualte total costs of repair of all parts in the booking
    {
        Statement statement;ResultSet rs;
        final double repairPercentage=0.2;// repair percentage of part cost
        double totalCost=0;
        try
        {
            statement=connection.createStatement();
            rs=statement.executeQuery("select PartInfo.Cost from PartInfo inner join Part on Part.Name=PartInfo.Name inner join PartForSPCRepair on PartForSPCRepair.PartID=Part.ID where PartForSPCRepair.SPCBookingID="+bookingID);
            while(rs.next())
            {
                totalCost=totalCost+ rs.getDouble("Cost");
            }
            return totalCost;
        }
        catch(NullPointerException e)
        {
            showError("Unable to repair", "Something went wrong, unable to repair part");
            return 0;
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
            return 0;
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(ProcessRepairController.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
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
            Logger.getLogger(ProcessRepairController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    public void showPartDetails(ActionEvent event)
    {
        if(partToRepair.getSelectionModel().isEmpty()||partToRepair.getSelectionModel().getSelectedIndex()==0){showError("Missing ", "Please make sure a part has been chosen");return;}
        Object selected=partToRepair.getSelectionModel().getSelectedItem();
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
                partToRepair.getSelectionModel().clearSelection();
        } 
        catch(Exception e) 
        {
                Logger.getLogger(ProcessRepairController.class.getName()).log(Level.SEVERE, null, e);
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
        alert= new Alert(Alert.AlertType.ERROR);
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


