package org.firstinspires.ftc.teamcode.robot.autonomous;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.commands.BufferCommand;
import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.commands.IntakeCommand;
import org.firstinspires.ftc.teamcode.robot.commands.ShooterCommand;
import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

/**
 * AUTÔNOMO (FORMA B): ATIRA, ANDA PARA FRENTE, GIRA PARA A DIREITA, SEGUE RETO,
 * GIRA PARA A ESQUERDA (MESMO TEMPO) E SEGUE RETO DE NOVO
 *
 * Nesta versão NÃO usamos o FeedSequenceCommand. O próprio OpMode liga o
 * intake e o buffer diretamente, usando os mesmos commands do teleop
 * (IntakeCommand e BufferCommand).
 *
 * Por que girar em vez de andar de lado? As rodas mecanum têm drift ao fazer
 * strafe, então o robô não ia reto para a direita. Girar e andar reto é mais
 * previsível.
 *
 * Fluxo:
 *
 *   1. SPINUP        -> liga o shooter e espera ele ficar pronto
 *                       (isAtTargetVelocity(), a mesma checagem do teleop).
 *   2. FEED          -> liga o INTAKE e o BUFFER (direto, sem parar) para atirar.
 *                       Quando o RPM do shooter CAI (uma bola foi atirada), os
 *                       dois PAUSAM por PAUSE_AFTER_DROP_SEC e depois voltam a
 *                       ligar direto, até a próxima queda de RPM.
 *   3. DRIVE_FORWARD -> (só se DRIVE_ENABLED = true) anda para frente.
 *   4. TURN_RIGHT    -> gira para a DIREITA por TURN_TIME_SEC.
 *   5. DRIVE_ANGLED  -> segue reto na nova direção (já virado para a direita).
 *   6. TURN_LEFT     -> gira para a ESQUERDA pelo MESMO tempo e potência,
 *                       voltando (aproximadamente) para a direção original.
 *   7. DRIVE_STRAIGHT-> segue reto mais um tempo.
 *   8. DONE          -> segura o robô parado até o fim do autônomo.
 *
 * O shooter usa o alvo e os ganhos que já estão no ShooterSubsystem,
 * exatamente como no teleop.
 *
 * COMO FUNCIONA O "TRIGGER COM VARIÁVEL":
 * No RobotContainer, um Trigger liga um command a um botão do gamepad:
 * enquanto o botão está apertado o command roda; ao soltar, ele é cancelado.
 * Aqui é igual, mas quem "aperta o botão" é uma variável boolean
 * (shooterOn, intakeOn, bufferOn, driveOn, holdOn) que a máquina de estados
 * liga e desliga.
 *
 * COMO O ROBÔ ANDA:
 * Existe UM DriveCommand só, que lê três variáveis a cada loop:
 * driveForward (frente/trás), driveStrafe (lateral) e driveTurn (giro).
 * Cada estado de movimento apenas troca esses valores, pelo método setDrive().
 */
@Autonomous(name = "Mega auto que atira e anda - direita 1.1", group = "Autonomo")
public class Autoshootanddrive extends CommandOpMode {

    // =====================================================
    // AJUSTES (mexa só aqui)
    // =====================================================

    /** true = atira e anda; false = só atira (teste do chute). NÃO ESQUEÇA de conferir. */
    private static final boolean DRIVE_ENABLED = true;

    /** Tempo máximo esperando o shooter ficar pronto. Passou disso, segue para o FEED. */
    private static final double SPINUP_TIMEOUT_SEC = 4.0;

    /**
     * Tempo total da fase de tiro. Conta também as pausas esperando o RPM
     * recuperar entre uma bola e outra, então precisa ser generoso.
     * Comece alto e vá diminuindo.
     */
    private static final double FEED_TIME_SEC = 5.0;

