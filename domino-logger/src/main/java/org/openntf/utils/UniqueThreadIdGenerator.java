package org.openntf.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generator class used to get a transaction ID.
 * 
 * @author Olle Thalén
 *
 */
public class UniqueThreadIdGenerator {

    private static final AtomicInteger uniqueId = new AtomicInteger(0);

    private static final ThreadLocal < Integer > uniqueNum = 
        new ThreadLocal < Integer > () {
            @Override protected Integer initialValue() {
                return uniqueId.getAndIncrement();
        }
    };

    /**
     * Gets the unique ID for the current thread. <br/>
     * If the thread local object hasn't been created yet, the uniqueId instance is incremented first.
     * 
     * @return a thread-local ID
     */
    public static int getCurrentThreadId() {
        return uniqueNum.get();
    }
} 
