package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.teamcode.autonomous.paths.ColetaPath;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.robot.subsystems.HuskyLensSubsystem;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;

@Autonomous(
        name = "Auto Principal",
        group = "Autonomous"
)
public class AutoPrincipal extends OpMode {

    private Follower follower;
    private ColetaPath coletaPath;
    private MecanumDriveSubsystem drive;
    private HuskyLensSubsystem huskyLens;

    // =========================
    // ESTADOS
    // =========================
    private static final int ESTADO_SEGUINDO_ROTINA = 1;
    private static final int ESTADO_ALINHANDO_TAG = 2;
    private static final int ESTADO_FINALIZADO = 9;

    private int estado = 0;


    // AprilTag
    private static final int CENTRO_X = 160;
    private static final int TOLERANCIA_X = 20;

    private static final double KP = 0.003;
    private static final double MAX_POTENCIA = 0.25;


    @Override
    public void init() {

        follower = Constants.createFollower(hardwareMap);

        coletaPath = new ColetaPath(follower);

        // O primeiro ponto do path é a posição inicial.
        follower.setStartingPose(
                new Pose(56.000, 8.000, Math.toRadians(90))
        );

        drive = new MecanumDriveSubsystem(hardwareMap);
        huskyLens = new HuskyLensSubsystem(hardwareMap);

        telemetry.addLine("Auto inicializado");
        telemetry.update();
    }


    @Override
    public void start() {

        // true = holdEnd, mantém o robô corrigindo a posição final
        follower.followPath(coletaPath.MainChain, true);

        estado = ESTADO_SEGUINDO_ROTINA;
    }


    @Override
    public void loop() {

        follower.update();
        autonomousUpdate();

        telemetry.addData("Estado", estado);
        telemetry.addData("X", follower.getPose().getX());
        telemetry.addData("Y", follower.getPose().getY());
        telemetry.addData("Heading", Math.toDegrees(follower.getPose().getHeading()));
        telemetry.addData("Pedro Busy", follower.isBusy());
        telemetry.update();
    }


    private void autonomousUpdate() {

        switch (estado) {

            case ESTADO_SEGUINDO_ROTINA:

                if (!follower.isBusy()) {
                    follower.breakFollowing(); // solta o controle dos motores
                    estado = ESTADO_ALINHANDO_TAG;
                }

                break;


            case ESTADO_ALINHANDO_TAG:

                alinharAprilTag();

                break;


            case ESTADO_FINALIZADO:

                drive.stop();

                break;
        }
    }


    private void alinharAprilTag() {

        HuskyLens.Block[] tags = huskyLens.lerAprilTags();

        if (tags.length == 0) {

            drive.stop();
            telemetry.addLine("Procurando AprilTag...");
            return;
        }

        HuskyLens.Block tag = tags[0];

        int erro = tag.x - CENTRO_X;


        if (Math.abs(erro) <= TOLERANCIA_X) {

            drive.stop();

            telemetry.addLine("AprilTag centralizada");
            telemetry.addData("Tag ID", tag.id);
            telemetry.addData("Tag X", tag.x);

            estado = ESTADO_FINALIZADO;

            return;
        }


        double potencia = erro * KP;

        if (potencia > MAX_POTENCIA) {
            potencia = MAX_POTENCIA;
        } else if (potencia < -MAX_POTENCIA) {
            potencia = -MAX_POTENCIA;
        }

        drive.drive(0, potencia, 0);

        telemetry.addData("Tag ID", tag.id);
        telemetry.addData("Tag X", tag.x);
        telemetry.addData("Erro X", erro);
        telemetry.addData("Potencia", potencia);
    }
}