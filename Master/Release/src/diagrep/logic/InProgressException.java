package diagrep.logic;

/**
 * This exception is used when either form of booking, DiagRepair or SPC is being cancelled/deleted while in progress.
 * We use this exception just so we know to warn the user of what their action will do.
 * A confirmation box should then be shown, and if agreed with, should force the action anyway.
 * @author adamlaraqui
 */

// Class by Adamski on StackOverflow - How to create custom exceptions in Java?
public class InProgressException extends Exception {
    
    public InProgressException() {
        super();
    }
    
    public InProgressException(String message) {
        super(message);
    }

}
