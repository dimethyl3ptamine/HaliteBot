package dmt;

import hlt.GameMap;

class State {
    private GameMap map;

    State(GameMap map) {
        this.map = map;
    }

    GameMap getMap() {
        return map;
    }

    // TODO: Instead of storing maps, it's better to store required info like ships, planets -- depends on memory size :D
}
