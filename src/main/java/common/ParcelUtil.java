package common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;

/**
 * 用于自动生成ParcelV2的序列化代码.
 * 文件底部有一个例子. 在intellij IDE中, 点击右键, 选择run common.ParcelUtil.main
 *
 */
public class ParcelUtil {

    public static void autocode(Class cls){
        autocode(cls, "", "");
    }

    /**
     *
     * @param cls 需要生成代码的类名
     * @param fieldPrefix 变量名称前缀  比如this.
     * @param methodPrefix 方法名的前缀, 比如this.
     */
    public static void autocode(Class cls, String fieldPrefix, String methodPrefix){
        StringBuilder ser = new StringBuilder();
        StringBuilder der = new StringBuilder();
        StringBuilder calc = new StringBuilder();
        StringBuilder merge = new StringBuilder();

        calc.append("\t@Override\r\n");
        calc.append("\tpublic int calculateSize(){\r\n");
        calc.append("\t\tint calcLength = 0;\r\n");

        ser.append("\t@Override\r\n");
        ser.append("\tpublic byte[] ser(){\r\n");
        ser.append("\t\tint calcLength = calculateSize();\r\n");
        ser.append("\t\tsetBuffer(calcLength);\r\n");

        der.append("\t@Override\r\n" +
                "\tpublic void deser(byte[] b, int offset, int len) {\r\n" +
                "\t\tthis.setBuffer(b, offset, len);\r\n");

        merge.append("\t@Override\r\n");
        merge.append("\tpublic void merge(Object o){\r\n");
        merge.append("\t\tif (!(o instanceof "+ cls.getSimpleName() + ")) {\r\n" +
                            "\t\t\treturn;\r\n" +
                        "\t\t}\r\n" +
         "\t\t" + cls.getSimpleName() + " v = (" + cls.getSimpleName() + ") o;\r\n");

        Field[] fields = cls.getDeclaredFields();
        for(Field field : fields){
            if(Modifier.isStatic(field.getModifiers())){
                continue;
            }
            String fieldName = fieldPrefix + field.getName();
            Class<?> fieldCls = field.getType();

            merge.append(String.format("\t\t%s = v.%s;\r\n", fieldName, fieldName));

            //枚举类型, 假设其实现了
            String type = field.getType().getSimpleName();;
            if(fieldCls.isEnum()){
                //枚举值不允许为null
                String clacMehtod = String.format("\t\tcalcLength += %scalcInt(%s==null?0:%s.code);\r\n", methodPrefix, fieldName, fieldName);
                String writeMethod = String.format("\t\t%swriteInt(%s==null?0:%s.code);\r\n", methodPrefix, fieldName, fieldName);
                String readMethod = String.format("\t\t%s = %s.fromCode(%sreadInt());\r\n", fieldName, type, methodPrefix);

                calc.append(clacMehtod);
                ser.append(writeMethod);
                der.append(readMethod);
                continue;
            }


            String upper = Character.toUpperCase(type.charAt(0)) + type.substring(1);
            String lower = type.toLowerCase();
            String defaultValue = "0";

            switch(type){
                case "boolean":
                case "byte":
                case "short":
                case "int":
                case "long":
                case "Date":{
                    String clacMehtod = String.format("\t\tcalcLength += %scalc%s(%s);\r\n", methodPrefix, upper, fieldName);
                    String writeMethod = String.format("\t\t%swrite%s(%s);\r\n", methodPrefix, upper, fieldName);
                    String readMethod = String.format("\t\t%s = %sread%s();\r\n", fieldName, methodPrefix, upper);

                    calc.append(clacMehtod);
                    ser.append(writeMethod);
                    der.append(readMethod);
                    break;
                }
                case "Integer":
                    upper = "Int";
                    lower = "int";
                case "Boolean":
                case "Byte":
                case "Short":
                case "Long":{
                    if(type.equals("Boolean")){
                        defaultValue = "false";
                    }
                    String clacMehtod = String.format("\t\tcalcLength += %scalc%s(%s == null ? %s : %s.%sValue());"
                            + "\r\n", methodPrefix, upper, fieldName, defaultValue, fieldName, lower);
                    String writeMethod = String.format("\t\t%swrite%s(%s == null ? %s : %s.%sValue());"
                            + "\r\n", methodPrefix, upper, fieldName, defaultValue, fieldName, lower);
                    String readMethod = String.format("\t\t%s = %sread%s();\r\n", fieldName, methodPrefix, upper);

                    calc.append(clacMehtod);
                    ser.append(writeMethod);
                    der.append(readMethod);
                    break;
                }
                case "common.AsciiString":
                case "String":
                case "byte[]":{
                    if(type.equals("common.AsciiString")){
                        upper = type;
                    }else if(type.equals("String")){
                        upper = "ShortString";
                    }else{
                        upper = "ByteArray";
                    }
                    String clacMehtod = String.format("\t\tcalcLength += %scalc%s(%s);\r\n", methodPrefix, upper, fieldName);
                    String writeMethod = String.format("\t\t%swrite%s(%s);\r\n", methodPrefix, upper, fieldName);
                    String readMethod = String.format("\t\t%s = %sread%s();\r\n", fieldName, methodPrefix, upper);

                    calc.append(clacMehtod);
                    ser.append(writeMethod);
                    der.append(readMethod);
                    break;
                }
                default:{
                    System.out.println("unkown class: " + field.getType());
                    break;
                }


            }

        }

        ser.append("\t\tif(calcLength != " + methodPrefix + "getWritedSize()){\r\n" +
        "\t\t\tSystem.err.println(\"common.ParcelV2 fata error, writed size not equal calc size\"); \r\n" +
        "\t\t}\r\n");

        ser.append("\t\treturn " + methodPrefix +"getBuffer();\r\n\t}");


        calc.append("\t\treturn calcLength;\r\n\t}");
        der.append("\t}");
        merge.append("\t}");

        System.out.println("\r\n");
        System.out.println(ser.toString());
        System.out.println("\r\n");
        System.out.println(calc.toString());

        System.out.println("\r\n");
        System.out.println(der.toString());
        System.out.println("\r\n\r\n\r\n\r\n");

        System.out.println("\r\n");
        System.out.println(merge.toString());
        System.out.println("\r\n\r\n\r\n\r\n");

    }

