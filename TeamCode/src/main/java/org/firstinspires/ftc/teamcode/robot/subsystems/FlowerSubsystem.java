
package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class FlowerSubsystem extends SubsystemBase {

    private final CRServo flower;

    public FlowerSubsystem(HardwareMap hardwareMap) {
        flower = hardwareMap.get(CRServo.class, "flower1");

        flower.setPower(0);
    }

    public void setPower(double power) {
        flower.setPower(power);
    }

    public void stop() {
        flower.setPower(0);
    }
}
