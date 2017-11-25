package com.aci.student24.tanks;

import java.util.List;
import java.util.stream.Collectors;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Base;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

public class TankPlayer implements Algorithm {
    private int teamId;
    private static int bestX;
    private static int bestY;
    private static Base homeBase;
    private static Base enemyBase;
    private static boolean flag;

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
                    return new TankMove(tank.getId(), tank.getDir(), true);
                }).collect(Collectors.toList());
    }

    private TankMove move(Object danger, int x, int y) {
        if (danger == null) {

        }

    }

    private void init(MapState mapState) {
        for (Base base : mapState.getBases()) {
            if (base.getTeamId() != teamId) {
                homeBase = base;
            } else {
                enemyBase = base;
            }
        }

        bestX = ;
        flag = true;
    }
}
