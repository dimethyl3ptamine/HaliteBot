package dmt;

import hlt.GameMap;
import hlt.Log;
import hlt.Planet;
import hlt.Ship;

import java.util.*;

public class Utils {

    private static final boolean LOGGING = false;

    /**
     * Returns the list of Planets from biggest to smallest
     */
    static List<Planet> getSortedPlanetsByRadius(Collection<Planet> allPlanets) {
        ArrayList<Planet> planets = new ArrayList<>(allPlanets);
        planets.sort((o1, o2) -> Double.compare(o2.getRadius(), o1.getRadius()));
        return planets;
    }

    /**
     * Returns the list of planets from closest to farthest for this particular Ship
     */
    static List<Planet> getSortedPlanetsByDistance(GameMap gameMap, Ship ship) {
        Map<Planet, Double> distanceToPlanet = new HashMap<>();

        for (Planet planet : gameMap.getAllPlanets().values()) {
            distanceToPlanet.put(planet, ship.getDistanceTo(planet));
        }

        List<Map.Entry<Planet, Double>> sortedPlanets = new ArrayList<>(distanceToPlanet.entrySet());
        sortedPlanets.sort(Comparator.comparingDouble(Map.Entry::getValue));

        List<Planet> result = new ArrayList<>();

        for (Map.Entry<Planet, Double> entry : sortedPlanets) {
            result.add(entry.getKey());
        }

        return result;
    }

    /**
     * Log message. It simply calls "Log.log(...)"
     *
     * @param isError - true if message should be started with "ERROR : "
     */
    public static void log(String message, boolean isError) {
        if (LOGGING) {
            if (isError) {
                message = "\n\n\n\n\nERROR : " + message + "\n\n\n\n\n";
            }

            Log.log(message);
        }
    }

    /**
     * Log message. It simply calls "Log.log(...)"
     */
    public static void log(String message) {
        log(message, false);
    }

}
