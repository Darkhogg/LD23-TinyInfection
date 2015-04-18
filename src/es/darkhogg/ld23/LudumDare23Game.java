package es.darkhogg.ld23;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.state.StateBasedGame;

@SuppressWarnings("unchecked")
public final class LudumDare23Game extends StateBasedGame {

    private static final String TITLE = "LD23";

    /* package */static final int WIDTH = 800;
    /* package */static final int HEIGHT = 600;
    /* package */static final int SCALE = 1;

    /* package */static final int ID_STATE_PLAY = 1;
    /* package */static final int ID_STATE_INIT = 2;

    /* package */static UnicodeFont GAME_FONT;
    /* package */static UnicodeFont GOVER_FONT;

    public LudumDare23Game () {
        super(TITLE);
    }

    @Override
    public boolean closeRequested () {
        return true;
    }

    @Override
    public String getTitle () {
        return TITLE;
    }

    @Override
    public void initStatesList (GameContainer cont) throws SlickException {
        GAME_FONT = new UnicodeFont("Ubuntu-L.ttf", 12, false, false);
        GAME_FONT.addAsciiGlyphs();
        GAME_FONT.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
        GAME_FONT.loadGlyphs();

        GOVER_FONT = new UnicodeFont("Ubuntu-L.ttf", 64, true, false);
        GOVER_FONT.addAsciiGlyphs();
        GOVER_FONT.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
        GOVER_FONT.loadGlyphs();

        Sounds.SND_ERROR = new Sound("error.wav");
        Sounds.SND_INJECT = new Sound("inject.wav");
        Sounds.SND_SEND = new Sound("send.wav");
        Sounds.SND_HIT = new Sound("hit.wav");
        Sounds.SND_INFECT = new Sound("infect.wav");

        addState(new InitScreenState());
        addState(new PlayState());

        getState(ID_STATE_PLAY).init(cont, this);
        getState(ID_STATE_INIT).init(cont, this);

        enterState(ID_STATE_INIT);
    }

}
