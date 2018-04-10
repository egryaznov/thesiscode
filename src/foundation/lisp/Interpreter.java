package foundation.lisp;

import foundation.lisp.exceptions.InterpreterException;
import foundation.lisp.exceptions.InvalidTermException;
import foundation.lisp.types.Lambda;
import foundation.lisp.types.TBoolean;
import foundation.lisp.types.TDate;
import foundation.lisp.types.TFunction;
import foundation.lisp.types.TList;
import foundation.lisp.types.TNumeral;
import foundation.lisp.types.TObject;
import foundation.lisp.types.TPerson;
import foundation.lisp.types.TString;
import foundation.lisp.types.TVoid;
import foundation.lisp.types.Type;
import foundation.main.Ontology;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class Interpreter
{
    private final static String DEFINE_KEYWORD = "define";
    private final static String LAMBDA_KEYWORD = "lambda";
    private final static String IF_KEYWORD     = "if";
    private final @NotNull Ontology ontology;
    private long lastBenchmark;

    private final @NotNull Map<String, Boolean> keywords = new HashMap<>();
    /*
        Map of all atomic atomicFunctions like +, * etc. and anonymous lambdas
     */
    private final @NotNull Map<String, TFunction> atomicFunctions = new HashMap<>();
    /*
        Users' custom definitions
     */
    private final @NotNull Map<String, String> definitions = new HashMap<>();
    /*
        Stores the result of previously evaluated subterms to speed up computation of the main term.
     */
    private final @NotNull Map<String, TObject> evaluatedTerms = new HashMap<>();

    public Interpreter(final @NotNull Ontology ontology)
    {
        this.ontology = ontology;
        TPerson.setInterpreter(this);
        initAtomicFunctions();
        initKeywords();
    }



    private void initKeywords()
    {
        keywords.put(DEFINE_KEYWORD, true);
        keywords.put(LAMBDA_KEYWORD, true);
        keywords.put(IF_KEYWORD, true);
        keywords.put(TPerson.PEOPLE_KEYWORD, true);
        keywords.put(TBoolean.TRUE_KEYWORD, true);
        keywords.put(TBoolean.FALSE_KEYWORD, true);
        keywords.put(Type.VOID.getName(), true);
        keywords.put(TDate.NOW_KEYWORD, true);
    }

    private void initAtomicFunctions()
    {
        TBoolean.registerAtomicFunctions( atomicFunctions );
        TDate.registerAtomicFunctions( atomicFunctions );
        TNumeral.registerAtomicFunctions( atomicFunctions );
        TString.registerAtomicFunctions( atomicFunctions );
        TObject.registerAtomicFunctions( atomicFunctions );
        TPerson.registerAtomicFunctions( atomicFunctions );
        TList.registerAtomicFunctions( atomicFunctions );
    }

    public @NotNull Map<String, TFunction> getAtomicFunctions()
    {
        return atomicFunctions;
    }

    public @NotNull Ontology ontology()
    {
        return ontology;
    }

    /*
        Executes every term in text file `axioms`, but ignores the result of an evaluation.
        Allows to insert line comments which should start with an '#'.
        Use the method to quickly evaluate definitions from a file.
     */
    public void exec(final @NotNull File file) throws InterpreterException
    {
        try ( final @NotNull BufferedReader br = new BufferedReader(new FileReader(file)) )
        {
            @NotNull String terms = "(";
            @Nullable String nextLine = br.readLine();
            while ( nextLine != null )
            {
                // NOTE: Строка с комментарием должна начинаться с ;
                if (nextLine.isEmpty())
                {
                    terms = terms.concat(" ");
                }
                else
                {
                    if (nextLine.charAt(0) != ';')
                    {
                        terms = terms.concat(nextLine);
                    }
                }
                //
                nextLine = br.readLine();
            }
            // Split the giant concatenated term and execute each axiom
            final @NotNull List<String> axioms = splitByTerms(terms + ")", false, false);
            for (final String term : axioms)
            {
                exec(term);
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }


    /*
        Removes outer brackets, like '(' and ')', from 'term'.
        For instance, free('(+ 2 3)') = '+ 2 3'
     */
    public String free(final String term, final boolean isClipped)
    {
        final String clippedTerm = ( isClipped )? term : clip(term);
        final String result;
        if ( isNotAFunctionalTerm(clippedTerm) )
        {
            result = term;
        }
        else
        {
            final int len = term.length();
            result = term.substring(1, len - 1);
        }
        //
        return result;
    }

    private boolean isLambda(final String term)
    {
        return isLeader(term, LAMBDA_KEYWORD);
    }

    private boolean isDefine(final String term)
    {
        return isLeader(term, DEFINE_KEYWORD);
    }

    private boolean isIf(final String term)
    {
        return isLeader(term, IF_KEYWORD);
    }

    /*
        Checks whether the leader, i.e. the first subterm in a functional term, equals to the string `leader`.
     */
    private boolean isLeader(final String term, final String leader)
    {
        final boolean result;
        if ( isNotAFunctionalTerm(term) )
        {
            result = term.equals(leader);
        }
        else
        {
            final int firstSpaceIndex = term.indexOf(' ');
            if ( firstSpaceIndex == -1 )
            {
                return term.equals(leader);
            }
            else
            {
                result = term.substring(1, firstSpaceIndex).equals(leader);
            }
        }
        //
        return result;
    }

    @SuppressWarnings("all")
    public @NotNull TObject<?> exec(final String query) throws InterpreterException
    {
        return exec(query, true, false);
    }

    /*
        The main method of this class. Returns the result of a computation of the term `query`.
        We can also specify whether we need to clip `query` first or benchmark its' execution.
     */
    public @NotNull TObject<?> exec(final String query, final boolean isQueryClipped, final boolean benchmark)
            throws InterpreterException
    {
        final long time = System.nanoTime();
        if ( !isTermValid(query) )
        {
            throw new InvalidTermException("Not well-formed term: " + query);
        }
        //
        final String clippedQuery = (isQueryClipped)? query : clip(query);
        final @Nullable TObject<?> result;
        if ( isDefine(clippedQuery) )
        {
            final @NotNull List<String> terms = splitByTerms(clippedQuery);
            final String reference = terms.get(1);
            // Check the validity of this 'define' `query`
            if ( !isReference(reference) )
            {
                throw new InvalidTermException("Expected a reference, found " + reference);
            }
            if ( terms.size() != 3 )
            {
                throw new InvalidTermException("Malformed define query: " + query);
            }
            if ( atomicFunctions.containsKey(reference) )
            {
                throw new InvalidTermException("Cannot redefine an atomic function: " + reference);
            }
            if ( keywords.containsKey(reference) )
            {
                throw new InvalidTermException("Cannot redefine a keyword: " + reference);
            }
            if ( definitions.containsKey(reference) )
            {
                throw new InvalidTermException("Cannot redefine previously defined reference: " + reference);
            }
            final @NotNull String designatum = terms.get(2);
            definitions.put( reference, eval(designatum).termToString() );
            result = TVoid.instance;
        }
        else
        {
            result = eval(rewrite(clippedQuery, definitions));
        }
        if (benchmark)
        {
            this.lastBenchmark = System.nanoTime() - time;
        }
        //
        return result;
    }

    @SuppressWarnings("unused")
    public long lastBenchmark()
    {
        return lastBenchmark;
    }

    public void printLastBenchmark()
    {
        System.out.printf("\n------\nQuery evaluated in %d milliseconds.\n", lastBenchmark / 1000000);
    }

    /*
        Evaluates the `term` and returns the result of its' evaluation.
     */
    @SuppressWarnings("unchecked")
    public @NotNull TObject<?> eval(final String term) throws InterpreterException
    {
        final @NotNull TObject<?> result;
        if ( isPrimitive(term) )
        {
            result = TObject.parsePrimitive(term);
        }
        else if ( atomicFunctions.containsKey(term) )
        {
            result = atomicFunctions.get(term);
        }
        else if ( isLambda(term) )
        {
            result = new Lambda(this, term);
        }
        // XXX: Может сделать if функцией в TObject и избавиться от этого кейса?
        // XXX: Не получится, т.к. если if сделать отдельной функцией, то придётся вычислять оба под-терма,
        // XXX: if & else, что сделает невозможным выполнение рекурсии.
        else if ( isIf(term) )
        {
            // Extract subterms
            final @NotNull List<String> subterms = splitByTerms(term);
            if (subterms.size() != 4)
            {
                throw new InvalidTermException("Malformed 'if' term: " + term);
            }
            final String condStr = subterms.get(1);
            final String thenTerm = subterms.get(2);
            final String elseTerm = subterms.get(3);
            // Evaluate boolean condition
            final @NotNull TObject<?> cond = eval(condStr);
            if ( !cond.instanceOf(Type.BOOLEAN) )
            {
                throw new InvalidTermException("Type Mismatch: condition in 'if' term: " + term
                        + ". Expected a Boolean, found a " + cond.getType().getName());
            }
            // Evaluate result based on the condition
            final @NotNull TBoolean condition = (TBoolean)cond;
            if ( condition.passes() )
            {
                result = eval(thenTerm);
            }
            else
            {
                result = eval(elseTerm);
            }
        }
        else if ( isFunctionalTerm(term) )
        {
            if (evaluatedTerms.containsKey(term))
            {
                return evaluatedTerms.get(term);
            }
            // Proceed with evaluation of the new term
            final @NotNull List<String> subTerms = splitByTerms(term);
            final @NotNull String firstSubTerm = subTerms.get(0);
            final String leader = definitions.getOrDefault(firstSubTerm, firstSubTerm);
            @NotNull TFunction<TObject, TObject> leaderFunction;
            if ( leader.equals(DEFINE_KEYWORD) )
            {
                throw new InvalidTermException("Define is not allowed in functional terms");
            }
            else if ( isLambda(leader) )
            {
                leaderFunction = new Lambda(this, leader);
            }
            else if (atomicFunctions.containsKey(leader))
            {
                leaderFunction = atomicFunctions.get(leader);
            }
            else
            {
                // Для функций высших порядков
                final @NotNull TObject<?> evaluatedLeader = eval( leader );
                if ( evaluatedLeader.instanceOf(Type.FUNCTION) )
                {
                    leaderFunction = (TFunction<TObject, TObject>) evaluatedLeader;
                }
                else
                {
                    throw new InvalidTermException("Expected a function, got: " + evaluatedLeader.valueToString());
                }
            }
            // We have the function, now we need to evaluate the arguments
            final int nSubTerms = subTerms.size();
            if (nSubTerms == 1)
            {
                // Term has no arguments, so just apply in to empty list
                result = leaderFunction.apply(new LinkedList<>());
            }
            else
            {
                // Evaluate arguments one by one recursively
                final @NotNull List<TObject> args = new LinkedList<>();
                for (int i = 1; i < nSubTerms; i++)
                {
                    args.add( eval(subTerms.get(i)) ); // Recursive call!
                }
                // Arguments ready, now let's apply them to the function
                result = leaderFunction.apply(args);
            }
            // Store the evaluated result in the map to speed up computation
            evaluatedTerms.putIfAbsent(term, result);
        }
        else
        {
            throw new InvalidTermException("Unbounded identifier: " + term);
        }
        //
        return result;
    }

    /*
        Splits a query into a list of its' terms.
        For example, splitByTerms("(+ (+ 1 2) 3 (+ 4 (+ 5 6)) 'a bc')") = ["+", "(+ 1 2)", "3", "(+ 4 (+ 5 6))", "'a bc'"]
     */
    public @NotNull List<String> splitByTerms(final String query, final boolean isClipped, final boolean benchmark)
    {
        final long time = System.nanoTime();
        final String clippedTerm = (isClipped)? rewrite(query, definitions) : rewrite(clip(query), definitions);
        final List<String> result = splitByTerms( clippedTerm ).stream()
                .map(term -> term.replaceAll("_", " "))
                .collect(Collectors.toList());
        if (benchmark)
        {
            lastBenchmark = System.nanoTime() - time;
        }
        return result;
    }

    // Используется только в методе `clip`
    private String replaceSpacesInStringLiterals(final String term)
    {
        final StringBuilder sb = new StringBuilder(term);
        boolean underStringLiteral = false;
        for (int i = 0; i < sb.length(); i++)
        {
            switch (sb.charAt(i))
            {
                case '\'': underStringLiteral = !underStringLiteral; break;
                case ' ': sb.setCharAt(i,  (underStringLiteral)? '_' : ' ');
            }
        }
        //
        return sb.toString();
    }

    /*
        In the term `context` replaces all occurrences of each reference in the global dictionary
        `this.definitions` with its' designatum. Doesn't touch any string literal. Works in linear time.
     */
    public String rewrite(final String context, final @NotNull Map<String, String> dictionary)
    {
        if (context.isEmpty())
        {
            return context;
        }
        if ( isNotAFunctionalTerm(context) )
        {
            return dictionary.getOrDefault(context, context);
        }
        //
        final List<String> terms = splitByTerms(context).stream()
                .map(t -> rewrite(t, dictionary)) // Recursive enumeration
                .collect(Collectors.toList());
        //
        final String almostResult = terms.stream().reduce("", (t1, t2) -> t1.concat(" " + t2));
        final int len = almostResult.length();
        if ( len == 0 )
        {
            return "(" + almostResult + ")";
        }
        else
        {
            // We need the `.substring(1, len)` part because the first char in `almostResult` is a space ' '
            return "(" + almostResult.substring(1, len) + ")";
        }
    }

    /*
        Removes unnecessary whitespaces in 'query' and makes the whole string lowercase.
        This operation prepares the query for evaluation. We called it 'clipping', hence the name of this method.
     */
    public String clip(final String query)
    {
        return replaceSpacesInStringLiterals(query).replaceAll("\\(\\s+", "(")
                .replaceAll("\\s+\\)", ")")
                .replaceAll("\\s+", " ")
                .replaceAll("_", " ")
                .trim();
    }

    private boolean isTermValid(final String term)
    {
        boolean areParenthesesBalanced;
        boolean hasUnderscore = false;
        boolean hasDollarSign = false;
        boolean isNumberOfQuotesEven = true;
        int nestingLevel = 1;
        for (int i = 0; i < term.length(); i++)
        {
            final char chr = term.charAt(i);
            // Update state variables
            switch ( chr )
            {
                case '\'' :
                {
                    isNumberOfQuotesEven = !isNumberOfQuotesEven;
                    break;
                }
                case '(' :
                {
                    nestingLevel += (isNumberOfQuotesEven)? 1 : 0;
                    break;
                }
                case ')' :
                {
                    nestingLevel -= (isNumberOfQuotesEven)? 1 : 0;
                    break;
                }
                case '_' :
                {
                    hasUnderscore = true;
                    break;
                }
                case '$' :
                {
                    hasDollarSign = true;
                    break;
                }
            }
            areParenthesesBalanced = (nestingLevel > 0);
            // Check whether we have an improper balancing of parentheses or an '_' or '$' in the term
            if ( !areParenthesesBalanced || hasUnderscore )
            {
                // Stop the cycle because we cannot recover from the unbalanced parentheses or an '_' or '$' and
                // it is therefore a waste of calculation to continue checking the validity of a term.
                break;
            }
        }
        areParenthesesBalanced = ( nestingLevel == 1 );
        //
        return areParenthesesBalanced && isNumberOfQuotesEven && !hasUnderscore && !hasDollarSign;
    }

    /*
        Checks whether the `term` is a primitive.
        Primitive is either a string '.*' or a numeral -?\d+ or a boolean.
     */
    public boolean isPrimitive(final String term) throws InvalidTermException
    {
        for (final Type type : Type.values())
        {
            if (type.canParse(term) && type.isPrimitive())
            {
                return true;
            }
        }
        return false;
    }

    private boolean isNotAFunctionalTerm(final String term)
    {
        final char lastChar  = term.charAt(term.length() - 1);
        final char firstChar = term.charAt(0);
        return ( (firstChar != '(') && (lastChar != ')') );
    }

    private boolean isFunctionalTerm(final String literal) throws InvalidTermException
    {
        final boolean result = !isNotAFunctionalTerm(literal);
        if ( literal.equals(Lambda.EMPTY_PARAM_TERM) )
        {
            throw new InvalidTermException("Invalid functional term: ()");
        }
        //
        return result;
    }

    /*
        Checks whether the `term` is a reference.
        A reference is used in a `(define ref term)` query.
     */
    private boolean isReference(final String term)
    {
        return term.matches("\\b([a-z]+-)*[a-z]+\\b\\??");
    }

    /*
        Splits a query into a list of its' terms.
        For example, splitByTerms("(+ (+ 1 2) 3 (+ 4 (+ 5 6)) 'a bc')") = ["+", "(+ 1 2)", "3", "(+ 4 (+ 5 6))", "'a bc'"]
     */
    private @NotNull List<String> splitByTerms(final String query)
    {
        // If query is a literal, then just return it
        if ( isNotAFunctionalTerm(query) )
        {
            return Collections.singletonList(query);
        }
        // Remove outer brackets from the query
        // Query supposed to be already clipped
        final StringBuilder freeQuery = new StringBuilder(free(query, true));
        final Stack<String> processedTokens = new Stack<>();
        int level = 0;
        boolean underString = false;
        boolean nextTerm = true;
        for (int i = 0; i < freeQuery.length(); i++)
        {
            final char chr = freeQuery.charAt(i);
            if (underString)
            {
                final String lastToken = processedTokens.pop();
                processedTokens.push( lastToken + chr );
                underString = ( chr != '\'' );
            }
            else
            {
                if (level == 0)
                {
                    if ( chr == ' ' )
                    {
                        nextTerm = true;
                        continue;
                    }
                    //
                    if ( !nextTerm )
                    {
                        final String lastToken = processedTokens.pop();
                        processedTokens.push( lastToken + chr );
                    }
                    else
                    {
                        processedTokens.push( String.valueOf(chr) );
                        nextTerm = false;
                    }
                }
                else
                {
                    final String lastToken = processedTokens.pop();
                    processedTokens.push( lastToken + chr );
                }
                // Update state variables
                switch ( chr )
                {
                    case '\'' :
                    {
                        underString = true;
                        break;
                    }
                    case '(' :
                    {
                        level++;
                        break;
                    }
                    case ')' :
                    {
                        level--;
                        break;
                    }
                }
            }
        }
        //
        return processedTokens;
    }
}