package dmt;

import hlt.GameMap;
import hlt.Move;

import java.util.ArrayList;
import java.util.Stack;

public enum StrategyHelper {
    INSTANCE;

    private static final int FLEE_SHIPS_INDICATOR = 10;
    private static final int FLEE_PLANETS_INDICATOR = 2;
    private static final int SPLIT_STRATEGY_MAX_TURNS = 20;

    private static final int PLAYERS_GAME_2 = 2;
    private static final int PLAYERS_GAME_4 = 4;

    private static final int KAMIKADZE_THRESHOLD = 49;
    private static final boolean I_JUST_WANNA_LULZ = Boolean.FALSE; // Means I'm fed up having fun! :D

    private Stack<GameState> states;
    private Strategy currentStrategy;
    private FleeStrategy fleeStrategy;
    private SuicideStrategy suicideStrategy;
    private MoveAndKillStrategy moveAndKillStrategy;
    private SplitToNearestPlanetsStrategy splitToNearestPlanetsStrategy;

    public void init(GameMap map) {
        Utils.log(String.format("Initialized map %s x %s with %s players and %s free planets (I am %s)",
                map.getWidth(), map.getHeight(), map.getAllPlayers().size(), map.getAllPlanets().size(), map.getMyPlayerId()));

        states = new Stack<>();
        states.push(new GameState(map, 0));

        boolean isTwoPlayersGame = getCurrentState().getNumberOfPlayers() == PLAYERS_GAME_2;
        splitToNearestPlanetsStrategy = new SplitToNearestPlanetsStrategy(isTwoPlayersGame);
        fleeStrategy = new FleeStrategy();
        suicideStrategy = new SuicideStrategy();
        moveAndKillStrategy = new MoveAndKillStrategy();

        currentStrategy = splitToNearestPlanetsStrategy;
    }

    public Strategy getStrategy(int turn, GameMap gameMap) {
        states.push(new GameState(gameMap, turn));

        currentStrategy = getBestStrategy();
        Utils.log("Current strategy: " + currentStrategy.getStrategyName());

        return currentStrategy;
    }

    public void rollbackToDefaultStrategy(ArrayList<Move> moveList) {
        Utils.logError("Rolling back to default strategy!");

        moveList.clear();
        currentStrategy = moveAndKillStrategy;

        try {
            currentStrategy.calculateMovements(moveList);
        } catch (StrategyException ignored) {
        }
    }

    GameState getCurrentState() {
        return states.peek();
    }

    private Strategy getBestStrategy() {
        if (I_JUST_WANNA_LULZ && getCurrentState().getTurn() >= KAMIKADZE_THRESHOLD) {
            return suicideStrategy;
        }

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

            return moveAndKillStrategy;
        }
    }

    // Returns true if we should proceed with SplitToNearestPlanetsStrategy
    private boolean validateSplitStrategy() {
        GameState state = getCurrentState();
        boolean lessThanMaxTurns = state.getTurn() < SPLIT_STRATEGY_MAX_TURNS;

        if (splitToNearestPlanetsStrategy.isHunterModeActivated() && lessThanMaxTurns) {
            return true;
        }

        return state.getMyUndockedShips().size() != 0 && lessThanMaxTurns;
    }

    // Returns true if we should switch to FleeStrategy
    private boolean validateFleeStrategy() {
        GameState state = getCurrentState();

        if (state.getNumberOfPlayers() != PLAYERS_GAME_4 || state.getNumberOfActivePlayers() <= PLAYERS_GAME_2) {
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
