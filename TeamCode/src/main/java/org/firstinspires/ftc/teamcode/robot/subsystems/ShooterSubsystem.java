package org.firstinspires.ftc.teamcode.robot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.Iterator;

/**
 * Dois flywheels, cada um com alvo, estado PID e saída independentes.
 *
 * Não usa feedforward (kS/kV): a potência é calculada somente por P + I + D.
 * O erro é normalizado pelo alvo de cada roda para manter os ganhos em uma
 * escala útil para uma saída de potência entre 0 e 1.
 */
public class ShooterSubsystem extends SubsystemBase {

    private final DcMotorEx shooter1;
    private final DcMotorEx shooter2;
    private final VoltageSensor voltageSensor;

    // CONFIRMAR pelo modelo e pela relação de transmissão de cada motor.
    private static final double SHOOTER1_TICKS_PER_REV = 800;
    private static final double SHOOTER2_TICKS_PER_REV = 1000;

    // Compatibilidade com a interface antiga, que recebia ticks/s.
    private static final double DEFAULT_TARGET_VELOCITY = 900;
    private static final double SHOOTER1_SPEED_FACTOR = 1.0;
    private static final double SHOOTER2_SPEED_FACTOR = 1.0;

    // Ganhos iniciais do PI(D), independentes e ajustáveis para cada flywheel.
    // O erro é (alvoRPM - RPMmedido) / alvoRPM, não RPM bruto.
    // Estes são valores iniciais de teste e devem ser refinados pela telemetria.
    private double kP1 = 0.4;
    private double kI1 = 0.75;
    private double kD1 = 0.005;

    private double kP2 = 0.4;
    private double kI2 = 0.75;
    private double kD2 = 0.005;

    private static final double INTEGRAL_LIMIT = 1.5;
    private static final double DERIVATIVE_FILTER_ALPHA = 0.20;
    private static final double MAX_POWER_SLEW_PER_SECOND = 8.0;

    private static final double READY_TOLERANCE = 0.03;
    private static final double READY_HOLD_MS = 150.0;
    private static final double TARGET_CHANGE_ABSOLUTE_RPM = 2.0;
    private static final double TARGET_CHANGE_PERCENT = 0.01;

    private static class PIDState {
        double integral = 0.0;
        double previousTime = 0.0;
        double previousRPM = 0.0;
        double filteredDerivative = 0.0;
        double errorRPM = 0.0;
        double lastPower = 0.0;

        void reset(double now, double currentRPM, boolean resetPower) {
            integral = 0.0;
            previousTime = now;
            previousRPM = currentRPM;
            filteredDerivative = 0.0;
            errorRPM = 0.0;
            if (resetPower) {
                lastPower = 0.0;
            }
        }

        // Re-base timing/derivative after a target change without a power jump.
        void rebase(double now, double currentRPM) {
            previousTime = now;
            previousRPM = currentRPM;
            filteredDerivative = 0.0;
        }
    }

    private final PIDState pid1 = new PIDState();
    private final PIDState pid2 = new PIDState();

    private double targetRPM1 = DEFAULT_TARGET_VELOCITY * SHOOTER1_SPEED_FACTOR
            * 60.0 / SHOOTER1_TICKS_PER_REV;
    private double targetRPM2 = DEFAULT_TARGET_VELOCITY * SHOOTER2_SPEED_FACTOR
            * 60.0 / SHOOTER2_TICKS_PER_REV;

    private double targetVelocity = DEFAULT_TARGET_VELOCITY;
    private boolean enabled = false;

    private double measuredVelocity1 = 0.0; // ticks/s
    private double measuredVelocity2 = 0.0; // ticks/s
    private double measuredVelocity = 0.0;  // média, para compatibilidade
    private double filteredVelocity = 0.0;

    private double currentRPM1 = 0.0;
    private double currentRPM2 = 0.0;
    private double currentRPM = 0.0; // média, somente telemetria
    private double targetRPM = 0.0;  // média, somente telemetria
    private double lastErrorRPM = 0.0;
    private double lastError = 0.0;
    private double lastPower = 0.0;   // média das saídas, somente compatibilidade

    private boolean inWindow = false;
    private final ElapsedTime readyTimer = new ElapsedTime();
    private final ElapsedTime voltageTimer = new ElapsedTime();
    private double batteryVoltage = 0.0;

