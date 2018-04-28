package foundation.lisp.types;

import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// NOTE: Лучше использовать ArrayList как реализацию интерфейса List
public class TList extends TObject<List<TObject<?>>>
{
    static final TList EMPTY_LIST = new TList( Collections.emptyList() );
    public static final String EMPTY_LIST_KEYWORD = "vacant";

    TList(final @NotNull List<TObject<?>> list)
    {
        super(Type.LIST, list);
    }



    /*
        Returns an object in the list at the specified index, or TVoid if there is no such object.
        First element has a zero (0) index.
     */
    @NotNull
    private TObject<?> at(final @NotNull TNumeral numeralIndex)
    {
        assert getValue() != null : "Assert: at, this.value is null";
        assert numeralIndex.getValue() != null: "Assert: at, index.value is null";
        final long longIndex = numeralIndex.getValue();
        if (longIndex > Integer.MAX_VALUE)
        {
            return TVoid.instance;
        }
        if (longIndex < Integer.MIN_VALUE)
        {
            return TVoid.instance;
        }
        // NOTE: Int.MIN_VALUE < index < Int.MAX_VALUE
        final int len = getValue().size();
        final int index = (int)( (longIndex < 0)? (len + longIndex) : longIndex );
        final @NotNull TObject<?> result = ( index < 0 || index >= len )? TVoid.instance : getValue().get( index );
        //
        return result;
    }

    /*
        Concatenates this list with the specified tail list.
     */
    @NotNull
    private TList join(final @NotNull TList tail)
    {
        assert getValue() != null : "Assert: TList.join, value is null!";
        assert tail.getValue() != null : "Assert: TList.join, tail.value is null!";
        final @NotNull List<TObject<?>> source = getValue();
        final @NotNull List<TObject<?>> resultList = new ArrayList<>(source);
        resultList.addAll(tail.getValue());
        return new TList( resultList );
    }

    private @NotNull TObject<?> head()
    {
        assert getValue() != null : "Assert TList.head, value is null!";
        final @NotNull List<TObject<?>> items = getValue();
        if ( items.isEmpty() )
        {
            return EMPTY_LIST;
        }
        //
        return at(new TNumeral(0));
    }

    private @NotNull TList tail()
    {
        assert getValue() != null : "Assert TList.tail, value is null!";
        final @NotNull List<TObject<?>> items = getValue();
        final int count = items.size();
        if ( count < 2 )
        {
            return EMPTY_LIST;
        }
        //
        return new TList(items.subList(1, count));
    }

    /*
        Appends a specified item to this list.
     */
    @NotNull
    private TList append(final @NotNull TObject<?> item)
    {
        assert getValue() != null : "Assert: TList.append, value is null!";
        final @NotNull List<TObject<?>> result = new ArrayList<>(getValue());
        result.add( item );
        return new TList( result );
    }

    private @NotNull TNumeral count()
    {
        assert getValue() != null : "Assert: TList.count, super.value is null";
        return new TNumeral( getValue().size() );
    }

    private @NotNull TList map(final @NotNull TFunction<TObject<?>, TObject<?>> binaryMapping,
                               final @NotNull TList secondArgToMapping) throws InterpreterException
    {
        assert this.getValue()               != null : "Assert: TList.map, this.value is null!";
        assert secondArgToMapping.getValue() != null : "Assert: TList.map, second.value is null!";
        final @NotNull var firstList  = getValue();
        final @NotNull var secondList = secondArgToMapping.getValue();
        final          int minLength  = ( firstList.size() <= secondList.size() )? firstList.size() : secondList.size();
        if ( minLength == 0 )
        {
            return EMPTY_LIST;
        }
        //
        final var mappedList = new LinkedList<TObject<?>>();
        for (int i = 0; i < minLength; i++)
        {
            final @NotNull var twoArgs = List.of( firstList.get(i), secondList.get(i) );
            mappedList.add( binaryMapping.apply(twoArgs) );
        }
        //
        return new TList( mappedList );
    }

