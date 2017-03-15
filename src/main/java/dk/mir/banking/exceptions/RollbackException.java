package dk.mir.banking.exceptions;

import dk.mir.banking.model.Transfer;
/**
 * The <code>RollbackException</code> indicates that failed transfer is also failed to roll back,
 * i.e. bank has failed to return money to payer &ndash; the sample case when a payer's account has been
 * blocked while rolling back failed {@linkplain Transfer transfer}.
 * @author Kalinin_DP
 *
 */
public class RollbackException extends Exception {
	private static final long serialVersionUID = -5884396665435958762L;

	private final Transfer transfer;
	
	public RollbackException(Transfer transfer){
		this.transfer = transfer;
	}
	
	public Transfer getTransfer(){
		return transfer;
	}

}
