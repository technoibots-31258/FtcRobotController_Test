package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.RobotContainer;

@TeleOp(name = "TeleOp - Drive Base")
public class TeleOpMode extends CommandOpMode {

    private RobotContainer robotContainer;

    @Override
    public void initialize() {

        GamepadEx driverGamepad =
                new GamepadEx(gamepad1);

        robotContainer =
                new RobotContainer(
                        hardwareMap,
                        driverGamepad
                );

        telemetry.addData(
                "Status",
                "Mecanum inicializado"
        );

        telemetry.update();
    }

    @Override
    public void run() {

        super.run();


        telemetry.addData(
                "Shooter 1",
                "%.0f",
                robotContainer.shooterSubsystem.getVelocity1()
        );

        telemetry.addData(
                "Shooter 2",
                "%.0f",
                robotContainer.shooterSubsystem.getVelocity2()
        );

        telemetry.addData(
                "Media",
                "%.0f",
                robotContainer.shooterSubsystem.getAverageVelocity()
        );

        telemetry.addData(
                "Alvo",
                "%.0f",
                robotContainer.shooterSubsystem.getTargetVelocity()
        );

        telemetry.addData(
                "RPM OK",
                robotContainer.shooterSubsystem.isAtTargetVelocity()
        );



        telemetry.update();

    }
}