    public ShooterSubsystem(HardwareMap hardwareMap) {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        shooter1 = hardwareMap.get(DcMotorEx.class, "Shooter1");
        shooter2 = hardwareMap.get(DcMotorEx.class, "Shooter");

        // Mantidas as direções da configuração anterior.
        shooter1.setDirection(DcMotor.Direction.REVERSE);
        shooter2.setDirection(DcMotor.Direction.REVERSE);

        shooter1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter1.setPower(0.0);
        shooter2.setPower(0.0);

        Iterator<VoltageSensor> iterator = hardwareMap.voltageSensor.iterator();
        voltageSensor = iterator.hasNext() ? iterator.next() : null;
        readBattery();

        double now = nowSeconds();
        pid1.reset(now, 0.0, true);
        pid2.reset(now, 0.0, true);
    }

    // =====================================================
    // START / STOP
    // =====================================================

    public void startShooter() {
        if (!enabled) {
            double now = nowSeconds();
            pid1.reset(now, currentRPM1, true);
            pid2.reset(now, currentRPM2, true);
            lastErrorRPM = 0.0;
            lastError = 0.0;
            lastPower = 0.0;
            inWindow = false;
            enabled = true;
        }
    }

    public void stopShooter() {
        enabled = false;
        inWindow = false;
        double now = nowSeconds();
        pid1.reset(now, currentRPM1, true);
        pid2.reset(now, currentRPM2, true);
        lastErrorRPM = 0.0;
        lastError = 0.0;
        lastPower = 0.0;
        shooter1.setPower(0.0);
        shooter2.setPower(0.0);
    }

    /** Executa reverso manual; enquanto chamado, ambos recebem -1. */
    public void reverse() {
        enabled = false;
        inWindow = false;
        double now = nowSeconds();
        pid1.reset(now, currentRPM1, false);
        pid2.reset(now, currentRPM2, false);
        pid1.lastPower = -1.0;
        pid2.lastPower = -1.0;
        lastPower = -1.0;
        shooter1.setPower(-1.0);
        shooter2.setPower(-1.0);
    }

    // =====================================================
    // CONTROL LOOP
    // =====================================================

    @Override
    public void periodic() {
        measuredVelocity1 = getVelocity1();
        measuredVelocity2 = getVelocity2();
        measuredVelocity = (measuredVelocity1 + measuredVelocity2) / 2.0;
        filteredVelocity += 0.35 * (measuredVelocity - filteredVelocity);

        currentRPM1 = measuredVelocity1 * 60.0 / SHOOTER1_TICKS_PER_REV;
        currentRPM2 = measuredVelocity2 * 60.0 / SHOOTER2_TICKS_PER_REV;
        currentRPM = (currentRPM1 + currentRPM2) / 2.0;
        targetRPM = (targetRPM1 + targetRPM2) / 2.0;
        lastErrorRPM = ((targetRPM1 - currentRPM1)
                + (targetRPM2 - currentRPM2)) / 2.0;
        lastError = targetVelocity - measuredVelocity;

        if (voltageTimer.milliseconds() >= 250.0) {
            readBattery();
        }

        double now = nowSeconds();

        if (!enabled) {
            // Não mexe na potência aqui: reverse() deve continuar ativo.
            pid1.previousTime = now;
            pid2.previousTime = now;
            pid1.previousRPM = currentRPM1;
            pid2.previousRPM = currentRPM2;
            pid1.filteredDerivative = 0.0;
            pid2.filteredDerivative = 0.0;
            return;
        }

        if (targetRPM1 <= 0.0) {
            pid1.reset(now, currentRPM1, true);
            shooter1.setPower(0.0);
        } else {
            pid1.errorRPM = targetRPM1 - currentRPM1;
            pid1.lastPower = calculatePID(
                    pid1, targetRPM1, currentRPM1,
                    kP1, kI1, kD1, now
            );
            shooter1.setPower(pid1.lastPower);
        }

        if (targetRPM2 <= 0.0) {
            pid2.reset(now, currentRPM2, true);
            shooter2.setPower(0.0);
        } else {
            pid2.errorRPM = targetRPM2 - currentRPM2;
            pid2.lastPower = calculatePID(
                    pid2, targetRPM2, currentRPM2,
                    kP2, kI2, kD2, now
            );
            shooter2.setPower(pid2.lastPower);
        }

        lastPower = (pid1.lastPower + pid2.lastPower) / 2.0;
        updateReadyWindow();
    }

