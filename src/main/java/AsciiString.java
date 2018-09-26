import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 类似于String, 但是易于修改, 更省内存, 只支持ascii字符
 */
public class AsciiString implements Comparable<AsciiString>, CharSequence {

    /** ascii值, 如果开发者传入了非ascii码, 后果自负 */
    private byte[] value;

    /** hash值, value的hash计算过程与String的hash计算过程一样, 值也一样 */
    private int hash;

    private final static byte[] EMPTY_BYTES = new byte[0];

    public final static AsciiString EMPTY_STR = new AsciiString();

    public AsciiString(){
        value = EMPTY_BYTES;
    }

    public AsciiString(byte[] bytes){
        this.value = bytes;
    }

    public AsciiString(String ascii){
        this.value = StringUtil.getAsciiBytes(ascii);
        this.hash = ascii.hashCode();
    }

    public AsciiString(byte[] bytes, int hashCode){
        this.value = bytes;
        this.hash = hashCode;
    }

    public AsciiString(byte[] bytes, int offset, int length){
        this.value = Arrays.copyOfRange(bytes, offset, offset+length);
    }

    public AsciiString(byte[] bytes, int offset, int length, int hashCode){
        this.value = Arrays.copyOfRange(bytes, offset, offset+length);
        this.hash = hashCode;
    }

    public AsciiString(ByteBuffer buffer, int hashCode){
        this(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining(), hashCode);
    }

    public byte[] getBytes(){
        return value;
    }

    @Override
    public int length() {
        return value.length;
    }

    @Override
    public char charAt(int index) {
        return (char)value[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (end > value.length) {
            throw new StringIndexOutOfBoundsException(end);
        }
        int subLen = end - start;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return ((start == 0) && (end == value.length)) ? this
                : new AsciiString(value, start, subLen);
    }

    @Override
    public String toString(){
        return new String(value, StandardCharsets.US_ASCII);
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        Class anCls = anObject.getClass();
        if(anCls == AsciiString.class) {
            AsciiString another = (AsciiString)anObject;
            int n = value.length;
            if (n == another.value.length) {
                byte v1[] = value;
                byte v2[] = another.value;
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i]) {
                        return false;
                    }
                    i++;
                }
                return true;
            }
        }else if (anObject instanceof CharSequence) {
            CharSequence another = (CharSequence)anObject;
            if(value.length == another.length()){
                for(int i=0; i<value.length; i++){
                    if(value[i] != another.charAt(i)){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        int h = hash;
        if (h == 0 && value.length>0) {
            for (int i = 0; i < value.length; i++) {
                h = 31 * h + value[i];
            }
            hash = h;
        }
        return h;
    }


    @Override
    public int compareTo(AsciiString o) {
        int len1 = value.length;
        int len2 = o.value.length;
        int lim = Math.min(len1, len2);
        byte v1[] = value;
        byte v2[] = o.value;

        int k = 0;
        while (k < lim) {
            byte c1 = v1[k];
            byte c2 = v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    public int compareTo(CharSequence cs){
        if(cs.getClass() == AsciiString.class){
            return compareTo((AsciiString)cs);
        }

        int len1 = value.length;
        int len2 = cs.length();
        int lim = Math.min(len1, len2);
        byte v1[] = value;

        int k = 0;
        while (k < lim) {
            byte c1 = v1[k];
            char c2 = cs.charAt(k);
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

}
