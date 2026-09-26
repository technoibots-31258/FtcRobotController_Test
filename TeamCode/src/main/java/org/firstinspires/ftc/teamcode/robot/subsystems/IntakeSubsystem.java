package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeSubsystem extends SubsystemBase {

    private final DcMotor intake;

    public IntakeSubsystem(HardwareMap hardwareMap) {

        intake = hardwareMap.get(
                DcMotor.class,
                "intake"
        );

        intake.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );
    }

    public void intake(double power) {
        intake.setPower(power);
    }

    public void stop() {
        intake.setPower(0);
    }
}