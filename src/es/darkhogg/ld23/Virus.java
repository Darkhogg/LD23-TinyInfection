package es.darkhogg.ld23;

import java.util.List;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

public final class Virus {

    /* package */final Player owner;
    /* package */final VirusType vt;

    /* package */List<Cell> path;
    /* package */int pathPos;
    /* package */Cell target;

    /* package */float posX;
    /* package */float posY;

    /* package */float spdX;
    /* package */float spdY;

    /* package */float ang;
    /* package */float angSpd;

    public Virus (Player owner, VirusType vt, double x, double y) {
        this.owner = owner;
        this.vt = vt;

        posX = (float) x;
        posY = (float) y;
    }

    public void update (float fdelta) {
        posX += spdX * fdelta;
        posY += spdY * fdelta;

        spdX *= Math.pow(.5f, fdelta);
        spdY *= Math.pow(.5f, fdelta);

        Cell next = (path == null || pathPos < 0 || pathPos >= path.size()) ? target : path.get(pathPos);

        double ang = Math.atan2(next.posY - posY, next.posX - posX);

        spdX += fdelta * 64f * Math.cos(ang);
        spdY += fdelta * 64f * Math.sin(ang);

        this.ang += fdelta * angSpd;
    }

    public void render (Graphics gr, float ctX, float ctY, float camZoom) {
        Shape s =
            vt
                .getShape().transform(Transform.createRotateTransform(ang))
                .transform(Transform.createScaleTransform(4 * camZoom, 4 * camZoom))
                .transform(Transform.createTranslateTransform(ctX + posX * camZoom, ctY + posY * camZoom));

        gr.setLineWidth(1);

        gr.setColor(owner.getLightColor());
        gr.fill(s);

        gr.setColor(owner.getDarkColor());
        gr.draw(s);
    }

}
