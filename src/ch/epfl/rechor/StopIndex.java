package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Classe qui représente un index de nom d'arrêts dans lequel
 *  il est possible d'effectuer des recherches
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class StopIndex {
    // TODO vérifier immuabilité à la fin

    public static Map<Character, String> mapEquivalences = new TreeMap<>();
    public Map<String, String> alternateNamesMap = new TreeMap<>();

    private final List<String> stopsList;




    public StopIndex(List<String> stopsList, Map<String, String> alternateNamesMap) {

        // remplissage du tableau
        mapEquivalences.put('a', "[aáàâä]");
        mapEquivalences.put('c', "[cç]");
        mapEquivalences.put('e', "[eéèêë]");
        mapEquivalences.put('i', "[iíìîï]");
        mapEquivalences.put('o', "[oóòôö]");
        mapEquivalences.put('u', "[uúùûü]");

        this.stopsList = List.copyOf(stopsList);
        this.alternateNamesMap = Map.copyOf(alternateNamesMap);

    }

    public List<String> stopsMatching(String rqt, int maxNumbersOfStopsToReturn) {

        // --- étape 1 : découper en subqueries------

        String[] originalSubQueries = rqt.split(" ");
        int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

        // transformation des subQueries en liste de pattern RegEx
        List<Pattern> subQueriesWithPattern = Arrays.stream(originalSubQueries)
                .map(subQuery -> subQuery.chars()
                    .mapToObj(this::transformCharToRE).collect(Collectors.joining()))
                .map(subQueryRe -> Pattern.compile(subQueryRe, flags))
                .toList();

        // Filtrer et récupérer les noms dans la liste stopsList
        Stream<String> stopsMatching = stopsList.stream()
                .filter(stopName ->
                        subQueriesWithPattern.stream().allMatch(subQueryPattern ->
                                subQueryPattern.matcher(stopName).find()
                        )
                );

        // Filtrer la Map et récupérer les valeurs associées pour lesquelles la clé correspond
        Stream<String> alternatesMatching = alternateNamesMap.entrySet().stream()
                .filter(entry ->
                        subQueriesWithPattern.stream().anyMatch(pattern ->
                                pattern.matcher(entry.getKey()).find()
                        )
                )
                .map(Map.Entry::getValue);

        List<String> resultList = Stream.concat(stopsMatching, alternatesMatching)
                .collect(Collectors.toList());

        // ---- étape finale : trier la liste ------

        return resultList;
    }

    /**
     * Transforme un caractère en sa représentation RE
     */
    private String transformCharToRE(int c) {
        char ch = (char) c;
        if (mapEquivalences.containsKey(ch)) {
            return mapEquivalences.get(ch);
        } else {
            return Pattern.quote(String.valueOf(ch));
        }
    }


}
