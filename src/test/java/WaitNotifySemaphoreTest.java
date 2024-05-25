import org.example.WaitNotifySemaphore;
import org.junit.jupiter.api.Test;

public class WaitNotifySemaphoreTest {

    @Test
    public void testSemaphore() throws InterruptedException {
        WaitNotifySemaphore semaphore = new WaitNotifySemaphore(1);
        semaphore.acquire();
        semaphore.acquire();
    }
}
