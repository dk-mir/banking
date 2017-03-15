package dk.mir.banking.functions;

/**
 * A functional interface (callback) that computes a value based on multiple input values.
 * @param <T1> the first value type
 * @param <T2> the second value type
 * @param <T3> the second value type
 * @param <R> the result type
 */
public interface Function3<T1, T2, T3, R> {
    /**
     * Calculate a value based on the input values.
     * @param t1 the first value
     * @param t2 the second value
     * @param t3 the third value
     * @return the result value
     * @throws Exception on error
     */
    R apply(T1 t1, T2 t2, T3 t3) throws Exception;
}