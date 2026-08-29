package org.firstinspires.ftc.teamcode.robot.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.robot.subsystems.ElevatorSubsystem;

public class ElevatorCommand extends CommandBase {

    private final ElevatorSubsystem elevator;
    private final GamepadEx gamepad;


    // Posição automática atual
    private Integer alvo = null;


    // Estado anterior dos botões
    private boolean xAnterior = false;
    private boolean yAnterior = false;
    private boolean bAnterior = false;


    public ElevatorCommand(
            ElevatorSubsystem elevator,
            GamepadEx gamepad
    ) {

        this.elevator = elevator;
        this.gamepad = gamepad;

        addRequirements(elevator);
    }


    @Override
    public void execute() {

        // =========================
        // BOTÕES
        // =========================

        boolean rb = gamepad.getButton(
                GamepadKeys.Button.RIGHT_BUMPER
        );

        boolean lb = gamepad.getButton(
                GamepadKeys.Button.LEFT_BUMPER
        );

        boolean x = gamepad.getButton(
                GamepadKeys.Button.X
        );

        boolean y = gamepad.getButton(
                GamepadKeys.Button.Y
        );

        boolean b = gamepad.getButton(
                GamepadKeys.Button.B
        );


        // =========================
        // CONTROLE MANUAL
        // =========================

        if (rb) {

            alvo = null;

            elevator.subir();

        } else if (lb) {

            alvo = null;

            elevator.descer();

        } else {


            // =========================
            // X = POSIÇÃO ZERO
            // =========================

            if (x && !xAnterior) {

                alvo = ElevatorSubsystem.POSICAO_ZERO;
            }


            // =========================
            // Y = POSIÇÃO MEIO
            // =========================

            if (y && !yAnterior) {

                alvo = ElevatorSubsystem.POSICAO_MEIO;
            }


            // =========================
            // B = POSIÇÃO CIMA
            // =========================

            if (b && !bAnterior) {

                alvo = ElevatorSubsystem.POSICAO_CIMA;
            }


            // =========================
            // MOVE PARA O ALVO
            // =========================

            if (alvo != null) {

                elevator.irParaPosicao(alvo);

                if (elevator.chegouNaPosicao(alvo)) {

                    alvo = null;
                }

            } else {

                elevator.parar();
            }
        }


        // Guarda estado dos botões
        xAnterior = x;
        yAnterior = y;
        bAnterior = b;
    }
}