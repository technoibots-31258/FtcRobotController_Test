//package org.firstinspires.ftc.teamcode; // use o MESMO pacote do seu Autoshootanddrive
//
//import com.arcrobotics.ftclib.command.Command;
//import com.arcrobotics.ftclib.command.CommandOpMode;
//import com.arcrobotics.ftclib.command.SequentialCommandGroup;
//import com.arcrobotics.ftclib.gamepad.GamepadEx;
//import com.qualcomm.hardware.lynx.LynxModule;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.teamcode.robot.RobotContainer;
//import org.firstinspires.ftc.teamcode.robot.commands.BufferCommand;
//import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
//import org.firstinspires.ftc.teamcode.robot.commands.IntakeCommand;
//import org.firstinspires.ftc.teamcode.robot.commands.ShooterCommand;
//
///**
// * EMERGÊNCIA - DIREITA
// *
// * Sequência:
// *  1) liga o shooter e espera chegar no RPM
// *  2) atira: intake + buffer só rodam com o RPM correto;
// *     quando o RPM cai (bola saiu), pausam e religam ao recuperar
// *  3) anda para frente
// *  4) gira para a direita
// *  5) segue reto
// *  6) NÃO gira para a esquerda: anda só um pouco mais para frente
// *  7) para o robô
// */
//@Autonomous(name = "Estacionar - Direita", group = "Emergencia")
//public class AutoDriveRight extends CommandOpMode {
//
//    // =====================================================
//    // CONFIGURAÇÕES (ajuste aqui)
//    // =====================================================
//
//    // Liga/desliga o movimento (false = testa só o chute)
//    private static final boolean DRIVE_ENABLED = true;
//
//    // ----- Tiro -----
//    private static final double SPINUP_TIMEOUT_SEC = 4.0;   // máximo esperando o RPM
//    private static final double FEED_TIME_SEC = 5.0;        // duração da fase de tiro
//    private static final double PAUSE_AFTER_DROP_SEC = 0.15; // pausa extra após queda de RPM
//    private static final double INTAKE_POWER = 1.0;
//    private static final double BUFFER_POWER = -1.0;        // mesmo sinal do teleop (para frente)
//
//    // ----- Movimento -----
//    private static final double DRIVE_POWER = 0.5;   // se andar para TRÁS, inverta o sinal
//    private static final double TURN_POWER = 0.4;    // positivo = direita (inverta se girar ao contrário)
//
//    private static final double FORWARD_1_SEC = 0.6;      // primeiro trecho para frente
//    private static final double TURN_RIGHT_SEC = 0.5;     // giro para a direita
//    private static final double STRAIGHT_SEC = 1.0;       // reto depois do giro
//    private static final double FORWARD_EXTRA_SEC = 0.5;  // "um pouco mais para frente"
//    private static final double STOP_SEC = 0.3;           // garante que o robô pare
//
//    // =====================================================
//    // ESTADO
//    // =====================================================
//
//    private enum Phase { INIT, SPIN_UP, FEED, DRIVE, DONE }
//
//    private RobotContainer robot;
//
//    private ShooterCommand shooterCommand;
//    private IntakeCommand intakeCommand;
//    private BufferCommand bufferCommand;
//    private SequentialCommandGroup driveSequence;
//
//    private Phase phase = Phase.INIT;
//
//    private final ElapsedTime phaseTimer = new ElapsedTime();
//    private double pauseUntilSec = 0.0;
//
//    // =====================================================
//    // INICIALIZAÇÃO (antes do Play)
//    // =====================================================
//
//    @Override
//    public void initialize() {
//
//        // Leituras mais rápidas dos hubs
//        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
//            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
//        }
//
//        robot = new RobotContainer(hardwareMap, new GamepadEx(gamepad1));
//
//        shooterCommand = new ShooterCommand(robot.shooterSubsystem);
//
//        intakeCommand = new IntakeCommand(
//                robot.intakeSubsystem,
//                INTAKE_POWER
//        );
//
//        bufferCommand = new BufferCommand(
//                robot.bufferSubsystem,
//                BUFFER_POWER
//        );
//
//        driveSequence = buildDriveSequence();
//
//        telemetry.addLine("Emergencia - Direita pronto");
//        telemetry.addData("Movimento", DRIVE_ENABLED ? "LIGADO" : "DESLIGADO (só chute)");
//        telemetry.update();
//    }
//
//    // =====================================================
//    // LOOP (depois do Play)
//    // =====================================================
//
//    @Override
//    public void run() {
//
//        updateAuto();
//
//        // Roda o CommandScheduler (periodic dos subsystems e commands)
//        super.run();
//
//        telemetry.addData("Fase", phase);
//        telemetry.addData("Shooter no RPM", robot.shooterSubsystem.isAtTargetVelocity());
//        telemetry.addData("Alimentando", intakeCommand.isScheduled() && bufferCommand.isScheduled());
//        telemetry.addData("Tempo na fase (s)", "%.2f", phaseTimer.seconds());
//        telemetry.update();
//    }
//
//    private void updateAuto() {
//
//        switch (phase) {
//
//            // ---------- Começa ----------
//            case INIT:
//                shooterCommand.schedule();
//                phaseTimer.reset();
//                phase = Phase.SPIN_UP;
//                break;
//
//            // ---------- Espera o shooter chegar no RPM ----------
//            case SPIN_UP:
//                if (robot.shooterSubsystem.isAtTargetVelocity()
//                        || phaseTimer.seconds() >= SPINUP_TIMEOUT_SEC) {
//
//                    phaseTimer.reset();
//                    pauseUntilSec = 0.0;
//                    phase = Phase.FEED;
//                }
//                break;
//
//            // ---------- Atira ----------
//            case FEED:
//                boolean atTarget = robot.shooterSubsystem.isAtTargetVelocity();
//
//                // Caiu o RPM (bola saiu): pausa intake e buffer por um tempo
//                if (!atTarget) {
//                    pauseUntilSec = phaseTimer.seconds() + PAUSE_AFTER_DROP_SEC;
//                }
//
//                // Só alimenta com RPM correto e fora da pausa
//                setFeeding(atTarget && phaseTimer.seconds() >= pauseUntilSec);
//
//                if (phaseTimer.seconds() >= FEED_TIME_SEC) {
//                    setFeeding(false);
//                    shooterCommand.cancel(); // desliga o shooter
//
//                    if (DRIVE_ENABLED) {
//                        driveSequence.schedule();
//                        phase = Phase.DRIVE;
//                    } else {
//                        phase = Phase.DONE;
//                    }
//                    phaseTimer.reset();
//                }
//                break;
//
//            // ---------- Anda ----------
//            case DRIVE:
//                if (!driveSequence.isScheduled()) {
//                    phase = Phase.DONE;
//                }
//                break;
//
//            // ---------- Fim ----------
//            case DONE:
//                setFeeding(false);
//                break;
//        }
//    }
//
//    // =====================================================
//    // AUXILIARES
//    // =====================================================
//
//    /** Liga ou desliga intake e buffer juntos. */
//    private void setFeeding(boolean on) {
//
//        if (on) {
//            if (!intakeCommand.isScheduled()) {
//                intakeCommand.schedule();
//            }
//            if (!bufferCommand.isScheduled()) {
//                bufferCommand.schedule();
//            }
//        } else {
//            if (intakeCommand.isScheduled()) {
//                intakeCommand.cancel();
//            }
//            if (bufferCommand.isScheduled()) {
//                bufferCommand.cancel();
//            }
//        }
//    }
//
//    /** Sequência de movimento da emergência (sem o giro para a esquerda). */
//    private SequentialCommandGroup buildDriveSequence() {
//
//        return new SequentialCommandGroup(
//
//                // anda para frente
//                move(DRIVE_POWER, 0.0, FORWARD_1_SEC),
//
//                // gira para a direita
//                move(0.0, TURN_POWER, TURN_RIGHT_SEC),
//
//                // segue reto
//                move(DRIVE_POWER, 0.0, STRAIGHT_SEC),
//
//                // EM VEZ de girar para a esquerda: só mais um pouco para frente
//                move(DRIVE_POWER, 0.0, FORWARD_EXTRA_SEC),
//
//                // para o robô
//                move(0.0, 0.0, STOP_SEC)
//        );
//    }
//
//    /** Move o robô por um tempo (forward: frente/trás | turn: giro). */
//    private Command move(double forward, double turn, double seconds) {
//
//        return new DriveCommand(
//                robot.driveSubsystem,
//                () -> forward,   // frente/trás
//                () -> 0.0,       // lateral (zerada por causa do drift)
//                () -> turn       // giro
//        ).withTimeout((long) (seconds * 1000));
//    }
//}