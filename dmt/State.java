package dmt;

import hlt.GameMap;

class State {
    private GameMap map;
    private int myId;

    State(GameMap map) {
        this.map = map;
        myId = map.getMyPlayerId();
    }

    GameMap getMap() {
        return map;
    }

    int getMyId() {
        return myId;
    }

    // TODO: Instead of storing maps, it's better to store required info like ships, planets --> for future usage
}
