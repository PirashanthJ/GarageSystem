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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import parts.logic.InstalledPart;
import parts.logic.Part;
import parts.logic.PartInventory;
import parts.logic.PartType;
import vehicles.logic.Vehicle;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class InstallPartNotThroughBookingController implements Initializable {

    @FXML
    private ChoiceBox<String> partChoice;
    @FXML
    private Label registrationNumberLabel;
    
    private Vehicle vehicle;
    private PartInventory inventory;
    private List<InstalledPart> partsInstalledAlready;
    
    @FXML
    private Label currentStockLabel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    /**
     * Sets the current stock textfield when the part to add stock to changes
     * @param name The name of the part to get stock for
     */
    private void setCurrentStock(String name){
        currentStockLabel.setText(String.valueOf(inventory.getStockLevel(name)));
    }
    
    /**
     * Initalizes the view with the vehicle given. 
     * 
     * @param vehicle
     * @param partsInstalledAlready 
     */
    public void initializeWithVehicle(Vehicle vehicle, ArrayList<InstalledPart> partsInstalledAlready){
        this.vehicle = vehicle;
        this.partsInstalledAlready = partsInstalledAlready;
        // shows registration number of vehicle part to be installed on
        registrationNumberLabel.setText(vehicle.getRegistration());
        
        inventory = PartInventory.getInstance();
        List<PartType> parts = inventory.getAllPartTypes();
        
        ArrayList<String> partInstalledNames = new ArrayList<>();
        for(int i = 0; i < partsInstalledAlready.size(); i++){
            partInstalledNames.add(partsInstalledAlready.get(i).getPart().getName());
        }
       
        ArrayList<String> partNamesPossibleToAdd = new ArrayList<>();
        for(int i = 0; i < parts.size(); i++){
            if(!partInstalledNames.contains(parts.get(i).getName())){
                partNamesPossibleToAdd.add(parts.get(i).getName());
            }
            
        }
        partChoice.setItems(FXCollections.observableArrayList(partNamesPossibleToAdd));
        partChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number value, Number newValue){
                setCurrentStock(partNamesPossibleToAdd.get(newValue.intValue()));
            }
        });
    }

    /**
     * Installs the part in the vehicle. If unsuccessful, it puts the part back in the inventory and 
     * displays an error message. 
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void install(ActionEvent event) {
        PartInventory inventory = PartInventory.getInstance();
        int partID = inventory.withdraw(partChoice.getSelectionModel().getSelectedItem());
        if(partID != -1){
            Part p = new Part(partID);
            boolean success = p.install(vehicle.getID());
            if(!success){
                p.putBackInInventory();
                showAlert("Can't Install Part", "This part cannot be installed because the max number of parts on a vehicle has been reached");
            }
        }else{ // there is no stock
            boolean yes = showYesNo("No Stock", "There is no stock of " + partChoice.getSelectionModel().getSelectedItem() + " in the inventory.\n" +
                    "More stock must be added before a it can be installed on a vehicle. Do you have stock to add?");
            if(yes){
                FXMLLoader loader = new FXMLLoader();
                Pane root = null;
                try {
                    root = loader.load(getClass().getResource("addStockView.fxml").openStream());
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
                stage.setTitle("Add Stock");
                stage.showAndWait();
            }
        }
        // gets the popup window and closes it once part added
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    /**
     * An alert giving the some information 
     * 
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
}
