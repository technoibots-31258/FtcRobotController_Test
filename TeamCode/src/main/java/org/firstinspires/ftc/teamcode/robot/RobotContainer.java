package org.firstinspires.ftc.teamcode.robot;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.commands.ElevatorCommand;
import org.firstinspires.ftc.teamcode.robot.subsystems.ElevatorSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;


/**
 * Monta os subsystems e commands do robô.
 */
public class RobotContainer {

    public final MecanumDriveSubsystem driveSubsystem;

    public final ElevatorSubsystem elevatorSubsystem;

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
        // COMMANDS
        // =========================

        configureDefaultCommands();
    }


    private void configureDefaultCommands() {

        // Drivetrain
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
}