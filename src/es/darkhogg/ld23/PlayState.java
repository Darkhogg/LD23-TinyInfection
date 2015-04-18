package es.darkhogg.ld23;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.Point;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public final class PlayState extends BasicGameState {

    private static final Integer ZERO = 0;

    private Input input;
    private final Random random = new Random();

    private final static int NUM_CELLS = 112;
    private final static int RAD_CELLS = 1280;

    private final static int SEND_DIST_MIN = 256;
    private final static int SEND_DIST_MAX = 512;

    /* package */final Collection<Cell> cells = new ArrayList<Cell>();
    private final Collection<Virus> viruses = new HashSet<Virus>();

    private float camX;
    private float camY;
    private float camZoom = 1f;

    private static final float ZOOM_MIN = 0.3f;
    private static final float ZOOM_MAX = 1.3f;
    private static final float ZOOM_STEP = 0.05f;

    private Cell cellOver;
    private Cell cellSelect;

    boolean paused = false;

    private static final float W = LudumDare23Game.WIDTH;
    private static final float H = LudumDare23Game.HEIGHT;
    private static final float W2 = W / 2;
    private static final float H2 = H / 2;

    /* package */Player humanPlayer = Player.BLUE;
    /* package */Map<Player,ArtificialIntelligence> ais = new EnumMap<Player,ArtificialIntelligence>(Player.class);
    /* package */Map<Player,Integer> numCells = new EnumMap<Player,Integer>(Player.class);

    private CellGraph graph;

    private EnumSet<VirusType> vSet;

    private boolean gameOver = false;
    private boolean won = false;

    @Override
    public int getID () {
        return LudumDare23Game.ID_STATE_PLAY;
    }

    @Override
    public void init (GameContainer cont, StateBasedGame game) throws SlickException {
        random.setSeed(System.nanoTime());
        input = cont.getInput();
    }

    @Override
    public void enter (GameContainer container, StateBasedGame game) throws SlickException {
        cells.clear();

        Map<Player,Point> initialPos = new EnumMap<Player,Point>(Player.class);
        for (Player player : Player.values()) {
            double ang = ((double) player.ordinal() / Player.values().length) * Math.PI * 2;

            initialPos.put(
                player, new Point((int) (RAD_CELLS * 0.6 * Math.cos(ang)), (int) (RAD_CELLS * 0.6 * Math.sin(ang))));
        }

        Map<Player,Cell> initialCells = new EnumMap<Player,Cell>(Player.class);
        Map<Player,Double> initialCellDists = new EnumMap<Player,Double>(Player.class);

        // Generate the whole world (I.E. cells)
        for (int i = 0; i < NUM_CELLS; i++) {
            double ang = random.nextDouble() * Math.PI * 2;
            double rad = RAD_CELLS * Math.sqrt(random.nextDouble());

            int cap = Cell.MIN_CAPACITY + random.nextInt(Cell.MAX_CAPACITY - Cell.MIN_CAPACITY);
            Cell cell = new Cell(cap, rad * Math.cos(ang), rad * Math.sin(ang), VirusType.getRandomWithNull(random));

            double sang = random.nextDouble() * Math.PI * 2;
            double spd = 2 * random.nextDouble();

            cell.spdX = (float) (spd * Math.cos(sang)) * 5;
            cell.spdY = (float) (spd * Math.sin(sang)) * 5;
            cells.add(cell);

            for (Player player : Player.values()) {
                Point pt = initialPos.get(player);

                double dist = Math.hypot(pt.getX() - cell.posX, pt.getY() - cell.posY);

                if (!initialCellDists.containsKey(player) || dist < initialCellDists.get(player)) {
                    initialCells.put(player, cell);
                    initialCellDists.put(player, dist);
                }
            }
        }

        // Cell selection
        for (Player player : Player.values()) {
            Cell cell = initialCells.get(player);
            cell.life = 0;
            cell.owner = player;
            cell.capacity = (Cell.MIN_CAPACITY + Cell.MAX_CAPACITY) * 2 / 5;
            cell.preference = null;

            for (VirusType vt : VirusType.values()) {
                cell.viruses.put(vt, cell.capacity / 6);
            }

            if (player != humanPlayer) {
                ais.put(player, new ArtificialIntelligence(player, new DefaultAICellSelector()));
            }
        }

        graph = new CellGraph(cells);

        camX = initialCells.get(humanPlayer).posX;
        camY = initialCells.get(humanPlayer).posY;

        gameOver = false;

        input.addMouseListener(this);
    }

    @Override
    public void leave (GameContainer container, StateBasedGame game) throws SlickException {
        input.removeMouseListener(this);
    }

    @Override
    public void update (GameContainer cont, StateBasedGame game, int delta) throws SlickException {
        final float fdelta = delta / 1000f;

        boolean keyLt = false, keyRt = false, keyDn = false, keyUp = false, keyVirAtk = false, keyVirDef = false, keyVirGen =
            false, mouseLeftClick = false, mouseRightClick = false, keyPause = false;

        if (!gameOver) {
            keyPause = input.isKeyPressed(Input.KEY_ESCAPE);

            keyLt = input.isKeyDown(Input.KEY_LEFT) || input.isKeyDown(Input.KEY_A);
            keyRt = input.isKeyDown(Input.KEY_RIGHT) || input.isKeyDown(Input.KEY_D);
            keyDn = input.isKeyDown(Input.KEY_DOWN) || input.isKeyDown(Input.KEY_S);
            keyUp = input.isKeyDown(Input.KEY_UP) || input.isKeyDown(Input.KEY_W);

            keyVirAtk = input.isKeyDown(Input.KEY_1);
            keyVirDef = input.isKeyDown(Input.KEY_2);
            keyVirGen = input.isKeyDown(Input.KEY_3);

            mouseLeftClick = input.isMousePressed(Input.MOUSE_LEFT_BUTTON);
            mouseRightClick = input.isMousePressed(Input.MOUSE_RIGHT_BUTTON);
        }
        if (!paused) {
            boolean checkEndGame = false;

            int mouseX = input.getMouseX();
            int mouseY = input.getMouseY();

            float scnMouseX = (mouseX - W2) / camZoom + camX;
            float scnMouseY = (mouseY - H2) / camZoom + camY;

            cellOver = null;

            // Move camera
            final int CAM_STEP = 256;
            if (keyLt) {
                camX -= CAM_STEP * fdelta / camZoom;
            }
            if (keyRt) {
                camX += CAM_STEP * fdelta / camZoom;
            }
            if (keyUp) {
                camY -= CAM_STEP * fdelta / camZoom;
            }
            if (keyDn) {
                camY += CAM_STEP * fdelta / camZoom;
            }

            // Fix camera past scenario boundaries
            {
                final int MAX_DST = (int) (RAD_CELLS - Math.min(W2, H2) + 64);

                double ang = Math.atan2(camY, camX);
                double dst = Math.hypot(camY, camX);

                if (dst > MAX_DST) {
                    camX = (float) (MAX_DST * Math.cos(ang));
                    camY = (float) (MAX_DST * Math.sin(ang));
                }
            }

            // Update all viruses
            for (Virus virus : viruses) {
                virus.update(fdelta);
            }

            // Cell counter reset
            for (Player p : Player.values()) {
                numCells.put(p, ZERO);
            }

            // Update all cells
            for (Cell cell : cells) {

                float rad = cell.getRadius();

                // Confine them to the scenario
                double sqdst = cell.posY * cell.posY + cell.posX * cell.posX;
                if (sqdst > (RAD_CELLS - rad) * (RAD_CELLS - rad)) {
                    double ang = Math.atan2(cell.posY, cell.posX);

                    cell.spdX -= .5 * Math.cos(ang);
                    cell.spdY -= .5 * Math.sin(ang);
                }

                // Make them collide
                // Also: if two cells are CLOSE (less than a fraction of their send distance), they experience some
                // repulsion, to avoid cells sticking too much together. Note that this virtually avoids collisions,
                // but whatever...
                for (Cell otherCell : cells) {
                    if (cell != otherCell) {
                        float otherRad = otherCell.getRadius();

                        float difX = otherCell.posX - cell.posX;
                        float difY = otherCell.posY - cell.posY;

                        double difDst = Math.hypot(difY, difX);
                        double difAng = Math.atan2(difY, difX);

                        float sendDist = getSendDistance(cell);
                        if (difDst < sendDist * 0.7f) {
                            float incX = (float) ((16f / (difDst)) * Math.cos(difAng));
                            float incY = (float) ((16f / (difDst)) * Math.sin(difAng));

                            cell.spdX -= incX;
                            cell.spdY -= incY;
                        } else if (difDst > sendDist * 1.2f && difDst < sendDist * 2.2f) {
                            float incX = (float) ((4f / (difDst)) * Math.cos(difAng));
                            float incY = (float) ((4f / (difDst)) * Math.sin(difAng));

                            cell.spdX += incX;
                            cell.spdY += incY;
                        }

                        double collDst = difDst - rad - otherRad - 6;
                        if (collDst < 0) {
                            float incX = (float) ((rad * rad * 24f / (difDst * difDst)) * Math.cos(difAng));
                            float incY = (float) ((rad * rad * 24f / (difDst * difDst)) * Math.sin(difAng));

                            cell.spdX -= incX;
                            cell.spdY -= incY;

                            otherCell.spdX += incX;
                            otherCell.spdY += incY;
                        }
                    }
                }

                // Push viruses
                for (Iterator<Virus> it = viruses.iterator(); it.hasNext();) {
                    Virus virus = it.next();

                    float difX = virus.posX - cell.posX;
                    float difY = virus.posY - cell.posY;

                    double difDst = Math.hypot(difY, difX);

                    double collDst = difDst - rad - 8;

                    if (collDst < 0) {
                        if (virus.path != null
                            && virus.pathPos >= 0 && virus.pathPos < virus.path.size()
                            && virus.path.get(virus.pathPos) == cell)
                        {
                            virus.pathPos++;

                        }

                        double difAng = Math.atan2(difY, difX);

                        float incX = (float) ((rad * rad * 3f / (difDst * difDst)) * Math.cos(difAng));
                        float incY = (float) ((rad * rad * 3f / (difDst * difDst)) * Math.sin(difAng));

                        cell.spdX -= incX * .002f;
                        cell.spdY -= incY * .002f;

                        if (virus.target != cell) {
                            virus.spdX += incX;
                            virus.spdY += incY;
                        }
                    }

                    if (collDst < -12) {
                        if (virus.target == cell) {
                            Player oldOwner = cell.owner;

                            cell.inject(virus, random);
                            playSoundAt(Sounds.SND_INJECT, cell.posX, cell.posY);
                            if (cell.owner != null && cell.owner != virus.owner) {
                                playSoundAt(Sounds.SND_HIT, cell.posX, cell.posY);
                            }
                            it.remove();

                            if (oldOwner != cell.owner) {
                                playSoundAt(Sounds.SND_INFECT, cell.posX, cell.posY);
                                checkEndGame = true;
                            }
                        }
                    }

                }

                // Select the closest to the mouse
                float msDstX = (cell.posX - scnMouseX);
                float msDstY = (cell.posY - scnMouseY);

                double sqMouseDst = msDstX * msDstX + msDstY * msDstY;
                if (sqMouseDst < rad * rad) {
                    cellOver = cell;
                }

                // Update counters
                if (cell.owner != null) {
                    numCells.put(cell.owner, numCells.get(cell.owner) + 1);
                }

                cell.update(fdelta);
            }

            // Select a cell
            if (mouseLeftClick) {
                cellSelect = cellOver;
            }

            // Send viruses out
            if (mouseRightClick && cellSelect != null && cellOver != null) {

                if (cellSelect != cellOver && cellSelect.owner == humanPlayer) {

                    // Form the available virus set
                    vSet = EnumSet.allOf(VirusType.class);
                    if (keyVirAtk | keyVirDef | keyVirGen) {
                        vSet.clear();
                        if (keyVirAtk) {
                            vSet.add(VirusType.ATTACKER);
                        }
                        if (keyVirDef) {
                            vSet.add(VirusType.DEFENDER);
                        }
                        if (keyVirGen) {
                            vSet.add(VirusType.GENERATOR);
                        }
                    }

                    // Send the viruses
                    // GUYS! I'M REFACTORING CODE! AWESOME!
                    // Just made it a different method so the AI can call it, basically
                    sendViruses(cellSelect, cellOver, vSet);

                }
            }

            // Check end game
            if (checkEndGame && !gameOver) {
                if (numCells.get(humanPlayer) == 0) {
                    gameOver = true;
                    won = false;
                }
                if (numCells.get(Player.BLUE) + numCells.get(Player.RED) + numCells.get(Player.GREEN) == numCells
                    .get(humanPlayer))
                {
                    gameOver = true;
                    won = true;
                    ais.put(humanPlayer, new ArtificialIntelligence(humanPlayer, new DefaultAICellSelector()));
                    if (humanPlayer == Player.BLUE) {
                        humanPlayer = Player.RED;
                    } else {
                        humanPlayer = Player.BLUE;
                    }
                }
            }

            // Update the AI
            for (ArtificialIntelligence ai : ais.values()) {
                ai.update(this, delta, random);
            }

            if (gameOver && input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
                game.enterState(LudumDare23Game.ID_STATE_INIT);
            }
        }
        if (keyPause) {
            paused = !paused;
        }
    }

    /* package */void sendViruses (Cell from, Cell to, Set<VirusType> set) {

        // Check it here so the AI can't cheat.
        // "HEY, BUT YOU WOULDN'T MAKE IT *TRY* TO CHEAT" - Yeah, sure...
        if (arePlayerReachable(from, to)) {
            List<Cell> path = getPath(from, to);

            for (VirusType vt : VirusType.values()) {
                int sent = 0;

                if (set.contains(vt)) {
                    int curr = from.viruses.get(vt);
                    int send = curr - (curr / 2);
                    sent += send;

                    from.viruses.put(vt, curr - send);
                    for (int i = 0; i < send; i++) {
                        double ang = Math.PI * 2 * random.nextDouble();
                        double dst = from.getRadius() - 4;

                        float vX = from.posX + (float) (dst * Math.cos(ang));
                        float vY = from.posY + (float) (dst * Math.sin(ang));

                        Virus virus = new Virus(from.owner, vt, vX, vY);
                        virus.target = to;
                        virus.path = path;
                        virus.ang = random.nextFloat();
                        virus.angSpd = (float) ((random.nextGaussian() * 2 + 2) + (random.nextGaussian() * 2 - 2));
                        viruses.add(virus);
                    }
                }

                if (sent > 0) {
                    playSoundAt(Sounds.SND_SEND, from.posX, from.posY);
                }
            }
        } else if (from.owner == humanPlayer) {
            // Don't play the sound if the AI is trying to cheat!
            // "BUT, BUT, DON'T MAKE IT EVEN TRY CHEAT!!" - ... of course I won't! shut up!
            playSoundAt(Sounds.SND_ERROR, from.posX, to.posY);
        }
    }

    private List<Cell> getPath (Cell from, Cell to) {
        return graph.findPlayerPath(from, to);
    }

    public static boolean areDirectlyReachable (Cell from, Cell to) {
        float difX = from.posX - to.posX;
        float difY = from.posY - to.posY;

        double sqDist = difX * difX + difY * difY;
        double sendDst = getSendDistance(from);

        return sendDst * sendDst >= sqDist;
    }

    public boolean arePlayerReachable (Cell from, Cell to) {
        return graph.findPlayerPath(from, to) != null;
    }

    public static float getSendDistance (Cell from) {
        float fcap = (float) (from.capacity - Cell.MIN_CAPACITY) / Cell.MAX_CAPACITY;

        return SEND_DIST_MIN + (SEND_DIST_MAX - SEND_DIST_MIN) * fcap;
    }

    private void playSoundAt (Sound snd, float x, float y) {
        float difX = (x - camX) / camZoom;
        float difY = (y - camY) / camZoom;

        double dist = Math.hypot(difX, difY);

        snd.playAt(1f, Math.max(0f, 1f - (float) (dist * camZoom * camZoom) / 600f), difX / W, difY / H, 0f);
    }

    @Override
    public void render (GameContainer cont, StateBasedGame game, Graphics gr) throws SlickException {

        final Color BG_COLOR = new Color(.7f, .65f, .6f);
        final Color FG_COLOR = new Color(.95f, .93f, .9f);

        gr.setBackground(BG_COLOR);
        gr.clear();

        float ctX = W2 - (camX * camZoom);
        float ctY = H2 - (camY * camZoom);

        gr.setColor(FG_COLOR);
        gr.fillOval(ctX - RAD_CELLS * camZoom, ctY - RAD_CELLS * camZoom, RAD_CELLS * 2 * camZoom, RAD_CELLS
            * 2 * camZoom, 128);

        // Render a circle with the maximum send distance
        if (cellSelect != null) {
            float sendDist = getSendDistance(cellSelect);

            gr.setLineWidth(2);
            gr.setColor(cellSelect.owner != null ? cellSelect.owner.getLightColor() : Cell.NULL_LIGHT_COLOR);
            gr.drawOval(
                ctX + (cellSelect.posX - sendDist) * camZoom, ctY + (cellSelect.posY - sendDist) * camZoom, sendDist
                    * 2 * camZoom, sendDist * 2 * camZoom);

            // Render an arrow when a cell of your color is selected
            if (cellOver != null && cellSelect.owner == humanPlayer && cellSelect != cellOver) {
                gr.setLineWidth(2);
                gr.setColor(cellSelect.owner.getDarkColor());

                List<Cell> path = graph.findPlayerPath(cellSelect, cellOver);

                if (path != null) {
                    path.add(cellOver);
                    Cell prev = cellSelect;

                    for (Cell curr : path) {
                        float difY = prev.posY - curr.posY;
                        float difX = prev.posX - curr.posX;
                        float currRad = curr.getRadius();

                        gr.drawLine(
                            curr.posX * camZoom + ctX, curr.posY * camZoom + ctY, prev.posX * camZoom + ctX, prev.posY
                                * camZoom + ctY);

                        double ang = Math.atan2(difY, difX);

                        float pcX = ctX + (float) (curr.posX + (currRad + 2) * Math.cos(ang)) * camZoom;
                        float pcY = ctY + (float) (curr.posY + (currRad + 2) * Math.sin(ang)) * camZoom;

                        float p1X = (float) (pcX + 8 * Math.cos(ang + 0.5));
                        float p1Y = (float) (pcY + 8 * Math.sin(ang + 0.5));

                        float p2X = (float) (pcX + 8 * Math.cos(ang - 0.5));
                        float p2Y = (float) (pcY + 8 * Math.sin(ang - 0.5));

                        gr.drawLine(pcX, pcY, p1X, p1Y);
                        gr.drawLine(pcX, pcY, p2X, p2Y);

                        prev = curr;
                    }
                }
            }
        }

        // Render all viruses
        for (Virus virus : viruses) {
            virus.render(gr, ctX, ctY, camZoom);
        }

        // Render all cells
        for (Cell cell : cells) {
            cell.render(gr, ctX, ctY, cell == cellOver || cell == cellSelect, camZoom);
        }

        // Render the cell info
        for (Cell cell : cells) {
            if (cell.owner == humanPlayer && (cell == cellOver || cell == cellSelect)) {
                gr.setLineWidth(1);
                gr.setFont(LudumDare23Game.GAME_FONT);

                for (VirusType vt : VirusType.values()) {
                    float x = ctX + cell.posX * camZoom;
                    float y =
                        ctY
                            + (cell.posY - cell.getRadius() - 12) * camZoom - 12
                            * (VirusType.values().length - vt.ordinal() - 1);

                    Shape s =
                        vt
                            .getShape().transform(Transform.createScaleTransform(3, 3))
                            .transform(Transform.createTranslateTransform(x, y));

                    gr.setColor(cell.owner.getLightColor());
                    gr.fill(s);
                    gr.setColor(cell.owner.getDarkColor());
                    gr.draw(s);
                    gr.drawString(String.valueOf(cell.viruses.get(vt)), x + 8, y - 8);

                }
            }
        }

        // Render cell number information
        gr.setFont(LudumDare23Game.GAME_FONT);
        for (Player player : Player.values()) {
            float x = 8;
            float y = H - 8 - player.ordinal() * 14;

            gr.setColor(player.getLightColor());
            gr.fillOval(x - 5, y - 5, 9, 9);

            gr.setColor(player.getDarkColor());
            gr.drawOval(x - 5, y - 5, 9, 9);

            gr.drawString(String.valueOf(numCells.get(player)), x + 8, y - 8);
        }

        // GAME OVER
        if (gameOver || paused) {
            gr.setColor(new Color(0, 0, 0, .7f));
            gr.fillRect(0, 0, W, H);

            gr.setFont(LudumDare23Game.GOVER_FONT);
            gr.setColor(Color.white);

            if (gameOver) {

                String gover = "GAME OVER";
                gr.drawString(gover, W2 - LudumDare23Game.GOVER_FONT.getWidth(gover) / 2, 64);

                String info = won ? "You won!" : "You lost :(";
                gr.drawString(info, W2 - LudumDare23Game.GOVER_FONT.getWidth(info) / 2, 256);
            } else {

                String paused = "Paused";
                gr.drawString(paused, W2 - LudumDare23Game.GOVER_FONT.getWidth(paused) / 2, H2
                    - LudumDare23Game.GOVER_FONT.getHeight(paused));
            }
        }
    }

    @Override
    public void mouseWheelMoved (int val) {
        if (val > 0) {
            camZoom = Math.min(camZoom + ZOOM_STEP, ZOOM_MAX);
        } else if (val < 0) {
            camZoom = Math.max(camZoom - ZOOM_STEP, ZOOM_MIN);
        }
    }
}
