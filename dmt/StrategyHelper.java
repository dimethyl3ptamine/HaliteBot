package dmt;

import hlt.GameMap;
import hlt.Move;

import java.util.ArrayList;
import java.util.Stack;

public enum StrategyHelper {
    HELPER;

    private static final int FLEE_INDICATOR = 2;

    private Stack<GameState> states;
    private SplitToNearestPlanetsStrategy splitToNearestPlanetsStrategy;
    private MoveOrKillStrategy moveOrKillStrategy;
    private FleeStrategy fleeStrategy;

    private Strategy currentStrategy;

    public void init(GameMap map) {
        Utils.log(String.format("Initialized map %s x %s with %s players and %s free planets (I am %s)",
                map.getWidth(), map.getHeight(), map.getAllPlayers().size(), map.getAllPlanets().size(), map.getMyPlayerId()));

        states = new Stack<>();
        states.push(new GameState(map, 0));

        splitToNearestPlanetsStrategy = new SplitToNearestPlanetsStrategy();
        moveOrKillStrategy = new MoveOrKillStrategy();
        fleeStrategy = new FleeStrategy();

        currentStrategy = splitToNearestPlanetsStrategy;
    }

    public Strategy getStrategy(int turn, GameMap gameMap) {
        states.push(new GameState(gameMap, turn));

        // TODO : analyse previous states

        currentStrategy = getNextStrategy();
        Utils.log("Current strategy: " + currentStrategy.getStrategyName());

        return currentStrategy;
    }

    public GameState getCurrentState() {
        return states.peek();
    }

    public void rollbackToDefaultStrategy(ArrayList<Move> moveList) {
        Utils.log("Rolling back to default strategy!", true);

        moveList.clear();
        currentStrategy = moveOrKillStrategy;

        try {
            currentStrategy.calculateMovements(moveList);
        } catch (StrategyException ignored) {
        }
    }

    private Strategy getNextStrategy() {
        if (fleeStrategy.isActivated()) {
            return fleeStrategy;
        }

        if (currentStrategy == splitToNearestPlanetsStrategy && validateSplitStrategy()) {
            return splitToNearestPlanetsStrategy;
        } else {
            if (validateFleeStrategy()) {
                fleeStrategy.initStrategy();
                return fleeStrategy;
            }

            // TODO: support extra strategies
            return moveOrKillStrategy;
        }
    }

    private boolean validateSplitStrategy() {
        return getCurrentState().getMyUndockedShips().size() != 0;
    }

    private boolean validateFleeStrategy() {
        GameState state = getCurrentState();

        if (state.getNumberOfPlayers() != 4) {
            return false;
        }

        int all = state.getAllPlanets().size();
        int mine = state.getAllMyPlanets().size();
        int free = state.getFreePlanets().size();
        int enemies = state.getEnemiesPlanets().size();
        Utils.log(String.format("Planets info: total %s, mine %s, enemies %s, free %s", all, mine, enemies, free));

        if (free >= FLEE_INDICATOR) {
            return false;
        }

        return mine <= FLEE_INDICATOR;
    }

}
