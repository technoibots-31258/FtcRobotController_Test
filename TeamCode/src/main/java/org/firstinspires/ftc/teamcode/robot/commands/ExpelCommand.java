//package org.firstinspires.ftc.teamcode.robot.commands;
//
//import com.arcrobotics.ftclib.command.CommandBase;
//
//import org.firstinspires.ftc.teamcode.robot.subsystems.BufferSubsystem;
//import org.firstinspires.ftc.teamcode.robot.subsystems.IntakeSubsystem;
//import org.firstinspires.ftc.teamcode.robot.subsystems.ShooterSubsystem;
//
//public class ExpelCommand extends CommandBase {
//
//    private final IntakeSubsystem intakeSubsystem;
//    private final BufferSubsystem bufferSubsystem;
//    private final ShooterSubsystem shooterSubsystem;
//
//    public ExpelCommand(
//            IntakeSubsystem intakeSubsystem,
//            BufferSubsystem bufferSubsystem,
//            ShooterSubsystem shooterSubsystem
//    ) {
//        this.intakeSubsystem = intakeSubsystem;
//        this.bufferSubsystem = bufferSubsystem;
//        this.shooterSubsystem = shooterSubsystem;
//
//        addRequirements(
//                intakeSubsystem,
//                bufferSubsystem,
//                shooterSubsystem
//        );
//    }
//
//    @Override
//    public void initialize() {
//
//        intakeSubsystem.intake(-1.0);
//        bufferSubsystem.buffer(-1.0);
//
//        shooterSubsystem.reverse();
//    }
//
//    @Override
//    public void execute() {
//
//        intakeSubsystem.intake(-1.0);
//        bufferSubsystem.buffer(-1.0);
//
//        shooterSubsystem.reverse();
//    }
//
//    @Override
//    public void end(boolean interrupted) {
//
//        intakeSubsystem.stop();
//        bufferSubsystem.stop();
//        shooterSubsystem.stopShooter();
//    }
//
//    @Override
//    public boolean isFinished() {
//        return false;
//    }
//}