/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diagrep.gui;


import diagrep.logic.BillForDiagRepair;
import java.net.URL;
import java.util.ArrayList;

import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import parts.logic.InstalledPart;


/**
 * FXML Controller class
 *
 * @author adamlaraqui
 */
public class ViewBookingBillController implements Initializable {
    
    @FXML
    private TextField totalCost;
    @FXML
    private Label hourlyLabourCost;
    @FXML
    private Label hoursWorked;
    @FXML
    private Label netLabourCost;
    @FXML
    private TableView<partRowForBill> partTable;
    @FXML
    private TableColumn<partRowForBill, String> partName;
    @FXML
    private TableColumn<partRowForBill, String> partWarrantyEndDate;
    @FXML
    private TableColumn<partRowForBill, String> partCost;
    @FXML
    private TableColumn<partRowForBill, String> costDeductions;
    @FXML
    private TableColumn<partRowForBill, Boolean> partInWarranty;
    
    /**
     * Initialises the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
    }
    
    public void setBill(BillForDiagRepair bill) {
        ArrayList<partRowForBill> rows = new ArrayList<>();
        
        for(int i = 0; i < bill.getAddedParts().size(); i++) {
            rows.add(new partRowForBill(bill.getAddedParts().get(i), true));
        }
        for(int i = 0; i < bill.getRepairParts().size(); i++) {
            rows.add(new partRowForBill(bill.getRepairParts().get(i), false));
        }
        
        partName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        partWarrantyEndDate.setCellValueFactory(new PropertyValueFactory<>("warrantyEndDate"));
        costDeductions.setCellValueFactory(new PropertyValueFactory<>("deduction"));
        partInWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyStatus"));
        partInWarranty.setCellFactory(column -> new CheckBoxTableCell());
        partCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        partTable.setItems(FXCollections.observableList(rows));
        
        totalCost.setText( "Current Cost: £"+String.format( "%.2f", bill.getCost( ) ) );
        hourlyLabourCost.setText("£"+String.format( "%.2f", bill.mechanicRate() ) );
        hoursWorked.setText(Integer.toString(bill.getHoursWorked()));
        netLabourCost.setText("£"+String.format( "%.2f", bill.netMechanicCost(bill.getHoursWorked() ) ) );
    }
    
    @FXML
    private void close(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    public class partRowForBill {
        private final InstalledPart part;
        private final String cost;
        private final boolean inWarranty;
        private String deduction = "";
        
        public partRowForBill(InstalledPart part, boolean useCost) {
            this.part = part;
            this.inWarranty = part.checkWarranty();
            if(useCost) {
                this.cost = "£"+String.format( "%.2f", part.getPart().getCost() );
                this.deduction = "No charge if replaced a part in warranty";
            } else {
                this.cost = "Time-based";
            }
        }
        
        public String getPartName() {
            return part.getPart().getName();
        }
        
        public String getWarrantyEndDate() {
            return part.getWarrantyEndDate();
        }
        
        public String getCost() {
            return cost;
        }
        
        public String getDeduction() {
            return this.deduction;
        }
        
        public BooleanProperty warrantyStatusProperty() {
            return new SimpleBooleanProperty(inWarranty);
        }
    }
}
