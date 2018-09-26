/**
 * 二分法搜索的一些特殊实现.
 *
 */
public class BinarySearchUtil {

    /** 读取数据和比较的相关接口 */
    public static interface ReadAndCompare<T>{
        /**
         * 读取pos位置的值
         * @param pos
         * @return
         */
        T read(int pos);

        /**
         * 比较t1, t2的值
         * @param t1
         * @param t2
         * @return t1小于t2时返回负值, t1==t2返回0, t1大于t2时,返回正值
         */
        int compare(T t1, T t2);
    }

    /**
     * ReadAndCompare<T>的特例实现, 减少整数类型的装包操作
     */
    public static interface ReadAndCompareLong{
        long read(int pos);
        int compare(long v1, long v2);
    }

    /**
     * 保存搜索过程中, 遍历的次数, 消耗和时间等
     */
    public static class Explain{
        /** 遍历的次数 */
        public int scaned;

        /** 耗费的时间, 纳秒 , 1毫秒==1000微秒==1000*1000纳秒*/
        public long time;

        /** 开始搜索的时间点, 纳秒, 内部使用 */
        private long beginTime;
    }

    public static int searchFirstGET(final int start, final int end, final long value, final ReadAndCompareLong readAndCompare){
        return searchFirstGET(start, end, value, readAndCompare, null);
    }
    public static int searchLastLET(final int start, final int end, final long value, final ReadAndCompareLong readAndCompare){
        return searchLastLET(start, end, value, readAndCompare, null);
    }

    public static <T> int searchFirstGET(final int start, final int end, final T value, final ReadAndCompare<T> readAndCompare){
        return searchFirstGET(start, end, value, readAndCompare, null);
    }
    public static <T> int searchLastLET(final int start, final int end, final T value, final ReadAndCompare<T> readAndCompare){
        return searchLastLET(start, end, value, readAndCompare, null);
    }


    /**
     * 查找第一个>=value的第一个值. 数据源必须是逻辑递增的. 如果数据源物理上是递减的, 那么readAndCompare需要使用反义的操作.
     * @param start 搜索的起始位置, 包含
     * @param end 搜索的结束位置, 包含
     * @param readAndCompare 取值和比较操作, 必须有效
     * @return 如果找到符合条件的值, 则返回[start,end]之间的一个位置; 没有找到时返回-1
     */
    public static int searchFirstGET(final int start, final int end, final long value, final ReadAndCompareLong readAndCompare, Explain explain){

        if(explain != null){
            explain.beginTime = System.nanoTime();
        }

        if(start > end ){
            return -1;
        }

        //记录索引的次数
        int scanCount = 0;

        int low = start;
        int high = end;

        //中间位置
        int middle;
        //中间位置的值
        long midValue;
        int cmp;
        do{
            scanCount++;
            middle = (low + high) >>> 1;
            midValue = readAndCompare.read(middle);
            cmp = readAndCompare.compare(midValue, value);
            if(cmp < 0){
                low = middle + 1;
            }else if(cmp >0){
                high = middle - 1;
            }else{
                break;
            }
        }while(low <= high);

        /** 没有找到合适的条件, 继续往右查询 */
        if(cmp < 0) {
            do{
                if(middle >= end){
                    /**当前位置已经是最后一个, 没有更多数据了 */
                    middle = -1;
                    break;
                }
                middle++;
                scanCount++;
                midValue  = readAndCompare.read(middle);
                cmp = readAndCompare.compare(midValue, value);
                if(cmp>=0){
                    //符合条件, 搜索完成
                    break;
                }
            }while(true);
        }else{
            //已经符合条件, 但是, 可能左边的值也符合条件
            int tempPos;
            do{
                if(middle <= start){
                    //没有更多数据, 当前位置就是最终结果
                    break;
                }
                tempPos = middle - 1;
                midValue = readAndCompare.read(tempPos);
                cmp = readAndCompare.compare(midValue, value);

                if(cmp < 0){
                    //左边的值不符合条件, 没有必须再遍历
                    break;
                }else{
                    //左边的值符合条件, 继续往左搜索
                    middle = tempPos;
                }
            }while(true);
        }

        if(explain != null){
            explain.scaned = scanCount;
            explain.time = System.nanoTime() - explain.beginTime;
        }
        return middle;
    }


    /**
     * 查找小于等于value的最后一个值. 数据源必须是逻辑递增的. 如果数据源物理上是递减的, 那么readAndCompare需要使用反义的操作.
     * @param start 搜索的起始位置, 包含
     * @param end 搜索的结束位置, 包含
     * @param readAndCompare 取值和比较操作, 必须有效
     * @return 如果找到符合条件的值, 则返回[start,end]之间的一个位置; 没有找到时返回-1
     */
    public static int searchLastLET(final int start, final int end, final long value, final ReadAndCompareLong readAndCompare, Explain explain){
        if(explain != null){
            explain.beginTime = System.nanoTime();
        }

        if(start > end ){
            return -1;
        }

        //记录索引的次数
        int scanCount = 0;

        int low = start;
        int high = end;

        //中间位置
        int middle;
        //中间位置的值
        long midValue;
        int cmp;
        do{
            scanCount++;
            middle = (low + high) >>> 1;
            midValue = readAndCompare.read(middle);
            cmp = readAndCompare.compare(midValue, value);
            if(cmp < 0){
                low = middle + 1;
            }else if(cmp >0){
                high = middle - 1;
            }else{
                break;
            }
        }while(low <= high);

        /** 符合条件, 继续往右搜索 */
        if(cmp <= 0) {
            int tempPos;
            do{
                if(middle >= end){
                    /** 当前位置已经是最后一个值, 搜索结束 */
                    break;
                }
                tempPos = middle + 1;
                scanCount++;
                midValue  = readAndCompare.read(tempPos);
                cmp = readAndCompare.compare(midValue, value);
                if(cmp > 0){
                    //不符合条件
                    break;
                }else{
                    //符合条件, 继续往右搜索
                    middle = tempPos;
                }
            }while(true);
        }else{
            /** 不符合条件, 继续往左搜索 */
            do{
                if(middle <= start){
                    middle = -1;
                    break;
                }
                scanCount++;
                middle--;
                midValue = readAndCompare.read(middle);
                cmp = readAndCompare.compare(midValue, value);
                if(cmp <= 0){
                    //符合条件.
                    break;
                }
            }while(true);
        }

        if(explain != null){
            explain.scaned = scanCount;
            explain.time = System.nanoTime() - explain.beginTime;
        }
        return middle;
    }


