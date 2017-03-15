package dk.mir.banking;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import dk.mir.banking.generators.PaymentsGenerator;
import dk.mir.banking.model.Transfer;
import dk.mir.banking.params.Parallelized;
import static org.assertj.core.api.Assertions.*;


@RunWith(Parallelized.class)
public class ParallelGeneratedTransferTest {
	
	private static final org.slf4j.Logger LOG = 
			org.slf4j.LoggerFactory.getLogger(ParallelGeneratedTransferTest.class);
	
	private final List<Triple<String, String, Long>> payments;
	
	private static AtomicLong invalidTransfersCounter = new AtomicLong();
	
	private static int plannedTransfers = 0;
	/**
	 * Future for security service Executor. Integer - is number of remaining checks in the queue.
	 */
	private static Future<Integer> securityServiceRunResult;
	
	public ParallelGeneratedTransferTest(List<Long> paymentsBatch){
		payments = PaymentsGenerator.getRandomPayments(Collections.unmodifiableMap(
				new HashMap<>(BankSystem.getBank().accounts)), paymentsBatch);
		
	}
	
	/**
	 * Creates batches for payments to schedule them for processing.
	 * @return collection of money amounts to transfer
	 */
	@Parameterized.Parameters
	public static Collection<List<Long>> createPaymentBatches(){
		int workers = 3;
		int recordsInBatch = 40000;
		int paymentMean = 3000;
		plannedTransfers = workers * recordsInBatch;
		return PaymentsGenerator.getRandomPaymentAmounts(workers, recordsInBatch, paymentMean);		
	}
	
	@Test
	public void testTransfer(){
		for (Triple<String, String, Long> triplet : payments){
			try{
				LOG.trace("Transfer {} : {} -> {}", triplet.getRight(), triplet.getLeft(), triplet.getMiddle());
				BankSystem.getBank().transfer(triplet.getLeft(), triplet.getMiddle(), triplet.getRight());
			}catch(Throwable e){
				LOG.debug("{}", e.getMessage());
				invalidTransfersCounter.incrementAndGet();
			}
		}
	}
	@BeforeClass
	public static void startSecruityService(){
		BankSystem.getBank().getSecurityService().ifPresent( ss -> {
			securityServiceRunResult = ss.start();
			LOG.info("==== security service started ========");
		});
	}
	@AfterClass
	public static void getStatistics(){
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
		NumberFormat numberFormatter = NumberFormat.getInstance();
		LOG.info("Accepted transfers: {}", 
				numberFormatter.format(BankSystem.getBank().getTransfers().size()));
		if(LOG.isTraceEnabled()){
			BankSystem.getBank().getTransfers().values().forEach( t -> {
				LOG.trace("OK {} : {} -> {}", t.getAmount(), t.getFromAccountNum(),
						t.getToAccountNum());
			});
		}
		
		long failedTransfers = BankSystem.getBank().getTransfers().values().stream()
			.filter( t -> t.getResult()==Transfer.RESULT.FAILED).count();
		
		long failedAmount = BankSystem.getBank().getTransfers().values().stream()
			.filter( t -> t.getResult()==Transfer.RESULT.FAILED)
			.map( t -> t.getAmount())
			.reduce(0L, (x, y) -> x+y);
		
		LOG.info(" - including failed: {} [{}]", numberFormatter.format(failedTransfers),
				currencyFormatter.format(failedAmount));
		
		long completedTransfers = BankSystem.getBank().getTransfers().values().stream()
			.filter( t -> t.getResult()==Transfer.RESULT.OK).count();
		
		long turnover = BankSystem.getBank().getTransfers().values().stream()
			.filter( t -> t.getResult()==Transfer.RESULT.OK)
			.map( t -> t.getAmount())
			.reduce(0L, (x, y) -> x+y);
		
		LOG.info(" - including OK: {} [{}]", numberFormatter.format(completedTransfers), 
				currencyFormatter.format(turnover));
		LOG.info("Invalid transfers: {}",
				numberFormatter.format(invalidTransfersCounter.get()));
		
		assertThat(BankSystem.getBank().getTransfers().size() +
				invalidTransfersCounter.get())
			.isEqualTo(plannedTransfers)
			.as("Bank has lost transfers!");
		
		BankSystem.getBank().getSecurityService().ifPresent( ss -> {
			//inject poisoned pill
			long checkCounter = ss.stop(false);
			Mockito.verify(BankSystem.getBank().getSecurityService().get(), Mockito.atLeast(1))
				.start();
			LOG.info("Performing security checks: {}. Wait for about {}s please!", 
					checkCounter, checkCounter);
		});
		
		if(securityServiceRunResult != null) {
			try {
				int uncheckedTransfers = securityServiceRunResult.get();
				if(uncheckedTransfers > 0)
					LOG.info("Remain unchecked transfers: {}", uncheckedTransfers);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		LOG.info("Fraudulent transfers: {}", BankSystem.getBank().getFrauds().size());
		
		if(LOG.isTraceEnabled()){
			BankSystem.getBank().getFrauds().forEach( fa -> {
				LOG.trace("FRAUD {} : {}[{}] -> {}[{}]", fa.getAmount(),
						fa.getFromAccountNum(), 
						BankSystem.getBank().accounts.get(fa.getFromAccountNum()).isBlocked(),
						fa.getToAccountNum(), 
						BankSystem.getBank().accounts.get(fa.getToAccountNum()).isBlocked());
			});
		}
		
		long blockedAccounts = BankSystem.getBank().accounts.values().stream()
			.filter( ac -> ac.isBlocked())
			.count();
		LOG.info("Blocked accounts: {}", blockedAccounts);
		
		BankSystem.getBank().accounts.values().stream()
			.filter( ac -> ac.isBlocked())
			.map(ac -> ac.getBalance())
			.reduce( (x,y) -> x+y).ifPresent( blockedBalance -> {
				LOG.info("Blocked money on accounts: {}", 
						currencyFormatter.format(blockedBalance));
			});
		
		long accountsBalance = BankSystem.getBank().accounts.values().stream()
			.map(ac -> ac.getBalance())
			.reduce( 0L, (x, y) -> x+y);
		
		long frozenMoney = BankSystem.getBank().getFrozenPaymentRollbacks().values().stream()
				.map(t -> t.getAmount())
				.reduce( 0L, (x, y) -> x+y);
		
		if(frozenMoney > 0){
			LOG.info("Frozen money: {}", currencyFormatter.format(frozenMoney));
		};

		assertThat(accountsBalance + frozenMoney + BankSystem.getBankOwnMoney())
			.isEqualTo(BankSystem.getMoneyPile())
			.as("Bank has lost money!");
	}
	
	/*
	private static Collection<String[]> getTestData(String fileName) throws IOException {
		List<String[]> records = new ArrayList<String[]>();
		String record;
		BufferedReader file = new BufferedReader(new FileReader(fileName));
		while ((record = file.readLine()) != null) {
		String fields[] = record.split(",");
		records.add(fields);
		}
			file.close();
			return records;
			}
	*/
}
