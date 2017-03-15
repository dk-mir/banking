package dk.mir.banking;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.*;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import dk.mir.banking.model.Account;

@RunWith(Parameterized.class)
public class CsvAssuranceTest {
	
	@Parameterized.Parameters
	public static Collection<TransferTestResult> spreadsheetData() throws Exception {
		try(InputStream input = CsvAccountsTest.class.getResource("/result.csv").openStream();
				CSVReader reader = new CSVReader(new InputStreamReader(input), ';',
						CSVParser.DEFAULT_QUOTE_CHARACTER, 1);){
			
				Collection<TransferTestResult> transferResults = new ArrayList<>();
				String [] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					String accNum = nextLine[0];
					long balance = Long.parseLong(nextLine[1]);
					long rollbackBalance = nextLine.length > 2 ?
							Long.parseLong(nextLine[2]) : balance;
					transferResults.add(new TransferTestResult(accNum, balance, rollbackBalance));
				}
				return transferResults;
		} catch (IOException | NumberFormatException e) {
			throw e;
		}
	}
	
	private static class TransferTestResult{
		private final String accNum;
		private final long balance;
		private final long rolledbackBalance;
		public TransferTestResult(String accNum, long balance, long rolledbackBalance){
			this.accNum = accNum;
			this.balance = balance;
			this.rolledbackBalance = rolledbackBalance;
		}
		public String getAccNum() {
			return accNum;
		}
		public long getBalance() {
			return balance;
		}
		public long getRolledbackBalance() {
			return rolledbackBalance;
		}
	}
	
	private final TransferTestResult transferTestResult;
	
	public CsvAssuranceTest(TransferTestResult result){
		this.transferTestResult = result;
	}
	
	@Test
	public void testCsvAccountsAfterTransfers(){
		Account acc = BankSystem.getBank().accounts.get(transferTestResult.getAccNum());
		assertThat(acc.getBalance())
			.matches( balance -> balance.longValue() == transferTestResult.getBalance() ||
						balance.longValue() == transferTestResult.getRolledbackBalance(),
			"Balance should be "+transferTestResult.getBalance()
			+ " or "+ transferTestResult.getRolledbackBalance());
	}
}
