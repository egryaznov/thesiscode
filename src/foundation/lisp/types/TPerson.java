package foundation.lisp.types;

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

    public @NotNull TString kinship(final @NotNull TPerson kinsman)
    {
        assert in != null : "Assert: TPerson.kinship, illegal state, interpreter is null!";
        assert getValue() != null : "Assert: TPerson.kinship, this.value is null!";
        assert kinsman.getValue() != null : "Assert: TPerson.kinship, kinsman.value is null!";
        final @NotNull String kinTerms = in.ontology().tree().kinship(getValue(), kinsman.getValue());
        return new TString( kinTerms, false );
    }

    public @NotNull TObject<?> father()
    {
        assert getValue() != null : "Assert: TPerson.father, this.value is null!";
        final @Nullable Vertex father = getValue().getFather();
        //
        return ( father == null )? TVoid.instance : new TPerson(father);
    }

    public @NotNull TList children()
    {
        assert getValue() != null : "Assert: TPerson.children, this.value is null!";
        final @Nullable List<Vertex> children = getValue().children();
        return new TList( children.stream().map(TPerson::new).collect(Collectors.toList()) );
    }

    public @NotNull TObject<?> spouse()
    {
        assert getValue() != null : "Assert: TPerson.spouse, this.value is null!";
        final @Nullable Vertex spouse = getValue().getSpouse();
        //
        return ( spouse == null )? TVoid.instance : new TPerson(spouse);
    }

    /*
        Returns mother (TPerson) of this person or TVoid if there is none.
     */
    public @NotNull TObject<?> mother()
    {
        assert getValue() != null : "Assert: TPerson.mother, this.value is null!";
        final @Nullable Vertex mother = getValue().getMother();
        //
        return ( mother == null )? TVoid.instance : new TPerson( mother );
    }

    public static @NotNull TList people()
    {
        assert in != null : "Assert: TPerson.people(), illegal state, in is null!";
        final @NotNull List<Vertex> vertices = in.ontology().tree().vertices();
        final @NotNull List<TObject<?>> people = vertices.stream().map( TPerson::new ).collect(Collectors.toList());
        return new TList( people );
    }

    private @NotNull TObject<?> getAttribute(final TString attrName) throws InvalidTermException
    {
        assert attrName.getValue() != null : "Assert: TPerson.getAttribute, attrName.value is null!";
        assert getValue() != null : "Assert: TPerson.getAttribute, super.value is null!";
        //
        final String lowercase = attrName.getValue().toLowerCase();
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
                result = new TString( v.profile().getSex(), false );
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

    private static TObject<?> getPerson(final @NotNull Ontology model, final String firstName, final String lastName)
    {
        // Search for a getPerson with provided name
        final @Nullable Vertex requestedVertex = model.tree().findVertex(firstName, lastName);
        if (requestedVertex == null)
        {
            return TVoid.instance;
        }
        else
        {
            return new TPerson(requestedVertex);
        }
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
    }



    private static class KinshipFunction extends TFunction<TPerson, TString>
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
        TString call(final @NotNull List<TPerson> args)
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
            return argsCount == 2;
        }

        @NotNull
        @Override
        TObject<?> call(final @NotNull List<TString> args)
        {
            // args.size() == 2
            final @NotNull TString firstName = args.get(0);
            final @NotNull TString lastName = args.get(1);
            assert firstName.getValue() != null : "Assert: GetPersonFunction.call, firstName.value is null";
            assert lastName.getValue() != null : "Assert: GetPersonFunction.call, lastName.value is null";
            assert in != null : "Assert: GetPersonFunction.call, interpreter is null!";
            final @NotNull Ontology ontology = in.ontology();
            return TPerson.getPerson(ontology, firstName.getValue(), lastName.getValue());
        }

        @Override
        String mismatchMessage()
        {
            return null;
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
        TObject<?> call(@NotNull List<TObject<?>> args) throws InvalidTermException
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
                    // TODO: Решить что делать с void элементами
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
                    // TODO: Решить что делать с void элементами
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
                    // TODO: Решить что делать с void элементами
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
                    // TODO: Решить что делать с void элементами
                    // TODO: Пока что я их просто пропускаю
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
}