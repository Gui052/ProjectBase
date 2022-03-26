package cn.cychee.schedule;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XxlJobConfig {
    private final Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${server.port:8080}")
    private Integer port;
    @Value("${application.id:}")
    private String id;

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.ip}")
    private String IP;

    @Value("${xxl.job.executor.logpath:./applogs/xxl-job/jobhandler}")
    private String logPath;
    //执行器日志保存时间
    @Value("${xxl.job.executor.logretentiondays:30}")
    private int logRetentionDays;


    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        logger.info("*-*-*-*-*-*-*-*-*-* xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(id);
        //执行器地址，如果没有配置，就使用 IP:PORT 作为默认值
        //xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(IP);
        xxlJobSpringExecutor.setPort(port);
        //执行器和调度中心之间的通信令牌，如果没有配置，表示关闭了通信令牌的校验。
        //在 xxl-job-admin 的配置文件中，有一个一模一样的配置项，两边都配置，就会进行校验。
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        logger.info("*-*-*-*-*-*-*-*-*-* xxl-job config adminAddresses:{}, appName:{}, Ip:{}, port:{}, accessToken:{}",
                adminAddresses, id, IP, port, accessToken);
        return xxlJobSpringExecutor;
    }

}