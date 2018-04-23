package dmt;

import hlt.GameMap;
import hlt.Move;
import hlt.Player;
import hlt.Ship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum StrategyHelper {
    HELPER;

    private Map<Integer, State> states;
    private SplitToNearestPlanetsStrategy splitToNearestPlanetsStrategy;
    private DefaultStrategy defaultStrategy;
    private Strategy currentStrategy;

    public void init(GameMap gameMap) {
        String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size()
                ;
        Utils.log(initialMapIntelligence);

        states = new HashMap<>();
        splitToNearestPlanetsStrategy = new SplitToNearestPlanetsStrategy(gameMap);
        defaultStrategy = new DefaultStrategy();
        currentStrategy = splitToNearestPlanetsStrategy;
    }

    public Strategy getStrategy(int turn, GameMap gameMap) {
        states.put(turn, new State(gameMap));

        String mapStat = "My id is " + gameMap.getMyPlayer().getId();
        for (Player p : gameMap.getAllPlayers()) {
            mapStat += "; player " + p.getId() + " has " + p.getShips().size();
        }

        mapStat += "--> Total ships: " + gameMap.getAllShips().size();
        Utils.log(mapStat);
        // TODO : analyse previous states

        if (currentStrategy == splitToNearestPlanetsStrategy) {
            currentStrategy = validateSplitStrategy(gameMap);
        }

        Utils.log("Current strategy: " + currentStrategy.getStrategyName());
        return currentStrategy;
    }

    public int getTurn() {
        return states.size();
    }

    public void rollbackToDefaultStrategy(GameMap gameMap, ArrayList<Move> moveList) {
        Utils.log("Rolling back to default strategy!", true);

        moveList.clear();
        currentStrategy = defaultStrategy;

        try {
            currentStrategy.calculateMovements(gameMap, moveList);
        } catch (StrategyException ignored) {
        }
    }

    private Strategy validateSplitStrategy(GameMap gameMap) {
        Map<Integer, Ship> ships = gameMap.getMyPlayer().getShips();
        int myShips = ships.size();

        for (Ship ship : ships.values()) {
            if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                myShips--;
            }
        }

        if (myShips == 0) {
            currentStrategy = defaultStrategy;
        }

        return currentStrategy;
    }

}
