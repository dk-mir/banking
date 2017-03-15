package dk.mir.banking;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import dk.mir.banking.functions.Function3;
import dk.mir.banking.model.FraudAttempt;
import dk.mir.banking.model.FraudDetectOperation;
import dk.mir.banking.model.Transfer;
import dk.mir.banking.model.Transfer.RESULT;

public class SecurityService {
	
	private AtomicLong checkCounter = new AtomicLong();
	private ExecutorService exec;
	// queue to check fraud with priority based on transfer amount
	private BlockingQueue<FraudDetectOperation> toCheckQueue = new PriorityBlockingQueue<>(180, (fdo1, fdo2) -> {
			Transfer t1 = fdo1.getTransfer();
			Transfer t2 = fdo2.getTransfer();
			long diff = t1.getAmount() - t2.getAmount();
			//the more is amount the bigger is priority
			return diff == 0 ? 0 : diff > 0 ? -1 : 1;
		}
	);
	
	public void setExecutor(ExecutorService exec){
		this.exec = exec;
	}
	public ExecutorService getExecutorService(){
		return exec;
	}

	/**
	 * 
	 * @param transfer to check
	 * @param fraudDetectFunction a function that implements fraud detection where signature is: 
	 * <code>boolean func(String fromAccountNum, String toAccountNum, amount)</code>
	 * @return check counter
	 */
	public long checkForFraud(Transfer transfer, 
			Function3<String, String, Long, Boolean> fraudDetectFunction,
			Consumer<FraudAttempt> fraudHandleFunction){
		
		toCheckQueue.add(new FraudDetectOperation(transfer, 
				fraudDetectFunction,
				fraudHandleFunction));
		return checkCounter.incrementAndGet();
	}
	
	/**
	 * Starts processing fraud-check requests
	 * @return number of remaining requests in the queue when process is over
	 */
	public Future<Integer> start(){
		if(exec == null) exec = Executors.newSingleThreadExecutor();
		return exec.submit(() -> {
			while (true) {
				try {
					//boolean isPoisoned = checkAndFinish(toCheckQueue.take());
					FraudDetectOperation fdo = toCheckQueue.take();
					Transfer t = fdo.getTransfer();
					//POISON PILL - when transfer has result FAILED
					if(t.getResult() == RESULT.FAILED) break;
					boolean isFraud = fdo.getFraudDetectFunction()
							.apply(t.getFromAccountNum(), t.getToAccountNum(), t.getAmount());
					if(isFraud){
						FraudAttempt fa = new FraudAttempt(
								t.getFromAccountNum(), t.getToAccountNum(), t.getAmount());
						
						fdo.getFraudHandleFunction().accept(fa);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// return number of remained check-requests
			return toCheckQueue.size();
		});
	}
	/**
	 * @param isUrgent - if <code>true</code> at the next iteration Executor will stop, 
	 * otherwise it will go till the end of queue.
	 * @return check counter - how many checks submitted
	 */
	public long stop(boolean isUrgent){
		// -1 puts transfer in tail of the queue, LONG.MAX - at the head
		long sum = isUrgent ? Long.MAX_VALUE : -1;
		// RESULT.FAILED is a poisoned pill
		toCheckQueue.add(new FraudDetectOperation(new Transfer(null, null, sum, RESULT.FAILED), 
				null,
				null));
		return checkCounter.get();
	}
}
