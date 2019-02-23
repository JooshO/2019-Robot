package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

<<<<<<< HEAD
/* TODO: 
Add method for setting elevator State using encoder position once we get proper
values from the encoders. 
*/

=======
>>>>>>> 2bf86b95005c5f2f1a4ed4ab88f796a999c8ed44
/**
 * This class defines the mechanism that moves up and down for hatch covers.
 */
public class Elevator {
	
	/**
	 * Enum used to contain values related to the position of the elevator.
	 */
	public enum State {
		/*
		From left to right: Change in height from position zero, modification to our max speed when
		in that state, the max speed we angle the placer when in that state, and the name of the
		state in String form.
		*/
		LEVEL_ZERO(0.0, 1.0, 1.0 , "Initial State"),
		//	CARGO_L1(16.75, 0.80, 0.50 , "Cargo level 1"),
		//	CARGO_L2(44.75, 0.60, 0.30 , "Cargo Level 2"),
		//	CARGO_L3(72.75, 0.40, 0.20 , "Cargo Level 3"),
		HATCH_L1(7.87, 0.90, 0.50 , "Hatch Level 1"),
		HATCH_L2(40.65, 0.65, 0.30 , "Hatch Level 2"),
		HATCH_L3(66.125, 0.45, 0.20 , "Hatch Level 3");

		private double deltaInches;
		private double maxSpeedPercent;
		private double maxAngleRate;
		private String stateName;

		/**
		 * 
		 * 
		 * @param deltaInches      Change in height from position zero in inches.
		 * @param maxSpeedModifier Percent of our max speed we use when the elevator is
		 *                         in this state.
		 * @param stateName A string representation of the state we are in.
		 * @param maxAngleRate     How fast our angle motor can move when the elevator
		 *                         is in this state.
		 */
		State(double deltaInches, double maxSpeedModifier, double maxAngleRate , String stateName) {
			this.deltaInches = deltaInches;
			this.maxSpeedPercent = maxSpeedModifier;
			this.maxAngleRate = maxAngleRate;
			this.stateName = stateName;
		}

		/**
		 * @return The change in height from initial to current state.
		 */
		public double getDeltaHeight() {
			return this.deltaInches;
		}

		/**
		 * @return The max speed modifier based on the elevator's current state.
		 */
		public double getMaxSpeedModifier() {
			return this.maxSpeedPercent;
		}

		/**
		 * @return The max speed we turn our angle arm at the elevator's current state.
		 */
		public double getMaxAngleRate() {
			return this.maxAngleRate;
		}

		/**
		 * @return The name of the state we are currently in.
		 */
		public String getStateName(){
			return this.stateName;
		}
	}

	// Declaring the encoder for the elevator height.
	SensorCollection m_elevatorEncoder;

	// Declaring the speed controller for the elevator.
	WPI_TalonSRX m_elevatorMotor;

	// Declaring the elevator state enum.
	State currentState;

	// Tracking Variables for Motion Magic
	boolean m_firstCall;
	double m_lockedDistance;
	double m_targetAngle;
	int m_smoothing;

	// This constructor is initializing in creating a new instance of an elevator
	// with limit port switch definitions.

	public Elevator() {

		// Instantiates Motor controller for elevator
		m_elevatorMotor = new WPI_TalonSRX(RobotMap.ELEVATOR_MOTOR_PORT);

		// Instantiating encoder for the elevator height
		m_elevatorEncoder = new SensorCollection(m_elevatorMotor);

		// Zeroes the encoder
		m_elevatorEncoder.setQuadraturePosition(0, 0);

		// Sets the State enum to it's initial state
		currentState = State.LEVEL_ZERO;

		m_firstCall = false;
		m_lockedDistance = 0;
		m_targetAngle = 0;
	}

