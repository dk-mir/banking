package dk.mir.banking.model;

import java.time.Instant;
import java.util.UUID;


/**
 * <strong>Thread safe and immutable</strong> class to keep bank transfer result.
 * @author Kalinin_DP
 *
 */
public class Transfer {
	
	private final String id = UUID.randomUUID().toString();
	private final Instant date = Instant.now();
	private final String fromAccountNum;
	private final String toAccountNum;
	private final long amount;
	public static enum RESULT{
		OK,
		FAILED,
		FAILED_NOT_ROLLBACKED
	}
	private final RESULT result;
	
	public Transfer(String fromAccountNum, String toAccountNum, long amount, RESULT result){
		this.fromAccountNum = fromAccountNum;
		this.toAccountNum = toAccountNum;
		this.amount = amount;
		this.result = result;
	}
	
	public String getId(){
		return id;
	}
	
	public Instant getDate(){
		return date;
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
	
	public RESULT getResult(){
		return result;
	}
}
