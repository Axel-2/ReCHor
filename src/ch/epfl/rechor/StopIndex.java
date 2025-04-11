package ch.epfl.rechor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *  Classe qui représente un index de nom d'arrêts dans lequel
 *  il est possible d'effectuer des recherches
 * @author Yoann Salamin (390522)
 * @author Axel Verga (398787)
 */
public final class StopIndex {
    // TODO vérifier immuabilité à la fin

    public static Map<Character, String> mapEquivalences = new TreeMap<>();
    public final Map<String, String> alternateNamesMap;
    private final List<String> stopsList;

    public StopIndex(List<String> stopsList, Map<String, String> alternateNamesMap) {

        // remplissage du tableau
        mapEquivalences.put('a', "[aáàâä]");
        mapEquivalences.put('c', "[cç]");
        mapEquivalences.put('e', "[eéèêë]");
        mapEquivalences.put('i', "[iíìîï]");
        mapEquivalences.put('o', "[oóòôö]");
        mapEquivalences.put('u', "[uúùûü]");

        this.stopsList = stopsList;
        this.alternateNamesMap = alternateNamesMap;

    }

    public List<String> stopsMatching(String rqt, int maxNumbersOfStopsToReturn) {

        // --- étape 1 : découper en subqueries------

        String[] originalSubQueries = rqt.split(" ");

        // transformation des subQueries en subQueries RE
        List<String> subQueriesWithRE = Arrays.stream(originalSubQueries)
                .map(subQuery -> subQuery.chars()
                    .mapToObj(this::transformCharToRE).collect(Collectors.joining()))
                .toList();

        // itérer sur les subQueries et trouver les arrêts qui correspondent
        for (String subQuery: subQueriesWithRE) {

            // TODO vérifier que les flags soient corrects parce que je crois que c'est juste
            // un exemple dans la doc
            int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            Pattern pattern = Pattern.compile(subQuery);

            for (String stopName : stopsList) {
                if (pattern.matcher(stopName).find()) {
                    int start = pattern.matcher(stopName).start();
                    int end = pattern.matcher(stopName).end();
                }

        }

        // ---- étape finale : trier la liste ------


    return null;

    }

    /**
     * Transforme un caractère en sa représentation RE
     */
    private String transformCharToRE(int c) {
        char ch = (char) c;
        if (mapEquivalences.containsKey(ch)) {
            return Pattern.quote(mapEquivalences.get(ch));
        } else {
            return Pattern.quote(String.valueOf(ch));
        }
    }


}
