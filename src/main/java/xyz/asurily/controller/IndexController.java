package xyz.asurily.controller;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.asurily.bean.http.PushRequest;
import xyz.asurily.bean.pojo.ConnectionUserInfo;
import xyz.asurily.push.DataPushConsumer;
import xyz.asurily.server.SessionSocketHolder;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 22/05/2018 14:46
 * @since JDK 1.8
 */
@Controller
@RequestMapping("/push/v1/api/")
public class IndexController {

    @Resource
    private DataPushConsumer dataPushConsumer;

    @RequestMapping(value = "data", method = RequestMethod.POST)
    @ResponseBody
    public void push(@RequestBody PushRequest pushRequest){
        if(StringUtils.isNoneEmpty(pushRequest.getTopics())){
            if(null != pushRequest.getData()){
                dataPushConsumer.addPushData(pushRequest);
            }
        }
    }

    @RequestMapping(value = "getClientSub", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, ConnectionUserInfo> getUser() {
        return SessionSocketHolder.clientSubMap;
    }

    @RequestMapping(value = "getEventClient", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Set<String>> getUserByEvent() {
        return SessionSocketHolder.eventClientMap;
    }

    @RequestMapping(value = "getClientSocket", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, NioSocketChannel> getClientSocket() {
        return SessionSocketHolder.client2socketMap;
    }
}
