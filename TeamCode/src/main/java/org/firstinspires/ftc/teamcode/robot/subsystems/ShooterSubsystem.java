
package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class ShooterSubsystem extends SubsystemBase {

    private final DcMotorEx shooter1;
    private final DcMotorEx shooter2;

    // ===== VELOCIDADES ALVO (ticks/s) =====
    public double TARGET_1 = 950;
    public double TARGET_2 = 1000;

    // ===== GANHOS PID DO MOTOR 1 =====
    public double P1 = 0.0125;
    public double I1 = 0.0;
    public double D1 = 0.0;

    // ===== GANHOS PID DO MOTOR 2 =====
    public double P2 = 0.0012;
    public double I2 = 0.0;
    public double D2 = 0.0;

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

        // Erros individuais
        double error1 = TARGET_1 - velocity1;
        double error2 = TARGET_2 - velocity2;

        // PID do motor 1
        integral1 += error1 * dt;
        double derivative1 = (error1 - lastError1) / dt;

        double power1 = P1 * error1
                + I1 * integral1
                + D1 * derivative1;

        // PID do motor 2
        integral2 += error2 * dt;
        double derivative2 = (error2 - lastError2) / dt;

        double power2 = P2 * error2
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
    public double getVelocity1() {
        return Math.abs(shooter1.getVelocity());
    }

    public double getVelocity2() {
        return Math.abs(shooter2.getVelocity());
    }

    public double getPower1() {
        return shooter1.getPower();
    }

    public double getPower2() {
        return shooter2.getPower();
    }
}
