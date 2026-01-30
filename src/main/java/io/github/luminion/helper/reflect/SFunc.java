package io.github.luminion.helper.reflect;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author luminion
 * @since 1.0.0
 */
@FunctionalInterface
public interface SFunc<T, R> extends Function<T, R>, Serializable {

}