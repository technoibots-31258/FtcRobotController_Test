package org.firstinspires.ftc.teamcode.autonomous;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.commands.DriveCommand;
import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;

/**
 * AUTÔNOMO SIMPLES: SÓ ANDA PARA FRENTE
 *
 * Não usa shooter, buffer nem intake. Serve como plano B (por exemplo, se o
 * shooter estiver com problema) e também para testar a tração isoladamente:
 * se este autônomo não anda direito, o problema está no drive, não no tiro.
 *
 * Usa a mesma técnica do AutoShootAndDrive: variáveis boolean funcionam como
 * "botões virtuais" que ligam os commands via Trigger.
 */
@Autonomous(name = "Só da uma andadinha", group = "Autonomo")
public class Autodriveonly extends CommandOpMode {

    // =====================================================
    // AJUSTES
    // =====================================================

    /** Espera antes de andar (útil se a aliança parceira precisa passar antes). */
    private static final double START_DELAY_SEC = 0.0;

    /** Tempo andando para frente. */
    private static final double DRIVE_TIME_SEC = 1.0;

    /** Potência (0 a 1). 0.5 = mediana. Se andar para trás, use -0.5. */
    private static final double DRIVE_POWER = 0.5;

    // =====================================================
    // ESTADO
    // =====================================================

    private final ElapsedTime timer = new ElapsedTime();
    private boolean firstLoop = true;

    private boolean driveOn = false;
    private boolean holdOn = false;

    private MecanumDriveSubsystem drive;

    @Override
    public void initialize() {

        // Limpa restos de outros OpModes.
        reset();

        drive = new MecanumDriveSubsystem(hardwareMap);

        // Anda para frente enquanto driveOn == true.
        new Trigger(() -> driveOn).whileActiveContinuous(
                new DriveCommand(drive, () -> DRIVE_POWER, () -> 0.0, () -> 0.0)
        );

        // Segura parado (tudo zero) depois de andar.
        new Trigger(() -> holdOn).whileActiveContinuous(
                new DriveCommand(drive, () -> 0.0, () -> 0.0, () -> 0.0)
        );

        telemetry.addLine("Pronto. Aperte START para andar.");
        telemetry.update();
    }

    @Override
    public void run() {

        // Roda o scheduler (Triggers, commands e periodic dos subsystems).
        super.run();

        if (firstLoop) {
            timer.reset();
            firstLoop = false;
        }

        double t = timer.seconds();

        if (t < START_DELAY_SEC) {
            // Ainda esperando o início.
            driveOn = false;
            holdOn = true;
        } else if (t < START_DELAY_SEC + DRIVE_TIME_SEC) {
            // Janela de andar.
            driveOn = true;
            holdOn = false;
        } else {
            // Acabou: para e segura.
            driveOn = false;
            holdOn = true;
        }

        telemetry.addData("Tempo (s)", "%.2f", t);
        telemetry.addData("Andando?", driveOn);
        telemetry.update();
    }
}