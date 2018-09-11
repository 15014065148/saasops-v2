package com.eveb.saasops.api.utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.eveb.saasops.api.constants.PbConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import com.eveb.saasops.common.exception.RRException;
import org.springframework.util.StringUtils;

@Slf4j
public class CipherText {
	/**
     * AES加密为base 64 code
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return base64Encode(encrypt(content, encryptKey));
    }
	/**
     * AES加密为base 64 code
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     * @throws Exception
     */
    public static String hexEncrypt(String content, String encryptKey) throws Exception {
        return base64HexEncode(encrypt(content, encryptKey));
    }
    /**
     * 将base 64 code AES解密
     * @param encryptStr 待解密的base 64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return encryptStr==null? null : decrypt(base64Decode(encryptStr), decryptKey);
    }

    /**
     * base 64 encode
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    public static String base64HexEncode(byte[] bytes){
        return Hex.encodeHexString(bytes);
    }
    
    /**
     * base 64 encode
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    public static String base64Encode(byte[] bytes){
        return  Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base 64 decode
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception
     */
    public static byte[] base64Decode(String base64Code) throws Exception{
        return base64Code==null ? null : Base64.getDecoder().decode(base64Code);
    }

    /**
     * 加密
     *
     * @param content
     * @param strKey
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(String content, String strKey,String initVector) throws Exception {
        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(StringUtils.isEmpty(initVector)?strKey.getBytes():initVector.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(content.getBytes());
        return encrypted;
    }
    public static byte[] encrypt(String content, String strKey) throws Exception
    {
        return encrypt(content,strKey,null);
    }
    /**
     * 解密
     *
     * @param strKey
     * @param content
     * @return
     * @throws Exception
     */
    public static String decrypt(byte[] content, String strKey, String initVector) throws Exception {
        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(StringUtils.isEmpty(initVector) ? strKey.getBytes() : initVector.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] original = cipher.doFinal(content);
        String originalString = new String(original);
        return originalString;
    }

    public static String decrypt(byte[] content, String strKey) throws Exception {
        return decrypt(content, strKey, null);
    }

    private static SecretKeySpec getKey(String strKey) throws Exception {
        byte[] arrBTmp = strKey.getBytes();
        byte[] arrB = new byte[16]; // 创建一个空的16位字节数组（默认值为0）

        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }

        SecretKeySpec skeySpec = new SecretKeySpec(arrB, "AES");

        return skeySpec;
    }

    /**
     * 加密
     * @param context
     * @param key
     * @return
     */
	public static String getEncrypt(String context, String key) {
		try {
			return aesEncrypt(context, key);
		} catch (Exception e) {
            log.debug("An exceptionn occurred when encript key: {}, plain text: {}, exception {}", new Object[]{key, context, e.toString()});
			throw new RRException("加密数据异常!");
		}
	}

    /**
     * 加密
     * @param context
     * @param key
     * @return
     */
    public static String getPbEncrypt(String context, String key) {
        try {
            return base64Encode(encrypt(context, key, PbConstants.INIT_VECTOR));
        } catch (Exception e) {
            log.debug("An exceptionn occurred when encript key: {}, plain text: {}, exception {}", new Object[]{key, context, e.toString()});
            throw new RRException("加密数据异常!");
        }
    }

    /**
     * 解密
     * @param context
     * @param key
     * @return
     */
	public static String getDecrypt(String context, String key) {
		try {
			return aesDecrypt(context, key);
		} catch (Exception e) {
            log.debug("An exceptionn occurred when encript key: {}, plain text: {}, exception {}", new Object[]{key, context, e.toString()});
			throw new RRException("解密数据异常!");
		}
	}

   /* public static String encryptAES(String secretKey, String tokenPayLoad) {
        try {
            String ALGORITHM = "AES";
            String INIT_VECTOR = "RandomInitVector";
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encrypted = cipher.doFinal(tokenPayLoad.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            log.debug("An exceptionn occurred when encript key: {0}, plain text: {1}, exception {2}", new Object[]{secretKey, tokenPayLoad, ex.toString()});
        }
        return null;
    }*/

   /* public static void main(String args[]) {
        try {
            //System.out.println("*****");
            //String string=CipherText.hexEncrypt("cagent=eveb&loginname=testdanny007&password=6b6c51a84f25c60276368a1661a1489e&dm=&sid=&token=&lang=1&gameType=0&oddtype=A&time=1520561858","PZD5n91AepGINMw2");
            //System.out.println(string);
            String s1 = getPbEncrypt("qweqweqweqweqwesdafasdcxzasdfasd", "4VfLoFh0OOlLuF6t");
            System.out.println(s1);
            s1 = encryptAES("4VfLoFh0OOlLuF6t", "qweqweqweqweqwesdafasdcxzasdfasd");
            System.out.println(s1);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
}
