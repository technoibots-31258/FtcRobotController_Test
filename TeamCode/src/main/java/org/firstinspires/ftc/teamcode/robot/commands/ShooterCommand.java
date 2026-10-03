package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

public class ShooterCommand extends CommandBase {

    private final ShooterSubsystem shooterSubsystem;
    private final IntakeSubsystem intakeSubsystem;
    private final BufferSubsystem bufferSubsystem;

    // Tempo que o shooter precisa permanecer estável
    private static final long STABILIZATION_TIME_MS = 300;

    // Tempo de atraso entre Intake e Buffer
    private static final long INTAKE_LEAD_TIME_MS = 300;

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
         * Mantém o Shooter funcionando pelo PID.
         */
        shooterSubsystem.startShooter();


        /*
         * =====================================================
         * 1. VERIFICAÇÃO DA VELOCIDADE
         * =====================================================
         */

        if (shooterSubsystem.isAtTargetVelocity()) {

            /*
             * Entrou na faixa correta.
             *
             * Começa a contar o tempo de estabilização.
             */
            if (!shooterStable) {

                if (stableSince == 0) {

                    stableSince =
                            System.currentTimeMillis();
                }

                /*
                 * Precisa permanecer estável durante
                 * STABILIZATION_TIME_MS.
                 */
                if (System.currentTimeMillis()
                        - stableSince
                        >= STABILIZATION_TIME_MS) {

                    shooterStable = true;

                    /*
                     * Começa o Intake.
                     */
                    intakeSubsystem.intake(1.0);

                    intakeStarted = true;

                    intakeStartedAt =
                            System.currentTimeMillis();
                }
            }

        } else {

            /*
             * =================================================
             * A VELOCIDADE CAIU
             * =================================================
             *
             * Para de alimentar a bola imediatamente.
             */

            shooterStable = false;

            stableSince = 0;

            intakeStarted = false;
            bufferStarted = false;

            intakeStartedAt = 0;

            intakeSubsystem.stop();
            bufferSubsystem.stop();
        }


        /*
         * =====================================================
         * 2. INTAKE
         * =====================================================
         *
         * Só funciona enquanto o Shooter estiver estável.
         */

        if (shooterStable) {

            intakeSubsystem.intake(1.0);
        }


        /*
         * =====================================================
         * 3. BUFFER
         * =====================================================
         *
         * Espera o Intake funcionar durante
         * INTAKE_LEAD_TIME_MS antes de liberar o Buffer.
         */

        if (shooterStable
                && intakeStarted
                && !bufferStarted
                && System.currentTimeMillis()
                - intakeStartedAt
                >= INTAKE_LEAD_TIME_MS) {

            bufferSubsystem.buffer(1.0);

            bufferStarted = true;
        }


        /*
         * Mantém o Buffer funcionando enquanto
         * o Shooter estiver estável.
         */

        if (shooterStable && bufferStarted) {

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