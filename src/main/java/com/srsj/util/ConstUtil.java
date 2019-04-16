package com.srsj.util;

public class ConstUtil {

    public enum LogLevelEnum {
        Error("error", "error"),
        Warn("warn", "warn"),
        Info("info", "info"),
        Debug("debug", "debug"),
        Trace("trace", "trace"),
        ;
        // 成员变量
        private String name;
        private String value;

        // 构造方法
        private LogLevelEnum(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static LogLevelEnum getEnum(String name) {
            for (LogLevelEnum type : LogLevelEnum.values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public static String getValue(String name) {
            for (LogLevelEnum type : LogLevelEnum.values()) {
                if (type.getName().equals(name)) {
                    return type.getValue();
                }
            }
            return "";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
