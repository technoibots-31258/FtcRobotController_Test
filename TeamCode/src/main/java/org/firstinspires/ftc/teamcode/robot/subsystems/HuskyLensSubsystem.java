package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class HuskyLensSubsystem extends SubsystemBase {

    private final HuskyLens huskyLens;

    // Tamanho da imagem da HuskyLens
    public static final int IMAGEM_LARGURA = 320;

    // Centro horizontal da imagem
    public static final int CENTRO_X = 160;

    // Tolerância para considerar centralizado
    public static final int TOLERANCIA_X = 12;

    public HuskyLensSubsystem(HardwareMap hardwareMap) {

        huskyLens = hardwareMap.get(
                HuskyLens.class,
                "huskylens"
        );

        // AprilTag
        huskyLens.selectAlgorithm(
                HuskyLens.Algorithm.TAG_RECOGNITION
        );
    }

    public HuskyLens.Block[] lerAprilTags() {
        return huskyLens.blocks();
    }

    public int getErroHorizontal(HuskyLens.Block tag) {
        return tag.x - CENTRO_X;
    }

    public boolean estaCentralizada(HuskyLens.Block tag) {

        return Math.abs(
                getErroHorizontal(tag)
        ) <= TOLERANCIA_X;
    }

    public int getId(HuskyLens.Block tag) {
        return tag.id;
    }

    public int getX(HuskyLens.Block tag) {
        return tag.x;
    }

    public int getY(HuskyLens.Block tag) {
        return tag.y;
    }

    public int getWidth(HuskyLens.Block tag) {
        return tag.width;
    }

    public int getHeight(HuskyLens.Block tag) {
        return tag.height;
    }
}