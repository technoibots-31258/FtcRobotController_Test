package org.firstinspires.ftc.teamcode.robot;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.commands.BufferCommand;
import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.commands.ExpelCommand;
import org.firstinspires.ftc.teamcode.robot.commands.IntakeCommand;
import org.firstinspires.ftc.teamcode.robot.commands.ShooterCommand;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.FlowerSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;

/**
 * MODO 1 CONTROLE : o controle 1 (gamepad1) faz tudo.
 *                   Coleta = LT | Flor = L1 | (sem expel)
 * MODO 2 CONTROLES: controle 1 = tração + chute;
 *                   controle 2 (gamepad2) = intake/buffer + flor + expel.
 *                   Coleta = X | Flor = Y | Expel = A  (controle 2)
 */
public class RobotContainer {

    public final MecanumDriveSubsystem driveSubsystem;
    public final IntakeSubsystem intakeSubsystem;
    public final BufferSubsystem bufferSubsystem;
    public final ShooterSubsystem shooterSubsystem;
    public final FlowerSubsystem flowerSubsystem;

    private final GamepadEx driverGamepad;     // user 1
    private final GamepadEx operatorGamepad;   // user 2

    // false = 1 controle | true = 2 controles
    private boolean twoControllers = true;

    /** Construtor mais simples: passe gamepad1 e gamepad2 do OpMode. */
    public RobotContainer(
            HardwareMap hardwareMap,
            Gamepad gamepad1,
            Gamepad gamepad2
    ) {
        this(hardwareMap, new GamepadEx(gamepad1), new GamepadEx(gamepad2));
    }

    /** Construtor com os GamepadEx já criados. */
    public RobotContainer(
            HardwareMap hardwareMap,
            GamepadEx driverGamepad,
            GamepadEx operatorGamepad
    ) {

        this.driverGamepad = driverGamepad;
        this.operatorGamepad = operatorGamepad;

        driveSubsystem = new MecanumDriveSubsystem(hardwareMap);
        intakeSubsystem = new IntakeSubsystem(hardwareMap);
        bufferSubsystem = new BufferSubsystem(hardwareMap);
        shooterSubsystem = new ShooterSubsystem(hardwareMap);
        flowerSubsystem = new FlowerSubsystem(hardwareMap);

        configureDefaultCommands();
        configureModeSwitch();
        configureIntake();
        configureBuffer();
        configureShooter();
        configureFlower();
        configureExpel();
        configureTargetAdjust();   // <-- nova
    }

    // =====================================================
    // MODO DE PILOTAGEM
    // =====================================================

    /**
     * Gamepad que controla intake, buffer e flor no modo atual.
     * 1 controle  -> controle 1
     * 2 controles -> controle 2
     * É consultado a cada ciclo, então a troca de modo vale na hora.
     */
    private GamepadEx intakeGamepad() {
        return twoControllers ? operatorGamepad : driverGamepad;
    }

    /** Botão de COLETA do intake: LT do gamepad que controla o intake. */
    /**
     * Botão de COLETA do intake.
     * 2 controles -> botão X do controle 2
     * 1 controle  -> LT do controle 1
     */
    private boolean collectPressed() {
        if (twoControllers) {
            return operatorGamepad.getButton(GamepadKeys.Button.X);
        }
        return driverGamepad.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.1;
    }

    /**
     * Botão da FLOR.
     * 2 controles -> botão Y do controle 2
     * 1 controle  -> L1 (LEFT_BUMPER) do controle 1
     */
    private boolean flowerPressed() {
        if (twoControllers) {
            return operatorGamepad.getButton(GamepadKeys.Button.Y);
        }
        return driverGamepad.getButton(GamepadKeys.Button.LEFT_BUMPER);
    }

