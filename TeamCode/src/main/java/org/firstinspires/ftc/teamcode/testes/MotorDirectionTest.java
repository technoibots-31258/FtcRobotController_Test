package org.firstinspires.ftc.teamcode.testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.robot.Constants.DriveConstants;

@TeleOp(name = "Teste Direcao Motores", group = "Pedro Pathing")
public class MotorDirectionTest extends LinearOpMode {
    @Override
    public void runOpMode() {
        DcMotorEx fl = hardwareMap.get(DcMotorEx.class, DriveConstants.FRONT_LEFT_NAME);
        DcMotorEx fr = hardwareMap.get(DcMotorEx.class, DriveConstants.FRONT_RIGHT_NAME);
        DcMotorEx bl = hardwareMap.get(DcMotorEx.class, DriveConstants.BACK_LEFT_NAME);
        DcMotorEx br = hardwareMap.get(DcMotorEx.class, DriveConstants.BACK_RIGHT_NAME);

        // Todos começam em FORWARD (padrão). Ajuste aqui conforme o teste.
        fl.setDirection(DcMotorSimple.Direction.FORWARD);
        fr.setDirection(DcMotorSimple.Direction.FORWARD);
        bl.setDirection(DcMotorSimple.Direction.FORWARD);
        br.setDirection(DcMotorSimple.Direction.FORWARD);

        waitForStart();
        while (opModeIsActive()) {
            fl.setPower(gamepad1.x ? 0.3 : 0);   // X = frontLeft
            fr.setPower(gamepad1.y ? 0.3 : 0);   // Y = frontRight
            bl.setPower(gamepad1.a ? 0.3 : 0);   // A = backLeft
            br.setPower(gamepad1.b ? 0.3 : 0);   // B = backRight

            telemetry.addData("FL enc", fl.getCurrentPosition());
            telemetry.addData("FR enc", fr.getCurrentPosition());
            telemetry.addData("BL enc", bl.getCurrentPosition());
            telemetry.addData("BR enc", br.getCurrentPosition());
            telemetry.update();
        }
    }
}