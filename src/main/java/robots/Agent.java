package robots;

import java.util.logging.Logger;
import comp329robosim.RobotMonitor;
import comp329robosim.SimulatedRobot;
import simulation.Env;
import simulation.Mode;

/**
 *
 */
public abstract class Agent extends RobotMonitor {
	static final Direction LEFT = Direction.LEFT;
	static final Direction UP = Direction.UP;
	static final Direction RIGHT = Direction.RIGHT;
	static final Direction DOWN = Direction.DOWN;

	static int moveCount = RobotController.STEP_COUNT;

	final Env env;

	Logger logger;

	Action exeAction;

	final RobotController controller;

	final boolean mode;

	int gx;
	int gy;

	Agent(final SimulatedRobot r, final int d, final Env env, final RobotController controller) {
		super(r, d);

		monitorRobotStatus(false);

		setTravelSpeed(100);

		gx = getX();

		gy = getY();

		this.env = env;

		this.controller = controller;

		// this.grid = env.getGrid();

		mode = env.getMode() == Mode.EVAL;
	}

	// @Override
	// public synchronized int getX() {
	// return super.getX() - (Env.CELL_RADIUS);
	// }

	// @Override
	// public synchronized int getY() {
	// return super.getY() - (Env.CELL_RADIUS);
	// }

	/**
	 * Get whether the robot can move into the x and y position
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	abstract boolean canMove(int x, int y);

	/**
	 * Set the cell at the x and y position with the value of the robot
	 *
	 * @param x
	 * @param y
	 */
	// abstract void updateGrid(final int x, final int y);

	// /**
	// * get x position on the grid from the robots location
	// *
	// * @return
	// */
	// final int getGridPosX() {
	// return (int) ((((double) getX() / Env.CELL_WIDTH) * 2) - 1) / 2;
	// }

	// /**
	// * get y position on the grid from the robots location
	// *
	// * @return
	// */
	// final int getGridPosY() {
	// return (int) ((((double) getY() / Env.CELL_WIDTH) * 2) - 1) / 2;
	// }

	// final int getDistanceAhead() {
	// return (int) ((((double) getUSenseRange() / Env.CELL_WIDTH) * 2) - 1) / 2;
	// }

	public abstract Action getAction(final Boolean[] state);

	public abstract Boolean[] getObservation();

	public Action getAction() {
		return exeAction;
	}

	/**
	 * Normalise value between -1 and 1
	 *
	 * @param x
	 * @param min
	 * @param max
	 * @return x Normalised between -1 and 1
	 */
	static final float normalise(final int x, final int min, final int max) {
		return (2 * ((float) (x - min) / (max - min))) - 1;
	}

	static synchronized void incrementMoves() {
		moveCount--;
	}

	static void resetMoves() {
		moveCount = RobotController.STEP_COUNT;
	}

	final void moveDirection(final Direction direction) {
		// final int x = getGridPosX();
		// final int y = getGridPosY();
		final int x = getX();
		final int y = getY();

		if (canMove(direction.px(x), direction.py(y))) {
			// env.updateGridEmpty(Env.ENV_SIZE / x, Env.ENV_SIZE / y);
			// updateGrid(Env.ENV_SIZE / direction.px(x), Env.ENV_SIZE / direction.py(y));

			gx = direction.px(x);
			gy = direction.py(y);

			if (env.getMode() == Mode.EVAL) {
				travel(Env.CELL_WIDTH);
			} else {
				setPose(direction.px(getX()), direction.py(getY()), getHeading());
			}
		}
	}

	/**
	 * Perform set in the environment based on chosen action
	 *
	 * @param action
	 */
	void doAction(final Action action, final boolean isPrey) {
		switch (action) {
			case FORWARD:
				moveDirection(Direction.fromDegree(getHeading()));
				break;

			case LEFT:
				if (mode) {
					rotate(-90);
				} else {
					setPose(getX(), getY(), getHeading() - 90);
				}
				break;

			case RIGHT:
				if (mode) {
					rotate(90);
				} else {
					setPose(getX(), getY(), getHeading() + 90);
				}
				break;

			case NOTHING:
				break;

			default:
				break;
		}


		// switch (action) {
		// case UP:
		// moveDirection(Direction.UP);
		// break;
		// case DOWN:
		// moveDirection(Direction.DOWN);
		// break;
		// case LEFT:
		// moveDirection(Direction.LEFT);
		// break;
		// case RIGHT:
		// moveDirection(Direction.RIGHT);
		// break;
		// case NOTHING:
		// break;

		// default:
		// break;
		// }
	}

}