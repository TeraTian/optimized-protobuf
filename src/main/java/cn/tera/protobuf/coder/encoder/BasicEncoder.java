package cn.tera.protobuf.coder.encoder;

import cn.tera.protobuf.coder.Helper;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicEncoder {
    public static <T> byte[] serialize(T obj, Class<T> clazz) {
        //主逻辑函数
        List<Byte> bytes = writeObject(0, obj, clazz);
        //将List转换成Array
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    /**
     * 主逻辑函数
     * @param o 序号，当第一次被调用时会传入0
     * @param obj 模型实例
     * @param clazz 模型类
     * @param <T> 泛型
     * @return
     */
    public static <T> List<Byte> writeObject(int o, T obj, Class<T> clazz) {
        //结果字节集，因为在编码结束前是不确定总大小的，因此用List来作为返回参数
        List<Byte> bytes = new ArrayList<>();
        try {
            List<Field> fields = Helper.getAllFields(clazz);
            Map<Integer, Field> fieldList = Helper.sortFields(fields);
            List<Integer> fieldNums = fieldList.keySet().stream().collect(Collectors.toList());
            fieldNums.sort(Comparator.comparing(f -> f));
            for (int order : fieldNums) {
                Field f = fieldList.get(order);
                f.setAccessible(true);
                Object value = f.get(obj);
                if (value != null) {
                    if (value instanceof String) {
                        bytes.addAll(writeString(order, (String) value));
                    } else if (value instanceof Boolean) {
                        bytes.addAll(writeBoolean(order, (Boolean) value));
                    } else if (value instanceof Integer) {
                        bytes.addAll(writeInt32(order, (Integer) value));
                    } else if (value instanceof Double) {
                        bytes.addAll(writeFixed64(order, (Double) value));
                    } else if (value instanceof Float) {
                        bytes.addAll(writeFixed32(order, (Float) value));
                    } else if (value instanceof Long) {
                        bytes.addAll(writeInt64(order, (Long) value));
                    } else if (value instanceof List) {
                        bytes.addAll(writeList(order, (List) value));
                    } else {
                        Class c = f.getType();
                        bytes.addAll(writeObject(order, f.get(obj), c));
                    }
                }
                order++;
            }
            //序号+类型字节
            List<Byte> headBytes = new ArrayList<>();
            if (o != 0) {
                headBytes.addAll(writeTag(o, 2));
            }
            if (headBytes.size() > 0) {
//                headBytes.add((byte) bytes.size());
                headBytes.addAll(writeUInt32NoTag(bytes.size()));
                bytes.addAll(0, headBytes);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return bytes;
    }

    public static List<Byte> writeTag(int order, int var) {
        int value = order << 3 | var;
        return writeUInt32NoTag(value);
    }

    public static List<Byte> writeList(int fieldNumber, List value) {
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

    public static List<Byte> writeObjectList(int fieldNumber, List list) {
        List<Byte> bytes = new ArrayList<>();
        for (Object o : list) {
            Class c = o.getClass();
            bytes.addAll(writeObject(fieldNumber, o, c));
        }
        return bytes;
    }

    public static List<Byte> writeLongList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Long.class);
    }

    public static List<Byte> writeBooleanList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Boolean.class);
    }

    public static List<Byte> writeIntegerList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Integer.class);
    }

    public static List<Byte> writeDoubleList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Double.class);
    }

    public static List<Byte> writeFloatList(int fieldNumber, List list) {
        return writeNoStringList(fieldNumber, list, Float.class);
    }

    public static <T> List<Byte> writeNoStringList(int fieldNumber, List list, Class<T> clazz) {
        List<Byte> bytes = new ArrayList<>();
        bytes.addAll(writeTag(fieldNumber, 2));
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

    public static List<Byte> writeStringList(int fieldNumber, List list) {
        List<Byte> bytes = new ArrayList<>();
        for (Object s : list) {
            bytes.addAll(writeString(fieldNumber, (String) s));
        }
        return bytes;
    }

    public static List<Byte> writeString(int order, String value) {
        List<Byte> bytes = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return bytes;
        }
        bytes.addAll(writeTag(order, 2));
        bytes.addAll(writeStringNoTag(value));
        return bytes;

    }

    public static List<Byte> writeInt32(int fieldNumber, int value) {
        List<Byte> result = new ArrayList<>();
        if (value == 0) {
            return result;
        }
        result.addAll(writeTag(fieldNumber, 0));
        result.addAll(writeInt32NoTag(value));
        return result;
    }

    public static List<Byte> writeInt64(int fieldNumber, long value) {
        List<Byte> result = new ArrayList<>();
        if (value == 0L) {
            return result;
        }
        result.addAll(writeTag(fieldNumber, 0));
        result.addAll(writeUInt64NoTag((value)));
        return result;
    }

    public static List<Byte> writeBoolean(int order, Boolean value) {
        List<Byte> bytes = new ArrayList<Byte>();
        if (value == null || !value) {
            return bytes;
        }
        bytes.addAll(writeTag(order, 0));
        bytes.add((byte) 1);
        return bytes;
    }

    public static List<Byte> writeInt32NoTag(int value) {
        if (value >= 0) {
            return writeUInt32NoTag(value);
        } else {
            return writeUInt64NoTag((long) value);
        }
    }

    public static List<Byte> writeUInt32NoTag(int value) {
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

    public static List<Byte> writeUInt64NoTag(long value) {
        List<Byte> bytes = new ArrayList<>();
        while ((value & -128L) != 0L) {
            bytes.add((byte) ((int) value & 127 | 128));
            value >>>= 7;
        }
        bytes.add((byte) ((int) value));
        return bytes;
    }


    public static List<Byte> writeStringNoTag(String value) {
        List<Byte> bytes = new ArrayList<>();
        bytes.addAll(writeInt32NoTag(value.getBytes(Charset.forName("UTF8")).length));
        for (Byte b : value.getBytes(Charset.forName("UTF8"))) {
            bytes.add(b);
        }
        return bytes;
    }

    public static List<Byte> writeFixed64(int fieldNumber, Double value) {
        List<Byte> bytes = new ArrayList<Byte>();
        if (value == null || value == 0) {
            return bytes;
        }
        bytes.addAll(writeTag(fieldNumber, 1));
        bytes.addAll(writeFixed64NoTag(Double.doubleToRawLongBits(value)));
        return bytes;
    }

    public static List<Byte> writeFixed32(int fieldNumber, Float value) {
        List<Byte> bytes = new ArrayList<Byte>();
        if (value == null || value == 0) {
            return bytes;
        }
        bytes.addAll(writeTag(fieldNumber, 5));
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

    public static List<Byte> writeFixed64NoTag(long value) {
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
}
