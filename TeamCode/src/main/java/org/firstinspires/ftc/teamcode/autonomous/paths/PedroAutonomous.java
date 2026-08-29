package org.firstinspires.ftc.teamcode.autonomous.paths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class PedroAutonomous {

    public Pose startPose;

    public PathChain andarUmMetro;


    public PedroAutonomous(Follower follower) {

        // Posição inicial
        startPose = new Pose(
                56.000,
                8.000,
                Math.toRadians(90)
        );


        // Path criado no Pedro Pathing Visualizer
        andarUmMetro = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                new Pose(56.000, 8.000),
                                new Pose(56.000, 42.909)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(180))
                .addPath(
                        new BezierLine(
                                new Pose(56.000, 42.909),
                                new Pose(53.185, 1.535)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(90))
                .build();
    }
}