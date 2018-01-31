package customers.gui;

import common.Database;
import customers.logic.Customer;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author ec15072
 */
public class AddEditCustomersController implements Initializable {
    
    @FXML
    private RadioButton privateChoice;
    @FXML
    private RadioButton businessChoice;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;
    @FXML
    private TextField fnameBox;
    @FXML
    private TextField snameBox;
    @FXML
    private TextField addBox;
    @FXML
    private TextField pcodeBox;
    @FXML
    private TextField phoneBox;
    @FXML
    private TextField emailBox;
    @FXML
    private Label title;
    
    private static final Database DB = Database.getInstance(); 
    private final ToggleGroup custypegroup = new ToggleGroup();
    private boolean customerExists = false;
    private Customer customer;
    private int customerID;
    private String customertype;
    private String firstname;
    private String surname;
    private String address;
    private String postcode;
    private String phone;
    private String email;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        privateChoice.setToggleGroup(custypegroup);
        privateChoice.setUserData("Private");
        businessChoice.setToggleGroup(custypegroup);
        businessChoice.setUserData("Business");
        privateChoice.setSelected(true);
        
        fnameBox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression
                if (!newValue.matches("[\\p{L} .'-]")) {
                    fnameBox.setText(newValue.replaceAll("[^\\p{L} .'-]", ""));
                }
                if (fnameBox.getText().length() > 35) {
                String s = fnameBox.getText().substring(0, 35);
                fnameBox.setText(s);
                }
            }
        });
        
        snameBox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression
                if (!newValue.matches("[\\p{L} .'-]")) {
                    snameBox.setText(newValue.replaceAll("[^\\p{L} .'-]", ""));
                }
                if (snameBox.getText().length() > 35) {
                String s = snameBox.getText().substring(0, 35);
                snameBox.setText(s);
                }
            }
        });
        
        addBox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression
                if (addBox.getText().length() > 254) {
                String s = addBox.getText().substring(0, 254);
                addBox.setText(s);
                }
            }
        });
        
        pcodeBox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression
                if (!newValue.matches("\\d\\w\\s")) {
                    pcodeBox.setText(newValue.replaceAll("[^\\d\\w\\s]", ""));
                }
                if (pcodeBox.getText().length() > 8) {
                String s = pcodeBox.getText().substring(0, 8);
                pcodeBox.setText(s);
                }
            }
        });
        
        phoneBox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // code by Jimmy (StackOverflow) Simple regular expression for decimal with precision of 2
                if (!newValue.matches("\\d")) {
                    phoneBox.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (phoneBox.getText().length() > 11) {
                String s = phoneBox.getText().substring(0, 11);
                phoneBox.setText(s);
                }
            }
        });
        
        emailBox.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (emailBox.getText().length() > 254) {
                String s = emailBox.getText().substring(0, 254);
                emailBox.setText(s);
                }
            }
        });
    }    
    
    //sets the title of the form
    public void setTitle(String titletext) {
        title.setText(titletext);
        if (titletext.equals("Edit Customer"))
            customerExists = true;
    }
    
    //method to send customer object from another controller to show their details when editing
    public void sendCustomerData(Customer cust){
        customer = cust;
        customertype = customer.getCustomerType();
        firstname = customer.getFirstname();
        surname = customer.getSurname();
        address = customer.getAddress();
        postcode = customer.getPostCode();
        phone = customer.getPhone();
        email = customer.getEmail();
        customerID = customer.getCustomerID();
        
        if (customertype.equals("Private"))
            privateChoice.setSelected(true);
        else
            businessChoice.setSelected(true);
        fnameBox.setText(firstname);
        snameBox.setText(surname);
        addBox.setText(address);
        pcodeBox.setText(postcode);
        phoneBox.setText(phone);
        emailBox.setText(email);
    }
    
    //method to add customer details in database
    public void addCustomer(){
        try {
        Connection conn = DB.getConnection();
        String query = "INSERT INTO `Customer` VALUES(null, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, customertype);
        ps.setString(2, firstname);
        ps.setString(3, surname);
        ps.setString(4, address);
        ps.setString(5, postcode.toUpperCase());
        ps.setString(6, phone);
        ps.setString(7, email);
        ps.executeUpdate();
        Alert successalert = new Alert(Alert.AlertType.INFORMATION);
        successalert.setTitle("Success");
        successalert.setHeaderText(null);
        successalert.setContentText("Customer account was successfully added");
        successalert.showAndWait();
        cancelButton.getScene().getWindow().hide();
        } catch (SQLException ex){
            Alert successalert = new Alert(Alert.AlertType.ERROR);
            successalert.setTitle("Error");
            successalert.setHeaderText("Customer account was not added.");
            successalert.setContentText("Customer with that Email or Phone Number already exists");
            successalert.showAndWait();
            System.err.println("Unable to Add Customer: "+ex);
        }
    }
    
    //method to update customer table in database 
    public void editCustomer(){
        try {
        Connection conn = DB.getConnection();
        String query = "UPDATE `Customer` "
                     + "SET CustomerType = ?, "
                     + "Firstname = ?, "
                     + "Surname = ?, "
                     + "Address = ?, "
                     + "Postcode = ?, "
                     + "Phone = ?, "
                     + "Email = ? "
                     + "WHERE CustomerID = ?;";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, customertype);
        ps.setString(2, firstname);
        ps.setString(3, surname);
        ps.setString(4, address);
        ps.setString(5, postcode);
        ps.setString(6, phone);
        ps.setString(7, email);
        ps.setInt(8, customerID);
        ps.executeUpdate();
        Alert successalert = new Alert(Alert.AlertType.INFORMATION);
        successalert.setTitle("Success");
        successalert.setHeaderText(null);
        successalert.setContentText("Customer account was successfully edited");
        successalert.showAndWait();
        cancelButton.getScene().getWindow().hide();
        }
        catch (SQLException ex){
            Alert successalert = new Alert(Alert.AlertType.ERROR);
            successalert.setTitle("Error");
            successalert.setHeaderText("Customer account was not edited.");
            successalert.setContentText("Customer with that Email or Phone Number already exists");
            successalert.showAndWait();
            System.err.println("Error Editing Customer: "+ex);
        }
    }
    
    //handles save button pressed, initliases customer details from boxes
    public void saveCustomerDetails(ActionEvent event){
        String errormsg = validateDetails();
        if (!errormsg.equals("")){
            Alert successalert = new Alert(Alert.AlertType.ERROR);
            successalert.setTitle("Invalid Customer Details");
            successalert.setHeaderText("Please fix the following before continuing.");
            successalert.setContentText(errormsg);
            successalert.showAndWait();
            return;
        }
        
        if (custypegroup.getSelectedToggle().getUserData().toString().equals("Private"))
            customertype = "Private";
        else
            customertype = "Business";
        firstname = fnameBox.getText();
        surname = snameBox.getText();
        address = addBox.getText();
        postcode = pcodeBox.getText();
        phone = phoneBox.getText();
        email = emailBox.getText();
        
        if (customerExists)
            editCustomer();
        else
            addCustomer();

        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    //handles cancel button press
    public void cancel(ActionEvent Event){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    //generates a validation message based on incorrect input
    private String validateDetails(){
        String errormsg = "";
        if (fnameBox.getText().trim().isEmpty())
            errormsg += "\n-First Name cannot be blank";
        if (snameBox.getText().trim().isEmpty())
            errormsg += "\n-Surname cannot be blank";
        if (addBox.getText().trim().isEmpty())
            errormsg += "\n-Address cannot be blank";
        if (pcodeBox.getText().trim().isEmpty())
            errormsg += "\n-Postcode cannot be blank";
        else if (pcodeBox.getText().trim().length() < 5)
            errormsg += "\n-Invalid Postcode (Must have at least 5 Characters)";
        if (phoneBox.getText().trim().isEmpty())
            errormsg += "\n-Phone number cannot be blank";
        else if (phoneBox.getText().length() < 8)
            errormsg += "\n-Invalid UK Phone number (Must be at least 8 digits)";
        if (emailBox.getText().trim().isEmpty())
            errormsg += "\n-Email cannot be blank";
        else if (!validEmail())
            errormsg += "\n-Invaid email format";
        
        return errormsg;
        
    }
    
    /*Method that checks if valid email has been entered
    Reference:
    https://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/ */
    private boolean validEmail() {
	Pattern p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\-[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	Matcher m =  p.matcher(emailBox.getText());
	return m.matches();
    }
}
