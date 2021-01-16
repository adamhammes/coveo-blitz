package codes.blitz.game.message.game;

import codes.blitz.game.message.exception.PositionOutOfMapException;

import java.util.*;
import java.util.stream.Collectors;

public class Terrain {
    private GameMessage gameMessage;
    private Unit unit;
    private Map<Position, List<Position>> fastestPath;
    private List<Unit> allUnits;
    private Set<Position> occupiedPositions;

    public Terrain(GameMessage gameMessage, Unit unit) {
        this.gameMessage = gameMessage;
        this.unit = unit;

        allUnits = new ArrayList<>();
        for (var crew: gameMessage.getCrews()) {
            allUnits.addAll(crew.getUnits());
        }

        occupiedPositions = allUnits.stream().map(Unit::getPosition).collect(Collectors.toSet());
        occupiedPositions.addAll(enemyBaseSquares());

        this.buildFastestPath();
    }

    private Set<Position> enemyBaseSquares() {
        var enemyPositions = new HashSet<Position>();

        for (var crew: gameMessage.getCrews()) {
            // don't eliminate our own home base
            if (crew.getId().equals(gameMessage.getCrewId())) {
                continue;
            }

            var enemyBase = crew.getHomeBase();
            var x = enemyBase.getX();
            var y = enemyBase.getY();
            for (var dx: new int[]{ 0, 1, 2, 3}) {
                for (var dy: new int[]{ 0, 1, 2, 3}) {

                    enemyPositions.add(new Position(x+dx,  y+dy));
                    enemyPositions.add(new Position(x-dx,  y+dy));
                    enemyPositions.add(new Position(x+dx, y-dy));
                    enemyPositions.add(new Position(x-dx, y-dy));
                }
            }
        }

        return enemyPositions;
    }

    private void buildFastestPath() {
        fastestPath = new HashMap<>();
        fastestPath.put(unit.getPosition(), Collections.singletonList(unit.getPosition()));

        Queue<Position> queue = new LinkedList<>(Collections.singletonList(unit.getPosition()));

        while (!queue.isEmpty()) {
            var currentPosition = queue.remove();
            var pathToPosition = fastestPath.get(currentPosition);

            for (var neighbor: neighbors(currentPosition)) {
                if (!fastestPath.containsKey(neighbor)) {
                    var pathCopy = new ArrayList<>(pathToPosition);
                    pathCopy.add(neighbor);
                    fastestPath.put(neighbor, pathCopy);
                    if (positionHasType(neighbor, TileType.EMPTY) && !occupiedPositions.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    public List<Position> neighbors(Position p) {
        var positions = new ArrayList<Position>();

        positions.add(new Position(p.getX() - 1, p.getY()));
        positions.add(new Position(p.getX(), p.getY() - 1));
        positions.add(new Position(p.getX() + 1, p.getY()));
        positions.add(new Position(p.getX(), p.getY() + 1));

        var reachablePositions = new ArrayList<Position>();
        for (var pos: positions) {
            try {
                gameMessage.getGameMap().validateTileExists(pos);
                reachablePositions.add(pos);
            } catch (PositionOutOfMapException e) {
            }
        }

        return reachablePositions;
    }

    private List<Position> getAllPositions() {
        var positions = new ArrayList<Position>();
        var mapSize = gameMessage.getGameMap().getMapSize();

        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                var position = new Position(x, y);

                try {
                    this.gameMessage.getGameMap().validateTileExists(position);
                    positions.add(position);
                } catch (PositionOutOfMapException e) {}
            }
        }

        return positions;
    }

    public List<Position> getMineablePositions() {
        var mines = positionsOfType(TileType.MINE);
        var mineNeighbors = mines.stream()
                .flatMap(m -> neighbors(m).stream())
                .filter(this::reachable)
                .filter(p -> !occupiedPositions.contains(p))
                .distinct()
                .sorted(Comparator.comparingInt(this::distanceTo));


        return mineNeighbors.collect(Collectors.toList());
    }

    public List<Position> getMineablePositions(Position fromPosition) {
        var mines = positionsOfType(TileType.MINE);
        var mineNeighbors = mines.stream()
                .flatMap(m -> neighbors(m).stream())
                .filter(p -> pathTo(fromPosition, p, new HashSet<>()) != null)
                .filter(p -> !occupiedPositions.contains(p))
                .distinct()
                .sorted(Comparator.comparingInt(this::distanceTo));


        return mineNeighbors.collect(Collectors.toList());
    }

    public boolean reachable(Position p) {
        return fastestPath.containsKey(p) && positionHasType(p, TileType.EMPTY);
    }

    public int distanceTo(Position p) {
        return fastestPath.get(p).size() - 1;
    }

    public List<Position> pathTo(Position p) {
        return fastestPath.get(p);
    }

    // Return `null` when there is no viable path
    public List<Position> pathTo(Position start, Position dest, Set<Position> restrictedPositions) {
        Map<Position, List<Position>> paths = new HashMap<>();
        paths.put(start, Collections.singletonList(start));

        Queue<Position> queue = new LinkedList<>(Collections.singletonList(start));

        while (!queue.isEmpty()) {
            var currentPosition = queue.remove();
            var pathToPosition = paths.get(currentPosition);

            for (var neighbor: neighbors(currentPosition)) {
                var pathCopy = new ArrayList<>(pathToPosition);
                pathCopy.add(neighbor);

                if (neighbor.equals(dest)) {
                    return pathCopy;
                }

                if (!paths.containsKey(neighbor)) {
                    paths.put(neighbor, pathCopy);
                    if (
                            positionHasType(neighbor, TileType.EMPTY)&&
                                    !restrictedPositions.contains(neighbor) &&
                                    !occupiedPositions.contains(neighbor)) {

                        queue.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

//    // Return a path for the current unit to position P, not using the positions in `excluding`.
//    public List<Position> pathTo(Position p, HashSet<Position> excluding) {
//
//    }

    public Position closestPosition(List<Position> positions) {
        var closestPosition = positions.stream().findFirst().orElseThrow();

        for (var position: positions) {
            if (distanceTo(position) < distanceTo(closestPosition)) {
                closestPosition = position;
            }
        }

        return closestPosition;
    }

    public boolean positionHasType(Position p, TileType t) {
        try {
            return this.gameMessage.getGameMap().getTileTypeAt(p) == t;
        } catch (PositionOutOfMapException e) {
            return false;
        }
    }

    public List<Position> positionsOfType(TileType type) {
        return getAllPositions()
                .stream()
                .filter(p -> positionHasType(p, type))
                .filter(p -> fastestPath.containsKey(p))
                .sorted(Comparator.comparingInt(this::distanceTo).thenComparingInt(Position::getX))
                .collect(Collectors.toList());

    }

    public Position closestPositionOfType(TileType type) {
        return getAllPositions()
                .stream()
                .filter(p -> positionHasType(p, type))
                .min(Comparator.comparingInt(this::distanceTo).thenComparingInt(Position::getX))
                .orElseThrow();
    }

    public boolean isNeighboring(Position p) {
        return neighbors(unit.getPosition()).contains(p);
    }
}
