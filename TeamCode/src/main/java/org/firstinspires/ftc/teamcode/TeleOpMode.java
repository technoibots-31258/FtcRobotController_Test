package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.RobotContainer;

@TeleOp(name = "TeleOp - SEM PID")
public class TeleOpMode extends CommandOpMode {

    private RobotContainer robotContainer;

    @Override
    public void initialize() {

        // gamepad1 = controle 1 (driver) | gamepad2 = controle 2 (operator)
        robotContainer = new RobotContainer(
                hardwareMap,
                gamepad1,
                gamepad2
        );

        telemetry.addData("Status", "Mecanum inicializado");
        telemetry.addData("Modo", "1 controle (Share + Options troca)");
        telemetry.update();
    }

    @Override
    public void run() {

        robotContainer.update();
        super.run();

        telemetry.addData(
                "Modo",
                robotContainer.isTwoControllers() ? "2 controles" : "1 controle"
        );

        telemetry.addData(
                "TARGET (D-pad +/-20)",
                "%.0f",
                robotContainer.shooterSubsystem.getTarget()
        );

        telemetry.addData(
                "Shooter 1 vel / erro",
                "%.0f / %.0f",
                robotContainer.shooterSubsystem.getVelocity1(),
                robotContainer.shooterSubsystem.getError1()
        );

        telemetry.addData(
                "Shooter 2 vel / erro",
                "%.0f / %.0f",
                robotContainer.shooterSubsystem.getVelocity2(),
                robotContainer.shooterSubsystem.getError2()
        );

        telemetry.addData(
                "Potencia 1 / 2",
                "%.2f / %.2f",
                robotContainer.shooterSubsystem.getPower1(),
                robotContainer.shooterSubsystem.getPower2()
        );

        telemetry.addData(
                "Pronto para chutar",
                robotContainer.shooterSubsystem.isReadyToShoot()
        );

        telemetry.update();
    }
}