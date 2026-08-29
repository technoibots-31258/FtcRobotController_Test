package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import com.pedropathing.follower.Follower;

import org.firstinspires.ftc.teamcode.autonomous.paths.PedroAutonomous;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;


@Autonomous(
        name = "Pedro - Andar 1 Metro",
        group = "Autonomous"
)
public class AutoUmMetro extends OpMode {

    private Follower follower;

    private PedroAutonomous paths;

    private int pathState = 0;


    @Override
    public void init() {

        // Cria o Follower
        follower = Constants.createFollower(hardwareMap);


        // Cria os Paths
        paths = new PedroAutonomous(follower);


        // Define posição inicial
        follower.setStartingPose(paths.startPose);


        telemetry.addData("Status", "Inicializado");
        telemetry.update();
    }


    @Override
    public void start() {

        // Inicia o caminho
        follower.followPath(paths.andarUmMetro);

        pathState = 1;
    }


    @Override
    public void loop() {

        // MUITO IMPORTANTE:
        // Atualiza constantemente o Pedro Pathing
        follower.update();


        autonomousPathUpdate();


        telemetry.addData("Path State", pathState);

        telemetry.addData(
                "X",
                follower.getPose().getX()
        );

        telemetry.addData(
                "Y",
                follower.getPose().getY()
        );

        telemetry.addData(
                "Heading",
                Math.toDegrees(
                        follower.getPose().getHeading()
                )
        );

        telemetry.addData(
                "Busy",
                follower.isBusy()
        );

        telemetry.update();
    }


    private void autonomousPathUpdate() {

        switch (pathState) {

            case 1:

                // Espera terminar o Path
                if (!follower.isBusy()) {

                    pathState = 2;

                }

                break;


            case 2:

                // Autônomo terminou
                break;
        }
    }
}