    private @NotNull TList map(final @NotNull TFunction<TObject<?>, TObject<?>> func) throws InterpreterException
    {
        assert this.getValue() != null : "Assert: TList.map, this.value is null!";
        final @NotNull var items = getValue();
        if ( items.isEmpty() )
        {
            return EMPTY_LIST;
        }
        // items not null and not empty
        final @NotNull var mappedItems = new LinkedList<TObject<?>>();
        for (var item : items)
        {
            mappedItems.add( func.apply(Collections.singletonList(item)) );
        }
        return new TList(mappedItems);
    }

    private @NotNull TList filter(final @NotNull TFunction<TObject<?>, TObject<?>> predicate) throws InterpreterException
    {
        assert getValue() != null : "Assert: TList.filter, super.value is null";
        final @NotNull List<TObject<?>> filteredList = new ArrayList<>(); // ArrayList is better than LinkedList in our case
        for (final TObject<?> item : getValue())
        {
            // Check whether the return type of the predicate is really boolean
            final @NotNull TObject<?> predicateResult = predicate.apply( Collections.singletonList(item) );
            if ( !predicateResult.instanceOf(Type.BOOLEAN) )
            {
                final String msg = String.format(
                        "Type Mismatch: filter, predicate %s returned value of type %s, but boolean was expected",
                        predicate.getName(),
                        predicateResult.getType().getName()
                );
                throw new InvalidTermException(msg);
            }
            //
            final @NotNull TBoolean isItemPasses = (TBoolean)predicateResult;
            if ( isItemPasses.passes() )
            {
                filteredList.add(item);
            }
        }
        //
        return new TList(filteredList);
    }

    @Override
    public @NotNull String termToString()
    {
        assert getValue() != null : "Assert: TList.termToString, value is null!";
        final @NotNull List<TObject<?>> items = getValue();
        //
        final @NotNull String result;
        if ( items.isEmpty() )
        {
            result = EMPTY_LIST_KEYWORD;
        }
        else
        {
            final @NotNull String tokens = items.stream()
                    .map(TObject::termToString)
                    .collect(Collectors.joining(" "));
            result = String.format("(list %s)", tokens);
        }
        //
        return result;
    }

    public static void registerAtomicFunctions(final @NotNull Map<String, TFunction> dict)
    {
        final @NotNull ListFunction list = new ListFunction();
        dict.put(list.getName(), list);
        //
        final @NotNull CountFunction count = new CountFunction();
        dict.put(count.getName(), count);
        //
        final @NotNull FilterListFunction filter = new FilterListFunction();
        dict.put(filter.getName(), filter);
        //
        final @NotNull AtFunction at = new AtFunction();
        dict.put(at.getName(), at);
        //
        final @NotNull Join join = new Join();
        dict.put(join.getName(), join);
        //
        final @NotNull Append append = new Append();
        dict.put(append.getName(), append);
        //
        final @NotNull Head head = new Head();
        dict.put(head.getName(), head);
        //
        final @NotNull Tail tail = new Tail();
        dict.put(tail.getName(), tail);
        //
        final @NotNull var mapping = new Mapping();
        dict.put(mapping.getName(), mapping);
    }



    private static class ListFunction extends TFunction<TObject<?>, TList>
    {
        ListFunction()
        {
            super("list", "list");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount > 0;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args)
        {
            return new TList(args);
        }

