package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;

public class BufferCommand extends CommandBase {

    private final BufferSubsystem bufferSubsystem;
    private final double power;

    public BufferCommand(
            BufferSubsystem bufferSubsystem,
            double power
    ) {

        this.bufferSubsystem = bufferSubsystem;
        this.power = power;

        addRequirements(bufferSubsystem);
    }

    @Override
    public void initialize() {
        bufferSubsystem.buffer(power);
    }

    @Override
    public void execute() {
        bufferSubsystem.buffer(power);
    }

    @Override
    public void end(boolean interrupted) {
        bufferSubsystem.stop();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}