package io.github.bootystar.helper.databind;


import lombok.Data;

/**
 * 通用数据返回类型
 * @author bootystar
 */
@Data
public class R<T> {
    public static final Integer UNAUTHORIZED = -1;
    public static final Integer SUCCESS = 1;
    public static final Integer FAILURE = 2;
    public static final Integer ERROR = 3;

    private Integer code;
    private String msg;
    private T data;

    public static <T> R<T> success(String msg, T data) {
        R<T> result = new R<>();
        result.setCode(SUCCESS);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> R<T> success(T data) {
        R<T> result = new R<>();
        result.setCode(SUCCESS);
        result.setData(data);
        return result;
    }

    public static <T> R<T> success() {
        R<T> result = new R<>();
        result.setCode(SUCCESS);
        return result;
    }

    public static <T> R<T> failure(String msg) {
        R<T> result = new R<>();
        result.setCode(FAILURE);
        result.setMsg(msg);
        return result;
    }

    public static <T> R<T> failure(String msg, T data) {
        R<T> result = new R<>();
        result.setCode(FAILURE);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> R<T> error(String msg) {
        R<T> result = new R<>();
        result.setCode(ERROR);
        result.setMsg(msg);
        return result;
    }

    public static <T> R<T> error(String msg, T data) {
        R<T> result = new R<>();
        result.setCode(ERROR);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> R<T> unauthorized(String msg, T data) {
        R<T> result = new R<>();
        result.setCode(UNAUTHORIZED);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> R<T> unauthorized() {
        R<T> result = new R<>();
        result.setCode(UNAUTHORIZED);
        result.setMsg("权限不足");
        return result;
    }

}