    /**
     * PID clássico aplicado separadamente em cada motor.
     * Sem kS, kV ou feedforward: saída = kP*erro + kI*integral + kD*derivada.
     * O erro normalizado evita saturar a saída por usar RPM diretamente.
     */
    private double calculatePID(
            PIDState state,
            double target,
            double measured,
            double kP,
            double kI,
            double kD,
            double now
    ) {
        double rawDt = now - state.previousTime;
        boolean validDt = rawDt > 0.0 && rawDt <= 0.10;
        double dt = validDt ? clamp(rawDt, 0.005, 0.05) : 0.0;

        double error = (target - measured) / target;

        // Derivada da medição (com sinal negativo), filtrada para reduzir ruído.
        double derivative = 0.0;
        if (validDt) {
            double measurementRate = (measured - state.previousRPM) / target / rawDt;
            double derivativeAlpha = 1.0 - Math.exp(-rawDt / 0.04);
            state.filteredDerivative += derivativeAlpha
                    * (measurementRate - state.filteredDerivative);
            derivative = -state.filteredDerivative;
        } else {
            state.filteredDerivative = 0.0;
        }

        double candidateIntegral = state.integral;
        if (validDt) {
            candidateIntegral = clamp(
                    state.integral + error * dt,
                    -INTEGRAL_LIMIT,
                    INTEGRAL_LIMIT
            );
        }

        double outputWithCandidateIntegral =
                kP * error + kI * candidateIntegral + kD * derivative;

        // Anti-windup: não acumula integral se a saída satura contra o erro.
        boolean canIntegrate = (outputWithCandidateIntegral >= 0.0
                && outputWithCandidateIntegral <= 1.0)
                || (outputWithCandidateIntegral > 1.0 && error < 0.0)
                || (outputWithCandidateIntegral < 0.0 && error > 0.0);
        if (validDt && canIntegrate) {
            state.integral = candidateIntegral;
        }

        double requestedPower = clamp(
                kP * error + kI * state.integral + kD * derivative,
                0.0,
                1.0
        );

        // Limita a variação da saída para reduzir trancos, sem mudar o alvo.
        double slewDt = validDt ? clamp(rawDt, 0.005, 0.05) : 0.02;
        double maxStep = MAX_POWER_SLEW_PER_SECOND * slewDt;
        double output = clamp(
                requestedPower,
                state.lastPower - maxStep,
                state.lastPower + maxStep
        );
        output = clamp(output, 0.0, 1.0);

        state.previousTime = now;
        state.previousRPM = measured;
        return output;
    }

    private void updateReadyWindow() {
        boolean shooter1Ready = targetRPM1 <= 0.0
                || Math.abs(targetRPM1 - currentRPM1) <= targetRPM1 * READY_TOLERANCE;
        boolean shooter2Ready = targetRPM2 <= 0.0
                || Math.abs(targetRPM2 - currentRPM2) <= targetRPM2 * READY_TOLERANCE;
        boolean atLeastOneTarget = targetRPM1 > 0.0 || targetRPM2 > 0.0;
        boolean inside = atLeastOneTarget && shooter1Ready && shooter2Ready;
// print o shooter1 ready e o shooter2 ready
        if (inside) {
            if (!inWindow) {
                inWindow = true;
                readyTimer.reset();
            }
        } else {
            inWindow = false;
        }
    }

    // =====================================================
    // TARGETS INDIVIDUAIS
    // =====================================================

    /** Define apenas o alvo do motor hardware-map "Shooter1", em RPM. */
    public void setTargetRPM1(double rpm) {
        applyTargets(rpm, targetRPM2);
    }

    /** Define apenas o alvo do motor hardware-map "Shooter", em RPM. */
    public void setTargetRPM2(double rpm) {
        applyTargets(targetRPM1, rpm);
    }

    /** Define os dois alvos individuais, em RPM: Shooter1 e depois Shooter. */
    public void setTargetRPMs(double shooter1RPM, double shooter2RPM) {
        applyTargets(shooter1RPM, shooter2RPM);
    }

    /**
     * Compatibilidade com comandos antigos: entrada em ticks/s e alvo equivalente
     * para ambos. Para velocidades diferentes, use setTargetRPM1/2 ou setTargetRPMs.
     */
    public void setTargetVelocity(double ticksPerSecond) {
        double safeVelocity = Math.max(0.0, ticksPerSecond);
        double target1Ticks = safeVelocity * SHOOTER1_SPEED_FACTOR;
        double target2Ticks = safeVelocity * SHOOTER2_SPEED_FACTOR;
        applyTargets(
                target1Ticks * 60.0 / SHOOTER1_TICKS_PER_REV,
                target2Ticks * 60.0 / SHOOTER2_TICKS_PER_REV
        );
        targetVelocity = (target1Ticks + target2Ticks) / 2.0;
    }

    /** Compatibilidade/opção: alvos individuais em ticks/s. */
    public void setTargetVelocities(double shooter1TicksPerSecond,
                                    double shooter2TicksPerSecond) {
        double ticks1 = Math.max(0.0, shooter1TicksPerSecond);
        double ticks2 = Math.max(0.0, shooter2TicksPerSecond);
        applyTargets(
                ticks1 * 60.0 / SHOOTER1_TICKS_PER_REV,
                ticks2 * 60.0 / SHOOTER2_TICKS_PER_REV
        );
        targetVelocity = (ticks1 + ticks2) / 2.0;
    }

