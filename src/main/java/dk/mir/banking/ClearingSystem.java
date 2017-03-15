package dk.mir.banking;

import dk.mir.banking.exceptions.RollbackException;
import dk.mir.banking.model.Account;
import dk.mir.banking.model.Transfer;
import dk.mir.banking.model.Transfer.RESULT;

public class ClearingSystem {
	
	/**
	 * 
	 * @param fromAccount
	 * @param toAccount
	 * @param amount
	 * @return
	 * @throws RollbackException if the transfer had failed and rolling back has also failed
	 */
	public Transfer commitTransfer(Account fromAccount, Account toAccount, long amount) 
			throws RollbackException{
		try{
			fromAccount.withdraw(amount);
		}catch(Throwable e){
			return new Transfer(fromAccount.getNumber(), toAccount.getNumber(),
					amount, RESULT.FAILED);
		}
		
		try{
			toAccount.deposit(amount);
			return new Transfer(fromAccount.getNumber(), toAccount.getNumber(), amount, RESULT.OK);
		}catch(Throwable e){
			try {
				fromAccount.deposit(amount);
			} catch (Throwable e1) {
				return new Transfer(fromAccount.getNumber(),
						toAccount.getNumber(), amount, RESULT.FAILED_NOT_ROLLBACKED);
			}
		}
		return new Transfer(fromAccount.getNumber(), toAccount.getNumber(), amount, RESULT.FAILED);
	}
}
