package com.OdysseyAi.demo.controller;

import com.OdysseyAi.demo.common.exception.customexception.UnauthorizedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {
    @GetMapping("/get")
    public String hello(){
        return "hello";
    }
    @GetMapping("/getex")
    public String excpt (){
        throw new UnauthorizedException("User not allowed ");
    }
}
