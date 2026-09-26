package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class BufferSubsystem extends SubsystemBase {

    private final DcMotor buffer;

    public BufferSubsystem(HardwareMap hardwareMap) {

        buffer = hardwareMap.get(
                DcMotor.class,
                "buffer"
        );

        buffer.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );
    }

    public void buffer(double power) {
        buffer.setPower(power);
    }

    public void stop() {
        buffer.setPower(0);
    }
}