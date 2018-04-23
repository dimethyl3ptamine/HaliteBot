package dmt;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KamikadzeStrategy implements Strategy {

    private static final String NAME = "No sense to live without our bravest and greatest Emperor!";

    private static final double CLOSEST_PLANET_RADIUS = 1.0d;

    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public void calculateMovements(ArrayList<Move> moveList) {
        Collection<Ship> ships = getState().getMyUndockedShips();

        for (Ship ship : ships) {
            Utils.log("Processing ship: " + ship);

            if (!tryToNavigateToAnyPlanet(moveList, ship)) {
                tryToKillEnemiesPlanet(moveList, ship);
            }
        }
    }

    private boolean tryToNavigateToAnyPlanet(ArrayList<Move> moveList, Ship ship) {
        List<Planet> nearestPlanets = Utils.getPlanetsSortedByDistance(ship, CLOSEST_PLANET_RADIUS);
        return Utils.isShipSentToNearestPlanet(moveList, ship, nearestPlanets);
    }

    private void tryToKillEnemiesPlanet(ArrayList<Move> moveList, Ship ship) {
        List<Planet> nearestPlanets = Utils.getPlanetsSortedByDistance(ship, Utils.UNLIMITED_RADIUS);

        for (Planet planet : nearestPlanets) {
            if (planet.isOwned() && planet.getOwner() != getState().getMyId()) {
                ThrustMove thrustMove = Utils.navigateShipToPosition(ship, planet, false);
                moveList.add(thrustMove);
                break;
            }
        }
    }

}
