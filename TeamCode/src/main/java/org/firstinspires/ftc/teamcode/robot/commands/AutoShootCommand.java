package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

/**
 * Chute para o autônomo, com a mesma lógica do ShooterCommand do teleop:
 *
 *  1) liga o shooter e espera os DOIS motores chegarem no alvo;
 *  2) ao ficar pronto, liga intake e buffer e MANTÉM ligados
 *     (não desliga se o RPM oscilar);
 *  3) termina sozinho depois do tempo de tiro e desliga tudo.
 */
public class AutoShootCommand extends CommandBase {

    // Mesmo sinal do ShooterCommand do teleop
    private static final double FEED_INTAKE_POWER = -1.0;
    private static final double FEED_BUFFER_POWER = -1.0;

    private final ShooterSubsystem shooter;
    private final IntakeSubsystem intake;
    private final BufferSubsystem buffer;

    private final double spinUpTimeoutSec;
    private final double feedTimeSec;

    private final ElapsedTime timer = new ElapsedTime();
    private boolean feedPhase = false;   // já começou a contar o tempo de tiro
    private boolean feeding = false;     // intake e buffer ligados

    public AutoShootCommand(
            ShooterSubsystem shooter,
            IntakeSubsystem intake,
            BufferSubsystem buffer,
            double spinUpTimeoutSec,
            double feedTimeSec
    ) {
        this.shooter = shooter;
        this.intake = intake;
        this.buffer = buffer;
        this.spinUpTimeoutSec = spinUpTimeoutSec;
        this.feedTimeSec = feedTimeSec;

        addRequirements(shooter, intake, buffer);
    }

    @Override
    public void initialize() {
        shooter.startPID();
        timer.reset();
        feedPhase = false;
        feeding = false;
    }

    @Override
    public void execute() {

        boolean ready = shooter.isReadyToShoot();

        // Começa a contar o tempo de tiro quando fica pronto
        // (ou quando estoura o tempo máximo de espera).
        if (!feedPhase && (ready || timer.seconds() >= spinUpTimeoutSec)) {
            feedPhase = true;
            timer.reset();
        }

        // Ficou pronto: liga e mantém ligado, igual ao teleop.
        if (ready) {
            feeding = true;
        }

        if (feeding) {
            intake.intake(FEED_INTAKE_POWER);
            buffer.buffer(FEED_BUFFER_POWER);
        }
    }

    /** Para telemetria: intake e buffer estão ligados para chutar. */
    public boolean isFeeding() {
        return feeding;
    }

    @Override
    public boolean isFinished() {
        return feedPhase && timer.seconds() >= feedTimeSec;
    }

    @Override
    public void end(boolean interrupted) {
        shooter.stopShooter();
        intake.stop();
        buffer.stop();
    }
}