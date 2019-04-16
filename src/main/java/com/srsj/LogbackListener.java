package com.srsj;

import ch.qos.logback.ext.spring.web.LogbackConfigListener;
import ch.qos.logback.ext.spring.web.WebLogbackConfigurer;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Created by david.sha on 2017/4/12.
 */
@WebListener
public class LogbackListener extends LogbackConfigListener {
    @Override
    public void contextInitialized(ServletContextEvent var1) {
        super.contextInitialized(var1);
    }
}
