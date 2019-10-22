package xyz.asurily.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {""})
public class HeartbeatController {

    @ResponseBody
    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public String heartbeat() {
        return "ok";
    }
}
