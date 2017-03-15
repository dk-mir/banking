package dk.mir.banking;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import dk.mir.banking.exceptions.RollbackException;
import dk.mir.banking.model.Account;
import dk.mir.banking.model.FraudAttempt;
import dk.mir.banking.model.Transfer;

public class Bank implements IFraudManager {
	private final Random random = new Random();	
	protected Map<String, Account> accounts = new HashMap<>();	
	private List<FraudAttempt> frauds = new LinkedList<>();	
	private ConcurrentMap<String, Transfer> transfers = new ConcurrentHashMap<>();
	// return money to fromAccounts manually if they get blocked while rolling back transaction
	private Map<String, Transfer> manualRollbacks = new ConcurrentHashMap<>();
	
	private ClearingSystem clearingSystem = new ClearingSystem();
	private volatile Optional<SecurityService> securityServiceOp = Optional.empty();
	private volatile Optional<IFraudManager> fraudManagerOp = Optional.of(this);

	
	///////Bank main functions///////////
	public void transfer(String fromAccountNum, String toAccountNum, long amount) throws Exception{
		if(fromAccountNum == null || toAccountNum == null) 
			throw new IllegalArgumentException("Invalid transfer (NULL account): " + 
					fromAccountNum + " => " + toAccountNum);
		
		if(fromAccountNum.equals(toAccountNum))
			throw new IllegalArgumentException("Both accounts are the same: " + fromAccountNum);
		
		if(amount <= 0) throw new IllegalArgumentException("Invalid transfer amount: 0 or negative");

		Account fromAccount = accounts.get(fromAccountNum);
		Account toAccount = accounts.get(toAccountNum);
		
		if(fromAccount == null) throw new IllegalArgumentException("Non-existent fromAccount: " +
				fromAccountNum);
		
		if(toAccount == null) throw new IllegalArgumentException("Non-existent toAccount: " +
				toAccountNum);
			
		try{
			Transfer transfer = clearingSystem.commitTransfer(fromAccount, toAccount, amount);
			Transfer previousTransfer = transfers.putIfAbsent(transfer.getId(), transfer);
			if(previousTransfer!=null){
				System.err.println("UUID collision - VERY strange");
			}
			securityServiceOp.ifPresent( securityService -> {
				if(transfer.getResult() == Transfer.RESULT.OK &&
						transfer.getAmount() > 50000){
					//security service expects transfer, isFraud function implementation
					// and handleFraud
					fraudManagerOp.ifPresent( fraudManager -> 
						securityService.checkForFraud(transfer,
								fraudManager::isFraud,
								fraudManager::handleFraud));
				}
			});
		}catch(RollbackException e){
			manualRollbacks.put(e.getTransfer().getId(), e.getTransfer());
			return;
		}
	}
	public long getBalance(String accountNum) {
		if(accountNum == null || !accounts.containsKey(accountNum)){
			throw new IllegalArgumentException("Non-existent account: " + accountNum);
		}
		return accounts.get(accountNum).getBalance();
	}
	/////////////////////////////////////
	/////////IFraudManager///////////
	@Override
	public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount) 
			throws InterruptedException {
		Thread.sleep(1000);
		return random.nextBoolean();
	}
	@Override
	public void handleFraud(FraudAttempt fraudAttempt){
		Account fromAccount = accounts.get(fraudAttempt.getFromAccountNum());
		Account toAccount = accounts.get(fraudAttempt.getToAccountNum());
		fromAccount.setBlocked(true);
		toAccount.setBlocked(true);
		frauds.add(fraudAttempt);
	}
	/////////////////////
	
	public void setSecurityService(SecurityService securityService){
		securityServiceOp = Optional.of(securityService);
	}
	
	public void setFraudManager(IFraudManager fraudManager){
		this.fraudManagerOp = Optional.ofNullable(fraudManager);
	}
	
	///////////publically accessble methods//////////////
	public Account openAccount(){
		String accountNum = UUID.randomUUID().toString();
		Account account = new Account(accountNum);
		accounts.put(accountNum, account);
		return account;
	}
	
	//////////Trusted systems (from the same package) can use these//////////////
	Map<String, Transfer> getTransfers(){
		return transfers;
	}
	
	List<FraudAttempt> getFrauds(){
		return frauds;
	}	
	Map<String, Transfer> getFrozenPaymentRollbacks(){
		return manualRollbacks;
	}
	Optional<SecurityService> getSecurityService(){
		return securityServiceOp;
	}
}
