//package org.firstinspires.ftc.teamcode.autonomous.paths;
//
//import com.pedropathing.follower.Follower;
//import com.pedropathing.geometry.BezierLine;
//import com.pedropathing.geometry.Pose;
//import com.pedropathing.paths.PathChain;
//
//
//public class TestPaths {
//
//
//    public final Pose startPose =
//            new Pose(
//                    20,
//                    20,
//                    Math.toRadians(0)
//            );
//
//
//    public final Pose endPose =
//            new Pose(
//                    59.37,
//                    20,
//                    Math.toRadians(0)
//            );
//
//
//    public PathChain andarUmMetro;
//
//
//    public TestPaths(Follower follower) {
//
//        andarUmMetro = follower.pathBuilder()
//
//                .addPath(
//                        new BezierLine(
//                                startPose,
//                                endPose
//                        )
//                )
//
//                .setLinearHeadingInterpolation(
//                        startPose.getHeading(),
//                        endPose.getHeading()
//                )
//
//                .build();
//    }
//}