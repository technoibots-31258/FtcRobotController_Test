package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.Encoder;
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Constants {

    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(16) // TROQUE: peso real do robô em kg
            .translationalPIDFCoefficients(new PIDFCoefficients(0.15, 0, 0, 0.015))
            .headingPIDFCoefficients(new PIDFCoefficients(1.0, 0, 0, 0.01))
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.02, 0, 0.0001, 0.6, 0.01));

    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .leftFrontMotorName("frontleft")
            .rightFrontMotorName("frontright")
            .leftRearMotorName("backleft")
            .rightRearMotorName("backright")
            // PROVISÓRIO: ajustar depois do teste de direção dos motores
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD    );

    public static DriveEncoderConstants localizerConstants = new DriveEncoderConstants()
            .leftFrontMotorName("frontleft")
            .rightFrontMotorName("frontright")
            .leftRearMotorName("backleft")
            .rightRearMotorName("backright")
            // PROVISÓRIO: confirmar no Localization Test
            .leftFrontEncoderDirection(Encoder.REVERSE)
                .rightFrontEncoderDirection(Encoder.REVERSE)
            .leftRearEncoderDirection(Encoder.FORWARD)
            .rightRearEncoderDirection(Encoder.FORWARD)
            .robotWidth(6.29)
            .robotLength(8.66)
            // Valores teóricos: recalibrar com Forward/Lateral/Turn Tuner (Etapa 3)
            .forwardTicksToInches(0.0016256468028606383)
            .strafeTicksToInches(0.0036459422058846094)
            .turnTicksToInches(0.008000478693666196);

    public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 1, 1);

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .mecanumDrivetrain(driveConstants)
                .driveEncoderLocalizer(localizerConstants)
                .pathConstraints(pathConstraints)
                .build();
    }
}