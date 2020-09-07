package cn.tera.test.protobuf;

import cn.tera.protobuf.model.*;
import cn.tera.protobuf.utility.Utility;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Test;

public class ProtobufTest {
    /**
     * protobuf的基础使用
     */
    @Test
    void basicUse() {
        //创建一个Person对象
        BasicUsage.Person person = BasicUsage.Person.newBuilder()
                .setId(5)
                .setName("tera")
                .setEmail("tera@google.com")
                .build();
        System.out.println("Person's name is " + person.getName());

        //编码
        //此时我们就可以通过我们想要的方式传递该byte数组了
        byte[] bytes = person.toByteArray();

        //将编码重新转换回Person对象
        BasicUsage.Person clone = null;
        try {
            //解码
            clone = BasicUsage.Person.parseFrom(bytes);
            System.out.println("The clone's name is " + clone.getName());
        } catch (InvalidProtocolBufferException e) {
        }


        //引用是不同的
        System.out.println("==:" + (person == clone));
        //equals方法经过了重写，所以equals是相同的
        System.out.println("equals:" + person.equals(clone));

        //修改clone中的值
        clone = clone.toBuilder().setName("clone").build();
        System.out.println("The clone's new name is " + clone.getName());
    }


    /**
     * 序号的重要性测试
     *
     * @throws Exception
     */
    @Test
    public void tagImportanceTest() throws Exception {
        TagImportance.Model1 model1 = TagImportance.Model1.newBuilder()
                .setEmail("model1@google.com")
                .setId(1)
                .setName("model1")
                .build();
        TagImportance.Model2 model2 = TagImportance.Model2.parseFrom(model1.toByteArray());
        System.out.println("model2 email:" + model2.getEmail());
        System.out.println("model2 id:" + model2.getId());
        System.out.println("model2 name:" + model2.getName());
        System.out.println("-------model2 数据---------");
        System.out.println(model2);
    }

    /**
     * 序号对编码大小的影响
     *
     * @throws Exception
     */
    @Test
    public void tagSizeInfluenceTest() throws Exception {
        TagImportance.Model1 model1 = TagImportance.Model1.newBuilder()
                .setEmail("model1@google.com")
                .setId(1)
                .setName("model1")
                .build();
        System.out.println("model1 编码大小：" + model1.toByteArray().length);

        TagImportance.Model3 model3 = TagImportance.Model3.newBuilder()
                .setEmail("model1@google.com")
                .setId(1)
                .setName("model1")
                .build();
        System.out.println("model3 编码大小：" + model3.toByteArray().length);
    }


    /**
     * 测试不同模型间的转换
     *
     * @throws Exception
     */
    @Test
    public void parseDifferentModelsTest() throws Exception {
        //创建一个Person对象
        DifferentModels.Person person = DifferentModels.Person.newBuilder()
                .setName("person name")
                .setId(1)
                .setEmail("tera@google.com")
                .build();
        //对person编码
        byte[] personBytes = person.toByteArray();
        //将编码后的数据直接merge成Article对象
        DifferentModels.Article article = DifferentModels.Article.parseFrom(personBytes);
        System.out.println("article's title:" + article.getTitle());
        System.out.println("article's wordsCount:" + article.getWordsCount());
        System.out.println("article's author:" + article.getAuthor());
    }

    /**
     * 模型字段不同类型的兼容性
     *
     * @throws Exception
     */
    @Test
    public void typeCompatibleTest() throws Exception {
        ModelTypeCompatible.NewPerson newPerson = ModelTypeCompatible.NewPerson.newBuilder()
                .setName(ModelTypeCompatible.Name.newBuilder()
                        .setFirst("tera")
                        .setLast("cn")
                        .setUsedYears(10)
                ).setId(5)
                .setEmail("tera@google.com")
                .build();
        ModelTypeCompatible.OldPerson oldPerson = ModelTypeCompatible.OldPerson.parseFrom(newPerson.toByteArray());
        System.out.println(oldPerson.getName());
    }