	public void elevatorPIDConfig() {
		// Stops motor controllers
		m_elevatorMotor.set(ControlMode.PercentOutput, 0);

		// Set neutral mode
		m_elevatorMotor.setNeutralMode(NeutralMode.Brake);

		// Configures sensor as quadrature encoder
		m_elevatorMotor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, RobotMap.PID_PRIMARY, RobotMap.TIMEOUT_MS);

		// Config sensor and motor direction
		m_elevatorMotor.setInverted(true);
		m_elevatorMotor.setSensorPhase(true);

		// Set status frame period for data collection
		m_elevatorMotor.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 5, RobotMap.TIMEOUT_MS);

		// Config neutral deadband
		m_elevatorMotor.configNeutralDeadband(RobotMap.NEUTRAL_DEADBAND, RobotMap.TIMEOUT_MS);

		// Config peak output
		m_elevatorMotor.configPeakOutputForward(+RobotMap.PID_PEAK_OUTPUT, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.configPeakOutputReverse(-RobotMap.PID_PEAK_OUTPUT, RobotMap.TIMEOUT_MS);

		// Motion Magic Config
		m_elevatorMotor.configMotionAcceleration(2000, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.configMotionCruiseVelocity(2000, RobotMap.TIMEOUT_MS);

		// PID Config
		m_elevatorMotor.config_kP(0, RobotMap.GAINS.kP, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.config_kI(0, RobotMap.GAINS.kI, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.config_kD(0, RobotMap.GAINS.kD, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.config_kF(0, RobotMap.GAINS.kF, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.config_IntegralZone(0, RobotMap.GAINS.kIzone, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.configClosedLoopPeakOutput(0, RobotMap.GAINS.kPeakOutput, RobotMap.TIMEOUT_MS);
		m_elevatorMotor.configAllowableClosedloopError(0, 0, RobotMap.TIMEOUT_MS);

		// PID closed loop config
		m_elevatorMotor.configClosedLoopPeriod(0, 5, RobotMap.TIMEOUT_MS);

		// Sets profile slot for PID
		m_elevatorMotor.selectProfileSlot(0, RobotMap.PID_PRIMARY);
	}

	public void elevatorPIDDrive(State state) {
		double target = (state.deltaInches) * (RobotMap.TICKS_PER_REVOLUTION / RobotMap.DRUM_CIRCUMFERENCE);
		System.out.println("PIDTarget in tics: \t" + target);
		System.out.println("Current Position in tics: \t" + m_elevatorMotor.getSelectedSensorPosition());
		System.out.println("State: \t" + state);
		m_elevatorMotor.set(ControlMode.MotionMagic, target);
	}

	/**
	 * Returns the current position of the elevator reported by the encoder
	 * 
	 * @return The position of the elevator encoder
	 */
	public int getElevatorEncoderPosition() {
		return m_elevatorEncoder.getQuadraturePosition();
	}

	/**
	 * Returns the current velocity of the elevator reported by the elevator encoder
	 * 
	 * @return The velocity of the elevator encoder
	 */
	public int getElevatorEncoderVelocity() {
		return m_elevatorEncoder.getQuadratureVelocity();
	}

	/**
	 * Method used to manually move the elevator.
	 * @param input Joystick/variable input.
	 */
	public void moveRaw(double input){
			m_elevatorMotor.set(ControlMode.PercentOutput, (input /* 0.4*/));
	}
	
	/**
	 * Calculates and returns the height of the elevator in inches.
	 * 
	 * @return The elevator's current height
	 */
	public double getPosition(){
<<<<<<< HEAD
		double position = 0.0;
		double numRevolutions = (m_elevatorEncoder.getQuadraturePosition() / RobotMap.TICKS_PER_REVOLUTION);
		position = RobotMap.DRUM_CIRCUMFERENCE * numRevolutions;
		currentState = getState(position);
		return position;
<<<<<<< HEAD
	} 

	public void HighElevatorPosition(boolean button){
		if(calcPosition()<RobotMap.MAX_ELEVATOR_HEIGHT){		//Double check that MAX_ELEVATOR_HEIGHT is the right thing to use
			while(calcPosition()<RobotMap.MAX_ELEVATOR_HEIGHT){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_UP);
			}
			//set speed to 0.0
		}
		else {
			while(calcPosition()>RobotMap.MAX_ELEVATOR_HEIGHT){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_DOWN);
			}
		}
	}
	
	public void MidElevatorPosition(boolean button){
		if(calcPosition()<RobotMap.MID_ELEVATOR_HEIGHT){
			while(calcPosition()<RobotMap.MID_ELEVATOR_HEIGHT){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_UP);
			}
		}
		else{
			while(calcPosition()>RobotMap.MID_ELEVATOR_HEIGHT){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_DOWN);

			}
		}
	}

	public void LowElevatorPosition(boolean button){
		if(calcPosition()<RobotMap.LOW_ELEVATOR_HEIGHT){
			while(calcPosition()<RobotMap.LOW_ELEVATOR_HEIGHT){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_UP)

			}
		}
		else{
			while(calcPosition()>RobotMap.LOW_ELEVATOR_HEIGHT){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_DOWN);
			}
		}
	}
	

