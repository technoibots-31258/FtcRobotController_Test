package org.firstinspires.ftc.teamcode.autonomous.paths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class ColetaPath {

    public static final double UM_METRO_POL = 100.0 / 2.54; // ≈ 39,37 pol

    // Ponto inicial, usado também pelo AutoPrincipal no setStartingPose
    public static final Pose START_POSE = new Pose(56.000, 8.000, Math.toRadians(90));

    public PathChain MainChain;

    public ColetaPath(Follower follower) {
        MainChain = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                new Pose(START_POSE.getX(), START_POSE.getY()),
                                new Pose(START_POSE.getX(), START_POSE.getY() + UM_METRO_POL)
                        )
                )
                .setConstantHeadingInterpolation(START_POSE.getHeading())
                .build();
    }
}