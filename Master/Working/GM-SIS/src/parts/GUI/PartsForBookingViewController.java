/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parts.GUI;

import common.Database;
import diagrep.gui.BookingController;
import diagrep.logic.DiagRepair;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import parts.logic.InstalledPart;
import parts.logic.Part;
import parts.logic.PartInBooking;
import parts.logic.PartInventory;
import parts.logic.PartType;
import parts.logic.Utility;

/**
 * FXML Controller class
 * Code to show checkboxes and use simple properties by Jens-Peter Haack (StackOverFlow) -
 * How to populate a tableview that contain CheckBoc in JavaFX
 * 
 * @author Mia
 */
public class PartsForBookingViewController implements Initializable {

    @FXML
    private TableView<rowForPartInBookingTable> partInBookingTable;
    @FXML
    private TableView<rowForPartToAddTable> partToAddToBookingTable;
    private DiagRepair booking;
    private ArrayList<rowForPartInBookingTable> partsInBookingTable = new ArrayList<>();
    private ArrayList<rowForPartToAddTable> partsNotInBookingYet = new ArrayList<>();
    private ArrayList<String> namesOfPartsInBooking = new ArrayList<>();
    private final Database DB = Database.getInstance();
    
    @FXML
    private TableColumn<rowForPartInBookingTable, String> inBookingPartNameColumn;
    @FXML
    private TableColumn<rowForPartInBookingTable, Boolean> inBookingInstalledColumn;
    @FXML
    private TableColumn<rowForPartInBookingTable, Boolean> repairColumn;
    @FXML
    private TableColumn<rowForPartInBookingTable, Boolean> inBookingAddNewColumn;
    @FXML
    private TableColumn<rowForPartInBookingTable, Boolean> SPCColumn;
    @FXML
    private TableColumn<rowForPartInBookingTable, Boolean> deleteColumn;
    @FXML
    private TableColumn<rowForPartToAddTable, String> partNameColumn;
    @FXML
    private TableColumn<rowForPartToAddTable, String> descriptionColumn;
    @FXML
    private TableColumn<rowForPartToAddTable, Double> costColumn;
    @FXML
    private TableColumn<rowForPartToAddTable, Boolean> installedColumn;
    @FXML
    private TableColumn<rowForPartToAddTable, Boolean> addNewColumn;
    @FXML
    private TextField searchText;
    @FXML
    private Label bookingIDText;
    @FXML
    private Label faultText;
    
