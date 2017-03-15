package dk.mir.banking;

import dk.mir.banking.exceptions.BalanceChangeException;
import dk.mir.banking.generators.PaymentsGenerator;
import dk.mir.banking.model.Account;

/**
 * Static test class to initialize an instance of {@linkplain dk.mir.banking.Bank Bank}
 * and to be shared in JUnit test suite.
 * @author Kalinin_DP
 *
 */
public class BankSystem {
	////////defaults////////
	private static int TOTAL_ACCOUNTS = 1000;
	// 50 000 000
	private static long TOTAL_MONEY = 50000000;
	//Median account balance 50 000
	private static double MEDIAN_BALANCE = 50000f;
	////////end of defaults///////////
	//money owned by bank, initially it has all.
	private static long BANK_MONEY = TOTAL_MONEY;
	
	private static final Bank bank = new Bank();
	
	
	/**
	 * Initialize bank system with some parameters.
	 * @param moneyPile total amount of money in the bank system (on accounts + own)
	 * @param totalAccounts number of opened accounts
	 * @param medianBalance median balance on all accounts
	 * @throws BalanceChangeException hardly be thrown on initialization
	 */
	public static void init(long moneyPile, int totalAccounts, double medianBalance)
				throws BalanceChangeException{
		TOTAL_ACCOUNTS = totalAccounts;
		TOTAL_MONEY = moneyPile;
		MEDIAN_BALANCE = medianBalance;
		init();
	}
	/**
	 * Initialize with defaults.
	 * @throws BalanceChangeException hardly be thrown on initialization
	 */
	public static void init() throws BalanceChangeException{
		BANK_MONEY = TOTAL_MONEY;
		int accCounter = 0;
		while(accCounter < TOTAL_ACCOUNTS){
			accCounter ++;
			Account acc = bank.openAccount();
			//long accBalance = (long) ( medianBalance + random.nextGaussian() * balanceVariance );
			long accBalance = (long)(PaymentsGenerator.MONEY_DISTRIBUTION.sample() * MEDIAN_BALANCE);
			if(accBalance > 0 && BANK_MONEY - accBalance > 0){
				BANK_MONEY -= accBalance;
				acc.deposit(accBalance);
			}
		}
	}
	/**
	 * Get underlying bank from the bank system.
	 * @return the bank itself
	 */
	public static Bank getBank(){
		return bank;
	}
	/**
	 * @return bank's own money (amount)
	 */
	public static long getBankOwnMoney(){
		return BANK_MONEY;
	}
	/**
	 * @return median account's balance of money distribution
	 */
	public static double getMedianBalance(){
		return MEDIAN_BALANCE;
	}
	/**
	 * @return total amount of money in the bank system
	 */
	public static long getMoneyPile(){
		return TOTAL_MONEY;
	}
	/**
	 * @param amount to be added to TOTAL_MONEY
	 * @return update sum in TOTAL_MONEY
	 */
	public static long addCapitalization(long amount){
		return TOTAL_MONEY += amount;
	}
}
