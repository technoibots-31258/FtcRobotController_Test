package org.firstinspires.ftc.teamcode.testes;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.robot.subsystems.MecanumDriveSubsystem;

@Autonomous(name = "HuskyLens - Coletar Bola", group = "Vision")
public class HuskyLensBallTest extends CommandOpMode {

    private HuskyLens huskyLens;
    private MecanumDriveSubsystem drive;


    // =========================================================
    // HUSKYLENS
    // =========================================================

    // Resolução da HuskyLens: 320 x 240
    private static final int CENTER_X = 160;


    // =========================================================
    // BOLA
    // =========================================================

    // ID da cor amarela treinada na HuskyLens
    private static final int YELLOW_ID = 1;


    // =========================================================
    // CENTRALIZAÇÃO
    // =========================================================

    /*
     * A bola pode ficar alguns pixels fora do centro
     * sem precisar fazer correção.
     */
    private static final int DEADZONE = 18;


    // =========================================================
    // STRAFE
    // =========================================================

    /*
     * Quanto maior o erro horizontal,
     * maior será o strafe.
     */
    private static final double KP_STRAFE = 0.003;


    // Limite máximo do strafe
    private static final double MAX_STRAFE = 0.22;


    // =========================================================
    // VELOCIDADE PARA FRENTE
    // =========================================================

    /*
     * Quando está longe.
     */
    private static final double FAST_FORWARD = 0.35;


    /*
     * Quando está chegando.
     */
    private static final double MEDIUM_FORWARD = 0.22;


    /*
     * Última aproximação.
     */
    private static final double SLOW_FORWARD = 0.12;


    // =========================================================
    // PROCURAR BOLA
    // =========================================================

    /*
     * O robô só gira quando NÃO está vendo a bola.
     */
    private static final double SEARCH_POWER = 0.18;


    // =========================================================
    // DISTÂNCIA DA BOLA
    // =========================================================

    /*
     * Parte inferior da bounding box.
     *
     * Quanto mais próximo de 240,
     * mais próxima a bola está da câmera.
     */
    private static final int STOP_BOTTOM = 230;


    /*
     * Segunda proteção.
     *
     * Se a bola ficar muito grande na imagem,
     * consideramos que já chegou.
     */
    private static final int STOP_WIDTH = 140;


    // =========================================================
    // FILTRO DO X
    // =========================================================

    private double filteredX = CENTER_X;

    private boolean firstDetection = true;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize() {

        // =====================================================
        // HUSKYLENS
        // =====================================================

        huskyLens = hardwareMap.get(
                HuskyLens.class,
                "huskylens"
        );


        huskyLens.selectAlgorithm(
                HuskyLens.Algorithm.COLOR_RECOGNITION
        );


        // =====================================================
        // DRIVETRAIN
        // =====================================================

        drive = new MecanumDriveSubsystem(hardwareMap);


        // =====================================================
        // TELEMETRIA
        // =====================================================

        telemetry.addLine("HUSKYLENS - COLETAR BOLA");
        telemetry.addLine("-------------------------");
        telemetry.addLine("HuskyLens: OK");
        telemetry.addLine("Drivetrain: OK");
        telemetry.addLine("");
        telemetry.addLine("Cor: AMARELO");
        telemetry.addData("Color ID", YELLOW_ID);
        telemetry.addLine("");
        telemetry.addLine("Pronto.");

        telemetry.update();
    }


    // =========================================================
    // LOOP PRINCIPAL
    // =========================================================

