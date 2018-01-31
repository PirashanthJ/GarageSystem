
package specialist;

import common.Database;
import diagrep.logic.DiagRepair;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import parts.GUI.SearchForPartsController;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * The main booking controller for SPC bookings.
 * Make bookings and delete bookings
 */
public class BookingController implements Initializable {
    Database db= Database.getInstance();
    Connection connection= db.getConnection();
    ArrayList<String> store= new ArrayList<String>();// array used for storing the added/deleted parts to show in the lisview.
    LocalDate currentDate;//store the current date from database
    LocalDate DiagStartDate;//store the start date of the diagRepair booking from database
    
    @FXML
    private TextField vehicleReg;
    @FXML
    private TextArea vehicleShow;
    @FXML
    private ListView addedPart;
    @FXML
    private ListView showPart;
    @FXML
    private TextArea bookingT;
    @FXML
    private ChoiceBox spcCentres;
    @FXML
    private Button addPart;
    @FXML
    private Button addVehicle;
    @FXML
    private CheckBox part;
    @FXML
    private CheckBox vehicle;
    @FXML
    private Button deletePart;
    @FXML
    private DatePicker returnDate;
    @FXML
    private DatePicker deliveryDate;
    @FXML
    private ChoiceBox diagID;
    @FXML
    private TitledPane section1;
    @FXML
    private TitledPane section2;
    @FXML
    private TitledPane section3;
    @FXML
    private TitledPane section4;
    @FXML
    private Button enterDiag;
    @FXML
    private TextField dFault;
    @FXML
    private CheckBox makeBooking;
    @FXML
    private CheckBox deleteBooking;
    @FXML
    private ListView allBookings;
    private Tab parentTab;
    private boolean isAdmin;
    @FXML
    private Label one;
    @FXML
    private Label two;
    @FXML
    private Label three;
    
   
    /**
     * Initializes the controller class.
     * get all the names of the SPC's
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
       
        one.setVisible(false);two.setVisible(false);three.setVisible(false);
        addPart.setDisable(true);
        spcCentres.getItems().clear();
        deletePart.setDisable(true);
        vehicleReg.setEditable(false);
      
        addedPart.setDisable(true);
        showPart.setDisable(true);
        section1.setCollapsible(false);
        section2.setCollapsible(false);
        section3.setCollapsible(false);
        section4.setCollapsible(false);
        part.setDisable(true);
        vehicle.setDisable(true);
        dFault.setEditable(false);
        store.add("PartID\t\tPartName");
       
        String insertInto= "select * from SPCDetails";
        try
        {
            
            Statement statement;
            statement= connection.createStatement();
            ResultSet rs= statement.executeQuery(insertInto);
            ArrayList<String> store= new ArrayList<String>();
           
            while(rs.next())
            {
                store.add(rs.getString("SPCName"));
                
            }
            ObservableList<String> items =FXCollections.observableArrayList (store);
            spcCentres.setItems(items);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(db.getCurrentDate());
            currentDate= LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(date));
        
            updateID();//initialize the diagRepair id's
            showAllBookings();//initialize all the bookings for delete bookings
           
        }
        
        catch(NullPointerException e)
        {
            showError("Could not start up", "Failed to initialize");
       
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
            
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        catch(ParseException e)
        {
            showError("Date error", e.getMessage());
        }
    }
    private void updateID()//update the DiagRepair ID's to choose from in the choice box
    {
        try
        {
            Statement statement;
            statement= connection.createStatement();
            ResultSet rs;
            ArrayList<String> store3= new ArrayList<String>();
         
            rs=statement.executeQuery("select distinct DiagRepairBooking.DiagRepID,VehicleInfo.Registration from DiagRepairBooking inner join VehicleInfo on VehicleInfo.VehicleID=DiagRepairBooking.VehicleID left join PartForRepairBooking on PartForRepairBooking.RepairBookingID=DiagRepairBooking.DiagRepID where (DiagRepairBooking.Completed='0' and DiagRepairBooking.sendVehicleToSPC='1' and DiagRepairBooking.DiagRepID not in (select DiagRepBookingID from SPCBooking)) or(DiagRepairBooking.Completed='0' and DiagRepairBooking.sendVehicleToSPC='0' and PartForRepairBooking.IsForSPC=1) ORDER BY DiagRepairBooking.DiagRepID");
            while(rs.next())
            {
                store3.add(Integer.toString(rs.getInt("DiagRepID"))+"\nVehicle registration: "+rs.getString("Registration"));
                
            }
            ObservableList<String> items3 =FXCollections.observableArrayList (store3);
            diagID.getItems().clear();
            diagID.setItems(items3);     
        }
        
        catch(NullPointerException e)
        {
            showError("Update failure","Could not update DiagRepairID");
       
        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());
            //System.out.println(e.getMessage());
           
        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
           
    }
    @FXML
    public void getParts() throws SQLException,ParseException
    {
        if(diagID.getSelectionModel().isEmpty()){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A DiagRepair ID has been chosen");return;}
        Statement statement= connection.createStatement();
        Object chosenID=diagID.getSelectionModel().getSelectedItem();
        String []arr= chosenID.toString().split("\n");
        String ID=arr[0];
        
        ResultSet rs2=statement.executeQuery("select StartDate,sendVehicleToSPC from DiagRepairBooking where DiagRepID='"+Integer.parseInt(ID)+"'");
        int forVehicle= rs2.getInt("sendVehicleToSPC");//boolean to check if booking is for Vehicle/Part
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(rs2.getString("StartDate"));
        DiagStartDate= LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(date));// set the diagID start date from the database
        
        addedPart.setDisable(false);showPart.setDisable(false);addPart.setDisable(false);
        if(forVehicle==0)//if booking is for part only then show list of parts send to SPC and only one part can be added per SPC booking for repair at a time
        {
            rs2=statement.executeQuery("select fault from DiagRepairBooking where DiagRepID='"+Integer.parseInt(ID)+"'");
            dFault.setText(rs2.getString("fault"));
            part.selectedProperty().set(true);vehicle.selectedProperty().set(false);
            //String get="select PartInfo.Name,PartInfo.Description,PartInfo.Cost from PartInfo,Part,PartForRepairBooking where PartForRepairBooking.PartID=Part.ID and PartForRepairBooking.IsForSPC='1' and PartForRepairBooking.RepairBookingID='"+Integer.parseInt(ID)+"' and PartInfo.Name=Part.Name and PartForRepairBooking.PartID not in(select PartID from PartForSPCRepair where SPCBookingID=(select SPCBookingID from SPCBooking where DiagRepBookingID='"+Integer.parseInt(ID)+"'))";
            String search="select count(PartForRepairBooking.IsForSPC=1) as totalCount,Part.ID,PartInfo.Name,PartInfo.Description,PartInfo.Cost from PartInfo inner join Part on Part.Name=PartInfo.Name inner join PartForRepairBooking on PartForRepairBooking.PartID=Part.ID inner join DiagRepairBooking on PartForRepairBooking.RepairBookingID=DiagRepairBooking.DiagRepID where DiagRepairBooking.DiagRepID="+Integer.parseInt(ID)+" and PartForRepairBooking.IsForSPC='1' and PartForRepairBooking.PartID not in(select PartID from PartForSPCRepair inner join SPCBooking on PartForSPCRepair.SPCBookingID=SPCBooking.SPCBookingID where SPCBooking.DiagRepBookingID="+Integer.parseInt(ID)+")";
            try
            {
                statement= connection.createStatement();
                ResultSet rs= statement.executeQuery(search);
                ArrayList<String> store= new ArrayList<String>();
                store.add("PartID\t\tPartName");
                if(rs.getInt("totalCount")==0){showAlert("All the parts are in repair", "Booking unavailable","All the parts in the repair are currently in repair\nNo extra parts have been added yet");return;}
                while(rs.next())
                {
                    store.add(rs.getInt("ID")+"\t\t\t"+rs.getString("Name")+"\n");
                
                }
                ObservableList<String> items2 =FXCollections.observableArrayList (store);
                addedPart.setItems(items2);
            }
       
            catch(NullPointerException e)
            {
                showError("Part list failure", "Nothing found");

            } 
            catch (IllegalArgumentException e) 
            {
                showError("Validation Error", e.getMessage());

            } 
        }
        else// if booking is for a vehicle sent then show partsInVehicle to repair from
        {
            ResultSet rs;
            ResultSet rs3;
            rs3=statement.executeQuery("select fault from DiagRepairBooking where DiagRepID='"+Integer.parseInt(ID)+"'");
            dFault.setText(rs3.getString("fault"));// set the fault from the database to UI to choose parts to repair
            vehicle.selectedProperty().set(true);part.selectedProperty().set(false);
       
            String get="select Part.ID,PartInfo.Name,PartInfo.Description,PartInfo.Cost from PartInfo,Part,PartInVehicle,Vehicle,DiagRepairBooking where DiagRepairBooking.DiagRepID='"+Integer.parseInt(ID)+"' and DiagRepairBooking.VehicleID=Vehicle.VehicleID and PartInVehicle.VehicleID=Vehicle.VehicleID and PartInVehicle.PartID=Part.ID and PartInfo.Name=Part.Name and PartInVehicle.PartID not in (select PartID from PartForSPCRepair where SPCBookingID=(select SPCBookingID from SPCBooking where DiagRepBookingID='"+Integer.parseInt(ID)+"'))";
            try
            {
              
                statement= connection.createStatement();
                rs= statement.executeQuery(get);
                ArrayList<String> store= new ArrayList<String>();
                store.add("PartID\t\tPartName");
                while(rs.next())
                {
                   store.add(rs.getInt("ID")+"\t\t\t"+rs.getString("Name")+"\n");
                
                }
                ObservableList<String> items2 =FXCollections.observableArrayList (store);
                addedPart.setItems(items2);

            }
       
            catch(NullPointerException e)
            {
                showError("PartInVehicle unavailable", "No parts available");
       
            } 
            catch (IllegalArgumentException e) 
            {
                showError("Validation Error", e.getMessage());

            } 
            catch (SQLException ex) 
            {
                showError("Database Error", ex.getMessage());
                Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);   
            }
        }
        section1.setExpanded(false);section1.setCollapsible(false);
        section2.setCollapsible(true);section2.setExpanded(true);
    }
    @FXML
    public void addPart()//add the selected part to the added part listView
    {
        if(addedPart.getSelectionModel().getSelectedItem() == null||addedPart.getSelectionModel().getSelectedIndex()==0){showError("Missing Input Fields", "Some Required Details Were Not Entered\n-Please check the following:\n-A Part has been selected");return;}
        int confirmed=showConfirm("","Adding part","Do you want to add this part?");
        if(confirmed==0){return;}
        
        Object selected=addedPart.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        store.add(text);
        ObservableList<String> items =FXCollections.observableArrayList (store);
        showPart.setItems(items);
        addedPart.getItems().remove(addedPart.getSelectionModel().getSelectedIndex());
        if(part.selectedProperty().getValue()==true){addPart.setDisable(true);}
        if(vehicle.selectedProperty().getValue()==true&&addedPart.getItems().size()==0){addPart.setDisable(true);};
        deletePart.setDisable(false);
        showPart.getSelectionModel().clearSelection();addedPart.getSelectionModel().clearSelection();
        showAlert("Part","Part added","Part added successfuly");
        
    }
    public void deletePart()// delete part from the added part list
    {
        if(showPart.getSelectionModel().getSelectedItem()==null||showPart.getSelectionModel().getSelectedIndex()==0){showError("Delete error", "Please check the following:\n-A part has been selected");return;}
        int confirmed=showConfirm("","Deleting part","Do you want to delete this part?");
        if(confirmed==0){return;}
        
        int selected=showPart.getSelectionModel().getSelectedIndex();
        addedPart.getItems().add(showPart.getSelectionModel().getSelectedItem());
        showPart.getItems().remove(selected);
        store.remove(selected);
        addPart.setDisable(false);
        addedPart.getSelectionModel().clearSelection(); showPart.getSelectionModel().clearSelection();
        if(store.isEmpty()||store.size()==1){deletePart.setDisable(true);addPart.setDisable(false);}
        showAlert("Part","Part deleted","Part deleted successfuly");
    }
    @FXML
    public void makeBooking()throws ParseException
            //To make the booking, first check the chosen return and delivery date make sense, so delivery date is before return date and
            //delivery date is after DiagRepBooking start date, so check with current date is unnecessary since diagRepair already checks constraint.
    {
        int size=showPart.getItems().size();
        if(returnDate.getValue()==null||deliveryDate.getValue()==null||returnDate.getValue().isBefore(deliveryDate.getValue())||currentDate.isAfter(deliveryDate.getValue())||deliveryDate.getValue().isBefore(DiagStartDate)||returnDate.getValue().equals(deliveryDate.getValue()))
        {
            showError("Date Error", "Please check the following:\n-Expected Return Date has been selected\n-Expected Delivery Date has been selected\n-The return date is after the delivery date\n-All dates are in the future");
            return;
        }
        int confirmed=showConfirm("","Make Booking","Do you want to make this booking?");
        if(confirmed==0){return;}
        try
            {
       
                Statement statement;
                statement=connection.createStatement();
                ResultSet rs;
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date returnTemp= format.parse(returnDate.getValue().toString());
                LocalDate returnD= LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(returnTemp));
        
                Date deliveryTemp= format.parse(returnDate.getValue().toString());
                LocalDate deliverD= LocalDate.parse( new SimpleDateFormat("yyyy-MM-dd").format(deliveryTemp));
                
                Object chosenID=diagID.getSelectionModel().getSelectedItem();
                String []arr= chosenID.toString().split("\n");
                String ID=arr[0];
        
                String DiagID="select DiagRepID from DiagRepairBooking where DiagRepID='"+Integer.parseInt(ID)+"'";  
                String SPC="select SPCID from SPCDetails where SPCName='"+spcCentres.getSelectionModel().getSelectedItem().toString()+"'";
                int DiagRepairID; int costOfService;int SPCID;
                int isBookingForVehicle= checkBoolean();
                int returnStatus=0;
                rs=statement.executeQuery(DiagID);
                DiagRepairID=rs.getInt("DiagRepID");
                rs=statement.executeQuery(SPC);
                SPCID=rs.getInt("SPCID");
        
        
                String toInsert="insert into SPCBooking('SPCID','DiagRepBookingID','IsBookingForVehicle','ReturnStatus','ExpectedReturnDate','ExpectedDeliveryDate') values('"+SPCID+"','"+DiagRepairID+"','"+isBookingForVehicle+"','"+returnStatus+"','"+returnD+"','"+deliverD+"')";
        
                String key[] = {"SPCBookingID"}; 
                PreparedStatement ps = connection.prepareStatement(toInsert, key);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
        
                long generatedKey = rs.getLong(1);
                int bookingID=(int)generatedKey;

        if(part.selectedProperty().getValue()==true)// if part is selected then add part to PartForSPCRepair
        {
                showPart.getItems().remove(0);
                ObservableList<String> list=showPart.getItems();
                ResultSet two;
                int partID=0;
                while(list.size()!=0)
                {
                    String []arr2= list.get(0).split("\t");
                    partID=Integer.parseInt(arr2[0]);
                    statement.executeUpdate("insert into PartForSPCRepair values('"+partID+"','"+bookingID+"','0')");
                    list.remove(0);
                }
        }
        if(isBookingForVehicle==1)// if the booking if for vehicle then add the vehicle and the selected parts to PartForSPCRepair
        {
                String toSearch=("select VehicleID from DiagRepairBooking where DiagRepID='"+(DiagRepairID)+"'");
                statement=connection.createStatement();
                rs=statement.executeQuery(toSearch);
                int vehicleID= rs.getInt("VehicleID");
                statement.executeUpdate("insert into VehicleForSPCRepair values('"+vehicleID+"','"+bookingID+"')");
          
                showPart.getItems().remove(0);
                ObservableList<String> list=showPart.getItems();
                ResultSet two;
                int partID=0;
                while(list.size()!=0)
                {
                    String []arr2= list.get(0).split("\t");
                    partID=Integer.parseInt(arr2[0]);
                    statement.executeUpdate("insert into PartForSPCRepair values('"+partID+"','"+bookingID+"','0')");
                    list.remove(0);
                }
        }
        addedPart.getItems().clear();
        showPart.getItems().clear();
        returnDate.setValue(null);
        deliveryDate.setValue(null);
        part.setSelected(false);
        vehicle.setSelected(false);
        showAlert("Booking Confirmation", "Booking Created!", "This booking has successfully been added to the database!\n\nBooking ID:"+bookingID+"\nExpected Return Date: "+returnD);
        section3.setExpanded(false);
        section2.setCollapsible(false);section3.setCollapsible(false);section1.setCollapsible(true);
        store.clear();
        updateID();
        showAllBookings();
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
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    private int checkBoolean()//check if booking is for vehicle or part
    {
        if(vehicle.selectedProperty().getValue()==true){return 1;}
        else{return 0;}
    }
    @FXML
    private void  getVehicle() throws SQLException,ParseException// get the Vehicle registration from database
    {
        if(diagID.getSelectionModel().isEmpty()||spcCentres.getSelectionModel().isEmpty()){showError("Missing Input Fields", "Please check the following:\n-A DiagRepair ID has been chosen\n-An SPC has been chosen");return;}
        showPart.getItems().clear();
        addedPart.getItems().clear();
        vehicleReg.clear();
        Object chosenID=diagID.getSelectionModel().getSelectedItem();
        String []arr= chosenID.toString().split("\n");
        String ID=arr[0];
        Statement statement; int vehicleID;
        String toSearch=("select VehicleID from DiagRepairBooking where DiagRepID='"+Integer.parseInt(ID)+"'");
        
        statement=connection.createStatement();
        ResultSet rs= statement.executeQuery(toSearch);
        vehicleID= rs.getInt("VehicleID");
        
        rs=statement.executeQuery("select Registration from VehicleInfo where VehicleID='"+vehicleID+"'");
        String registration= rs.getString("Registration");
        vehicleReg.setText(registration);
        
        getParts();// call the get parts method to get the associated parts/part
    }
    @FXML
    private void enterPart()
    {
       
        int size=showPart.getItems().size();
        if(size!=0)
        {
        section2.setExpanded(false);section2.setCollapsible(false);
        section3.setCollapsible(true);section3.setExpanded(true);
        }
        else
        {
         showError("Missing parts", "Check part has been added");
         return;
        }
    }
    @FXML
    private void chooseBookingMode()// choosen make booking or delet booking at the start
    {
        if(makeBooking.isSelected())
        {
            section1.setCollapsible(true);section1.setExpanded(true);
            deleteBooking.setDisable(true);
            section4.setCollapsible(false);section4.setExpanded(false);
            makeBooking.setDisable(true);
            one.setVisible(true);two.setVisible(true);three.setVisible(true);
            
        }
        else if(deleteBooking.isSelected())
        {
            section4.setCollapsible(true);section4.setExpanded(true);
            section3.setCollapsible(false);section3.setExpanded(false);
            section2.setCollapsible(false);section2.setExpanded(false);
            section1.setCollapsible(false);section1.setExpanded(false);
            makeBooking.setDisable(true);
            deleteBooking.setDisable(true);
            one.setVisible(false);two.setVisible(false);three.setVisible(false);
        }
        else
        {
            section4.setCollapsible(false);
            section1.setCollapsible(false);
            section2.setCollapsible(false);
            section3.setCollapsible(false);
            one.setVisible(false);two.setVisible(false);three.setVisible(false);
        }
        
    
    
}
    @FXML
    private void edit()// to change bookingMode or start again..
    {
        section4.setExpanded(false);section4.setCollapsible(false);
        section1.setExpanded(false);section1.setCollapsible(false);
        section2.setExpanded(false);section2.setCollapsible(false);
        section3.setExpanded(false);section3.setCollapsible(false);
        makeBooking.setDisable(false);deleteBooking.setDisable(false);
        makeBooking.setSelected(false);deleteBooking.setSelected(false);
        showPart.getItems().clear();addedPart.getItems().clear();store.clear();
        deletePart.setDisable(true);
        one.setVisible(false);two.setVisible(false);three.setVisible(false);
        
    }
    @FXML
    private void showAllBookings()// show all SPC bookings for delete bookings
    {
        ArrayList<String> store= new ArrayList<String>();
        String insertInto= "select SPCBooking.SPCBookingID, SPCDetails.SPCName from SPCBooking inner join SPCDetails on SPCBooking.SPCID=SPCDetails.SPCID where ReturnStatus='0'";
        try
        {
            Statement statement;
            statement= connection.createStatement();
            ResultSet rs= statement.executeQuery(insertInto);
            ListView<String> list = new ListView<String>();
            store.add("SPCBookingID\t\t\t\t\t\tSPCName");
            while(rs.next())
            {
                store.add(rs.getInt("SPCBookingID")+"\t\t\t\t\t\t\t\t"+rs.getString("SPCName")+"\n");
                
            }
            ObservableList<String> items =FXCollections.observableArrayList (store);
            allBookings.setItems(items);
           
        }
        
        catch(NullPointerException e)
        {
            showError("Bookings unavailable", "Could not get list of bookings");

        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());

        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
    @FXML
    private void deleteBooking()// delete the chosen booking and show confirmation before doing so and delete associated parts/Vehicle and check with DiagRepairBooking
    {
        if(allBookings.getSelectionModel().isEmpty()||allBookings.getSelectionModel().getSelectedIndex()==0){showError("Missing Input Fields", "Some Required Details Were Not Entered\nPlease check the following:\n-A Booking has been chosen");return;}
   
        int confirmed=showConfirm("","Deleting booking","Do you want to delete this booking?");
        if(confirmed==0){return;}

        Object selected=allBookings.getSelectionModel().getSelectedItem();
        String text= selected.toString();
        int index= text.indexOf("\t");
        int spcBooking=Integer.parseInt(text.substring(0, index));
        
        Statement statement;Statement statement2;
        try
        {
            statement=connection.createStatement();
            statement2=connection.createStatement();
            ResultSet rs=statement.executeQuery("select IsBookingForVehicle,DiagRepBookingID,SPCID from SPCBooking where SPCBookingID='"+spcBooking+"'");
            int bookingForVehicle=rs.getInt("IsBookingForVehicle");//check if the booking is for vehicle or part
            int spcID=rs.getInt("SPCID");
            int DiagRep=rs.getInt("DiagRepBookingID");
            if(bookingForVehicle==0)//if booking is for part only
            {
                ResultSet rs2=statement2.executeQuery("select PartForSPCRepair.PartID from SPCBooking inner join PartForSPCRepair on PartForSPCRepair.SPCBookingID=SPCBooking.SPCBookingID where SPCBooking.SPCBookingID='"+spcBooking+"'");
                //statement.executeUpdate("delete from PartForSPCRepair where SPCBookingID='"+spcBooking+"'");//delete the part
                statement.executeUpdate("delete from SPCBooking where SPCBookingID='"+spcBooking+"' and DiagRepBookingID="+DiagRep+" and SPCID="+spcID);// delete the SPC booking
                DiagRepair diag= new DiagRepair(DiagRep);// make instance of DiagRepair to check if DiagRepairBooking can be deleted
                diag.removePart(rs2.getInt("PartID"));// this method will check if the part can be deleted from DiagRepair
                showAlert("Deleting Booking", "Part Deleted", "Booking deleted successfully");
                showAllBookings();
            }
            else// if booking is for Vehicle
            {
                statement.executeUpdate("delete from PartForSPCRepair where SPCBookingID='"+spcBooking+"'");// delete all parts 
                statement.executeUpdate("delete from VehicleForSPCRepair where SPCBookingID='"+spcBooking+"'");//delete the vehicle from list
                statement.executeUpdate("delete from SPCBooking where SPCBookingID='"+spcBooking+"' and DiagRepBookingID='"+DiagRep+"' and SPCID='"+spcID+"'");//delete the booking
                statement.executeUpdate("Update DiagRepairBooking set sendVehicleToSPC=0 where DiagRepID='"+DiagRep+"'");// Do no delete booking, set the boolean to false in DiagRepairBooking
                showAlert("Deleting Booking", "Vehicle deleted", "Booking deleted successfully");
                showAllBookings();   
            }
            updateID();
            
        }
        catch(NullPointerException e)
        {
            showError("Missing Input Fields", "Please check the following \nBooking has been chosen");

        } 
        catch (IllegalArgumentException e) 
        {
            showError("Validation Error", e.getMessage());

        } 
        catch (SQLException ex) 
        {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(diagrep.logic.InProgressException e)    
        {
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    public void showPartDetails(ActionEvent event)
    {
        if(addedPart.getSelectionModel().isEmpty()||addedPart.getSelectionModel().getSelectedIndex()==0){showError("Missing ", "Please make sure a part has been chosen");return;}
        
        Object selected=addedPart.getSelectionModel().getSelectedItem();
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
                showPart.getSelectionModel().clearSelection();addedPart.getSelectionModel().clearSelection();
        }
        catch(Exception e) 
        {
                Logger.getLogger(RepairsVehicleController.class.getName()).log(Level.SEVERE, null, e);
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
