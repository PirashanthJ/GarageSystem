/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parts.GUI;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import parts.logic.PartInventory;
import parts.logic.PartType;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class SearchForPartsController implements Initializable {

    @FXML
    private TableColumn<PartType, String> nameColumn;
    @FXML
    private TableColumn<PartType, String> descriptionColumn;
    @FXML
    private TableColumn<PartType, Double> costColumn;
    @FXML
    private TableColumn<PartType, Integer> stockLevelColumn;
    @FXML
    private TableView<PartType> partTableView;
    @FXML
    private TextField searchInput;
    
    private Tab parentTab;
    private Tab bookingsTab;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        partTableView.setPlaceholder(new Label("Nothing to Display"));

        // Code by Alexander.Berg (StackOverFlow) - Detect doubleclock on row of TableView JavaFX
        // shows popup with part information on double click
        partTableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override 
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    FXMLLoader loader = new FXMLLoader();
                    Pane root = null;
                    try {
                        root = loader.load(getClass().getResource("editPartTypeView.fxml").openStream());
                    } catch (IOException ex) {
                        Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    EditPartTypeViewController control = (EditPartTypeViewController)loader.getController();
                    control.initializeTextViews(partTableView.getSelectionModel().getSelectedItem());
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                    Scene scene = new Scene(root);
                    stage.setResizable(false);
                    stage.setScene(scene);
                    stage.setTitle("Edit Part Information");
                    stage.showAndWait();
                }
            }
        });
    }    

    /**
     * Shows the view for the user to add a new part type 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void addNewPartType(ActionEvent event) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        try {
            stage.setResizable(false);
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("AddPartView.fxml")));
            stage.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        stage.setTitle("Add New Part Type");
        stage.showAndWait();
    }

    /**
     * Shows all parts in the inventory 
     * 
     * @param event The even that causes this method to run
     */
    @FXML
    private void showAllParts(ActionEvent event) {
        PartInventory inventory = PartInventory.getInstance();
        List<PartType> partTypes = inventory.getAllPartTypes();
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        stockLevelColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        partTableView.setItems(FXCollections.observableList(partTypes));
    }

    /**
     * shows the view to edit the part type 
     * 
     * @param event The event that causes this method to run
     */
    private void editPartType(ActionEvent event) {
        PartType partType = partTableView.getSelectionModel().getSelectedItem();
        if(partType != null){
            FXMLLoader loader = new FXMLLoader();
            Pane root = null;
            try {
                root = loader.load(getClass().getResource("editPartTypeView.fxml").openStream());
            } catch (IOException ex) {
                Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
            }
            EditPartTypeViewController control = (EditPartTypeViewController)loader.getController();
            control.initializeTextViews(partType);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node)event.getSource()).getScene().getWindow());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Edit Part Type");
            stage.showAndWait();
            // shows updated part
            this.showAllParts(null);
        }else{
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("No Part Type Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please Select a Part Type to Edit");
            alert.showAndWait();
        }
    }

    /**
     * Takes the user to the screen to view recent stock changes
     * 
     * @param event The event that causes this vehicle to run
     */
    @FXML
    private void showRecent(ActionEvent event) {
        // gets current stage
        Parent root;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("recentStockChangesView.fxml"));
        try {
            // load document I want to go to
            root = loader.load();
            RecentStockChangesViewController controller = loader.getController();
            controller.setTabs(parentTab, bookingsTab);
            parentTab.setContent(root);
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Shows the screen for the user to add more stock 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void addNewStock(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("addStockView.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        try {
            Scene scene = new Scene(loader.load());
            stage.setResizable(false);
            AddStockViewController controller = loader.getController();
            controller.setAdd(true);
            stage.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        stage.setTitle("Add Stock");
        stage.showAndWait();
        partTableView.refresh();
        //this.showAllParts(null);
    }

    /**
     * Searches for a part in the inventory by name
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void search(ActionEvent event) {
        PartInventory p = PartInventory.getInstance();
        
        String itemToSearchBy = searchInput.getText();
        List<PartType> parts = null;

        parts = p.searchForPartByName(itemToSearchBy);
       
        // updates table view
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        stockLevelColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        partTableView.setItems(FXCollections.observableList(parts));
        partTableView.setPlaceholder(new Label("No Results"));

        // clear search bar
        searchInput.clear();
    }

    /**
     * Takes the user to the page to search for installed parts 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void goToSearchPartsUsed(ActionEvent event) {
        Parent root;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("advancedSearchForUsedParts.fxml"));
        try {
            root = loader.load();
            AdvancedSearchForUsedPartsController controller = loader.getController();
            controller.setTabs(parentTab, bookingsTab);
            
            parentTab.setContent(root);
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
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

    /**
     * Displays the popup to delete stock 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void deleteStock(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("addStockView.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow());
        try {
            stage.setResizable(false);
            Scene scene = new Scene(loader.load());
            AddStockViewController controller = loader.getController();
            controller.setAdd(false);
            stage.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        stage.setTitle("Remove Stock");
        stage.showAndWait();
        partTableView.refresh();
        //this.showAllParts(null);
    }

    /**
     * Clears the table
     * @param event The event that causes this method to run
     */
    @FXML
    private void clear(ActionEvent event) {
        partTableView.setPlaceholder(new Label("Nothing to Display"));
        partTableView.getItems().clear();
    }
}
