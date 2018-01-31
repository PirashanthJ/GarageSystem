/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parts.GUI;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import parts.logic.InstalledPart;
import parts.logic.PartInventory;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class AdvancedSearchForUsedPartsController implements Initializable {

    @FXML
    private TextField searchText;
    @FXML
    private TableView<PartForAdvancedSearchTable> partTableView;
    @FXML
    private TableColumn<PartForAdvancedSearchTable, String> partNameColumn;
    @FXML
    private TableColumn<PartForAdvancedSearchTable, String> vehicleRegistrationColumn;
    @FXML
    private TableColumn<PartForAdvancedSearchTable, String> customerNameColumn;

    @FXML
    private ChoiceBox<String> searchByChoice;
    @FXML
    private TextField lastNameText;
    
    private Tab parentTab;
    private Tab bookingsTab;
    @FXML
    private TextField firstNameText;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        partTableView.setPlaceholder(new Label("Nothing to Display"));
        searchByChoice.setItems(FXCollections.observableArrayList("Vehicle Registration", "Customer Name"));
        searchByChoice.getSelectionModel().selectFirst();
        searchByChoice.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>(){
                    @Override
                    public void changed(ObservableValue ov, Number value, Number newValue){
                        if(newValue.intValue() == 1){
                            searchText.clear();
                            searchText.setVisible(false);
                            firstNameText.setVisible(true);
                            lastNameText.setVisible(true);
                        }else{
                            firstNameText.setVisible(false);
                            firstNameText.clear();
                            lastNameText.clear();
                            lastNameText.setVisible(false);
                            searchText.setVisible(true);

                        }
                    }
                });
        // Code by Alexander.Berg (StackOverFlow) - Detect doubleclock on row of TableView JavaFX
        // shows popup with part information on double click
        partTableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override 
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    PartForAdvancedSearchTable rowSelected = partTableView.getSelectionModel().getSelectedItem();
                    InstalledPart part = rowSelected.getInstalledPart();
                    FXMLLoader loader = new FXMLLoader();
                    Pane root = null;
                    try {
                        root = loader.load(getClass().getResource("partUsedDetails.fxml").openStream());
                    } catch (IOException ex) {
                        Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    PartUsedDetailsController control = (PartUsedDetailsController)loader.getController();
                    control.initializeTextViews(part);
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.setTitle("Installed Part Details");
                    stage.showAndWait();
                }
            }});
        // Code by ItachiUchiha (StackOverflow) - JavaFX TextField : Automatically transform text to uppercase
        searchText.textProperty().addListener((ov, oldValue, newValue) -> {
            searchText.setText(newValue.toUpperCase());
        });
    }
        
        
    /**
     * Takes the user to the part home screen which is the search for parts in inventory page.
     * @param event Event that causes this method to run
     */
    @FXML
    private void goHome(ActionEvent event) {
        // gets current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("searchForParts.fxml"));
        try {
            // load document I want to go to
            root = loader.load();
            SearchForPartsController controller = loader.getController();
            controller.setTabs(parentTab, bookingsTab);
            parentTab.setContent(root);
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Searches for a part by vehicle registration number or customer name, depending on the options selected
     * on the GUI
     * 
     * @param event Event that causes this method to run
     */
    @FXML
    private void search(ActionEvent event) {
        String searchBy = searchByChoice.getValue();
        String searchFor = searchText.getText();
        List<PartForAdvancedSearchTable> partsToDisplay = new ArrayList<>(); 
        if(!searchFor.trim().equals("") || (!lastNameText.getText().trim().equals("") || !firstNameText.getText().trim().equals(""))){
            PartInventory partInventory = PartInventory.getInstance();
            List<InstalledPart> parts;

            String firstName, lastName;
            if(searchBy.equals("Vehicle Registration")){
                parts = partInventory.searchForPart(searchFor);
            }else{
                // search by Customer Name
                firstName = firstNameText.getText().trim();
                lastName = lastNameText.getText().trim();
                System.out.println(firstName + " " + lastName);
                parts = partInventory.searchForPart(firstName, lastName);
            }
            for(int i = 0; i < parts.size(); i++){
                partsToDisplay.add(new PartForAdvancedSearchTable(parts.get(i)));
            }
        }
        vehicleRegistrationColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleRegistration"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        if(!partsToDisplay.isEmpty()){
            partTableView.setItems(FXCollections.observableList(partsToDisplay));
        
        }else{
            partTableView.setItems(FXCollections.observableList(partsToDisplay));
            partTableView.setPlaceholder(new Label("No Search Results"));
        }
        // display results in table
        searchText.clear();
        lastNameText.clear();
        firstNameText.clear();

    }

    /**
     * Goes to the view that only shows parts for a specific vehicle, with additional 
     * options for that vehicle. 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void showPartsForVehicle(ActionEvent event) {
        PartForAdvancedSearchTable rowSelected = partTableView.getSelectionModel().getSelectedItem();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("showPartsInVehicle.fxml"));
        Parent root;
        if(rowSelected != null){
            try {
                root = loader.load();
                ShowPartsInVehicleController control = (ShowPartsInVehicleController)loader.getController();
                control.initializeView(rowSelected.getInstalledPart().getVehicle());
                control.setTabs(parentTab, bookingsTab);
                parentTab.setContent(root);
            } catch (IOException ex) {
                Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        }else{
            try {
                root = loader.load();
                ShowPartsInVehicleController control = (ShowPartsInVehicleController)loader.getController();
                control.setTabs(parentTab, bookingsTab);
                parentTab.setContent(root);
            } catch (IOException ex) {
                Logger.getLogger(AdvancedSearchForUsedPartsController.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }

    }

    /**
     * Clears the tableview. 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void clear(ActionEvent event) {
        partTableView.setPlaceholder(new Label("Nothing to Display"));
        partTableView.getItems().clear();
    }
    
    /**
     * Class to create objects to fill the table view
     */
    public class PartForAdvancedSearchTable{
        private final InstalledPart installedPart;

        public PartForAdvancedSearchTable(InstalledPart p){
            installedPart = p;
        }
        
        public InstalledPart getInstalledPart(){
            return this.installedPart;
        }
        
        public String getVehicleRegistration(){
            return this.installedPart.getVehicle().getRegistration();
        }
        
        public String getCustomerName(){
            return this.installedPart.getOwnerFirstName() + " " + this.installedPart.getOwnerLastName();
        }
        
        public String getPartName(){
            return this.installedPart.getPart().getName();
        }
        
    }
    
    /**
     * Sets the parent tab and the bookings tab to display part view properly and 
     * bookings tab where required. 
     * 
     * @param parent The parent tab for the part views
     * @param bookings The tab that contains the booking views
     */
    public void setTabs(Tab parent, Tab bookings){
        parentTab = parent;
        bookingsTab = bookings;
    }
    
}