    @Override
    public void run() {

        // =====================================================
        // PROCURAR BOLA
        // =====================================================

        HuskyLens.Block ball = findYellowBall();


        // =====================================================
        // NÃO ENCONTROU
        // =====================================================

        if (ball == null) {

            /*
             * IMPORTANTE:
             *
             * Só gira quando não está vendo a bola.
             */
            drive.drive(
                    0,
                    0,
                    SEARCH_POWER
            );


            // Reinicia o filtro
            firstDetection = true;
            filteredX = CENTER_X;


            telemetry.clearAll();

            telemetry.addLine("PROCURANDO BOLA");
            telemetry.addLine("-------------------------");
            telemetry.addLine("Girando...");

            telemetry.update();

            return;
        }


        // =====================================================
        // DADOS DA BOLA
        // =====================================================

        int x = ball.x;
        int y = ball.y;

        int width = ball.width;
        int height = ball.height;


        // =====================================================
        // CENTRO INFERIOR DA BOLA
        // =====================================================

        int bottom = y + (height / 2);


        // =====================================================
        // FILTRO DO X
        // =====================================================

        if (firstDetection) {

            filteredX = x;
            firstDetection = false;

        } else {

            /*
             * Suaviza a leitura da HuskyLens.
             *
             * Isso evita pequenas oscilações.
             */
            filteredX =
                    filteredX * 0.80
                            + x * 0.20;
        }


        // =====================================================
        // ERRO HORIZONTAL
        // =====================================================

        double errorX =
                filteredX - CENTER_X;


        // =====================================================
        // VERIFICAR SE CHEGOU PERTO
        // =====================================================

        boolean ballIsClose =
                bottom >= STOP_BOTTOM
                        || width >= STOP_WIDTH;


        if (ballIsClose) {

            /*
             * Chegou muito perto.
             */
            drive.stop();


            telemetry.clearAll();

            telemetry.addLine("BOLA NO COLETOR");
            telemetry.addLine("-------------------------");

            telemetry.addData("X", x);
            telemetry.addData(
                    "X filtrado",
                    "%.1f",
                    filteredX
            );

            telemetry.addData("Y", y);
            telemetry.addData("Width", width);
            telemetry.addData("Height", height);
            telemetry.addData("Bottom", bottom);

            telemetry.addLine("");
            telemetry.addLine("ROBO PARADO");
            telemetry.addLine("PRONTO PARA COLETAR");

            telemetry.update();

            return;
        }


        // =====================================================
        // CALCULAR STRAFE
        // =====================================================

        double strafe = 0;


        if (Math.abs(errorX) > DEADZONE) {

            /*
             * A bola está fora do centro.
             *
             * Corrigimos lateralmente.
             */
            strafe =
                    errorX * KP_STRAFE;


            // Limite máximo
            if (strafe > MAX_STRAFE) {
                strafe = MAX_STRAFE;
            }

            if (strafe < -MAX_STRAFE) {
                strafe = -MAX_STRAFE;
            }
        }


        // =====================================================
        // VELOCIDADE PARA FRENTE
        // =====================================================

        double forward;


        /*
         * Longe
         */
        if (width < 50) {

            forward = FAST_FORWARD;

            /*
             * Distância intermediária
             */
        } else if (width < 80) {

            forward = MEDIUM_FORWARD;

            /*
             * Aproximação final
             */
        } else {

            forward = SLOW_FORWARD;
        }


        // =====================================================
        // IR PARA A BOLA
        // =====================================================

        /*
         * IMPORTANTE:
         *
         * y     = frente
         * x     = strafe
         * rx    = 0
         *
         * O robô NÃO gira enquanto está vendo a bola.
         */
        drive.drive(
                forward,
                strafe,
                0
        );


        // =====================================================
        // TELEMETRIA
        // =====================================================

        telemetry.clearAll();

        telemetry.addLine("BOLA ENCONTRADA");
        telemetry.addLine("-------------------------");

        telemetry.addData(
                "ID",
                ball.id
        );

        telemetry.addData(
                "X",
                x
        );

        telemetry.addData(
                "X filtrado",
                "%.1f",
                filteredX
        );

        telemetry.addData(
                "Y",
                y
        );

        telemetry.addData(
                "Width",
                width
        );

        telemetry.addData(
                "Height",
                height
        );

        telemetry.addData(
                "Bottom",
                bottom
        );

        telemetry.addLine("-------------------------");

        telemetry.addData(
                "Erro X",
                "%.1f",
                errorX
        );

        telemetry.addData(
                "Strafe",
                "%.2f",
                strafe
        );

        telemetry.addData(
                "Frente",
                "%.2f",
                forward
        );

        telemetry.addLine("-------------------------");

        telemetry.addLine("INDO PARA A BOLA");

        telemetry.update();
    }


    // =========================================================
    // ENCONTRAR BOLA AMARELA
    // =========================================================

    private HuskyLens.Block findYellowBall() {

        HuskyLens.Block[] blocks =
                huskyLens.blocks();


        HuskyLens.Block biggestBall = null;


        for (HuskyLens.Block block : blocks) {

            /*
             * Ignora qualquer outra cor.
             */
            if (block.id != YELLOW_ID) {
                continue;
            }


            /*
             * Se houver mais de uma detecção amarela,
             * escolhe a maior.
             */
            if (biggestBall == null
                    || block.width > biggestBall.width) {

                biggestBall = block;
            }
        }


        return biggestBall;
    }
}