/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customers.logic;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author ec15072
 * Bill Class to display Bills and booking information from database
 */
public class BillRow {
    private final StringProperty vehicleReg;
    private final StringProperty startDate;
    private final StringProperty endDate;
    private final StringProperty warranty;
    private final DoubleProperty bill;
    private StringProperty status;
    private final int diagrepid;
    private final int customerid;
    
    public BillRow(String vehReg, String sdate, String edate, int inwarranty, double bil, int paid, int drid, int cid) {
        this.vehicleReg = new SimpleStringProperty(vehReg);
        this.startDate = new SimpleStringProperty(sdate);
        this.endDate = new SimpleStringProperty(edate);
        if (inwarranty == 1)
            this.warranty = new SimpleStringProperty("Valid");
        else
            this.warranty = new SimpleStringProperty("Expired");
        this.bill = new SimpleDoubleProperty(bil);
        if (paid == 1)
            this.status = new SimpleStringProperty("Paid");
        else
            this.status = new SimpleStringProperty("Outstanding");
        this.diagrepid = drid;
        this.customerid = cid;
    }

    public StringProperty VehicleRegProperty() {
        return vehicleReg;
    }

    public StringProperty StartDateProperty() {
        return startDate;
    }

    public StringProperty EndDateProperty() {
        return endDate;
    }

    public StringProperty WarrantyProperty() {
        return warranty;
    }

    public DoubleProperty BillProperty() {
        return bill;
    }

    public StringProperty StatusProperty() {
        return status;
    }
    
    public int getDiagRepID(){
        return diagrepid;
    }
    
    public int getCustomerID(){
        return customerid;
    }
    
    public void setStatus(String status){
        this.status = new SimpleStringProperty(status);
    }
}
