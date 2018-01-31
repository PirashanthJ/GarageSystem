/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diagrep.gui;


import common.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ResourceBundle;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;


/**
 * FXML Controller class
 *
 * @author adamlaraqui
 */
public class ViewCustomerController implements Initializable {
    
    @FXML
    private Label firstName;
    @FXML
    private Label lastName;
    @FXML
    private Label type;
    @FXML
    private Label address;
    @FXML
    private Label postCode;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    
    /**
     * Initialises the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
    }
    
    public void setCustomer(int customerID) throws SQLException {
        Database db = Database.getInstance();
        Connection con = db.getConnection();
        String getCustomerQuery = "SELECT * FROM Customer WHERE CustomerID = "+customerID;
        PreparedStatement ps = con.prepareStatement(getCustomerQuery);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            firstName.setText(rs.getString("Firstname"));
            lastName.setText(rs.getString("Surname"));
            type.setText(rs.getString("CustomerType"));
            address.setText(rs.getString("Address"));
            postCode.setText(rs.getString("Postcode"));
            phone.setText(rs.getString("Phone"));
            email.setText(rs.getString("Email"));
        }
    }
}
