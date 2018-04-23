package dmt;

import hlt.*;

import java.util.*;

import static dmt.Utils.getSortedPlanetsByRadius;

public class Game {

    public static void calculateMovements(GameMap gameMap, ArrayList<Move> moveList) {
        Collection<Ship> ships = gameMap.getMyPlayer().getShips().values();
        Collection<Planet> planets = gameMap.getAllPlanets().values();
        ArrayList<Integer> usedPlanets = new ArrayList<>();

        for (final Ship ship : ships) {
            if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                continue;
            }

            List<Planet> nearestPlanets = getSortedPlanetsByDistance(gameMap, ship);
            if (isSentToNearest(moveList, usedPlanets, ship, nearestPlanets)) {
                continue;
            }

            List<Planet> largestPlanets = getSortedPlanetsByRadius(planets);
            if (isSentToLargest(gameMap, moveList, ship, largestPlanets)) {
                continue;
            }

            Planet nearestFreePlanet = getNearestFreePlanet(nearestPlanets);
            if (isSentToNearestFree(moveList, ship, nearestFreePlanet)) {
                continue;
            }

            // No more free planets? Let's attack the closest one!
            Planet planet = getNearestEnemyPlanet(gameMap, nearestPlanets);
//            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
            final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship, new Position(planet.getXPos(), planet.getYPos()), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);

            if (newThrustMove != null) {
                moveList.add(newThrustMove);
            }
        }
    }

    private static boolean isSentToNearestFree(ArrayList<Move> moveList, Ship ship, Planet planet) {
        if (ship.canDock(planet)) {
            moveList.add(new DockMove(ship, planet));
            return true;
        }

        return false;
    }

    private static boolean isSentToLargest(GameMap gameMap, ArrayList<Move> moveList, Ship ship, List<Planet> largestPlanets) {
        boolean sentToLargest = false;

        for (Planet planet : largestPlanets) {
            if (planet.isOwned() && planet.getOwner() == gameMap.getMyPlayer().getId() && planet.isFull()) {
                continue;
            }

            if (ship.canDock(planet)) {
                moveList.add(new DockMove(ship, planet));
                sentToLargest = true;
                break;
            }
        }
        return sentToLargest;
    }

    private static boolean isSentToNearest(ArrayList<Move> moveList, ArrayList<Integer> usedPlanets, Ship ship, List<Planet> nearestPlanets) {
        for (Planet planet : nearestPlanets) {
            if (planet.isOwned()) {
                continue;
            }

            if (ship.canDock(planet) && !isPlanetUsed(planet, usedPlanets)) {
                moveList.add(new DockMove(ship, planet));
                usedPlanets.add(planet.getId());
                return true;
            }
        }

        return false;
    }


    private static boolean isPlanetUsed(Planet planet, ArrayList<Integer> usedPlanets) {
        return usedPlanets.contains(planet.getId());
    }

    private static Planet getNearestFreePlanet(List<Planet> nearestPlanets) {
        for (Planet planet : nearestPlanets) {
            if (!planet.isOwned()) {
                return planet;
            }
        }

        return nearestPlanets.get(0);
    }

    private static Planet getNearestEnemyPlanet(GameMap gameMap, List<Planet> nearestPlanets) {
        for (Planet planet : nearestPlanets) {
            if (planet.getOwner() != gameMap.getMyPlayer().getId()) {
                return planet;
            }
        }

        return nearestPlanets.get(0);
    }

    private static List<Planet> getSortedPlanetsByDistance(GameMap gameMap, Ship ship) {
        List<Planet> result = new ArrayList<>();

        Map<Double, Entity> closestEntities = gameMap.nearbyEntitiesByDistance(ship);

        for (Entity entity : closestEntities.values()) {
            if (entity instanceof Planet) {
                result.add((Planet) entity);
            }
        }

        return result;
    }
}
