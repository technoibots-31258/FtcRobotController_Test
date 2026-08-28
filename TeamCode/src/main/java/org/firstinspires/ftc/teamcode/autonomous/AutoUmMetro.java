package org.firstinspires.ftc.teamcode.autonomous;

import com.pedropathing.follower.Follower;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.autonomous.paths.TestPaths;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;


@Autonomous(
        name = "Pedro - Andar 1 Metro"
)
public class AutoUmMetro extends LinearOpMode {


    private Follower follower;

    private TestPaths paths;


    @Override
    public void runOpMode() {


        follower =
                Constants.createFollower(hardwareMap);


        paths =
                new TestPaths(follower);


        follower.setStartingPose(
                paths.startPose
        );


        waitForStart();


        if (isStopRequested()) {
            return;
        }


        follower.followPath(
                paths.andarUmMetro
        );


        while (opModeIsActive()) {


            follower.update();


            telemetry.addData(
                    "X",
                    follower.getPose().getX()
            );

            telemetry.addData(
                    "Y",
                    follower.getPose().getY()
            );

            telemetry.addData(
                    "Busy",
                    follower.isBusy()
            );

            telemetry.update();
        }
    }
}