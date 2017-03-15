package dk.mir.banking.model;

import java.util.function.Consumer;

import dk.mir.banking.functions.Function3;
/**
 * <p><strong>Thread safe and immutable</strong> class describing how to implement
 * fraud detection over specified <code>transfer</code>.</p>
 * <p>Each operation can hold independent reference to fraud detection procedure
 * and how to handle situation when transfer is fraudulent.</p>
 * @author Kalinin_DP
 *
 */
public class FraudDetectOperation {
	private final Transfer transfer;
	private final Function3<String, String, Long, Boolean> fraudDetectFunction;
	private final Consumer<FraudAttempt> fraudHandleFunction;
	/**
	 * @param transfer to be checked for fraud
	 * @param fraudDectectFunction assigned function to perform fraud detection
	 * @param fraudHandleFunction assigned function to handle fraudulent <code>transfer</code>
	 */
	public FraudDetectOperation(Transfer transfer, 
			Function3<String, String, Long, Boolean> fraudDectectFunction,
			Consumer<FraudAttempt> fraudHandleFunction){
		this.transfer = transfer;
		this.fraudDetectFunction = fraudDectectFunction;
		this.fraudHandleFunction = fraudHandleFunction;
	}

	public Transfer getTransfer() {
		return transfer;
	}

	public Function3<String, String, Long, Boolean> getFraudDetectFunction() {
		return fraudDetectFunction;
	}

	public Consumer<FraudAttempt> getFraudHandleFunction() {
		return fraudHandleFunction;
	}
}
