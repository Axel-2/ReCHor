package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
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

    private static final Map<Character, String> mapEquivalences = new TreeMap<>();
    private final Map<String, String> alternateNamesMap;

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

    /**
     * Retourne la liste des arrêts correspondants à la requête
     * @param rqt requête émise par l'utilisateur
     * @param maxNumbersOfStopsToReturn nombre maximal de propositions affichées
     * @return list des noms d'arrêts correspondant à la requête
     */
    public List<String> stopsMatching(String rqt, int maxNumbersOfStopsToReturn) {

        Preconditions.checkArgument(maxNumbersOfStopsToReturn > 0);

        // --- étape 1 : découper en subqueries------

        // TODO case vérifier
        // flags par défaut
        final int flags;
        String[] originalSubQueries = rqt.split(" ");
        if (rqt.toLowerCase().equals(rqt)) {
            flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        } else {
            flags = Pattern.UNICODE_CASE;
        }

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
                                subQueryPattern.matcher(stopName).find())
                );

        // Filtrer la Map et récupérer les valeurs associées pour lesquelles la clé correspond
        Stream<String> alternatesMatching = alternateNamesMap.entrySet().stream()
                .filter(entry ->
                        subQueriesWithPattern.stream().anyMatch(subQueryPattern ->
                                subQueryPattern.matcher(entry.getKey()).find())
                )
                .map(Map.Entry::getValue);

        return Stream.concat(stopsMatching, alternatesMatching)
                // on enlève les doublons
                .distinct()
                // on trie avec la méthode définie ci-dessus
                .sorted((stopName1, stopName2) -> Integer.compare(
                        score(stopName2, subQueriesWithPattern),
                        score(stopName1, subQueriesWithPattern)))
                .limit(maxNumbersOfStopsToReturn)
                .collect(Collectors.toList());
    }

    /**
     * Calcule le score de compatibilité entre une query et un stop Name
     * @param stopName nom d'arrêt, nom de requête
     * @return score de compatibilité (int)
     */
    private int score(String stopName, List<Pattern> subQueries) {

        Preconditions.checkArgument(!stopName.isEmpty());

        int finalScore = 0;

        for (Pattern subQueryRE : subQueries) {

            Matcher matcher = subQueryRE.matcher(stopName);

            // On ne teste que la première occurrence
            // On est sûr qu'elle existe car la liste contient uniquement des matchs
            matcher.find();

            int subScore = 0;
            int multiplier = 1;

            // 1) subScore += sub.length() / stop.length()
            subScore += (int) Math.floor(100 *((double)(matcher.end() - matcher.start()) / stopName.length()));

            // 2) Si début ou espace avant: multiplier * 4
            if (matcher.start() == 0 || !Character.isLetter(stopName.charAt(matcher.start()-1))) {
                multiplier *= 4;
            }


            // 3) Si fin ou espace après : multiplier * 2
            if (matcher.end() == stopName.length() || !Character.isLetter(stopName.charAt(matcher.end()))) {
                multiplier *= 2;
            }

            finalScore += subScore * multiplier;

        }

        return finalScore;
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
