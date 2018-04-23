package dmt;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MoveOrKillStrategy implements Strategy {

    private static final String NAME = "Move to planet peacefully (or kill everyone furiously)";

    private static final int ENEMIES_DOCKED_SHIPS_THRESHOLD = 13;

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
        }
    }

    private void tryToKillAnyEnemy(ArrayList<Move> moveList, Ship ship) {
        List<Ship> enemyShips = Utils.getShipsSortedByDistance(ship, true);

        if (!Utils.isShipSentToAttackDockedEnemies(moveList, ship, enemyShips, ENEMIES_DOCKED_SHIPS_THRESHOLD)) {
            if (!Utils.isShipSentToAttackEnemies(moveList, ship, enemyShips, true)) {
                Utils.isShipSentToAttackEnemies(moveList, ship, enemyShips, false);
            }
        }
    }

    private boolean tryToNavigateToAnyPlanet(ArrayList<Move> moveList, Ship ship) {
        List<Planet> nearestPlanets = Utils.getPlanetsSortedByDistance(ship);

        if (Utils.isShipSentToNearestPlanet(moveList, ship, nearestPlanets)) {
            return true;
        }

        return Utils.isShipSentToNearestFreePlanet(moveList, ship,
                nearestPlanets, getState().getFreePlanets().size());
    }

}
