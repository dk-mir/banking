package dk.mir.banking.exceptions;
/**
 * The <code>BalanceChangeException</code> indicates that there is a problem with changing balance
 * of an account, i.e. some banking rules restrict withdraw-deposit operations.
 * @author Kalinin_DP
 *
 */
public class BalanceChangeException extends Exception {

	private static final long serialVersionUID = -2565997930502966328L;
	
	public BalanceChangeException(String message){
		super(message);
	}

}
