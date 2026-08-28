package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Constants {

    public static FollowerConstants followerConstants =
            new FollowerConstants();


    public static MecanumConstants driveConstants =
            new MecanumConstants()

                    .maxPower(1)

                    .rightFrontMotorName("frontright")
                    .rightRearMotorName("backright")

                    .leftRearMotorName("backleft")
                    .leftFrontMotorName("frontleft");


    public static DriveEncoderConstants localizerConstants =
            new DriveEncoderConstants();


    public static PathConstraints pathConstraints =
            new PathConstraints(
                    0.99,
                    100,
                    1,
                    1
            );


    public static Follower createFollower(
            HardwareMap hardwareMap
    ) {

        return new FollowerBuilder(
                followerConstants,
                hardwareMap
        )

                .mecanumDrivetrain(driveConstants)

                .driveEncoderLocalizer(localizerConstants)

                .pathConstraints(pathConstraints)

                .build();
    }
}