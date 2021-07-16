package cn.ac.caict.iiiiot.id.client.adapter;

import cn.ac.caict.iiiiot.id.client.adapter.cache.IdentifierRecordCache;
import cn.ac.caict.iiiiot.id.client.core.IdentifierException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class MonitorTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     *   "ip": "139.198.126.227",
     *   "port": "2644",
     *   {
     *   "ip": "36.112.25.8",
     *   "port": "3641",
     *   "query": true,
     *   "admin": false,
     *   "protocol": "TCP"
     * }
     */
    @Test
    public void test() {
        Map<String,Object> config = new HashMap<>();
        config.put("ip","139.198.126.227");
        config.put("port","2644");
        config.put("query",true);
        config.put("admin",false);
        config.put("protocol","TCP");
        Configuration.getInstance().setConfig(config);
        try {
            Long l = monitor("88.902.000000",2000);
            System.out.println(l);
        } catch (IdentifierException e) {
            e.printStackTrace();
        } catch (IdentifierAdapterException e) {
            e.printStackTrace();
        }
    }

    private Long monitor(String prefix,int timeout) throws IdentifierException, IdentifierAdapterException {

        IDAdapter idAdapter = IDAdapterFactory.cachedInstance();

        IdentifierRecordCache.getInstance().clear();
        PrefixSiteInfo prefixSiteInfo = idAdapter.resolveSiteByProxy(prefix);

        Long result = -1l;
        FutureTask<Long> futureTask = new FutureTask<>(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return doMonitor(prefixSiteInfo);
            }
        });

        executorService.execute(futureTask);
        try {
            result = futureTask.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            futureTask.cancel(true);
        }
        return result;
    }

    private Long doMonitor(PrefixSiteInfo prefixSiteInfo){
        long begin = System.currentTimeMillis();
        IDAdapter adapter = IDAdapterFactory.newInstance(prefixSiteInfo.getIp(), prefixSiteInfo.getPort());
        try {
            adapter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis() - begin;
    }
}
