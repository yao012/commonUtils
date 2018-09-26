import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zijiang.wu
 *
 * 用于优化字符串相关的处理.
 * apache的StringUtils中已经优化过的操作, 不包含在本接口中.
 * 本接口尽量减少内存/cpu消耗, 减少临时对象的产生,
 * 避免使用正则表达式.
 *
 */
public class StringUtil {
    private final static Logger logger = LogManager.getLogger(StringUtil.class);

    /**
     * 示例:
     * containWord("hello,world", "world", ',') == true
     * containWord("hello,world", "hello", ',') == true
     * containWord("hello,world", "ld", ',') == false
     * containWord("hello,world", "he", ',') == false
     *
     * 不接口不产生临时对象, 仅使用遍历的方式即可完成
     * @param src
     * @param search
     * @param separateChar
     * @return
     */
    public static boolean containWord(String src, String search, char separateChar){
        if(StringUtils.isBlank(src) || StringUtils.isBlank(search)){
            return false;
        }
        int pos;
        final int searchLength = search.length();
        final int srcLength = src.length();

        pos = StringUtils.indexOf(src, search);
        if(pos == -1){
            return false;
        }

        //前面必须是分隔符,或者是最开始的位置
        if(pos > 0){
            char c = src.charAt(pos-1) ;
            if(c != separateChar){
                return false;
            }

        }

        //末尾必须是分隔符, 或者结束位置
        int separatePos = pos + searchLength;
        if((separatePos == srcLength) || src.charAt(separatePos) == separateChar ){
            return true;
        }

        return false;
    }

    /**
     * 示例:
     * removeWord("hello,world", "world", ',') == "hello"
     * removeWord("hello,world,world", "world", ',') == "hello,world"
     * removeWord("hello,world,world", "wo", ',') == "hello,world,world"
     *
     * @param src
     * @param search
     * @param separateChar
     */
    public static String removeFirstWord(String src, String search, char separateChar){
        int pos;
        if(StringUtils.isBlank(src) || StringUtils.isBlank(search)){
            return src;
        }

        pos = StringUtils.indexOf(src, search);
        if(pos == -1){
            return src;
        }

        if(pos > 0){
            char c = src.charAt(pos-1) ;
            if(c != separateChar){
                return src;
            }

        }

        int separatePos = pos + search.length();
        if((separatePos == src.length()) || (src.charAt(separatePos) == separateChar) ){
            //找到了匹配的位置, 开始位置为pos(包含), 结束位置为separatePos-1 (包含)
            //即需要拼接这两个位置 [0, pos-1], [separatePos, end];
            //删除中间的的词, 删除额外删除一个分隔符
            //删除开始位置的词, 需要删除后面的分隔符
            //删除结束位置的词, 需要删除前面的分隔符
            //如果完全匹配, 则返回空字符串

            if(pos == 0 && separatePos == src.length()){
                //完全匹配
                return "";
            }

            final StringBuilder buf = new StringBuilder(src.length());
            if(pos == 0){
                separatePos++;
            }else{
                pos--;
            }

            //logger.info("{}, {}", pos, separatePos);
            if(pos>1){

                buf.append(src.substring(0, pos));
            }
            if(separatePos<src.length()){
                String sub = src.substring(separatePos);
                buf.append(sub);
            }
            return buf.toString();
        }else{
            return src;
        }
    }

    /**
     * 过滤掉本系统不支持的字符, 比如emoji
     * @param str
     * @return
     */
    public static String filterUnsupportedChars(String str) {
        if(str == null){
            return null;
        }
        int strlen = str.length();
        //字符串中合法的字符数量
        int supportedChars = 0;
        int c;
        int i;
        for(i=0; i<strlen; i++){
            c = str.charAt(i);
            //0要去除掉
            if(  c==0
                    || (c >= Character.MIN_HIGH_SURROGATE && c < (Character.MAX_HIGH_SURROGATE + 1))
                    || (c >= Character.MIN_LOW_SURROGATE && c < (Character.MAX_LOW_SURROGATE + 1))){
                //处于SURROGATE范围的字符,可能是双char的字符(即4字节的utf8)
            }else{
                supportedChars++;
            }
        }

        if(supportedChars == strlen){
            return str;
        }
        //logger.info("found unsupportedChars");
        StringBuilder builder = new StringBuilder(supportedChars);
        for(i=0; i<strlen; i++){
            c = str.charAt(i);
            //0要去除掉
            if(  c==0
                    || (c >= Character.MIN_HIGH_SURROGATE && c < (Character.MAX_HIGH_SURROGATE + 1))
                    || (c >= Character.MIN_LOW_SURROGATE && c < (Character.MAX_LOW_SURROGATE + 1))){
                //处于SURROGATE范围的字符,可能是双char的字符(即4字节的utf8)
            }else{
                builder.append((char)c);
            }
        }
        return builder.toString();
    }


