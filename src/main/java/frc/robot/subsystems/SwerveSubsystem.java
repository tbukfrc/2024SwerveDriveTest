package frc.robot.subsystems;
import java.io.File;
import java.io.IOException;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.PIDConstants;
import com.pathplanner.lib.util.ReplanningConfig;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;
import swervelib.SwerveDrive;
import edu.wpi.first.math.util.Units;

public class SwerveSubsystem extends SubsystemBase {
    SwerveDrive swerveDrive;
    private boolean fieldRelative = true;
    public void init() {
        File swerveJsonDirectory = new File(Filesystem.getDeployDirectory(),"swerve");
        try {
            swerveDrive = new SwerveParser(swerveJsonDirectory).createSwerveDrive(Units.feetToMeters(1));
            swerveDrive.zeroGyro();
            AutoBuilder.configureHolonomic(
                this::getPose, // Robot pose supplier
                this::resetPose, // Method to reset odometry (will be called if your auto has a starting pose)
                this::getChassisSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
                this::driveRelative, // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds
                new HolonomicPathFollowerConfig( // HolonomicPathFollowerConfig, this should likely live in your Constants class
                        new PIDConstants(5.0, 0.0, 0.0), // Translation PID constants
                        new PIDConstants(5.0, 0.0, 0.0), // Rotation PID constants
                        4.5, // Max module speed, in m/s
                        0.4, // Drive base radius in meters. Distance from robot center to furthest module.
                        new ReplanningConfig() // Default path replanning config. See the API for the options here
                ),
                () -> {
                    // Boolean supplier that controls when the path will be mirrored for the red alliance
                    // This will flip the path being followed to the red side of the field.
                    // THE ORIGIN WILL REMAIN ON THE BLUE SIDE

                    var alliance = DriverStation.getAlliance();
                    if (alliance.isPresent()) {
                        return alliance.get() == DriverStation.Alliance.Red;
                    }
                    return false;
                },
                this // Reference to this subsystem to set requirements
            );
        } catch (IOException err) {
            err.printStackTrace();
        }
        
    }

    public void resetHeading() {
        swerveDrive.zeroGyro();
    }

    public Command toggleOrientedMode() {
        return this.runOnce(() -> {fieldRelative = !fieldRelative; SmartDashboard.putBoolean("relativityMode", fieldRelative);});
    }

    public void Drive(Translation2d translation, double rotation) {
        swerveDrive.drive(translation, rotation, fieldRelative, false);
    }
    public void updateOdometry() {
        swerveDrive.updateOdometry();
    }
    public Pose2d getPose() {
        return swerveDrive.getPose();
    }
    public void resetPose(Pose2d pose) {
        swerveDrive.resetOdometry(pose);
    }
    public ChassisSpeeds getChassisSpeeds() {
        return swerveDrive.getRobotVelocity();
    }
    public void driveRelative(ChassisSpeeds speeds) {
        swerveDrive.drive(speeds);
    }
}