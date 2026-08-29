package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.qualcomm.hardware.dfrobot.HuskyLens;

import org.firstinspires.ftc.teamcode.robot.subsystems.HuskyLensSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;

public class CenterAprilTagCommand extends CommandBase {

    private final HuskyLensSubsystem huskyLens;
    private final MecanumDriveSubsystem drive;

    // =========================
    // AJUSTES
    // =========================

    // Potência de correção esquerda/direita
    private static final double POTENCIA_LATERAL = 0.40;

    // Potência para aproximar/afastar
    private static final double POTENCIA_DISTANCIA = 0;

    // Largura desejada da AprilTag na imagem
    // Vamos ajustar depois.
    private static final int TAG_WIDTH_ALVO = 150;

    // Tolerância da distância
    private static final int TOLERANCIA_WIDTH = 5;


    public CenterAprilTagCommand(
            HuskyLensSubsystem huskyLens,
            MecanumDriveSubsystem drive
    ) {

        this.huskyLens = huskyLens;
        this.drive = drive;

        addRequirements(
                huskyLens,
                drive
        );
    }


    @Override
    public void execute() {

        HuskyLens.Block[] tags =
                huskyLens.lerAprilTags();


        // Nenhuma AprilTag encontrada
        if (tags.length == 0) {

            drive.stop();

            return;
        }


        // Por enquanto usamos a primeira tag encontrada
        HuskyLens.Block tag = tags[0];


        int erroX =
                huskyLens.getErroHorizontal(tag);

        int erroWidth =
                TAG_WIDTH_ALVO - tag.width;


        double x = 0;
        double y = 0;


        // =========================
        // CENTRALIZAÇÃO HORIZONTAL
        // =========================

        if (Math.abs(erroX)
                > HuskyLensSubsystem.TOLERANCIA_X) {

            if (erroX < 0) {

                // Tag está à esquerda
                x = -POTENCIA_LATERAL;

            } else {

                // Tag está à direita
                x = POTENCIA_LATERAL;
            }
        }


        // =========================
        // DISTÂNCIA
        // =========================

        if (Math.abs(erroWidth)
                > TOLERANCIA_WIDTH) {

            if (erroWidth > 0) {

                // Tag pequena = robô longe
                y = POTENCIA_DISTANCIA;

            } else {

                // Tag grande = robô perto
                y = -POTENCIA_DISTANCIA;
            }
        }


        // =========================
        // MOVIMENTO
        // =========================

        drive.drive(
                y,
                x,
                0
        );
    }


    @Override
    public void end(boolean interrupted) {

        drive.stop();
    }
}