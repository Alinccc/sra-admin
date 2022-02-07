package com.jwss.sra.framework.constant;

/**
 * redis缓存键
 * @author jwss
 */
public class RedisKey {
    /**
     * 验证码键：第一个参数为验证码类型（必填），第二个参数为
     */
    public final static String VERIFY_CODE = "%s_CODE__%s";

    /**
     * 在线用户，参数为用户id
     */
    public final static String ONLINE_USER = "ONLINE_USER_%s";
}
