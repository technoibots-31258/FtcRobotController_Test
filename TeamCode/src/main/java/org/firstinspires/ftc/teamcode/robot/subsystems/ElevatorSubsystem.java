package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.Constants;

public class ElevatorSubsystem extends SubsystemBase {

    private final DcMotor elevator;

    // =========================
    // POSIÇÕES
    // =========================

    // Posição mais baixa
    public static final int POSICAO_ZERO = 200;

    // AJUSTAR depois do teste real
    public static final int POSICAO_MEIO = 3000;

    // AJUSTAR depois do teste real
    public static final int POSICAO_CIMA = 4000;

    private static final int TOLERANCIA = 20;

    private static final double POTENCIA_MANUAL = 0.8;
    private static final double POTENCIA_AUTO = 1.0;


    public ElevatorSubsystem(HardwareMap hardwareMap) {

        elevator = hardwareMap.get(
                DcMotor.class,
                Constants.ElevatorConstants.ELEVATOR_NAME
        );

        elevator.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        elevator.setMode(
                DcMotor.RunMode.RUN_USING_ENCODER
        );
    }


    // =========================
    // MANUAL
    // =========================

    public void subir() {

        elevator.setMode(
                DcMotor.RunMode.RUN_USING_ENCODER
        );

        elevator.setPower(POTENCIA_MANUAL);
    }


    public void descer() {

        elevator.setMode(
                DcMotor.RunMode.RUN_USING_ENCODER
        );

        elevator.setPower(-POTENCIA_MANUAL);
    }


    public void parar() {

        elevator.setPower(0);
    }


    // =========================
    // POSIÇÃO AUTOMÁTICA
    // =========================

    public void irParaPosicao(int alvo) {

        int atual = elevator.getCurrentPosition();

        // Chegou
        if (Math.abs(alvo - atual) <= TOLERANCIA) {

            parar();
            return;
        }


        // Está abaixo do alvo
        if (atual < alvo) {

            elevator.setPower(POTENCIA_AUTO);
        }

        // Está acima do alvo
        else {

            elevator.setPower(-POTENCIA_AUTO);
        }
    }


    public boolean chegouNaPosicao(int alvo) {

        return Math.abs(
                alvo - elevator.getCurrentPosition()
        ) <= TOLERANCIA;
    }


    public int getEncoderPosition() {

        return elevator.getCurrentPosition();
    }
}