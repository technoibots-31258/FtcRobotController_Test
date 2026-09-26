package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

public class ShooterCommand extends CommandBase {

    private final ShooterSubsystem shooterSubsystem;
    private final IntakeSubsystem intakeSubsystem;
    private final BufferSubsystem bufferSubsystem;

    // Quanto tempo o RPM precisa permanecer estável
    private static final long STABILIZATION_TIME_MS = 300;

    // Intake começa antes do buffer
    private static final long INTAKE_LEAD_TIME_MS = 100;

    private long stableSince = 0;
    private long intakeStartedAt = 0;

    private boolean shooterStable = false;
    private boolean intakeStarted = false;
    private boolean bufferStarted = false;

    public ShooterCommand(
            ShooterSubsystem shooterSubsystem,
            IntakeSubsystem intakeSubsystem,
            BufferSubsystem bufferSubsystem
    ) {
        this.shooterSubsystem = shooterSubsystem;
        this.intakeSubsystem = intakeSubsystem;
        this.bufferSubsystem = bufferSubsystem;

        addRequirements(
                shooterSubsystem,
                intakeSubsystem,
                bufferSubsystem
        );
    }

    @Override
    public void initialize() {

        stableSince = 0;
        intakeStartedAt = 0;

        shooterStable = false;
        intakeStarted = false;
        bufferStarted = false;

        shooterSubsystem.startShooter();
    }

    @Override
    public void execute() {

        /*
         * PRIMEIRA ETAPA:
         * esperar o shooter chegar ao RPM desejado.
         */
        if (!shooterStable) {

            if (shooterSubsystem.isAtTargetVelocity()) {

                if (stableSince == 0) {
                    stableSince =
                            System.currentTimeMillis();
                }

                /*
                 * Ficou 300 ms dentro da faixa.
                 */
                if (System.currentTimeMillis()
                        - stableSince
                        >= STABILIZATION_TIME_MS) {

                    shooterStable = true;

                    /*
                     * Assim que estabilizar,
                     * liga o intake.
                     */
                    intakeSubsystem.intake(1.0);

                    intakeStarted = true;

                    intakeStartedAt =
                            System.currentTimeMillis();
                }

            } else {

                /*
                 * Saiu da faixa.
                 * Reinicia a contagem.
                 */
                stableSince = 0;
            }
        }

        /*
         * SEGUNDA ETAPA:
         * mantém o intake ligado.
         */
        if (intakeStarted) {

            intakeSubsystem.intake(1.0);
        }

        /*
         * TERCEIRA ETAPA:
         * depois de 100 ms de intake,
         * liga o buffer.
         */
        if (intakeStarted
                && !bufferStarted
                && System.currentTimeMillis()
                - intakeStartedAt
                >= INTAKE_LEAD_TIME_MS) {

            bufferSubsystem.buffer(1.0);

            bufferStarted = true;
        }

        /*
         * Mantém o buffer ligado.
         */
        if (bufferStarted) {

            bufferSubsystem.buffer(1.0);
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