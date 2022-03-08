package org.example.common.utils;

/**
 * @author : lzp
 * @date :
 */
public class TimeUtils {


    public static String getTimestamp() {
        return String.format("%013d", System.currentTimeMillis());
    }

    // key : {ticket(0, 13)}{timestamp(0, 13)}

    /**
     *  获取 非时间戳部分
     * @param key
     * @return
     */
    public static String getBareKey(String key) {
        if(key.length() < 13) {
            return key;
        }else{
            return key.substring(0, key.length() - 13);
        }
    }

    /**
     *  获取时间戳部分
     * @param key
     * @return
     */
    public static String getTimePart(String key) {
        if(key.length() < 13) {
            return null;
        } else {
            return key.substring(key.length() - 13);
        }
    }

}
