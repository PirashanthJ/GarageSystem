
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
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * shows a list of all the vehicles sent to the SPC's, which includes completed and uncompleted bookings
 */
public class AllVehiclesController implements Initializable 
{
    @FXML
    Button allVehicles;
    @FXML
    ListView showAll;
    @FXML
    ChoiceBox chooseSPC;
    @FXML
    Button partsList;    
    private Tab parentTab;
    private boolean isAdmin;
         
    Database db= Database.getInstance();
    Connection connection= db.getConnection();//create connection to database
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
                store.add(rs.getString("SPCName"));// get all the names of the SPC centres
                
            }
            ObservableList<String> items =FXCollections.observableArrayList (store);
            chooseSPC.setItems(items);
           
        }
        
        catch(SQLException err)
        {
            showError("Database error", err.getMessage());
            Logger.getLogger(AllVehiclesController.class.getName()).log(Level.SEVERE, null, err);
            
        }
    }
    @FXML
    public void checkAllVehicles()throws SQLException
    {
        if(chooseSPC.getSelectionModel().isEmpty()){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-An SPC has been chosen");return;}
        Statement statement;ResultSet rs; ArrayList<String> store2= new ArrayList<String>();
        statement=connection.createStatement();
        rs=statement.executeQuery("select SPCID from SPCDetails where SPCName='"+chooseSPC.getSelectionModel().getSelectedItem().toString()+"'");
        int id=rs.getInt("SPCID");
        rs=statement.executeQuery("select VehicleInfo.Registration from SPCBooking,VehicleForSPCRepair,VehicleInfo,Vehicle where SPCBooking.SPCID='"+id+"' and Vehicle.VehicleID=VehicleInfo.VehicleID and SPCBooking.IsBookingForVehicle='1' and VehicleForSPCRepair.SPCBookingID=SPCBooking.SPCBookingID and VehicleInfo.VehicleID=VehicleForSPCRepair.VehicleID");
        ListView<String> list = new ListView<String>();
        store2.add("Registration\n");
        
        while(rs.next())
            {
               store2.add(rs.getString("Registration")+"\n");
            }
        ObservableList<String> items2 =FXCollections.observableArrayList (store2);
        showAll.setItems(items2);
    }
    @FXML
    private void mainPage(ActionEvent event) 
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
            Logger.getLogger(AllVehiclesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    public void showVehicleDetails()
    {
        // get the vehicle registration from the selected item in listview...
        if(showAll.getSelectionModel().isEmpty()||showAll.getSelectionModel().getSelectedIndex()==0){showError("Missing", "Please make sure a Vehicle has been selected");return;}
        Object selected=showAll.getSelectionModel().getSelectedItem();
        String registration= selected.toString().trim();
        
        try 
        {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customerDetails.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                CustomerDetailsController controller= fxmlLoader.<CustomerDetailsController>getController();
                controller.customerDetails(registration);
                Stage stage = new Stage();
                stage.setScene(new Scene(root1)); 
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
        } 
        catch(Exception e) 
        {
                Logger.getLogger(AllVehiclesController.class.getName()).log(Level.SEVERE, null, e);
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
    private void showError(String title, String content) //shown an alert pop of an error thrown
    {
        Alert alert;
        alert=new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
}
