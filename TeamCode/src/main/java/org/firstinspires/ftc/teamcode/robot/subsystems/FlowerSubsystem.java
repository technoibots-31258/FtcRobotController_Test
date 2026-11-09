package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class FlowerSubsystem extends SubsystemBase {

    private final CRServo flower;

    // AJUSTE: o sinal define a direção; o módulo define a velocidade
    private static final double UP_POWER = 1.0;
    private static final double DOWN_POWER = -1.0;

    // AJUSTE: quanto tempo o servo gira para ir de uma posição à outra
    private static final double MOVE_TIME_MS = 800;

    private boolean isUp = false;       // posição em que ele está/ficou
    private boolean moving = false;
    private boolean targetUp = false;

    private final ElapsedTime timer = new ElapsedTime();

    public FlowerSubsystem(HardwareMap hardwareMap) {
        flower = hardwareMap.get(CRServo.class, "flower");
        flower.setPower(0);
    }

    public void up() {
        startMove(true);
    }

    public void down() {
        startMove(false);
    }

    /** Se estiver em cima, desce; se estiver no chão, sobe. Ignora cliques durante o movimento. */
    public void toggle() {
        if (moving) return;

        if (isUp) {
            down();
        } else {
            up();
        }
    }

    private void startMove(boolean goUp) {
        targetUp = goUp;
        moving = true;
        timer.reset();
        flower.setPower(goUp ? UP_POWER : DOWN_POWER);
    }

    @Override
    public void periodic() {
        if (moving && timer.milliseconds() >= MOVE_TIME_MS) {
            flower.setPower(0);   // para: não força mais
            moving = false;
            isUp = targetUp;
        }
    }

    public boolean isUp() {
        return isUp;
    }

    public boolean isMoving() {
        return moving;
    }
}