package diagrep.logic;

import java.util.ArrayList;
import parts.logic.InstalledPart;

/**
 *
 * @author adamlaraqui
 */
public class BillForDiagRepair {
    private ArrayList<InstalledPart> addedParts = new ArrayList<>();
    private ArrayList<InstalledPart> repairOnlyParts = new ArrayList<>();
    private double totalCostAddedParts = 0;
    private final double mechanicRate;
    private int hoursWorked;
    private double currentCost;

    public BillForDiagRepair(double rate) {
        this.mechanicRate = rate;
    }

    public void addInstalledPart(InstalledPart part) {
        addedParts.add(part);
        totalCostAddedParts += part.getPart().getCost();
    }

    public void addRepairedPart(InstalledPart part) {
        repairOnlyParts.add(part);
    }

    public double netInstalledPartsCost() { return totalCostAddedParts; }
    public double mechanicRate() { return this.mechanicRate; }
    public double netMechanicCost(int repairTime) { return (mechanicRate*repairTime); }
    public ArrayList<InstalledPart> getAddedParts() { return addedParts; }
    public ArrayList<InstalledPart> getRepairParts() { return repairOnlyParts; }
    public int getHoursWorked() { return this.hoursWorked; }
    public void setHoursWorked(int hours) { this.hoursWorked = hours; }
    public double getCost() { return currentCost; }
    public void setCost(double cost) { currentCost = cost; }
    public void resetBill() { addedParts.clear(); repairOnlyParts.clear(); totalCostAddedParts=0; }
}