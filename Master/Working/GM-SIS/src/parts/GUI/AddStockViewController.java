package parts.GUI;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import parts.logic.PartInventory;
import parts.logic.PartType;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class AddStockViewController implements Initializable {

    @FXML
    private ChoiceBox<String> partChoice;
    @FXML
    private TextField quantityText;
    @FXML
    private Label currentStockText;
    private PartInventory inventory = null;
    private String part = "";
    @FXML
    private Label addTitle;
    @FXML
    private Label quantityAdd;
    @FXML
    private Button addStock;
    @FXML
    private Button removeStock;
    @FXML
    private Label removeTitle;
    @FXML
    private Label quantityRemove;
    @FXML
    private Label quantityHelpText;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inventory = PartInventory.getInstance();
        List<PartType> parts = inventory.getAllPartTypes();
        ArrayList<String> partNames = new ArrayList<>();
        for(int i = 0; i < parts.size(); i++){
            partNames.add(parts.get(i).getName());
        }
        partChoice.setItems(FXCollections.observableArrayList(partNames));
        partChoice.getSelectionModel().selectFirst();
        setCurrentStock(partChoice.getSelectionModel().getSelectedItem());
        partChoice.getSelectionModel().selectedIndexProperty().addListener(
        new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number value, Number newValue){
                part = partNames.get(newValue.intValue());
                setCurrentStock(partNames.get(newValue.intValue()));
            }
        });
        
        FadeTransition fadeInQuantity = new FadeTransition(Duration.millis(3000), quantityHelpText);
        fadeInQuantity.setFromValue(0.0);
        fadeInQuantity.setToValue(1.0);
        FadeTransition fadeOutQuantity = new FadeTransition(Duration.millis(3000), quantityHelpText);
        fadeOutQuantity.setFromValue(1.0);
        fadeOutQuantity.setToValue(0.0);
        SequentialTransition fadeInThenOutQuantityHelp = new SequentialTransition(quantityHelpText, fadeInQuantity, fadeOutQuantity);
        
        quantityText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d+")) {
                    if(!newValue.equals("")){
                        quantityHelpText.setVisible(true);
                        fadeInThenOutQuantityHelp.play();
                    }
                    quantityText.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }
    
    /**
     * Sets this screen to be for adding stock or removing stock
     * 
     * @param add true to set the screen to add stock, false to set the screen to remove stock
     */
    public void setAdd(boolean add){
        addTitle.setVisible(add);
        removeTitle.setVisible(!add);
        addStock.setVisible(add);
        removeStock.setVisible(!add);
        quantityAdd.setVisible(add);
        quantityRemove.setVisible(!add);
    }

    /**
     * Sets the current stock textfield when the part to add stock to changes
     * @param name The name of the part to get stock for
     */
    private void setCurrentStock(String name){
        currentStockText.setText(String.valueOf(inventory.getStockLevel(name)));
    }
    
    /**
     * Adds stock to the inventory and closes the popup
     * @param event The event that causes this method to run.
    */
    @FXML
    private void addStock(ActionEvent event) {
        int quantity = 0;
        if(!quantityText.getText().equals("")){
            quantity = Integer.parseInt(quantityText.getText());
            inventory.addExistingPartTypeToInventory(partChoice.getSelectionModel().getSelectedItem(), quantity);
            
            // gets the popup window and closes it once part added
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Quantity Given");
            alert.setHeaderText(null);
            alert.setContentText("Please Give Quantity to Add");
            alert.showAndWait();
        }
    }

    /**
     * Remove stock from the inventory and close the popup
     * @param event The even that causes this method to run
     */
    @FXML
    private void removeStock(ActionEvent event) {
        int quantity = 0;
        if(!quantityText.getText().equals("")){
            quantity = Integer.parseInt(quantityText.getText());
            inventory.deleteQuantity(partChoice.getSelectionModel().getSelectedItem(), quantity);
            
            // gets the popup window and closes it once part added
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Quantity Given");
            alert.setHeaderText(null);
            alert.setContentText("Please Give Quantity to Add");
            alert.showAndWait();
        }
        
    }
    
}
