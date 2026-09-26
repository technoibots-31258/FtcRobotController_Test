package org.firstinspires.ftc.teamcode.testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Encoder Direction Test", group = "Test")
public class EncoderDirectionTest extends LinearOpMode {

    DcMotor fr;
    DcMotor fl;
    DcMotor br;
    DcMotor bl;

    @Override
    public void runOpMode() {

        fr = hardwareMap.get(DcMotor.class, "frontright");
        fl = hardwareMap.get(DcMotor.class, "frontleft");
        br = hardwareMap.get(DcMotor.class, "backright");
        bl = hardwareMap.get(DcMotor.class, "backleft");

        fr.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        br.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addLine("ENCODER TEST");
        telemetry.addLine("Pronto");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            telemetry.addData("FR", fr.getCurrentPosition());
            telemetry.addData("FL", fl.getCurrentPosition());
            telemetry.addData("BR", br.getCurrentPosition());
            telemetry.addData("BL", bl.getCurrentPosition());

            telemetry.update();

            sleep(50);
        }
    }
}