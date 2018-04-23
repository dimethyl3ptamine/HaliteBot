package dmt;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExploringStrategy implements Strategy {

    // TODO : REFACTORING :D
    // TODO : support weights

    private static final String NAME = "Explore the nearest planets";

    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public void calculateMovements(GameMap gameMap, ArrayList<Move> moveList) throws StrategyException {
        Collection<Ship> ships = Utils.getUndockedShips(gameMap);

        for (Ship ship : ships) {
            Utils.log("Processing ship: " + ship);

            List<Planet> nearestPlanets = Utils.getPlanetsSortedByDistance(gameMap, ship);

            boolean isSentToNearest = isSentToNearest(moveList, ship, nearestPlanets);
            if (isSentToNearest) {
                continue;
            }
            Utils.log("isSentToNearest failed");

            boolean isSentToClosestFree = isSentToClosestFree(gameMap, moveList, ship, nearestPlanets);
            if (isSentToClosestFree) {
                continue;
            }
            Utils.log("isSentToClosestFree failed");

            List<Ship> enemyShip = Utils.getSortedShipsByDistance(gameMap, ship, true);

            boolean isSentToAttack = Utils.isShipSentToAttack(gameMap, moveList, ship, enemyShip, true);
            if (isSentToAttack) {
                continue;
            }
            Utils.log("isSentToAttack1 failed");

            isSentToAttack = Utils.isShipSentToAttack(gameMap, moveList, ship, enemyShip, false);
            if (isSentToAttack) {
                continue;
            }
            Utils.log("isSentToAttack2 failed");
        }
    }

    private boolean isSentToNearest(ArrayList<Move> moveList, Ship ship, List<Planet> nearestPlanets) {
        for (Planet planet : nearestPlanets) {
            if (Utils.doesPlanetHaveDockingSpots(planet)) {
                if (ship.canDock(planet)) {
                    moveList.add(new DockMove(ship, planet));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSentToClosestFree(GameMap gameMap, ArrayList<Move> moveList, Ship ship, List<Planet> nearestPlanets) {
        for (Planet planet : nearestPlanets) {
            if (!planet.isOwned()) {
                if (ship.canDock(planet)) {
                    moveList.add(new DockMove(ship, planet));
                    return true;
                } else {
                    ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
