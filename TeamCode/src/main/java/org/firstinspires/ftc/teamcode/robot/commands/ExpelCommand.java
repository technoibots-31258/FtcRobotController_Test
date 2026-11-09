package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

/**
 * Expele as bolinhas enquanto o botão estiver pressionado:
 * intake, buffer e shooter giram no sentido contrário ao da coleta/chute.
 * Ao soltar o botão, tudo para.
 */
public class ExpelCommand extends CommandBase {

    // Intake e buffer: a coleta usa -1, então expelir é +1
    private static final double EXPEL_POWER = 1.0;

    // Shooter: o chute usa potência positiva, então expelir é negativo.
    // Ajuste cada roda separadamente se alguma girar para o lado errado.
    private static final double SHOOTER1_EXPEL_POWER = -1.0;
    private static final double SHOOTER2_EXPEL_POWER = -1.0;

    private final ShooterSubsystem shooterSubsystem;
    private final IntakeSubsystem intakeSubsystem;
    private final BufferSubsystem bufferSubsystem;

    public ExpelCommand(
            IntakeSubsystem intakeSubsystem,
            BufferSubsystem bufferSubsystem,
            ShooterSubsystem shooterSubsystem
    ) {
        this.intakeSubsystem = intakeSubsystem;
        this.bufferSubsystem = bufferSubsystem;
        this.shooterSubsystem = shooterSubsystem;
        addRequirements(shooterSubsystem, intakeSubsystem, bufferSubsystem);
    }

    @Override
    public void execute() {
        intakeSubsystem.intake(EXPEL_POWER);
        bufferSubsystem.buffer(EXPEL_POWER);
        shooterSubsystem.setManualPower(
                SHOOTER1_EXPEL_POWER,
                SHOOTER2_EXPEL_POWER
        );
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