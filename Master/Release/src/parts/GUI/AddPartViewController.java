package parts.GUI;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import parts.logic.PartInventory;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class AddPartViewController implements Initializable {

    @FXML
    private TextField nameText;
    @FXML
    private TextField costText;
    @FXML
    private TextArea descriptionText;
    
    private PartInventory partInventory;
    @FXML
    private Label costHelpText;
    
    @FXML
    private TextField quantityText;
    @FXML
    private Label quantityHelpText;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        partInventory = PartInventory.getInstance();

        FadeTransition fadeInCost = new FadeTransition(Duration.millis(3000), costHelpText);
        fadeInCost.setFromValue(0.0);
        fadeInCost.setToValue(1.0);
        FadeTransition fadeOutCost = new FadeTransition(Duration.millis(3000), costHelpText);
        fadeOutCost.setFromValue(1.0);
        fadeOutCost.setToValue(0.0);
        SequentialTransition fadeInThenOutCostHelp = new SequentialTransition(costHelpText, fadeInCost, fadeOutCost);
        
        FadeTransition fadeInQuantity = new FadeTransition(Duration.millis(3000), quantityHelpText);
        fadeInQuantity.setFromValue(0.0);
        fadeInQuantity.setToValue(1.0);
        FadeTransition fadeOutQuantity = new FadeTransition(Duration.millis(3000), quantityHelpText);
        fadeOutQuantity.setFromValue(1.0);
        fadeOutQuantity.setToValue(0.0);
        SequentialTransition fadeInThenOutQuantityHelp = new SequentialTransition(quantityHelpText, fadeInQuantity, fadeOutQuantity);
        
        // Code by Evan Knowles (StackOverflow) What is the recommended way to make a numeric TextField in JavaFX?
        // force the field to only allow decimals
        costText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression for decimal with precision of 2
                if (!newValue.matches("(\\d*)|(\\d+\\.\\d{0,2})")) {
                    costHelpText.setVisible(true);
                    fadeInThenOutCostHelp.play();
                    costText.setText(oldValue);
                }
            }
        });
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
     * Adds a new part type to the inventory with the given name, description, cost, and quantity
     * 
     * @param event Event that causes method to run
     */
    @FXML
    private void addNewPartType(ActionEvent event) {
        String name = nameText.getText();
        name = name.trim();
        if(name.equals("")){
            showAlert("No Name", "This part type has no name, please input a name");
            return;
        }
        
        String costText = this.costText.getText();
        costText = costText.trim();
        if(costText.equals("")){
            showAlert("No Cost", "This part type has no cost, please input a cost.");
            return;
        }
        
        String quantityText = this.quantityText.getText();
        quantityText = quantityText.trim();
        if(quantityText.equals("")){
            showAlert("No Quantity", "This part type has no quantity, please input a quantity.");
            return;
        }
        
        String description = descriptionText.getText();
        description = description.trim();
        if(description.equals("")){
            showAlert("No Description", "This part type has no decription, please input a description.");
            return;
        }
        
        double cost = Double.parseDouble(costText);
        int quantity = Integer.parseInt(quantityText);
        boolean success = partInventory.addNewPartTypeToInventory(name, 
                description, cost, quantity);
        if(success){
            showAlert("Part Type Sucessfully Added", "The new part type was sucessfully added");
        }else{
            showAlert("Part Type Already Exists", "This part already exists. If you would like to add stock, please select \"Add Stock\" on the righthand side of the screen.\n" 
                    + "If you would like to edit this part, please double click it in the table.");
        }
        // gets the popup window and closes it once part added
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    /**
     * Shows an alert dialog box
     * 
     * @param title The title of the dialog
     * @param description The description in the dialog
     */
    private void showAlert(String title, String description){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(description);
        alert.showAndWait();
    }
    
}
