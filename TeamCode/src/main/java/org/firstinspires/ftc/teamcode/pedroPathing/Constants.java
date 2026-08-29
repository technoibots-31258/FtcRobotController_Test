package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.control.FilteredPIDFCoefficients;
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

    public static FollowerConstants followerConstants =
            new FollowerConstants()
                    .mass(5.0) // TROQUE pela massa real do robô em kg (peso na balança)

                    .translationalPIDFCoefficients(new PIDFCoefficients(
                            0.1, 0, 0, 0.015
                    ))

                    .headingPIDFCoefficients(new PIDFCoefficients(
                            1.0, 0, 0, 0.01
                    ))

                    .drivePIDFCoefficients(new FilteredPIDFCoefficients(
                            0.02, 0, 0.0001, 0.6, 0.01
                    ));


    public static MecanumConstants driveConstants =
            new MecanumConstants()

                    .maxPower(1)

                    .rightFrontMotorName("frontright")
                    .rightRearMotorName("backright")
                    .leftRearMotorName("backleft")
                    .leftFrontMotorName("frontleft")

                    .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
                    .leftRearMotorDirection(DcMotorSimple.Direction.FORWARD)
                    .rightFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
                    .rightRearMotorDirection(DcMotorSimple.Direction.REVERSE);


    public static DriveEncoderConstants localizerConstants =
            new DriveEncoderConstants()

                    .rightFrontMotorName("frontright")
                    .rightRearMotorName("backright")
                    .leftRearMotorName("backleft")
                    .leftFrontMotorName("frontleft")

                    .leftFrontEncoderDirection(Encoder.REVERSE)
                    .leftRearEncoderDirection(Encoder.FORWARD)
                    .rightFrontEncoderDirection(Encoder.REVERSE)
                    .rightRearEncoderDirection(Encoder.REVERSE)

                    .robotWidth(15)  // TROQUE pela largura real do chassi em polegadas
                    .robotLength(16) // TROQUE pelo comprimento real do chassi em polegadas

                    // Valores calculados p/ REV HD Hex Motor 15:1 nominal + roda mecanum 75mm
                    // OBRIGATÓRIO rodar o Tuning OpMode (forward/lateral/turn) para calibrar de verdade
                    .forwardTicksToInches(0.002876454511721299)
                    .strafeTicksToInches(0.004244221823999264)
                    .turnTicksToInches(0.0134688313252435);


    public static PathConstraints pathConstraints =
            new PathConstraints(
                    0.99,
                    100,
                    1,
                    1
            );


    public static Follower createFollower(HardwareMap hardwareMap) {

        return new FollowerBuilder(followerConstants, hardwareMap)

                .mecanumDrivetrain(driveConstants)

                .driveEncoderLocalizer(localizerConstants)

                .pathConstraints(pathConstraints)

                .build();
    }
}