package foundation.lisp.types;

import foundation.entity.KinshipDictionary;
import foundation.entity.Person;
import foundation.entity.Vertex;
import foundation.lisp.Interpreter;
import foundation.lisp.exceptions.InvalidTermException;
import foundation.main.Ontology;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TPerson extends TObject<Vertex>
{
    private static @Nullable Interpreter in;
    public final static String PEOPLE_KEYWORD = "people";

    private TPerson(final @NotNull Vertex v)
    {
        super(Type.PERSON, v);
    }



    public static void setInterpreter(final @NotNull Interpreter interpreter)
    {
        TPerson.in = interpreter;
    }

    /*
        Computes the so-called "generation distance" between two relatives.
        The "generation distance" is the difference between the generation of this person and the generation of `kinsman`.
     */
    private @NotNull TNumeral generationDistance(final @NotNull TPerson kinsman)
    {
        assert in != null : "Assert: TPerson.generationDistance, illegal state, interpreter is null!";
        assert getValue() != null : "Assert: TPerson.generationDistance, this.value is null!";
        assert kinsman.getValue() != null : "Assert: TPerson.generationDistance, kinsman.value is null!";
        final int distance = in.ontology().tree().generationDistance(getValue(), kinsman.getValue());
        return new TNumeral(distance);
    }

    private @NotNull TList kinship(final @NotNull TPerson kinsman)
    {
        assert in != null : "Assert: TPerson.kinship, illegal state, interpreter is null!";
        assert getValue() != null : "Assert: TPerson.kinship, this.value is null!";
        assert kinsman.getValue() != null : "Assert: TPerson.kinship, kinsman.value is null!";
        final @NotNull List<String> kinTerms = in.ontology().tree().kinship(getValue(), kinsman.getValue());
        return new TList( kinTerms.stream().map(str -> new TString(str, false)).collect(Collectors.toList()) );
    }

    private @NotNull TObject<?> father()
    {
        assert getValue() != null : "Assert: TPerson.father, this.value is null!";
        final @Nullable Vertex father = getValue().getFather();
        //
        return ( father == null )? TVoid.instance : new TPerson(father);
    }

    private @NotNull TList children()
    {
        assert getValue() != null : "Assert: TPerson.children, this.value is null!";
        final @Nullable List<Vertex> children = getValue().children();
        return new TList( children.stream().map(TPerson::new).collect(Collectors.toList()) );
    }

    private @NotNull TObject<?> spouse()
    {
        assert getValue() != null : "Assert: TPerson.spouse, this.value is null!";
        final @Nullable Vertex spouse = getValue().getSpouse();
        //
        return ( spouse == null )? TVoid.instance : new TPerson(spouse);
    }

    /*
        Returns mother (TPerson) of this person or TVoid if there is none.
     */
    private @NotNull TObject<?> mother()
    {
        assert getValue() != null : "Assert: TPerson.mother, this.value is null!";
        final @Nullable Vertex mother = getValue().getMother();
        //
        return ( mother == null )? TVoid.instance : new TPerson( mother );
    }

    static @NotNull TList people()
    {
        assert in != null : "Assert: TPerson.people(), illegal state, in is null!";
        final @NotNull List<Vertex> vertices = in.ontology().tree().vertices();
        final @NotNull List<TObject<?>> people = vertices.stream().map( TPerson::new ).collect(Collectors.toList());
        return new TList( people );
    }

    private @NotNull TObject<?> getAttribute(final @NotNull TString attrName) throws InvalidTermException
    {
        assert attrName.getValue() != null : "Assert: TPerson.getAttribute, attrName.value is null!";
        assert getValue() != null : "Assert: TPerson.getAttribute, super.value is null!";
        //
        final @NotNull String lowercase = attrName.getValue().toLowerCase();
        final @NotNull Vertex v = getValue();
        final @NotNull TObject<?> result;
        switch (lowercase)
        {
            case "first name" :
            {
                result = new TString( v.profile().getFirstName(), false );
                break;
            }
            case "second name" : // NOTE: Will do the same as the "last name" case
            case "last name" :
            {
                result = new TString( v.profile().getLastName(), false );
                break;
            }
            case "full name" :
            {
                result = new TString( v.profile().getFirstName() + " " + v.profile().getLastName(), false );
                break;
            }
            case "age" :
            {
                final @NotNull Calendar birthDate = Person.stringToCalendar( v.profile().getDateOfBirth() );
                final @NotNull Calendar now       = Calendar.getInstance();
                final int currentYear = now.get(Calendar.YEAR);
                final int birthYear   = birthDate.get(Calendar.YEAR);
                return new TNumeral(currentYear - birthYear);
            }
            case "birth date" :    // NOTE: Identical to the "birth" case
            case "date of birth" : // NOTE: Identical to the "birth" case
            case "birth" :
            {
                result = TDate.parseDate( v.profile().getDateOfBirth() );
                break;
            }
            case "gender" : // NOTE: Identical to the "sex" case
            case "sex" :
            {
                // NOTE: Either "MALE" or "FEMALE"!
                result = new TString( v.profile().getSex().toLowerCase(), false );
                break;
            }
            case "occupation" :
            {
                result = new TString( v.profile().getOccupation(), false );
                break;
            }
            case "phone" :         // NOTE: Identical to the "tel" case
            case "phone number" :  // NOTE: Identical to the "tel" case
            case "tel" :
            {
                result = new TString( v.profile().getPhone(), false );
                break;
            }
            case "email" : // NOTE: Identical to the "e-mail" case
            case "e-mail" :
            {
                result = new TString( v.profile().getEmail(), false );
                break;
            }
            case "date of wedding" :
            case "wedding date" :
            case "wedding" :
            case "marriage date" :
            case "date of marriage" :
            case "marriage" :
            {
                assert in != null : "Assert: TPerson.getAttribute(), Interpreter is not set up";
                final @Nullable Calendar weddingDate = v.getWeddingDate();
                if ( weddingDate == null )
                {
                    result = TVoid.instance;
                }
                else
                {
                    result = new TDate( weddingDate );
                }
                break;
            }
            default:
            {
                result = TVoid.instance;
                break;
            }
        }
        //
        return result;
    }

    private static TObject<?> getPerson(final @NotNull Ontology model, final @NotNull String fullName)
    {
        // Search for a getPerson with provided name
        final @Nullable Vertex requestedVertex = model.tree().getVertex(fullName);
        if (requestedVertex == null)
        {
            return TVoid.instance;
        }
        else
        {
            return new TPerson(requestedVertex);
        }
    }

    private static TObject<?> getPerson(final @NotNull Ontology model, final String firstName, final String lastName)
    {
        // Search for a getPerson with provided name
        final @Nullable Vertex requestedVertex = model.tree().getVertex(firstName, lastName);
        if (requestedVertex == null)
        {
            return TVoid.instance;
        }
        else
        {
            return new TPerson(requestedVertex);
        }
    }

    @Override
    public @NotNull String termToString()
    {
        final @Nullable Vertex v = getValue();
        if ( v == null )
        {
            return TVoid.instance.termToString();
        }
        final @NotNull String firstName = v.profile().getFirstName();
        final @NotNull String lastName = v.profile().getLastName();
        return String.format("(person '%s' '%s')", firstName, lastName);
    }

    public static void registerAtomicFunctions(final @NotNull Map<String, TFunction> dict)
    {
        final @NotNull TFunction getPerson = new GetPersonFunction();
        dict.put(getPerson.getName(), getPerson);
        final @NotNull TFunction attr = new GetAttributeFunction();
        dict.put(attr.getName(), attr);
        final @NotNull Father father = new Father();
        dict.put(father.getName(), father);
        final @NotNull Mother mother = new Mother();
        dict.put(mother.getName(), mother);
        final @NotNull Spouse spouse = new Spouse();
        dict.put(spouse.getName(), spouse);
        final @NotNull Children children = new Children();
        dict.put(children.getName(), children);
        final @NotNull KinshipFunction kinship = new KinshipFunction();
        dict.put(kinship.getName(), kinship);
        final @NotNull GenDistanceFunction genDist = new GenDistanceFunction();
        dict.put(genDist.getName(), genDist);
        final @NotNull Shorten shorten = new Shorten();
        dict.put(shorten.getName(), shorten);
        final @NotNull PutKinshipTerm pkt = new PutKinshipTerm();
        dict.put(pkt.getName(), pkt);
    }



    private static class GenDistanceFunction extends TFunction<TPerson, TNumeral>
    {
        GenDistanceFunction()
        {
            super("gen-dist", "gen-dist");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TNumeral call(final @NotNull List<TPerson> args)
        {
            // args.size() == 2
            final @NotNull TPerson from = args.get(0);
            final @NotNull TPerson to = args.get(1);
            assert from.getValue() != null : "Assert: gen-dist, from.value is null";
            assert to.getValue() != null : "Assert: gen-dist, to.value is null";
            return from.generationDistance(to);
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch: gen-dist, expected exactly two arguments";
        }
    }

    private static class KinshipFunction extends TFunction<TPerson, TList>
    {
        KinshipFunction()
        {
            super("kinship", "kinship");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TPerson> args)
        {
            // args.size() == 2
            final @NotNull TPerson p1 = args.get(0);
            final @NotNull TPerson p2 = args.get(1);
            return p1.kinship(p2);
        }

        @Override
        String mismatchMessage()
        {
            return "Arity Mismatch: kinship, expected exactly 2 arguments";
        }
    }

    private static class GetPersonFunction extends TFunction<TString, TObject<?>>
    {
        GetPersonFunction()
        {
            super("person", "person");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return (argsCount == 2) || (argsCount == 1);
        }

        @NotNull
        @Override
        TObject<?> call(final @NotNull List<TString> args)
        {
            // args.size() == 2 or 1
            assert in != null : "Assert: GetPersonFunction.call, interpreter is null!";
            final @NotNull Ontology ontology = in.ontology();
            final TObject<?> result;
            if (args.size() == 1)
            {
                final @NotNull TString fullName = args.get(0);
                assert fullName.getValue() != null : "Assert, GetPersonFunction.call, name.value is null!";
                result = TPerson.getPerson(in.ontology(), fullName.getValue());
            }
            else
            {
                final @NotNull TString firstName = args.get(0);
                final @NotNull TString lastName = args.get(1);
                assert firstName.getValue() != null : "Assert: GetPersonFunction.call, firstName.value is null";
                assert lastName.getValue() != null : "Assert: GetPersonFunction.call, lastName.value is null";
                result = TPerson.getPerson(ontology, firstName.getValue(), lastName.getValue());
            }
            //
            return result;
        }

        @Override
        @NotNull String mismatchMessage()
        {
            return "Arity Mismatch: person, expected either one or two arguments";
        }
    }

    private static class GetAttributeFunction extends TFunction<TObject<?>, TObject<?>>
    {
        GetAttributeFunction()
        {
            super("attr", "attr");
        }


        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TObject<?> call(final @NotNull List<TObject<?>> args) throws InvalidTermException
        {
            // NOTE: args.size() == 2
            final @NotNull TPerson person = (TPerson)args.get(0);
            final @NotNull TString attribute = (TString)args.get(1);
            assert person.getValue() != null : "Assert: GetAttributeFunction.call, person.value is null";
            assert attribute.getValue() != null : "Assert: GetAttributeFunction.call, attribute.value is null";
            return person.getAttribute( attribute );
        }

        @Override
        String mismatchMessage()
        {
            return "Arity mismatch: attr, expected 2 arguments";
        }
    }

    /*
        TPerson        -> singleton TList<TPerson> with TPerson.father or empty TList iff there is none.
        TList<TPerson> -> TList<TPerson> with their fathers
     */
    private static class Father extends TFunction<TObject<?>, TList>
    {
        Father()
        {
            super("father", "father");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args) throws InvalidTermException
        {
            // args.size() == 1
            final @NotNull TObject<?> firstArg = args.get(0);
            final @NotNull TList result;
            if ( firstArg.instanceOf(Type.PERSON) )
            {
                final @NotNull TObject<?> fatherOrVoid = ((TPerson)firstArg).father();
                if ( fatherOrVoid.isVoid() )
                {
                    result = TList.EMPTY_LIST;
                }
                else
                {
                    result = new TList( Collections.singletonList(fatherOrVoid) );
                }
            }
            else if ( firstArg.instanceOf(Type.LIST) )
            {
                final @NotNull TList egos = (TList)firstArg;
                assert egos.getValue() != null : "Assert: father, egos.value is null!";
                final @NotNull List<TObject<?>> kin = egos.getValue();
                final @NotNull List<TObject<?>> fathers = new ArrayList<>();
                for (final TObject<?> item : kin)
                {
                    if ( !item.instanceOf(Type.PERSON) )
                    {
                        final String got = item.getType().getName();
                        throw new InvalidTermException("Type Mismatch: father, found a non-person item in the argument list: "+ got);
                    }
                    final @NotNull TObject<?> fatherOrVoid = ((TPerson)item).father();
                    if ( !fatherOrVoid.isVoid() )
                    {
                        fathers.add( fatherOrVoid );
                    }
                }
                result = new TList( fathers );
            }
            else
            {
                final String got = firstArg.getType().getName();
                throw new InvalidTermException("Type Mismatch: father, expected a Person or List of persons, got " + got);
            }
            //
            return result;
        }

        @Override
        String mismatchMessage()
        {
            return "Arity Mismatch: father, expected exactly one argument";
        }
    }

    /*
        TPerson        -> singleton TList<TPerson> with TPerson.mother or empty TList iff there is none.
        TList<TPerson> -> TList<TPerson> with their mothers
     */
    private static class Mother extends TFunction<TObject<?>, TList>
    {
        Mother()
        {
            super("mother", "mother");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args) throws InvalidTermException
        {
            // args.size() == 1
            final @NotNull TObject<?> firstArg = args.get(0);
            final @NotNull TList result;
            if ( firstArg.instanceOf(Type.PERSON) )
            {
                final @NotNull TObject<?> motherOrVoid = ((TPerson)firstArg).mother();
                if ( motherOrVoid.isVoid() )
                {
                    result = TList.EMPTY_LIST;
                }
                else
                {
                    result = new TList( Collections.singletonList(motherOrVoid) );
                }
            }
            else if ( firstArg.instanceOf(Type.LIST) )
            {
                final @NotNull TList egos = (TList)firstArg;
                assert egos.getValue() != null : "Assert: mother, egos.value is null!";
                final @NotNull List<TObject<?>> kin = egos.getValue();
                final @NotNull List<TObject<?>> mothers = new ArrayList<>();
                for (final TObject<?> item : kin)
                {
                    if ( !item.instanceOf(Type.PERSON) )
                    {
                        final String got = item.getType().getName();
                        throw new InvalidTermException("Type Mismatch: mother, found a non-person item in the argument list: "+ got);
                    }
                    final @NotNull TObject<?> motherOrVoid = ((TPerson)item).mother();
                    if ( !motherOrVoid.isVoid() )
                    {
                        mothers.add( motherOrVoid );
                    }
                }
                result = new TList( mothers );
            }
            else
            {
                final String got = firstArg.getType().getName();
                throw new InvalidTermException("Type Mismatch: mother, expected a Person or List of persons, got " + got);
            }
            //
            return result;
        }

        @Override
        String mismatchMessage()
        {
            return "Arity Mismatch: mother, expected exactly one argument";
        }
    }

    /*
        TPerson        -> singleton TList<TPerson> with TPerson.spouse or empty TList iff there is none.
        TList<TPerson> -> TList<TPerson> with their spouses
     */
    private static class Spouse extends TFunction<TObject<?>, TList>
    {
        Spouse()
        {
            super("spouse", "spouse");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args) throws InvalidTermException
        {
            // args.size() == 1
            final @NotNull TObject<?> firstArg = args.get(0);
            final @NotNull TList result;
            if ( firstArg.instanceOf(Type.PERSON) )
            {
                final @NotNull TObject<?> spouseOrVoid = ((TPerson)firstArg).spouse();
                if ( spouseOrVoid.isVoid() )
                {
                    result = TList.EMPTY_LIST;
                }
                else
                {
                    result = new TList( Collections.singletonList(spouseOrVoid) );
                }
            }
            else if ( firstArg.instanceOf(Type.LIST) )
            {
                final @NotNull TList egos = (TList)firstArg;
                assert egos.getValue() != null : "Assert: spouse, egos.value is null!";
                final @NotNull List<TObject<?>> kin = egos.getValue();
                final @NotNull List<TObject<?>> spouses = new ArrayList<>();
                for (final TObject<?> item : kin)
                {
                    if ( !item.instanceOf(Type.PERSON) )
                    {
                        final String got = item.getType().getName();
                        throw new InvalidTermException("Type Mismatch: spouse, found a non-person item in the argument list: "+ got);
                    }
                    final @NotNull TObject<?> spouseOrVoid = ((TPerson)item).spouse();
                    if ( !spouseOrVoid.isVoid() )
                    {
                        spouses.add( spouseOrVoid );
                    }
                }
                result = new TList( spouses );
            }
            else
            {
                final String got = firstArg.getType().getName();
                throw new InvalidTermException("Type Mismatch: spouse, expected a Person or List of persons, got " + got);
            }
            //
            return result;
        }

        @Override
        String mismatchMessage()
        {
            return "Arity Mismatch: spouse, expected exactly one argument";
        }
    }

    /*
        TPerson -> TList<TPerson> with his/her children, maybe empty.
        TList<TPerson> -> TList<TPerson> all lists of their children joined together
     */
    private static class Children extends TFunction<TObject<?>, TList>
    {
        Children()
        {
            super("children", "children");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 1;
        }

        @NotNull
        @Override
        TList call(final @NotNull List<TObject<?>> args) throws InvalidTermException
        {
            // args.size() == 1
            final @NotNull TObject<?> firstArg = args.get(0);
            final @NotNull TList result;
            if ( firstArg.instanceOf(Type.PERSON) )
            {
                result = ((TPerson)firstArg).children();
            }
            else if ( firstArg.instanceOf(Type.LIST) )
            {
                final @NotNull TList egos = (TList)firstArg;
                assert egos.getValue() != null : "Assert: children, egos.value is null!";
                final @NotNull List<TObject<?>> kin = egos.getValue();
                final @NotNull List<Vertex> allChildren = new ArrayList<>();
                for (final TObject<?> item : kin)
                {
                    // NOTE: Я просто пропускаю все void элементы
                    if ( !item.instanceOf(Type.PERSON) )
                    {
                        final String got = item.getType().getName();
                        throw new InvalidTermException("Type Mismatch: children, found a non-person item in the argument list: "+ got);
                    }
                    final @NotNull TPerson kinsman = (TPerson)item;
                    assert kinsman.getValue() != null : "Assert: children, kinsman.value is null!";
                    allChildren.addAll(kinsman.getValue().children());
                }
                //
                result = new TList( allChildren.stream().map(TPerson::new).collect(Collectors.toList()) );
            }
            else
            {
                final String got = firstArg.getType().getName();
                throw new InvalidTermException("Type Mismatch: spouse, children a Person or List of persons, got " + got);
            }
            //
            return result;
        }

        @Override
        String mismatchMessage()
        {
            return "Arity Mismatch: children, expected exactly one argument";
        }
    }

    private static class Shorten extends TFunction<TList, TList>
    {

        Shorten()
        {
            super("shorten", "shorten");
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
            // args.size() == 1;
            final @NotNull TList firstArg = args.get(0);
            assert firstArg.getValue() != null : "Assert: TPerson.shortenKinshipTerm, term.value is null!";
            final @NotNull List<String> kinship = firstArg.getValue().stream()
                    .map(tStr -> (String)tStr.getValue())
                    .collect(Collectors.toList());
            //
            final @NotNull List<TObject<?>> shortenedTerm = KinshipDictionary.instance.shorten(kinship).stream()
                    .map(str -> new TString(str, false))
                    .collect(Collectors.toList());
            return new TList( shortenedTerm );
        }

        @Override
        String mismatchMessage()
        {
            return "Arity Mismatch: shorten, expected exactly one argument";
        }
    }

    private static class PutKinshipTerm extends TFunction<TString, TVoid>
    {

        PutKinshipTerm()
        {
            super("put-kinship-term", "put-kinship-term");
        }

        @Override
        boolean argsArityMatch(int argsCount)
        {
            return argsCount == 2;
        }

        @NotNull
        @Override
        TVoid call(final @NotNull List<TString> args)
        {
            // args.size() == 2;
            final @NotNull TString longKinshipTerm = args.get(0);
            assert longKinshipTerm.getValue() != null : "Assert: put-kinship-term, long.value is null!";
            final @NotNull TString shortKinshipTerm = args.get(1);
            assert shortKinshipTerm.getValue() != null : "Assert: put-kinship-term, short.value is null!";
            //
            final @NotNull KinshipDictionary kinDict = KinshipDictionary.instance;
            kinDict.putKinship( longKinshipTerm.getValue(), shortKinshipTerm.getValue() );
            kinDict.saveToFile();
            return TVoid.instance;
        }

        @Override
        String mismatchMessage()
        {
            return "Arity Mismatch: put-kinship-term, expected exactly 2 arguments";
        }
    }

}