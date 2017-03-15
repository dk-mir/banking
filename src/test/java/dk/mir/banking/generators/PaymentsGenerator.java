package dk.mir.banking.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import dk.mir.banking.model.Account;

public class PaymentsGenerator {
	
	private static final Random RANDOM_GENERATOR = new Random();
	//mode = (2d-1) / (2d+5d-2) = 0.2	
	//public static final BetaDistribution betaDist = new BetaDistribution(2d, 5d);
	/**
	 * An attempt to simulate real-life money distribution on bank accounts:
	 * <ul>
	 *  <li>many accounts with small balance</li>
	 *  <li>few accounts with great balance</li>
	 * </ul>
	 */
	public static final ChiSquaredDistribution MONEY_DISTRIBUTION = new ChiSquaredDistribution(1);
	
	public static List<Triple<String, String, Long>> getRandomPayments(
			Map<String, Account> accountsMap, List<Long> paymentsBatch){
		
		Account[] accounts = accountsMap.values().toArray(new Account[]{});
		List<Triple<String, String, Long>> payments = new ArrayList<>();
		for(long amount : paymentsBatch){
			Account from = accounts[RANDOM_GENERATOR.nextInt(accounts.length)];
			Account to = accounts[RANDOM_GENERATOR.nextInt(accounts.length)];
			Triple<String, String, Long> triplet = new ImmutableTriple<>(from.getNumber(), 
					to.getNumber(), amount);
			payments.add(triplet);
		}
		return payments;
	}
	
	public static List<List<Long>> getRandomPaymentAmounts(int workers, 
			int recordsInBatch,
			long paymentMean){
		List<List<Long>> testBatches = new ArrayList<>();
		while(workers > 0){
			workers--;
			List<Long> paymentsBatch = new ArrayList<>();
			int paymentsInBatch = recordsInBatch;
			while(paymentsInBatch > 0){
				paymentsInBatch--;
				long amount = (long)(MONEY_DISTRIBUTION.sample() * paymentMean );
				paymentsBatch.add(amount);
			}
			testBatches.add(paymentsBatch);
		}
		return testBatches;
	}

}
