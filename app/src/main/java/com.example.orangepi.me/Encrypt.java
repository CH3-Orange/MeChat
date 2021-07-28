package com.example.orangepi.me;

import java.security.MessageDigest;
import java.util.UUID;

public class Encrypt {
    private enum DigestType{
        MD5("MD5")
        ,SHA("SHA")
        ,SHA256("SHA-256")
        ,SHA512("SHA-512");

        private String digestDesc;

        private DigestType(String digestDesc){
            this.digestDesc = digestDesc;
        }

        public String getDigestDesc() {
            return digestDesc;
        }
    }

    private final static String digest(String sourceStr,DigestType type) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        try {
            byte[] btInput = sourceStr.getBytes();
            // 获得MD5摘要算法的MessageDigest对象
            MessageDigest mdInst = MessageDigest.getInstance(type.digestDesc);
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 将密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public final static String MD5(String s) {
        return digest(s, DigestType.MD5);
    }

    public final static String SHA(String s) {
        return digest(s, DigestType.SHA);
    }

    public final static String SHA256(String s){
        return digest(s, DigestType.SHA256);
    }

    public final static String SHA512(String s){
        return digest(s, DigestType.SHA512);
    }
    public final static String GetNewSalt(){
        return UUID.randomUUID().toString();
    }
}
