package com.atguigu.yygh.common.exception;

import com.atguigu.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice //将其注册到spring boot中
public class GlobalExceptionHandler {
    //全局异常处理
    @ExceptionHandler(Exception.class) //返回异常的类型
    @ResponseBody //以json形式返回异常
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }
    //自定义异常处理,自定义异常需要手动抛出异常
    @ExceptionHandler(YyghException.class) //返回异常的类型
    @ResponseBody //以json形式返回异常
    public Result error(YyghException e){
        e.printStackTrace();
        return Result.fail();
    }
}
