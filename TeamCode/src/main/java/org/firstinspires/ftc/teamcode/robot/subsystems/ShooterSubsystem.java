package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.Iterator;

public class ShooterSubsystem extends SubsystemBase {

    private final DcMotorEx shooter1;
    private final DcMotorEx shooter2;

    // UM controlador só: os dois motores movem a MESMA roda.
    private final PIDController controller;

    private final VoltageSensor voltageSensor;

    // ---------------- Parâmetros (ajuste em campo) ----------------

    // Velocidade desejada da roda, em ticks/s.
    private static final double DEFAULT_TARGET_VELOCITY = 940.0;

    // KF = 1 / velocidade máxima medida com potência 1.0 (bateria cheia).
    // Meça e substitua este valor.
    private double kF = 0.000713;

    // KP maior que o original para recuperar rápido depois de cada bola.
    // Suba aos poucos até a velocidade começar a oscilar, depois reduza ~20%.
    private double kP = 0.035;
    private double kI = 0.00001;
    private double kD = 0.0001;

    // Tensão em que o kF foi medido.
    private static final double NOMINAL_VOLTAGE = 11.16;

    // Filtro passa-baixa da velocidade (0..1). Menor = mais suave, mais atraso.
    private static final double FILTER_ALPHA = 0.35;

    // "Pronto" = dentro de +-3% da meta por pelo menos 80 ms.
    private static final double READY_TOLERANCE = 0.015;
    private static final double READY_HOLD_MS = 150;

    // Intervalo entre leituras de tensão (leitura cara no hub).
    private static final double VOLTAGE_READ_PERIOD_MS = 250.0;

    // ---------------- Estado ----------------

    private double targetVelocity = DEFAULT_TARGET_VELOCITY;
    private boolean enabled = false;

    private double filteredVelocity = 0.0;
    private double batteryVoltage = NOMINAL_VOLTAGE;

    private final ElapsedTime voltageTimer = new ElapsedTime();
    private final ElapsedTime readyTimer = new ElapsedTime();
    private boolean inWindow = false;

    public ShooterSubsystem(HardwareMap hardwareMap) {

        // Evita uma transação com o hub a cada leitura de encoder.
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        shooter1 = hardwareMap.get(DcMotorEx.class, "Shooter1");
        shooter2 = hardwareMap.get(DcMotorEx.class, "Shooter");

        shooter1.setDirection(DcMotor.Direction.REVERSE);
        shooter2.setDirection(DcMotor.Direction.FORWARD);

        shooter1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // Potência direta: o controle é o nosso (feedforward + P).
        shooter1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        controller = new PIDController(kP, kI, kD);

        Iterator<VoltageSensor> iterator =
                hardwareMap.voltageSensor.iterator();
        voltageSensor = iterator.hasNext() ? iterator.next() : null;

        readBattery();
    }

    // ---------------- Controle ----------------

    public void startShooter() {
        if (!enabled) {
            controller.reset();
            inWindow = false;
            enabled = true;
        }
    }

    public void stopShooter() {
        enabled = false;
        inWindow = false;

        shooter1.setPower(0);
        shooter2.setPower(0);

        controller.reset();
    }

    public void reverse() {
        enabled = false;
        inWindow = false;
        controller.reset();

        shooter1.setPower(-1.0);
        shooter2.setPower(-1.0);
    }

    @Override
    public void periodic() {

        // Mantém o filtro atualizado mesmo desligado,
        // assim a leitura já está estável quando ligar.
        double rawVelocity = (getVelocity1() + getVelocity2()) / 2.0;
        filteredVelocity += FILTER_ALPHA * (rawVelocity - filteredVelocity);

        if (!enabled) {
            return;
        }

        if (voltageTimer.milliseconds() >= VOLTAGE_READ_PERIOD_MS) {
            readBattery();
        }

        double feedforward =
                kF * targetVelocity * NOMINAL_VOLTAGE / batteryVoltage;

        double correction =
                controller.calculate(filteredVelocity, targetVelocity);

        double power = clamp(feedforward + correction, 0.0, 1.0);

        // Mesma potência nos dois: eles não brigam entre si.
        shooter1.setPower(power);
        shooter2.setPower(power);

        updateReadyWindow();
    }

    private void updateReadyWindow() {
        double tolerance = targetVelocity * READY_TOLERANCE;
        boolean inside =
                Math.abs(targetVelocity - filteredVelocity) <= tolerance;

        if (inside) {
            if (!inWindow) {
                inWindow = true;
                readyTimer.reset();
            }
        } else {
            inWindow = false;
        }
    }

    private void readBattery() {
        voltageTimer.reset();

        if (voltageSensor != null) {
            double measured = voltageSensor.getVoltage();
            if (measured > 1.0) {
                batteryVoltage = measured;
            }
        }
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    // ---------------- Ajustes em runtime ----------------

    public void setTargetVelocity(double ticksPerSecond) {
        targetVelocity = Math.max(0.0, ticksPerSecond);
    }

    public void setPIDF(double kP, double kI, double kD, double kF) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.kF = kF;
        controller.setPID(kP, kI, kD);
    }

    // ---------------- Leituras ----------------

    public double getVelocity1() {
        return Math.abs(shooter1.getVelocity());
    }

    public double getVelocity2() {
        return Math.abs(shooter2.getVelocity());
    }

    public double getAverageVelocity() {
        return (getVelocity1() + getVelocity2()) / 2.0;
    }

    public double getFilteredVelocity() {
        return filteredVelocity;
    }

    public double getTargetVelocity() {
        return targetVelocity;
    }

    public double getError() {
        return targetVelocity - filteredVelocity;
    }

    public double getPower1() {
        return shooter1.getPower();
    }

    public double getPower2() {
        return shooter2.getPower();
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    /**
     * Verdadeiro só quando a velocidade ficou dentro de +-3% da meta
     * por pelo menos 80 ms. Quando a bola passa e a roda perde
     * velocidade, volta a ser falso até recuperar: use isto para
     * liberar a próxima bola.
     */
    public boolean isAtTargetVelocity() {
        return enabled
                && inWindow
                && readyTimer.milliseconds() >= READY_HOLD_MS;
    }
}