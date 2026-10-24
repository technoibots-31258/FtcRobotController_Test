
package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

public class ShooterCommand extends CommandBase {

    private final ShooterSubsystem shooterSubsystem;

    public ShooterCommand(ShooterSubsystem shooterSubsystem) {
        this.shooterSubsystem = shooterSubsystem;
        addRequirements(shooterSubsystem);
    }

    @Override
    public void initialize() {
        shooterSubsystem.startPID();
    }

    @Override
    public void execute() {
        // O PID é atualizado pelo periodic()
        // do ShooterSubsystem.
    }

    @Override
    public void end(boolean interrupted) {
        shooterSubsystem.stopShooter();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