    /**
     * Quanto tempo intake e buffer ficam PARADOS depois que o RPM cai (bola
     * atirada), para o shooter ter tempo de recuperar antes da próxima bola.
     * Depois disso eles voltam a ligar direto (sem ficar pausando).
     * Se o 2º tiro ainda sair fraco, AUMENTE este valor; se demorar demais
     * entre os tiros, diminua. Lembre de manter FEED_TIME_SEC grande o
     * bastante para caberem todas as pausas.
     */
    private static final double PAUSE_AFTER_DROP_SEC = 0.5;

    /** Potência do intake (1.0 = puxa a bola, como no teleop com LEFT_TRIGGER). */
    private static final double INTAKE_POWER = 1.0;

    /**
     * Potência do buffer para EMPURRAR a bola ao shooter.
     * O FeedSequenceCommand usava -1.0 para frente; o botão B do teleop usa
     * +1.0 para o buffer REVERSO. Se o buffer girar para o lado errado,
     * troque o sinal aqui.
     */
    private static final double BUFFER_POWER = -1.0;

    // ---------- 1ª etapa de movimento: para frente ----------

    private static final double DRIVE_TIME_SEC = 0.6; // frente depois de atirar

    private static final double DRIVE_POWER = 0.5; // potencia

    // ---------- Giros (usados para a direita e para a esquerda) ----------

    /**
     * Tempo de CADA giro. O giro para a direita e o giro para a esquerda usam
     * este mesmo valor, para o robô voltar à direção original.
     * Defina o ângulo desejado testando: mais tempo = gira mais.
     */
    private static final double TURN_TIME_SEC = 0.5;

    /**
     * Potência do giro (0 a 1). Positivo = gira para a DIREITA (sentido
     * horário). Se girar para o lado errado, troque o sinal (-0.4).
     * Potência mais baixa deixa o giro mais repetível (menos inércia).
     */
    private static final double TURN_POWER = 0.4;

    // ---------- Reta depois do giro para a direita ----------

    private static final double ANGLED_TIME_SEC = 1.0; // reta pra direita

    private static final double ANGLED_POWER = 0.5; // potencia

    // ---------- Reta final (depois de girar de volta para a esquerda) ----------

    /** Quanto tempo segue reto no final. AJUSTE em campo. */
    private static final double FINAL_TIME_SEC = 2.0;

    /** Potência da reta final. */
    private static final double FINAL_POWER = 0.5;

    // =====================================================
    // ESTADOS
    // =====================================================

    private enum State {
        SPINUP, FEED, DRIVE_FORWARD, TURN_RIGHT, DRIVE_ANGLED,
        TURN_LEFT, DRIVE_STRAIGHT, DONE
    }

    private State state = State.SPINUP;

    /** Cronômetro do estado atual (zera a cada troca de estado). */
    private final ElapsedTime stateTimer = new ElapsedTime();

    /** Faz o cronômetro só começar quando o autônomo inicia (e não no INIT). */
    private boolean firstLoop = true;

    // "Botões virtuais": a máquina de estados liga/desliga estas variáveis.
    private boolean shooterOn = false;
    private boolean intakeOn = false;
    private boolean bufferOn = false;
    private boolean driveOn = false;
    private boolean holdOn = false;

    // Controle interno do estado FEED (pausa depois da queda de RPM).
    /** true = intake e buffer estão pausados esperando o shooter recuperar. */
    private boolean feedPaused = false;

    /**
     * true = já vimos o shooter pronto neste ciclo, então uma saída da faixa
     * de RPM conta como "queda" (bola atirada). Evita pausar à toa enquanto o
     * shooter ainda está se recuperando logo depois de religar.
     */
    private boolean dropArmed = false;

    /** Cronômetro da pausa. */
    private final ElapsedTime pauseTimer = new ElapsedTime();

    // Valores que o DriveCommand lê a cada loop (trocados por cada estado).
    private double driveForward = 0.0;
    private double driveStrafe = 0.0;
    private double driveTurn = 0.0;

    private ShooterSubsystem shooter;
    private BufferSubsystem buffer;
    private IntakeSubsystem intake;
    private MecanumDriveSubsystem drive;

    // =====================================================
    // INIT (roda ao apertar INIT no Driver Station)
    // =====================================================