    /**
     * json和protobuf的互相转换
     */
    @Test
    void jsonToProtobuf() throws Exception {
        //构造简单的模型
        PersonJson model = new PersonJson();
        model.email = "personJson@google.com";
        model.id = 1;
        model.name = "personJson";
        String json = JSON.toJSONString(model);
        System.out.println("原始json");
        System.out.println("------------------------");
        System.out.println(json);
        System.out.println();

        //parser
        JsonFormat.Parser parser = JsonFormat.parser();
        //需要build才能转换
        BasicUsage.Person.Builder personBuilder = BasicUsage.Person.newBuilder();
        //将json字符串转换成protobuf模型，并打印
        parser.merge(json, personBuilder);
        BasicUsage.Person person = personBuilder.build();
        //需要注意的是，protobuf的toString方法并不会自动转换成json，而是以更简单的方式呈现，所以一般没法直接用
        System.out.println("protobuf内容");
        System.out.println("------------------------");
        System.out.println(person.toString());

        //修改protobuf模型中的字段，并再转换会json字符串
        person = person.toBuilder().setName("protobuf").setId(2).build();
        String buftoJson = JsonFormat.printer().print(person);
        System.out.println("protobuf修改过数据后的json");
        System.out.println("------------------------");
        System.out.println(buftoJson);
    }

    /**
     * json和protobuf的编码数据大小
     */
    @Test
    void codeSizeJsonVsProtobuf() throws Exception {
        //构造简单的模型
        PersonJson model = new PersonJson();
        model.email = "personJson@google.com";
        model.id = 1;
        model.name = "personJson";
        String json = JSON.toJSONString(model);
        System.out.println("原始json");
        System.out.println("------------------------");
        System.out.println(json);
        System.out.println("json编码后的字节数：" + json.getBytes("utf-8").length + "\n");

        //parser
        JsonFormat.Parser parser = JsonFormat.parser();
        //需要build才能转换
        BasicUsage.Person.Builder personBuilder = BasicUsage.Person.newBuilder();
        //将json字符串转换成protobuf模型，并打印
        parser.merge(json, personBuilder);
        BasicUsage.Person person = personBuilder.build();
        //需要注意的是，protobuf的toString方法并不会自动转换成json，而是以更简单的方式呈现，所以一般没法直接用
        System.out.println("protobuf内容");
        System.out.println("------------------------");
        System.out.println(person.toString());
        System.out.println("protobuf编码后的字节数：" + person.toByteArray().length);
    }


    /**
     * varint数字编码
     */
    @Test
    void varintTest() {
        BasicUsage.Person person = BasicUsage.Person.newBuilder()
                .setId(91809)
                .build();
        Utility.printByte(person.toByteArray());
    }

    /**
     * protobuf基础编码，varint类型
     */
    @Test
    void protobufBaseEncodeTestVarint() {
        ProtobufStudent.Student student = ProtobufStudent.Student.newBuilder()
                .setAge(15)
                .setHairCount(239281373231123L)
                .setIsMale(true)
                .setHairColor(ProtobufStudent.Color.RED)
                .build();
        Utility.printByte(student.toByteArray());
    }

    /**
     * protobuf基础编码，double和float类型
     */
    @Test
    void protobufBaseEncodeTestDoubleAndFloat() {
        ProtobufStudent.Student student = ProtobufStudent.Student.newBuilder()
                .setHeight(99.6)
                .setWeight(99.6F)
                .build();
        Utility.printByte(student.toByteArray());
    }

    /**
     * protobuf基础编码，lengthDelimited类型
     */
    @Test
    void protobufBaseEncodeTestLengthDelimited() {
        ProtobufStudent.Student student = ProtobufStudent.Student.newBuilder()
//            .setName("tera")
//            .setScores(ByteString.copyFrom(new byte[200]))
//            .addFriends("a")
//            .addFriends("b")
//            .setFather(ProtobufStudent.Parent.newBuilder()
//                    .setName("MrTera"))
                .addHobbies(ProtobufStudent.Hobby.newBuilder().setName("a"))
                .addHobbies(ProtobufStudent.Hobby.newBuilder().setName("b"))
                .build();
        Utility.printByte(student.toByteArray());
    }


    /**
     * protobuf基础编码，有符号的整数
     */
    @Test
    void negativeIntTest() {
        ProtobufStudent.Student student = ProtobufStudent.Student.newBuilder()
                .setAge(-7)
//            .setUage(-7)
//                .setSage(-7)
                .build();
        Utility.printByte(student.toByteArray());
    }

    /**
     * 一个相对完整的模型
     */
    @Test
    void entireModelTest() {
        ProtobufStudent.Student student = ProtobufStudent.Student.newBuilder()
                .setAge(12)
                .setName("tera")
                .setIsMale(true)
                .setFather(ProtobufStudent.Parent.newBuilder()
                        .setName("MrTera"))
                .addFriends("peter")
                .build();
        Utility.printByte(student.toByteArray());
    }