    /**
     * 允许用户自定义是否将相邻分隔符视为空令牌分隔符
     * 或是相邻分隔符认为是一个分隔符
     * StringUtils类只是把相邻分隔符视为一个分隔符,没有给用户选择的机会
     *
     * <p>Splits the provided text into an array, separators specified.
     * This is an alternative to using StringTokenizer.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * A {@code null} separatorChars splits on whitespace.</p>
     *
     * <pre>
     * StringUtils.split(null, *)         = null
     * StringUtils.split("", *)           = []
     * StringUtils.split("abc def", null) = ["abc", "def"]
     * StringUtils.split("abc def", " ")  = ["abc", "def"]
     * StringUtils.split("abc  def", " ") = ["abc", "def"]
     * StringUtils.split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
     * </pre>
     *
     * @param str  the String to parse, may be null
     * @param separatorChars  the characters used as the delimiters,
     * @param preserveAllTokens  if {@code true}, adjacent separators are
     * treated as empty token separators; if {@code false}, adjacent
     * separators are treated as one separator.
     *  {@code null} splits on whitespace
     * @return an array of parsed Strings, {@code null} if null String input
     */
    public static String[] split(final String str, final String separatorChars,final boolean preserveAllTokens) {
        return splitWorker(str, separatorChars, -1, preserveAllTokens);
    }


    /**
     * Performs the logic for the {@code split} and
     * {@code splitPreserveAllTokens} methods that return a maximum array
     * length.
     *
     * @param str  the String to parse, may be {@code null}
     * @param separatorChars the separate character
     * @param max  the maximum number of elements to include in the
     *  array. A zero or negative value implies no limit.
     * @param preserveAllTokens if {@code true}, adjacent separators are
     * treated as empty token separators; if {@code false}, adjacent
     * separators are treated as one separator.
     * @return an array of parsed Strings, {@code null} if null String input
     */
    private static String[] splitWorker(final String str, final String separatorChars, final int max, final boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final List<String> list = new ArrayList<>();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            final char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }






    /**===================================================================================================
 UTF8编码相关的接口
 1. unicode与utf-8的关系.
 unicode(16进制显示)   utf8(二进制显示)
 00000000 - 0000007F: 0xxxxxxx
 00000080 - 000007FF: 110xxxxx 10xxxxxx
 00000800 - 0000FFFF: 1110xxxx 10xxxxxx 10xxxxxx
 00010000 - 001FFFFF: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
 00200000 - 03FFFFFF: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
 04000000 - 7FFFFFFF: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx

 [0x00, 0x7F]为ascii码, unicode==utf8
 [0x80, 0x7FF]包含latin等字符. 11位有效bits. utf8编码方式为 110-高5位-10-低6位
 [0x800, 0xFFFF]包含中日韩(CJK)等字符, 16位有效bits, utf8编码方式为 1110-高4位-10-中6位-10-低6位
 [0x10000, 0x1FFFFF]包含emoji表情字符, 21位有效bits, utf8编码方式为 11110-高3位-10-高6位-10-中6位-10-低6位

 unicode转utf方法:
 只需要按照上述的分段规则, 执行不同的移位操作, 即可生成utf8.
 utf8转unicode方法:
 读取出高位连续1的数量, 从而判断出当前字符所占用的字节数, 然后按不同的分段, 将有效的bits拼接起来.

 2.	java中char与unicode(也称code point)的关系.
 char为无符号16位.
 [0x0000, 0xFFFF]间的unicode只需要一个char即可存储. 即unicode==char;
 [0x10000, 0x7FFFFFFF]间的unicode需要两个char来存储.
 在java中, 为了区分一个char是独立的unicode, 还是unicode的高低位, 采用了以下的策略:
 如果有两个相邻char: high, low, 如果high处于[0xD800, 0XDBFF], low处于[0xDC00, 0XDFFF],
 则需要按一定规则将这两个char组合成一个unicode, 具体 @see java.lang.Character#toCodePoint(char, char)
由于high和low的范围很狭窄, 导致其表示的范围仅为[0x10000, 0x10ffff], 即java目前最多可支持4字节字符.
 [0xD800, 0XDBFF], [0xD800, 0XDBFF]是unicode中一段保留区域, 称为Surrogate

 资料网站: http://www.utf8-chartable.de/unicode-utf8-table.pl

 =====================================================================================================*/

