import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;
import org.apache.http.impl.client.HttpClientBuilder;

public class Generator {
    static Logger logger = LoggerFactory.getLogger(Generator.class);
    private static String URI = null;
    // New vars
    private static AtomicLong rangeStart = new AtomicLong(0L);
    private static AtomicLong rangeEnd = new AtomicLong(-1L);
    private static final long defaultRangeLengthRefill = 1000L;
    private static final String defaultRequestRangeLength = "1";

    public static void main(String[] args) {
        BasicConfigurator.configure();
        try {
            if (args.length > 1 && args[1].startsWith("http")) {
                URI = args[1];
                getNewRangeAndGetNextRange(0L);
            } else {
                rangeEnd.set(Long.MAX_VALUE);
            }
            port(Integer.valueOf(args[0]));
            get("/getrange", (req, res) ->
                    getNextRange(Long.valueOf(req.queryParams("length") != null ? req.queryParams("length") : defaultRequestRangeLength)));
        } catch (Exception err) {
            System.out.println(err);
            logger.error(err.getMessage());
        }
    }

    private static synchronized long getNewRangeAndGetNextRange(long rangeLength) throws IOException, URISyntaxException {
        long responseRangeStart = rangeStart.getAndAdd(rangeLength);
        if (responseRangeStart+rangeLength <= rangeEnd.get()) {
            return responseRangeStart;
        }
        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder builder = new URIBuilder(URI);
        long maxRangeLengthNeeded = Math.max(defaultRangeLengthRefill, rangeLength * 2);
        builder.setParameter("length", String.valueOf(maxRangeLengthNeeded));
        HttpGet request = new HttpGet(builder.build());
        HttpResponse response = httpClient.execute(request);
        String responseText = EntityUtils.toString(response.getEntity());
        long rangeStartValue = Long.valueOf(responseText);
        rangeStart.set(rangeStartValue + rangeLength);
        rangeEnd.set(rangeStartValue + maxRangeLengthNeeded-1);
        return rangeStartValue;
    }

    private static long getNextRange(long rangeLength) throws IOException, URISyntaxException {

        long responseRangeStart = rangeStart.getAndAdd(rangeLength);

        if (responseRangeStart+rangeLength > rangeEnd.get()) {
            responseRangeStart = getNewRangeAndGetNextRange(rangeLength);
        }
        return responseRangeStart;
    }
}
/*
input: range_size
output: [start, end]
input 5
output 30,34

if parent_url
get the ranges from parent
else:
assume you are a root
[0, int_max]


range_start
range_end

1. 0 - 2000       2001-4000
2. 4001 - 6000

(start, end)
[start, end]
 */