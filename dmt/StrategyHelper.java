package dmt;

import hlt.GameMap;
import hlt.Move;

import java.util.ArrayList;
import java.util.Stack;

public enum StrategyHelper {
    HELPER;

    private static final int SPLIT_STRATEGY_MAX_TURNS = 20;
    private static final int FLEE_PLANETS_INDICATOR = 2;
    private static final int FLEE_SHIPS_INDICATOR = 10;

    private static final int PLAYERS_GAME_2 = 2;
    private static final int PLAYERS_GAME_4 = 4;

    private static final boolean I_JUST_WANNA_LULZ = Boolean.FALSE; // Let's have some fun!
    private static final int KAMIKADZE_THRESHOLD = 49;

    private Stack<GameState> states;
    private Strategy currentStrategy;
    private SplitToNearestPlanetsStrategy splitToNearestPlanetsStrategy;
    private MoveAndKillStrategy moveAndKillStrategy;
    private FleeStrategy fleeStrategy;
    private KamikadzeStrategy kamikadzeStrategy;

    public void init(GameMap map) {
        Utils.log(String.format("Initialized map %s x %s with %s players and %s free planets (I am %s)",
                map.getWidth(), map.getHeight(), map.getAllPlayers().size(), map.getAllPlanets().size(), map.getMyPlayerId()));

        states = new Stack<>();
        states.push(new GameState(map, 0));

        boolean isTwoPlayersGame = getCurrentState().getNumberOfPlayers() == PLAYERS_GAME_2;
        splitToNearestPlanetsStrategy = new SplitToNearestPlanetsStrategy(isTwoPlayersGame);
        moveAndKillStrategy = new MoveAndKillStrategy();
        fleeStrategy = new FleeStrategy();
        kamikadzeStrategy = new KamikadzeStrategy();

        currentStrategy = splitToNearestPlanetsStrategy;
    }

    public Strategy getStrategy(int turn, GameMap gameMap) {
        states.push(new GameState(gameMap, turn));

        // TODO : analyse previous states, but too lazy to do that :D

        currentStrategy = getBestStrategy();
        Utils.log("Current strategy: " + currentStrategy.getStrategyName());

        return currentStrategy;
    }

    public GameState getCurrentState() {
        return states.peek();
    }

    public void rollbackToDefaultStrategy(ArrayList<Move> moveList) {
        Utils.log("Rolling back to default strategy!", true);

        moveList.clear();
        currentStrategy = moveAndKillStrategy;

        try {
            currentStrategy.calculateMovements(moveList);
        } catch (StrategyException ignored) {
        }
    }

    private Strategy getBestStrategy() {
        if (fleeStrategy.isActivated()) {
            return fleeStrategy;
        }

        if (currentStrategy == splitToNearestPlanetsStrategy && validateSplitStrategy()) {
            return splitToNearestPlanetsStrategy;
        } else {
            if (I_JUST_WANNA_LULZ && getCurrentState().getTurn() >= KAMIKADZE_THRESHOLD) {
                return kamikadzeStrategy;
            }

            if (validateFleeStrategy()) {
                fleeStrategy.initStrategy();
                return fleeStrategy;
            }

            // TODO: support extra strategies (if have time)
            return moveAndKillStrategy;
        }
    }

    // Returns true if we should proceed to use SplitToNearestPlanetsStrategy
    private boolean validateSplitStrategy() {
        if (splitToNearestPlanetsStrategy.isHunterModeActivated()) {
            return true;
        }

        GameState state = getCurrentState();
        return state.getMyUndockedShips().size() != 0 && state.getTurn() < SPLIT_STRATEGY_MAX_TURNS;
    }

    // Returns true if we should switch to FleeStrategy
    private boolean validateFleeStrategy() {
        GameState state = getCurrentState();

        if (state.getNumberOfPlayers() != PLAYERS_GAME_4 || state.getNumberOfActivePlayers() <= PLAYERS_GAME_2) {
            // Not applicable in 2-players games
            return false;
        }

        boolean manyFreePlanets = state.getFreePlanets().size() >= FLEE_PLANETS_INDICATOR;
        boolean fewMyPlanets = state.getAllMyPlanets().size() <= FLEE_PLANETS_INDICATOR;
        boolean fewMyShips = state.getEnemiesShips().size() > (state.getAllMyShips().size() * FLEE_SHIPS_INDICATOR);

        if (fewMyShips) {
            return true;
        }

        if (manyFreePlanets) {
            return false;
        }

        return fewMyPlanets;
    }

}
