package common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 用于计算各种hash值
 */
public class HashCodeUtil {
    private final static Logger logger = LogManager.getLogger(HashCodeUtil.class);
    /**
     * 计算一段字节数组的hash值.
     * @param hash
     * @param val
     * @param offset
     * @param length
     * @return
     */
    public static int hashCode(int hash, byte[] val, int offset, int length) {
        if (length > 0) {
            for (int i = offset; i < offset + length; i++) {
                hash = 31 * hash + val[i];
            }
        }
        return hash;
    }



    private final static int FILE_SAMPLE_STEP = 1024;
    private final static int FILE_SAMPLE_EXPONENT = 10; //1024是2的10次方

    /**
     * 一个很简陋的计算文件的hash值的方法.
     * 每FILE_SAMPLE_STEP字节中, 抽取最开始的4个字节(如果不足4个字节, 则抛弃), 然后计算这些字节的hash值.
     * 如果文件大部分二进制数据相同, 则这个方法没有任何价值.
     * 但是对于视频或图片文件来说, 即使图形内容类似,二进制数据的差异性还是很大.
     * @param filePath
     * @return
     */
    public static int fileSampleHashCode1(String filePath){
        RandomAccessFile file = null;
        int hash = 0;
        byte[] bytes = new byte[4];
        try {
            file = new RandomAccessFile(filePath, "r");
            long length = file.length();
            for(long skip=0; skip<=(length-4); skip +=FILE_SAMPLE_STEP ) {
                file.seek(skip);
                file.readFully(bytes);
                //logger.info("{}", common.HexUtils.hexString(bytes));
                hash = hashCode(hash, bytes, 0, bytes.length);
            }
            if(hash == 0){
                hash = 1;
            }
            return hash;
        }catch(Exception ex){
            logger.error("calc file hash failed, {}",filePath, ex);
            return -1;
        }finally {
            try {
                if(file != null) {
                    file.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static int fileSampleHashCode1(InputStream in, long length){
        int hash = 0;
        byte[] bytes = new byte[4];
        try {
            for(long skip=0; skip<=(length-4); skip +=FILE_SAMPLE_STEP ) {
                in.skip(4);
                in.read(bytes);
                //logger.info("{}", common.HexUtils.hexString(bytes));
                hash = hashCode(hash, bytes, 0, bytes.length);
            }
            if(hash == 0){
                hash = 1;
            }
            return hash;
        }catch(Exception ex){
            logger.error("calc file hash failed, {}", ex);
            return -1;
        }finally {
            try {
                if(in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }

    }

    /**
     * 用于帮助计算文件流的hash值. {@code fileSampleHashCode1}
     * 输入的数据必须是连续的.
     */
    public static class FileHashGenerator{
        private int hash;

        /**
         * 已处理的字节数
         */
        private int readed=0;

        private byte[] temp = new byte[4];
        private int remained = 0;

        /**
         * 每FILE_SAMPLE_STEP个字节中, 取出开始位置的4个字节(如果不足4个字节, 则抛弃), 来计算hash值.
         * 假设一个完整的文件大小为Length, 对于文件位置position
         * 当position%FILE_SAMPLE_STEP == 0, 1, 2, 3时, 需要将这4个字节取出来.
         * 转换为数学问题, 如下:
         * 对于处于[x1, x2]之间的值x, 如果x%1024等于0,1,2,3, 则x匹配条件.
         * 解答方法,
         * y1 = x1/1024;
         * y2 = x2/1024;
         * x的值为 [y1, y2]*1024 + [0, 3], 然后与[x1, x2]取交集.
         *
         */
        public FileHashGenerator(){

        }

        /**
         * 结束计算过程, 返回hash值.
         * @return
         */
        public int getHash(){
            return hash==0?1:hash;
        }

        public void calc(byte[] val, int offset, int length) {
            if (length <= 0) {
                return;
            }
            //将范围映射到完整的数据轴上.
            int x1 = readed;
            int x2 = readed + length -1;
            int y1 = (x1>>FILE_SAMPLE_EXPONENT);
            int y2 = (x2>>FILE_SAMPLE_EXPONENT);

            for(int y=y1; y<=y2; y++){
                int x = (y<<FILE_SAMPLE_EXPONENT);
                for(int i=0; i<4; i++,x++){
                    if(x>=x1 && x<=x2){
                        temp[remained++] = val[ x-x1+offset];
                        //满4个字节才处理.
                        if(remained==4){
                            for(byte v : temp){
                                hash = 31 * hash + v;
                            }
                            remained = 0;
                        }

                    }
                }
            }
            readed += length;
        }
    }

}