    @Override
    public void initialize() {

        // Limpa qualquer command/subsystem que tenha sobrado de outro OpMode.
        reset();

        // Os subsystems se registram sozinhos no scheduler ao serem criados,
        // então o periodic() deles (o PID do shooter!) roda a cada loop.
        drive = new MecanumDriveSubsystem(hardwareMap);
        buffer = new BufferSubsystem(hardwareMap);
        shooter = new ShooterSubsystem(hardwareMap);
        intake = new IntakeSubsystem(hardwareMap);

        // Shooter: roda enquanto shooterOn == true. Ao ficar false, o command
        // é cancelado e o end() dele chama stopShooter().
        new Trigger(() -> shooterOn).whileActiveContinuous(
                new ShooterCommand(shooter)
        );

        // Intake: roda enquanto intakeOn == true (mesmo command do teleop).
        new Trigger(() -> intakeOn).whileActiveContinuous(
                new IntakeCommand(intake, INTAKE_POWER)
        );

        // Buffer: roda enquanto bufferOn == true (mesmo command do teleop).
        new Trigger(() -> bufferOn).whileActiveContinuous(
                new BufferCommand(buffer, BUFFER_POWER)
        );

        // Andar: DriveCommand recebe (frente, lateral, giro). Os três valores
        // são lidos das variáveis a cada loop (os lambdas leem o valor atual),
        // então basta trocar driveForward/driveStrafe/driveTurn para mudar o
        // movimento sem criar outro command.
        new Trigger(() -> driveOn).whileActiveContinuous(
                new DriveCommand(
                        drive,
                        () -> driveForward,
                        () -> driveStrafe,
                        () -> driveTurn
                )
        );

        // "Segurar parado": DriveCommand com tudo zero. Garante que a tração
        // termine em 0 mesmo que o end() do DriveCommand não zere sozinho.
        new Trigger(() -> holdOn).whileActiveContinuous(
                new DriveCommand(drive, () -> 0.0, () -> 0.0, () -> 0.0)
        );

        telemetry.addLine("Pronto. Aperte START.");
        telemetry.addData("Anda depois de atirar?", DRIVE_ENABLED);
        telemetry.update();
    }

    // =====================================================
    // LOOP (roda continuamente depois do START)
    // =====================================================

