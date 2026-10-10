package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

/**
 * Enquanto o botão estiver pressionado, alimenta UMA bola por vez:
 *
 *   1) espera o shooter ficar pronto (velocidade estável na meta);
 *   2) liga o buffer por FEED_MS (o tempo de uma bola passar);
 *   3) para o buffer e espera a velocidade recuperar;
 *   4) repete.
 *
 * Só exige o BufferSubsystem. O ShooterSubsystem é apenas lido,
 * então não conflita com o ShooterCommand.
 */
public class FeedSequenceCommand extends CommandBase {

    // Potência do buffer para a frente (mesmo sinal do BufferCommand atual).
    private static final double FEED_POWER = -1.0;

    // Tempo ligado para empurrar uma bola. AJUSTE em campo.
    private static final double FEED_MS = 400.0;

    // Pausa mínima depois de cada bola, antes de olhar se o shooter
    // está pronto. Evita liberar a próxima bola antes de a queda de
    // velocidade da anterior aparecer na leitura.
    private static final double MIN_GAP_MS = 150.0;

    private enum State {
        WAIT_READY,
        FEEDING
    }

    private final BufferSubsystem bufferSubsystem;
    private final ShooterSubsystem shooterSubsystem;

    private final ElapsedTime timer = new ElapsedTime();

    private State state = State.WAIT_READY;
    private boolean alreadyFed = false;

    public FeedSequenceCommand(
            BufferSubsystem bufferSubsystem,
            ShooterSubsystem shooterSubsystem
    ) {
        this.bufferSubsystem = bufferSubsystem;
        this.shooterSubsystem = shooterSubsystem;

        addRequirements(bufferSubsystem);
    }

    @Override
    public void initialize() {
        state = State.WAIT_READY;
        alreadyFed = false;
        bufferSubsystem.stop();
        timer.reset();
    }

    @Override
    public void execute() {

        switch (state) {

            case WAIT_READY:
                bufferSubsystem.stop();

                boolean gapOk =
                        !alreadyFed || timer.milliseconds() >= MIN_GAP_MS;

                if (gapOk && shooterSubsystem.isAtTargetVelocity()) {
                    state = State.FEEDING;
                    timer.reset();
                }
                break;

            case FEEDING:
                bufferSubsystem.buffer(FEED_POWER);

                if (timer.milliseconds() >= FEED_MS) {
                    bufferSubsystem.stop();
                    alreadyFed = true;
                    state = State.WAIT_READY;
                    timer.reset();
                }
                break;
        }
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