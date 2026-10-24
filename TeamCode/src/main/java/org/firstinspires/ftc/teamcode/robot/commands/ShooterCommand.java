
package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

import java.nio.Buffer;

public class ShooterCommand extends CommandBase {

    private final ShooterSubsystem shooterSubsystem;
    private final IntakeSubsystem intakeSubsystem;
    private final BufferSubsystem bufferSubsystem;

    public ShooterCommand(
            ShooterSubsystem shooterSubsystem,
            IntakeSubsystem intakeSubsystem,
            BufferSubsystem bufferSubsystem
    ) {
        this.shooterSubsystem = shooterSubsystem;
        this.bufferSubsystem = bufferSubsystem;
        this.intakeSubsystem = intakeSubsystem;
        addRequirements(shooterSubsystem, intakeSubsystem, bufferSubsystem);
    }



    @Override
    public void initialize() {
        shooterSubsystem.startPID();
    }

    @Override
    public void execute() {
        // O PID é atualizado pelo periodic()
        // do ShooterSubsystem.
        if(shooterSubsystem.isReadyToShoot()){
            intakeSubsystem.intake(-0.6);
            bufferSubsystem.buffer(-0.7);
        }
    }

    @Override
    public void end(boolean interrupted) {
        shooterSubsystem.stopShooter();
        intakeSubsystem.stop();
        bufferSubsystem.stop();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