    /**
     * 数据类型的分辨
     */
    @Test
    void differDatatype() {
        ProtobufStudent.Student student = ProtobufStudent.Student.newBuilder()
                .setName("aaa")
                .setScores(ByteString.copyFrom(new byte[]{97, 97, 97}))
                .build();
        Utility.printByte(student.toByteArray());
    }
//
//    /**
//     * sint类型
//     */
//    @Test
//    void negativeSIntTest() {
//        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .setSintf(-1)
//                .build();
//        Utility.printByte(peter.toByteArray());
//    }
//
//    /**
//     * double和float
//     */
//    @Test
//    void doubleAndFloatTest() {
//        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .setF2(8.25F)
////                .setD(1)
//                .build();
//        Utility.printByte(peter.toByteArray());
//    }
//
//    /**
//     * 子对象
//     */
//    @Test
//    void embeddedTest() {
//        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .setEmbedded(
//                        AddressBookProtos.Embedded.newBuilder()
//                                .setE1(1))
//                .build();
//        Utility.printByte(peter.toByteArray());
//    }
//
//    /**
//     * repeated string
//     */
//    @Test
//    void repeatedStringTest() {
//        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .addContacts("1")
//                .addContacts("2")
//                .addContacts("3")
//                .build();
//        Utility.printByte(peter.toByteArray());
//    }
//
//    /**
//     * repeated int
//     */
//    @Test
//    void repeatedIntTest() {
//        AddressBookProtos.Person peter = AddressBookProtos.Person.newBuilder()
//                .addNumList(1)
//                .addNumList(2)
//                .addNumList(3)
//                .build();
//        Utility.printByte(peter.toByteArray());
//    }


    /**
     * 比较较多数量的字段分别采用平铺和子对象方式的编码大小
     */
    @Test
    void compareNestedAndNotNestedModel() {
        //采用子对象的方式
        NestedTestModel.Nested nested = NestedTestModel.Nested.newBuilder()
                .setNested1(
                        NestedTestModel.Nested1.newBuilder()
                                .setField1("1").setField2("2").setField3("3").setField4("4").setField5("5").setField6("6").setField7("7").setField8("8")
                                .setField9("9").setField10("10").setField11("11").setField12("12").setField13("13").setField14("14").setField15("15")
                                .build()
                )
                .setNested2(
                        NestedTestModel.Nested2.newBuilder()
                                .setField16("16").setField17("17").setField18("18").setField19("19").setField20("20").setField21("21").setField22("22").setField23("23")
                                .setField24("24").setField25("25").setField26("26").setField27("27").setField28("28").setField29("29").setField30("30")
                                .build()
                ).build();
        System.out.println(String.format("nested size:%s", nested.toByteArray().length));


        //平铺的方式
        NestedTestModel.NotNested notNested = NestedTestModel.NotNested.newBuilder()
                .setField1("1").setField2("2").setField3("3").setField4("4").setField5("5").setField6("6").setField7("7").setField8("8")
                .setField9("9").setField10("10").setField11("11").setField12("12").setField13("13").setField14("14").setField15("15")
                .setField16("16").setField17("17").setField18("18").setField19("19").setField20("20").setField21("21").setField22("22").setField23("23")
                .setField24("24").setField25("25").setField26("26").setField27("27").setField28("28").setField29("29").setField30("30")
                .build();
        System.out.println(String.format("not nested size:%s", notNested.toByteArray().length));
    }

