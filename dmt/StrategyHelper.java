package dmt;

import hlt.GameMap;
import hlt.Move;
import hlt.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum StrategyHelper {
    HELPER;

    private static final int FLEE_INDICATOR = 2;

    private Map<Integer, State> states;
    private SplitToNearestPlanetsStrategy splitToNearestPlanetsStrategy;
    private ExploringStrategy exploringStrategy;
    private FleeStrategy fleeStrategy;

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
        exploringStrategy = new ExploringStrategy();
        fleeStrategy = new FleeStrategy();

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
        currentStrategy = exploringStrategy;

        try {
            currentStrategy.calculateMovements(gameMap, moveList);
        } catch (StrategyException ignored) {
        }
    }

    private Strategy getNextStrategy(GameMap gameMap) {
        if (fleeStrategy.isActivated()) {
            return fleeStrategy;
        }

        if (currentStrategy == splitToNearestPlanetsStrategy && validateSplitStrategy(gameMap)) {
            return splitToNearestPlanetsStrategy;
        } else {
            if (validateFleeStrategy(gameMap)) {
                fleeStrategy.initStrategy(gameMap);
                return fleeStrategy;
            }

            // TODO: support extra strategies
            return exploringStrategy;
        }
    }

    private boolean validateSplitStrategy(GameMap gameMap) {
        return Utils.getUndockedShips(gameMap).size() != 0;
    }

    private boolean validateFleeStrategy(GameMap gameMap) {
        if (gameMap.getAllPlayers().size() != 4) {
            return false;
        }

        int all = gameMap.getAllPlanets().size();
        int mine = Utils.getMyPlanets(gameMap).size();
        int free = Utils.getFreePlanets(gameMap).size();
        int enemies = Utils.getEnemiesPlanets(gameMap).size();
        Utils.log(String.format("Planets info: total %s, mine %s, enemies %s, free %s", all, mine, enemies, free));

        if (free != 0) {
            return false;
        }

        return mine <= FLEE_INDICATOR;
    }


}
