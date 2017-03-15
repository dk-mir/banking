package dk.mir.banking.model;


import dk.mir.banking.exceptions.BalanceChangeException;

/**
 * <strong>Thread safe</strong> bank account.
 * @author Kalinin_DP
 *
 */
public class Account {
	private final String number;
	private volatile long balance;
	private volatile boolean isBlocked = false;
	private volatile long balanceLimit = 0;
	
	public Account(String number){
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public long getBalance() {
		return balance;
	}
	/**
	 * @param balanceLimit if positive - irreducible balance, negative - overdraft limit
	 */
	public void setBalanceLimit(long balanceLimit){
		this.balanceLimit = balanceLimit;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}
	/**
	 * Deposits the <code>amount</code>
	 * @param amount to be added to account's balance
	 * @return resulting account's balance
	 * @throws TransferException
	 * @throws IllegalArgumentException when <code>amount</code> is negative or zero 
	 */
	public synchronized long deposit(long amount) throws BalanceChangeException{
		if(amount <= 0) throw new IllegalArgumentException("Depositing negative or zero");
		if(isBlocked())
			throw new BalanceChangeException("Account "+number+"is blocked. Depositing restricted.");
		return balance += amount;
	}
	/**
	 * Withdraws the <code>amount</code>
	 * @param amount to be subtracted from the account's balance
	 * @return resulting account's balance
	 * @throws TransferException
	 * @throws IllegalArgumentException when <code>amount</code> is negative or zero 
	 */
	public synchronized long withdraw(long amount) throws BalanceChangeException{
		if(amount <= 0) throw new IllegalArgumentException("Withdrawing negative or zero");
		if(isBlocked())
			throw new BalanceChangeException("Account " + number + "is blocked. Withdrawal is denied.");
		long newBalance = balance - amount;
		if(newBalance < balanceLimit){
			throw new BalanceChangeException("Account " + number + " is going to hit balance limit ["+
					balanceLimit+"]. Withdrawal is denied.");
		}
		balance = newBalance;
		return balance;
	}
}
