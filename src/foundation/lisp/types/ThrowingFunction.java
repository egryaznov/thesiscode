package foundation.lisp.types;

import foundation.lisp.exceptions.InvalidTermException;

@FunctionalInterface
public interface ThrowingFunction<T, R>
{
    R apply(T t) throws InvalidTermException;
}
