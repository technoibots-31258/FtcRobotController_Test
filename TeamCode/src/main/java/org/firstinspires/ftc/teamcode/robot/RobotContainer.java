package org.firstinspires.ftc.teamcode.robot;

import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.commands.BufferCommand;
import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.commands.IntakeCommand;
import org.firstinspires.ftc.teamcode.robot.commands.ShooterCommand;

import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;


/**
 * Monta os subsystems e commands do robô.
 */
public class RobotContainer {

    public final MecanumDriveSubsystem driveSubsystem;

    public final IntakeSubsystem intakeSubsystem;

    public final BufferSubsystem bufferSubsystem;

    public final ShooterSubsystem shooterSubsystem;

    private final GamepadEx driverGamepad;


    public RobotContainer(
            HardwareMap hardwareMap,
            GamepadEx driverGamepad
    ) {

        this.driverGamepad =
                driverGamepad;


        // =========================
        // DRIVETRAIN
        // =========================

        driveSubsystem =
                new MecanumDriveSubsystem(
                        hardwareMap
                );


        // =========================
        // INTAKE
        // =========================

        intakeSubsystem =
                new IntakeSubsystem(
                        hardwareMap
                );


        // =========================
        // BUFFER
        // =========================

        bufferSubsystem =
                new BufferSubsystem(
                        hardwareMap
                );


        // =========================
        // SHOOTER
        // =========================

        shooterSubsystem =
                new ShooterSubsystem(
                        hardwareMap
                );


        configureDefaultCommands();

        configureIntake();

        configureBuffer();

        configureShooter();
    }


    private void configureDefaultCommands() {

        // =========================
        // DRIVE
        // =========================

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
    // INTAKE
    // =====================================================

    private void configureIntake() {

        // RB = puxar bola
        new Trigger(
                () -> driverGamepad.getButton(
                        GamepadKeys.Button.LEFT_BUMPER
                )
        ).whileActiveContinuous(

                new IntakeCommand(
                        intakeSubsystem,
                        1.0
                )
        );


        // LB = inverter intake
        new Trigger(
                () -> driverGamepad.getButton(
                        GamepadKeys.Button.A
                )
        ).whileActiveContinuous(

                new IntakeCommand(
                        intakeSubsystem,
                        -1.0
                )
        );
    }


    // =====================================================
    // BUFFER
    // =====================================================

    private void configureBuffer() {

        // A = buffer para frente
        new Trigger(
                () -> driverGamepad.getButton(
                        GamepadKeys.Button.RIGHT_BUMPER
                )
        ).whileActiveContinuous(

                new BufferCommand(
                        bufferSubsystem,
                        1.0
                )
        );


        // B = buffer reverso
        new Trigger(
                () -> driverGamepad.getButton(
                        GamepadKeys.Button.B
                )
        ).whileActiveContinuous(

                new BufferCommand(
                        bufferSubsystem,
                        -1.0
                )
        );
    }


    // =====================================================
    // SHOOTER
    // =====================================================

    private void configureShooter() {

        new Trigger(
                () -> driverGamepad.getTrigger(
                        GamepadKeys.Trigger.RIGHT_TRIGGER
                ) > 0.1
        ).whileActiveContinuous(
                new ShooterCommand(
                        shooterSubsystem,
                        intakeSubsystem,
                        bufferSubsystem
                )
        );
    }
}