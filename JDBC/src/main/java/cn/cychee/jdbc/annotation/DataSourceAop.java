package cn.cychee.jdbc.annotation;

import cn.cychee.jdbc.config.DBContextHolder;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 注解实现切面
 */
@Aspect
@Component
public class DataSourceAop {
    @Pointcut("@annotation(Master)")
    public void masterPointcut() {
    }

    @Before("masterPointcut()")
    public void master() {
        DBContextHolder.master();
        DBContextHolder.useMaster();
    }

    @After("masterPointcut()")
    public void cleanUseMaster() {
        DBContextHolder.cleanUseMaster();
    }
}
