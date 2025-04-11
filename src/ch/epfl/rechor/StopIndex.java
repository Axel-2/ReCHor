package ch.epfl.rechor;

import java.util.*;
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

        List<String> resultList = new ArrayList<>();

        // --- étape 1 : découper en subqueries------

        String[] originalSubQueries = rqt.split(" ");

        // transformation des subQueries en subQueries RE
        List<String> subQueriesWithRE = Arrays.stream(originalSubQueries)
                .map(subQuery -> subQuery.chars()
                    .mapToObj(this::transformCharToRE).collect(Collectors.joining()))
                .toList();

        // itérer sur les noms de gares normaux
        for (String stopName : stopsList) {
            // TODO vérifier que les flags soient corrects parce que je crois que c'est juste
            // un exemple dans la doc

           for (String subQuery: subQueriesWithRE) {

               int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
               Pattern pattern = Pattern.compile(subQuery);
                if (pattern.matcher(stopName).find()) {
                    int start = pattern.matcher(stopName).start();
                    int end = pattern.matcher(stopName).end();

                    resultList.add(stopName);
                }
            }
        }

        // itérer sur les noms alternatifs
        for (Map.Entry<String, String> entry: alternateNamesMap.entrySet()) {
            for (String subQuery: subQueriesWithRE) {
                int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
                Pattern pattern = Pattern.compile(subQuery);
                if (pattern.matcher(entry.getKey()).find()) {
                    int start = pattern.matcher(entry.getKey()).start();
                    int end = pattern.matcher(entry.getKey()).end();

                    resultList.add(entry.getValue());
                }
            }
        }

        // ---- étape finale : trier la liste ------

        return resultList;
    }

    /**
     * Calcule le score de compatibilité entre une query et un stop Name
     * @param stopName nom d'arrêt, nom de requête
     * @return score de compatibilité (int)
     */
    private int score(String stopName, Pattern subqueryRe){
        int score  = 0;

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
