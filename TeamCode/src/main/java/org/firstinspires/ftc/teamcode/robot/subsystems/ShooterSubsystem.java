package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;


public class ShooterSubsystem extends SubsystemBase {

    private final DcMotorEx shooter1;
    private final DcMotorEx shooter2;

    // ===== VELOCIDADE ALVO (ticks/s) =====
    // Variável única: os dois motores usam o mesmo alvo.
    private double target = 620;

    // Passo de ajuste pelo d-pad
    public static final double TARGET_STEP = 20;

    // Limites do alvo (ajuste o máximo para o seu motor)
    public static final double TARGET_MIN = 0;
    public static final double TARGET_MAX = 2800;

    // ===== GANHOS PID DO MOTOR 1 =====
    public double P1 = 0.01;
    public double I1 = 0.0;
    public double D1 = 0.0;
    public double F1 = 0.00059;

    // ===== GANHOS PID DO MOTOR 2 =====
    public double P2 = 0.01;
    public double I2 = 0.0;
    public double D2 = 0.0;
    public double F2 = 0.00057;

    private double integral1 = 0;
    private double integral2 = 0;

    private double lastError1 = 0;
    private double lastError2 = 0;

    private long lastTime = 0;

    private boolean pidEnabled = false;

    public ShooterSubsystem(HardwareMap hardwareMap) {
        shooter1 = hardwareMap.get(DcMotorEx.class, "Shooter1");
        shooter2 = hardwareMap.get(DcMotorEx.class, "Shooter");

        shooter1.setDirection(DcMotor.Direction.REVERSE);
        shooter2.setDirection(DcMotor.Direction.REVERSE);

        shooter1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        shooter1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        stopShooter();
    }

    // =====================================================
    // TARGET (ajustável durante a partida)
    // =====================================================

    public double getTarget() {
        return target;
    }

    public void setTarget(double newTarget) {
        target = Math.max(TARGET_MIN, Math.min(TARGET_MAX, newTarget));
    }

    public void increaseTarget() {
        setTarget(target + TARGET_STEP);
    }

    public void decreaseTarget() {
        setTarget(target - TARGET_STEP);
    }

    // Liga o controle PID dos dois motores
    public void startPID() {
        integral1 = 0;
        integral2 = 0;
        lastError1 = 0;
        lastError2 = 0;
        lastTime = System.nanoTime();
        pidEnabled = true;
    }

    @Override
    public void periodic() {
        if (!pidEnabled) return;

        long now = System.nanoTime();
        double dt = (now - lastTime) / 1e9;
        lastTime = now;

        if (dt <= 0 || dt > 0.1) return;

        // Velocidades medidas
        double velocity1 = Math.abs(shooter1.getVelocity());
        double velocity2 = Math.abs(shooter2.getVelocity());

        // Erros individuais (mesmo alvo para os dois)
        double error1 = target - velocity1;
        double error2 = target - velocity2;

        // PID do motor 1
        integral1 += error1 * dt;
        double derivative1 = (error1 - lastError1) / dt;

        double power1 = F1 * target
                + P1 * error1
                + I1 * integral1
                + D1 * derivative1;

        // PID do motor 2
        integral2 += error2 * dt;
        double derivative2 = (error2 - lastError2) / dt;

        double power2 = F2 * target
                + P2 * error2
                + I2 * integral2
                + D2 * derivative2;

        // Limita a potência entre 0 e 1
        power1 = Math.max(0, Math.min(1, power1));
        power2 = Math.max(0, Math.min(1, power2));

        shooter1.setPower(power1);
        shooter2.setPower(power2);

        lastError1 = error1;
        lastError2 = error2;
    }

    // Desliga os motores e o PID
    public void stopShooter() {
        pidEnabled = false;
        integral1 = 0;
        integral2 = 0;

        shooter1.setPower(0);
        shooter2.setPower(0);
    }

    // Leituras para os testes

    // Tolerancia para autorizar o disparo, em RPM
    public static final double SHOOTER_THRESHOLD_RPM = 5;

    // Velocidade real do motor 1 em RPM
    public double getVelocity1() {
        return Math.abs(shooter1.getVelocity());
    }

    // Velocidade real do motor 2 em RPM
    public double getVelocity2() {
        return Math.abs(shooter2.getVelocity());
    }

    // Erro entre o setpoint e a velocidade real do motor 1
    public double getError1() {
        return target - getVelocity1();
    }

    // Erro entre o setpoint e a velocidade real do motor 2
    public double getError2() {
        return target - getVelocity2();
    }

    // Autoriza o disparo somente dentro do threshold
    // Autoriza o disparo somente quando os DOIS motores estão dentro do threshold
    public boolean isReadyToShoot() {
        return pidEnabled
                && Math.abs(getError1()) <= SHOOTER_THRESHOLD_RPM
                && Math.abs(getError2()) <= SHOOTER_THRESHOLD_RPM;
    }


    public double getPower1() {
        return shooter1.getPower();
    }

    public double getPower2() {
        return shooter2.getPower();
    }

    // Gira os dois motores direto, sem PID.
    // Positivo = sentido do chute | Negativo = sentido contrário.
    public void setManualPower(double power1, double power2) {
        pidEnabled = false;   // desliga o PID para o periodic() não sobrescrever
        integral1 = 0;
        integral2 = 0;

        shooter1.setPower(Math.max(-1.0, Math.min(1.0, power1)));
        shooter2.setPower(Math.max(-1.0, Math.min(1.0, power2)));
    }

}