    private static class StringReflectUtil {

        private static Field value;
        private static char[] empty = new char[0];

        static {
            try {
                value = String.class.getDeclaredField("value");
                value.setAccessible(true);
            } catch (NoSuchFieldException e) {

            }
        }

        public static char[] getChars(String str) {
            try {
                return (char[]) value.get(str);
            } catch (IllegalAccessException e) {
                return empty;
            }
        }

    }

    /**
     * 计算String的utf8编码的长度, 支持4字节的字符.
     * 本接口不需要实际编码转换, 不会产生副作用.
     * @param str 入参不能为null
     * @return
     */
    public static int utf8Length(String str){
        int strlen = str.length();
        int utflen = 0;
        int c;
        int i=0;

        if(strlen > 256){
            char[] chars = StringReflectUtil.getChars(str);
            for (; i < strlen; i++) {
                //char转换成int不会出现负数, char是无符号类型, 高位用0补位.
                c = chars[i];
                //java的字符串中允许包含0
                if ((c >= 0x0000) && (c <= 0x007F)) {
                    utflen++;
                } else if (c > 0x007F && c <= 0x07FF) {
                    utflen += 2;
                } else if (c >= Character.MIN_HIGH_SURROGATE && c < Character.MAX_HIGH_SURROGATE + 1) {
                    //判断是否需要使用两个char来组成一个unicode.
                    if (i + 1 == strlen) {
                        //已经是最后一个字符了.
                        utflen += 3;
                    } else {
                        char low = str.charAt(i + 1);
                        if (low >= Character.MIN_LOW_SURROGATE && low < (Character.MAX_LOW_SURROGATE + 1)) {
                            utflen += 4;
                            i++;
                        } else {
                            utflen += 3;
                        }
                    }
                } else {
                    utflen += 3;
                }
            }
        }else {
            for (; i < strlen; i++) {
                //char转换成int不会出现负数, char是无符号类型, 高位用0补位.
                c = str.charAt(i);
                //java的字符串中允许包含0
                if ((c >= 0x0000) && (c <= 0x007F)) {
                    utflen++;
                } else if (c > 0x007F && c <= 0x07FF) {
                    utflen += 2;
                } else if (c >= Character.MIN_HIGH_SURROGATE && c < Character.MAX_HIGH_SURROGATE + 1) {
                    //判断是否需要使用两个char来组成一个unicode.
                    if (i + 1 == strlen) {
                        //已经是最后一个字符了.
                        utflen += 3;
                    } else {
                        char low = str.charAt(i + 1);
                        if (low >= Character.MIN_LOW_SURROGATE && low < (Character.MAX_LOW_SURROGATE + 1)) {
                            utflen += 4;
                            i++;
                            /** 从下面的公式可以推断出unicode的值范围为[10000, 10ffff], 必定为4个字符 */
                            /**
                             使用两个char组合成一个unicode
                             int unicode = ((c - Character.MIN_HIGH_SURROGATE) << 10)
                             + (low - Character.MIN_LOW_SURROGATE)
                             + Character.MIN_SUPPLEMENTARY_CODE_POINT;
                             if(unicode >= 0x10000 && unicode <=0x1FFFFF){
                             //logger.info("4 bytes utf8");
                             utflen += 4;
                             }else if(unicode >= 0x200000 && unicode <=0x3FFFFFF){
                             logger.info("5 bytes utf8");
                             utflen += 5;
                             }else{
                             logger.info("6 bytes utf8");
                             utflen += 6;
                             }
                             */
                        } else {
                            utflen += 3;
                        }
                    }
                } else {
                    utflen += 3;
                }
            }
        }
        return utflen;
    }

