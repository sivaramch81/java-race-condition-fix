class Counter {
    private int count = 0;

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}

class SynchronizedCounter {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Race Condition Demo ===\n");

        // Demonstrate race condition
        System.out.println("1. WITHOUT synchronization (Race Condition):");
        Counter unsafeCounter = new Counter();
        Thread[] threads1 = new Thread[10];

        for (int i = 0; i < 10; i++) {
            threads1[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    unsafeCounter.increment();
                }
            });
            threads1[i].start();
        }

        for (Thread thread : threads1) {
            thread.join();
        }

        System.out.println("Expected: 10000");
        System.out.println("Actual:   " + unsafeCounter.getCount());
        System.out.println("Result:   " + (unsafeCounter.getCount() == 10000 ? "PASS" : "FAIL - Race condition occurred!"));

        // Demonstrate fix with synchronization
        System.out.println("\n2. WITH synchronization (Fixed):");
        SynchronizedCounter safeCounter = new SynchronizedCounter();
        Thread[] threads2 = new Thread[10];

        for (int i = 0; i < 10; i++) {
            threads2[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    safeCounter.increment();
                }
            });
            threads2[i].start();
        }

        for (Thread thread : threads2) {
            thread.join();
        }

        System.out.println("Expected: 10000");
        System.out.println("Actual:   " + safeCounter.getCount());
        System.out.println("Result:   " + (safeCounter.getCount() == 10000 ? "PASS - Race condition fixed!" : "FAIL"));
    }
}