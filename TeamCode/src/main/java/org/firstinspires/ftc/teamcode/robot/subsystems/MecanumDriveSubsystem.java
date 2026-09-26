package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.Constants.DriveConstants;

/**
 * Subsystem responsável pelo drivetrain mecanum.
 */
public class MecanumDriveSubsystem extends SubsystemBase {

    private final DcMotor frontRight;
    private final DcMotor frontLeft;
    private final DcMotor backRight;
    private final DcMotor backLeft;


    public MecanumDriveSubsystem(HardwareMap hardwareMap) {

        frontRight = hardwareMap.get(
                DcMotor.class,
                DriveConstants.FRONT_RIGHT_NAME
        );

        frontLeft = hardwareMap.get(
                DcMotor.class,
                DriveConstants.FRONT_LEFT_NAME
        );

        backRight = hardwareMap.get(
                DcMotor.class,
                DriveConstants.BACK_RIGHT_NAME
        );

        backLeft = hardwareMap.get(
                DcMotor.class,
                DriveConstants.BACK_LEFT_NAME
        );


        // Direção dos motores
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.FORWARD);
        backLeft.setDirection(DcMotor.Direction.REVERSE);


        // Brake
        frontRight.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        frontLeft.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        backRight.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        backLeft.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );
    }


    /**
     * Controle robot-centric.
     *
     * @param y frente/trás
     * @param x strafe
     * @param rx rotação
     */
    public void drive(double y, double x, double rx) {

        double frontLeftPower = y - x - rx;
        double backLeftPower = y + x - rx;
        double frontRightPower = y - x + rx;
        double backRightPower = y + x + rx;


        // Normalização
        double max = Math.max(
                1.0,
                Math.max(
                        Math.abs(frontLeftPower),
                        Math.max(
                                Math.abs(backLeftPower),
                                Math.max(
                                        Math.abs(frontRightPower),
                                        Math.abs(backRightPower)
                                )
                        )
                )
        );


        frontLeft.setPower(frontLeftPower / max);
        backLeft.setPower(backLeftPower / max);
        frontRight.setPower(frontRightPower / max);
        backRight.setPower(backRightPower / max);
    }


    public void stop() {

        frontRight.setPower(0);
        frontLeft.setPower(0);
        backRight.setPower(0);
        backLeft.setPower(0);
    }


    public double getFrontRightPower() {
        return frontRight.getPower();
    }

    public double getFrontLeftPower() {
        return frontLeft.getPower();
    }

    public double getBackRightPower() {
        return backRight.getPower();
    }

    public double getBackLeftPower() {
        return backLeft.getPower();
    }
}