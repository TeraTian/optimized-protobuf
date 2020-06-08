package cn.tera.protobuf.coder.encoder;

import cn.tera.protobuf.coder.Helper;
import cn.tera.protobuf.coder.annotations.AppVersion;
import cn.tera.protobuf.coder.annotations.EncodeDefault;
import cn.tera.protobuf.coder.annotations.EncodeDefaults;
import cn.tera.protobuf.coder.annotations.FindIndexResult;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomProtobufEncoder {
    private String appVersion;
    private Comparator<String> comparator;
    private String split = "%s";

    public CustomProtobufEncoder(String appVersion, Comparator<String> comparator) {
        this.appVersion = appVersion;
        this.comparator = comparator;
    }

    public <T> byte[] serialize(T obj, Class<T> clazz) {
        List<Byte> bytes = writeObject(0, obj, clazz);
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    private <T> List<Byte> writeObject(int o, T obj, Class<T> clazz) {
        List<Byte> bytes = new ArrayList<>();
        try {
            List<Byte> headBytes = new ArrayList<>();
            List<Field> fields = getAllFields(clazz, appVersion);
            Map<Integer, Field> fieldMao = Helper.sortFields(fields);
            List<Integer> fieldNums = fieldMao.keySet().stream().collect(Collectors.toList());
            fieldNums.sort(Comparator.comparing(f -> f));
            int fieldNum = 1;
            for (int num : fieldNums) {
                Field f = fieldMao.get(num);
                f.setAccessible(true);
                Object value = f.get(obj);
                if (value != null) {
                    label1:
                    {
                        if (value instanceof String) {
                            String str = (String) value;
                            EncodeDefaults multiple = f.getAnnotation(EncodeDefaults.class);
                            if (multiple != null) {
                                EncodeDefault[] singles = multiple.value();
                                if (singles.length > 0) {
                                    int startIndex = 0;
                                    for (int i = 0; i < singles.length; i++) {
                                        EncodeDefault single = singles[i];
                                        if (single.version().isEmpty() || comparator.compare(appVersion, single.version()) >= 0) {
                                            FindIndexResult indexResult = findIndex(startIndex, single.value(), str, single.replace());
                                            int index = indexResult.index;
                                            if (index >= 0) {
                                                bytes.addAll(writeDefaultString(fieldNum, index, indexResult.params));
                                                break label1;
                                            }
                                            startIndex += single.value().length;
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                EncodeDefault single = f.getAnnotation(EncodeDefault.class);
                                if (single != null && (single.version().isEmpty() || comparator.compare(appVersion, single.version()) >= 0)) {
                                    FindIndexResult indexResult = findIndex(0, single.value(), str, single.replace());
                                    int index = indexResult.index;
                                    if (index >= 0) {
                                        bytes.addAll(writeDefaultString(fieldNum, index, indexResult.params));
                                        break label1;
                                    }
                                }
                            }
                            bytes.addAll(writeString(fieldNum, (String) value));
                        } else if (value instanceof Boolean) {
                            bytes.addAll(writeBoolean(fieldNum, (Boolean) value));
                        } else if (value instanceof Integer) {
                            bytes.addAll(writeInt32(fieldNum, (Integer) value));
                        } else if (value instanceof Double) {
                            bytes.addAll(writeFixed64(fieldNum, (Double) value));
                        } else if (value instanceof Float) {
                            bytes.addAll(writeFixed32(fieldNum, (Float) value));
                        } else if (value instanceof Long) {
                            bytes.addAll(writeInt64(fieldNum, (Long) value));
                        } else if (value instanceof List) {
                            bytes.addAll(writeList(fieldNum, (List) value));
                        } else {
                            Class c = f.getType();
                            bytes.addAll(writeObject(fieldNum, f.get(obj), c));
                        }
                    }
                }
                fieldNum++;
            }
            if (o != 0) {
                headBytes.addAll(writeTag(o));
            }
            if (headBytes.size() > 0) {
                headBytes.addAll(writeUInt32NoTag(bytes.size()));
                bytes.addAll(0, headBytes);
            }
        } catch (
                Exception e) {
            System.out.println(e);
        }
        return bytes;
    }

    private List<Field> getAllFields(Class clazz, String ver) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && !clazz.getName().toLowerCase().equals("java.lang.object")) {
            Field[] fieldTemp = clazz.getDeclaredFields();
            for (Field f : fieldTemp) {
                AppVersion version = f.getAnnotation(AppVersion.class);
                if (version == null || comparator.compare(ver, version.value()) >= 0) {
                    fields.add(f);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private List<Byte> writeDefaultString(int fieldNum, int index, List<String> params) {
        List<Byte> bytes = new ArrayList<>();
        bytes.addAll(writeTag(fieldNum, 1));
        if (params == null || params.size() == 0) {
            bytes.addAll(writeInt32NoTag(index << 1 | 0));
        } else {
            bytes.addAll(writeInt32NoTag(index << 1 | 1));
            bytes.addAll(writeStringNoTag(String.join("^", params)));
        }
        return bytes;
    }


    private List<Byte> writeTag(int fieldNum, int var) {
        int value = fieldNum << 1 | var;
        return writeUInt32NoTag(value);
    }

    private List<Byte> writeTag(int fieldNum) {
        return writeTag(fieldNum, 0);
    }

    private List<Byte> writeList(int fieldNumber, List value) {
        List<Byte> bytes = new ArrayList<>();
        if (value != null && value.size() > 0) {
            Object v = value.get(0);
            if (v instanceof String) {
                bytes.addAll(writeStringList(fieldNumber, value));
            } else if (v instanceof Boolean) {
                bytes.addAll(writeBooleanList(fieldNumber, value));
            } else if (v instanceof Integer) {
                bytes.addAll(writeIntegerList(fieldNumber, value));
            } else if (v instanceof Double) {
                bytes.addAll(writeDoubleList(fieldNumber, value));
            } else if (v instanceof Float) {
                bytes.addAll(writeFloatList(fieldNumber, value));
            } else if (v instanceof Long) {
                bytes.addAll(writeLongList(fieldNumber, value));
            } else if (v instanceof List) {
                bytes.addAll(writeList(fieldNumber, (List) v));
            } else {
                bytes.addAll(writeObjectList(fieldNumber, value));
            }
        }
        return bytes;
    }

    private List<Byte> writeObjectList(int fieldNumber, List list) {
        List<Byte> bytes = new ArrayList<>();
        for (Object o : list) {
            Class c = o.getClass();
            bytes.addAll(writeObject(fieldNumber, o, c));
        }
        return bytes;
    }

    private List<Byte> writeLongList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Long.class);
    }

    private List<Byte> writeBooleanList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Boolean.class);
    }

    private List<Byte> writeIntegerList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Integer.class);
    }

    private List<Byte> writeDoubleList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Double.class);
    }

    private List<Byte> writeFloatList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Float.class);
    }

    private <T> List<Byte> writeNoStringList(int fieldNumber, List list, Class<T> clazz) {
        List<Byte> bytes = new ArrayList<>();
        bytes.addAll(writeTag(fieldNumber));
        List<Byte> contentBytes = new ArrayList<>();
        for (Object d : list) {
            if (clazz.equals(Double.class)) {
                contentBytes.addAll(writeFixed64NoTag(Double.doubleToRawLongBits((Double) d)));
            } else if (clazz.equals(Float.class)) {
                contentBytes.addAll(writeFixed32NoTag(Float.floatToRawIntBits((Float) d)));
            } else if (clazz.equals(Integer.class)) {
                contentBytes.addAll(writeInt32NoTag((Integer) d));
            } else if (clazz.equals(Long.class)) {
                contentBytes.addAll(writeUInt64NoTag((Long) d));
            } else if (clazz.equals(Boolean.class)) {
                contentBytes.add((byte) (((Boolean) d) ? 1 : 0));
            }
        }
        bytes.addAll(writeUInt32NoTag(contentBytes.size()));
        bytes.addAll(contentBytes);
        return bytes;
    }

    private List<Byte> writeStringList(int fieldNumber, List list) {
        List<Byte> bytes = new ArrayList<>();
        for (Object s : list) {
            bytes.addAll(writeString(fieldNumber, (String) s));
        }
        return bytes;
    }

    private List<Byte> writeString(int fieldNum, String value) {
        List<Byte> bytes = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return bytes;
        }
        bytes.addAll(writeTag(fieldNum));
        bytes.addAll(writeStringNoTag(value));
        return bytes;

    }

    private List<Byte> writeInt32(int fieldNumber, int value) {
        List<Byte> result = new ArrayList<>();
        if (value == 0) {
            return result;
        }
        result.addAll(writeTag(fieldNumber));
        result.addAll(writeInt32NoTag(value));
        return result;
    }

    private List<Byte> writeInt64(int fieldNumber, long value) {
        List<Byte> result = new ArrayList<>();
        if (value == 0L) {
            return result;
        }
        result.addAll(writeTag(fieldNumber));
        result.addAll(writeUInt64NoTag((value)));
        return result;
    }

    private List<Byte> writeBoolean(int fieldNumber, Boolean value) {
        List<Byte> bytes = new ArrayList<Byte>();
        if (value == null || !value) {
            return bytes;
        }
        bytes.addAll(writeTag(fieldNumber));
        bytes.add((byte) 1);
        return bytes;
    }

    private List<Byte> writeInt32NoTag(int value) {
        if (value >= 0) {
            return writeUInt32NoTag(value);
        } else {
            return writeUInt64NoTag((long) value);
        }
    }

    private List<Byte> writeUInt32NoTag(int value) {
        List<Byte> result = new ArrayList<>();
        try {
            while ((value & -128) != 0) {
                result.add((byte) (value & 127 | 128));
                value >>>= 7;
            }
            result.add((byte) value);
        } catch (IndexOutOfBoundsException var3) {
            System.out.println(var3.getMessage());
        }
        return result;
    }

    private List<Byte> writeUInt64NoTag(long value) {
        List<Byte> bytes = new ArrayList<>();
        while ((value & -128L) != 0L) {
            bytes.add((byte) ((int) value & 127 | 128));
            value >>>= 7;
        }
        bytes.add((byte) ((int) value));
        return bytes;
    }


    private List<Byte> writeStringNoTag(String value) {
        List<Byte> bytes = new ArrayList<>();
        bytes.addAll(writeInt32NoTag(value.getBytes(Charset.forName("UTF8")).length));
        for (Byte b : value.getBytes(Charset.forName("UTF8"))) {
            bytes.add(b);
        }
        return bytes;
    }

    private List<Byte> writeFixed64(int fieldNumber, Double value) {
        List<Byte> bytes = new ArrayList<Byte>();
        if (value == null || value == 0) {
            return bytes;
        }
        bytes.addAll(writeTag(fieldNumber));
        bytes.addAll(writeFixed64NoTag(Double.doubleToRawLongBits(value)));
        return bytes;
    }

    public List<Byte> writeFixed32(int fieldNumber, Float value) {
        List<Byte> bytes = new ArrayList<Byte>();
        if (value == null || value == 0) {
            return bytes;
        }
        bytes.addAll(writeTag(fieldNumber));
        bytes.addAll(writeFixed32NoTag(Float.floatToRawIntBits(value)));
        return bytes;
    }

    public static List<Byte> writeFixed32NoTag(int value) {
        List<Byte> result = new ArrayList<>();
        try {
            result.add((byte) (value & 255));
            result.add((byte) (value >> 8 & 255));
            result.add((byte) (value >> 16 & 255));
            result.add((byte) (value >> 24 & 255));
        } catch (IndexOutOfBoundsException var3) {
            return new ArrayList<>();
        }
        return result;
    }

    private List<Byte> writeFixed64NoTag(long value) {
        List<Byte> result = new ArrayList<>();
        try {
            result.add((byte) ((int) value & 255));
            result.add((byte) ((int) (value >> 8) & 255));
            result.add((byte) ((int) (value >> 16) & 255));
            result.add((byte) ((int) (value >> 24) & 255));
            result.add((byte) ((int) (value >> 32) & 255));
            result.add((byte) ((int) (value >> 40) & 255));
            result.add((byte) ((int) (value >> 48) & 255));
            result.add((byte) ((int) (value >> 56) & 255));
        } catch (IndexOutOfBoundsException var4) {
            return new ArrayList<>();
        }
        return result;
    }


    private List<Byte> writeInt32Total(int fieldNumber, Integer value) {
        List<Byte> bytes = new ArrayList<>();
        if (value == null || value == 0) {
            return bytes;
        }
        if (value > 0) {
            bytes.addAll(writeInt32(fieldNumber, value));
        } else {
            bytes.addAll(writeString(fieldNumber, value.toString()));
        }
        return bytes;
    }

    private FindIndexResult findIndex(int startIndex, String[] arr, String target, boolean replace) {
        FindIndexResult result = new FindIndexResult();
        int i = startIndex;
        for (String str : arr) {
            if (replace && str.contains(split)) {
                List<String> params = getTemplateParameters(str, target);
                if (params.size() > 0) {
                    result.index = i;
                    result.params = params;
                    return result;
                }
            } else if (str != null && str.equals(target)) {
                result.index = i;
                return result;
            }
            i++;
        }
        result.index = -1;
        return result;
    }

    private List<String> getTemplateParameters(String template, String target) {
        List<String> result = new ArrayList<>();
        try {
            template = template.replace(split, "(.*)");
            Pattern r = Pattern.compile(template);
            Matcher m = r.matcher(target);
            if (m.find()) {
                for (int i = 1; i <= m.groupCount(); i++) {
                    result.add(m.group(i));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
//        String[] templates = template.split(split);
//        int findPos = 0;
//        for (String temp : templates) {
//            if (temp.equals("")) {
//                continue;
//            }
//            System.out.println(temp);
//            int index = target.indexOf(temp);
//            System.out.println(index);
//            if (index < findPos || findPos == 0 && index != 0) {
//                result.clear();
//                return result;
//            }
//            if (findPos != 0 || index != 0 || template.startsWith(split)) {
//                result.add(target.substring(findPos, index));
//            }
//            findPos = index + temp.length();
//        }
//        System.out.println(target);
//        System.out.println("---------------");
//        System.out.println(Helper.format(template, result, split));
//        for (String sss : result) {
//            System.out.println(sss);
//        }
//        return result;
    }
}
