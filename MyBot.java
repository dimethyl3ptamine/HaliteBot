import dmt.Strategy;
import dmt.StrategyException;
import dmt.StrategyHelper;
import dmt.Utils;
import hlt.*;

import java.util.*;

public class MyBot {

    private static final String BOT_NAME = "_Nag1bAt0r_";

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize(BOT_NAME);
        final ArrayList<Move> moveList = new ArrayList<>();

        Log.log("**H***E***L***L***O**");
        Log.log("    " + BOT_NAME);
        Log.log("**T***H***E***R***E**");

        int turn = 0;
        StrategyHelper.HELPER.init(gameMap);

        while (true) {
            turn++;
            moveList.clear();
            networking.updateMap(gameMap);

            Strategy strategy = StrategyHelper.HELPER.getStrategy(turn, gameMap);
            try {
                strategy.calculateMovements(gameMap, moveList);
            } catch (StrategyException e) {
                Utils.log(e.getMessage(), true);
                StrategyHelper.HELPER.rollbackToDefaultStrategy(gameMap, moveList);
            }

            Networking.sendMoves(moveList);
        }
    }

}
