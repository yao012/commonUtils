package Excel;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.read.context.AnalysisContext;
import com.alibaba.excel.read.event.AnalysisEventListener;
import com.alibaba.excel.support.ExcelTypeEnum;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * yaozhenguo
 * 2018/09/29
 * 使用easyExcel 进行读写excel ,比较简单的
 *
 */
public class EasyExcelDemo {

    public static void testRead() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File("F:\\testEasyExcel.xlsx"));

            ExcelReader excelReader = new ExcelReader(fileInputStream, ExcelTypeEnum.XLSX, null,
                    new AnalysisEventListener<List<String>>() {

                        // 这个监听每次使用都需要new一个新的,不可以单例,至于为什么还没有研究,easyExcel的维护者注明的
                        @Override
                        public void invoke(List<String> object, AnalysisContext context) {
                            // 每读取一行触发一次
                            System.out.println("当前sheet名称" + context.getCurrentSheet());
                            System.out.println("当前行数" + context.getCurrentRowNum());
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            // 所有行被解析完成后执行?
                            System.out.println("当前sheet = " + context.getCurrentSheet() + " 行数 " + context.getCurrentRowNum());
                        }
                    });

            excelReader.read();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void testWrite() {
        FileOutputStream fileOutputStream = null;
        ExcelWriter excelWriter = null;
        try {
            fileOutputStream = new FileOutputStream(new File("F:\\testWrite.xlsx"));
            excelWriter = new ExcelWriter(fileOutputStream,ExcelTypeEnum.XLSX);
            Sheet testSheet = new Sheet(1,0);
            testSheet.setSheetName("测试sheet");
            excelWriter.write0(getList(),testSheet);
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            excelWriter.finish();
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<List<String>> getList(){
        List<List<String>> finalList = new ArrayList<>();

        for(int i=1;i<30;i++){
            List<String> list = new ArrayList<>();
            for(int j =1;j<20;j++){
                list.add("第"+i+"行,第"+j+"列");
            }
            finalList.add(list);
        }
        return finalList;
    }



    public static void main(String[] args) {
//        testRead();
        testWrite();
    }

}