    /**
     * 比较json编码与protobuf编码同样信息量下的编码大小，其中还包括了采用子对象和平铺编码方式的比较
     * 为了使得比较相对更趋近于真实情况，因此对于每个字段的key采用6个字符，value采用8个字符
     */
    @Test
    void compareFieldNest() {
        String source0 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\"}";
        Utility.PrintCompareResult("七个字段", source0, com.tera.huazhu.protobufmodel.Nested.Seven1.newBuilder());

        String source1 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"}";
        Utility.PrintCompareResult("十四个字段", source1, com.tera.huazhu.protobufmodel.Nested.Fourteen.newBuilder());

        String source2 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"part\":{\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"}}";
        Utility.PrintCompareResult("十四个字段，包含一个七字段", source2, com.tera.huazhu.protobufmodel.Nested.OneNestedFourteen.newBuilder());

        String source3 = "{\"part1\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\"},\"part2\":{\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"}}";
        Utility.PrintCompareResult("十四个字段，包含两个七字段", source3, com.tera.huazhu.protobufmodel.Nested.TwoNestedFourteen.newBuilder());

        String source4 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\",\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\"}";
        Utility.PrintCompareResult("二十八个字段", source4, com.tera.huazhu.protobufmodel.Nested.TwentyEight.newBuilder());


        String source6 = "{\"field15\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"},\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\"}";
        Utility.PrintCompareResult("二十八个字段，包含一个十四字段", source6, com.tera.huazhu.protobufmodel.Nested.OneNestedTwentyEight.newBuilder());

        String source5 = "{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\",\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\",\"xsxscd\":\"xkclvodo\",\"nvnnnv\":\":L()#_)(\",\"IUJNUJ\":\"893iU&*(\",\"LKOKIK\":\"LLL!!)(*\",\"AAAAAl\":\"~)!_+_)(\",\"kciII8\":\"zkcl,.(*\",\"LMK909\":\"L:>;/.,;\",\"QWE89f\":\"LKOkid90\",\"LKX983\":\"O()(OIKD\",\"LOPO90\":\"!@#*(kjC\",\"lbkvof\":\"LOPA(Q)O\",\"POlop0\":\"LKCI893i\",\"QWEOPI\":\"l;lv0()(\"}";
        Utility.PrintCompareResult("四十一个字段", source5, com.tera.huazhu.protobufmodel.Nested.FortyOne.newBuilder());

        String source7 = "{\"part\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"},\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\",\"xsxscd\":\"xkclvodo\",\"nvnnnv\":\":L()#_)(\",\"IUJNUJ\":\"893iU&*(\",\"LKOKIK\":\"LLL!!)(*\",\"AAAAAl\":\"~)!_+_)(\",\"kciII8\":\"zkcl,.(*\",\"LMK909\":\"L:>;/.,;\",\"QWE89f\":\"LKOkid90\",\"LKX983\":\"O()(OIKD\",\"LOPO90\":\"!@#*(kjC\",\"lbkvof\":\"LOPA(Q)O\",\"POlop0\":\"LKCI893i\",\"QWEOPI\":\"l;lv0()(\"}";
        Utility.PrintCompareResult("四十一个字段，包含一个十四字段", source7, com.tera.huazhu.protobufmodel.Nested.OneNestedFortyOne.newBuilder());

        String source8 = "{\"part1\":{\"asdcvc\":\"9cidk3og\",\"zlzlzo\":\"zkzia81i\",\"qieuIU\":\"4c5d8rov\",\"KLCKDI\":\"29384959\",\"zlapap\":\"sfdsewsd\",\"IOIOud\":\"kcicicic\",\"cmvjkf\":\"kiaiskwi\",\"kdidke\":\"xcvfdgd2\",\"xixoxi\":\"gdgdgdgd\",\"LKidkc\":\"lckvobpn\",\"POIoif\":\"q4q4q4q4\",\"LKIoif\":\"v9v8vlv;\",\"ababab\":\"s7x4c1d4\",\"dedfed\":\"9q8q9q8q\"},\"part2\":{\"CHDjdj\":\"c3v2b0(*\",\"jchdue\":\"!@#$%$#@\",\"opo909\":\"asdfgfds\",\"QWUikj\":\"vkcid9s0\",\"ZASkjd\":\"qwertrew\",\"zlpao9\":\"xcvsdfwe\",\"dloci9\":\"[][][]|]\",\"eodlc9\":\"120.2365\",\"pqpOL9\":\"*/98-+87\",\"LKi981\":\"sd4fd5s4\",\"skdlco\":\"POSIKI()\",\"qpqppl\":\"(*D&DIKL\",\"azasqa\":\"12315fdd\",\"clckdi\":\"ddddfgdd\"},\"xsxscd\":\"xkclvodo\",\"nvnnnv\":\":L()#_)(\",\"IUJNUJ\":\"893iU&*(\",\"LKOKIK\":\"LLL!!)(*\",\"AAAAAl\":\"~)!_+_)(\",\"kciII8\":\"zkcl,.(*\",\"LMK909\":\"L:>;/.,;\",\"QWE89f\":\"LKOkid90\",\"LKX983\":\"O()(OIKD\",\"LOPO90\":\"!@#*(kjC\",\"lbkvof\":\"LOPA(Q)O\",\"POlop0\":\"LKCI893i\",\"QWEOPI\":\"l;lv0()(\"}";
        Utility.PrintCompareResult("四十一个字段，包含两个十四字段", source8, com.tera.huazhu.protobufmodel.Nested.TwoNestedFortyOne.newBuilder());
    }
}
