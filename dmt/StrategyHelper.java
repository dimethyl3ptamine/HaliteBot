package dmt;

import hlt.GameMap;
import hlt.Move;
import hlt.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum StrategyHelper {
    HELPER;

    private Map<Integer, State> states;
    private DefaultStrategy defaultStrategy;
    private SplitToNearestPlanetsStrategy splitToNearestPlanetsStrategy;
    private ExploringStrategy exploringStrategy;

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

        defaultStrategy = new DefaultStrategy();
        splitToNearestPlanetsStrategy = new SplitToNearestPlanetsStrategy(gameMap);
        exploringStrategy = new ExploringStrategy();

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
        Utils.log("My undocked ships: " + Utils.getUndockedShips(gameMap).size());
        // TODO : analyse previous states

        currentStrategy = getNextStrategy(gameMap);
        Utils.log("Current strategy: " + currentStrategy.getStrategyName());

        return currentStrategy;
    }

    public int getTurn() {
        return states.size();
    }

    public State getCurrentState() {
        return states.get(getTurn());
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

    private Strategy getNextStrategy(GameMap gameMap) {
        if (currentStrategy == splitToNearestPlanetsStrategy && validateSplitStrategy(gameMap)) {
            return splitToNearestPlanetsStrategy;
        } else {
            // TODO: support extra strategies
            return exploringStrategy;
        }
    }

    private boolean validateSplitStrategy(GameMap gameMap) {
        return Utils.getUndockedShips(gameMap).size() != 0;
    }

}
