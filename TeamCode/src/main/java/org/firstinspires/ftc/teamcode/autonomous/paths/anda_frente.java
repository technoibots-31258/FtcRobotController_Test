package org.firstinspires.ftc.teamcode.autonomous.paths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class anda_frente {

    public final PathChain caminho;

    public anda_frente(Follower follower) {

        caminho = follower.pathBuilder()

                .addPath(
                        new BezierLine(
                                new Pose(
                                        84.256,
                                        7.953
                                ),

                                new Pose(
                                        57.200,
                                        10.000
                                )
                        )
                )

                .setLinearHeadingInterpolation(
                        Math.toRadians(90),
                        Math.toRadians(90)
                )

                .build();
    }
}