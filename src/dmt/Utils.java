package dmt;

import hlt.*;

import java.util.*;

import static dmt.StrategyHelper.HELPER;

public class Utils {

    // TODO : always fix logging before commit. Just in case :D
    private static final boolean LOGGING = false;

    private static final int UNLIMITED_ENEMIES = 1000;

    static final double UNLIMITED_RADIUS = 1000.0d;

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
    static List<Planet> getPlanetsSortedByDistance(Ship ship, Double radiusLimit) {
        GameState state = HELPER.getCurrentState();
        List<Map.Entry<Planet, Double>> sortedPlanets = getPlanetsSortedByDistance(ship, state);
        sortedPlanets.sort(Comparator.comparingDouble(Map.Entry::getValue));

        List<Planet> result = new ArrayList<>();

        for (Map.Entry<Planet, Double> entry : sortedPlanets) {
            if (entry.getValue() <= radiusLimit) {
                result.add(entry.getKey());
            }
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

        for (Planet planet : getPlanetsSortedByDistance(ship, UNLIMITED_RADIUS)) {
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
        return getShipsSortedByDistance(myShip, isEnemiesOnly, UNLIMITED_RADIUS);
    }

    /**
     * Returns the list of ships from closest to farthest for this particular Ship.
     * Set isEnemiesOnly to true if only enemies' ships should be returned.
     */
    static List<Ship> getShipsSortedByDistance(Ship myShip, boolean isEnemiesOnly, Double radiusLimit) {
        GameState state = HELPER.getCurrentState();
        List<Map.Entry<Ship, Double>> sortedDistances = getShipsSortedByDistance(myShip, isEnemiesOnly, state);
        sortedDistances.sort(Comparator.comparingDouble(Map.Entry::getValue));

        List<Ship> result = new ArrayList<>();

        for (Map.Entry<Ship, Double> entry : sortedDistances) {
            if (entry.getValue() <= radiusLimit) {
                result.add(entry.getKey());
            }
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
        int playerId = HELPER.getCurrentState().getMyId();
        return planet.isOwned() && planet.getOwner() == playerId;
    }

    /**
     * Returns true if the Planet is owned but not by my player
     */
    static boolean isPlanetOwnedByEnemy(Planet planet) {
        int playerId = HELPER.getCurrentState().getMyId();
        return planet.isOwned() &&  planet.getOwner() != playerId;
    }

    /**
     * Returns true if the Planet allows more ships to be docked
     */
    static boolean doesPlanetHaveDockingSpots(Planet planet) {
        return isPlanetOwnedByMe(planet) && !planet.isFull();
    }

    public static void saveShipToMap(Ship ship, Position newPos) {
        Map<Integer, Map.Entry<Position, Position>> allShips = HELPER.getCurrentState().getNavigationShipsMap();
        allShips.put(ship.getId(), new AbstractMap.SimpleEntry<>(ship, newPos));
    }

    /**
     * Checks if Ships intersect or not
     */
    public static boolean intersectShipWithMyOtherShips(Position oldPos, Position targetPos) {
        Line line1 = getLine(oldPos, targetPos, 90);
        Line line2 = getLine(oldPos, targetPos, -90);

        Map<Integer, Map.Entry<Position, Position>> allShips = HELPER.getCurrentState().getNavigationShipsMap();

        // TODO : Too many calculations! We should care about closest ships, not all!
        for (Integer i : allShips.keySet()) {
            Map.Entry<Position, Position> entry = allShips.get(i);
            Position pos1 = entry.getKey();
            Position pos2 = entry.getValue();

            Line line3 = getLine(pos1, pos2, 90);
            Line line4 = getLine(pos1, pos2, -90);

            if (intersectLines(line1, line3) || intersectLines(line1, line4) || intersectLines(line2, line3) || intersectLines(line2, line4)) {
                return true;
            }
        }

        return false;
    }

    private static Line getLine(Position oldPos, Position targetPos, int angl) {
        final double distance = Constants.FORECAST_FUDGE_FACTOR;
        final double angleRadOld = oldPos.orientTowardsInRad(targetPos);

        final double oldShipDx1 = Math.cos(angleRadOld + angl) * distance;
        final double oldShipDy1 = Math.sin(angleRadOld + angl) * distance;

        final Position oldShip1 = new Position(oldPos.getXPos() + oldShipDx1, oldPos.getYPos() + oldShipDy1);
        final Position newShip1 = new Position(targetPos.getXPos() + oldShipDx1, targetPos.getYPos() + oldShipDy1);

        return new Line(oldShip1, newShip1);
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
        GameMap gameMap = HELPER.getCurrentState().getMap();
        ThrustMove result = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

        return result;
    }

    /**
     * Navigation shortcut to Navigation.navigateShipTowardsTarget()
     */
    static ThrustMove navigateShipToPosition(Ship ship, Position position, boolean avoidObstacles) {
        GameMap gameMap = HELPER.getCurrentState().getMap();
        ThrustMove result = Navigation.navigateShipTowardsTarget(gameMap, ship, position, Constants.MAX_SPEED,
                avoidObstacles, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180.0);

        return result;
    }

    /**
     * Navigation: move to another ship, but avoid collision
     */
    static ThrustMove navigateShipToShip(Ship ship, Ship enemy, boolean avoidObstacles) {
        GameMap gameMap = HELPER.getCurrentState().getMap();
        ThrustMove result = Navigation.navigateShipTowardsTarget(gameMap, ship,
                getClosestPointToShip(ship, enemy), Constants.MAX_SPEED, avoidObstacles,
                Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180.0);

        return result;
    }

    // Similar to Position.getClosestPoint()
    private static Position getClosestPointToShip(Entity ship, Entity target) {
        final double minDistance = Constants.WEAPON_RADIUS - Constants.SHIP_RADIUS;
        final double radius = target.getRadius() + minDistance;
        final double angleRad = target.orientTowardsInRad(ship);

        final double x = target.getXPos() + radius * Math.cos(angleRad);
        final double y = target.getYPos() + radius * Math.sin(angleRad);

        return new Position(x, y);
    }

    /**
     * Navigation: Returns true if the ship was sent to attack enemy
     */
    static boolean isShipSentToAttackEnemies(ArrayList<Move> moveList, Ship ship, List<Ship> enemyShips, boolean avoidObstacles) {
        for (Ship enemyShip : enemyShips) {
            ThrustMove newThrustMove = navigateShipToShip(ship, enemyShip, avoidObstacles);

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
    static boolean isShipSentToAttackDockedEnemies(ArrayList<Move> moveList, Ship ship, List<Ship> enemyShips) {
        return isShipSentToAttackDockedEnemies(moveList, ship, enemyShips, UNLIMITED_ENEMIES);
    }

    /**
     * Navigation: Returns true if the ship was sent to one of the nearest enemy docked to the planet
     */
    static boolean isShipSentToAttackDockedEnemies(ArrayList<Move> moveList, Ship ship, List<Ship> enemyShips, int threshold) {
        int counter = -1;

        for (Ship enemy : enemyShips) {
            if (enemy.getDockingStatus() != Ship.DockingStatus.Undocked) {
                ThrustMove newThrustMove = Utils.navigateShipToShip(ship, enemy, true);

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