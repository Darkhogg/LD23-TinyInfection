package es.darkhogg.ld23;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

public final class Cell {

    final static Color NULL_BASE_COLOR = new Color(.5f, .5f, .5f);
    final static Color NULL_LIGHT_COLOR = new Color(.75f, .75f, .75f);
    final static Color NULL_DARK_COLOR = new Color(.25f, .25f, .25f);

    /* package */final static int MIN_CAPACITY = 24;
    /* package */final static int MAX_CAPACITY = 128;

    /* package */Player owner;

    /* package */float posX;
    /* package */float posY;

    /* package */float spdX;
    /* package */float spdY;

    /* package */int capacity;
    /* package */float life;
    /* package */final Map<VirusType,Integer> viruses = new EnumMap<VirusType,Integer>(VirusType.class);
    /* package */VirusType preference;

    /* package */float next = 0;
    /* package */float ang = 0;

    public Cell (int capacity, double x, double y, VirusType pref) {
        this.capacity = capacity;
        life = capacity;
        preference = pref;

        posX = (float) x;
        posY = (float) y;

        for (VirusType vt : VirusType.values()) {
            viruses.put(vt, 0);
        }
    }

    public float getRadius () {
        return 16 + ((capacity - MIN_CAPACITY) * 16) / (MAX_CAPACITY - MIN_CAPACITY);
    }

    public int getTotalViruses () {
        int sum = 0;

        for (int i : viruses.values()) {
            sum += i;
        }

        return sum;
    }

    public void update (float fdelta) {
        posX += fdelta * spdX;
        posY += fdelta * spdY;

        double spd = Math.hypot(spdX, spdY);
        if (spd > 3) {
            spdX *= Math.pow(0.2f, fdelta);
            spdY *= Math.pow(0.2f, fdelta);
        } else if (spd > 1) {
            spdX *= Math.pow(0.5f, fdelta);
            spdY *= Math.pow(0.5f, fdelta);
        } else {
            spdX *= Math.pow(0.99f, fdelta);
            spdY *= Math.pow(0.99f, fdelta);
        }

        if (owner != null) {
            float base = owner == Player.GREEN ? .25f : .2f;
            float mult = owner == Player.GREEN ? .11f : .1f;
            next += fdelta * (base + viruses.get(VirusType.GENERATOR) * mult);
            if (next > 1) {
                next -= 1;
                if (getTotalViruses() < capacity) {
                    VirusType type = getRandomVirusType();
                    viruses.put(type, viruses.get(type) + 1);
                }
            }
        }

        ang += fdelta * 0.5f;
    }

    private VirusType getRandomVirusType () {
        if (preference != null && Math.random() < 0.5) {
            return preference;
        } else {
            return VirusType.values()[(int) (Math.random() * VirusType.values().length)];
        }
    }

    public void render (Graphics gr, float ctX, float ctY, boolean highlight, float camZoom) throws SlickException {
        float rad = getRadius();

        // Color base = ( owner == null ) ? NULL_BASE_COLOR : owner.getBaseColor();
        Color light = (owner == null) ? NULL_LIGHT_COLOR : owner.getLightColor();
        Color dark = (owner == null) ? NULL_DARK_COLOR : owner.getDarkColor();

        gr.setColor(light);
        gr.fillOval(ctX + (posX - rad) * camZoom, ctY + (posY - rad) * camZoom, rad * 2 * camZoom, rad * 2 * camZoom);

        gr.setLineWidth(highlight ? 3 : 1);
        gr.setColor(dark);
        gr.drawOval(ctX + (posX - rad) * camZoom, ctY + (posY - rad) * camZoom, rad * 2 * camZoom, rad * 2 * camZoom);

        if (preference != null) {
            gr.setLineWidth(1);

            Shape s =
                preference
                    .getShape().transform(Transform.createRotateTransform(ang))
                    .transform(Transform.createScaleTransform(12 * camZoom, 12 * camZoom))
                    .transform(Transform.createTranslateTransform(ctX + posX * camZoom, ctY + posY * camZoom));

            gr.setLineWidth(1);

            gr.setColor(dark);
            gr.draw(s);
        }

        String str = String.valueOf(owner == null ? (int) life : getTotalViruses());

        int w = LudumDare23Game.GAME_FONT.getWidth(str);
        int h = LudumDare23Game.GAME_FONT.getHeight(str);
        gr.setFont(LudumDare23Game.GAME_FONT);
        gr.setColor(Color.black);
        gr.drawString(str, ctX + (posX * camZoom) - 1 - w / 2, ctY + (posY * camZoom) - h / 2 - 1);
    }

    public void inject (Virus virus, Random random) {
        if (owner == null) {
            // The cell is free
            float mult = (virus.owner == Player.RED) ? 1.25f : 1f;
            life -= mult * (virus.vt == VirusType.ATTACKER ? 2.5f : 1.25f);
            if (life <= 0f) {
                owner = virus.owner;
                life = 0;
            }

        } else if (owner == virus.owner) {
            // Cell is already yours
            if (getTotalViruses() >= capacity) {
                killVirus(random);
            }
            viruses.put(virus.vt, viruses.get(virus.vt) + 1);

        } else {
            // Cell is being conquered!
            float triAtk = (virus.owner == Player.RED) ? 2.31f : 2.1f;
            float attack = virus.vt == VirusType.ATTACKER ? triAtk : .7f;

            float defBase = owner == Player.BLUE ? 1f : .8f;
            float defBonus = owner == Player.BLUE ? .055f : .05f;
            float defense =
                (defBase + viruses.get(VirusType.DEFENDER) * defBonus) / (virus.vt == VirusType.ATTACKER ? 1.1f : 1f);

            life -= attack / defense;
            while (life < -1f) {
                if (getTotalViruses() > 0) {
                    killVirus(random);
                    life += 1f;
                } else {
                    owner = virus.owner;
                    life = 0;
                }
            }
        }
    }

    private void killVirus (Random random) {
        VirusType vt = VirusType.getRandom(random);
        int n = 0;
        while (n == 0) {
            n = viruses.get(vt);
            if (n == 0) {
                vt = VirusType.values()[(vt.ordinal() + 1) % VirusType.values().length];
            } else {
                viruses.put(vt, n - 1);
            }
        }
    }

}
