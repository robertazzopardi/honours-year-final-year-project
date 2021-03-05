package robots;

import java.io.IOException;
import java.nio.file.Files;

import comp329robosim.MyGridCell;
import comp329robosim.OccupancyType;
import comp329robosim.SimulatedRobot;
import intelligence.DeepQLearning;
import simulation.Mode;
import simulation.SimulationEnv;

public class RobotController {
	// public static final int STATE_COUNT = 8;
	public static final int STATE_COUNT = 10;

	private static final int DELAY = 1000;

	private final Hunter[] hunters = new Hunter[4];

	private Prey prey;

	private final SimulationEnv env;

	public RobotController(final SimulationEnv env) {
		this.env = env;

		initRobots();

		startRobots();
	}

	private void initRobots() {
		// initialise the robots from the environment
		final SimulatedRobot preyRobot = env.getAndSetPrey();
		prey = new Prey(preyRobot, DELAY, env, this);

		final Mode mode = env.getMode();

		for (int i = 0; i < hunters.length; i++) {
			do {
				final SimulatedRobot simulatedRobot = env.getAndSetHunter(i);
				DeepQLearning learning = null;
				// if (mode == Mode.EVAL) {
				// learning = DeepQLearning.loadNetwork(i);
				// } else {
				// learning = new DeepQLearning();
				// }
				switch (mode) {
					case EVAL:
						learning = DeepQLearning.loadNetwork(env.getFiles()[i]);
						break;

					case TRAIN_ON:
						if (env.getFiles().length < 4) {
							learning = new DeepQLearning();

						} else {
							learning = DeepQLearning.loadNetwork(env.getFiles()[i]);

						}
						break;

					case TRAIN:
						learning = new DeepQLearning();
						break;

					default:
						break;
				}
				hunters[i] = new Hunter(simulatedRobot, DELAY, env, learning, this, prey);
			} while (isSamePosition(i));
		}

		for (final Hunter hunter : hunters) {
			hunter.setOthers(hunters);
		}
	}

	private boolean isSamePosition(final int i) {
		if (hunters[i].getGridPosX() == prey.getGridPosX() && hunters[i].getGridPosY() == prey.getGridPosY()) {
			return true;
		}

		for (int j = 0; j < i; j++) {
			if (hunters[i].getGridPosX() == hunters[j].getGridPosX()
					&& hunters[i].getGridPosY() == hunters[j].getGridPosY()) {
				return true;
			}
		}

		return false;
	}

	public void restartRobots() {
		final SimulatedRobot preyRobot = env.getAndSetPrey();
		prey = new Prey(preyRobot, DELAY, env, this);

		for (int i = 0; i < 4; i++) {
			do {
				final SimulatedRobot simulatedHunter = env.getAndSetHunter(i);
				hunters[i] = new Hunter(simulatedHunter, DELAY, env, hunters[i].getLearning(), this, prey);
			} while (isSamePosition(i));
		}

		for (final Hunter hunter : hunters) {
			hunter.setOthers(hunters);
		}

		startRobots();
	}

	public void resumeHunters() {
		for (final Hunter hunter : hunters) {
			hunter.resumeRobot();
		}
	}

	private void startRobots() {
		prey.start();
		for (final Hunter hunter : hunters) {
			hunter.start();
		}
	}

	public void stopRobots() {
		prey.stopRobot();
		for (final Hunter hunter : hunters) {
			hunter.stopRobot();
		}
	}

	// public void saveNetwork() {
	// // pick the network with the highest score
	// final Optional<Hunter> maxHunter = Arrays.stream(hunters)
	// .max(Comparator.comparing(v -> v.getLearning().getNetwork().score()));

