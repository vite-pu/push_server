package xyz.asurily.server;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.asurily.bean.pojo.ConnectionUserInfo;
import xyz.asurily.server.valid.ValidResult;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class EventRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(EventRegisterService.class);

    @PostConstruct
    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    ValidResult validResult = SessionSocketHolder.onlineEventQueue.poll();

                    if (validResult != null) {
                        long now = System.currentTimeMillis();

                        switch (validResult.getOpType()) {
                            case sub:
                                //订阅信息
                                if (SessionSocketHolder.clientSubMap.containsKey(validResult.getClientId())) {
                                    ConnectionUserInfo dexUserInfo = SessionSocketHolder.clientSubMap.get(validResult.getClientId());
                                    if (dexUserInfo != null) {
                                        dexUserInfo.setLastPing(now);
                                        validResult.getEventMap().forEach((k, v1) -> {
                                            dexUserInfo.getEventSubMap().put(k, now);
                                        });
                                    }
                                } else {
                                    Map<String, Long> subMap = new ConcurrentHashMap<>();
                                    validResult.getEventMap().forEach((k, v1) -> {
                                        subMap.put(k, now);
                                    });
                                    ConnectionUserInfo dexUserInfo = new ConnectionUserInfo(validResult.getClientId(), now, validResult.getClientIp(), subMap);
                                    dexUserInfo.setLastPing(now);
                                    SessionSocketHolder.clientSubMap.put(validResult.getClientId(), dexUserInfo);
                                }
                                //事件汇总
                                validResult.getEventMap().forEach((k, v1) -> {
                                    if (SessionSocketHolder.eventClientMap.containsKey(k)) {
                                        SessionSocketHolder.eventClientMap.get(k).add(validResult.getClientId());
                                    } else {
                                        Set<String> clientSet = Sets.newConcurrentHashSet();
                                        ;
                                        clientSet.add(validResult.getClientId());
                                        SessionSocketHolder.eventClientMap.put(k, clientSet);
                                    }
                                });
                                break;
                            case un_sub:
                                validResult.getEventMap().forEach((k, v1) -> {
                                    //订阅信息
                                    if (SessionSocketHolder.clientSubMap.containsKey(validResult.getClientId())) {
                                        SessionSocketHolder.clientSubMap.get(validResult.getClientId()).getEventSubMap().remove(k);
                                    }

                                    //事件汇总
                                    if (SessionSocketHolder.eventClientMap.containsKey(k)) {
                                        SessionSocketHolder.eventClientMap.get(k).remove(validResult.getClientId());
                                    }
                                });
                                break;
                            case ping:
                                if (SessionSocketHolder.clientSubMap.containsKey(validResult.getClientId())) {
                                    ConnectionUserInfo dexUserInfo = SessionSocketHolder.clientSubMap.get(validResult.getClientId());
                                    if (dexUserInfo != null) {
                                        dexUserInfo.setLastPing(System.currentTimeMillis());
                                    }
                                }
                                break;
                            case logout:
                                SessionSocketHolder.client2socketMap.remove(validResult.getClientId());
                                if (SessionSocketHolder.clientSubMap.containsKey(validResult.getClientId())) {
                                    SessionSocketHolder.clientSubMap.get(validResult.getClientId()).getEventSubMap().forEach(
                                            (k, v) -> {
                                                SessionSocketHolder.eventClientMap.get(k).remove(validResult.getClientId());
                                            }
                                    );
                                    SessionSocketHolder.clientSubMap.remove(validResult.getClientId());
                                }
                                break;
                        }
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
    }
}
