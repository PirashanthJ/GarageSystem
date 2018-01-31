/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customers.logic;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author ec15072
 * Class to display past and future bookings
 */
public class BookingDetails {
        private final StringProperty VehicleReg;
        private final StringProperty startDate;
        private final StringProperty endDate;

        public BookingDetails(String vehReg, String startDate, String endDate) {
            this.VehicleReg = new SimpleStringProperty(vehReg);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
        }
        
        public StringProperty VehRegProperty() { 
            return VehicleReg; 
        }
        
        public StringProperty StartDateProperty() { 
            return startDate; 
        }
        
        public StringProperty EndDateProperty() { 
            return endDate; 
        }
        
    }
    
