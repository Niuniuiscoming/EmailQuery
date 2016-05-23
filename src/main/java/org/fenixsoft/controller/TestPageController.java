package org.fenixsoft.controller;

import org.springframework.web.bind.annotation.*;

/**
 * Created by IcyFenix on 2016-05-20.
 */
@RestController
@RequestMapping("/test")
public class TestPageController {

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @ResponseBody
    public String sayHello(@PathVariable("name") String name) {
        return "hello " + name;
    }

}
