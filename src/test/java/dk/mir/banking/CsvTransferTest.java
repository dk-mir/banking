package dk.mir.banking;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;


@RunWith(Parameterized.class)
public class CsvTransferTest {

	@Parameterized.Parameters
	public static Collection<Triple<String, String, Long>> csvData() throws Exception {
		try(InputStream input = CsvAccountsTest.class.getResource("/paymentsBatch.csv").openStream();
				CSVReader reader = new CSVReader(new InputStreamReader(input), ';',
						CSVParser.DEFAULT_QUOTE_CHARACTER, 1);){
			
				Collection<Triple<String,String,Long>> transfersData = new ArrayList<>();
				String [] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					String fromAccNum = nextLine[0];
					String toAccNum = nextLine[1];
					long amount = Long.parseLong(nextLine[2]);
					Triple<String, String, Long> triple = 
							new ImmutableTriple<>(fromAccNum, toAccNum, amount);
					transfersData.add(triple);
				}
				return transfersData;
		} catch (IOException | NumberFormatException e) {
			throw e;
		}
	}
	
	private final Triple<String, String, Long> testData;
	public CsvTransferTest(Triple<String, String, Long> testData){
		this.testData = testData;
	}
	
	@Test
	public void commitCsvTransfer() throws Exception{
		try{
			BankSystem.getBank().transfer(testData.getLeft(), testData.getMiddle(), testData.getRight());
		}catch(IllegalArgumentException e){}
	}
}
