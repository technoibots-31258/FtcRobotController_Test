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
// * OUTRO LADO DA QUADRA
// *
// * Sequência:
// *  1) liga o shooter e espera chegar no RPM
// *  2) atira: intake + buffer só rodam com o RPM correto;
// *     quando o RPM cai (bola saiu), pausam e religam ao recuperar
// *  3) gira para a ESQUERDA
// *  4) anda reto um pouco
// *  5) gira para a DIREITA (mesmo tempo do giro anterior)
// *  6) anda reto por um tempo
// *  7) anda para a ESQUERDA (lateral) por 1 segundo
// *  8) para o robô
// */
//@Autonomous(name = "Estacionar - Esquerda 1.67", group = "Autonomos")
//public class AutoDriveLeft extends CommandOpMode {
//
//    // =====================================================
//    // CONFIGURAÇÕES (ajuste aqui)
//    // =====================================================
//
//    // Liga/desliga o movimento (false = testa só o chute)
//    private static final boolean DRIVE_ENABLED = true;
//
//    // ----- Tiro -----
//    private static final double SPINUP_TIMEOUT_SEC = 4.0;    // máximo esperando o RPM
//    private static final double FEED_TIME_SEC = 5.0;         // duração da fase de tiro
//    private static final double PAUSE_AFTER_DROP_SEC = 0.15; // pausa extra após queda de RPM
//    private static final double INTAKE_POWER = 1.0;
//    private static final double BUFFER_POWER = -1.0;         // mesmo sinal do teleop (para frente)
//
//    // ----- Movimento: potências -----
//    private static final double DRIVE_POWER = 0.5;   // se andar para TRÁS, inverta o sinal
//    private static final double TURN_POWER = 0.4;    // positivo = direita, negativo = esquerda
//    private static final double STRAFE_POWER = 0.5;  // módulo; o sinal é aplicado abaixo
//
//    // ----- Movimento: tempos (segundos) -----
//    private static final double TURN_SEC_1 = 0.65;        // tempo dos primeiro giro
//    private static final double TURN_SEC_2 = 0.5;        // tempo do segundo giro
//
//    private static final double STRAIGHT_1_SEC = 1.3;  // reto depois do giro à esquerda
//    private static final double STRAIGHT_2_SEC = 2.7;  // reto depois do giro à direita
//    private static final double STRAFE_LEFT_SEC = 2.0; // lateral para a esquerda
//    private static final double STOP_SEC = 0.3;        // garante que o robô pare
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
//        telemetry.addLine("Outro Lado da Quadra pronto");
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
//       // telemetry.addData("Shooter no RPM", robot.shooterSubsystem.isAtTargetVelocity());
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
//    /** Sequência de movimento do outro lado da quadra. */
//    private SequentialCommandGroup buildDriveSequence() {
//
//        return new SequentialCommandGroup(
//
//                // gira para a ESQUERDA
//                move(0.0, 0.0, -TURN_POWER, TURN_SEC_1),
//
//                // anda reto um pouco
//                move(DRIVE_POWER, 0.0, 0.0, STRAIGHT_1_SEC),
//
//                // gira para a DIREITA (mesmo tempo do giro anterior)
//                move(0.0, 0.0, TURN_POWER, TURN_SEC_2),
//
//                // anda reto por um tempo
//                move(DRIVE_POWER, 0.0, 0.0, STRAIGHT_2_SEC),
//
//                // anda para a ESQUERDA por 1 segundo (lateral negativa = esquerda)
//                move(0.0, -STRAFE_POWER, 0.0, STRAFE_LEFT_SEC),
//
//                // para o robô
//                move(0.0, 0.0, 0.0, STOP_SEC)
//        );
//    }
//
//    /**
//     * Move o robô por um tempo.
//     * forward: frente/trás | strafe: lateral (negativo = esquerda) | turn: giro (positivo = direita)
//     */
//    private Command move(double forward, double strafe, double turn, double seconds) {
//
//        return new DriveCommand(
//                robot.driveSubsystem,
//                () -> forward,   // frente/trás
//                () -> strafe,    // lateral
//                () -> turn       // giro
//        ).withTimeout((long) (seconds * 1000));
//    }
//}