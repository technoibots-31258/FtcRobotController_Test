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
                                new Pose(57.200, 10.000),
                                new Pose(84.256, 7.953)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(90))
                .addPath(
                        new BezierLine(
                                new Pose(84.256, 7.953),
                                new Pose(83.976, 26.107)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierLine(
                                new Pose(83.976, 26.107),
                                new Pose(47.593, 17.144)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(180))
                .addPath(
                        new BezierLine(
                                new Pose(47.593, 17.144),
                                new Pose(73.969, 30.986)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                .addPath(
                        new BezierLine(
                                new Pose(73.969, 30.986),
                                new Pose(49.126, -11.000)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(90))
                .build();
    }
}