=======
=======
		double positionInches = (m_elevatorEncoder.getQuadraturePosition() * (RobotMap.DRUM_CIRCUMFERENCE / RobotMap.TICKS_PER_REVOLUTION));
		//position = RobotMap.DRUM_CIRCUMFERENCE * numRevolutions;
		currentState = getState(positionInches);
		return positionInches;
>>>>>>> 2bf86b95005c5f2f1a4ed4ab88f796a999c8ed44
	}

	/**
	 * Method used to move the elevator to our desired height while a button of our choice
	 * is pressed.
	 * 
	 * @param button The button we want associated with moving to the desired state.
	 * @param desiredState The state we want the button to move the elevator to.
	 */
	public void moveToPosition(boolean button , State desiredState){
		if(button && currentState != desiredState){
			if(currentState.deltaInches > desiredState.deltaInches){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_DOWN);
			}
			else if(currentState.deltaInches < desiredState.deltaInches){
				m_elevatorMotor.set(RobotMap.ELEVATOR_MOTOR_SPEED_UP);
			}
		}
		else if(currentState == desiredState){
			m_elevatorMotor.set(0.0);
		}
	}

	/**
	 * Method to set currentState based on the current height of the elevator.
	 * 
	 * @param position The position gathered by the getPosition method.
	 * @return The state of the elevator based on it's height.
	 */
	private State getState(double position){
		// if(position < State.CARGO_L1.deltaInches + 0.7 && position > State.CARGO_L1.deltaInches - 0.7){
		// 	return State.CARGO_L1;
		// }
		// else if(position < State.CARGO_L2.deltaInches + 0.7 && position > State.CARGO_L2.deltaInches - 0.7){
		// 	return State.CARGO_L2;
		// }
		// else if(position < State.CARGO_L3.deltaInches + 0.7 && position > State.CARGO_L3.deltaInches - 0.7){
		// 	return State.CARGO_L3;
		// }
		if(position < State.HATCH_L1.deltaInches + 0.7 && position > State.HATCH_L1.deltaInches - 0.7){
			return State.HATCH_L1;
		}
		else if(position < State.HATCH_L2.deltaInches + 0.7 && position > State.HATCH_L2.deltaInches - 0.7){
			return State.HATCH_L2;
		}
		else if(position < State.HATCH_L3.deltaInches + 1.5 && position > State.HATCH_L3.deltaInches - 1.5){
			return State.HATCH_L3;
		}
		else if(position < State.HATCH_L1.deltaInches - 0.7) {
			return State.LEVEL_ZERO;
		}
		else{
			return currentState;
		}
	}
>>>>>>> 1dcf4c991d7e78244cdbf1b95120cfbd211926cd

}