package io.github.bootystar.helper.third.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import io.github.bootystar.helper.base.reflect.ReflectHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 用于支持EasyExcel和FastExcel的转化器帮助类
 *
 * @author bootystar
 */
@Slf4j
public abstract class FastExcelHelper {
    
    public abstract static class ExcelConverterInvocation<T> implements InvocationHandler {
        protected final String packageName;
        protected final Class<T> clazz;

        public ExcelConverterInvocation(String packageName, Class<T> supportJavaType) {
            this.packageName = packageName;
            this.clazz = supportJavaType;
        }

        public Class<T> supportJavaTypeKey() {
            return clazz;
        }

        @SneakyThrows
        public Object supportExcelTypeKey() {
            // todo 
            CellDataTypeEnum string = CellDataTypeEnum.STRING;
            return null;
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
                    String string = ReflectHelper.invokePublicMethod(args[0], "getStringValue").toString();
                    return convertToJavaData(string);
                case "convertToExcelData":
                    return convertToExcelData((T) args[0]);
            }
            return method.invoke(proxy, args);
        }
    }
}
