package dk.mir.banking;

import dk.mir.banking.model.FraudAttempt;

/**
 * @author Kalinin_DP
 *
 */
public interface IFraudManager {
	/**
	 * Detects fraudulent transfer. Bank fraud-detection manager defines procedures/instructions
	 * that security service applies/follows (i.e. security service will call this method
	 * to get logic for fraud detection).
	 * @param fromAccountNum
	 * @param toAccountNum
	 * @param amount
	 * @return <code>true</code> if fraudulent
	 * @throws InterruptedException
	 */
	boolean isFraud(String fromAccountNum, String toAccountNum, long amount) 
			throws InterruptedException;
	
	/**
	 * Security service calls this method when fraud is detected, and manager in bank
	 * should define how to handle it.
	 * @param fraudAttempt
	 */
	void handleFraud(FraudAttempt fraudAttempt);
}
