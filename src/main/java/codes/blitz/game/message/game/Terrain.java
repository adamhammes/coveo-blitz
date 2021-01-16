package codes.blitz.game.message.game;

import codes.blitz.game.message.exception.PositionOutOfMapException;

import java.util.*;
import java.util.stream.Collectors;

public class Terrain {
    private GameMessage gameMessage;
    private Unit unit;
    private Map<Position, List<Position>> fastestPath;

    public Terrain(GameMessage gameMessage, Unit unit) {
        this.gameMessage = gameMessage;
        this.unit = unit;

        this.buildFastestPath();
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
                    queue.add(neighbor);
                    var pathCopy = new ArrayList<>(pathToPosition);
                    pathCopy.add(neighbor);
                    fastestPath.put(neighbor, pathCopy);
                }
            }
        }
    }

    public List<Position> neighbors(Position p) {
        var positions = new ArrayList<Position>();

        positions.add(new Position(p.getX() - 1, p.getY()));
        positions.add(new Position(p.getX() + 1, p.getY()));
        positions.add(new Position(p.getX(), p.getY() - 1));
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

    public int distanceTo(Position p) {
        return fastestPath.get(p).size() - 1;
    }

    public List<Position> pathTo(Position p) {
        return fastestPath.get(p);
    }

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
