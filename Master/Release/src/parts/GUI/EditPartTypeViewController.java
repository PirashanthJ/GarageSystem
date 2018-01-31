/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
public class EditPartTypeViewController implements Initializable {

    @FXML
    private TextField nameText;
    @FXML
    private TextField costText;
    @FXML
    private TextArea descriptionText;
    @FXML
    private Button updateConfirmButton;
    
    private String oldName = "";
    private double oldCost = 0.0;
    private String oldDescription = "";
    @FXML
    private Label costHelpText;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FadeTransition fadeInCost = new FadeTransition(Duration.millis(3000), costHelpText);
        fadeInCost.setFromValue(0.0);
        fadeInCost.setToValue(1.0);
        FadeTransition fadeOutCost = new FadeTransition(Duration.millis(3000), costHelpText);
        fadeOutCost.setFromValue(1.0);
        fadeOutCost.setToValue(0.0);
        SequentialTransition fadeInThenOutCostHelp = new SequentialTransition(costHelpText, fadeInCost, fadeOutCost);
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
    }    
    
    /**
     * Initializes the view with the current details of the part 
     * 
     * IMPORTANT - must be called before the view is shown
     * @param partType The part type to edit
     */
    public void initializeTextViews(PartType partType){
        oldName = partType.getName();
        oldCost = partType.getCost();
        oldDescription = partType.getDescription();
        nameText.setText(oldName);
        costText.setText(String.valueOf(oldCost));
        descriptionText.setText(oldDescription);
    }

    /**
     * Updates part name, description, and cost if changed and closes the window
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void updatePart(ActionEvent event) {
        PartInventory inventory = PartInventory.getInstance();
        // must change cost/description first if necessary because method relies
        // on name of part type
        if(!costText.getText().trim().equals(String.valueOf(oldCost)) && !costText.getText().trim().equals("")){
            inventory.changePartTypeCost(oldName, Double.parseDouble(costText.getText().trim()));
        }else if(costText.getText().trim().equals("")){
            costText.setText(String.valueOf(oldCost));
            showAlert("No Cost", "This part type has no cost, please input a cost.");
            return;
        }
        
        if(!descriptionText.getText().trim().equals(oldDescription) && !descriptionText.getText().trim().equals("")){
            inventory.changePartTypeDescription(oldName, descriptionText.getText().trim());
        }else if(descriptionText.getText().trim().equals("")){
            descriptionText.setText(oldDescription);
            showAlert("No Cost", "This part type has no description, please input a description.");
            return;
        }
        
        if(!nameText.getText().trim().equals(oldName) && !nameText.getText().trim().equals("")){
            inventory.changePartTypeName(oldName, nameText.getText().trim());
        }else if(nameText.getText().trim().equals("")){
            nameText.setText(oldName);
            showAlert("No Name", "This part type has no name, please input a name.");
            return;
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
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
}
