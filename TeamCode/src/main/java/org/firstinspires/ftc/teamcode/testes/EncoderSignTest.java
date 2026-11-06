package org.firstinspires.ftc.teamcode.pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Teste Sinal Encoders", group = "Pedro Pathing")
public class EncoderSignTest extends LinearOpMode {
    @Override
    public void runOpMode() {
        DcMotorEx fl = hardwareMap.get(DcMotorEx.class, "frontleft");
        DcMotorEx fr = hardwareMap.get(DcMotorEx.class, "frontright");
        DcMotorEx bl = hardwareMap.get(DcMotorEx.class, "backleft");
        DcMotorEx br = hardwareMap.get(DcMotorEx.class, "backright");

        // COPIE AQUI as mesmas direções que estão no driveConstants do Constants.java
        fl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.REVERSE);
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        br.setDirection(DcMotorSimple.Direction.REVERSE);

        for (DcMotorEx m : new DcMotorEx[]{fl, fr, bl, br}) {
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        waitForStart();
        while (opModeIsActive()) {
            telemetry.addLine("Empurre o robô PARA FRENTE com a mão");
            telemetry.addData("FL", fl.getCurrentPosition());
            telemetry.addData("FR", fr.getCurrentPosition());
            telemetry.addData("BL", bl.getCurrentPosition());
            telemetry.addData("BR", br.getCurrentPosition());
            telemetry.update();
        }
    }
}