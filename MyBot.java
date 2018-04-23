import hlt.*;

import java.util.*;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("_Nag1bAt0r2OO5_");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            doCalculations(gameMap, moveList);
            Networking.sendMoves(moveList);
        }
    }

    private static void doCalculations(GameMap gameMap, ArrayList<Move> moveList) {
        Collection<Ship> ships = gameMap.getMyPlayer().getShips().values();
        Collection<Planet> planets = gameMap.getAllPlanets().values();
        ArrayList<Planet> usedPlanets = new ArrayList<>();

        for (final Ship ship : ships) {
            if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                continue;
            }

            boolean sentToNearest = false;
            List<Planet> nearestPlanets = getSortedPlanetsByDistance(gameMap, ship);

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

            if (sentToNearest) {
                continue;
            }

            boolean sentToLargest = false;
            List<Planet> largestPlanets = getSortedPlanetsByRadius(planets);

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

            if (sentToLargest) {
                continue;
            }

            Planet planet = getNearestFreePlanet(nearestPlanets);

            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

            if (newThrustMove != null) {
                moveList.add(newThrustMove);
            }
        }
    }

    private static boolean isPlanetUsed(Planet planet, ArrayList<Planet> usedPlanets) {
        for (Planet p : usedPlanets) {
            if (p.equals(planet)) {
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

    // Get the biggest planet!
    private static List<Planet> getSortedPlanetsByRadius(Collection<Planet> allPlanets) {
        ArrayList<Planet> planets = new ArrayList<>(allPlanets);
        planets.sort((o1, o2) -> Double.compare(o2.getRadius(), o1.getRadius()));
        return planets;
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
