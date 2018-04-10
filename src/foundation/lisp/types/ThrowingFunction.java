package foundation.lisp.types;

import foundation.lisp.exceptions.InvalidTermException;

@FunctionalInterface
interface ThrowingFunction<T, R>
{
    R apply(T t) throws InvalidTermException;
}
