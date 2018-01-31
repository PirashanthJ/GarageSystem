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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * to add, edit or delete parts from a booking
 */
public class RepairsVehicleController implements Initializable 
{
    Database db= Database.getInstance();
    Connection connection= db.getConnection();
    LocalDate currentDate;
    private int spcID;
    private int VehicleID;
    
    @FXML
    private ChoiceBox vehicleReg;
    @FXML
    private ListView showRepairs;
    @FXML
    private Button changeRepair;
    @FXML
    private ListView existingParts;
    @FXML
    private ListView newParts;
    @FXML
    private TitledPane vehicle;
    @FXML
    private TitledPane changes;
    @FXML
    private Button Edit;
    @FXML
    private Button EnterEdit;
    @FXML
    private TextField installDate;
    @FXML
    private DatePicker pickDate;
    private Tab parentTab;
    private boolean isAdmin;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) // get all registration from vehicle where a vehicle has been sent to repair
    {
        changes.setCollapsible(false);
        vehicle.setCollapsible(true);
        changeRepair.setDisable(true);
        pickDate.setDisable(true);
        showRepairs.setEditable(false);showRepairs.setDisable(true);
        ArrayList<String> store= new ArrayList<>();
        Statement statement;
        try
        {
            statement=connection.createStatement();
            ResultSet rs= statement.executeQuery("select distinct VehicleInfo.Registration from VehicleInfo inner join DiagRepairBooking on VehicleInfo.VehicleID=DiagRepairBooking.VehicleID inner join SPCBooking on SPCBooking.DiagRepBookingID=DiagRepairBooking.DiagRepID where DiagRepairBooking.sendVehicleToSPC='1'");
            while(rs.next())
            {
                store.add(rs.getString("Registration"));
            }
            ObservableList<String> items= FXCollections.observableArrayList(store);
            vehicleReg.setItems(items);
            
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date date = format.parse(db.getCurrentDate());
            currentDate= LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(date));
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
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(ParseException e)
        {
            showError("Date error", e.getMessage());
        }
        
    } 
    @FXML
    private void getRepairs()// get a list of all bookings made for the vehicle
    {
        if(vehicleReg.getSelectionModel().isEmpty()){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A Vehicle Registration has been chosen");return;}
        showRepairs.setDisable(false);
        existingParts.getItems().clear();
        newParts.getItems().clear();
        pickDate.setDisable(true);
        pickDate.getEditor().clear();
        pickDate.setValue(null);

        ArrayList<String> store= new ArrayList<>();
        String registration=vehicleReg.getSelectionModel().getSelectedItem().toString();
        Statement statement;
        try
        {
            statement=connection.createStatement();
            ResultSet rs= statement.executeQuery("select SPCDetails.SPCName,SPCBooking.SPCBookingID, DiagRepairBooking.DiagRepID, VehicleInfo.Registration, SPCBooking.ReturnStatus from SPCBooking inner join DiagRepairBooking on SPCBooking.DiagRepBookingID= DiagRepairBooking.DiagRepID inner join VehicleInfo on VehicleInfo.VehicleID=DiagRepairBooking.VehicleID inner join SPCDetails on SPCBooking.SPCID=SPCDetails.SPCID and VehicleInfo.Registration='"+registration+"' and DiagRepairBooking.sendVehicleToSPC='1'");
            store.add("SPC BookingID\tSPCName\t\t\t\t DiagRepID\t\tCompleted");
            while(rs.next())
            {
                store.add(rs.getInt("SPCBookingID")+"\t\t\t\t"+rs.getString("SPCName")+"\t\t\t"+rs.getInt("DiagRepID")+"\t\t\t"+rs.getInt("ReturnStatus"));
            }
            ObservableList<String> items= FXCollections.observableArrayList(store);
            showRepairs.setItems(items);
            changeRepair.setDisable(false);
        }
        catch(NullPointerException e)
        {
            showError("Unable to retrieve repairs", "Repairs unavailable");
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML 
    private void showParts()// show all parts associated with the chosen booking for the chosen vehicle
    {
        if(showRepairs.getSelectionModel().isEmpty()||showRepairs.getSelectionModel().getSelectedIndex()==0){showError("Missing", "Some Required Details Were Not Entered\n-Please check the following:\n-A repair has been chosen");return;}
        Object selected=showRepairs.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        int spcBooking=Integer.parseInt(text.substring(0, index));
        spcID=spcBooking;
        //check if the booking is completed or not
        int checkProgress=inProgress(spcID);
        if(checkProgress==0){showAlert("Booking in progress","The booking is in progress","Please wait for completion of the repair");return;}
        
        changes.setCollapsible(true);
        vehicle.setExpanded(false);
        changes.setExpanded(true);
        existingParts.getItems().clear();
        ArrayList<String> store= new ArrayList<>();
        Statement statement;
        try
        {
            statement=connection.createStatement();
            String query="select distinct PartInfo.Name,PartInfo.Description,Part.ID,Part.InstallDate from PartForSPCRepair inner join Part on Part.ID=PartForSPCRepair.PartID inner join PartInfo on PartInfo.Name=Part.Name where PartForSPCRepair.SPCBookingID='"+spcBooking+"'";
            ResultSet rs= statement.executeQuery(query);
            store.add("PartID\tName");
            while(rs.next())
            {
                store.add(rs.getInt("ID")+"\t\t"+rs.getString("Name"));
            }
            ObservableList<String> items= FXCollections.observableArrayList(store);
            existingParts.setItems(items);
            showAllParts();
        }
        catch(NullPointerException e)
        {
            showError("Unable to show parts", "Please check the following:\n-A repair has been chosen");
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    private void deletePart()// delete a part from list of repair parts
    {
        if(existingParts.getSelectionModel().isEmpty()||existingParts.getSelectionModel().getSelectedIndex()==0){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A part been chosen");return;}
        int confirmed=showConfirm("","Delete part","Do you want to delete this part");
        if(confirmed==0){return;}
        
        Object selected=existingParts.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        int partID=Integer.parseInt(text.substring(0, index));
        int spcBooking= spcID;
        Statement statement;Statement statement2;Statement statement3;
        ResultSet rs;ResultSet rs2;
        try
        {
            DecimalFormat df= new DecimalFormat("0.00");
            statement=connection.createStatement();
            statement2=connection.createStatement();
            statement3=connection.createStatement();
            rs=statement.executeQuery("select PartInfo.Cost from PartInfo inner join Part on Part.Name=PartInfo.Name where Part.ID="+partID);
            rs2=statement2.executeQuery("select CostOfService from SPCBooking where SPCBookingID="+spcBooking);
            statement3.executeUpdate("Update SPCBooking set CostOfService="+Double.parseDouble(df.format(rs2.getDouble("CostOfService")-(rs.getDouble("Cost")*0.2)))+" where SPCBookingID="+spcBooking);
            statement3.executeUpdate("delete from PartForSPCRepair where SPCBookingID='"+spcBooking+"' and PartID='"+partID+"'");
            showAlert("Deleting Part", "Part Deleted", "This part has successfully removed from the repair list");
            showParts();
        }
        catch(NullPointerException e)
        {
            showError("Unable to delete part", "Please check the following:\n-A part has been chosen");
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
    private void showAllParts()// shows all the parts in the Vehicle exlcuding parts already in for repair
    {
        ArrayList<String> store= new ArrayList<>();
        Statement statement;
        try
        {
            statement=connection.createStatement();
            //ResultSet rs= statement.executeQuery("select distinct PartInVehicle.InstallDate,Part.ID,PartInfo.Name,PartInfo.Description from PartInfo,Part,PartInVehicle,Vehicle where Part.Name=PartInfo.Name and Vehicle.VehicleID=PartInVehicle.VehicleID and Part.ID=PartInVehicle.PartID and Vehicle.Registration='"+vehicleReg.getSelectionModel().getSelectedItem().toString()+"' and Part.ID not in(select PartForSPCRepair.PartID from PartForSPCRepair inner join SPCBooking on PartForSPCRepair.SPCBookingID=SPCBooking.SPCBookingID where SPCBooking.ReturnStatus='0')");
            ResultSet rs=statement.executeQuery("select Part.InstallDate,Part.ID,PartInfo.Name,PartInfo.Description from PartInfo inner join Part on PartInfo.Name=Part.Name inner join PartInVehicle on Part.ID=PartInVehicle.PartID inner join VehicleInfo on PartInVehicle.VehicleID=VehicleInfo.VehicleID where VehicleInfo.Registration='"+vehicleReg.getSelectionModel().getSelectedItem().toString()+"' and Part.ID not in (select PartForSPCRepair.PartID from PartForSPCRepair inner join SPCBooking on SPCBooking.SPCBookingID=PartForSPCRepair.SPCBookingID where SPCBooking.SPCBookingID="+spcID+")");
            store.add("PartID\tName");
            while(rs.next())
            {
                store.add(rs.getInt("ID")+"\t\t"+rs.getString("Name"));
            }
            ObservableList<String> items= FXCollections.observableArrayList(store);
            newParts.setItems(items);
        }
        catch(NullPointerException e)
        {
            showError("Unable to show all parts", "Cannot retrieve list of all parts");
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    @FXML
    private void addPart()// add part to the booking
    {
        if(newParts.getSelectionModel().isEmpty()||newParts.getSelectionModel().getSelectedIndex()==0){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A part been chosen");return;}
        int confirmed=showConfirm("","Add part","Do you want to add this part?");
        if(confirmed==0){return;}
        Statement statement;Statement statement2;Statement statement3;ResultSet rs;ResultSet rs2;
        ArrayList<String> store= new ArrayList<>();
        Object selected=newParts.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        int partID=Integer.parseInt(text.substring(0, index));
        DecimalFormat df= new DecimalFormat("0.00");
        
        try
        {
            statement=connection.createStatement();
            statement2=connection.createStatement();
            statement3=connection.createStatement();
            rs=statement.executeQuery("select PartInfo.Cost from PartInfo inner join Part on Part.Name=PartInfo.Name where Part.ID="+partID);
            rs2=statement2.executeQuery("select CostOfService from SPCBooking where SPCBookingID="+spcID);
            statement3.executeUpdate("Update SPCBooking set CostOfService="+Double.parseDouble(df.format(rs2.getDouble("CostOfService")+(rs.getDouble("Cost")*0.2)))+" where SPCBookingID="+spcID);
            statement.executeUpdate("insert into PartForSPCRepair values('"+partID+"','"+spcID+"','1')");
            showAlert("Adding part", "Part Added", "This part has been successfully added to the repair list");
            showParts();
        }
        catch(NullPointerException e)
        {
            showError("Unable to add part", "Please check the following:\n-Part has been chosen");
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
    private void editInstallDate()// edit the install date for a part
    {
       if(existingParts.getSelectionModel().isEmpty()||existingParts.getSelectionModel().getSelectedIndex()==0){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A part been chosen");return;}
       try
       {
           pickDate.setDisable(false);
           Object selected=existingParts.getSelectionModel().getSelectedItem();
           String text= selected.toString();
           int index= text.indexOf("\t");
           int partID=Integer.parseInt(text.substring(0, index));
           Statement statement;
           statement=connection.createStatement();
           ResultSet rs;
           rs= statement.executeQuery("select distinct Part.InstallDate from Part,PartInVehicle,VehicleInfo where VehicleInfo.Registration='"+vehicleReg.getSelectionModel().getSelectedItem().toString()+"' and VehicleInfo.VehicleID=PartInVehicle.VehicleID and PartInVehicle.PartID='"+partID+"' and Part.ID=PartInVehicle.PartID");

           if(rs.isClosed()){showError("Part unavailable","This part is no longer installed in the vehicle");return;}
           DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
           LocalDate partInstalledDate=LocalDate.parse(rs.getString("InstallDate"),dateTimeFormatter);
           pickDate.setValue(partInstalledDate);
       }
       catch(NullPointerException e)
       {
            showError("Unable to edit Install date", "Please check the following:\n- A part has been chosen");
       } 
       catch (IllegalArgumentException e) 
       {
            showError("Validation Error", e.getMessage());
       } 
       catch (SQLException ex) 
       {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
       }
      
    }
    @FXML
    private void enterInstallDate()// enter the install date to update in database
    {
       if(pickDate.getValue()==null||pickDate.getValue().isAfter(currentDate)){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A valid date has been chosen and is not in the future");return;}
       int confirmed=showConfirm("","Install Date","Confirm Install date as: "+pickDate.getValue()+" ?");
       if(confirmed==0){return;}
       try
       {
           Object selected=existingParts.getSelectionModel().getSelectedItem();
           String text= selected.toString();
           int index= text.indexOf("\t");
           int partID=Integer.parseInt(text.substring(0, index));
           
           Statement statement=connection.createStatement();
           DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
           java.util.Date returnTemp= format.parse(pickDate.getValue().toString());
           LocalDate partInstalledDate= LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(returnTemp));
           statement.executeUpdate("update Part set InstallDate='"+partInstalledDate+"' where ID='"+partID+"'");
           
           showAlert("Changing Install date", "Date changed", "The Install date has been successfully updated");
           showParts();
           pickDate.setDisable(true);
           pickDate.getEditor().clear();
           pickDate.setValue(null);
 
       }
       catch(NullPointerException e)
       {
            showError("Unable to change install date", "Please check the following:\nDate has been chosen");
       } 
       catch (IllegalArgumentException e) 
       {
            showError("Validation Error", e.getMessage());
       } 
       catch (SQLException ex) 
       {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
       }  
       catch(ParseException e)
       {
           showError("Date format error", "Some Required Details Were Not Entered - Please check the following:\nDate entered correctly");
       }
    }
    public int inProgress(int BookingID)
    {
       try
       {
           Statement statement;
           statement=connection.createStatement();
           ResultSet rs;
           rs= statement.executeQuery("select ReturnStatus from SPCBooking where SPCBookingID="+BookingID);
           return rs.getInt("ReturnStatus");
       }
       catch(NullPointerException e)
       {
            showError("Unable to edit Install date", "Please check the following:\n-A part has been chosen");
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
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
       }
    }
    @FXML
    private void cancelEdit()// cancel the edit install date
    {
        installDate.clear();
        installDate.setEditable(false);
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
            Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    public void showVehicleDetails(ActionEvent event)
    {
        if(vehicleReg.getSelectionModel().isEmpty()){showError("Missing ", "Please make sure a vehicle has been chosen");return;}
        String registration=vehicleReg.getSelectionModel().getSelectedItem().toString();
        try 
        {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("vehicleDetails.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                VehicleDetailsController controller= fxmlLoader.<VehicleDetailsController>getController();
                controller.vehicleDetails(registration);
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
        } 
        catch(Exception e) 
        {
                Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    @FXML
    public void showPartDetails(ActionEvent event)
    {
        if(existingParts.getSelectionModel().isEmpty()||existingParts.getSelectionModel().getSelectedIndex()==0){showError("Missing ", "Please make sure a part has been chosen");return;}
        Object selected=existingParts.getSelectionModel().getSelectedItem();
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
                existingParts.getSelectionModel().clearSelection();
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