    /**
     * 查找第一个>=value的第一个值. 数据源必须是逻辑递增的. 如果数据源物理上是递减的, 那么readAndCompare需要使用反义的操作.
     * @param start 搜索的起始位置, 包含
     * @param end 搜索的结束位置, 包含
     * @param readAndCompare 取值和比较操作, 必须有效
     * @return 如果找到符合条件的值, 则返回[start,end]之间的一个位置; 没有找到时返回-1
     */
    public static <T> int searchFirstGET(final int start, final int end, final T value, final ReadAndCompare<T> readAndCompare, Explain explain){

        if(explain != null){
            explain.beginTime = System.nanoTime();
        }

        if(start > end ){
            return -1;
        }

        //记录索引的次数
        int scanCount = 0;

        int low = start;
        int high = end;

        //中间位置
        int middle;
        //中间位置的值
        T midValue;
        int cmp;
        do{
            scanCount++;
            middle = (low + high) >>> 1;
            midValue = readAndCompare.read(middle);
            cmp = readAndCompare.compare(midValue, value);
            if(cmp < 0){
                low = middle + 1;
            }else if(cmp >0){
                high = middle - 1;
            }else{
                break;
            }
        }while(low <= high);

        /** 没有找到合适的条件, 继续往右查询 */
        if(cmp < 0) {
            do{
                if(middle >= end){
                    /**当前位置已经是最后一个, 没有更多数据了 */
                    middle = -1;
                    break;
                }
                middle++;
                scanCount++;
                midValue  = readAndCompare.read(middle);
                cmp = readAndCompare.compare(midValue, value);
                if(cmp>=0){
                    //符合条件, 搜索完成
                    break;
                }
            }while(true);
        }else{
            //已经符合条件, 但是, 可能左边的值也符合条件
            int tempPos;
            do{
                if(middle <= start){
                    //没有更多数据, 当前位置就是最终结果
                    break;
                }
                tempPos = middle - 1;
                midValue = readAndCompare.read(tempPos);
                cmp = readAndCompare.compare(midValue, value);

                if(cmp < 0){
                    //左边的值不符合条件, 没有必须再遍历
                    break;
                }else{
                    //左边的值符合条件, 继续往左搜索
                    middle = tempPos;
                }
            }while(true);
        }

        if(explain != null){
            explain.scaned = scanCount;
            explain.time = System.nanoTime() - explain.beginTime;
        }
        return middle;
    }


    /**
     * 查找小于等于value的最后一个值. 数据源必须是逻辑递增的. 如果数据源物理上是递减的, 那么readAndCompare需要使用反义的操作.
     * @param start 搜索的起始位置, 包含
     * @param end 搜索的结束位置, 包含
     * @param readAndCompare 取值和比较操作, 必须有效
     * @return 如果找到符合条件的值, 则返回[start,end]之间的一个位置; 没有找到时返回-1
     */
    public static <T> int searchLastLET(final int start, final int end, final T value, final ReadAndCompare<T> readAndCompare, Explain explain){
        if(explain != null){
            explain.beginTime = System.nanoTime();
        }

        if(start > end ){
            return -1;
        }

        //记录索引的次数
        int scanCount = 0;

        int low = start;
        int high = end;

        //中间位置
        int middle;
        //中间位置的值
        T midValue;
        int cmp;
        do{
            scanCount++;
            middle = (low + high) >>> 1;
            midValue = readAndCompare.read(middle);
            cmp = readAndCompare.compare(midValue, value);
            if(cmp < 0){
                low = middle + 1;
            }else if(cmp >0){
                high = middle - 1;
            }else{
                break;
            }
        }while(low <= high);

        /** 符合条件, 继续往右搜索 */
        if(cmp <= 0) {
            int tempPos;
            do{
                if(middle >= end){
                    /** 当前位置已经是最后一个值, 搜索结束 */
                    break;
                }
                tempPos = middle + 1;
                scanCount++;
                midValue  = readAndCompare.read(tempPos);
                cmp = readAndCompare.compare(midValue, value);
                if(cmp > 0){
                    //不符合条件
                    break;
                }else{
                    //符合条件, 继续往右搜索
                    middle = tempPos;
                }
            }while(true);
        }else{
            /** 不符合条件, 继续往左搜索 */
            do{
                if(middle <= start){
                    middle = -1;
                    break;
                }
                scanCount++;
                middle--;
                midValue = readAndCompare.read(middle);
                cmp = readAndCompare.compare(midValue, value);
                if(cmp <= 0){
                    //符合条件.
                    break;
                }
            }while(true);
        }

        if(explain != null){
            explain.scaned = scanCount;
            explain.time = System.nanoTime() - explain.beginTime;
        }
        return middle;
    }

}
