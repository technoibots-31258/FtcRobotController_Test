package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;

public class IntakeCommand extends CommandBase {

    private final IntakeSubsystem intakeSubsystem;
    private final double power;

    public IntakeCommand(
            IntakeSubsystem intakeSubsystem,
            double power
    ) {

        this.intakeSubsystem = intakeSubsystem;
        this.power = power;

        addRequirements(intakeSubsystem);
    }

    @Override
    public void initialize() {
        intakeSubsystem.intake(power);
    }

    @Override
    public void execute() {
        intakeSubsystem.intake(power);
    }

    @Override
    public void end(boolean interrupted) {
        intakeSubsystem.stop();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}