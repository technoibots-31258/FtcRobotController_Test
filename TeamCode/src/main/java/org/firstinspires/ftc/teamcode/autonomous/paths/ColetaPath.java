package org.firstinspires.ftc.teamcode.autonomous.paths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class ColetaPath {

    public PathChain MainChain;


    public ColetaPath(Follower follower) {

        MainChain = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                new Pose(56.000, 8.000),
                                new Pose(56.256, 60.052)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(180))
                .addPath(
                        new BezierLine(
                                new Pose(56.256, 60.052),
                                new Pose(47.500, 59.860)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierLine(
                                new Pose(47.500, 59.860),
                                new Pose(63.736, 66.855)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(-90))
                .addPath(
                        new BezierLine(
                                new Pose(63.736, 66.855),
                                new Pose(85.035, 27.295)
                        )
                )
                .setTangentHeadingInterpolation()
                .build();
    }
}