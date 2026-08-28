package org.firstinspires.ftc.teamcode.robot;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;


/**
 * Monta os subsystems e commands do robô.
 */
public class RobotContainer {

    public final MecanumDriveSubsystem driveSubsystem;

    private final GamepadEx driverGamepad;


    public RobotContainer(
            HardwareMap hardwareMap,
            GamepadEx driverGamepad
    ) {

        this.driverGamepad = driverGamepad;


        // Cria o drivetrain
        driveSubsystem = new MecanumDriveSubsystem(hardwareMap);


        // Configura comandos padrão
        configureDefaultCommands();
    }


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
}