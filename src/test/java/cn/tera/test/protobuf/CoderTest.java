package cn.tera.test.protobuf;

import cn.tera.protobuf.coder.Helper;
import cn.tera.protobuf.coder.decoder.BasicDecoder;
import cn.tera.protobuf.coder.encoder.BasicEncoder;
import cn.tera.protobuf.coder.models.java.Student;
import cn.tera.protobuf.coder.models.protobuf.ProtobufStudent;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.Message;
import org.junit.jupiter.api.Test;

public class CoderTest {
/**
 * 类库的基本使用方式
 */
@Test
void basicEncoderTest() {
    String source = "{\"score2\":13213.1231,\"age\":5,\"name\":\"Peter\",\"hairCount\":183728182371871131,\"isMale\":true,\"score\":13213.1231}";
    test(source, Student.class, ProtobufStudent.Student.class);
}

    /**
     * tag注解的使用
     */
    @Test
    void tagAnnotationTest() {
        String source = "{\"name\":\"Peter\",\"score\":13213.1231,\"tag\":\"tagtest\"}";
        test(source, Student.class, ProtobufStudent.Student.class);
    }

    /**
     * ignore注解的使用
     */
    @Test
    void ignoreAnnotationTest() {
        String source = "{\"name\":\"Peter\",\"score\":13213.1231,\"ignore\":\"ignoretest\"}";
        test(source, Student.class, ProtobufStudent.Student.class);
    }


    /**
     * test method
     *
     * @param source        model json
     * @param javaClass     java class
     * @param protobufClass protobuf class
     */
    static <T, P extends Message> void test(String source, Class<T> javaClass, Class<P> protobufClass) {
        try {
            //输出原始json
            System.out.println("-------------------     source json     --------------------");
            System.out.println(source);
            System.out.println("count:" + source.getBytes().length);
            System.out.println();

            //输出protobuf原生类库的编码结果
            System.out.println("-------------------protobuf encode result-------------------");
            Message.Builder builder = (Message.Builder) protobufClass.getMethod("newBuilder").invoke(null);
            byte[] protoBytes = Helper.protobufSerialize(source, builder);
            Helper.printBytes(protoBytes);

            //输出字节写的类库的编码结果
            System.out.println();
            System.out.println("-------------------  tera encode result  -------------------");
            T javaModel = JSON.parseObject(source, javaClass);
            byte[] teraBytes = BasicEncoder.serialize(javaModel, javaClass);
            Helper.printBytes(teraBytes);

            //比较两种编码结果
            System.out.println();
            System.out.println("------------------- bytes compare result -------------------");
            System.out.println(Helper.compareBytes(protoBytes, teraBytes));

            //打印解码结果
            System.out.println();
            System.out.println("-------------------  tera decode result  -------------------");
            T deserialJavaModel = new BasicDecoder().deserialize(teraBytes, javaClass);
            System.out.println(JSON.toJSON(deserialJavaModel));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