    @Override
    public void run() {

        // Executa o scheduler: avalia os Triggers, roda os commands e chama o
        // periodic() de cada subsystem (é aqui que o PID do shooter trabalha).
        // NUNCA remova esta linha, sem ela nada se move.
        super.run();

        if (firstLoop) {
            stateTimer.reset();
            firstLoop = false;
        }

        // "Pronto" = RPM estável dentro da tolerância por tempo suficiente.
        // Quando a bola passa e o RPM cai, vira false até recuperar.
        boolean ready = shooter.isAtTargetVelocity();

        switch (state) {

            case SPINUP:
                // Liga só o shooter e espera chegar na velocidade.
                shooterOn = true;

                if (shooter.isAtTargetVelocity()
                        || stateTimer.seconds() >= SPINUP_TIMEOUT_SEC) {
                    goTo(State.FEED);
                }
                break;

            case FEED:
                // Shooter sempre ligado durante o tiro.
                shooterOn = true;

                if (!feedPaused) {
                    // ALIMENTANDO: intake e buffer ligados direto, sem parar.
                    intakeOn = true;
                    bufferOn = true;

                    if (ready) {
                        // Shooter no RPM certo: a partir de agora, sair dessa
                        // faixa significa que uma bola foi atirada.
                        dropArmed = true;
                    } else if (dropArmed) {
                        // O RPM CAIU: a bola passou pelo shooter.
                        // Para intake e buffer e espera o shooter recuperar.
                        feedPaused = true;
                        dropArmed = false;
                        pauseTimer.reset();
                        intakeOn = false;
                        bufferOn = false;
                        buffer.stop();   // garante que o buffer para na hora
                    }
                } else {
                    // PAUSADO: intake e buffer desligados por um tempo.
                    intakeOn = false;
                    bufferOn = false;

                    if (pauseTimer.seconds() >= PAUSE_AFTER_DROP_SEC) {
                        // Passou o tempo: volta a ligar direto, sem parar.
                        feedPaused = false;
                    }
                }

                if (stateTimer.seconds() >= FEED_TIME_SEC) {
                    // Terminou de atirar: desliga tudo e limpa o controle da pausa.
                    shooterOn = false;
                    intakeOn = false;
                    bufferOn = false;
                    feedPaused = false;
                    dropArmed = false;
                    buffer.stop();   // garantia extra de que o buffer para
                    goTo(DRIVE_ENABLED ? State.DRIVE_FORWARD : State.DONE);
                }
                break;

            case DRIVE_FORWARD:
                // 1ª etapa: anda reto para frente.
                setDrive(DRIVE_POWER, 0.0, 0.0);

                if (stateTimer.seconds() >= DRIVE_TIME_SEC) {
                    goTo(State.TURN_RIGHT);
                }
                break;

            case TURN_RIGHT:
                // Gira no lugar para a direita (sem ir para frente).
                setDrive(0.0, 0.0, TURN_POWER);

                if (stateTimer.seconds() >= TURN_TIME_SEC) {
                    goTo(State.DRIVE_ANGLED);
                }
                break;

            case DRIVE_ANGLED:
                // Segue reto, já virado para a direita.
                setDrive(ANGLED_POWER, 0.0, 0.0);

                if (stateTimer.seconds() >= ANGLED_TIME_SEC) {
                    goTo(State.TURN_LEFT);
                }
                break;

            case TURN_LEFT:
                // Gira para a esquerda: MESMO tempo e MESMA potência do giro
                // para a direita, só com o sinal invertido.
                setDrive(0.0, 0.0, -TURN_POWER);

                if (stateTimer.seconds() >= TURN_TIME_SEC) {
                    goTo(State.DRIVE_STRAIGHT);
                }
                break;

            case DRIVE_STRAIGHT:
                // Última etapa: segue reto na direção original.
                setDrive(FINAL_POWER, 0.0, 0.0);

                if (stateTimer.seconds() >= FINAL_TIME_SEC) {
                    driveOn = false;
                    driveForward = 0.0;
                    driveStrafe = 0.0;
                    driveTurn = 0.0;
                    holdOn = true;   // freia e segura parado
                    goTo(State.DONE);
                }
                break;

            case DONE:
                // Nada a fazer: o robô fica parado até o autônomo acabar.
                holdOn = true;
                break;
        }

        // Telemetria para acompanhar (e ajustar) durante os testes.
        telemetry.addData("Estado", state);
        telemetry.addData("Tempo no estado (s)", "%.2f", stateTimer.seconds());
        telemetry.addData("Shooter RPM atual", "%.1f", shooter.getCurrentRPM());
        telemetry.addData("Shooter RPM alvo", "%.1f", shooter.getTargetRPM());
        telemetry.addData("Shooter pronto?", shooter.isAtTargetVelocity());
        telemetry.addData("Potência do PID", "%.2f", shooter.getPIDPower());
        telemetry.addData("intakeOn", intakeOn);
        telemetry.addData("bufferOn", bufferOn);
        telemetry.addData("Feed pausado?", feedPaused);
        telemetry.addData("Queda armada?", dropArmed);
        telemetry.addData("driveForward", "%.2f", driveForward);
        telemetry.addData("driveStrafe", "%.2f", driveStrafe);
        telemetry.addData("driveTurn", "%.2f", driveTurn);
        telemetry.update();
    }

    /**
     * Liga o DriveCommand com os valores de frente, lateral e giro.
     * Chamado a cada loop pelos estados de movimento.
     */
    private void setDrive(double forward, double strafe, double turn) {
        driveOn = true;
        driveForward = forward;
        driveStrafe = strafe;
        driveTurn = turn;
    }

    /** Troca de estado e zera o cronômetro. */
    private void goTo(State next) {
        state = next;
        stateTimer.reset();
    }
}