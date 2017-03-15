package dk.mir.banking;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import dk.mir.banking.model.Account;

/**
 * Tests that bank didn't lost money since opening accounts in batches, i.e.:
 * <br /><code>bank's own money + on accounts = total money</code>
 */
public class AccountsTest {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountsTest.class);
	
	@Test
	public void bankDidNotLostMoney(){
		Map<String, Account> totalAccounts = Collections.unmodifiableMap(
				new HashMap<>(BankSystem.getBank().accounts));
		boolean isMode = false;
		List<Account> activeAccounts = totalAccounts.values().stream()
			.filter( acc -> acc.getBalance() > 0)
			.sorted( (acc1, acc2) -> Long.compare(acc1.getBalance(), acc2.getBalance()))
			.collect(Collectors.toList());
		
		if(LOG.isTraceEnabled()){
			int counter = 0;
			for( Account acc : activeAccounts){
				counter ++ ;
				LOG.trace("{} : {} {}", acc.getBalance(), acc.getNumber(), counter);
				if(!isMode && acc.getBalance() > BankSystem.getMedianBalance()){
					LOG.trace("==== MEDIAN =====");
					isMode = true;
				}
			}
		}
		
		long totalOnAccounts = activeAccounts.stream()
			.map(acc -> acc.getBalance() )
			.reduce(0L, (x, y) -> x+y);
		if(LOG.isInfoEnabled()){
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			LOG.info("Active random accounts: {} (of {})", activeAccounts.size(), totalAccounts.size());
			LOG.info("Total on random accounts: {}", formatter.format(totalOnAccounts));
			LOG.info("Remained in the Bank: {}", formatter.format(BankSystem.getBankOwnMoney()));
		}
		
		Assert.assertEquals("Bank has lost money!", 
				totalOnAccounts + BankSystem.getBankOwnMoney(),
				BankSystem.getMoneyPile());
	}
	
	

}
