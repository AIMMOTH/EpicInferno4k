
package EpicInferno;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Vector;
import javax.swing.JApplet;
import javax.swing.JPanel;

public class A extends JApplet implements KeyListener, Runnable {

	// Applet stuff
	private Thread thread;
	private boolean running = false;
	private int currentFps = 0;
	
	public void init() {
		// Set up components.
		addKeyListener(this);
		
		// Set up game
		makeNewLandscape();
		setupDefaultGame();
		
		// Start thread!
		thread = new Thread(this);
		thread.start();
	}
	
	public void keyTyped(KeyEvent ke) {
	}
	
	public void run() {
		// Frame timing.
		long currentFrameTime = 0l;
		double totalFrameTime = 0.0;
		final double FRAME_TIME = 1000.0 / 20.0; // 20fps
		
		// Frame calculator.
		long currentSecond = System.currentTimeMillis() / 1000;
		long newSecond = 0l;
		int currentSecondFps = 0;
		
		try {
			running = true;
			while (running) {
				// Timing.
				currentFrameTime = System.currentTimeMillis();
				tick();
				totalFrameTime = System.currentTimeMillis() - currentFrameTime;
				if (totalFrameTime < FRAME_TIME) {
					thread.sleep((int) (FRAME_TIME - totalFrameTime));
				}
				
				// Frame count.
				newSecond = System.currentTimeMillis() / 1000;
				if (newSecond > currentSecond) {
					currentSecond = newSecond;
					currentFps = currentSecondFps;
					currentSecondFps = 0;
				} else {
					currentSecondFps++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			running = false;
			destroy();
		}
	}
	
	public void destroy() {
		running = false;
	}
	
	// Game stuff
	private boolean p1Winner;
	private boolean p2Winner;
	private boolean gameRunning;
	private int rows;
	private int cols;
	private int squareWidth, squareHeight;
	//private Square[] squares; // Containing the landscape
	// Index of square types:
	// 0 Beach
	// 1 Cannon
	// 2 Dirt
	// 3 Fire
	// 4 Forest
	// 5 Grass
	// 6 Rubble
	// 7 Tower
	// 8 Wall
	// 9 Water
	private int[] squares;
	// Fire animation tick
	private int[] fireAnimationTick;
	// Fire expire tick
	private int[] fireExpireTick;
	//private Vector<Cannon> p1Cannons = new Vector<Cannon>();
	//private Vector<Cannon> p2Cannons = new Vector<Cannon>();
	// Index of data (index, name, type):
	// 0 Name, String
	// 1 Position, Integer
	// 2 ready?, Boolean
	private Vector<Object[]> p1Cannons = new Vector<Object[]>(10, 2);
	private Vector<Object[]> p2Cannons = new Vector<Object[]>(10, 2);
	boolean isCannonReady(Object[] cannon) { return ((Boolean) cannon[2]).booleanValue(); }
	// Index of data (index, name, type):
	// 0 Name of parent cannon, String
	// 1-2 position x and y, Double
	// 3-4 delta x and y, Double
	// 5-6 step x and y, Double
	// 7-8 target x and y, Double
	// 9-10 square x and y, Integer (never used?)
	private Vector<Object[]> p1CannonBalls = new Vector<Object[]>(10, 2);
	private Vector<Object[]> p2CannonBalls = new Vector<Object[]>(10, 2);
	double getCannonBallPositionX(Object[] cannonBall) { return ((Double) cannonBall[1]).doubleValue(); }
	double getCannonBallPositionY(Object[] cannonBall) { return ((Double) cannonBall[2]).doubleValue(); }
	double getCannonBallDeltaX(Object[] cannonBall) { return ((Double) cannonBall[3]).doubleValue(); }
	double getCannonBallDeltaY(Object[] cannonBall) { return ((Double) cannonBall[4]).doubleValue(); }
	double getCannonBallStepX(Object[] cannonBall) { return ((Double) cannonBall[5]).doubleValue(); }
	double getCannonBallStepY(Object[] cannonBall) { return ((Double) cannonBall[6]).doubleValue(); }
	double getCannonBallTargetX(Object[] cannonBall) { return ((Double) cannonBall[7]).doubleValue(); }
	double getCannonBallTargetY(Object[] cannonBall) { return ((Double) cannonBall[8]).doubleValue(); }
	int getCannonBallSquareX(Object[] cannonBall) { return ((Integer) cannonBall[9]).intValue(); }
	int getCannonBallSquareY(Object[] cannonBall) { return ((Integer) cannonBall[10]).intValue(); }
	private int firingCannonIndex1; // Determines which cannon fires
	private int firingCannonIndex2; // Determines which cannon fires
	//private Vector<Tower> p1Towers = new Vector<Tower>();
	//private Vector<Tower> p2Towers = new Vector<Tower>();
	private Vector<Integer> p1Towers = new Vector<Integer>(3, 1);
	private Vector<Integer> p2Towers = new Vector<Integer>(3, 1);
	// Keep "time"
	private int tickNumber;
	// Fonts
	private Font fontWinner = new Font("Monospaced", Font.BOLD, 40);
	private Font fontWinnerWait = new Font("Monospaced", Font.BOLD, 14);
	private Font fontWinnerReplay = new Font("Monospaced", Font.BOLD, 20);
	private Font fontWelcomeHeadline = new Font("Monospaced", Font.BOLD, 40);
	private Font fontWelcomeText = new Font("Monospaced", Font.BOLD, 20);
	private Font fontWelcomeTextSmall = new Font("Monospaced", Font.BOLD, 12);
	//Player
	public static final int STATE_BUILD = 1, STATE_CANNON = 2, STATE_FIRE = 3;
	// Index of data:
	// 0 right player?
	// 1-6 left, right, up, down, switch state, use
	// 7-8 focus x, focus y
	// 9-14 left?, right?, up?, down?, switch state?, use?
	// 15 which state?
	private int[] player1 = new int[] {0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_R, KeyEvent.VK_T, 10, 10, 0, 0, 0, 0, 0, 0, 2};
	private int[] player2 = new int[] {1, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_O, KeyEvent.VK_P, 10, 10, 0, 0, 0, 0, 0, 0, 2};
	int getPlayerFocusX(int[] player) { return player[7]; }
	int getPlayerFocusY(int[] player) { return player[8]; }
	int getPlayerState(int[] player) { return player[15]; }
	
	void setupDefaultGame()
	{
		// Set this first frame.
		squareWidth = 0;
		squareHeight = 0;
		// Start with zero
		tickNumber = 0;
		firingCannonIndex1 = 0;
		firingCannonIndex2 = 0;
		p1Winner = false;
		p2Winner = false;
		gameRunning = false;
		// Image
		bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	}
	
	void makeNewLandscape()
	{
		int size = 20;
		rows = size * 3;
		cols = size * 4;
		//squares = new Square[rows*cols];
		squares = new int[rows * cols];
		fireExpireTick = new int[rows * cols];
		fireAnimationTick = new int[rows * cols];
	
		int previousRow = 0;
	
		int forestFromBeach = 5;
		double forestRandom = 2.0;
	
		// Center the water
		int colWater = cols/2-1;
		int waterWidth = 1;
		int waterWidthMin = 2;
		int waterWidthMax = 6;
		for (int i=0; i<rows*cols; i++) {
			int currentCol = i % cols;
			int currentRow = i / cols;
			// Place towers in first place
			if (currentCol == (cols / 4) && currentRow == (rows / 4))
			{
				//Tower t = new Tower(currentCol, currentRow);
				p1Towers.add(new Integer(i));
				squares[i] = 7;
			}
			else if (currentCol == (cols / 4) && currentRow == (rows / 2))
			{
				//Tower t = new Tower(currentCol, currentRow);
				p1Towers.add(new Integer(i));
				//p1.setFocus(currentCol, currentRow);
				player1[7] = currentCol;
				player2[8] = currentRow;
				squares[i] = 7;
			}
			else if (currentCol == (cols / 4) && currentRow == (3*(rows / 4)))
			{
				//Tower t = new Tower(currentCol, currentRow);
				//p1Towers.push_back(t);
				p1Towers.add(new Integer(i));
				squares[i] = 7;
			}
			// Player 2
			else if (currentCol == (3*(cols/4)) && currentRow == (rows / 4))
			{
				//Tower t = new Tower(currentCol, currentRow);
				//p2Towers.push_back(t);
				p2Towers.add(new Integer(i));
				squares[i] = 7;
			}
			else if (currentCol == (3*(cols/4)) && currentRow == (rows / 2))
			{
				//Tower t = new Tower(currentCol, currentRow);
				//p2Towers.push_back(t);
				p2Towers.add(new Integer(i));
				//p2.setFocus(currentCol, currentRow);
				player2[7] = currentCol;
				player2[8] = currentRow;
				squares[i] = 7;
			}
			else if (currentCol == (3*(cols/4)) && currentRow == (3*(rows / 4)))
			{
				//Tower t = new Tower(currentCol, currentRow);
				//p2Towers.push_back(t);
				p2Towers.add(new Integer(i));
				squares[i] = 7;
			}
			else if (currentCol == colWater -1 || currentCol == colWater + waterWidth + 1)
			{
				squares[i] = 0;
			}
			else if(currentCol >= colWater && currentCol <= colWater + waterWidth)
			{
				squares[i] = 9;
			}
			/*
			 * Add forest if square is a distance from water. There is a
			 * greater chance if there is a forest one row up then if the
			 * square is alone.
			 */
			else if (
				(currentCol < colWater - forestFromBeach ||
				currentCol > colWater + waterWidth + forestFromBeach) &&
				
				(
				(i - cols >= 0 && // Is there one row above?
				 squares[i - cols] == 4 && // Is there forest one row up?
				(int) (Math.random() * forestRandom + 0.5) == (int) forestRandom) || // random chance
				
				(i - cols - 1>= 0 &&
				squares[i - cols - 1] == 4 &&
				(int) (Math.random() * forestRandom + 0.5) == (int) forestRandom) ||
				
				(i - cols + 1>= 0 &&
				squares[i - cols + 1] == 4 &&
				(int) (Math.random() * forestRandom + 0.5) == (int) forestRandom) ||
				
				(int) (Math.random()*15.0 + 0.5) == 15 // Or 1 of 15's chance
				)
				)
			{
				squares[i] = 4;
				// Add forest to the square before if it is not a Tower (make forest bigger)
				if (i>0 && !(squares[i - 1] == 7)) // Tower
				{
					//delete squares[i-1];
					squares[i-1] = 4;
				}
			}
			else 
				squares[i] = 5;
		

			if (currentRow != previousRow)
			{
				previousRow = currentRow;
				waterWidth = waterWidth - 1 + (int) ((Math.random() * 2.0) + 0.5);// + GameEngine::random(0, 2);
				if (waterWidth < waterWidthMin) waterWidth = 2;
				if (waterWidth > waterWidthMax) waterWidth = 6;
				int temp = (int) ((Math.random() * 2.0) + 0.5);
				// -1, 0 or +1 in position
				colWater = colWater + temp - 1;
			}

		}
	}
	
	private BufferedImage bufferedImage;
	public void paint(Graphics g2)
	{
		Graphics g = bufferedImage.getGraphics();
		if (squareWidth == 0 || squareHeight == 0)
		{
			squareHeight = (int) (getHeight() / (rows * 1.0));
			squareWidth = (int) (getWidth() / (cols * 1.0));
			
		}
	
		for (int i=0; gameRunning && i<rows; i++)
		{
			for (int j=0; j<cols; j++)
			{
				//SDL_Rect r = {j*squareWidth, i*squareHeight, squareWidth, squareHeight};
				Rectangle r = new Rectangle(j*squareWidth, i*squareHeight, squareWidth, squareHeight);
				//squares[i*cols + j]->draw2(surface, &r);
				//squares[i*cols + j].draw2(g, r);
				int currentPosition = i*cols + j;
				Color color = new Color(200, 200, 0);
				switch (squares[currentPosition]) {
					case(0): // Beach
						g.setColor(new Color(200, 200, 0));
						break;
					case(1): // Cannon
						g.setColor(new Color(100, 100, 100));
						break;
					case(2): // Dirt
						g.setColor(new Color(90, 70, 40));
						break;
					case(3): // Fire
						if (fireAnimationTick[currentPosition] <= 3)
							g.setColor(new Color(200, 200, 0));
						else if (fireAnimationTick[currentPosition]  <= 10)
							g.setColor(new Color(200, 0, 0));
						else if (fireAnimationTick[currentPosition]  <= 15)
							g.setColor(new Color(150, 0, 0));
						if (++fireAnimationTick[currentPosition] >= 15)
							fireAnimationTick[currentPosition] = 0;
						break;
					case(4): // Forest
						g.setColor(new Color(0, 100, 0));
						break;
					case(5): // Grass
						g.setColor(new Color(50, 200, 50));
						break;
					case(6): // Rubble
						g.setColor(new Color(50, 50, 50));
						break;
					case(7): // Tower
						g.setColor(new Color(20, 20, 20));
						break;
					case(8): // Wall
						g.setColor(new Color(150, 150, 150));
						break;
					case(9): // Water
						g.setColor(new Color(50, 80, 200));
						break;
				}
				g.fillRect(r.x, r.y, r.width, r.height);
				if (i == player1[8] && j == player1[7]) { // 7 = focus x, 8 = focus y
					color = new Color(250, 0, 0);
					if (player1[15] == STATE_BUILD)
						color = new Color(0, 250, 0);
					else if (player1[15] == STATE_CANNON)
						color = new Color(0, 0, 250);
					//p1->draw2(surface, &r);
					//p1.draw2(g, r);
					g.setColor(color);
					g.drawRect(r.x, r.y, r.width, r.height);
					g.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
				}
				else if (i == player2[8] && j == player2[7]) {
					//p2->draw2(surface, &r);
					//p2.draw2(g, r);
					color = new Color(250, 0, 0);
					if (player2[15] == STATE_BUILD)
						color = new Color(0, 250, 0);
					else if (player2[15] == STATE_CANNON)
						color = new Color(0, 0, 250);
					g.setColor(color);
					g.drawRect(r.x, r.y, r.width, r.height);
					g.drawRect(r.x + 1, r.y + 1, r.width - 4, r.height - 4);
				}
			}
		}
	
		// Cannonballs
		
		drawCannonBalls(g, p1CannonBalls);
		drawCannonBalls(g, p2CannonBalls);
	
		if (p1Winner)
		{
			g.setColor(Color.white);
			g.fillRect(30, 15, 200, 70);
			g.setColor(Color.black);
			g.setFont(fontWinner);
			g.drawString("WINNER!", 50, 50);
		}
		if (p2Winner)
		{
			g.setColor(Color.white);
			g.fillRect(430, 15, 200, 70);
			g.setColor(Color.black);
			g.setFont(fontWinner);
			g.drawString("WINNER!", 450, 50);
		}
		if (p1Winner || p2Winner) {
			g.setColor(Color.white);
			g.fillRect(100, 200, 600, 70);
			g.setColor(Color.black);
			g.setFont(fontWinnerWait);
			g.drawString("Wait for fires and cannonballs to see if there is a draw.", 120, 220);
			g.setFont(fontWinnerReplay);
			g.drawString("Reload the page to play again!!", 190, 240);
		}
		if (!gameRunning)
		{
			g.setColor(Color.black);
			g.fillRect(0, 0, 800, 600);
			
			g.setColor(Color.red);
			g.setFont(fontWelcomeHeadline);
			g.drawString("E P I C    I N F E R N O!!", 80, 50);
			g.setFont(fontWelcomeTextSmall);
			g.drawString("Player one starts on the left side of the river, player two on the right. You each have three towers (black", 20, 100);
			g.drawString("squares) which must survive or all is lost. You each control a pointer which have three states: build", 20, 120);
			g.drawString("cannons (blue pointer), fire a cannon (red pointer) or build protective wall(green pointer). The cannons are", 20, 140);
			g.drawString("yellowish and the walls are grey.", 20, 160);

			g.drawString("You will be playing in a fully randomized, fully destructable and fully naturalistic enviroment. In the heat", 20, 200);
			g.drawString("of the battle the landscape can be rippled with devestating forest fires and exploding cannons and towers.", 20, 220);

			g.drawString("Player ONE steers the pointer with WSAD, switches state with R and have the use-button(build/fire cannon)", 20, 260);
			g.drawString("assigned to T.", 20, 280);
			g.drawString("Player TWO uses the arrow keys to steer, P for use, and O to switch state.", 20, 300);

			g.setFont(fontWelcomeText);
			g.drawString("CHALLANGE A FRIEND AND", 260, 340);
			g.drawString("PRESS ANY KEY WHEN READY FOR INFERNO", 170, 360);
			
			g.setFont(fontWelcomeTextSmall);
			g.drawString("©2010 Carl Emmoth, Karl Restorp", 280, 400);
		}
		
		g.setColor(Color.red);
		g.drawString(debugString, 20, 20);
		
		g2.drawImage(bufferedImage, 0, 0, this);
	}
	
	private String debugString = new String();
	
	void drawCannonBalls(Graphics g, Vector<Object[]> balls)
	{
		for (int i=0; i < balls.size(); i++) {
			Object[] os = balls.elementAt(i);
			g.setColor(new Color(100, 100, 100));
			g.fillRect((int) ((Double) os[1]).doubleValue(), (int) ((Double) os[2]).doubleValue(), 5, 5);
		}
	}
	
	public void tick()
	{
		if (gameRunning) {
			if (p1Towers.size() == 0)
				p2Winner = true;
			if(p2Towers.size() == 0)
				p1Winner = true;
		
			tickNumber++;
			// Handle players
			if (!p1Winner && !p2Winner)
			{
				updatePlayerAction(player1, firingCannonIndex1, p1Cannons, p1CannonBalls, p1Towers);
				updatePlayerAction(player2, firingCannonIndex2, p2Cannons, p2CannonBalls, p2Towers);
			}
			// Move cannonballs
			updateCannonBalls(player1, p1CannonBalls, p1Cannons);
			updateCannonBalls(player2, p2CannonBalls, p2Cannons);
			// Update fires
			updateFires();
		}
		// REPAINT!!! FGS
		repaint();
	}
	
	void updateFires()
	{
		int totalSquares = rows*cols;
		for (int i=0; i<totalSquares; i++)
		{
			if (squares[i] == 3) // Fire
			{
				//Fire tf = (Fire) (squares[i]);
				int tx = i % cols;
				int ty = i / cols;
				// Extinguish?
				if (fireExpireTick[i] <= tickNumber)
				{
					//delete squares[i];
					squares[i] = 2;
				}
				// Spread fire by 1/80 chance
				else if ((int) (Math.random() * 80.0 + 0.5) == 80)
				{
					int[] adjecent = getRandomAdjecent(i);
					int count = 0;
					while (count < 8 && !spreadFire(i, adjecent[count]))
					{
						count++;
					}
				}
			}
		}
	}

	// Dirt, fire, forest, grass
	boolean isSquareBuildable(int square) {
		return square == 2 || square == 3 || square == 4 || square == 5;
	}

	// Cannon, tower, wall	
	boolean isSquareDestroyable(int square) {
		return square == 1 || square == 7 || square == 8;
	}
	
	// Cannon, tower
	boolean isSquareExplodable(int square) {
		return square == 1 || square == 7;
	}
	
	// Cannon, forest, tower.
	boolean isSquareFlamable(int square) {
		return square == 1 || square == 4 || square == 7;
	}
	
	boolean spreadFire(int from, int pos)
	{
		if (!checkAdjecentOnField(from, pos))
			return false;
	
		if (isSquareFlamable(squares[pos]))
		{
			if (squares[pos] == 1) { // Cannon
				// Update cannon vector
				removeCannon(pos, p1Cannons);
				removeCannon(pos, p2Cannons);
				explosion(pos); // test
			}
			
			if (squares[pos] == 7) { // Tower
				// Update tower vector
				removeTower(pos, p1Towers);
				removeTower(pos, p2Towers);
				explosion(pos); // test
			}
	
			//delete squares[pos];
			createFireOnSquare(pos);
			return true;
		}
		else
			return false;
	}

	void createFireOnSquare(int pos) {
		squares[pos] = 3; // Fire
		fireExpireTick[pos] = tickNumber + 100 + (int) (Math.random() * 100.0 + 0.5);
		fireAnimationTick[pos] = 0;
	}

	void removeCannon(int pos, Vector<Object[]> cannons)
	{
		for (int i=0; i<cannons.size(); i++) {
			Object[] o = cannons.elementAt(i);
			if (pos == ((Integer) o[1]).intValue())
			{
				cannons.removeElementAt(i);
				return;
			}
		}
	}

	void removeTower(int pos, Vector<Integer> towers)
	{
		for (int i=0; i<towers.size(); i++)
			if (pos == towers.elementAt(i).intValue())
			{
				towers.removeElementAt(i);
				return;
			}
	}
	
	void updateCannonBalls(
							int[] player,
							Vector<Object[]> balls,
							Vector<Object[]> cannons
										)
	{
		for (int i=0; i<balls.size(); i++)
		{
			// Move
			//balls.elementAt(i).tick();
			Object[] os = balls.elementAt(i);
			os[1] = new Double(getCannonBallPositionX(os) + getCannonBallStepX(os));
			os[2] = new Double(getCannonBallPositionY(os) + getCannonBallStepY(os));
			// Check explosion after move
			
			// Calculate square position
			int tx = (int) ( getCannonBallPositionX(os) / squareWidth);
			int ty = (int) ( getCannonBallPositionY(os) / squareHeight);
			// Check if ball is on opponent side
			boolean opponentSide =  (player[0] == 0 && tx > (cols / 2)) ||
								(player[0] == 1 && tx < (cols / 2));
								
			int currentPosition = ty*cols + tx;
			int currentSquare = squares[currentPosition];
	
			// Target square x and y
			int targetX = getCannonBallSquareX(os);
			int targetY = getCannonBallSquareY(os);
	
			if ((tx == targetX && ty == targetY) || // if on target
				(opponentSide && isSquareDestroyable(currentSquare)))// or current square is opponent wall, cannon or tower
			{
				// Alter squares
				
				//int index = cols*ty + tx;
				//Square* target = squares[index];
				
				//Remove cannon, start fire to adjecent squares
				if (currentSquare == 1) // Cannon
				{
					removeCannon(currentPosition, p1Cannons);
					removeCannon(currentPosition, p2Cannons);
					explosion(currentPosition);
	
				}
				//Remove tower, start fire to adjecent squares
				else if (currentSquare == 7) // Tower
				{
					removeTower(currentPosition, p1Towers);
					removeTower(currentPosition, p2Towers);
					explosion(currentPosition);
				}
				// Remove wall and add rubble
				else if (currentSquare == 8) // Wall
				{
					//delete squares[index];
					squares[currentPosition] = 6; // Rubble
				}
				// Replace forest2 with fire
				else if (currentSquare == 4) // Forest
				{
					//delete squares[index];
					createFireOnSquare(currentPosition);
				}
				// Replace grass with dirt
				else if (currentSquare == 5) // Grass
				{
					//delete squares[index];
					squares[currentPosition] = 2; // Dirt
				}
				// Remove ball
				//delete balls->at(i);
				setCannonReady(os[0].toString(), cannons); // Name
				balls.removeElementAt(i);				
			}
		}
	}
	
	void setCannonReady(String name, Vector<Object[]> cannons) {
		for (int i=0; i<cannons.size(); i++) {
			Object[] os = cannons.elementAt(i);
			if (os[0].toString().equals(name)) {
				os[2] = new Boolean(true);
				return;
			}
		}
	}
	
	void explosion(int from)
	{
		// Adjecent square indexes
		int[] adjecent = getAdjecent(from);
		int fromX = from % cols;
		int fromY = from / cols;
	
		// Remove the destroyable and explodable from lists
		if (squares[from] == 1) { // Cannon
			removeCannon(from, p1Cannons);
			removeCannon(from, p2Cannons);
		}
	
		if (squares[from] == 7) { // Tower
			removeTower(from, p1Towers);
			removeTower(from, p2Towers);
		}
	
		//delete squares[from];
		createFireOnSquare(from);
	
		for (int i=0; i<8; i++)
		{
			int pos = adjecent[i];
			int tx = pos % cols;
			int ty = pos / cols;
			if (checkAdjecentOnField(from, pos))
			{
				// Cumulative explosion
				if (isSquareExplodable(squares[pos]))
					explosion(pos);
				//delete squares[pos];
				createFireOnSquare(pos);
			}
		}
	}
	
	// A bit complicated
	boolean checkAdjecentOnField(int from, int pos)
	{
		int fromX = from % cols;
		return !(pos < 0 ||
				 pos >= (cols*rows) ||
				(fromX == cols - 1 && pos != from + 1) ||
				(fromX == cols - 1 && pos != from - cols + 1) ||
				(fromX == cols - 1 && pos != from + cols + 1) ||
				(fromX == 0 && pos != from - 1) ||
				(fromX == 0 && pos != from - cols - 1) ||
				(fromX == 0 && pos != from + cols - 1));
	}
	
	void updatePlayerAction(
							int[] player,
							int firingCannonIndex,
							Vector<Object[]> cannons,
							Vector<Object[]> balls,
							Vector<Integer> towers
										)
	{
		int tx = player[7];
		int ty = player[8];
		// Movement
		if (player[9] == 1 && tx > 0) // left
			player[7] = tx - 1;
			
		if (player[10] == 1 && tx < cols) // right
			player[7] = tx + 1;
			
		if (player[11] == 1 && ty > 0) // up
			player[8] = ty - 1;
			
		if (player[12] == 1 && ty < rows) // down
			player[8] = ty + 1;
	
		// The new focused square
		tx = player[7];
		ty = player[8];
		int index = ty*cols + tx;
		int focusedSquare = squares[index];
	
		// Use
		if (player[14] == 1)
		{
			if (getPlayerState(player) == STATE_BUILD)
			{
				if ((player[0] == 0 && tx < (cols / 2) ||
					player[0] == 1 && tx > (cols / 2)) &&
					isSquareBuildable(focusedSquare))
				{
					//delete squares[index];
					squares[index] = 8; // Wall
				}
			}
			else if (getPlayerState(player) == STATE_CANNON && cannons.size() < 10)
			{
				if ((player[0] == 0 && tx < (cols / 2) ||
					player[0] == 1 && tx > (cols / 2)) &&
					isSquareBuildable(focusedSquare))
				{
					//delete squares[index];
					//Cannon tc = new Cannon(tx, ty);
					squares[index] = 1;
					//cannons.push_back(tc);
					cannons.add(new Object[] {String.valueOf(System.currentTimeMillis()), new Integer(index), new Boolean(true)});
				}
			}
			/*
			 * Fire cannons in circle and one cannon at a time.
			 */
			else if (getPlayerState(player) == STATE_FIRE && cannons.size() > 0)
			{
				// Check index
				if (firingCannonIndex >= cannons.size())
					firingCannonIndex = 0;
	
				boolean cannonFound = false;
				int count = 0;
				// Find ready cannon. temp will prevent infinite loop.
				while (
					!cannonFound &&
					count < cannons.size()
					)
				{
					// Found?
					Object[] cannon = cannons.elementAt(firingCannonIndex);
					if (isCannonReady(cannon))
						cannonFound = true;
					else
						firingCannonIndex++;
					// Check index
					if (firingCannonIndex == cannons.size())
						firingCannonIndex = 0;
					count++;
				}
				// Fire?
				Object[] tc = cannons.elementAt(firingCannonIndex);
				int cannonX = ((Integer) tc[1]).intValue() % cols;
				int cannonY = ((Integer) tc[1]).intValue() / cols;
				if (cannonFound && !(cannonX == tx && cannonY == ty)) //fire on self?
				//if (cannonFound && !((tc.getSquareX() == tx) && (tc.getSquareY() == ty)))//fire on self?
				{
					firingCannonIndex++;
					tc[2] = new Boolean(false); // ready?
					// Calculate pixel x and y and make cannonball
	
					double startX = cannonX * squareWidth + (squareWidth / 2.0);
					double startY = cannonY * squareHeight + (squareHeight / 2.0);
	
					// Calculate pixel target x and y
					double targetX = getPlayerFocusX(player) * squareWidth + (squareWidth / 2.0);
					double targetY = getPlayerFocusY(player) * squareHeight + (squareHeight / 2.0);
					// Randomize around target square (-1 + random(2) in x and y).
					targetX = targetX - squareWidth + squareWidth * (int) (Math.random() * 2.0 + 0.5);
					targetY = targetY - squareHeight + squareHeight * (int) (Math.random() * 2.0 + 0.5);
					// Calculate target square x and y
					int squareX = (int) (targetX / (squareWidth * 1.0));
					int squareY = (int) (targetY / (squareHeight * 1.0));
	
					/*
		posX = startPixelX;
		posY = startPixelY;
		targetX = targetPixelX;
		targetY = targetPixelY;
		squareX = sX;
		squareY = sY;
		cannon = parent;
		
		double speed = 5;
		dY = targetY - posY;
		dX = targetX - posX;
	
		double hyp = Math.sqrt(dX*dX + dY*dY);
		double nofSteps = hyp / speed;
		stepX = dX / nofSteps;
		stepY = dY / nofSteps;
		*/
		
					double deltaX = targetX - startX;
					double deltaY = targetY - startY;
					double hyp = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
					double nofSteps = hyp / 5.0;
					
					Object[] cb = new Object[] {
						tc[0].toString(), // Cannon name
						new Double(startX), new Double(startY),
						new Double(deltaX), new Double(deltaY),
						new Double(deltaX / nofSteps), new Double(deltaY / nofSteps), // Step
						new Double(targetX), new Double(targetY),
						(int) squareX, (int) squareY
						};
					//balls->push_back(cb);
					balls.add(cb);
				}
			}
			player[14] = 0; // Reset use.
		}
	
		if (player[13] == 1) // Switch
		{
			if (getPlayerState(player) == STATE_BUILD)
				player[15] = STATE_CANNON;
	
			else if (getPlayerState(player) == STATE_CANNON)
				player[15] = STATE_FIRE;
	
			else if (getPlayerState(player) == STATE_FIRE)
				player[15] = STATE_BUILD;
				
			player[13] = 0; // Reset switch
		}
	}
	
	public void keyPressed(KeyEvent key)
	{
		//p1.keyDown(key.getKeyCode());
		//p2.keyDown(key.getKeyCode());
		
		// 0 right player?, 1 left, 2 right, 3 up, 4 down, 5 switch state, 6 use,
		// 7 focus x, 8 focus y, 9 left?, 10 right?, 11 up?, 12 down?, 13 switch state?, 14 use?
		setKey(key.getKeyCode(), 1);
	}
	
	public void keyReleased(KeyEvent key)
	{
		if (!gameRunning)
		{
			gameRunning = true;
		}
		//p1.keyUp(key.getKeyCode());
		//p2.keyUp(key.getKeyCode());
		setKey(key.getKeyCode(), 0);
	}
	
	void setKey(int keyCode, int value) {
		int invertedValue = (value == 0)? 1: 0;
		for (int i=0; i<6; i++) {
			if (keyCode == player1[i + 1]) {
				player1[i + 9] = (i >= 4) ? invertedValue : value; // Switch state and use inverts key press and release
				return;
			}
		}
		for (int i=0; i<6; i++) {
			if (keyCode == player2[i + 1]) {
				player2[i + 9] = (i >= 4) ? invertedValue : value;
				return;
			}
		}
	}
	
	int[] getAdjecent(int i)
	{
		int[] adjecent = new int[8];
		adjecent[0] = i - cols - 1;
		adjecent[1] = i - cols;
		adjecent[2] = i - cols + 1;
		adjecent[3] = i - 1;
		adjecent[4] = i + 1;
		adjecent[5] = i + cols - 1;
		adjecent[6] = i + cols;
		adjecent[7] = i + cols + 1;
	
		return adjecent;
	}
	
	int[] getRandomAdjecent(int index)
	{
		int[] adjecent = getAdjecent(index);
		//int temp = 0;
		int p1 = 0;
		int p2 = 0;
		for (int i=0; i<20; i++)
		{
			p1 = (int) (Math.random() * 7.0 + 0.5);
			p2 = (int) (Math.random() * 7.0 + 0.5);
			adjecent[p1] ^= adjecent[p2];
			adjecent[p2] ^= adjecent[p1];
			adjecent[p1] ^= adjecent[p2];
		}
	
		return adjecent;
	}
} // A
