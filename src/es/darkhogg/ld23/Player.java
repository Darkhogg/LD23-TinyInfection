package es.darkhogg.ld23;

import org.newdawn.slick.Color;

public enum Player {
	RED( Color.red ), GREEN( Color.green ), BLUE( Color.blue );
	
	private final Color baseColor;
	private final Color darkColor;
	private final Color lightColor;
	
	private Player ( Color color ) {
		this.baseColor = color;
		
		lightColor =
			new Color( ( baseColor.getRed() * 127 ) / 255 + 128, ( baseColor.getGreen() * 127 ) / 255 + 128,
				( baseColor.getBlue() * 127 ) / 255 + 128 );
		darkColor =
			new Color( ( baseColor.getRed() * 127 ) / 255, ( baseColor.getGreen() * 127 ) / 255,
				( baseColor.getBlue() * 127 ) / 255 );
	}
	
	public Color getBaseColor () {
		return baseColor;
	}
	
	public Color getDarkColor () {
		return darkColor;
	}
	
	public Color getLightColor () {
		return lightColor;
	}
}
