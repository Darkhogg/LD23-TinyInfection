package es.darkhogg.ld23;

import java.util.Random;


public final class ArtificialIntelligence {

	private static final int MIN_TICK_TIME = 5000;
	private static final int MAX_TICK_TIME = 10000;
	
	private final Player player;
	private final AICellSelector selector;
	
	private int tickTime = MAX_TICK_TIME;
	
	public ArtificialIntelligence ( Player player, AICellSelector selector ) {
		this.player = player;
		this.selector = selector;
	}
	
	public void update ( PlayState state, int delta, Random random ) {
		tickTime -= delta;
		
		if ( tickTime <= 0 ) {
			tickTime += MIN_TICK_TIME + random.nextInt( MAX_TICK_TIME - MIN_TICK_TIME );
			
			selector.select( state, player, state.cells, random );
			if ( selector.isMoveWanted() ) {
				state.sendViruses( selector.getSelectedOrigin(), selector.getSelectedTarget(), selector.getSelectedTypes() );
			}
		}
	}
	
}
