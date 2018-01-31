/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customers.logic;

/**
 *
 * @author ec15072
 */
public class TestClass {
    public static void main(String[] args){
        System.out.println("*********************");
        Bill bill = new Bill(20);
        bill.insertBill();
        System.out.println(bill.getCustomerid());
        System.out.println(bill.getVehicleid());
        System.out.println(bill.getDiagrepid());
        System.out.println(bill.getBill());
        System.out.println("*********************");
    }
}
