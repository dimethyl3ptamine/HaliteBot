package dmt;

import hlt.*;

import java.util.*;

class SplitToNearestPlanetsStrategy implements Strategy {

    private static final String NAME = "Split initial ships to nearest planets";

    @Override
    public String getStrategyName() {
        return NAME;
    }

    // Ship.id -> Planet.id
    private Map<Integer, Integer> shipsToPlanet = new HashMap<>();

    SplitToNearestPlanetsStrategy(GameMap gameMap) {
        Collection<Ship> ships = gameMap.getMyPlayer().getShips().values();

        for (Ship ship : ships) {
            List<Planet> nearestPlanets = Utils.getSortedPlanetsByDistance(gameMap, ship);

            for (Planet planet : nearestPlanets) {
                if (shipsToPlanet.containsValue(planet.getId())) {
                    continue;
                }

                shipsToPlanet.put(ship.getId(), planet.getId());
                break;
            }
        }

        // TODO : check for friendly collision
    }

    @Override
    public void calculateMovements(GameMap gameMap, ArrayList<Move> moveList) throws StrategyException {
        Collection<Ship> ships = gameMap.getMyPlayer().getShips().values();
        Collection<Planet> planets = gameMap.getAllPlanets().values();

        for (Ship ship : ships) {
            Utils.log("Ship: " + ship);

            if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                continue;
            }

            Planet planet = getPlanetById(shipsToPlanet.get(ship.getId()), planets);

            if (planet != null) {
                if (ship.canDock(planet)) {
                    moveList.add(new DockMove(ship, planet));
                } else {
                    ThrustMove newThrustMove;
                    newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    } else {
                        // We shouldn't reach this point
                        Utils.log("null newThrustMove", true);
                    }
                }
            } else {
                // This should be a rare case for this strategy
                throw new StrategyException("StrategyException: null planet in " + getStrategyName());
            }
        }
    }

    private Planet getPlanetById(Integer id, Collection<Planet> planets) {
        if (id != null) {
            for (Planet planet : planets) {
                if (planet.getId() == id) {
                    return planet;
                }
            }
        }

        return null;
    }

}
