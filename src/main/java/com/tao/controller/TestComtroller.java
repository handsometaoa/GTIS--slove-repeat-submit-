package com.tao.controller;


import com.tao.anno.RepeatSubmit;
import com.tao.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tao
 */
@RestController
public class TestComtroller {

    @RepeatSubmit(interval = 10000,message = "请勿重复提交")
    @GetMapping("/test")
    Result<String> testName(@RequestParam("name") String name) {
        return Result.success("success");
    }

}
