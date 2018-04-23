package dmt;

import hlt.*;

import java.util.*;

public class Utils {

    // TODO : always fix logging before commit. Just in case :D
    private static final boolean LOGGING = true;

    // TODO : better to move getFreePlanets() and similar to State.java

    /**
     * Returns the list of Planets from biggest to smallest
     */
    static List<Planet> getPlanetsSortedByRadius(Collection<Planet> allPlanets) {
        ArrayList<Planet> planets = new ArrayList<>(allPlanets);
        planets.sort((o1, o2) -> Double.compare(o2.getRadius(), o1.getRadius()));
        return planets;
    }

    /**
     * Returns the list of planets from closest to farthest for this particular Ship
     */
    static List<Planet> getPlanetsSortedByDistance(GameMap gameMap, Ship ship) {
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
     * Returns the list of planets from closest to farthest for this particular Ship, considering planets' radius
     */
    static List<Planet> getPlanetsSortedByRadiusAndDistance(GameMap gameMap, Ship ship, int numberOfPlanets) {
        int counter = 0;
        List<Planet> planets = getPlanetsSortedByDistance(gameMap, ship);
        List<Planet> sortedByRadius = new ArrayList<>();

        for (Planet planet : planets) {
            if (counter == numberOfPlanets) {
                break;
            }

            sortedByRadius.add(planet);
            counter++;
        }

        return getPlanetsSortedByRadius(sortedByRadius);
    }

    /**
     * Returns the list of ships from closest to farthest for this particular Ship.
     * Set isEnemiesOnly to true if only enemies' ships should be returned.
     */
    static List<Ship> getSortedShipsByDistance(GameMap gameMap, Ship myShip, boolean isEnemiesOnly) {
        int playerId = StrategyHelper.HELPER.getCurrentState().getMyId();
        Map<Ship, Double> distanceToShips = new HashMap<>();

        for (Ship ship : gameMap.getAllShips()) {
            if (isEnemiesOnly) {
                if (ship.getOwner() != playerId) {
                    distanceToShips.put(ship, ship.getDistanceTo(myShip));
                }
            } else {
                distanceToShips.put(ship, ship.getDistanceTo(myShip));
            }
        }

        List<Map.Entry<Ship, Double>> sortedDistances = new ArrayList<>(distanceToShips.entrySet());
        sortedDistances.sort(Comparator.comparingDouble(Map.Entry::getValue));

        List<Ship> result = new ArrayList<>();

        for (Map.Entry<Ship, Double> entry : sortedDistances) {
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

    /**
     * Returns the Planet from the list of planets by its id
     */
    static Planet getPlanetById(Integer id, Collection<Planet> planets) {
        if (id != null) {
            for (Planet planet : planets) {
                if (planet.getId() == id) {
                    return planet;
                }
            }
        }

        return null;
    }

    /**
     * Returns the Ship from the list of ships by its id
     */
    static Ship getShipById(Integer id, Collection<Ship> ships) {
        if (id != null) {
            for (Ship ship : ships) {
                if (ship.getId() == id) {
                    return ship;
                }
            }
        }

        return null;
    }

    /**
     * Returns my player's Undocked ships
     */
    static Collection<Ship> getUndockedShips(GameMap gameMap) {
        List<Ship> result = new ArrayList<>();
        Collection<Ship> allShips = gameMap.getMyPlayer().getShips().values();

        for (Ship ship : allShips) {
            if (ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
                result.add(ship);
            }
        }

        return result;
    }

    /**
     * Returns my player's planets
     */
    static Collection<Planet> getMyPlanets(GameMap gameMap) {
        List<Planet> result = new ArrayList<>();
        Collection<Planet> allPlanets = gameMap.getAllPlanets().values();

        for (Planet planet : allPlanets) {
            if (isPlanetMine(planet)) {
                result.add(planet);
            }
        }

        return result;
    }

    /**
     * Returns enemies planets
     */
    static Collection<Planet> getEnemiesPlanets(GameMap gameMap) {
        List<Planet> result = new ArrayList<>();
        Collection<Planet> allPlanets = gameMap.getAllPlanets().values();

        for (Planet planet : allPlanets) {
            if (!isPlanetMine(planet)) {
                result.add(planet);
            }
        }

        return result;
    }

    /**
     * Returns free planets
     */
    static Collection<Planet> getFreePlanets(GameMap gameMap) {
        List<Planet> result = new ArrayList<>();
        Collection<Planet> allPlanets = gameMap.getAllPlanets().values();

        for (Planet planet : allPlanets) {
            if (!planet.isOwned()) {
                result.add(planet);
            }
        }

        return result;
    }

    /**
     * Returns all my ships
     */
    static Collection<Ship> getMyShips(GameMap gameMap) {
        return gameMap.getMyPlayer().getShips().values();
    }

    /**
     * Returns true if the Planet belongs to my player
     */
    static boolean isPlanetMine(Planet planet) {
        int playerId = StrategyHelper.HELPER.getCurrentState().getMyId();
        return planet.isOwned() && planet.getOwner() == playerId;
    }

    /**
     * Returns true if the Planet allows more ships to be docked
     */
    static boolean doesPlanetHaveDockingSpots(Planet planet) {
        return isPlanetMine(planet) && !planet.isFull();
    }

    /**
     * NAVIGATION: Returns true if the ship was sent to attack enemy
     */
    static boolean isShipSentToAttack(GameMap gameMap, ArrayList<Move> moveList, Ship ship, List<Ship> enemyShips, boolean avoidObstacles) {
        for (Ship enemyShip : enemyShips) {
            ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship,
                    new Position(enemyShip.getXPos(), enemyShip.getYPos()), Constants.MAX_SPEED,
                    avoidObstacles, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180.0);

            if (newThrustMove != null) {
                moveList.add(newThrustMove);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if two Lines intersect or not
     */
    static boolean intersectLines(Line line1, Line line2) {
        return intersectLines(line1.getPos1(), line1.getPos2(), line2.getPos1(), line2.getPos2());
    }

    private static boolean intersectLines(Position posStart1, Position posEnd1, Position posStart2, Position posEnd2) {
        double x1 = posStart1.getXPos();
        double y1 = posStart1.getYPos();
        double x2 = posEnd1.getXPos();
        double y2 = posEnd1.getYPos();

        double x3 = posStart2.getXPos();
        double y3 = posStart2.getYPos();
        double x4 = posEnd2.getXPos();
        double y4 = posEnd2.getYPos();

        double a1 = x2 - x1;
        double b1 = y2 - y1;
        double a2 = x4 - x3;
        double b2 = y4 - y3;
        double c1 = x3 - x1;
        double c2 = y3 - y1;

        double delta = a1 * b2 - a2 * b1;
        if (delta == 0) {
            return false;
        }

        if (checkIntersectionPoint((c1 * b2 - c2 * a2) / delta)) return false;
        if (checkIntersectionPoint((c1 * b1 - c2 * a1) / delta)) return false;

        return true;
    }

    private static boolean checkIntersectionPoint(double delta) {
        return delta < 0 || delta > 1;
    }
}
