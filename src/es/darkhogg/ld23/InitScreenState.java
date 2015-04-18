package es.darkhogg.ld23;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public final class InitScreenState extends BasicGameState {

    private Image title;

    float angR, angG, angB;

    @Override
    public int getID () {
        return LudumDare23Game.ID_STATE_INIT;
    }

    @Override
    public void init (GameContainer container, StateBasedGame game) throws SlickException {
        title = new Image("title.png");
    }

    @Override
    public void render (GameContainer container, StateBasedGame game, Graphics gr) throws SlickException {
        gr.setFont(LudumDare23Game.GAME_FONT);

        gr.setBackground(new Color(.9f, .85f, .8f));
        gr.clear();
        gr.drawImage(
            title, (LudumDare23Game.WIDTH - title.getWidth()) / 2, LudumDare23Game.HEIGHT / 5 - title.getHeight() / 2);

        // Draw a red triangle spinning and some text explaining red bonuses
        Shape sR =
            VirusType.ATTACKER
                .getShape()
                .transform(Transform.createRotateTransform(angR))
                .transform(Transform.createScaleTransform(32, 32))
                .transform(
                    Transform.createTranslateTransform(LudumDare23Game.WIDTH / 6, LudumDare23Game.HEIGHT - (240)));
        gr.setColor(Player.RED.getLightColor());
        gr.fill(sR);
        gr.setColor(Player.RED.getDarkColor());
        gr.draw(sR);
        gr.drawString("RED :: Offensive", LudumDare23Game.WIDTH / 6 + 48, LudumDare23Game.HEIGHT - 240 - 24);
        gr
            .drawString(
                "+25% damage to healthy cells", LudumDare23Game.WIDTH / 6 + 64, LudumDare23Game.HEIGHT - 240 - 12);
        gr.drawString(
            "+10% bonus damage for triangles to infected cells", LudumDare23Game.WIDTH / 6 + 64,
            LudumDare23Game.HEIGHT - 240);

        // Green Circle now
        Shape sG =
            VirusType.GENERATOR
                .getShape()
                .transform(Transform.createRotateTransform(angG))
                .transform(Transform.createScaleTransform(32, 32))
                .transform(
                    Transform.createTranslateTransform(LudumDare23Game.WIDTH / 6, LudumDare23Game.HEIGHT - (160)));
        gr.setColor(Player.GREEN.getLightColor());
        gr.fill(sG);
        gr.setColor(Player.GREEN.getDarkColor());
        gr.draw(sG);
        gr.drawString("GREEN :: Reproductive", LudumDare23Game.WIDTH / 6 + 48, LudumDare23Game.HEIGHT - 160 - 24);
        gr.drawString("+25% base reproduction rate", LudumDare23Game.WIDTH / 6 + 64, LudumDare23Game.HEIGHT - 160 - 12);
        gr.drawString(
            "+10% reproduction bonus for circles", LudumDare23Game.WIDTH / 6 + 64, LudumDare23Game.HEIGHT - 160);

        // Blue square at last
        Shape sB =
            VirusType.DEFENDER
                .getShape()
                .transform(Transform.createRotateTransform(angB))
                .transform(Transform.createScaleTransform(32, 32))
                .transform(Transform.createTranslateTransform(LudumDare23Game.WIDTH / 6, LudumDare23Game.HEIGHT - (80)));
        gr.setColor(Player.BLUE.getLightColor());
        gr.fill(sB);
        gr.setColor(Player.BLUE.getDarkColor());
        gr.draw(sB);
        gr.drawString("BLUE :: Defensive", LudumDare23Game.WIDTH / 6 + 48, LudumDare23Game.HEIGHT - 80 - 24);
        gr.drawString("+25% base defense", LudumDare23Game.WIDTH / 6 + 64, LudumDare23Game.HEIGHT - 80 - 12);
        gr.drawString("+10% defense bonus for squares", LudumDare23Game.WIDTH / 6 + 64, LudumDare23Game.HEIGHT - 80);
    }

    @Override
    public void update (GameContainer container, StateBasedGame game, int delta) throws SlickException {
        float fdelta = delta / 1000f;

        angR += fdelta * 1.2f;
        angG += fdelta * 1.3f;
        angB += fdelta * 1.1f;

        Input input = container.getInput();

        if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
            Player player = null;

            int y = LudumDare23Game.HEIGHT - input.getMouseY();
            if (y > 80 - 32 && y < 80 + 32) {
                player = Player.BLUE;
            }
            if (y > 160 - 32 && y < 160 + 32) {
                player = Player.GREEN;
            }
            if (y > 240 - 32 && y < 240 + 32) {
                player = Player.RED;
            }

            if (player != null) {
                ((PlayState) game.getState(LudumDare23Game.ID_STATE_PLAY)).humanPlayer = player;
                game.enterState(LudumDare23Game.ID_STATE_PLAY);
            }
        }
    }

}
