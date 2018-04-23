package dmt;

import hlt.*;

import java.util.*;

public class Utils {

    // TODO : always fix logging before commit. Just in case :D
    private static final boolean LOGGING = false;

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
    static List<Planet> getPlanetsSortedByDistance(Ship ship) {
        GameState state = StrategyHelper.HELPER.getCurrentState();
        List<Map.Entry<Planet, Double>> sortedPlanets = getPlanetsSortedByDistance(ship, state);
        sortedPlanets.sort(Comparator.comparingDouble(Map.Entry::getValue));

        List<Planet> result = new ArrayList<>();

        for (Map.Entry<Planet, Double> entry : sortedPlanets) {
            result.add(entry.getKey());
        }

        return result;
    }

    private static List<Map.Entry<Planet, Double>> getPlanetsSortedByDistance(Ship ship, GameState state) {
        Map<Planet, Double> distanceToPlanet = new HashMap<>();

        for (Planet planet : state.getAllPlanets()) {
            distanceToPlanet.put(planet, ship.getDistanceTo(planet));
        }

        return new ArrayList<>(distanceToPlanet.entrySet());
    }

    /**
     * Returns the list of planets from closest to farthest for this particular Ship, considering planets' radius
     * Number of planets to be checked: numberOfPlanets
     */
    static List<Planet> getPlanetsSortedByRadiusAndDistance(Ship ship, int numberOfPlanets) {
        int counter = -1;
        List<Planet> sortedByRadius = new ArrayList<>();

        for (Planet planet : getPlanetsSortedByDistance(ship)) {
            if (counter++ == numberOfPlanets) {
                break;
            }

            sortedByRadius.add(planet);
        }

        return getPlanetsSortedByRadius(sortedByRadius);
    }

    /**
     * Returns the list of ships from closest to farthest for this particular Ship.
     * Set isEnemiesOnly to true if only enemies' ships should be returned.
     */
    static List<Ship> getShipsSortedByDistance(Ship myShip, boolean isEnemiesOnly) {
        GameState state = StrategyHelper.HELPER.getCurrentState();
        List<Map.Entry<Ship, Double>> sortedDistances = getShipsSortedByDistance(myShip, isEnemiesOnly, state);
        sortedDistances.sort(Comparator.comparingDouble(Map.Entry::getValue));

        List<Ship> result = new ArrayList<>();

        for (Map.Entry<Ship, Double> entry : sortedDistances) {
            result.add(entry.getKey());
        }

        return result;
    }

    private static List<Map.Entry<Ship, Double>> getShipsSortedByDistance(Ship myShip, boolean isEnemiesOnly, GameState state) {
        Map<Ship, Double> distanceToShips = new HashMap<>();

        for (Ship ship : state.getAllShips()) {
            if (isEnemiesOnly) {
                if (ship.getOwner() != state.getMyId()) {
                    distanceToShips.put(ship, ship.getDistanceTo(myShip));
                }
            } else {
                distanceToShips.put(ship, ship.getDistanceTo(myShip));
            }
        }

        return new ArrayList<>(distanceToShips.entrySet());
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
     * Returns true if the Planet belongs to my player
     */
    static boolean isPlanetOwnedByMe(Planet planet) {
        int playerId = StrategyHelper.HELPER.getCurrentState().getMyId();
        return planet.isOwned() && planet.getOwner() == playerId;
    }

    /**
     * Returns true if the Planet is owned but not by my player
     */
    static boolean isPlanetOwnedByEnemy(Planet planet) {
        int playerId = StrategyHelper.HELPER.getCurrentState().getMyId();
        return planet.isOwned() &&  planet.getOwner() != playerId;
    }

    /**
     * Returns true if the Planet allows more ships to be docked
     */
    static boolean doesPlanetHaveDockingSpots(Planet planet) {
        return isPlanetOwnedByMe(planet) && !planet.isFull();
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

    /**
     * Navigation shortcut
     */
    static ThrustMove navigateShipToDock(Ship ship, Planet planet) {
        GameMap gameMap = StrategyHelper.HELPER.getCurrentState().getMap();
        return Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
    }

    /**
     * Navigation shortcut
     */
    static ThrustMove navigateShipToTarget(Ship ship, Position position, boolean avoidObstacles) {
        GameMap gameMap = StrategyHelper.HELPER.getCurrentState().getMap();
        return Navigation.navigateShipTowardsTarget(gameMap, ship, position, Constants.MAX_SPEED,
                avoidObstacles, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180.0);
    }

    /**
     * Navigation: Returns true if the ship was sent to attack enemy
     */
    static boolean isShipSentToAttackEnemies(ArrayList<Move> moveList, Ship ship, List<Ship> enemyShips, boolean avoidObstacles) {
        for (Ship enemyShip : enemyShips) {
            ThrustMove newThrustMove = navigateShipToTarget(ship, enemyShip, avoidObstacles);

            if (newThrustMove != null) {
                moveList.add(newThrustMove);
                return true;
            }
        }

        return false;
    }

    /**
     * Navigation: Returns true if the ship was sent to the nearest planet
     */
    static boolean isShipSentToNearestPlanet(ArrayList<Move> moveList, Ship ship, List<Planet> nearestPlanets) {
        for (Planet planet : nearestPlanets) {
            if (Utils.doesPlanetHaveDockingSpots(planet) && ship.canDock(planet)) {
                moveList.add(new DockMove(ship, planet));
                return true;
            }
        }

        return false;
    }

    /**
     * Navigation: Returns true if the ship was sent to the nearest free planet
     */
    static boolean isShipSentToNearestFreePlanet(ArrayList<Move> moveList, Ship ship, List<Planet> nearestPlanets, int freePlanets) {
        for (Planet planet : nearestPlanets) {
            if (!planet.isOwned()) {
                if (ship.canDock(planet)) {
                    moveList.add(new DockMove(ship, planet));
                    return true;
                } else {
                    // No need to rush to the last free planet like an idiot
                    if (freePlanets != 1) {
                        ThrustMove newThrustMove = Utils.navigateShipToDock(ship, planet);

                        if (newThrustMove != null) {
                            moveList.add(newThrustMove);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Navigation: Returns true if the ship was sent to one of the nearest enemy docked to the planet
     */
    static boolean isShipSentToAttackDockedEnemies(ArrayList<Move> moveList, Ship ship, List<Ship> enemyShips, int threshold) {
        int counter = -1;

        for (Ship enemy : enemyShips) {
            if (enemy.getDockingStatus() != Ship.DockingStatus.Undocked) {
                ThrustMove newThrustMove = Utils.navigateShipToTarget(ship, enemy, true);

                if (newThrustMove != null) {
                    moveList.add(newThrustMove);
                    return true;
                }
            }

            if (counter++ == threshold) {
                return false;
            }
        }

        return false;
    }
}
