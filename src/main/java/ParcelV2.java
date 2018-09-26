import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Created by zijiang.wu on 2017/6/21.
 *  {@link Parcel}}的升级优化版,与之兼容
 * 优化点:
 *  1. 不再使用ArrayList<Object>来暂存需要序列化的元素, 减少整数的装包操作
 *  2. 使用{@link StringUtil#getUtf8Bytes(String, byte[], int) }来优化字符串的序列化过程.
 *  3. 尽量使用工具来生成序列化的代码. {@link ParcelUtil}
 *
 * 本工具序列化与反序列化时, 字段的顺序要一致, 否则出错.
 *
 */
public abstract class ParcelV2 {
    private final static Logger logger = LogManager.getLogger(ParcelV2.class);

    /** 数据 */
    private byte[] _buffer;

    private int _offset;

    /** 当前的读写位置 */
    private int _position;

    /** _buffer中数据的结束位置(不包含) */
    private int _end;

    /** 字符串的最大长度, 防止严重错误 */
    public static final int MAX_STRING_LENGTH = 10*1024;

    /** 序列化成byte[],  需要调用者使用工具生成 */
    public abstract byte[] ser();

    /** 计算总长度, 需要调用者使用工具生成 */
    public abstract int calculateSize();

    /** 反序列化过程, 需要调用者使用工具生成 */
    public abstract void deser(byte[] b, int offset, int len);

    public void deser(byte[] b){
    	deser(b, 0, b.length);
    }

    /** 仅仅是为了调用其它的一些read接口 */
    public static class BasicParcel extends ParcelV2{

        @Override
        public byte[] ser() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int calculateSize() {
            throw new UnsupportedOperationException();
        }
        @Override
        public void deser(byte[] b, int offset, int len) {
            throw new UnsupportedOperationException();
        }
    }


    public void setBuffer(byte[] bytes, int offset, int len){
        this._buffer = bytes;
        this._offset = offset;
        this._position = offset;
        this._end = len + offset;
    }

    public byte[] getBuffer(){
        return _buffer;
    }

    public void setBuffer(int length){
        this._buffer = new byte[length];
        this._position = 0;
        this._end = length;
    }

    public int calcBoolean(boolean value){
        return 1;
    }

    public void writeBoolean(boolean value){
        _buffer[_position++] = (byte) (value ? 1 : 0);
    }

    /** 获取已经写入的数据大小 */
    public int getWritedSize(){
        return _position - _offset;
    }


    public boolean readBoolean(){
        if(this._end - this._position < 1){
            return false;
        }
        byte b = _buffer[_position++];
        return b == 1;
    }

    public int calcByte(byte value){
        return 1;
    }
    public int calcByte(int value){
        return 1;
    }
    
    public void writeByte(int value){
        _buffer[_position++] = (byte)value;
    }

    public byte readByte(){
        if(this._end - this._position < 1){
            return 0;
        }
        byte b = _buffer[_position++];
        return b;
    }

    public int calcShort(short value){
        return 2;
    }
    
    public void writeShort(short value){
        _buffer[_position++] = (byte) ((value) & 0xFF);
        _buffer[_position++] = (byte) ((value >> 8) & 0xFF);
    }


    public short readShort(){
        if(this._end - this._position < 2){
            return 0;
        }
        short x = (short) (
                (_buffer[_position] & 0xff)
                        | ((_buffer[_position + 1] & 0xff) <<8)
        );
        _position += 2;
        return x;
    }

    public int calcInt(int value){
        return 4;
    }
    
    public void writeInt(int value){
        _buffer[_position++] = (byte) (value);
        _buffer[_position++] = (byte) (value >> 8);
        _buffer[_position++] = (byte) (value >> 16);
        _buffer[_position++] = (byte) (value >> 24);
    }

    public int readInt(){
        if(this._end - this._position < 4){
            this._position = this._end;
            logger.error("read int failed, no more data");
            return 0;
        }
        int x = (((_buffer[_position] & 0xff))
                | ((_buffer[_position + 1] & 0xff) << 8)
                | ((_buffer[_position + 2] & 0xff) << 16)
                | ((_buffer[_position + 3] & 0xff) << 24));
        _position += 4;
        return x;
    }

    public int calcLong(long value){
        return 8;
    }
 
    public void writeLong(long value){
        _buffer[_position++] = (byte) (value);
        _buffer[_position++] = (byte) (value >> 8);
        _buffer[_position++] = (byte) (value >> 16);
        _buffer[_position++] = (byte) (value >> 24);
        _buffer[_position++] = (byte) (value >> 32);
        _buffer[_position++] = (byte) (value >> 40);
        _buffer[_position++] = (byte) (value >> 48);
        _buffer[_position++] = (byte) (value >> 56);
    }


    public long readLong(){
        if(this._end - this._position < 8){
            this._position = this._end;
            logger.error("read long failed, no more data");
            return 0;
        }
        return _readLong();
    }

    private long _readLong(){
        long x = ((((long) _buffer[_position + 0] & 0xffL))
                | (((long) _buffer[_position + 1] & 0xffL) << 8)
                | (((long) _buffer[_position + 2] & 0xffL) << 16)
                | (((long) _buffer[_position + 3] & 0xffL) << 24)
                | (((long) _buffer[_position + 4] & 0xffL) << 32)
                | (((long) _buffer[_position + 5] & 0xffL) << 40)
                | (((long) _buffer[_position + 6] & 0xffL) << 48)
                | (((long) _buffer[_position + 7] & 0xffL) << 56));
        _position += 8;
        return x;
    }

    /**
     * 供 {@link ParcelV2#readDate()} 使用
     *
     * @return -1 short read exception
     */
    public long readLong2(){
        if(this._end - this._position < 8){
            this._position = this._end;
            logger.error("read long failed, no more data");
            return -1L;
        }

        return _readLong();
    }

    public int calcDate(Date date){
        return 8;
    }

    public void writeDate(Date date){
        if(date == null){
            writeLong(Long.MIN_VALUE);
        }else {
            writeLong(date.getTime());
        }
    }

    public Date readDate(){
        long value = readLong2();
        if(value < 0){
            return null;
        }else {
            return new Date(value);
        }
    }

    public int calcString(String value){
        if(value == null){
            return 4;
        }
        int strLen = value.length();
        if(strLen == 0){
            return 4;
        }else if(strLen>MAX_STRING_LENGTH){
            return 4;
        }else{
            return 4 + StringUtil.utf8Length(value);
        }
    }

    public void writeString(String str){
    	if(str == null){
    		writeInt(-1);
    	}else{
    	    int strLen = str.length();
            if(strLen == 0){
                writeInt(0);
                return;
            }else if(strLen > MAX_STRING_LENGTH){
                logger.fatal("string length too long: {}, should less than {}", strLen, MAX_STRING_LENGTH);
                writeInt(0);
                return;
            }else {
                int utf8Length = StringUtil.utf8Length(str);
                writeInt(utf8Length);
                StringUtil.getUtf8Bytes(str, _buffer, _position);
                _position += utf8Length;
            }
    	}
    }

    public String readString(){
        if(this._end - this._position < 4){
            logger.error("read string failed, no more data");
            return null;
        }
        int len = readInt();
        if(len == 0){
            return "";
        }else if(len <0){
            return null;
        }else if(len>(MAX_STRING_LENGTH*3)){
            //太大了, 可能是调用者搞错了接口
            logger.error("string too big");
            this._position = this._end;
            //为了不让错误继续传递下去
            return null;
        }else if(this._end - this._position < len){
            logger.error("read string failed, no more data");
            this._position = this._end;
            return null;
        }
        String str = new String(this._buffer, this._position, len, StandardCharsets.UTF_8);
        this._position += len;
        return str;
    }

    public int calcAsciiString(AsciiString value){
        if(value == null){
            return 2;
        }
        int strLen = value.length();
        if(strLen == 0){
            return 2;
        }else if(strLen>MAX_STRING_LENGTH){
            return 2;
        }else{
            //hashCode也序列化
            return 2 + 4 + strLen;
        }
    }


    public int calcShortString(String value){
        if(value == null){
            return 2;
        }
        int strLen = value.length();
        if(strLen == 0){
            return 2;
        }else if(strLen>MAX_STRING_LENGTH){
            return 2;
        }else{
            return 2 + StringUtil.utf8Length(value);
        }
    }

    public int calcByteArray(byte[] bytes){
        if(bytes == null){
            return 2;
        }
        if(bytes.length == 0){
            return 2;
        }else if(bytes.length>MAX_STRING_LENGTH){
            return 2;
        }else{
            return 2 + bytes.length;
        }
    }

    public void writeAsciiString(AsciiString str){
        if(str == null){
            writeShort((short) -1);
        }else{
            byte[] value = str.getBytes();
            if(value.length == 0){
                writeShort((short) 0);
                return;
            }else if(value.length > MAX_STRING_LENGTH){
                logger.fatal("string length too long: {}, should less than {}", value.length, MAX_STRING_LENGTH);
                writeShort((short) 0);
                return;
            }else {
                writeShort((short) value.length);
                writeInt(str.hashCode());
                System.arraycopy(value, 0, _buffer, _position, value.length);
                _position += value.length;
            }
        }
    }

    public void writeByteArray(byte[] bytes){
        if(bytes == null){
            writeShort((short) -1);
        }else{
            int length = bytes.length;
            if(length == 0){
                writeShort((short) 0);
            }else if(length > MAX_STRING_LENGTH){
                logger.fatal("string length too long: {}, should less than {}", length, MAX_STRING_LENGTH);
                writeShort((short) 0);
            }else {
                writeShort((short) length);
                System.arraycopy(bytes, 0,  _buffer, _position, length);
                _position += length;
            }
        }
    }

    public void writeShortString(String str){
        if(str == null){
            writeShort((short) -1);
        }else{
            int strLen = str.length();
            if(strLen == 0){
                writeShort((short) 0);
            }else if(strLen > MAX_STRING_LENGTH){
                logger.fatal("string length too long: {}, should less than {}", strLen, MAX_STRING_LENGTH);
                writeShort((short) 0);
            }else {
                int utf8Length = StringUtil.utf8Length(str);
                writeShort((short) utf8Length);
                StringUtil.getUtf8Bytes(str, _buffer, _position);
                _position += utf8Length;
            }
        }
    }

    private final static AsciiString EMPTY_ASCII_STRING = new AsciiString();
    public AsciiString readAsciiString(){
        if(this._end - this._position < 2){
            return null;
        }
        int len = readShort();
        if(len == 0){
            return EMPTY_ASCII_STRING;
        }else if(len < 0){
            return null;
        }else if(len> (MAX_STRING_LENGTH*4) ){
            //太大了, 可能是调用者搞错了接口
            this._position = this._end;
            //为了不让错误继续传递下去
            logger.error("read short string failed, no more data");
            return null;
        }else if(this._end - this._position < (len + 4)){
            this._position = this._end;
            return null;
        }
        int hash = readInt();
        //logger.info("ascii string hash code: {}", hash);
        AsciiString str = new AsciiString(this._buffer, this._position, len, hash);
        this._position += len;
        return str;
    }
    private final static byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public byte[] readByteArray(){
        if(this._end - this._position < 2){
            return null;
        }
        int len = readShort();
        if(len == 0){
            return EMPTY_BYTE_ARRAY;
        }else if(len<0){
            return null;
        }else if(len > MAX_STRING_LENGTH){
            this._position = this._end;
            return null;
        }else if(this._end - this._position < len){
            this._position = this._end;
            return null;
        }
        byte[] array = new byte[len];
        System.arraycopy(this._buffer, this._position, array, 0, len);
        this._position += len;
        return array;
    }
    
    public String readShortString(){
        if(this._end - this._position < 2){
            return null;
        }
        int len = readShort();
        if(len == 0){
            return "";
        }else if(len < 0){
            return null;
        }else if(this._end - this._position < len){
            this._position = this._end;
            return null;
        }

        String str = new String(this._buffer, this._position, len, StandardCharsets.UTF_8);
        this._position += len;
        return str;
    }

}



