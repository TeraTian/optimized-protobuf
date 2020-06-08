package cn.tera.protobuf.coder;

import cn.tera.protobuf.coder.annotations.Ignore;
import cn.tera.protobuf.coder.annotations.Tag;
import cn.tera.protobuf.coder.annotations.Version;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Helper {
    public static boolean compareBytes(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            return false;
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static void printBytes(byte[] bytes) {
        for (byte b : bytes) {
            System.out.print(b + "\t");
        }
        System.out.println();
        System.out.println("count:" + bytes.length);
        System.out.println();
    }

    public static byte[] protobufSerialize(String source, Message.Builder builder) {
        JsonFormat.Parser parser = JsonFormat.parser().ignoringUnknownFields();
        try {
            parser.merge(source, builder);
            Message message = builder.build();
            return message.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static <K, V> Map<K, List<V>> groupBy(List<V> vList, Function<V, K> p) {
        Map<K, List<V>> hashMap = new HashMap<>();
        for (V l : vList) {
            K groupKey = p.apply(l);
            if (!hashMap.containsKey(groupKey)) {
                List<V> list = new ArrayList<V>();
                list.add(l);
                hashMap.put(groupKey, list);
            } else {
                hashMap.get(groupKey).add(l);
            }
        }
        return hashMap;
    }

    /**
     * 获取所有有效字段
     *
     * @param clazz
     * @return
     */
    public static List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        //需要循环查找父类的字段
        while (clazz != null && !clazz.equals(Object.class)) {
            //这里需要所有的字段，包括private的
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        //过滤ignore字段
        fields.removeIf(f -> {
            Ignore ignore = f.getAnnotation(Ignore.class);
            return ignore != null;
        });
        return fields;
    }

    public static Map<Integer, Field> sortFields(List<Field> fields) {
        Map<Integer, Field> result = new HashMap<>();
        Set<String> tagedNames = new HashSet<>();
        //寻找被指定Tag的字段
        for (Field f : fields) {
            Tag tag = f.getAnnotation(Tag.class);
            if (tag != null) {
                result.put(tag.value(), f);
                tagedNames.add(f.getName());
            }
        }
        List<Field> fieldList = fields;
        fieldList.removeIf(f -> tagedNames.contains(f.getName()));
        //
//        fields.removeIf(f -> tagedNames.contains(f.getName()));

        List<Field> sortedFields = new ArrayList<>();
        Map<Integer, List<Field>> groups = Helper.groupBy(fields, f -> {
            Version sort = f.getAnnotation(Version.class);
            if (sort == null) {
                return -1;
            } else {
                return sort.value();
            }
        });
        List<Integer> sorts = groups.keySet().stream().collect(Collectors.toList());
        sorts.sort(Comparator.comparing(f -> f));
        for (int s : sorts) {
            groups.get(s).sort(Comparator.comparing(f -> f.getName().toLowerCase()));
            sortedFields.addAll(groups.get(s));
        }

        int fieldNum = 1;
        for (Field field : sortedFields) {
            while (result.containsKey(fieldNum)) {
                fieldNum++;
            }
            result.put(fieldNum++, field);
        }
        return result;
    }

    public static String format(String template, List<String> params, String split) {
        StringBuilder sb = new StringBuilder(template);
        for (String p : params) {
            int index = sb.indexOf(split);
            if (index >= 0) {
                sb.replace(index, index + split.length(), "");
                sb.insert(index, p);
            }
        }
        return sb.toString();
    }
}
