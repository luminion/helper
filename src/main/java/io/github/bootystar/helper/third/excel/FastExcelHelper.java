package io.github.bootystar.helper.third.excel;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 用于支持EasyExcel和FastExcel的转化器帮助类
 *
 * @author bootystar
 */
@Slf4j
public abstract class FastExcelHelper {
    
    public abstract static class ExcelConverterInvocation<T> implements InvocationHandler {
        protected final String packageName;
        protected final Class<T> supportType;

        public ExcelConverterInvocation(String packageName, Class<T> supportJavaType) {
            this.packageName = packageName;
            this.supportType = supportJavaType;
        }

        public Class<T> supportJavaTypeKey() {
            return supportType;
        }

        @SneakyThrows
        @SuppressWarnings("unchecked")
        public Object supportExcelTypeKey() {
            // todo 
//            CellDataTypeEnum string = CellDataTypeEnum.STRING;
            String clazzStr = packageName + "." + "CellDataTypeEnum";
            Class<?> clazz = Class.forName(clazzStr);
            return Enum.valueOf((Class<? extends Enum>) clazz, "STRING");
        }

        public abstract T convertToJavaData(String cellData);

        public abstract String convertToExcelData(T value);

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "supportJavaTypeKey":
                    return supportJavaTypeKey();
                case "supportExcelTypeKey":
                    return supportExcelTypeKey();
                case "convertToJavaData":
                    String string = args[0].getClass().getMethod("getStringValue").invoke(args[0]).toString();
                    return convertToJavaData(string);
                case "convertToExcelData":
                    return convertToExcelData((T) args[0]);
            }
            return method.invoke(proxy, args);
        }
    }
}
