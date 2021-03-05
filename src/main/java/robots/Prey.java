package robots;

import java.util.logging.Logger;

import comp329robosim.OccupancyType;
import comp329robosim.SimulatedRobot;

import simulation.Mode;
import simulation.SimulationEnv;

/**
 * @author rob
 *
 */
final class Prey extends RobotRunner {
	private Action randomMove;

	public Prey(final SimulatedRobot r, final int d, final SimulationEnv env, final RobotController controller) {
		super(r, d, env, controller);

		logger = Logger.getLogger(Prey.class.getName());

		// randomMove = getRandomDirection(ALL);
		randomMove = Action.getRandomAction();

		setPositionNew(getGridPosX(), getGridPosY());

	}

	@Override
	boolean canMove(final int x, final int y) {
		// return grid[y][x].getCellType() != OccupancyType.OBSTACLE &&
		// grid[y][x].getCellType() != OccupancyType.HUNTER;
		return grid[y][x].getCellType() == OccupancyType.GOAL || grid[y][x].getCellType() == OccupancyType.EMPTY;
	}

	@Override
	public void run() {
		while (!exit) {
			final int x = getGridPosX();
			final int y = getGridPosY();
			// final int a = getHeading();

			final boolean right = canMove(x + 1, y);
			final boolean down = canMove(x, y + 1);
			final boolean left = canMove(x - 1, y);
			final boolean up = canMove(x, y - 1);

			// check if surrounded by the hunters
			if (!right && !left && !up && !down) {

				// Do nothing while in goal state
				// logger.info("trapped");

				controller.handleCapture();

				break;
			}

			randomMove = Action.getRandomAction();

			doAction(randomMove);
		}
		// logger.info("Prey Stopped");
	}

	private void doAction(final Action direction) {
		switch (direction) {
			case TRAVEL:
				travel();
				break;

			case LEFT_TURN:
				if (env.getMode() == Mode.EVAL) {
					rotate(-90);
				} else {
					setPose(getX(), getY(), getHeading() + -90);
				}
				break;

			case RIGHT_TURN:
				if (env.getMode() == Mode.EVAL) {
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

	}

	@Override
	final void left(final int x, final int y) {
		if (canMove(x - 1, y)) {
			controller.resumeHunters();

			setPositionOld(x, y);
			setPositionNew(x - 1, y);

			if (env.getMode() == Mode.EVAL) {
				travel(CELL_DISTANCE);
			} else {
				setPose(getX() - CELL_DISTANCE, getY(), getHeading());
			}

			randomMove = Action.getRandomAction();
		}
	}

	@Override
	final void up(final int x, final int y) {
		if (canMove(x, y - 1)) {
			controller.resumeHunters();

			setPositionOld(x, y);
			setPositionNew(x, y - 1);

			if (env.getMode() == Mode.EVAL) {
				travel(CELL_DISTANCE);
			} else {
				setPose(getX(), getY() - CELL_DISTANCE, getHeading());
			}

			randomMove = Action.getRandomAction();
		}
	}

	@Override
	final void right(final int x, final int y) {
		if (canMove(x + 1, y)) {
			controller.resumeHunters();

			setPositionOld(x, y);
			setPositionNew(x + 1, y);

			if (env.getMode() == Mode.EVAL) {
				travel(CELL_DISTANCE);
			} else {
				setPose(getX() + CELL_DISTANCE, getY(), getHeading());
			}

			randomMove = Action.getRandomAction();
		}
	}

	@Override
	final void down(final int x, final int y) {
		if (canMove(x, y + 1)) {
			controller.resumeHunters();

			setPositionOld(x, y);
			setPositionNew(x, y + 1);

			if (env.getMode() == Mode.EVAL) {
				travel(CELL_DISTANCE);
			} else {
				setPose(getX(), getY() + CELL_DISTANCE, getHeading());
			}

			randomMove = Action.getRandomAction();
		}
	}

	private void setPositionNew(final int x, final int y) {
		// set position
		env.updateGridPrey(x, y);

		if (grid[y][x + 1].getCellType() != OccupancyType.OBSTACLE
				&& grid[y][x + 1].getCellType() != OccupancyType.HUNTER) {
			env.updateGridGoal(x + 1, y);
		}
		if (grid[y][x - 1].getCellType() != OccupancyType.OBSTACLE
				&& grid[y][x - 1].getCellType() != OccupancyType.HUNTER) {
			env.updateGridGoal(x - 1, y);
		}
		if (grid[y + 1][x].getCellType() != OccupancyType.OBSTACLE
				&& grid[y + 1][x].getCellType() != OccupancyType.HUNTER) {
			env.updateGridGoal(x, y + 1);
		}
		if (grid[y - 1][x].getCellType() != OccupancyType.OBSTACLE
				&& grid[y - 1][x].getCellType() != OccupancyType.HUNTER) {
			env.updateGridGoal(x, y - 1);
		}
	}

	private void setPositionOld(final int x, final int y) {
		// set previous position
		env.updateGridEmpty(x, y);

		if (grid[y][x + 1].getCellType() != OccupancyType.OBSTACLE
				&& grid[y][x + 1].getCellType() != OccupancyType.HUNTER) {
			env.updateGridEmpty(x + 1, y);
		}
		if (grid[y][x - 1].getCellType() != OccupancyType.OBSTACLE
				&& grid[y][x - 1].getCellType() != OccupancyType.HUNTER) {
			env.updateGridEmpty(x - 1, y);
		}
		if (grid[y + 1][x].getCellType() != OccupancyType.OBSTACLE
				&& grid[y + 1][x].getCellType() != OccupancyType.HUNTER) {
			env.updateGridEmpty(x, y + 1);
		}
		if (grid[y - 1][x].getCellType() != OccupancyType.OBSTACLE
				&& grid[y - 1][x].getCellType() != OccupancyType.HUNTER) {
			env.updateGridEmpty(x, y - 1);
		}
	}

}
