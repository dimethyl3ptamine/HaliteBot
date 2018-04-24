package dmt;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MoveAndKillStrategy implements Strategy {

    private static final String NAME = "Move to planet peacefully (and kill everyone furiously)";

    private static final int ENEMIES_DOCKED_SHIPS_THRESHOLD = 13;

    private final double RADIUS_THRESHOLD;

    MoveAndKillStrategy() {
        RADIUS_THRESHOLD = getState().getMap().getHeight() / 8.0d;
   }

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
                tryToKillAnyEnemy(moveList, ship);
            }

            // TODO : What else we can do here?
        }
    }

    private void tryToKillAnyEnemy(ArrayList<Move> moveList, Ship ship) {
        List<Ship> enemyShips = Utils.getShipsSortedByDistance(ship, true);

        if (!Utils.isShipSentToAttackDockedEnemies(moveList, ship, enemyShips, ENEMIES_DOCKED_SHIPS_THRESHOLD)) {
            Utils.isShipSentToAttackEnemies(moveList, ship, enemyShips, true);
        }
    }

    private boolean tryToNavigateToAnyPlanet(ArrayList<Move> moveList, Ship ship) {
        List<Planet> nearestPlanets = Utils.getPlanetsSortedByDistance(ship, RADIUS_THRESHOLD);

        if (Utils.isShipSentToNearestPlanet(moveList, ship, nearestPlanets)) {
            return true;
        }

        return Utils.isShipSentToNearestFreePlanet(moveList, ship,
                nearestPlanets, getState().getFreePlanets().size());
    }

}
