package org.firstinspires.ftc.teamcode.robot;

import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.commands.CenterAprilTagCommand;
import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.commands.ElevatorCommand;

import org.firstinspires.ftc.teamcode.robot.subsystems.ElevatorSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.HuskyLensSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;


/**
 * Monta os subsystems e commands do robô.
 */
public class RobotContainer {

    public final MecanumDriveSubsystem driveSubsystem;

    public final ElevatorSubsystem elevatorSubsystem;

    public final HuskyLensSubsystem huskyLensSubsystem;

    private final GamepadEx driverGamepad;


    public RobotContainer(
            HardwareMap hardwareMap,
            GamepadEx driverGamepad
    ) {

        this.driverGamepad = driverGamepad;


        // =========================
        // DRIVETRAIN
        // =========================

        driveSubsystem =
                new MecanumDriveSubsystem(hardwareMap);


        // =========================
        // ELEVADOR
        // =========================

        elevatorSubsystem =
                new ElevatorSubsystem(hardwareMap);


        // =========================
        // HUSKYLENS
        // =========================

        huskyLensSubsystem =
                new HuskyLensSubsystem(hardwareMap);


        configureDefaultCommands();

        configureAprilTag();
    }


    private void configureDefaultCommands() {

        // Drivetrain normal
        driveSubsystem.setDefaultCommand(

                new DriveCommand(
                        driveSubsystem,

                        () -> driverGamepad.getLeftY(),

                        driverGamepad::getLeftX,

                        driverGamepad::getRightX
                )
        );


        // Elevador
        elevatorSubsystem.setDefaultCommand(

                new ElevatorCommand(
                        elevatorSubsystem,
                        driverGamepad
                )
        );
    }


    private void configureAprilTag() {

        new Trigger(
                () -> driverGamepad.getButton(
                        GamepadKeys.Button.A
                )
        ).whileActiveContinuous(

                new CenterAprilTagCommand(
                        huskyLensSubsystem,
                        driveSubsystem
                )
        );
    }
}