package com.suineng.common;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DigestUtil {

    @Value("${user.key:1234567}")
    private String key;

    public String md5(String text)
    {
        //加密后的字符串
        String encodeStr= DigestUtils.md5Hex(text + key);
        return encodeStr;
    }

    public boolean verify(String text, String md5) throws Exception {
        //根据传入的密钥进行验证
        String md5Text = md5(text);
        if(md5Text.equalsIgnoreCase(md5))
        {
            return true;
        }
        return false;
    }
}
