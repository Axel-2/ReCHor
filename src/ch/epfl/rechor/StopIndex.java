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

    private static final Map<Character, String> MAP_EQUIVALENCES;
    private final Map<String, String> alternateNamesMap;

    public static final int BEGIN_OR_SPACE_MULTIPLIER = 4;
    public static final int END_OR_SPACE_MULTIPLIER = 2;

    private final List<String> stopsList;

    // Bloc statique pour initialiser le map
    static {
        Map<Character, String> m = new TreeMap<>();
        m.put('a', "[aáàâä]");
        m.put('c', "[cç]");
        m.put('e', "[eéèêë]");
        m.put('i', "[iíìîï]");
        m.put('o', "[oóòôö]");
        m.put('u', "[uúùûü]");
        MAP_EQUIVALENCES = Collections.unmodifiableMap(m);
    }

    /**
     * Crée un index de recherche sur la liste d'arrêts donnée
     * et leurs noms alternatifs.
     * @param stopsList la liste des noms d'arrêts à indexer
     * @param alternateNamesMap map des noms alternatifs vers noms principaux
     */
    public StopIndex(List<String> stopsList, Map<String, String> alternateNamesMap) {

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
        String[] originalSubQueries = rqt.split(" ");
        
        // transformation des subQueries en liste de "pattern" RegEx
        List<Pattern> subQueriesWithPattern = Arrays.stream(originalSubQueries)
                .filter(subQuery -> !subQuery.isEmpty())
                .map(subQuery -> {
                    // Gestion du "case sensitive" pour chaque sous-requête
                    int flags;
                    boolean containsCapitalLetter = !subQuery.equals(subQuery.toLowerCase());
                    if (containsCapitalLetter) {flags = Pattern.UNICODE_CASE;}
                    else {flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;}
                    String subQueryRE = subQuery.chars()
                            .mapToObj(this::transformCharToRE).collect(Collectors.joining());
                    return Pattern.compile(subQueryRE, flags);

                })
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
                        subQueriesWithPattern.stream().allMatch(subQueryPattern ->
                                subQueryPattern.matcher(entry.getKey()).find())
                )
                .map(Map.Entry::getKey);

        return Stream.concat(stopsMatching, alternatesMatching)
                // on enlève les doublons
                .distinct()
                // on trie avec la méthode définie ci-dessus
                .sorted((stopName1, stopName2) -> Integer.compare(
                        score(stopName2, subQueriesWithPattern),
                        score(stopName1, subQueriesWithPattern)))
                // on remplace les alternates par le vrai nom
                .map(name -> alternateNamesMap.getOrDefault(name, name))
                // on enlève encore les doublons
                .distinct()
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
            if (matcher.find()) {
                int subScore = 0;
                int multiplier = 1;

                // 1) subScore += sub.length() / stop.length()
                subScore += (int) Math.floor(100 *((double)(matcher.end() - matcher.start()) / stopName.length()));

                // 2) Si début ou espace avant : multiplier * 4
                if (matcher.start() == 0 || !Character.isLetter(stopName.charAt(matcher.start()-1))) {
                    multiplier *= BEGIN_OR_SPACE_MULTIPLIER;
                }

                // 3) Si fin ou espace après : multiplier * 2
                if (matcher.end() == stopName.length() || !Character.isLetter(stopName.charAt(matcher.end()))) {
                    multiplier *= END_OR_SPACE_MULTIPLIER;
                }

                finalScore += subScore * multiplier;
            }
        }

        return finalScore;
    }

    /**
     * Transforme un caractère en sa représentation RE
     * @param c le char c
     */
    private String transformCharToRE(int c) {
        char ch = (char) c;
        if (MAP_EQUIVALENCES.containsKey(ch)) {
            return MAP_EQUIVALENCES.get(ch);
        } else {
            return Pattern.quote(String.valueOf(ch));
        }
    }


}
