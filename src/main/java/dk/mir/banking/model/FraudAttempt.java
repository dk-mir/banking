package dk.mir.banking.model;

import java.time.Instant;

/**
 * <strong>Thread safe and immutable</strong> class to keep detected fraud attempt.
 * @author Kalinin_DP
 *
 */
public class FraudAttempt {
	
	private final String fromAccountNum;
	private final String toAccountNum;
	private final long amount;
	private final Instant date;
	
	public FraudAttempt(String fromAccountNum, String toAccountNum, long amount){
		this.fromAccountNum = fromAccountNum;
		this.toAccountNum = toAccountNum;
		this.amount = amount;
		this.date = Instant.now();
	}

	public String getFromAccountNum() {
		return fromAccountNum;
	}

	public String getToAccountNum() {
		return toAccountNum;
	}

	public long getAmount() {
		return amount;
	}
	
	public Instant getDate(){
		return date;
	}
}
