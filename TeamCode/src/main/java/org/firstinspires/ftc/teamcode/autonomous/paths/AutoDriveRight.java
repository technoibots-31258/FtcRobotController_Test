package org.firstinspires.ftc.teamcode.autonomous.paths; // use o MESMO pacote do seu Autoshootanddrive

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.robot.commands.AutoShootCommand;
import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

/**
 * EMERGÊNCIA - DIREITA
 *
 * Sequência:
 *  1) liga o shooter e espera os dois motores chegarem no alvo
 *  2) atira: ao ficar pronto, liga intake + buffer e mantém ligados
 *  3) anda para frente
 *  4) gira para a direita
 *  5) segue reto
 *  6) NÃO gira para a esquerda: anda só um pouco mais para frente
 *  7) para o robô
 */
@Autonomous(name = "Estacionar - Direita", group = "Emergencia")
public class AutoDriveRight extends CommandOpMode {

    // =====================================================
    // CONFIGURAÇÕES (ajuste aqui)
    // =====================================================

    // Liga/desliga o movimento (false = testa só o chute)
    private static final boolean DRIVE_ENABLED = true;

    // ----- Tiro -----
    // Alvo do shooter no autônomo (ticks/s). O d-pad do teleop não vale aqui.
    private static final double AUTO_SHOOTER_TARGET = 620;
    private static final double SPINUP_TIMEOUT_SEC = 4.0;   // máximo esperando o RPM
    private static final double FEED_TIME_SEC = 5.0;        // duração do tiro, contada a partir de "pronto"

    // ----- Movimento -----
    private static final double DRIVE_POWER = 0.5;   // se andar para TRÁS, inverta o sinal
    private static final double TURN_POWER = 0.4;    // positivo = direita (inverta se girar ao contrário)

    private static final double FORWARD_1_SEC = 0.46;      // primeiro trecho para frente
    private static final double TURN_RIGHT_SEC = 0.71;     // giro para a direita
    private static final double STRAIGHT_SEC = 2.0;       // reto depois do giro
    private static final double FORWARD_EXTRA_SEC = 0.5;  // "um pouco mais para frente"
    private static final double STOP_SEC = 0.3;           // garante que o robô pare

    // =====================================================
    // ESTADO
    // =====================================================

    private MecanumDriveSubsystem driveSubsystem;
    private IntakeSubsystem intakeSubsystem;
    private BufferSubsystem bufferSubsystem;
    private ShooterSubsystem shooterSubsystem;

    private AutoShootCommand shootCommand;
    private SequentialCommandGroup autoSequence;

    private boolean started = false;
    private String stage = "Aguardando Play";

    // =====================================================
    // INICIALIZAÇÃO (antes do Play)
    // =====================================================

    @Override
    public void initialize() {

        // Leituras mais rápidas dos hubs
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        // No autônomo usamos os subsystems direto, sem o RobotContainer:
        // assim nenhum botão do controle dispara comandos sem querer.
        driveSubsystem = new MecanumDriveSubsystem(hardwareMap);
        intakeSubsystem = new IntakeSubsystem(hardwareMap);
        bufferSubsystem = new BufferSubsystem(hardwareMap);
        shooterSubsystem = new ShooterSubsystem(hardwareMap);

        shooterSubsystem.setTarget(AUTO_SHOOTER_TARGET);

        autoSequence = buildAutoSequence();

        telemetry.addLine("Emergencia - Direita pronto");
        telemetry.addData("Movimento", DRIVE_ENABLED ? "LIGADO" : "DESLIGADO (só chute)");
        telemetry.addData("Alvo do shooter", "%.0f", shooterSubsystem.getTarget());
        telemetry.update();
    }

    // =====================================================
    // LOOP (depois do Play)
    // =====================================================

    @Override
    public void run() {

        // Agenda a sequência só depois do Play
        if (!started) {
            autoSequence.schedule();
            started = true;
        }

        // Roda o CommandScheduler (periodic dos subsystems e commands)
        super.run();

        telemetry.addData("Etapa", stage);
        telemetry.addData("Alvo", "%.0f", shooterSubsystem.getTarget());
        telemetry.addData("Shooter 1 vel / erro", "%.0f / %.0f",
                shooterSubsystem.getVelocity1(), shooterSubsystem.getError1());
        telemetry.addData("Shooter 2 vel / erro", "%.0f / %.0f",
                shooterSubsystem.getVelocity2(), shooterSubsystem.getError2());
        telemetry.addData("Shooter pronto", shooterSubsystem.isReadyToShoot());
        telemetry.addData("Alimentando (intake/buffer)", shootCommand.isFeeding());
        telemetry.addData("Sequência rodando", autoSequence.isScheduled());
        telemetry.update();
    }

    // =====================================================
    // SEQUÊNCIA
    // =====================================================

    private SequentialCommandGroup buildAutoSequence() {

        SequentialCommandGroup sequence = new SequentialCommandGroup();

        // ----- Tiro -----
        shootCommand = new AutoShootCommand(
                shooterSubsystem,
                intakeSubsystem,
                bufferSubsystem,
                SPINUP_TIMEOUT_SEC,
                FEED_TIME_SEC
        );

        sequence.addCommands(
                stage("Atirando"),
                shootCommand
        );

        // ----- Movimento -----
        if (DRIVE_ENABLED) {
            sequence.addCommands(

                    stage("Andando para frente"),
                    move(DRIVE_POWER, 0.0, FORWARD_1_SEC),

                    stage("Girando para a direita"),
                    move(0.0, TURN_POWER, TURN_RIGHT_SEC),

                    stage("Seguindo reto"),
                    move(DRIVE_POWER, 0.0, STRAIGHT_SEC),

                    // EM VEZ de girar para a esquerda: só mais um pouco para frente
                    stage("Mais um pouco para frente"),
                    move(DRIVE_POWER, 0.0, FORWARD_EXTRA_SEC),

                    stage("Parando"),
                    move(0.0, 0.0, STOP_SEC)
            );
        }

        sequence.addCommands(stage("Finalizado"));

        return sequence;
    }

    /** Só atualiza o texto da etapa mostrada na telemetria. */
    private Command stage(String name) {
        return new InstantCommand(() -> stage = name);
    }

    /** Move o robô por um tempo (forward: frente/trás | turn: giro). */
    private Command move(double forward, double turn, double seconds) {

        return new DriveCommand(
                driveSubsystem,
                () -> forward,   // frente/trás
                () -> 0.0,       // lateral (zerada por causa do drift)
                () -> turn       // giro
        ).withTimeout((long) (seconds * 1000));
    }
}