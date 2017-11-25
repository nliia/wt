package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Base;
import com.aci.student24.api.tanks.objects.Brick;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Shell;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class TankPlayer implements Algorithm {
    private int teamId;
    private static int bestX;
    private static int bestY;
    private static Base homeBase;
    private static Base enemyBase;
    private static boolean flag;
    private static final int RADIUS = 6;

    @Override
    public void setMyId(final int id) {
        teamId = id;

    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        if (!flag)
            init(mapState);

        return mapState.getTanks(teamId).stream()
                .map(tank -> {
                    return checkField(tank, findBlocks(mapState,tank.getPosition()),findEnemyTanks(mapState,tank.getPosition()),findShells(mapState,tank.getPosition()),mapState);
                }).collect(Collectors.toList());
    }

    private void init(MapState mapState) {
        for (Base base : mapState.getBases()) {
            if (base.getTeamId() != teamId) {
                homeBase = base;
            } else {
                enemyBase = base;
            }
        }

        flag = true;
    }

    private TankMove checkField(Tank currentTank, List<Position> blockList, List<Tank> tanks, List<Shell> shells, MapState mapState) {
        Position currPosition = currentTank.getPosition();
        Position nextPosition = getNextPost(currentTank);
        HashSet<Shell> blockedShells = new HashSet<>();
        for (Shell shell : shells) {
            if (shell.getPosition().getX() == nextPosition.getX()) {
                for (Position position : blockList) {
                    if (position.getX() == nextPosition.getX() && Math.abs(shell.getPosition().getY() - nextPosition.getY()) > Math.abs(shell.getPosition().getY() - position.getPosition().getY())) {
                        blockedShells.add(shell);
                    }
                    if (position.getY() == nextPosition.getY() && Math.abs(shell.getPosition().getX() - nextPosition.getX()) > Math.abs(shell.getPosition().getX() - position.getPosition().getX())) {
                        blockedShells.add(shell);
                    }
                }
            }
        }
        shells.removeAll(blockedShells);
        if (shells.isEmpty()) {
            Position obj = getPosition(nextPosition, mapState);
            if (obj instanceof Brick || (obj instanceof Tank && ((Tank) obj).getTeamId() != teamId)) {
                return new TankMove(currentTank.getId(), currentTank.getDir(), true);
            } else if (obj == null) {
                return new TankMove(currentTank.getId(), currentTank.getDir(), false);
            }
        }
        return shootMove(tanks, currentTank, blockList);
    }

    private Position getNextPost(Tank currentTank) {
        Position currPosition = currentTank.getPosition();
        if (currentTank.getDir() == 1) {
            return new Position(currPosition.getX(), currPosition.getY() + 1);
        }
        if (currentTank.getDir() == 2) {
            return new Position(currPosition.getX() + 1, currPosition.getY());
        }
        if (currentTank.getDir() == 3) {
            return new Position(currPosition.getX(), currPosition.getY() - 1);
        }
        if (currentTank.getDir() == 4) {
            return new Position(currPosition.getX() - 1, currPosition.getY());
        }
        return null;
    }


    private Position getPosition(Position position, MapState mapState) {
        List<? extends Position> positions = mapState.getBricks();
        for (Position objPosition : positions) {
            if (position.equals(objPosition)) {
                return objPosition;
            }
        }
        positions = mapState.getTanks();
        for (Position objPosition : positions) {
            if (position.equals(objPosition)) {
                return objPosition;
            }
        }
        positions = mapState.getIndestructibles();
        for (Position objPosition : positions) {
            if (position.equals(objPosition)) {
                return objPosition;
            }
        }
        return null;
    }


    private List<Position> findBlocks(MapState mapState, Position position) {
        List<Brick> bricks = mapState.getBricks();
        List<Position> results = new ArrayList<>();
        for (Brick brick : bricks) {
            if ((brick.getPosition().getX() < position.getX() + RADIUS || brick.getPosition().getX() > position.getX() - RADIUS) && brick.getPosition().getY() == position.getY()) {
                results.add(brick);
            }

            if ((brick.getPosition().getY() < position.getY() + RADIUS || brick.getPosition().getY() > position.getY() - RADIUS) && brick.getPosition().getX() == position.getX()) {
                results.add(brick);
            }
        }

        return results;
    }


    private List<Tank> findEnemyTanks(MapState mapState, Position position) {
        List<Tank> results = new ArrayList<>();
        List<Tank> allTanks = mapState.getTanks();
        List<Tank> tanks = new ArrayList<>();

        for (Tank tank : allTanks) {
            if (tank.getTeamId() != teamId) {
                tanks.add(tank);
            }
        }

        for (Tank tank : tanks) {
            if ((((tank.getPosition().getX() <= position.getX() + RADIUS && tank.getPosition().getX() >= position.getX() - RADIUS) && tank.getPosition().getY() == position.getY()))
                    || ((tank.getPosition().getY() <= position.getY() + RADIUS && tank.getPosition().getY() >= position.getY() - RADIUS) && tank.getPosition().getX() == position.getX())) {
                results.add(tank);
            }

        }
        return results;
    }


    private List<Shell> findShells(MapState mapState, Position position) {
        List<Shell> results = new ArrayList<>();
        List<Shell> shells = mapState.getShells();
        for (Shell shell : shells) {
            if ((shell.getPosition().getX() <= position.getX() + RADIUS && shell.getPosition().getX() > position.getX() && shell.getPosition().getY() == position.getY() && shell.getDir() == Direction.LEFT)
                    || (shell.getPosition().getX() >= position.getX() - RADIUS && shell.getPosition().getX() < position.getX() && shell.getPosition().getY() == position.getY() && shell.getDir() == Direction.RIGHT)
                    || (shell.getPosition().getY() <= position.getY() + RADIUS && shell.getPosition().getY() > position.getY() && shell.getPosition().getX() == position.getX() && shell.getDir() == Direction.DOWN)
                    || (shell.getPosition().getY() >= position.getY() - RADIUS && shell.getPosition().getY() < position.getY() && shell.getPosition().getX() == position.getX() && shell.getDir() == Direction.UP)) {

                results.add(shell);
            }

        }
        return results;
    }

    private TankMove shootMove(List<Tank> tanks, Tank tank, List<Position> blocks) {
        if (!tanks.isEmpty()) {
            Tank enemyTank = tanks.get(0);
        }
        return null;
    }

}
