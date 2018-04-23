package dmt;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static dmt.Utils.getSortedPlanetsByRadius;

public class Game {

    public static void calculateMovements(GameMap gameMap, ArrayList<Move> moveList) {
        Collection<Ship> ships = gameMap.getMyPlayer().getShips().values();
        Collection<Planet> planets = gameMap.getAllPlanets().values();
        ArrayList<Planet> usedPlanets = new ArrayList<>();

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
            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

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

    private static boolean isSentToNearest(ArrayList<Move> moveList, ArrayList<Planet> usedPlanets, Ship ship, List<Planet> nearestPlanets) {
        boolean sentToNearest = false;

        for (Planet planet : nearestPlanets) {
            if (planet.isOwned()) {
                continue;
            }

            if (ship.canDock(planet) && !isPlanetUsed(planet, usedPlanets)) {
                moveList.add(new DockMove(ship, planet));
                usedPlanets.add(planet);
                sentToNearest = true;
                break;
            }
        }

        return sentToNearest;
    }


    private static boolean isPlanetUsed(Planet planet, ArrayList<Planet> usedPlanets) {
        for (Planet p : usedPlanets) {
            if (p.getId() == planet.getId()) {
                return true;
            }
        }

        return false;
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
