package es.darkhogg.ld23;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

public interface AICellSelector {
	
	public void select ( PlayState state, Player player, Collection<Cell> cells, Random random );
	
	public boolean isMoveWanted ();
	
	public Cell getSelectedOrigin ();
	
	public Cell getSelectedTarget ();

	public Set<VirusType> getSelectedTypes ();
	
}
