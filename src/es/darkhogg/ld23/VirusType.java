package es.darkhogg.ld23;

import java.util.Random;

import org.newdawn.slick.geom.Ellipse;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

public enum VirusType {
    /** An attacker virus with a triangle for shape */
    ATTACKER(new Polygon(new float[] {
        (float) Math.cos(Math.PI * 2 / 3), (float) Math.sin(Math.PI * 2 / 3), (float) Math.cos(Math.PI * 4 / 3),
        (float) Math.sin(Math.PI * 4 / 3), (float) Math.cos(Math.PI * 6 / 3), (float) Math.sin(Math.PI * 6 / 3), })
        .transform(Transform.createScaleTransform(1.3f, 1.3f))),

    /** A defender virus with a square for shape */
    DEFENDER(new Polygon(new float[] {
        (float) Math.cos(Math.PI * 2 / 4), (float) Math.sin(Math.PI * 2 / 4), (float) Math.cos(Math.PI * 4 / 4),
        (float) Math.sin(Math.PI * 4 / 4), (float) Math.cos(Math.PI * 6 / 4), (float) Math.sin(Math.PI * 6 / 4),
        (float) Math.cos(Math.PI * 8 / 4), (float) Math.sin(Math.PI * 8 / 4), }).transform(Transform
        .createScaleTransform(1.15f, 1.15f))),

    /** A generator virus with a circle for shape */
    GENERATOR(new Ellipse(0, 0, 1, 1).transform(Transform.createScaleTransform(1.1f, .9f))),

    ;

    private final Shape shape;

    private VirusType (Shape shape) {
        this.shape = shape;
    }

    public Shape getShape () {
        return shape;
    }

    public static VirusType getRandomWithNull (Random random) {
        if (random.nextBoolean()) {
            return null;
        } else {
            return getRandom(random);
        }
    }

    public static VirusType getRandom (Random random) {
        return values()[random.nextInt(values().length)];
    }
}
