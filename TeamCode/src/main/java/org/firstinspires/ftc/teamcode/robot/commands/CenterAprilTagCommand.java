package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.hardware.dfrobot.HuskyLens;

import org.firstinspires.ftc.teamcode.robot.subsystems.HuskyLensSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;

public class CenterAprilTagCommand extends CommandBase {

    private final HuskyLensSubsystem huskyLens;
    private final MecanumDriveSubsystem drive;
    private final GamepadEx gamepad;

    // =========================
    // AJUSTE AUTOMÁTICO
    // =========================

    private static final double KP_LATERAL = 0.003;

    private static final double MAX_POTENCIA_LATERAL = 0.25;

    private static final int TOLERANCIA_X = 20;


    // =========================
    // DISTÂNCIA
    // =========================

    // Valor provisório.
    // Vamos calibrar depois.
    private static final int TAG_WIDTH_ALVO = 80;

    private static final int TOLERANCIA_WIDTH = 5;

    private static final double POTENCIA_DISTANCIA = 0.15;


    // =========================
    // AJUSTE MANUAL
    // =========================

    // Quanto do joystick permanece disponível
    // enquanto o LT está pressionado.
    private static final double MANUAL_FACTOR = 0.25;


    public CenterAprilTagCommand(
            HuskyLensSubsystem huskyLens,
            MecanumDriveSubsystem drive,
            GamepadEx gamepad
    ) {

        this.huskyLens = huskyLens;
        this.drive = drive;
        this.gamepad = gamepad;

        addRequirements(
                huskyLens,
                drive
        );
    }


    @Override
    public void execute() {

        HuskyLens.Block[] tags =
                huskyLens.lerAprilTags();


        // =========================
        // JOYSTICK MANUAL
        // =========================

        double manualY =
                gamepad.getLeftY() * MANUAL_FACTOR;

        double manualX =
                gamepad.getLeftX() * MANUAL_FACTOR;

        double manualRotation =
                gamepad.getRightX() * MANUAL_FACTOR;


        // =========================
        // NENHUMA TAG
        // =========================

        if (tags.length == 0) {

            // Mesmo sem tag, o piloto
            // ainda consegue fazer pequenos ajustes.
            drive.drive(
                    -manualY,
                    manualX,
                    manualRotation
            );

            return;
        }


        // Por enquanto usa a primeira tag
        HuskyLens.Block tag = tags[0];


        // =========================
        // CORREÇÃO LATERAL
        // =========================

        int erroX =
                huskyLens.getErroHorizontal(tag);

        double automaticX = 0;


        if (Math.abs(erroX) > TOLERANCIA_X) {

            automaticX =
                    erroX * KP_LATERAL;


            // Limita potência
            if (automaticX > MAX_POTENCIA_LATERAL) {

                automaticX =
                        MAX_POTENCIA_LATERAL;

            } else if (
                    automaticX < -MAX_POTENCIA_LATERAL
            ) {

                automaticX =
                        -MAX_POTENCIA_LATERAL;
            }
        }


        // =========================
        // DISTÂNCIA
        // =========================

        int erroWidth =
                TAG_WIDTH_ALVO - tag.width;

        double automaticY = 0;


        if (Math.abs(erroWidth)
                > TOLERANCIA_WIDTH) {

            if (erroWidth > 0) {

                // Tag pequena = longe
                automaticY =
                        POTENCIA_DISTANCIA;

            } else {

                // Tag grande = perto
                automaticY =
                        -POTENCIA_DISTANCIA;
            }
        }


        // =========================
        // SOMA AUTOMÁTICO + MANUAL
        // =========================

        double finalY =
                automaticY - manualY;

        double finalX =
                automaticX + manualX;

        double finalRotation =
                manualRotation;


        // =========================
        // DRIVE
        // =========================

        drive.drive(
                finalY,
                finalX,
                finalRotation
        );
    }


    @Override
    public void end(boolean interrupted) {

        drive.stop();
    }
}