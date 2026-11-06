package org.firstinspires.ftc.teamcode.autonomous;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.autonomous.paths.ColetaPath;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.robot.subsystems.HuskyLensSubsystem;

@Autonomous(name = "Auto Principal", group = "Autonomous")
public class AutoPrincipal extends OpMode {

    // false = só anda 1 metro e segura a posição. true = depois alinha na AprilTag.
    private static final boolean ALINHAR_TAG = false;

    private Follower follower;
    private ColetaPath coletaPath;
    private HuskyLensSubsystem huskyLens;

    private static final int ESTADO_SEGUINDO_ROTINA = 1;
    private static final int ESTADO_ALINHANDO_TAG = 2;
    private static final int ESTADO_FINALIZADO = 9;
    private int estado = 0;

    // AprilTag
    private static final int CENTRO_X = 160;
    private static final int TOLERANCIA_X = 20;
    private static final double KP = 0.003;
    private static final double MAX_POTENCIA = 0.25;
    // No Pedro, strafe positivo = esquerda. Tag à direita (erro > 0) => strafe negativo.
    // Se o robô se afastar da tag em vez de centralizar, troque para +1.0
    private static final double SINAL_STRAFE = -1.0;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(ColetaPath.START_POSE);

        coletaPath = new ColetaPath(follower);

        if (ALINHAR_TAG) {
            huskyLens = new HuskyLensSubsystem(hardwareMap);
        }

        telemetry.addLine("Auto pronto. Deixe pelo menos 1,5 m livres à frente.");
        telemetry.update();
    }

    @Override
    public void start() {
        follower.followPath(coletaPath.MainChain, true); // true = segura a posição final
        estado = ESTADO_SEGUINDO_ROTINA;
    }

    @Override
    public void loop() {
        autonomousUpdate();
        follower.update();

        double andadoPol = follower.getPose().getY() - ColetaPath.START_POSE.getY();
        telemetry.addData("Estado", estado);
        telemetry.addData("Andou (cm)", andadoPol * 2.54);
        telemetry.addData("X (desvio lateral, pol)", follower.getPose().getX() - ColetaPath.START_POSE.getX());
        telemetry.addData("Heading", Math.toDegrees(follower.getPose().getHeading()));
        telemetry.addData("Pedro Busy", follower.isBusy());
        telemetry.update();
    }

    private void autonomousUpdate() {
        switch (estado) {
            case ESTADO_SEGUINDO_ROTINA:
                if (!follower.isBusy()) {
                    if (ALINHAR_TAG) {
                        follower.breakFollowing();
                        follower.startTeleopDrive(true); // true = freia quando a potência é 0
                        estado = ESTADO_ALINHANDO_TAG;
                    } else {
                        estado = ESTADO_FINALIZADO; // o follower continua segurando a posição
                    }
                }
                break;

            case ESTADO_ALINHANDO_TAG:
                alinharAprilTag();
                break;

            case ESTADO_FINALIZADO:
                if (ALINHAR_TAG) {
                    follower.setTeleOpDrive(0, 0, 0, true);
                }
                break;
        }
    }

    private void alinharAprilTag() {
        HuskyLens.Block[] tags = huskyLens.lerAprilTags();

        if (tags.length == 0) {
            follower.setTeleOpDrive(0, 0, 0, true);
            telemetry.addLine("Procurando AprilTag...");
            return;
        }

        HuskyLens.Block tag = tags[0];
        int erro = tag.x - CENTRO_X;

        if (Math.abs(erro) <= TOLERANCIA_X) {
            follower.setTeleOpDrive(0, 0, 0, true);
            telemetry.addLine("AprilTag centralizada");
            telemetry.addData("Tag ID", tag.id);
            telemetry.addData("Tag X", tag.x);
            estado = ESTADO_FINALIZADO;
            return;
        }

        double potencia = Math.max(-MAX_POTENCIA, Math.min(MAX_POTENCIA, erro * KP));

        // (forward, strafe, turn, robotCentric)
        follower.setTeleOpDrive(0, SINAL_STRAFE * potencia, 0, true);

        telemetry.addData("Tag ID", tag.id);
        telemetry.addData("Tag X", tag.x);
        telemetry.addData("Erro X", erro);
        telemetry.addData("Potencia", potencia);
    }
}