package es.darkhogg.ld23;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public final class DefaultAICellSelector implements AICellSelector {
	
	private Cell origin, target;
	private final Set<VirusType> virusTypes = EnumSet.allOf( VirusType.class );
	
	@Override
	public void select ( PlayState state, Player player, Collection<Cell> cells, Random random ) {
		origin = target = null;
		
		// Get all cells of the player
		ArrayList<Cell> playerCells = new ArrayList<Cell>();
		for ( Cell cell : cells ) {
			if ( cell.owner == player && ( (float) cell.getTotalViruses() / cell.capacity ) >= .7f ) {
				playerCells.add( cell );
			}
		}
		
		// Select one cell randomly
		if ( !playerCells.isEmpty() ) {
			origin = playerCells.get( random.nextInt( playerCells.size() ) );
			
			// Get all reachable cells
			ArrayList<Cell> reachablePlayerCells = new ArrayList<Cell>();
			ArrayList<Cell> reachableOtherCells = new ArrayList<Cell>();
			for ( Cell cell : cells ) {
				if ( cell != origin && state.arePlayerReachable( origin, cell ) ) {
					if ( cell.owner == player ) {
						reachablePlayerCells.add( cell );
					} else {
						reachableOtherCells.add( cell );
					}
				}
			}
			
			// Select another cell randomly
			if ( reachableOtherCells.isEmpty() ) {
				if ( !reachablePlayerCells.isEmpty() ) {
					target = reachablePlayerCells.get( random.nextInt( reachablePlayerCells.size() ) );
				}
			} else {
				target = reachableOtherCells.get( random.nextInt( reachableOtherCells.size() ) );
			}
		}
	}
	
	@Override
	public boolean isMoveWanted () {
		return origin != null && target != null;
	}
	
	@Override
	public Cell getSelectedOrigin () {
		return origin;
	}
	
	@Override
	public Cell getSelectedTarget () {
		return target;
	}
	
	@Override
	public Set<VirusType> getSelectedTypes () {
		return virusTypes;
	}
	
}
