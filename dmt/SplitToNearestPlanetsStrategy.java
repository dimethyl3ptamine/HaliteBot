package dmt;

import hlt.*;

import java.util.*;

class SplitToNearestPlanetsStrategy implements Strategy {

    // TODO : Fix collision validation as in some rare cases collision still occurs

    private static final String NAME = "Split initial ships to nearest planets";

    private static final int MAX_CLOSEST_PLANETS = 5;
    private static final int MAX_INITIAL_COLLISION_CHECK = 3;

    @Override
    public String getStrategyName() {
        return NAME;
    }

    private Map<Integer, Integer> shipsToPlanets = new HashMap<>(); // Ship.id -> Planet.id

    SplitToNearestPlanetsStrategy() {
        Map<Ship, Planet> shipsPlanetsMap = generateShipPlanetMap();
        checkShipsCollision(shipsPlanetsMap);

        for (Ship ship : shipsPlanetsMap.keySet()) {
            Utils.log("Sending ship " + ship.getId() + " to planet " + shipsPlanetsMap.get(ship).getId());
            shipsToPlanets.put(ship.getId(), shipsPlanetsMap.get(ship).getId());
        }
    }

    private Map<Ship, Planet> generateShipPlanetMap() {
        Map<Ship, Planet> shipsPlanetsMap = new TreeMap<>(Comparator.comparingInt(Entity::getId));

        for (Ship ship : getState().getAllMyShips()) {
            List<Planet> nearestPlanets = Utils.getPlanetsSortedByRadiusAndDistance(ship, MAX_CLOSEST_PLANETS);

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

        do {
            isCollision = checkLinesCollision(shipsPlanetsMap);

            if (isCollision) {
                Utils.log("Possible collision detected");
                swapPlanetsForShips(shipsPlanetsMap);
            }
        } while (isCollision && (check++ <= MAX_INITIAL_COLLISION_CHECK));
    }

    private List<Line> regenerateLines(Map<Ship, Planet> shipsPlanetsMap) {
        List<Line> lines = new ArrayList<>();

        for (Ship ship : shipsPlanetsMap.keySet()) {
            Planet planet = shipsPlanetsMap.get(ship);

            if (planet != null) {
                lines.add(new Line(ship, planet));
            }
        }

        return lines;
    }

    private boolean checkLinesCollision(Map<Ship, Planet> shipsPlanetsMap) {
        List<Line> positions = regenerateLines(shipsPlanetsMap);

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
        // We should have only three ships in the beginning
        ArrayList<Ship> ships = new ArrayList<>(shipsPlanetsMap.keySet());
        Ship ship0 = ships.get(0);
        Ship ship1 = ships.get(1);
        Ship ship2 = ships.get(2);

        if (ship0 == null || ship1 == null || ship2 == null) {
            // It seems that something went wrong... better to use the first set of pairs
            Utils.log("SplitToNearestPlanetsStrategy: null ship for swapPlanetsForShips()", true);
            return;
        }

        Planet planet0 = shipsPlanetsMap.get(ship0);

        shipsPlanetsMap.replace(ship0, shipsPlanetsMap.get(ship1));
        shipsPlanetsMap.replace(ship1, shipsPlanetsMap.get(ship2));
        shipsPlanetsMap.replace(ship2, planet0);
    }

    @Override
    public void calculateMovements(ArrayList<Move> moveList) throws StrategyException {
        Collection<Ship> ships = getState().getMyUndockedShips();
        Collection<Planet> planets = getState().getAllPlanets();

        for (Ship ship : ships) {
            Utils.log("Processing ship: " + ship);
            Planet planet = Utils.getPlanetById(shipsToPlanets.get(ship.getId()), planets);

            if (planet != null) {
                if (Utils.isPlanetOwnedByEnemy(planet)) {
                    attackEnemies(moveList, ship);
                } else {
                    navigateToPlanet(moveList, ship, planet);
                }
            } else {
                // This should be a rare case for this strategy, better to rollback
                throw new StrategyException("Null processing planet");
            }
        }
    }

    private void navigateToPlanet(ArrayList<Move> moveList, Ship ship, Planet planet) throws StrategyException {
        if (ship.canDock(planet)) {
            moveList.add(new DockMove(ship, planet));
        } else {
            ThrustMove newThrustMove = Utils.navigateShipToDock(ship, planet);

            if (newThrustMove != null) {
                moveList.add(newThrustMove);
            } else {
                // Can't navigate to this planet, better to rollback
                throw new StrategyException("newThrustMove is null");
            }
        }
    }

    private void attackEnemies(ArrayList<Move> moveList, Ship ship) throws StrategyException {
        List<Ship> enemies = Utils.getShipsSortedByDistance(ship, true);

        if (!Utils.isShipSentToAttackEnemies(moveList, ship, enemies, true)) {
            // Couldn't attack anyone, better to rollback
            throw new StrategyException("Could't attack any enemy");
        }
    }

}
