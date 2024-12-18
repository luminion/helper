package io.github.bootystar.helper.databind;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author bootystar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class P<T> {
    /**
     * 当前页码
     */
    private Long current;
    /**
     * 每页大小
     */
    private Long size;
    /**
     * 总数
     */
    private Long total;
    /**
     * 数据
     */
    private List<T> records;


}
