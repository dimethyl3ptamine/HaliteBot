package dmt;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

class GameState {

    private static final int INITIAL_NUMBER_OF_SHIPS = 3;

    private int turn;
    private int myId;
    private int numberOfPlayers;
    private int numberOfActivePlayers;
    private GameMap gameMap;
    private Collection<Ship> allShips;
    private Collection<Ship> allMyShips;
    private Collection<Ship> myUndockedShips;
    private Collection<Ship> enemiesShips;
    private Collection<Planet> allPlanets;
    private Collection<Planet> allMyPlanets;
    private Collection<Planet> freePlanets;
    private Collection<Planet> enemiesPlanets;
    private Map<Integer, Integer> enemiesPlanetMap; // Enemy.id -> Number of planets
    private Map<Integer, Integer> enemiesShipMap;   // Enemy.id -> Number of ships

    GameState(GameMap map, int turn) {
        this.gameMap = map;
        this.turn = turn;
        myId = gameMap.getMyPlayerId();
        numberOfPlayers = gameMap.getAllPlayers().size();

        initShips();
        initPlanets();
        initEnemiesStat();
        printCommonStat();
    }

    GameMap getMap() {
        return gameMap;
    }

    int getTurn() {
        return turn;
    }

    int getMyId() {
        return myId;
    }

    int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    int getNumberOfActivePlayers() {
        return numberOfActivePlayers;
    }

    Collection<Ship> getAllShips() {
        return allShips;
    }

    Collection<Ship> getAllMyShips() {
        return allMyShips;
    }

    Collection<Ship> getEnemiesShips() {
        return enemiesShips;
    }

    Collection<Ship> getMyUndockedShips() {
        return myUndockedShips;
    }

    Collection<Planet> getAllMyPlanets() {
        return allMyPlanets;
    }

    Collection<Planet> getAllPlanets() {
        return allPlanets;
    }

    Collection<Planet> getEnemiesPlanets() {
        return enemiesPlanets;
    }

    Collection<Planet> getFreePlanets() {
        return freePlanets;
    }

    private void initShips() {
        myUndockedShips = new ArrayList<>();
        enemiesShips = new ArrayList<>();

        allMyShips = gameMap.getMyPlayer().getShips().values();
        allShips = gameMap.getAllShips();

        for (Ship ship : allShips) {
            if (ship.getOwner() != myId) {
                enemiesShips.add(ship);
            } else {
                if (ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
                    myUndockedShips.add(ship);
                }
            }
        }
    }

    private void initPlanets() {
        allMyPlanets = new ArrayList<>();
        freePlanets = new ArrayList<>();
        enemiesPlanets = new ArrayList<>();
        enemiesPlanetMap = new TreeMap<>();

        allPlanets = gameMap.getAllPlanets().values();

        for (Planet planet : allPlanets) {
            if (!planet.isOwned()) {
                freePlanets.add(planet);
            } else {
                int ownerId = planet.getOwner();
                enemiesPlanetMap.merge(ownerId, 1, (a, b) -> a + b);

                if (ownerId != myId) {
                    enemiesPlanets.add(planet);
                } else {
                    allMyPlanets.add(planet);
                }
            }
        }
    }

    private void initEnemiesStat() {
        numberOfActivePlayers = 0;
        enemiesShipMap = new TreeMap<>();

        for (Player player : gameMap.getAllPlayers()) {
            int id = player.getId();
            int ships = player.getShips().size();

            enemiesShipMap.put(id, ships);

            if (enemiesPlanetMap.get(id) != null) {
                numberOfActivePlayers++;
                continue;
            }

            if (ships >= INITIAL_NUMBER_OF_SHIPS) {
                numberOfActivePlayers++;
            }
        }
    }

    private void printCommonStat() {
        StringBuilder commonStat = new StringBuilder("Stats:\n");
        commonStat.append(String.format("\tPlanets common info: All %s - My %s - Free %s - Enemies %s\n",
                allPlanets.size(), allMyPlanets.size(), freePlanets.size(), enemiesPlanets.size()));
        commonStat.append(String.format("\tShips common info: All %s - My %s - Undocked %s - Enemies %s\n",
                allShips.size(), allMyShips.size(), myUndockedShips.size(), enemiesShips.size()));

        for (Integer player : enemiesShipMap.keySet()) {
            commonStat.append(String.format("\tPlayer %s has %s planets and %s ships\n",
                    player, enemiesPlanetMap.get(player), enemiesShipMap.get(player)));
        }

        Utils.log(commonStat.toString());
    }

}
