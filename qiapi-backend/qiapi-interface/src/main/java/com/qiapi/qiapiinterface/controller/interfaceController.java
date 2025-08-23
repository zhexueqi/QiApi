package com.qiapi.qiapiinterface.controller;


import com.qiapi.model.User;
import com.qiapi.project.utils.SignUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * @author zhexueqi
 * @ClassName interfaceController
 * @since 2024/8/2    16:56
 */
@RestController
@RequestMapping("/name")
public class interfaceController {

    @GetMapping
    public String getName(@RequestParam String name){
        return "GET 你的名字是:"+name;
    }

    @PostMapping
    public String postName(String name){
        return "POST 你的名字是:"+name;
    }

    @PostMapping("/restful")
    public String postName(@RequestBody User user, HttpServletRequest request) throws UnsupportedEncodingException {
        return "POST 你的名字是:"+user.getName();
    }

}
