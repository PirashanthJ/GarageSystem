package parts.GUI;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import parts.logic.InstalledPart;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class PartUsedDetailsController implements Initializable {

    @FXML
    private Label partNameText;
    @FXML
    private Label vehicleRegText;
    @FXML
    private Label customerNameText;
    @FXML
    private Label installDateText;
    @FXML
    private Label warEndDateText;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    /**
     * Closes the popup 
     * @param event The event that causes this method to run
     */
    @FXML
    private void close(ActionEvent event) {
        // gets the popup window and closes it once part added
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    /**
     * Shows the details of the installed part 
     * 
     * IMPORTANT - must be called before this view is shown
     * @param part The part to show the details of
     */
    public void initializeTextViews(InstalledPart part){
        partNameText.setText(part.getPart().getName());
        vehicleRegText.setText(part.getVehicle().getRegistration());
        customerNameText.setText(part.getOwnerFirstName() + " " + part.getOwnerLastName());
        installDateText.setText(part.getInstallDate());
        warEndDateText.setText(part.getWarrantyEndDate());
    }
    
}
