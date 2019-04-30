package com.srsj.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;


public class RtmpUtil {

    private static int chunkSize = 4096;

    public static double bytes2Double(byte[] arr) {
        long bits =
                ((long) (arr[0] & 0xff) << 56)
                        | ((long) (arr[1] & 0xff) << 48)
                        | ((long) (arr[2]
                        & 0xff) << 40)
                        | ((long) (arr[3] & 0xff) << 32)
                        | ((arr[4] & 0xff) << 24)
                        | ((arr[5] & 0xff) << 16)
                        | ((arr[6] & 0xff) << 8)
                        | (arr[7] & 0xff);
        return Double.longBitsToDouble(bits);
    }

    public static boolean bytes2boolean(byte[] bytes) {
        Byte b = bytes[0];
        return BooleanUtils.toBoolean(b.intValue());
    }

    public static int bytes2Int(byte[] bytes) {
        return Integer.parseInt(Hex.encodeHexString(bytes), 16);
    }

    public static long bytes2Long(byte[] bytes) {
        return Long.parseLong(Hex.encodeHexString(bytes), 16);
    }

    public static byte[] int2bytes(int i, int byteCount) throws Exception {
        String iHexString = Integer.toHexString(i);
        iHexString = fillZero(iHexString, byteCount);
        byte[] bytes = Hex.decodeHex(iHexString);
        return bytes;
    }

    public static byte[] long2bytes(long l, int byteCount) throws Exception {
        String lHexString = Long.toHexString(l);
        lHexString = fillZero(lHexString, byteCount);
        byte[] bytes = Hex.decodeHex(lHexString);
        return bytes;
    }

    public static byte[] double2bytes(double d) {
        long l = Double.doubleToRawLongBits(d);
        return new byte[]{
                (byte) ((l >> 56) & 0xff), (byte) ((l >> 48) & 0xff), (byte) ((l >> 40) & 0xff),
                (byte) ((l >> 32) & 0xff), (byte) ((l >> 24) & 0xff), (byte) ((l >> 16) & 0xff),
                (byte) ((l >> 8) & 0xff), (byte) (l & 0xff),
        };
    }

    public static String fillZero(String hexString, int byteCount) {
        int byteLength = byteCount * 2;
        int hexLength = hexString.length();
        if (byteLength > hexLength) {
            for (int i = 0; i < byteLength - hexLength; i++) {
                hexString = "0" + hexString;
            }
            return hexString;
        } else {
            return hexString;
        }
    }

    public static boolean isValidCollect(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Collection) {
            Collection<?> objl = (Collection<?>) obj;
            return !objl.isEmpty();
        } else if (obj instanceof Object[]) {
            Object[] objs = (Object[]) obj;
            return objs.length > 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) > 0;
        } else if (obj instanceof Map) {
            Map<?, ?> objm = (Map<?, ?>) obj;
            return !objm.isEmpty();
        } else {
            return false;
        }
    }

    public static void log(Logger logger, ConstUtil.LogLevelEnum level, String msg, Object obj, Throwable e) {
        if (logger == null || level==null) {
            return;
        }
        msg = StringUtils.trimToEmpty(msg);
        switch (level) {
            case Warn: {
                if (logger.isWarnEnabled()) {
                    if (e == null) {
                        if (obj == null) {
                            logger.warn("{}", msg);
                        } else {
                            try {
                                logger.warn(msg + ":{}", JacksonUtils.toJson(obj));
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    } else {
                        if (obj == null) {
                            logger.warn(msg, e);
                        } else {
                            try {
                                logger.warn(msg + ":" + JacksonUtils.toJson(obj), e);
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    }
                }
                break;
            }
            case Error: {
                if (logger.isErrorEnabled()) {
                    if (e == null) {
                        if (obj == null) {
                            logger.error("{}", msg);
                        } else {
                            try {
                                logger.error(msg + ":{}", JacksonUtils.toJson(obj));
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    } else {
                        if (obj == null) {
                            logger.error(msg, e);
                        } else {
                            try {
                                logger.error(msg + ":" + JacksonUtils.toJson(obj), e);
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    }
                }
                break;
            }
            case Debug: {
                if (logger.isDebugEnabled()) {
                    if (e == null) {
                        if (obj == null) {
                            logger.debug("{}", msg);
                        } else {
                            try {
                                logger.debug(msg + ":{}", JacksonUtils.toJson(obj));
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    } else {
                        if (obj == null) {
                            logger.debug(msg, e);
                        } else {
                            try {
                                logger.debug(msg + ":" + JacksonUtils.toJson(obj), e);
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    }
                }
                break;
            }
            case Trace: {
                if (logger.isTraceEnabled()) {
                    if (e == null) {
                        if (obj == null) {
                            logger.trace("{}", msg);
                        } else {
                            try {
                                logger.trace(msg + ":{}", JacksonUtils.toJson(obj));
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    } else {
                        if (obj == null) {
                            logger.trace(msg, e);
                        } else {
                            try {
                                logger.trace(msg + ":" + JacksonUtils.toJson(obj), e);
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    }
                }
                break;
            }
            default: {
                // info
                if (logger.isInfoEnabled()) {
                    if (e == null) {
                        if (obj == null) {
                            logger.info("{}", msg);
                        } else {
                            try {
                                logger.info(msg + ":{}", JacksonUtils.toJson(obj));
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    } else {
                        if (obj == null) {
                            logger.info(msg, e);
                        } else {
                            try {
                                logger.info(msg + ":" + JacksonUtils.toJson(obj), e);
                            } catch (Exception ex) {
                                logger.warn(ex.toString(), ex);
                            }
                        }
                    }
                }
                break;
            }
        }

    }

    public static boolean isPrd() {
        return StringUtils.startsWithAny(getSpringProfilesActive(), "prd", "prod");
    }

    public static String getSpringProfilesActive() {
        String active = System.getenv("spring.profiles.active");
        if (StringUtils.isBlank(active)) {
            active = System.getProperty("spring.profiles.active");
        }
        return active;
    }

    public static int getChunkSize() {
        return chunkSize;
    }

    public static void setChunkSize(int chunkSize) {
        RtmpUtil.chunkSize = chunkSize;
    }

    public static byte[] sha256(final byte[] message, final byte[] key) {
        final Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mac.doFinal(message);
    }

    public static String obj2str(Object value) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }
}
