package io.github.bootystar.helper.base.reflect;

import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * @author bootystar
 */
@Data
public class EnumHelper {
    private Integer key;
    private String value;

    @SneakyThrows
    public static <E extends Enum<E>> String getValueByKey(Class<E> clazz, Integer key) {
        if (key == null) {
            return null;
        }
        Field kf = key(clazz);
        for (Enum<E> e : EnumSet.allOf(clazz)) {
            Object o = kf.get(e);
            if (Objects.equals(key, o)) {
                return String.valueOf(value(clazz).get(e));
            }
        }
        return null;
    }

    @SneakyThrows
    public static <E extends Enum<E>> List<EnumHelper> list(Class<E> clazz) {
        Field kf = key(clazz);
        Field vf = value(clazz);
        ArrayList<EnumHelper> enumHelpers = new ArrayList<EnumHelper>();
        for (Enum<E> e : EnumSet.allOf(clazz)) {
            EnumHelper enumHelper = new EnumHelper();
            enumHelper.key = (Integer) kf.get(e);
            enumHelper.value = (String) vf.get(e);
/*            if (enumHelper.key > 0) {
                enumHelpers.add(enumHelper);
            }*/
            enumHelpers.add(enumHelper);
        }
        return enumHelpers;
    }

    public static <E extends Enum<E>> Field key(Class<E> clazz) {
        return field(clazz, "key" );
    }


    public static <E extends Enum<E>> Field value(Class<E> clazz) {
        return field(clazz, "value" );
    }


    @SneakyThrows
    public static <E extends Enum<E>> Field field(Class<E> clazz, String filed) {
        Field field = clazz.getDeclaredField(filed);
        field.setAccessible(true);
        return field;
    }


}
