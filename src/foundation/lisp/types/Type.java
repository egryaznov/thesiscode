package foundation.lisp.types;

import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Type
{
    // XXX: Может нам вообще не нужно это перечисление и его можно слить с классом TObject?
    // XXX: Я его создал только чтобы обойти проблему нелегальности абстрактных статических методов.
    // NOTE: Пока что полностью избавиться от этого класса не получается, слишком много рефакторинга.
    @NotNull BOOLEAN("boolean", TBoolean::isBoolean, TBoolean::parseBoolean, true),
    @NotNull NUMERAL("numeral", TNumeral::isNumeral, TNumeral::new, true),
    @NotNull STRING("string", TString::isString, prim -> new TString(prim, true), true),
    @NotNull VOID("void", TVoid::isVoid, (s -> TVoid.instance), true),
    @NotNull DATE("date", TDate::isDate, TDate::parseDate, true),
    @NotNull LIST("list", (term -> term.equals(TList.EMPTY_LIST_KEYWORD)), (t -> TList.EMPTY_LIST), true),
    @NotNull PERSON("person", (term -> term.equals(TPerson.PEOPLE_KEYWORD)), (s -> TPerson.people()), true),
    @NotNull FUNCTION("function", null, null, false);

    private final boolean isPrimitive;
    private final @NotNull String name;
    private final @Nullable ThrowingFunction<String, Boolean> canParse;
    private final @Nullable ThrowingFunction<String, TObject<?>> parse;

    Type(final @NotNull String name,
         final @Nullable ThrowingFunction<String, Boolean> canParse,
         final @Nullable ThrowingFunction<String, TObject<?>> parse,
         final boolean isPrimitive)
    {
        this.name = name;
        this.parse = parse;
        this.canParse = canParse;
        this.isPrimitive = isPrimitive;
    }



    public boolean isPrimitive()
    {
        return isPrimitive;
    }

    public @NotNull String getName()
    {
        return name;
    }

    public boolean canParse(final String literal) throws InvalidTermException
    {
        return ( this.canParse != null ) && canParse.apply(literal);
    }

    public TObject<?> parse(final String literal) throws InvalidTermException
    {
        if ( !canParse(literal) )
        {
            throw new InvalidTermException("Cannot parse an invalid literal: " + literal);
        }
        else
        {
            assert (this.parse != null) : "Assert: Variable 'parse' is NULL";
            return this.parse.apply(literal);
        }
    }

}