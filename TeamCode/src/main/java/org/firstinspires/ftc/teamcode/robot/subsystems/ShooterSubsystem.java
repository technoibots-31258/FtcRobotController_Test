package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class ShooterSubsystem extends SubsystemBase {

    private final DcMotorEx shooter1;
    private final DcMotorEx shooter2;

    private final PIDController pidController;

    // Velocidade desejada em ticks por segundo
    private static final double TARGET_VELOCITY = 2080.0;

    // PID inicial
    private static final double KP = 0.0005;
    private static final double KI = 0.00001;
    private static final double KD = 0.00001;

    private boolean enabled = false;

    public ShooterSubsystem(HardwareMap hardwareMap) {

        shooter1 = hardwareMap.get(
                DcMotorEx.class,
                "Shooter1"
        );

        shooter2 = hardwareMap.get(
                DcMotorEx.class,
                "Shooter"
        );

        shooter1.setDirection(
                DcMotor.Direction.REVERSE
        );

        shooter2.setDirection(
                DcMotor.Direction.FORWARD
        );

        shooter1.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.FLOAT
        );

        shooter2.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.FLOAT
        );

        shooter1.setMode(
                DcMotor.RunMode.RUN_USING_ENCODER
        );

        shooter2.setMode(
                DcMotor.RunMode.RUN_USING_ENCODER
        );

        pidController = new PIDController(
                KP,
                KI,
                KD
        );
    }

    public void startShooter() {
        enabled = true;
    }

    public void stopShooter() {

        enabled = false;

        shooter1.setPower(0);
        shooter2.setPower(0);

        pidController.reset();
    }

    @Override
    public void periodic() {

        if (!enabled) {
            return;
        }

        double velocity1 =
                Math.abs(shooter1.getVelocity());

        double velocity2 =
                Math.abs(shooter2.getVelocity());

        double currentVelocity =
                (velocity1 + velocity2) / 2.0;

        double output =
                pidController.calculate(
                        currentVelocity,
                        TARGET_VELOCITY
                );

        output = Math.max(
                -1.0,
                Math.min(1.0, output)
        );

        shooter1.setPower(output);
        shooter2.setPower(output);
    }

    public double getVelocity1() {
        return Math.abs(shooter1.getVelocity());
    }

    public double getVelocity2() {
        return Math.abs(shooter2.getVelocity());
    }

    public double getAverageVelocity() {

        return (
                getVelocity1()
                        + getVelocity2()
        ) / 2.0;
    }

    public double getTargetVelocity() {
        return TARGET_VELOCITY;
    }

    public boolean isAtTargetVelocity() {

        double currentVelocity = getAverageVelocity();

        double targetForShooting = TARGET_VELOCITY / 2.0;

        double tolerance = targetForShooting * 0.10;

        return currentVelocity >= targetForShooting - tolerance
                && currentVelocity <= targetForShooting + tolerance;
    }

    public void reverse() {

        shooter1.setPower(-1.0);
        shooter2.setPower(-1.0);
    }
}