    private void applyTargets(double rpm1, double rpm2) {
        double safe1 = Math.max(0.0, rpm1);
        double safe2 = Math.max(0.0, rpm2);
        double now = nowSeconds();

        boolean changed1 = targetChanged(targetRPM1, safe1);
        boolean changed2 = targetChanged(targetRPM2, safe2);

        targetRPM1 = safe1;
        targetRPM2 = safe2;
        targetVelocity = (
                targetRPM1 * SHOOTER1_TICKS_PER_REV / 60.0
                        + targetRPM2 * SHOOTER2_TICKS_PER_REV / 60.0
        ) / 2.0;

        // Não zera potência/integral para mudanças pequenas ou reenvio do alvo
        // a cada ciclo. Em mudança real, apenas reinicia a derivada/timing.
        if (changed1) {
            pid1.rebase(now, currentRPM1);
        }
        if (changed2) {
            pid2.rebase(now, currentRPM2);
        }
        if (changed1 || changed2) {
            inWindow = false;
        }
    }

    private boolean targetChanged(double oldTarget, double newTarget) {
        double threshold = Math.max(
                TARGET_CHANGE_ABSOLUTE_RPM,
                Math.abs(oldTarget) * TARGET_CHANGE_PERCENT
        );
        return Math.abs(newTarget - oldTarget) > threshold;
    }

    // =====================================================
    // GANHOS PID INDIVIDUAIS
    // =====================================================

    public void setPID(double kP, double kI, double kD) {
        setPID1(kP, kI, kD);
        setPID2(kP, kI, kD);
    }

    public void setPID1(double kP, double kI, double kD) {
        kP1 = Math.max(0.0, kP);
        kI1 = Math.max(0.0, kI);
        kD1 = Math.max(0.0, kD);
        pid1.integral = 0.0;
        pid1.filteredDerivative = 0.0;
    }

    public void setPID2(double kP, double kI, double kD) {
        kP2 = Math.max(0.0, kP);
        kI2 = Math.max(0.0, kI);
        kD2 = Math.max(0.0, kD);
        pid2.integral = 0.0;
        pid2.filteredDerivative = 0.0;
    }

    // =====================================================
    // BATTERY / HELPERS
    // =====================================================

    private void readBattery() {
        voltageTimer.reset();
        if (voltageSensor != null) {
            double measured = voltageSensor.getVoltage();
            if (measured > 1.0) {
                batteryVoltage = measured;
            }
        }
    }

    private double nowSeconds() {
        return System.nanoTime() / 1e9;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    // =====================================================
    // LEITURAS / TELEMETRIA
    // =====================================================

    /** Velocidade absoluta do Shooter1 em ticks/s. */
    public double getVelocity1() {
        return Math.abs(shooter1.getVelocity());
    }

    /** Velocidade absoluta do Shooter em ticks/s. */
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
        return lastError;
    }

    public double getErrorRPM() {
        return lastErrorRPM;
    }

    /** RPM médio, apenas para compatibilidade/telemetria. */
    public double getCurrentRPM() {
        return currentRPM;
    }

    /** RPM alvo médio, apenas para compatibilidade/telemetria. */
    public double getTargetRPM() {
        return targetRPM;
    }

    public double getCurrentRPM1() { return currentRPM1; }
    public double getCurrentRPM2() { return currentRPM2; }
    public double getTargetRPM1() { return targetRPM1; }
    public double getTargetRPM2() { return targetRPM2; }
    public double getErrorRPM1() { return pid1.errorRPM; }
    public double getErrorRPM2() { return pid2.errorRPM; }

    /** Saída média, para compatibilidade com telemetria antiga. */
    public double getPIDPower() { return lastPower; }
    public double getPIDPower1() { return pid1.lastPower; }
    public double getPIDPower2() { return pid2.lastPower; }
    public double getPower1() { return shooter1.getPower(); }
    public double getPower2() { return shooter2.getPower(); }
    public double getBatteryVoltage() { return batteryVoltage; }

    public double getIntegralOutput() {
        return (kI1 * pid1.integral + kI2 * pid2.integral) / 2.0;
    }

    public double getIntegralOutput1() { return kI1 * pid1.integral; }
    public double getIntegralOutput2() { return kI2 * pid2.integral; }

    public boolean isAtTargetVelocity() {
        return enabled
                && inWindow
                && readyTimer.milliseconds() >= READY_HOLD_MS;
    }
}
