package parts.GUI;

import customers.logic.Customer;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class CustomerDetailsViewController implements Initializable {

    @FXML
    private Label phoneNumberText;
    @FXML
    private Label typeText;
    @FXML
    private Label surnameText;
    @FXML
    private Label firstNameText;
    @FXML
    private Label emailText;
    @FXML
    private Label addressText;
    @FXML
    private Label postCodeText;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    /**
     * Sets all of the labels to show customer information for a given customer. 
     * IMPORTANT - must be called before switching the user to this screen. 
     * 
     * @param customer The customer with details to be shown 
     */
    public void initializeTextViews(Customer customer){
        phoneNumberText.setText(customer.getPhone());
        typeText.setText(customer.getCustomerType());
        surnameText.setText(customer.getSurname());
        firstNameText.setText(customer.getFirstname());
        emailText.setText(customer.getEmail());
        addressText.setText(customer.getAddress());
        postCodeText.setText(customer.getPostCode());
    }

    /**
     * Closes the popup 
     * @param event The event that causes this method to run
     */
    @FXML
    private void close(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
}