	// if (maxHunter.isPresent()) {
	// final DeepQLearning dqn = maxHunter.get().getLearning();
	// // System.out.println(Double.toString(dqn.getNetwork().score()));
	// // for (Hunter hunter : hunters) {
	// // if (!hunter.equals(h)) {
	// // hunter.setLearning(new DeepQLearning(dqn));
	// // }
	// // }
	// if (env.getMode() == Mode.TRAIN) {
	// // DeepQLearning.saveNetwork(hunters[i].getLearning().getNetwork(), i,
	// // Integer.toString(SimulationEnv.EPISODES));
	// DeepQLearning.saveNetwork(dqn.getNetwork(), 0,
	// Integer.toString(SimulationEnv.EPISODES));
	// } else if (env.getMode() == Mode.TRAIN_ON) {
	// if (env.getFiles().length != 0) {
	// // final String fileName = env.getFiles()[i].getName();
	// try {
	// Files.delete(env.getFiles()[0].toPath());
	// } catch (IOException e) {
	// e.printStackTrace();
	// }

	// // DeepQLearning.saveNetwork(hunters[i].getLearning().getNetwork(), i,
	// // Integer.toString(env.getEpisode() - 1));
	// DeepQLearning.saveNetwork(dqn.getNetwork(), 0,
	// Integer.toString(env.getEpisode() - 1));

	// } else {
	// // DeepQLearning.saveNetwork(hunters[i].getLearning().getNetwork(), i,
	// // Integer.toString(SimulationEnv.EPISODES));
	// DeepQLearning.saveNetwork(dqn.getNetwork(), 0,
	// Integer.toString(SimulationEnv.EPISODES));
	// }
	// }
	// }
	// }

	public void saveNetworks() {
		for (int i = 0; i < hunters.length; i++) {
			if (env.getMode() == Mode.TRAIN) {
				DeepQLearning.saveNetwork(hunters[i].getLearning().getNetwork(), i,
						Integer.toString(SimulationEnv.EPISODES));
			} else if (env.getMode() == Mode.TRAIN_ON) {
				if (env.getFiles().length != 0) {
					// final String fileName = env.getFiles()[i].getName();
					try {
						Files.delete(env.getFiles()[i].toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}

					DeepQLearning.saveNetwork(hunters[i].getLearning().getNetwork(), i,
							Integer.toString(env.getEpisode() - 1));

				} else {
					DeepQLearning.saveNetwork(hunters[i].getLearning().getNetwork(), i,
							Integer.toString(SimulationEnv.EPISODES));
				}
			}
		}
	}

	public void handleCapture() {
		stopRobots();

		// GridPrinter.printGrid(env.getGrid());

		// Done with current episode, now we can restart the simulation
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		env.resetGrid();

		if (env.getEpisode() <= SimulationEnv.EPISODES + env.getTrainedEpisodes()) {
			env.updateTitle(env.incrementEpisode());
			restartRobots();
		} else if (env.getMode() == Mode.TRAIN || env.getMode() == Mode.TRAIN_ON) {
			// saveNetwork();
			saveNetworks();
			env.stopRunning();
			System.exit(0);
		}
	}

	public void addCaptureScore(final float[] currState, final Action direction, final Hunter hunter) {
		final MyGridCell[][] grid = env.getGrid();

		final int pX = prey.getGridPosX();
		final int pY = prey.getGridPosY();

		int score = 0;
		if (grid[pY + 1][pX].getCellType() == OccupancyType.HUNTER) {
			score += 0.25;
		}
		if (grid[pY - 1][pX].getCellType() == OccupancyType.HUNTER) {
			score += 0.25;
		}
		if (grid[pY][pX + 1].getCellType() == OccupancyType.HUNTER) {
			score += 0.25;
		}
		if (grid[pY][pX - 1].getCellType() == OccupancyType.HUNTER) {
			score += 0.25;
		}

		if (grid[pY + 1][pX].getCellType() == OccupancyType.OBSTACLE) {
			score -= 0.25;
		}
		if (grid[pY - 1][pX].getCellType() == OccupancyType.OBSTACLE) {
			score -= 0.25;
		}
		if (grid[pY][pX + 1].getCellType() == OccupancyType.OBSTACLE) {
			score -= 0.25;
		}
		if (grid[pY][pX - 1].getCellType() == OccupancyType.OBSTACLE) {
			score -= 0.25;
		}

		hunter.getLearning().update(currState, direction, score, hunter.getStates());

		// hunter.getLearning().update(currState, direction, 2, hunter.getStates());
	}
}
