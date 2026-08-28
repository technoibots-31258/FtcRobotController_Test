package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;

import java.util.function.DoubleSupplier;


/**
 * Command responsável pelo controle manual do drivetrain.
 */
public class DriveCommand extends CommandBase {

    private final MecanumDriveSubsystem driveSubsystem;

    private final DoubleSupplier ySupplier;
    private final DoubleSupplier xSupplier;
    private final DoubleSupplier rotationSupplier;


    public DriveCommand(

            MecanumDriveSubsystem driveSubsystem,

            DoubleSupplier ySupplier,

            DoubleSupplier xSupplier,

            DoubleSupplier rotationSupplier
    ) {

        this.driveSubsystem = driveSubsystem;

        this.ySupplier = ySupplier;
        this.xSupplier = xSupplier;
        this.rotationSupplier = rotationSupplier;


        // Este command usa o drivetrain
        addRequirements(driveSubsystem);
    }


    @Override
    public void execute() {

        double y = ySupplier.getAsDouble();
        double x = xSupplier.getAsDouble();
        double rotation = rotationSupplier.getAsDouble();


        driveSubsystem.drive(y, x, rotation);
    }


    @Override
    public void end(boolean interrupted) {

        driveSubsystem.stop();
    }
}