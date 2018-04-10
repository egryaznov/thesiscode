package foundation.lisp.types;

import foundation.entity.Person;
import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

// NOTE: NOW := Calendar.getInstance();
public class TDate extends TObject<Calendar>
{
    public static final String DATE_PATTERN = "(0?[1-9]|[1-2]\\d|30|31)\\.(0?[1-9]|1[0-2])\\.[1-2]\\d\\d\\d";
    public static final String NOW_KEYWORD = "now";

    TDate(final @NotNull Calendar date)
    {
        super(Type.DATE, date);
    }

    private TDate(final String date)
    {
        this(Person.stringToCalendar(date));
    }



    /*
        Checks whether a string matches the date of birth pattern: "dd.mm.year".
     */
    static boolean isDate(final String date)
    {
        return date.equals(NOW_KEYWORD) || date.matches(DATE_PATTERN);
    }

    @SuppressWarnings("unused")
    private @NotNull TBoolean before(final @NotNull TDate date)
    {
        assert this.getValue() != null : "Assert: TDate.before, super.value is null";
        assert date.getValue() != null : "Assert: TDate.before, date.value is null";
        return TBoolean.get(this.getValue().before( date.getValue() ));
    }

    @SuppressWarnings("unused")
    private @NotNull TBoolean after(final @NotNull TDate date)
    {
        assert this.getValue() != null : "Assert: TDate.after, super.value is null";
        assert date.getValue() != null : "Assert: TDate.after, date.value is null";
        // NOTE: (after d1 d2) = (before d2 d1)?
        // return date.before(this);
        return TBoolean.get(this.getValue().after( date.getValue() ));
    }

    @SuppressWarnings("unused")
    private @NotNull TBoolean during(final @NotNull TDate beginning, final @NotNull TDate end)
    {
        assert after(beginning).getValue() != null : "Assert: TDate.during, super.value is null";
        assert before(end).getValue() != null : "Assert: TDate.during, beginning.value is null";
        final @NotNull TBoolean isAfterBeginning = after(beginning);
        final @NotNull TBoolean isBeforeEnd = before(end);
        final @NotNull TBoolean equalsBeginning = TBoolean.get( equals(beginning) );
        final @NotNull TBoolean equalsEnd = TBoolean.get( equals(end) );
        // XXX: Check correctness of this return
        return ( equalsBeginning.or(isAfterBeginning) ).and( isBeforeEnd.or(equalsEnd) );
    }

    public static void registerAtomicFunctions(final @NotNull Map<String, TFunction> dict)
    {
        final @NotNull TFunction during = new During();
        dict.put( during.getName(), during );
        final @NotNull TFunction before = new Before();
        dict.put( before.getName(), before );
        final @NotNull TFunction after = new After();
        dict.put( after.getName(), after );
        final @NotNull DateFunction date = new DateFunction();
        dict.put( date.getName(), date );
    }

    static @NotNull TDate parseDate(final String rawDate) throws InvalidTermException
    {
        // Check whether `rawDate` is a well-formed date type
        if ( isDate(rawDate) )
        {
            return new TDate(rawDate);
        }
        else
        {
            throw new InvalidTermException("Invalid date format: " + rawDate + "; Should be 'dd.mm.year'");
        }
    }

    @Override
    public String valueToString()
    {
        final @Nullable Calendar value = getValue();
        if (value == null)
        {
            return "null";
        }
        else
        {
            return Person.calendarToString(value);
        }
    }

    @Override
    public @NotNull String termToString()
    {
        final @Nullable Calendar value = getValue();
        return ( value == null )? TVoid.instance.valueToString() : String.format("(date %s)", Person.calendarToString(value));
    }

    private static class During extends TFunction<TDate, TBoolean>
    {
        private During()
        {
            super("during", "during");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 3;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TDate> args)
        {
            final @NotNull TDate between = args.get(0);
            final @NotNull TDate beginning = args.get(1);
            final @NotNull TDate end = args.get(2);
            return between.during(beginning, end);
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch: during, expected 3 arguments";
        }
    }

    private static class Before extends TFunction<TDate, TBoolean>
    {
        private Before()
        {
            super("before", "before");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TDate> args)
        {
            final @NotNull TDate past = args.get(0);
            final @NotNull TDate future = args.get(1);
            return past.before(future);
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch: before, expected 2 arguments";
        }
    }

    private static class After extends TFunction<TDate, TBoolean>
    {
        private After()
        {
            super("after", "after");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TBoolean call(final @NotNull List<TDate> args)
        {
            assert argsArityMatch(args.size()) : "Assert: After.call, the number of args should be 2, but it's " + args.size() + " instead";
            final @NotNull TDate future = args.get(0);
            final @NotNull TDate past = args.get(1);
            return future.after(past);
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch: after, expected 2 arguments";
        }
    }

    private static class DateFunction extends TFunction<TString, TDate>
    {
        DateFunction()
        {
            super("date", "date");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TDate call(@NotNull List<TString> args) throws InvalidTermException
        {
            assert argsArityMatch(args.size()) : "Assert: DateFunction.call, arity mismatch: " + args.size();
            final @NotNull TString rawDate = args.get(0);
            assert rawDate.getValue() != null : "Assert: DateFunction.call, rawDate.value is null!";
            return TDate.parseDate(rawDate.getValue());
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch: date, expected 1 argument";
        }
    }
}