    private Tab parentTab;
    private Tab bookingsTab;
    @FXML
    private Label costText;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Code by Alexander.Berg (StackOverFlow) - Detect doubleclock on row of TableView JavaFX
        // shows popup with part information on double click
        partInBookingTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override 
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    FXMLLoader loader = new FXMLLoader();
                    Pane root = null;
                    try {
                        root = loader.load(getClass().getResource("partUsedDetails.fxml").openStream());
                    } catch (IOException ex) {
                        Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    PartUsedDetailsController control = loader.getController();
                    control.initializeTextViews(new InstalledPart(partInBookingTable.getSelectionModel().getSelectedItem().getPartInBoooking().getPart().getID()));
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(root);
                    stage.setResizable(false);
                    stage.setScene(scene);
                    stage.setTitle("View Used Part Details");
                    stage.showAndWait();
                }
            }
        });
    }    
    
    /**
     * Sets the tables, bookingid, cost, and fault text based on the given booking 
     * 
     * @param booking The booking to populate the view for
     */
    public void initializeWithBooking(DiagRepair booking){
        this.booking = booking;
        bookingIDText.setText(String.valueOf(booking.getBookingID()));
        faultText.setText(booking.getFault());
        costText.setText(String.valueOf(booking.getCost()));
        
        // prevents rows from being selected
        partToAddToBookingTable.setSelectionModel(null);
        
        namesOfPartsInBooking.clear();
        partsInBookingTable.clear();
        partsNotInBookingYet.clear();
        
        initPartsInBookingTable();
        initPartToAddTable();
    }
    
    /**
     * Displays the parts for the part to add to booking table
     */
    private void initPartsInBookingTable(){
        getPartsInBooking();
        inBookingPartNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        inBookingInstalledColumn.setCellValueFactory(new PropertyValueFactory<>("installed"));
        inBookingInstalledColumn.setCellFactory(column -> new CheckBoxTableCell());
        repairColumn.setCellValueFactory(new PropertyValueFactory<>("repair"));
        repairColumn.setCellFactory(column -> new CheckBoxTableCell());
        inBookingInstalledColumn.setCellFactory(column -> new CheckBoxTableCell());
        inBookingAddNewColumn.setCellValueFactory(new PropertyValueFactory<>("addNew"));
        inBookingAddNewColumn.setCellFactory(column -> new CheckBoxTableCell());
        SPCColumn.setCellValueFactory(new PropertyValueFactory<>("SPC"));
        SPCColumn.setCellFactory(column -> new CheckBoxTableCell());
        deleteColumn.setCellValueFactory(new PropertyValueFactory<>("delete"));
        deleteColumn.setCellFactory(column -> new CheckBoxTableCell());
        partInBookingTable.setItems(FXCollections.observableList(partsInBookingTable));
    }
    /**
     * gets the parts in the booking
     */
    private void getPartsInBooking(){
        String getPartsInBooking = "SELECT PartID FROM PartForRepairBooking WHERE RepairBookingID=" + booking.getBookingID();
        Connection conn = DB.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(getPartsInBooking);
            while(rs.next()){
                rowForPartInBookingTable row = new rowForPartInBookingTable(new PartInBooking(rs.getInt("PartID"), booking.getBookingID()));
                partsInBookingTable.add(row);
                namesOfPartsInBooking.add(row.getPartName());
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Displays the parts in the part to add table
     */
    private void initPartToAddTable(){
        getPartsNotInBooking();
        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        installedColumn.setCellValueFactory(new PropertyValueFactory<>("installed"));
        installedColumn.setCellFactory(column -> new CheckBoxTableCell());
        addNewColumn.setCellValueFactory(new PropertyValueFactory<>("addNew"));
        addNewColumn.setCellFactory(column -> new CheckBoxTableCell());
        partToAddToBookingTable.setItems(FXCollections.observableList(partsNotInBookingYet));
    }
    
    /**
     * gets the parts that aren't in the booking 
     */
    private void getPartsNotInBooking(){
        // clears list before repopulating it to avoid duplicates
        partsNotInBookingYet.clear();
        PartInventory partInventory = PartInventory.getInstance();
        List<PartType> allParts = partInventory.getAllPartTypes();
        for(int i = 0; i < allParts.size(); i++){
            if(!namesOfPartsInBooking.contains(allParts.get(i).getName())){
                partsNotInBookingYet.add(new rowForPartToAddTable(allParts.get(i)));
            }
        }
    }
    
    /**
     * searches for a part by name and changes the list of parts to display 
     * in the parts to add to booking table
     * @param event The event that causes this method to run
     */
    @FXML
    private void search(ActionEvent event) {
        updateBooking(null);
        String searchBy = searchText.getText();
        // search for parts not in booking already
        PartInventory inventory = PartInventory.getInstance();
        ArrayList<PartType> searchResults = inventory.searchForPartByName(searchBy);
        // clear what is in list currently
        partsNotInBookingYet.clear();
        for(int i = 0; i < searchResults.size(); i++){
            if(!namesOfPartsInBooking.contains(searchResults.get(i).getName())){
                partsNotInBookingYet.add(new rowForPartToAddTable(searchResults.get(i)));
            }
        }
        searchText.clear();
    }

    /**
     * shows all possible parts that are not in the booking
     * @param event The event that causes this method to run
     */
    @FXML
    private void showAllPossibleParts(ActionEvent event) {
        //show parts not in booking already
        updateBooking(null);
        initPartToAddTable();
    }
    
    /**
     * Deletes the parts from the booking, and removes them from the list that 
     * populates the table that shows the existing parts in the booking. 
     */
    private void deletePartsFromBooking(){
        // get all parts to be deleted from booking
        ArrayList<Integer> indexesToRemove = new ArrayList<>();
        for(int i = 0; i < partsInBookingTable.size(); i++){
            // want to delete part from booking
            if(partsInBookingTable.get(i).delete.get()){
                // logic for removing from booking in removeFromBooking method
                partsInBookingTable.get(i).getPartInBoooking().removeFromBooking();
                indexesToRemove.add(i);
            }
        }   
        for(int i = 0; i < indexesToRemove.size(); i++){
            partsInBookingTable.remove(i);
        }
    }

    /**
     * 1) all parts to be deleted are removed from the booking according to our logic in PartInBooking
     * 2) The parts to be added are gathered, and a part is added to the vehicle by withdrawing it from the inventory, 
     * installing it in the vehicle, and costing if necessary .
     * The parts will be added to the 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void updateBooking(ActionEvent event) {
        deletePartsFromBooking();     
        
        ArrayList<PartType> partsToAddToBooking = new ArrayList<>();
        // get all parts to add
        for(int i = 0; i < partsNotInBookingYet.size(); i++){
            if(partsNotInBookingYet.get(i).addNew.get()){
                partsToAddToBooking.add(partsNotInBookingYet.get(i).getPartType());
            }
        }
        
        PartInventory inventory = PartInventory.getInstance();
        
        ArrayList<String> installedPartNames = getNamesOfInstalledParts();
        ArrayList<InstalledPart> installedParts = getInstalledParts();
        
        for(int i = 0; i < partsToAddToBooking.size(); i++){
            // withdraw from inventory, install in vehicle, add to booking, and add the cost of the part to the booking
            Connection conn = DB.getConnection();
            Statement statement = null;
            int partID = inventory.withdraw(partsToAddToBooking.get(i).getName());
            if(partID != -1){
                //install part
                Part part = new Part(partID);
                // the part is already installed in the vehicle, so it is to be replaced, and the warranty checked for costing purposes
                if(installedPartNames.contains(part.getName())){
                    int index = installedPartNames.indexOf(part.getName());
                    InstalledPart existingPart = installedParts.get(index);
                    
                    if(existingPart.checkWarranty()){
                        System.out.println("Part in Warranty, Not charging");
                        boolean uninstalled = existingPart.uninstall();
                        if(uninstalled){
                            boolean success = part.install(booking.getVehicleID());
                            if(!success){
                                showAlert("Could Not Install", part.getName() + " could not be installed on this vehicle, as it already has 10 parts installed.\n");
                                part.putBackInInventory();
                            }else{
                                addPartToBooking(partID, false);
                                partsNotInBookingYet.remove(i);
                                showAlert("Part in Warranty", existingPart.getPart().getName() 
                                        + " is in warranty, so the cost of the replacement part is not being added to the cost of the booking.");
                            }
                        }else{
                            // should never run, but here in case
                            showAlert("Error Replacing Part", "This part cannot be replaced, as the vehicle the part is installed on is at SPC.\n" + 
                                    "Please ensure the SPC repair is completed and try again.");
                        }
                    }else{
                        boolean uninstalled = existingPart.uninstall();
                        if(uninstalled){
                            boolean success = part.install(booking.getVehicleID());
                            if(!success){
                                showAlert("Could Not Install", part.getName() + " could not be installed on this vehicle, as it already has 10 parts installed.\n");
                                part.putBackInInventory();
                            }else{
                                addPartToBooking(partID, true);
                                booking.incrementCost(part.getCost());
                                partsNotInBookingYet.remove(i);
                            }
                        }else{// should never run, but here in case
                            showAlert("Error Replacing Part", "This part cannot be replaced, as the vehicle the part is installed on is at SPC.\n" 
                               + "Please ensure the SPC repair is completed and try again.");
                        }
                    }
                }else{
                    boolean success = part.install(booking.getVehicleID());
                    if(!success){
                        showAlert("Could Not Install", part.getName() + " could not be installed on this vehicle, as it already has 10 parts installed.\n");
                        part.putBackInInventory();
                    }else{
                        // cost always incremented here because I am not replacing an existing part
                        addPartToBooking(partID, true);
                        booking.incrementCost(part.getCost());
                        partsNotInBookingYet.remove(i);
                    }
                }
            }else{
                boolean yes = showYesNo("No Stock", "There is no stock of " + partsToAddToBooking.get(i).getName() + " in the inventory.\n" +
                    "More stock must be added before it can be installed on a vehicle. Do you have stock to add?\nNote: You will need to reselect the part to add after adding stock.");
                if(yes){
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("addStockView.fxml"));
                    Pane root = null;
                    try {
                        root = loader.load();
                        AddStockViewController controller = loader.getController();
                        controller.setAdd(true);
                    } catch (IOException ex) {
                        Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.setTitle("Add Stock");
                    stage.showAndWait();
                }
            }
        }
        
        try {
            booking = new DiagRepair(booking.getBookingID());
            initializeWithBooking(booking);
        } catch (SQLException ex) {
            Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        costText.setText(String.valueOf(booking.getCost()));
    }
    
    /**
     * Sets the view for the part tab, so when the user navigates back to the part tab
     * after viewing this page 
     */
    private void setPartScreen(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("searchForParts.fxml"));
        Pane root = null;
        try {
            root = loader.load();
            SearchForPartsController control = loader.getController();
            control.setTabs(parentTab, bookingsTab);
            parentTab.setContent(root);
            TabPane tabPane = parentTab.getTabPane();
                    ObservableList<Tab> tabs = tabPane.getTabs();
                    for(int i = 0; i < tabs.size(); i++){
                        if(!tabs.get(i).isSelected()){
                            tabs.get(i).setDisable(false);
                        }
                    }
        } catch (IOException ex) {
            Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Loads the booking screen and calls the required methods on the controller 
     * to populate the view properly, and sets the view the user has to the bookings tab
     * 
     */
    private void goToBookingScreen(){
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("diagrep/gui/BookingFXML.fxml"));
        Pane root = null;
        try {
            root = loader.load();
            BookingController control = loader.getController();
            control.setTabs(bookingsTab, parentTab);
            control.setBookingToEdit(new DiagRepair(booking.getBookingID()));
            bookingsTab.getTabPane().getSelectionModel().select(bookingsTab);
            bookingsTab.setContent(root);
            TabPane tabPane = parentTab.getTabPane();
                    ObservableList<Tab> tabs = tabPane.getTabs();
                    for(int i = 0; i < tabs.size(); i++){
                        if(!tabs.get(i).isSelected()){
                            tabs.get(i).setDisable(false);
                        }
                    }
        } catch (IOException ex) {
            Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Adds the part to the booking and charges where appropriate 
     * @param partID The id of the part to add to the booking
     * @param charge whether or not the part is being charged for
     */
    private void addPartToBooking(int partID, boolean charge){
        //add part to booking
        String addPartToBooking = "";
        if(charge){
            addPartToBooking = "INSERT INTO PartForRepairBooking VALUES(" + partID + ", " + booking.getBookingID() + ", 0,0,1,1)";
        }else{
            addPartToBooking = "INSERT INTO PartForRepairBooking VALUES(" + partID + ", " + booking.getBookingID() + ", 0,0,1,0)";
        }
        
        Connection conn = DB.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.executeUpdate(addPartToBooking);
        } catch (SQLException ex) {
            Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
    }

    /**
     * sets the part home screen 
     * @param event The event that causes this method to run
     */
    @FXML
    private void partHome(ActionEvent event) {
        setPartScreen();
    }

    /**
     * Sets the part screen to be the home part screen, and then takes the user
     * to the bookings tab populated with bookings for the given vehicle.
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void viewBooking(ActionEvent event) {
        setPartScreen();
        goToBookingScreen();
    }
    
    public class rowForPartInBookingTable{
        private PartInBooking p;
        private BooleanProperty delete;
        
        public rowForPartInBookingTable(PartInBooking p){
            this.p = p;
            delete = new SimpleBooleanProperty(false);
        }
        
        public PartInBooking getPartInBoooking(){
            return p;
        }

        public String getPartName(){
            return p.getPart().getName();
        }
        
        public BooleanProperty installedProperty(){
            if(p.getPart().isInstalled()){
                return new SimpleBooleanProperty(true);
            }
            return new SimpleBooleanProperty(false);
        }
        
        public BooleanProperty repairProperty(){
            if(p.getRepair()){
                return new SimpleBooleanProperty(true);
            }
            return new SimpleBooleanProperty(false);
        }
        
        public BooleanProperty addNewProperty(){
            if(p.getAddNew()){
                return new SimpleBooleanProperty(true);
            }
            return new SimpleBooleanProperty(false);
        }
        
        public BooleanProperty SPCProperty(){
            if(p.getSPC()){
                return new SimpleBooleanProperty(true);
            }
            return new SimpleBooleanProperty(false);
        }
        
        public BooleanProperty deleteProperty(){
            return delete;
        }

    }
    
    /**
     * An alert asking the user a yes or no question.
     * 
     * @param title The title of the dialog
     * @param context The content of the dialog
     * @return true if the user selected yes, false if other
     */
    private static boolean showYesNo(String title, String context){
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType[] buttons = {yes, no};
        Alert alert = new Alert(Alert.AlertType.WARNING, context, buttons);
        alert.setHeaderText(title);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yes){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Shows a dialog with information for the user
     * @param title The title of the dialog 
     * @param content The content of the dialog
     */
    private static void showAlert(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public class rowForPartToAddTable{
        private PartType p;
        private BooleanProperty addNew, installed;
        
        public rowForPartToAddTable(PartType p){
            this.p = p;
            addNew = new SimpleBooleanProperty(false);
            installed = new SimpleBooleanProperty(false);
            String getInstalledParts = "SELECT PartID FROM PartInVehicle WHERE VehicleID=" + booking.getVehicleID();
            Connection conn = DB.getConnection();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(getInstalledParts);
                // if there is no result, there are no installed parts, so automatically installed is false
                if(!rs.isClosed()){
                    while(rs.next()){
                        // if an installed part has the same name as the PartType, that part type is installed, so installed true
                        Part part = new Part(rs.getInt("PartID"));
                        if(part.getName().equals(p.getName())){
                            installed = new SimpleBooleanProperty(true);
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        public PartType getPartType(){
            return p;
        }
        public String getPartName(){
            return p.getName();
        }
        
        public Double getCost(){
            return p.getCost();
        }
        
        public String getDescription(){
            return p.getDescription();
        }
        
        public BooleanProperty installedProperty(){
            return installed;
        }
        
        public BooleanProperty addNewProperty(){
            return addNew;
        }
        
    }
    
    /**
     * Sets the parent tab and the bookings tab to display part view properly and 
     * bookings tab where required. 
     * 
     * @param parent The parent tab for the part views
     * @param booking The tab that contains the booking views
     */
    public void setTabs(Tab parent, Tab booking){
        parentTab = parent;
        bookingsTab = booking;
    }
    
    /**
     * Gets the names of the parts installed in the given vehicle.
     * 
     * @return An arralist of strings, for the names of the installed parts 
     */
    private ArrayList<String> getNamesOfInstalledParts(){
        ArrayList<String> installedParts = new ArrayList<>();
        String getPartsInVehicle = "SELECT PartID FROM PartInVehicle WHERE VehicleID=" + booking.getVehicleID();
        
        Connection conn = DB.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(getPartsInVehicle);
            while(rs.next() && !rs.isClosed()){
                installedParts.add((new InstalledPart(rs.getInt("PartID"))).getPart().getName());
            }
        } catch (SQLException ex) {
             Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return installedParts;
    }
    
    /**
     * Gets a list of the installed parts for the vehicle being displayed 
     * 
     * @return An arralist containing the installed parts
     */
    private ArrayList<InstalledPart> getInstalledParts(){
 
        ArrayList<InstalledPart> installedParts = new ArrayList<>();
        String getPartsInVehicle = "SELECT PartID FROM PartInVehicle WHERE VehicleID=" + booking.getVehicleID();

        Connection conn = DB.getConnection();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(getPartsInVehicle);
            while(rs.next() && !rs.isClosed()){
                installedParts.add(new InstalledPart(rs.getInt("PartID")));
            }
        } catch (SQLException ex) {
             Logger.getLogger(PartsForBookingViewController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return installedParts;
    }
}
