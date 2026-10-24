package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

/**
 * Dispara uma rajada de até quatro bolas por acionamento do botão.
 *
 * 1) Espera os flywheels atingirem a velocidade-alvo.
 * 2) Alimenta as bolas em pulsos curtos, sem aguardar o shooter recuperar
 *    totalmente o RPM entre cada bola.
 * 3) Para intake e buffer após a quarta alimentação ou ao soltar o botão.
 *
 * O comando não controla o shooter: apenas lê seu estado, então não disputa
 * o ShooterSubsystem com ShooterCommand.
 */
public class FeedSequenceCommand extends CommandBase {

    private static final double FEED_POWER = -1.0;
    private static final double INTAKE_POWER = -1.0;

    // Ajuste em campo: tempo necessário para uma bola atravessar o buffer.
    private static final double FEED_MS = 300.0;

    // Pequeno intervalo entre bolas para separar as alimentações.
    private static final double BALL_GAP_MS = 35.0;

    private static final int MAX_BALLS_PER_PRESS = 4;

    private enum State {
        WAIT_READY,
        FEEDING,
        GAP,
        DONE
    }

    private final BufferSubsystem bufferSubsystem;
    private final ShooterSubsystem shooterSubsystem;
    private final IntakeSubsystem intakeSubsystem;

    private final ElapsedTime timer = new ElapsedTime();

    private State state = State.WAIT_READY;
    private int ballsFed = 0;

    public FeedSequenceCommand(
            BufferSubsystem bufferSubsystem,
            ShooterSubsystem shooterSubsystem,
            IntakeSubsystem intakeSubsystem
    ) {
        this.bufferSubsystem = bufferSubsystem;
        this.shooterSubsystem = shooterSubsystem;
        this.intakeSubsystem = intakeSubsystem;

        // O comando controla intake e buffer; apenas lê o shooter.
        addRequirements(bufferSubsystem, intakeSubsystem);
    }

    @Override
    public void initialize() {
        state = State.WAIT_READY;
        ballsFed = 0;
        stopFeeders();
        timer.reset();
    }

    @Override
    public void execute() {
        switch (state) {

            case WAIT_READY:
                stopFeeders();

                // Aguarda a velocidade correta somente antes da primeira bola.
                if (shooterSubsystem.isAtTargetVelocity()) {
                    state = State.FEEDING;
                    timer.reset();
                }
                break;

            case FEEDING:
                intakeSubsystem.intake(INTAKE_POWER);
                bufferSubsystem.buffer(FEED_POWER);

                if (timer.milliseconds() >= FEED_MS) {
                    bufferSubsystem.stop();
                    ballsFed++;

                    if (ballsFed >= MAX_BALLS_PER_PRESS) {
                        state = State.DONE;
                        stopFeeders();
                    } else {
                        state = State.GAP;
                        timer.reset();
                    }
                }
                break;

            case GAP:
                // O intake continua puxando a próxima bola para a posição.
                intakeSubsystem.intake(INTAKE_POWER);
                bufferSubsystem.stop();

                if (timer.milliseconds() >= BALL_GAP_MS) {
                    state = State.FEEDING;
                    timer.reset();
                }
                break;

            case DONE:
                stopFeeders();
                break;
        }
    }

    private void stopFeeders() {
        bufferSubsystem.stop();
        intakeSubsystem.stop();
    }

    @Override
    public void end(boolean interrupted) {
        stopFeeders();
    }

    @Override
    public boolean isFinished() {
        // Mantém-se agendado em DONE até soltar o botão; evita iniciar
        // outra rajada de quatro bolas enquanto o botão continuar pressionado.
        return false;
    }
}
