package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;

public class IntakeHoldCommand extends CommandBase {

    private final IntakeSubsystem intakeSubsystem;
    private final BufferSubsystem bufferSubsystem;

    public IntakeHoldCommand(
            IntakeSubsystem intakeSubsystem,
            BufferSubsystem bufferSubsystem
    ) {
        this.intakeSubsystem = intakeSubsystem;
        this.bufferSubsystem = bufferSubsystem;

        addRequirements(
                intakeSubsystem,
                bufferSubsystem
        );
    }

    @Override
    public void initialize() {
        intakeSubsystem.intake(1.0);
        bufferSubsystem.buffer(-1.0);
    }

    @Override
    public void execute() {
        intakeSubsystem.intake(1.0);
        bufferSubsystem.buffer(-5.0);
    }

    @Override
    public void end(boolean interrupted) {
        intakeSubsystem.stop();
        bufferSubsystem.stop();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}