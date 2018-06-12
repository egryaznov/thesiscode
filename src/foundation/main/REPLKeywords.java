package foundation.main;

import foundation.lisp.Interpreter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum REPLKeywords
{
    TIME_KEYWORD("$time")
            {
                @Override
                public void act(@NotNull Interpreter in)
                {
                    final double MILLION = 1000000L; // Six zeroes
                    final double millis = in.lastBenchmark() / MILLION;
                    System.out.println(String.format("Eval Time: %.4f ms", millis));
                }
            },
    DISABLE_CACHING_KEYWORD("$nocache")
            {
                @Override
                public void act(@NotNull Interpreter in)
                {
                    in.disableCaching();
                    System.out.println("Caching DISABLED");
                }
            },
    DEFINITIONS_KEYWORD("$defs")
            {
                @Override
                public void act(@NotNull Interpreter in)
                {
                    System.out.println("All user-defined functions:");
                    Arrays.stream( in.definitions() )
                            .sorted()
                            .forEach( def -> System.out.println("    " + def) );
                }
            },
    CACHE_SIZE_KEYWORD("$size")
            {
                @Override
                public void act(@NotNull Interpreter in)
                {
                    System.out.println("# entries in cache: " + in.cacheSize());
                }
            },
    ENABLE_CACHING_KEYWORD("$cache")
            {
                @Override
                public void act(@NotNull Interpreter in)
                {
                    in.enableCaching();
                    System.out.println("Caching ENABLED");
                }
            },
    EXPUNGE_CACHE_KEYWORD("$expunge")
            {
                @Override
                public void act(@NotNull Interpreter in)
                {
                    in.expungeCache();
                    System.out.println("Cache erased");
                }
            };



    private final @NotNull String keyword;

    REPLKeywords(final @NotNull String keyword)
    {
        this.keyword = keyword;
    }



    public static boolean isKeyword(final @NotNull String userTyped)
    {
        return Arrays.stream(values()).anyMatch( keyword -> keyword.matches(userTyped) );
    }

    public static @Nullable REPLKeywords get(final @NotNull String word)
    {
        return Arrays.stream( values() )
                .filter( keyword -> keyword.matches(word) )
                .findAny()
                .orElse(null);
    }



    public abstract void act(final @NotNull Interpreter in);



    public boolean matches(final @NotNull String possibleKeyword)
    {
        return this.keyword.equalsIgnoreCase(possibleKeyword);
    }

    public @NotNull String getKeyword()
    {
        return keyword;
    }
}
