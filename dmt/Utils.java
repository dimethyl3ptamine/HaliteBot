package dmt;

import hlt.Planet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Utils {

    // Returns from biggest to smallestd
    static List<Planet> getSortedPlanetsByRadius(Collection<Planet> allPlanets) {
        ArrayList<Planet> planets = new ArrayList<>(allPlanets);
        planets.sort((o1, o2) -> Double.compare(o2.getRadius(), o1.getRadius()));
        return planets;
    }



    /* TODO: maybe useful
            // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

     * */

}
