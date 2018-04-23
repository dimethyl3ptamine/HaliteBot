import dmt.Game;
import hlt.*;

import java.util.*;

public class MyBot {

    private static final String BOT_NAME = "_Nag1bAt0r_";

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize(BOT_NAME);
        final ArrayList<Move> moveList = new ArrayList<>();

        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            Game.calculateMovements(gameMap, moveList);
            Networking.sendMoves(moveList);
        }
    }

}
