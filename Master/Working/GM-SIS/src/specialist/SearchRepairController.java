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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * Search for repairs by partial or full vehicle registration and customer first and surname..
 */
public class SearchRepairController implements Initializable 
{
    Database db= Database.getInstance();
    Connection connection=db.getConnection();
    Statement statement;
    DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    TextField fName;
    @FXML
    TextField sName;
    @FXML
    TextField vReg;
    @FXML
    Button searchName;
    @FXML
    Button searchReg;
    @FXML
    ChoiceBox chooseSPC;
    @FXML
    private TableView<Repair> showDetails;
    @FXML
    private TableColumn<Repair, String> reg;
    @FXML
    private TableColumn<Repair, Integer> id;
    @FXML
    private TableColumn<Repair, String> ReturnDate;
    @FXML
    private TableColumn<Repair, Integer> ReturnStatus;
    @FXML
    private TableColumn<Repair, String> spcName;
    private Tab parentTab;
    private boolean isAdmin;
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
    public final class Repair
    {
        private IntegerProperty id;private StringProperty reg; private StringProperty spcName ;private StringProperty ReturnDate ;private StringProperty ReturnStatus ;
    
        public Repair(int id, String reg, String spcName, String ReturnDate, String ReturnStatus)// when do no know the spcID to add a new SPC to the database
        {
            this.id=new SimpleIntegerProperty(id);
            this.spcName=new SimpleStringProperty(spcName);
            this.reg=new SimpleStringProperty(reg);
            this.ReturnDate=new SimpleStringProperty(ReturnDate);
            this.ReturnStatus=new SimpleStringProperty(ReturnStatus);
        }
        public Repair(int id, String reg,String ReturnDate, String ReturnStatus)// when do no know the spcID to add a new SPC to the database
        {
            this.id=new SimpleIntegerProperty(id);
            this.reg=new SimpleStringProperty(reg);
            this.ReturnDate=new SimpleStringProperty(ReturnDate);
            this.ReturnStatus=new SimpleStringProperty(ReturnStatus);
        }
        public StringProperty regProperty() { return this.reg; }
        public StringProperty ReturnStatusProperty() { return this.ReturnStatus; }
        public StringProperty ReturnDateProperty() { return this.ReturnDate; }
        public StringProperty spcNameProperty() { return this.spcName; }
        public IntegerProperty idProperty() { return this.id; }
    }
    @FXML
    public void searchName()throws SQLException,ParseException// search repair by name
    {
        if(fName.getText().equals("")||sName.getText().equals("")){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-Firstname and Surname have been entered");return;}
        statement=connection.createStatement();
        ResultSet rs;
        rs= statement.executeQuery("select VehicleInfo.Registration,SPCBooking.SPCBookingID,SPCDetails.SPCName,SPCBooking.ExpectedReturnDate,SPCBooking.ReturnStatus from SPCDetails,SPCBooking,DiagRepairBooking,VehicleInfo,Customer,Vehicle where SPCBooking.SPCID=SPCDetails.SPCID and VehicleInfo.VehicleID=Vehicle.VehicleID and SPCBooking.DiagRepBookingID=DiagRepairBooking.DiagRepID and DiagRepairBooking.VehicleID=VehicleInfo.VehicleID and Vehicle.CustomerID=Customer.CustomerID and Customer.Firstname like '%"+fName.getText()+"%' and Customer.Surname like '%"+sName.getText()+"%'");
        ArrayList<Repair> store= new ArrayList<Repair>(); 

        while(rs.next())
        {
            Repair repair=new Repair(rs.getInt("SPCBookingID"),rs.getString("Registration"),rs.getString("SPCName"),rs.getString("ExpectedReturnDate"),rs.getString("ReturnStatus"));
            store.add(repair);
        }
        
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        reg.setCellValueFactory(new PropertyValueFactory<>("reg"));
        spcName.setCellValueFactory(new PropertyValueFactory<>("spcName"));
        ReturnDate.setCellValueFactory(new PropertyValueFactory<>("ReturnDate"));
        ReturnStatus.setCellValueFactory(new PropertyValueFactory<>("ReturnStatus"));
        
        ObservableList<Repair> items2 =FXCollections.observableArrayList (store);
        showDetails.setItems(items2);
    }
    @FXML
    public void searchReg()throws SQLException,ParseException// search by vehicle registration for repairs at all SPC's or at a selected SPC..
    {
        if(vReg.getText().equals("")){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A partial or full Vehicle Registration has been entered");return;}
        if(chooseSPC.getSelectionModel().isEmpty())// to search at all SPC's
        {
        statement=connection.createStatement();
        ResultSet rs;
        rs= statement.executeQuery("select SPCBooking.SPCBookingID,SPCDetails.SPCName,SPCBooking.ExpectedReturnDate,SPCBooking.ReturnStatus,VehicleInfo.Registration from SPCDetails,SPCBooking,DiagRepairBooking,VehicleInfo where SPCBooking.SPCID=SPCDetails.SPCID and SPCBooking.DiagRepBookingID=DiagRepairBooking.DiagRepID and DiagRepairBooking.VehicleID=VehicleInfo.VehicleID and VehicleInfo.Registration like '%"+vReg.getText()+"%'");
        ArrayList<Repair> store= new ArrayList<Repair>(); 

        while(rs.next())
        {
            Repair repair=new Repair(rs.getInt("SPCBookingID"),rs.getString("Registration"),rs.getString("SPCName"),rs.getString("ExpectedReturnDate"),rs.getString("ReturnStatus"));
            store.add(repair);
        }
        
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        spcName.setCellValueFactory(new PropertyValueFactory<>("spcName"));
        reg.setCellValueFactory(new PropertyValueFactory<>("reg"));
        ReturnDate.setCellValueFactory(new PropertyValueFactory<>("ReturnDate"));
        ReturnStatus.setCellValueFactory(new PropertyValueFactory<>("ReturnStatus"));
        ObservableList<Repair> items2 =FXCollections.observableArrayList (store);
        showDetails.setItems(items2);
        }
        else// to search for by selected SPC
        {
            statement=connection.createStatement();
            ResultSet rs;
            rs= statement.executeQuery("select SPCDetails.SPCName,SPCBooking.SPCBookingID,SPCBooking.ExpectedReturnDate,SPCBooking.ReturnStatus,VehicleInfo.Registration from SPCDetails,SPCBooking,DiagRepairBooking,VehicleInfo where SPCBooking.SPCID=SPCDetails.SPCID and SPCBooking.DiagRepBookingID=DiagRepairBooking.DiagRepID and DiagRepairBooking.VehicleID=VehicleInfo.VehicleID and VehicleInfo.Registration like '%"+vReg.getText()+"%' and SPCDetails.SPCName='"+chooseSPC.getSelectionModel().getSelectedItem().toString()+"'");
            ArrayList<Repair> store= new ArrayList<Repair>(); 

            while(rs.next())
            {
                Repair repair=new Repair(rs.getInt("SPCBookingID"),rs.getString("Registration"),rs.getString("SPCName"),rs.getString("ExpectedReturnDate"),rs.getString("ReturnStatus"));
                store.add(repair);
            }
        
            id.setCellValueFactory(new PropertyValueFactory<>("id"));
            spcName.setCellValueFactory(new PropertyValueFactory<>("spcName"));
            reg.setCellValueFactory(new PropertyValueFactory<>("reg"));
            ReturnDate.setCellValueFactory(new PropertyValueFactory<>("ReturnDate"));
            ReturnStatus.setCellValueFactory(new PropertyValueFactory<>("ReturnStatus"));
        
            ObservableList<Repair> items2 =FXCollections.observableArrayList (store);
            showDetails.setItems(items2);
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
            Logger.getLogger(SearchRepairController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    public void showVehicleDetails()
    {
        // get the vehicle registration from the selected item in listview...
        if(showDetails.getSelectionModel().isEmpty()){showError("No Repair selected", "Please make sure a repair has been selected");return;}
        Repair repair= showDetails.getSelectionModel().getSelectedItem();
        TablePosition position=(TablePosition)showDetails.getSelectionModel().getSelectedCells().get(0);
        Object selected= position.getTableColumn().getCellData(repair);
        String reg= selected.toString();
        System.out.println(reg);
        try 
        {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customerDetails.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                CustomerDetailsController controller= fxmlLoader.<CustomerDetailsController>getController();
                controller.customerDetails(reg);
                Stage stage = new Stage();
                stage.setScene(new Scene(root1)); 
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
        } 
        catch(Exception e) 
        {
                Logger.getLogger(SearchRepairController.class.getName()).log(Level.SEVERE, null, e);
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
