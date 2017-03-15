package dk.mir.banking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.io.Files;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import dk.mir.banking.exceptions.BalanceChangeException;
import dk.mir.banking.generators.PaymentsGenerator;
import dk.mir.banking.model.Account;
import dk.mir.banking.params.Parallelized;

import static org.assertj.core.api.Assertions.*;

/**
 * In this tes data is provided from CSV files
 * @author Kalinin_DP
 *
 */
@RunWith(Parameterized.class)
public class CsvAccountsTest {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CsvAccountsTest.class);
	
	private static final NumberFormat formatter = NumberFormat.getCurrencyInstance();
	
	@Parameterized.Parameters
	public static Collection<Map<String, Account>> spreadsheetData() throws Exception {
		Collection<Map<String, Account>> allAccounts = new ArrayList<>();
		try(InputStream input = CsvAccountsTest.class.getResource("/accounts.csv").openStream();
				CSVReader reader = new CSVReader(new InputStreamReader(input), ';',
						CSVParser.DEFAULT_QUOTE_CHARACTER, 1);){
			
				Map<String, Account> accounts = new HashMap<>();
				String [] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					Account acc = new Account(nextLine[0]);
					acc.deposit(Long.parseLong(nextLine[1]));
					accounts.put(acc.getNumber(),  acc);
				}
				allAccounts.add(accounts);
		} catch (IOException | NumberFormatException | BalanceChangeException e) {
			throw e;
		}
		return allAccounts;
	}
	
	
	
	public CsvAccountsTest(Map<String, Account> accounts){
		BankSystem.getBank().accounts.putAll(accounts);
	}
	
	@Test
	public void printAccountsFromCsv(){
		LOG.trace("=== accounts from csv-file ===");
		long csvAccounts = BankSystem.getBank().accounts.values().stream()
			.filter( acc -> acc.getNumber().length() == 4)
			.map( acc -> {
				LOG.trace("{} : {}", acc.getNumber(), formatter.format(acc.getBalance()));
				return acc;
			}).count();
		LOG.info("Accounts from csv-file: {}", csvAccounts);
	}
	@Test
	public void addCsvAccountBalanceToMoneypile(){
		long csvAccountsTotalBalance = BankSystem.getBank().accounts.values().stream()
			.filter( acc -> acc.getNumber().length() == 4)
			.map( acc -> acc.getBalance())
			.reduce(0L, (x, y) -> x+y);
		
		long oldTotalMoney = BankSystem.getMoneyPile();
		long newTotalMoney = BankSystem.addCapitalization(csvAccountsTotalBalance);
		assertThat(newTotalMoney).isEqualTo(oldTotalMoney + csvAccountsTotalBalance);
	}
}
