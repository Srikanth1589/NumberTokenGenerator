import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;
import org.apache.http.impl.client.HttpClientBuilder;

public class Generator {
    static Logger logger = LoggerFactory.getLogger(Generator.class);
    private static AtomicLong currentValue = new AtomicLong(0L);
    private static long incrementBy = 1L;
    private static boolean isWorker = false;
    private static long flagNumber = 0L;
    private static final long masterIncrementBy = 1000L;
    private static Long startValue;
    private static String URI = null;

    public static void main(String[] args) {
        try {
            if (args.length > 2 && args[2].startsWith("http")) {
                URI = args[2];
                getNewRangeAndGetNextNumber();
            }
            incrementBy = Long.valueOf(args[0]);
            port(Integer.valueOf(args[1]));
            get("/hello", (req, res) -> getNextNumber());
        } catch (Exception err) {
            System.out.println(err);
            logger.error(err.getMessage());
        }
    }

    private static synchronized long getNewRangeAndGetNextNumber() throws IOException {
        long nextNumber = currentValue.addAndGet(incrementBy);
        if (nextNumber < (startValue+masterIncrementBy)){
            return nextNumber;
        }
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(URI);
        HttpResponse response = httpClient.execute(request);
        String responseText = EntityUtils.toString(response.getEntity());
        startValue = Long.valueOf(responseText);
        nextNumber = startValue+incrementBy;
        currentValue.set(nextNumber);
        flagNumber =  startValue + Math.round(0.75 * masterIncrementBy);
        isWorker = true;
        return nextNumber;
    }

    private static long getNextNumber() throws IOException {

        long nextNumber = currentValue.addAndGet(incrementBy);

        if (nextNumber >= (startValue+masterIncrementBy)) {
            nextNumber = getNewRangeAndGetNextNumber();
        }
        /*
        if (nextNumber >= flagNumber){
            getNewRange();
        }*/
        return nextNumber;
    }
}
