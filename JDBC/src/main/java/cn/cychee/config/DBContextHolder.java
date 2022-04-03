package cn.cychee.config;

import cn.cychee.enumerate.DBTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过ThreadLocal将数据源设置到每个线程上下文中
 */
@Slf4j
public class DBContextHolder {

    private static final ThreadLocal<DBTypeEnum> contextHolder = new ThreadLocal<>();
    /**
     * 是否强制走主库
     */
    private static final ThreadLocal<Boolean> mandatoryMaster = new ThreadLocal<>();

    private static final AtomicInteger counter = new AtomicInteger(-1);

    public static void useMaster() {
        mandatoryMaster.set(Boolean.TRUE);
    }
    public static void cleanUseMaster() {
        mandatoryMaster.remove();
    }
    public static boolean isUseMaster() {
        return mandatoryMaster.get() == Boolean.TRUE;
    }

    public static void set(DBTypeEnum dbType) {
        contextHolder.set(dbType);
    }

    public static DBTypeEnum get() {
        return contextHolder.get();
    }

    public static void master() {
        set(DBTypeEnum.MASTER);
        log.info("切换到master");
    }

    public static void slave() {
        //  轮询。负载均衡
        int index = counter.getAndIncrement() % 2;
        if (counter.get() > 9999) {
            counter.set(-1);
        }
        if (index == 0) {
            set(DBTypeEnum.SLAVE_ONE);
            log.info("切换到slaveOne");
        }else {
            set(DBTypeEnum.SLAVE_TWO);
            log.info("切换到slaveTwo");
        }
    }

}
