package xyz.asurily.push;

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.asurily.bean.http.PushRequest;
import xyz.asurily.server.PushServer;
import xyz.asurily.server.SessionSocketHolder;

import javax.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class DataPushConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DataPushConsumer.class);

    @Autowired
    private PushServer pushServer;

    public static final ArrayBlockingQueue<PushRequest> pushQueue = new ArrayBlockingQueue(10000);

    public void addPushData(PushRequest pushRequest){
        pushQueue.add(pushRequest);
    }

    @PostConstruct
    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    PushRequest record = pushQueue.poll();
                    if(record != null){
                        receive(record);
                    }else {
                        try {
                            TimeUnit.MILLISECONDS.sleep(50);
                        } catch (InterruptedException e) {
                            logger.error("",e);
                        }
                    }
                }
            }
        }).start();
    };

    public void receive(PushRequest record) {
        logger.info("msg:{}", record);
        try {
            for (String eventKey : record.getTopics().split(",")) {
                if (SessionSocketHolder.eventClientMap.containsKey(eventKey)) {
                    for (String clientId : SessionSocketHolder.eventClientMap.get(eventKey)) {
                        StopWatch stopWatch = StopWatch.createStarted();
                        pushServer.sendMsg(clientId, eventKey, ByteString.copyFrom(record.getData()));
                        stopWatch.stop();
                        logger.info("push-diff-time:{}", stopWatch.getTime(TimeUnit.MILLISECONDS));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Send msg error", e);
        }
    }
}
