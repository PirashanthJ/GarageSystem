package customers.logic;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property .IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
/**
 *
 * @author ec15072
 * Customer Class to create a Customer Object
 */
public class Customer {

    private final String firstname;
    private final String surname;
    private final String address;
    private final String postcode;
    private final String phone;
    private final String email;
    private final String customertype;
    private final int customerid;
    
    public Customer(int cid, String ctype, String fname, String sname, String adrs, String pcode, String phn, String eml) {
        customerid = cid;
        customertype = ctype;
	firstname = fname;
        surname = sname;
        address = adrs;
        postcode = pcode;
        phone = phn;
        email = eml;
    }
    
    public int getCustomerID() {
        return customerid;
    }
    
    public String getCustomerType() {
        return customertype;
    }
    
    public String getFirstname() {
        return firstname;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getPostCode() {
        return postcode;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public IntegerProperty customerIDProperty() {
        return new SimpleIntegerProperty(customerid);
    }
    
    public StringProperty customertypeProperty() {
        return new SimpleStringProperty(customertype);
    }
    
    public StringProperty firstnameProperty() {
        return new SimpleStringProperty(firstname);
    }
    
    public StringProperty surnameProperty() {
        return new SimpleStringProperty(surname);
    }
    
    public StringProperty addressProperty() {
        return new SimpleStringProperty(address);
    }
    
    public StringProperty postcodeProperty() {
        return new SimpleStringProperty(postcode);
    }
    
    public StringProperty phoneProperty() {
        return new SimpleStringProperty(phone);
    }
    
    public StringProperty emailProperty() {
        return new SimpleStringProperty(email);
    }
 }