    /** Botão de EXPELIR: A do controle 2, somente no modo 2 controles. */
    private boolean expelPressed() {
        return twoControllers
                && operatorGamepad.getButton(GamepadKeys.Button.A);
    }
    /** RT do controle 1: chute automático. */
    private boolean shootPressed() {
        return driverGamepad.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER) > 0.1;
    }

    public boolean isTwoControllers() {
        return twoControllers;
    }

    /** Pode ser chamado no init do OpMode para escolher o modo inicial. */
    public void setTwoControllers(boolean twoControllers) {
        this.twoControllers = twoControllers;
    }

    private void toggleMode() {
        twoControllers = !twoControllers;
    }

    private void configureModeSwitch() {

        // Share + Options juntos, no controle 1
        new Trigger(
                () -> driverGamepad.getButton(GamepadKeys.Button.BACK)
                        && driverGamepad.getButton(GamepadKeys.Button.START)
        ).whenActive(
                new InstantCommand(this::toggleMode)
        );
    }

    /**
     * Chame uma vez por ciclo no OpMode, ANTES de rodar o scheduler.
     * Atualiza o estado dos botões dos dois controles.
     */
    public void update() {
        driverGamepad.readButtons();
        operatorGamepad.readButtons();
    }

    // =====================================================
    // DRIVE (sempre controle 1)
    // =====================================================

    private void configureDefaultCommands() {

        driveSubsystem.setDefaultCommand(

                new DriveCommand(

                        driveSubsystem,

                        () -> driverGamepad.getLeftY(),

                        driverGamepad::getLeftX,

                        driverGamepad::getRightX
                )
        );
    }

    // =====================================================
    // INTAKE (coleta: LT)
    // =====================================================

    private void configureIntake() {

        new Trigger(
                this::collectPressed
        ).whileActiveContinuous(
                new IntakeCommand(
                        intakeSubsystem,
                        -1.0
                )
        );

        new Trigger(
                this::collectPressed
        ).whileActiveContinuous(
                new BufferCommand(
                        bufferSubsystem,
                        0
                )
        );
    }



    // =====================================================
    // BUFFER (acompanha o gamepad do intake)
    // =====================================================

    private void configureBuffer() {

        // RB: intake + buffer simultaneamente para alimentar várias bolas
        new Trigger(
                () -> intakeGamepad().getButton(
                        GamepadKeys.Button.RIGHT_BUMPER
                )
        ).whileActiveContinuous(

                new ParallelCommandGroup(

                        new IntakeCommand(
                                intakeSubsystem,
                                -1.0
                        ),

                        new BufferCommand(
                                bufferSubsystem,
                                -1.0
                        )
                )
        );

        // B: buffer no sentido reverso
        new Trigger(
                () -> intakeGamepad().getButton(
                        GamepadKeys.Button.B
                )
        ).whileActiveContinuous(
                new BufferCommand(
                        bufferSubsystem,
                        1.0
                )
        );
    }

    // =====================================================
    // SHOOTER (sempre controle 1)
    // =====================================================

    private void configureShooter() {

        new Trigger(
                () -> driverGamepad.getTrigger(
                        GamepadKeys.Trigger.RIGHT_TRIGGER
                ) > 0.1
        ).whileActiveContinuous(
                new ShooterCommand(
                        shooterSubsystem, intakeSubsystem, bufferSubsystem
                )
        );
    }

    // =====================================================
    // FLOWER (L1 no modo 1 | Y do controle 2 no modo 2)
    // Se estiver em cima, desce; se estiver no chão, sobe.
    // =====================================================

    private void configureFlower() {

        new Trigger(
                this::flowerPressed
        ).whenActive(
                new InstantCommand(
                        flowerSubsystem::toggle,
                        flowerSubsystem
                )
        );
    }

    // =====================================================
    // EXPEL (A do controle 2, só no modo 2 controles)
    // Segure para expelir as bolinhas; solte para parar.
    // =====================================================

    private void configureExpel() {

        new Trigger(
                this::expelPressed
        ).whileActiveContinuous(
                new ExpelCommand(
                        intakeSubsystem,
                        bufferSubsystem,
                        shooterSubsystem
                )
        );
    }

    // =====================================================
// AJUSTE DO TARGET DO SHOOTER (D-pad do controle 1)
// Cima: +20 | Baixo: -20
// =====================================================

    private void configureTargetAdjust() {

        // Controle 2 no modo 2 controles.
        // No modo 1 controle, o intakeGamepad() é o controle 1, então o
        // d-pad continua funcionando mesmo sem o segundo controle.
        // InstantCommand SEM requisito: não interrompe o ShooterCommand.
        new Trigger(
                () -> intakeGamepad().getButton(GamepadKeys.Button.DPAD_UP)
        ).whenActive(
                new InstantCommand(shooterSubsystem::increaseTarget)
        );

        new Trigger(
                () -> intakeGamepad().getButton(GamepadKeys.Button.DPAD_DOWN)
        ).whenActive(
                new InstantCommand(shooterSubsystem::decreaseTarget)
        );
    }

}