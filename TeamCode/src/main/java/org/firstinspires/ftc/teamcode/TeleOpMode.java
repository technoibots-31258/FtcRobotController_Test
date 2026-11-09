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

        // Atualiza os botões dos dois controles antes do scheduler
        robotContainer.update();

        // Roda o CommandScheduler
        super.run();

        // =========================
        // TELEMETRIA
        // =========================

        telemetry.addData(
                "Modo",
                robotContainer.isTwoControllers() ? "2 controles" : "1 controle"
        );

        telemetry.addData(
                "Flor",
                robotContainer.flowerSubsystem.isUp() ? "Em cima" : "No chao"
        );

        telemetry.addData(
                "Shooter 1 (ticks/s)",
                "%.0f",
                robotContainer.shooterSubsystem.getVelocity1()
        );

        telemetry.addData(
                "Shooter 2 (ticks/s)",
                "%.0f",
                robotContainer.shooterSubsystem.getVelocity2()
        );



        telemetry.update();
    }
}