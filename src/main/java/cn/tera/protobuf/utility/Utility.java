package cn.tera.protobuf.utility;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

public class Utility {
    /**
     * 打印字节数组
     *
     * @param bytes
     */
    public static void printByte(byte[] bytes) {
        for (Byte b : bytes) {
            System.out.print(b + "\t");
        }
        System.out.println();
        for (Byte b : bytes) {
            System.out.print(to8BinaryString(b) + " ");
        }
    }

    /**
     * 将int转换成32位字符串
     *
     * @param n
     * @returnint
     */
    public static String to32BinaryString(int n) {
        return String.format("%32s", Integer.toBinaryString(n))
                .replaceAll(" ", "0");
    }

    /**
     * 将short转换成8位字符串
     *
     * @param n
     * @return
     */
    public static String to8BinaryString(short n) {
        String s = String.format("%8s", Integer.toBinaryString(n));
        s = s.substring(s.length() - 8)
                .replaceAll(" ", "0");
        return s;
    }

    /**
     * 保留小数
     *
     * @param target 目标数
     * @return
     */
    public static Double retainDecimal(double target) {
        return Double.parseDouble(String.format("%." + 2 + "f", target));
    }

    /**
     * 将unicode编码转换成utf8
     *
     * @param theString
     * @return
     */
    public static String unicodeToUtf8(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    public static void PrintCompareResult(String name, String source, com.google.protobuf.Message.Builder builder) {
        System.out.println("----------------------------" + name + "----------------------------------");
        double nocompress = -1, protobufOriginal = 0;

        //json格式的字节数
        nocompress = source.getBytes().length;


        //protobuf
        JsonFormat.Parser parser = JsonFormat.parser().ignoringUnknownFields();
        try {
            parser.merge(source, builder);
            Message message = builder.build();
            //protobuf字节数
            protobufOriginal = message.toByteArray().length;
            //打印protobuf的数据
            System.out.println(message.toString());
            JsonFormat.Printer printer = JsonFormat.printer();
            String jsonMessage = printer.print(message);
            System.out.println(jsonMessage);
            System.out.println("---------------------------------------");
        } catch (Exception e) {
        }
        System.out.println("json格式节数\t\tprotobuf字节数\t\t比例");
        System.out.println(String.format("%s\t\t\t\t%s\t\t\t\t%s", retainDecimal(nocompress), retainDecimal(protobufOriginal), retainDecimal(protobufOriginal / nocompress)));
        System.out.println();
    }


}