    /**
     * ========================================================================
     * 例子
     * ========================================================================
      */
    private static enum MyEnum{
        A(-1),
        B(0),
        C(1);
        public final int code;
        MyEnum(int code){
            this.code = code;
        }

        public static MyEnum fromCode(int code) {
            switch (code) {
                case 1:
                    return C;
                case 0:
                    return B;
                default:
                    return A;
            }
        }
    }


    private static class TestClass extends ParcelV2{

        boolean bool1;
        Boolean bool2;
        Boolean bool3;

        byte byte1;
        Byte byte2;
        Byte byte3;

        short short1;
        Short short2;
        Short short3;

        int int1;
        Integer int2;
        Integer int3;

        long long1;
        Long long2;
        Long long3;

        String str1;
        String str2;
        String str3;

        Date date1;
        Date date2;

        MyEnum enum1;


        @Override
        public byte[] ser(){
            int calcLength = calculateSize();
            setBuffer(calcLength);
            this.writeBoolean(this.bool1);
            this.writeBoolean(this.bool2 == null ? false : this.bool2.booleanValue());
            this.writeBoolean(this.bool3 == null ? false : this.bool3.booleanValue());
            this.writeByte(this.byte1);
            this.writeByte(this.byte2 == null ? 0 : this.byte2.byteValue());
            this.writeByte(this.byte3 == null ? 0 : this.byte3.byteValue());
            this.writeShort(this.short1);
            this.writeShort(this.short2 == null ? 0 : this.short2.shortValue());
            this.writeShort(this.short3 == null ? 0 : this.short3.shortValue());
            this.writeInt(this.int1);
            this.writeInt(this.int2 == null ? 0 : this.int2.intValue());
            this.writeInt(this.int3 == null ? 0 : this.int3.intValue());
            this.writeLong(this.long1);
            this.writeLong(this.long2 == null ? 0 : this.long2.longValue());
            this.writeLong(this.long3 == null ? 0 : this.long3.longValue());
            this.writeShortString(this.str1);
            this.writeShortString(this.str2);
            this.writeShortString(this.str3);
            this.writeDate(this.date1);
            this.writeDate(this.date2);
            this.writeInt(this.enum1==null?0:this.enum1.code);
            if(calcLength != this.getWritedSize()){
                System.err.println("common.ParcelV2 fata error, writed size not equal calc size");
            }
            return this.getBuffer();
        }


        @Override
        public int calculateSize(){
            int calcLength = 0;
            calcLength += this.calcBoolean(this.bool1);
            calcLength += this.calcBoolean(this.bool2 == null ? false : this.bool2.booleanValue());
            calcLength += this.calcBoolean(this.bool3 == null ? false : this.bool3.booleanValue());
            calcLength += this.calcByte(this.byte1);
            calcLength += this.calcByte(this.byte2 == null ? 0 : this.byte2.byteValue());
            calcLength += this.calcByte(this.byte3 == null ? 0 : this.byte3.byteValue());
            calcLength += this.calcShort(this.short1);
            calcLength += this.calcShort(this.short2 == null ? 0 : this.short2.shortValue());
            calcLength += this.calcShort(this.short3 == null ? 0 : this.short3.shortValue());
            calcLength += this.calcInt(this.int1);
            calcLength += this.calcInt(this.int2 == null ? 0 : this.int2.intValue());
            calcLength += this.calcInt(this.int3 == null ? 0 : this.int3.intValue());
            calcLength += this.calcLong(this.long1);
            calcLength += this.calcLong(this.long2 == null ? 0 : this.long2.longValue());
            calcLength += this.calcLong(this.long3 == null ? 0 : this.long3.longValue());
            calcLength += this.calcShortString(this.str1);
            calcLength += this.calcShortString(this.str2);
            calcLength += this.calcShortString(this.str3);
            calcLength += this.calcDate(this.date1);
            calcLength += this.calcDate(this.date2);
            calcLength += this.calcInt(this.enum1==null?0:this.enum1.code);
            return calcLength;
        }


        @Override
        public void deser(byte[] b, int offset, int len) {
            this.setBuffer(b, offset, len);
            this.bool1 = this.readBoolean();
            this.bool2 = this.readBoolean();
            this.bool3 = this.readBoolean();
            this.byte1 = this.readByte();
            this.byte2 = this.readByte();
            this.byte3 = this.readByte();
            this.short1 = this.readShort();
            this.short2 = this.readShort();
            this.short3 = this.readShort();
            this.int1 = this.readInt();
            this.int2 = this.readInt();
            this.int3 = this.readInt();
            this.long1 = this.readLong();
            this.long2 = this.readLong();
            this.long3 = this.readLong();
            this.str1 = this.readShortString();
            this.str2 = this.readShortString();
            this.str3 = this.readShortString();
            this.date1 = this.readDate();
            this.date2 = this.readDate();
            this.enum1 = MyEnum.fromCode(this.readInt());
        }

    }

    public static void main(String[] args){
        autocode(TestClass.class, "this.", "this.");
    }

}



