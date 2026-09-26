package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import java.util.function.DoubleSupplier;

import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;

public class DriveCommand extends CommandBase {

    private final MecanumDriveSubsystem driveSubsystem;

    private final DoubleSupplier ySupplier;
    private final DoubleSupplier xSupplier;
    private final DoubleSupplier rxSupplier;

    public DriveCommand(
            MecanumDriveSubsystem driveSubsystem,
            DoubleSupplier ySupplier,
            DoubleSupplier xSupplier,
            DoubleSupplier rxSupplier
    ) {

        this.driveSubsystem = driveSubsystem;

        this.ySupplier = ySupplier;
        this.xSupplier = xSupplier;
        this.rxSupplier = rxSupplier;

        addRequirements(driveSubsystem);
    }

    @Override
    public void execute() {

        // Frente / trás
        double y =
                -ySupplier.getAsDouble();

        // Esquerda / direita
        double x =
                xSupplier.getAsDouble();

        // Rotação
        double rx =
                rxSupplier.getAsDouble();

        driveSubsystem.drive(
                y,
                x,
                rx
        );
    }

    @Override
    public void end(boolean interrupted) {
        driveSubsystem.stop();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}