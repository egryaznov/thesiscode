package foundation.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/*
    Format:
    json: {
        "term,term,...,term" : "kinship",
        "term,term,...,term" : "kinship",
        ...
        "term,term,...,term" : "kinship"
        }
 */
public class KinshipDictionary
{
    private static final @NotNull String DELIMITER = ",";
    private static final @NotNull String JSON_FILE_NAME = "res/kinship-dict.json";
    private final @NotNull JSONObject jsonDict;
    private final @NotNull Map<List<String>, String> kinDict = new HashMap<>();
    public static final @NotNull KinshipDictionary instance = new KinshipDictionary();

    private KinshipDictionary()
    {
        final @Nullable InputStream jarIn = getClass().getResourceAsStream("/" + JSON_FILE_NAME);
        final @NotNull File jsonFile = new File(JSON_FILE_NAME);
        if ( jarIn == null )
        {
            System.out.println("Cannot find kinship dict in jar, trying to find it in: " + JSON_FILE_NAME);
            if ( !jsonFile.exists() )
            {
                System.out.println("There is no .json either! Kinship dictionary will be empty!");
                this.jsonDict = new JSONObject();
            }
            else
            {
                System.out.println("Found .json file, proceeding to load");
                this.jsonDict = loadJSONFromFile(jsonFile);
            }
        }
        else
        {
            System.out.println("Successfully loaded .json resource from jar");
            this.jsonDict = loadJSONFromJar(jarIn);
        }
        //
        initDictFromJSON(this.jsonDict);
    }



    private @NotNull JSONObject loadJSONFromJar(final @NotNull InputStream jarInStream)
    {
        @NotNull JSONObject result;
        try (final @NotNull BufferedReader br = new BufferedReader(new InputStreamReader(jarInStream)))
        {
            final @NotNull JSONParser parser = new JSONParser();
            result = (JSONObject)parser.parse(br);
        }
        catch (final IOException | ParseException e)
        {
            result = new JSONObject();
            e.printStackTrace();
        }
        //
        return result;
    }

    private @NotNull JSONObject loadJSONFromFile(final @NotNull File jsonFile)
    {
        @NotNull JSONObject result;
        try (final @NotNull BufferedReader br = new BufferedReader(new FileReader(jsonFile)))
        {
            final @NotNull JSONParser parser = new JSONParser();
            result = (JSONObject)parser.parse(br);
        }
        catch (final IOException | ParseException e)
        {
            result = new JSONObject();
            e.printStackTrace();
        }
        //
        return result;
    }

    private void initDictFromJSON(final @NotNull JSONObject json)
    {
        for (final Object entry : json.entrySet())
        {
            final @NotNull Entry next = (Entry)entry;
            putKinship((String)next.getKey(), (String)next.getValue(), true);
        }
    }

    public @NotNull List<String> shorten(final @NotNull List<String> kinship)
    {
        if ( kinship.isEmpty() )
        {
            return kinship;
        }
        // Initialize maximum shortcut interval and its' length
        final int kinLen = kinship.size();
        final int[] maxShortcut = new int[]{0, 0};
        int currMaxShortcutLen = 1;
        // Try all possible sub-lists to shorten the main kinship list
        for (int i = 0; i < kinLen; i++)
        {
            // We skip the last item at i, because it's useless to try to shorten one basic term
            for (int j = kinLen - 1; j > i; j--)
            {
                final @NotNull List<String> candidate = kinship.subList(i, j + 1); // from i to j
                if ( kinDict.containsKey(candidate) && currMaxShortcutLen < (j - i + 1) )
                {
                    maxShortcut[0] = i;
                    maxShortcut[1] = j;
                    currMaxShortcutLen = j - i + 1;
                }
            }
        }
        // Construct result
        final @NotNull List<String> result;
        if ( maxShortcut[0] == 0 && maxShortcut[1] == 0 )
        {
            // We were unable to shorten the `kinship`, so just return it untouched
            result = kinship;
        }
        else
        {
            // We successfully shortened `kinship`. Time to return it
            result = new LinkedList<>();
            // Extract and shorten the left part
            final @NotNull List<String> leftPart = shorten( kinship.subList(0, maxShortcut[0]) );
            result.addAll(leftPart);
            // Shorten the candidate
            final @NotNull String shortcut = kinDict.get( kinship.subList(maxShortcut[0], maxShortcut[1] + 1) );
            result.add(shortcut);
            // Extract and shorten the right part
            final @NotNull List<String> rightPart = shorten( kinship.subList(maxShortcut[1] + 1, kinLen) );
            result.addAll(rightPart);
        }
        return result;
    }

    public void putKinship(final @NotNull String kinship, final @NotNull String value)
    {
        putKinship(kinship, value, false);
    }

    @SuppressWarnings("unchecked")
    private void putKinship(final @NotNull String kinship, final @NotNull String value, final boolean isLoadingFromDisk)
    {
        if (!isLoadingFromDisk)
        {
            if (jsonDict.containsKey(kinship))
            {
                return;
            }
            jsonDict.put(kinship, value);
        }
        //
        @NotNull List<List<String>> keys = new LinkedList<>();
        keys.add(new LinkedList<>());
        //
        for (final String term : kinship.split(DELIMITER))
        {
            switch (term)
            {
                case "parent" :
                    concatTwoTerms(keys, "mother", "father");
                    break;
                case "child" :
                    concatTwoTerms(keys, "son", "daughter");
                    break;
                case "spouse" :
                    concatTwoTerms(keys, "husband", "wife");
                    break;
                default :
                    // NOTE: Cannot replace this with "foreach" because we MODIFY the collection "keys" here
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < keys.size(); i++)
                    {
                        keys.get(i).add(term);
                    }
                    break;
            }
        }
        // Insert all (key, value) pairs
        keys.forEach( key -> kinDict.put(key, value) );
    }

    public void saveToFile()
    {
        final @NotNull File dict = new File(JSON_FILE_NAME);
        try (final @NotNull BufferedWriter out = new BufferedWriter(new FileWriter( dict )))
        {
            jsonDict.writeJSONString(out);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private void concatTwoTerms(final @NotNull List<List<String>> keys, final @NotNull String first, final @NotNull String second)
    {
        final @NotNull List<List<String>> keysSecondAdded = new LinkedList<>();
        for (final List<String> list : keys)
        {
            final @NotNull List<String> copy = new ArrayList<>(list);
            copy.add(second);
            keysSecondAdded.add( copy );
        }
        //
        keys.forEach( key -> key.add(first) );
        keys.addAll(keysSecondAdded);
    }

    @SuppressWarnings("unused")
    private @NotNull List<String> stringKeyToList(final @NotNull String key)
    {
        return Arrays.asList(key.split(DELIMITER));
    }

    @SuppressWarnings("unused")
    private @NotNull String listKeyToString(final @NotNull List<String> key)
    {
        return key.stream().collect(Collectors.joining(DELIMITER));
    }

}
