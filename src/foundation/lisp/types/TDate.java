package foundation.lisp.types;

import foundation.entity.Person;
import foundation.lisp.exceptions.InvalidTermException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TDate extends TObject<Calendar>
{
    public static final String DATE_PATTERN = "(0?[1-9]|[1-2]\\d|30|31)\\.(0?[1-9]|1[0-2])\\.[1-2]\\d\\d\\d";
    public static final String NOW_KEYWORD = "now";
    private static final @NotNull TDate NOW = new TDate(Calendar.getInstance());

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

    private @NotNull TNumeral day()
    {
        assert this.getValue() != null : "Assert: TDate.year, this.value is null!";
        return new TNumeral( getValue().get(Calendar.DAY_OF_MONTH) );
    }

    private @NotNull TNumeral month()
    {
        assert this.getValue() != null : "Assert: TDate.year, this.value is null!";
        // NOTE: Отсчёт месяцев в классе Calendar начинается с нуля!
        return new TNumeral( getValue().get(Calendar.MONTH) + 1);
    }

    private @NotNull TNumeral year()
    {
        assert this.getValue() != null : "Assert: TDate.year, this.value is null!";
        return new TNumeral( this.getValue().get(Calendar.YEAR) );
    }

    private @NotNull TBoolean before(final @NotNull TDate date)
    {
        assert this.getValue() != null : "Assert: TDate.before, super.value is null";
        assert date.getValue() != null : "Assert: TDate.before, date.value is null";
        return TBoolean.get(this.getValue().before( date.getValue() ));
    }

    private @NotNull TBoolean after(final @NotNull TDate date)
    {
        assert this.getValue() != null : "Assert: TDate.after, super.value is null";
        assert date.getValue() != null : "Assert: TDate.after, date.value is null";
        // NOTE: (after d1 d2) = (before d2 d1)?
        return TBoolean.get(this.getValue().after( date.getValue() ));
    }

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
        final @NotNull var year = new YearFunction();
        dict.put( year.getName(), year );
        final @NotNull var month = new MonthFunction();
        dict.put( month.getName(), month );
        final @NotNull var day = new DayFunction();
        dict.put( day.getName(), day );
    }

    static @NotNull TDate parseDate(final String rawDate) throws InvalidTermException
    {
        // Check whether `rawDate` is a well-formed date type
        if ( isDate(rawDate) )
        {
            if ( rawDate.equals(NOW_KEYWORD) )
            {
                return NOW;
            }
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
        return ( value == null )? TVoid.instance.valueToString() : String.format("(date '%s')", Person.calendarToString(value));
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
        String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: during, expected exactly 3 arguments, but " + nGivenArgs + " given.";
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
        String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: before, expected exactly 2 arguments, but got " + nGivenArgs;
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
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: after, expected exactly 2 arguments, but got " + nGivenArgs;
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
        @NotNull String mismatchMessage(final int nGivenArgs)
        {
            return "Arity mismatch: date, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class DayFunction extends TFunction<TDate, TNumeral>
    {
        DayFunction()
        {
            super("day", "day");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TDate> args)
        {
            // args.size == 1
            final @NotNull TDate date = args.get(0);
            return date.day();
        }

        @Override
        @NotNull String mismatchMessage(int nGivenArgs)
        {
            return "Arity Mismatch: day, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class MonthFunction extends TFunction<TDate, TNumeral>
    {
        MonthFunction()
        {
            super("month", "month");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TDate> args)
        {
            // args.size == 1
            final @NotNull TDate date = args.get(0);
            return date.month();
        }

        @Override
        @NotNull String mismatchMessage(int nGivenArgs)
        {
            return "Arity Mismatch: month, expected exactly one argument, but got " + nGivenArgs;
        }
    }

    private static class YearFunction extends TFunction<TDate, TNumeral>
    {
        YearFunction()
        {
            super("year", "year");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TDate> args)
        {
            // args.size == 1
            final @NotNull TDate date = args.get(0);
            return date.year();
        }

        @Override
        @NotNull String mismatchMessage(int nGivenArgs)
        {
            return "Arity Mismatch: year, expected exactly one argument, but got " + nGivenArgs;
        }
    }
}