    /**
     * 将str编码成utf8, 存入到bytes中, bytes必须保证还有足够的空间, 否则将出错.
     * 以下是与原生的str.getBytes(StandardCharsets.UTF_8)方式的对比结果,
     * 编码速度:
     * linux(1核1线程), 本接口略快, 但不明显;
     * windows(双核4线程), 本接口有较大优势
     * 内存使用:
     * 原生接口产生较多的临时对象, 导致young区域gc频繁.
     * 本接口不会产生临时对象, gc次数为原生接口的1/4.
     * @param str
     * @param bytes
     * @param offset
     * @return 返回存入的字节数.
     */
    public static int getUtf8Bytes(String str, byte[] bytes, int offset){
        int strlen = str.length();
        int c;
        int count = offset;
        int i=0;

        if(strlen > 256){
            char[] chars = StringReflectUtil.getChars(str);
            for (; i < strlen; i++) {
                c = chars[i];
                if ((c >= 0x0000) && (c <= 0x007F)) {
                    //ascii
                    bytes[count++] = (byte) c;
                } else if (c > 0x007F && c <= 0x07FF) {
                    //latin及其它双字节字符
                    bytes[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                    bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                } else if (c >= Character.MIN_HIGH_SURROGATE && c < Character.MAX_HIGH_SURROGATE + 1) {
                    //可能是4字节字符
                    if (i + 1 == strlen) {
                        //已经是最后一个字符了.作为普通双字节字符处理
                        bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                        bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                        bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                    } else {
                        char low = str.charAt(i + 1);
                        if (low >= Character.MIN_LOW_SURROGATE && low < (Character.MAX_LOW_SURROGATE + 1)) {
                            i++;
                            /** 使用两个char组合成一个unicode */
                            int unicode = ((c - Character.MIN_HIGH_SURROGATE) << 10)
                                    + (low - Character.MIN_LOW_SURROGATE)
                                    + Character.MIN_SUPPLEMENTARY_CODE_POINT;
                            /** 从公式可以推断出unicode的值范围为[10000, 10ffff], 必定为4个字符 */
                            /** 其处于区间[0x10000, 0x1FFFFF], 最多21 bit
                             * 共24bit, 从高位开始分别取出3bit, 6 bit, 6 bit, 6 bit
                             * utf8以11110开始.
                             */
                            bytes[count++] = (byte) (0xF0 | ((unicode >> 18) & 0x07));
                            bytes[count++] = (byte) (0x80 | ((unicode >> 12) & 0x3F));
                            bytes[count++] = (byte) (0x80 | ((unicode >> 6) & 0x3F));
                            bytes[count++] = (byte) (0x80 | ((unicode >> 0) & 0x3F));
                        } else {
                            bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                            bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                            bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                        }
                    }
                } else {
                    //3字节字符, unicode范围为[0x800, 0xffff], 最多16bit, 从高位开始, 分别取出4bit, 6bit, 6bit
                    //utf8编码以1110开始
                    bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                    bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                    bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                }
            }

        }else {

            for (; i < strlen; i++) {
                c = str.charAt(i);
                if ((c >= 0x0000) && (c <= 0x007F)) {
                    //ascii
                    bytes[count++] = (byte) c;
                } else if (c > 0x007F && c <= 0x07FF) {
                    //latin及其它双字节字符
                    bytes[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                    bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                } else if (c >= Character.MIN_HIGH_SURROGATE && c < Character.MAX_HIGH_SURROGATE + 1) {
                    //可能是4字节字符
                    if (i + 1 == strlen) {
                        //已经是最后一个字符了.作为普通双字节字符处理
                        bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                        bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                        bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                    } else {
                        char low = str.charAt(i + 1);
                        if (low >= Character.MIN_LOW_SURROGATE && low < (Character.MAX_LOW_SURROGATE + 1)) {
                            i++;
                            /** 使用两个char组合成一个unicode */
                            int unicode = ((c - Character.MIN_HIGH_SURROGATE) << 10)
                                    + (low - Character.MIN_LOW_SURROGATE)
                                    + Character.MIN_SUPPLEMENTARY_CODE_POINT;
                            /** 从公式可以推断出unicode的值范围为[10000, 10ffff], 必定为4个字符 */
                            /** 其处于区间[0x10000, 0x1FFFFF], 最多21 bit
                             * 共24bit, 从高位开始分别取出3bit, 6 bit, 6 bit, 6 bit
                             * utf8以11110开始.
                             */
                            bytes[count++] = (byte) (0xF0 | ((unicode >> 18) & 0x07));
                            bytes[count++] = (byte) (0x80 | ((unicode >> 12) & 0x3F));
                            bytes[count++] = (byte) (0x80 | ((unicode >> 6) & 0x3F));
                            bytes[count++] = (byte) (0x80 | ((unicode >> 0) & 0x3F));
                        } else {
                            bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                            bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                            bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                        }
                    }
                } else {
                    //3字节字符, unicode范围为[0x800, 0xffff], 最多16bit, 从高位开始, 分别取出4bit, 6bit, 6bit
                    //utf8编码以1110开始
                    bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                    bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                    bytes[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
                }
            }
        }
        return count - offset;
    }

    public static int getAsciiBytes(String str, byte[] bytes, int offset){
        int strlen = str.length();
        if(strlen > 256) {
            char[] chars = StringReflectUtil.getChars(str);
            for(int i=0; i<strlen; i++){
                bytes[offset++] = (byte) chars[i];
            }
        }else{
            for(int i=0; i<strlen; i++){
                bytes[offset++] = (byte)str.charAt(i);
            }
        }
        return strlen;
    }

    public static byte[] getAsciiBytes(String str){
        int len = str.length();
        byte[] bytearr = new byte[len];
        getAsciiBytes(str, bytearr, 0);
        return bytearr;
    }

    public static byte[] getAsciiBytes(CharSequence str){
        Class cls = str.getClass();
        if(cls == AsciiString.class){
            return ((AsciiString)str).getBytes();
        }else if(cls == String.class){
            return getAsciiBytes(((String)str));
        }else{
            //未实现
            return new byte[0];
        }

    }

    /**
     * 装str编码成utf-8.
     * @see #getUtf8Bytes(String, byte[], int)
     * @param str
     * @return
     */
    public static byte[] getUtf8Bytes(String str){
        int utflen = utf8Length(str);
        byte[] bytearr = new byte[utflen];
        int outLen = getUtf8Bytes(str, bytearr, 0);
        if(outLen != utflen){
            logger.fatal("utf-8 encoding error");
        }
        return bytearr;
    }

    public static String truncate(String str, int maxSize)
    {
        if (str == null)
        {
            return null;
        }

        if (str.length() <= maxSize)
        {
            return str;
        }

        return str.substring(0,maxSize);
    }


    /**
     * 判断字符串是否是正实数，是否有效数字最多 n 位
     * @param candidate
     * @param fixed
     * @return
     */
    public static boolean isDecimal(String candidate, int fixed) {
        if(candidate == null ) {
            return false;
        }

        int length = candidate.length();
        if(length == 0) {
            return false;
        }

        boolean waitingDecimal = true;
        for (int i = 0; i < length; i++) {

            if(i == 0) { // 不能以小数点开头
                if (!Character.isDigit(candidate.charAt(i))) {
                    return false;
                }
            } else {
                char ch = candidate.charAt(i);
                if (!Character.isDigit(ch)) {
                    if(waitingDecimal && '.' == ch) {
                        // 接下来为小数部分，现在观察小数位是否超过有效精度
                        if((length - i - 1) > fixed) {
                            return false;
                        }

                        waitingDecimal = false;
                    } else {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}














