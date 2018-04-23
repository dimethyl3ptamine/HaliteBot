package dmt;

import hlt.*;

import java.util.*;

class SplitToNearestPlanetsStrategy implements Strategy {

    private static final String NAME = "Split initial ships to nearest planets";
    private static final int MAX_INITIAL_COLLISION_CHECK = 3;
    private static final int MAX_CLOSEST_PLANETS = 5;

    @Override
    public String getStrategyName() {
        return NAME;
    }

    // Ship.id -> Planet.id
    private Map<Integer, Integer> shipsToPlanets = new HashMap<>();

    SplitToNearestPlanetsStrategy(GameMap gameMap) {
        Map<Ship, Planet> shipsPlanetsMap = generateShipPlanetMap(gameMap);
        checkShipsCollision(shipsPlanetsMap);

        for (Ship ship : shipsPlanetsMap.keySet()) {
            Utils.log("Sending ship " + ship.getId() + " to planet " + shipsPlanetsMap.get(ship).getId());
            shipsToPlanets.put(ship.getId(), shipsPlanetsMap.get(ship).getId());
        }
    }

    private Map<Ship, Planet> generateShipPlanetMap(GameMap gameMap) {
        // Consider all player's ships
        Collection<Ship> allShips = gameMap.getMyPlayer().getShips().values();
        Map<Ship, Planet> shipsPlanetsMap = new TreeMap<>(Comparator.comparingInt(Entity::getId));

        for (Ship ship : allShips) {
            List<Planet> nearestPlanets = Utils.getPlanetsSortedByRadiusAndDistance(gameMap, ship, MAX_CLOSEST_PLANETS);

            for (Planet planet : nearestPlanets) {
                if (shipsPlanetsMap.containsValue(planet)) {
                    continue;
                }

                shipsPlanetsMap.put(ship, planet);
                break;
            }
        }

        return shipsPlanetsMap;
    }

    private void checkShipsCollision(Map<Ship, Planet> shipsPlanetsMap) {
        int check = 0;
        boolean isCollision;
        List<Line> lines = new ArrayList<>();

        do {
            check++;
            lines.clear();

            for (Ship ship : shipsPlanetsMap.keySet()) {
                Planet planet = shipsPlanetsMap.get(ship);
                lines.add(new Line(new Position(ship.getXPos(), ship.getYPos()),
                                   new Position(planet.getXPos(), planet.getYPos())));
            }

            isCollision = checkLinesCollision(lines);

            if (isCollision) {
                Utils.log("Possible collision detected");
                swapPlanetsForShips(shipsPlanetsMap);
            }
        } while (isCollision && (check < MAX_INITIAL_COLLISION_CHECK));
    }

    private boolean checkLinesCollision(List<Line> positions) {
        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 1; j < positions.size(); j++) {
                if (Utils.intersectLines(positions.get(i), positions.get(j))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void swapPlanetsForShips(Map<Ship, Planet> shipsPlanetsMap) {
        // We should have only 3 ships in the beginning
        ArrayList<Ship> ships = new ArrayList<>(shipsPlanetsMap.keySet());
        Ship ship0 = ships.get(0);
        Ship ship1 = ships.get(1);
        Ship ship2 = ships.get(2);

        if (ship0 == null || ship1 == null || ship2 == null) {
            Utils.log("SplitToNearestPlanetsStrategy: null ship for swapPlanetsForShips()", true);
            return;
        }

        Planet planet0 = shipsPlanetsMap.get(ship0);

        shipsPlanetsMap.replace(ship0, shipsPlanetsMap.get(ship1));
        shipsPlanetsMap.replace(ship1, shipsPlanetsMap.get(ship2));
        shipsPlanetsMap.replace(ship2, planet0);
    }

    @Override
    public void calculateMovements(GameMap gameMap, ArrayList<Move> moveList) throws StrategyException {
        Collection<Ship> ships = Utils.getUndockedShips(gameMap);
        Collection<Planet> planets = gameMap.getAllPlanets().values();

        for (Ship ship : ships) {
            Utils.log("Processing ship: " + ship);
            Planet planet = Utils.getPlanetById(shipsToPlanets.get(ship.getId()), planets);

            if (planet != null) {
                if (planet.isOwned() && !Utils.isPlanetMine(planet)) {
                    List<Ship> enemies = Utils.getSortedShipsByDistance(gameMap, ship, true);

                    if (!Utils.isShipSentToAttack(gameMap, moveList, ship, enemies, true)) {
                        // Can't attack the closest enemy, better to rollback
                        throw new StrategyException("Can't attack the closest enemy");
                    }
                } else {
                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                    } else {
                        ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

                        if (newThrustMove != null) {
                            moveList.add(newThrustMove);
                        } else {
                            // Can't navigate to this planet, better to rollback
                            throw new StrategyException("newThrustMove is null");
                        }
                    }
                }
            } else {
                // This should be a rare case for this strategy
                throw new StrategyException("Null processing planet");
            }
        }
    }

}
