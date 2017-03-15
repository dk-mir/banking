package dk.mir.banking;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;

import dk.mir.banking.exceptions.BalanceChangeException;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		AccountsTest.class,
		ParallelGeneratedTransferTest.class,
		CsvAccountsTest.class,
		CsvTransferTest.class,
		CsvAssuranceTest.class
	})
public class BankTestSuit {
	@BeforeClass
	public static void initResource() throws BalanceChangeException {
		//BankSystem.init(5000000, 1000, 20000);
		BankSystem.init();
		SecurityService ss = Mockito.spy(new SecurityService());
		//SecurityService ss = new SecurityService();
		BankSystem.getBank().setSecurityService(ss);
	}
}
