package com.shine.usbcameralib.serialdog.serialutil;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Random;

public class Toolkit {

	/** 
     * utf-8字符串转gbk字符串<br/>  
     * @param str
     * @return gbk
     */
	public static byte[] UTF8ToGBK(String str) {
		byte[] bytes = new byte[0];
		if (str != null) {
			try {
				bytes = str.getBytes("gbk");
			} catch (UnsupportedEncodingException e) {
				System.out.println("UTF8转GBK错误");
			}
		}
		return bytes;
	}
	
    /** 
     * hex字符串转byte数组<br/> 
     * 2个hex转为一个byte 
     * @param src 
     * @return 
     */  
	public static byte[] hex2Bytes(String src){
		byte[] res = new byte[src.length()/2];
        char[] chs = src.toCharArray();  
        int[] b = new int[2];  
  
        for(int i=0,c=0; i<chs.length; i+=2,c++){              
            for(int j=0; j<2; j++){  
                if(chs[i+j]>='0' && chs[i+j]<='9'){  
                    b[j] = (chs[i+j]-'0');  
                }else if(chs[i+j]>='A' && chs[i+j]<='F'){  
                    b[j] = (chs[i+j]-'A'+10);  
                }else if(chs[i+j]>='a' && chs[i+j]<='f'){  
                    b[j] = (chs[i+j]-'a'+10);  
                }  
            }              
            b[0] = (b[0]&0x0f)<<4;  
            b[1] = (b[1]&0x0f);  
            res[c] = (byte) (b[0] | b[1]);  
        }

        return res;
    }
	
	/** 
     * byte数组转hex字符串<br/> 
     * 一个byte转为2个hex字符 
     * @param src 
     * @return res
     */  
    public static String bytes2Hex(byte[] src){
        char[] res = new char[src.length*2];    
        final char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};    
        for(int i=0,j=0; i<src.length; i++){    
            res[j++] = hexDigits[src[i] >>>4 & 0x0f];    
            res[j++] = hexDigits[src[i] & 0x0f];    
        }
        return new String(res);
    }   

    
    /** 
     * 验证摘要与数据一致性 
     * @param digest 摘要 
     * @param src 数据源 
     * @param offset 偏移量 
     * @param len 数据源长度 
     * @return true:一致,false:不一致 
     */  
    public static boolean checkDigest(byte[] digest, byte[] src, int offset, int len){  
        boolean ok = false;  
        final int secret_size = 8;  
        byte[] sign = new byte[secret_size+16];       
          
        try{  
            System.arraycopy(digest, 0, sign, 0, secret_size);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(digest,0,secret_size);  
            md5.update(src,offset,len);  
            md5.digest(sign,secret_size,sign.length-secret_size);  
            ok = Toolkit.bytes2Hex(digest).equals(Toolkit.bytes2Hex(sign));  
        }catch(Exception ex){
            ex.printStackTrace();  
        }  
           	return ok;  
    }  
    
    /** 
     * 生成包含8位随机码+16位MD5的消息摘要 
     * @param src 数据源 
     * @param offset 偏移量 
     * @param len 长度 
     * @return 如果执行成功则返回8+16位的字节摘要,否则返回null 
     */  
    public static byte[] digest(byte[] src, int offset, int len){  
        final int secret_size = 8;  
        byte[] secret = new byte[secret_size];  
        byte[] sign = new byte[secret_size+16];       
        new Random().nextBytes(secret);
          
        try{  
            System.arraycopy(secret, 0, sign, 0, secret_size);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(secret);  
            md5.update(src,offset,len);  
            md5.digest(sign,secret_size,sign.length-secret_size);  
        }catch(Exception ex){
            ex.printStackTrace();  
            sign = null;  
        }        
        return sign;  
    }
    
    public static byte[] getImageGRB(Bitmap bufImg) {
    	byte[] result = null;
	
        int height = bufImg.getHeight();
        int width = bufImg.getWidth();
        String imgHex = new String();

        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < height; i++) {
        	for (int j = 0; j < width; j++) {
        	    int color = bufImg.getPixel(j, i);
            	 
        	    int r = Color.red(color) >> 3;
        	 	int g = Color.green(color) >> 2;
                int b = Color.blue(color) >> 3;
        	 
                r = r << 11;
                g = g << 5;

                imgHex =  Integer.toHexString(r+g+b);
                if (imgHex.length() == 1) {
                	imgHex = "000" + imgHex;
                } else if (imgHex.length() == 2) {
                	imgHex = "00" + imgHex;
                } else if (imgHex.length() == 3) {
                	imgHex = "0" + imgHex;
                }
                hex.append(imgHex);
        	}
       }
       
       result = hex2Bytes(new String(hex));
       bufImg.recycle();
       return result;
    }
    
    
    public static byte[][] getFont(byte[] bytes,String id) {
    	byte[][] resbytes = null;
        byte[] result = bytes;
        
        int len = result.length/128;
		if (result.length%128 !=  0) {
			len = len + 1;
		}
		
		resbytes = new byte[len][128];
		for (int i = 0; i < len-1; i++) {
			System.arraycopy(result, i*128, resbytes[i], 0, 128);
			
			String a = bytes2Hex(resbytes[i]);

			String tmp = "55" + id + "02" + "80" + a;
			
			resbytes[i] = hex2Bytes(tmp);
		}

		resbytes[len-1] = new byte[result.length - (len-1)*128];
		System.arraycopy(result, (len-1)*128, resbytes[len-1], 0, result.length - (len-1)*128);
		
		String a = bytes2Hex(resbytes[len-1]);
		
		String length = Integer.toHexString(a.length()/2).toUpperCase(Locale.CHINA);

		if (length.length() == 1) {
			length = "0" + length;
		}
		String tmp = "55" + id + "02" + length + a;

		resbytes[len-1] = hex2Bytes(tmp);

        return resbytes;
    }
}