        @Override
        String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: list, expected at least 1 argument, zero given";
        }
    }

    private static class CountFunction extends TFunction<TObject<?>, TNumeral>
    {
        CountFunction()
        {
            super("count", "count");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TObject<?>> args) throws InterpreterException
        {
            // argsCount == 1
            final @NotNull TObject<?> firstArg = args.get(0);
            if ( !firstArg.instanceOf(Type.LIST) )
            {
                throw new InvalidTermException("Type Mismatch: count, expected a list, got a " + firstArg.getType().getName());
            }
            final @NotNull TList record = (TList)firstArg;
            return record.count();
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: count, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class FilterListFunction extends TFunction<TObject<?>, TList>
    {
        FilterListFunction()
        {
            super( "filter", "filter" );
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args) throws InterpreterException
        {
            // args.size() == 2
            final @NotNull TObject<?> firstArg = args.get(0);
            final @NotNull TObject<?> secondArg = args.get(1);
            // Check whether type of the first argument is correct
            if ( !firstArg.instanceOf(Type.FUNCTION) )
            {
                throw new InvalidTermException("Type Mismatch: filter, first argument, expected a predicate: Object -> Boolean, found a " + firstArg.getType().getName());
            }
            //noinspection unchecked
            final @NotNull TFunction<TObject<?>, TObject<?>> predicate = (TFunction<TObject<?>, TObject<?>>)firstArg;
            //
            if ( !secondArg.instanceOf(Type.LIST) )
            {
                throw new InvalidTermException("Type Mismatch: filter, second argument, expected a list, found a " + secondArg.getType().getName());
            }
            final @NotNull TList listToFilter = (TList)secondArg;
            //
            return listToFilter.filter( predicate );
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: filter, expected exactly two arguments, but got " + nGivenArgs;
        }
    }

    private static class AtFunction extends TFunction<TObject<?>, TObject<?>>
    {
        AtFunction()
        {
            super("at", "at");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TObject<?> call(final @NotNull List<TObject<?>> args) throws InterpreterException
        {
            // args.size() == 2
            final @NotNull TObject<?> firstArg = args.get(0);
            final @NotNull TObject<?> secondArg = args.get(1);
            // Check whether type of the first argument is correct
            if ( !firstArg.instanceOf(Type.LIST) )
            {
                throw new InvalidTermException("Type Mismatch: at, first argument, expected a list, found a " + firstArg.getType().getName());
            }
            final @NotNull TList list = (TList)firstArg;
            //
            if ( !secondArg.instanceOf(Type.NUMERAL) )
            {
                throw new InvalidTermException("Type Mismatch: at, second argument, expected a numeral, found a " + secondArg.getType().getName());
            }
            final @NotNull TNumeral index = (TNumeral)secondArg;
            //
            return list.at(index);
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: at, expected exactly two arguments";
        }
    }

    private static class Join extends TFunction<TList, TList>
    {
        Join()
        {
            super("join", "join");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount > 0;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TList> args)
        {
            // args.size() >= 1
            return args.stream().reduce(EMPTY_LIST, TList::join);
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: join, expected at least 1 argument, but got " + nGivenArgs;
        }
    }

    private static class Append extends TFunction<TObject<?>, TList>
    {
        Append()
        {
            super( "append", "append" );
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount >= 2;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args) throws InvalidTermException
        {
            // args.size() >= 2
            final @NotNull TObject<?> firstArg = args.get(0);
            // Check whether the first param is a list
            if ( !firstArg.instanceOf(Type.LIST) )
            {
                final String gotType = firstArg.getType().getName();
                throw new InvalidTermException("Type Mismatch: append, first argument, expected a List, found a " + gotType);
            }
            // Append all remaining items
            @NotNull TList source = (TList)firstArg;
            for (int i = 1; i < args.size(); i++)
            {
                source = source.append(args.get(i));
            }
            //
            return source;
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: append, expected at least 2 arguments, but got " + nGivenArgs;
        }
    }

    private static class Head extends TFunction<TList, TObject<?>>
    {

        Head()
        {
            super("head", "head");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TObject<?> call(final @NotNull List<TList> args)
        {
            // args.size == 1
            final @NotNull TList list = args.get(0);
            return list.head();
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: head, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class Tail extends TFunction<TList, TList>
    {

        Tail()
        {
            super("tail", "tail");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TList> args)
        {
            // args.size == 1
            final @NotNull TList list = args.get(0);
            return list.tail();
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: tail, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class Mapping extends TFunction<TObject<?>, TList>
    {

        Mapping()
        {
            super("map", "map");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return (argsCount == 2) || (argsCount == 3);
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args) throws InterpreterException
        {
            // args.size is 2 or 3
            final @NotNull var mapping = (TFunction<TObject<?>, TObject<?>>)args.get(0);
            final @NotNull var firstList = (TList)args.get(1);
            if ( args.size() == 3 )
            {
                final @NotNull var secondList = (TList)args.get(2);
                return firstList.map(mapping, secondList);
            }
            else
            {
                return firstList.map(mapping);
            }
        }

        @Override
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity Mismatch: map, expected 2 or 3 arguments, but got " + nGivenArgs;
        }
    }

}