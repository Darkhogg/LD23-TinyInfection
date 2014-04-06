package es.darkhogg.ld23;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class CellGraph {
	
	private final ArrayList<Cell> cellList;
	private final Map<Cell,Integer> cellMap;
	
	public CellGraph ( Collection<Cell> cells ) {
		cellList = new ArrayList<Cell>( cells );
		cellMap = new HashMap<Cell,Integer>();
		
		for ( int i = 0; i < cellList.size(); i++ ) {
			cellMap.put( cellList.get( i ), i );
		}
	}
	
	public List<Cell> findPlayerPath ( Cell from, Cell to ) {
		// Hello, Dijkstra, we meet again...
		// But this time, I won't lose to you...
		
		int[] prev = new int[ cellList.size() ];
		double[] dist = new double[ cellList.size() ];
		boolean[] vis = new boolean[ cellList.size() ];
		
		Player player = from.owner;
		int iFrom = cellMap.get( from );
		int iTo = cellMap.get( to );
		
		// Reset the structures
		for ( int i = 0; i < cellList.size(); i++ ) {
			
			prev[ i ] = iFrom;
			
			Cell c = cellList.get( i );
			if ( ( i == iTo || c.owner == player ) && PlayState.areDirectlyReachable( from, c ) ) {
				dist[ i ] = Math.hypot( from.posX - c.posX, from.posY - c.posY );
			} else {
				dist[ i ] = Double.POSITIVE_INFINITY;
			}
			
			vis[ i ] = ( i == iFrom );
		}
		
		boolean keepLooping = !vis[ iTo ];
		while ( keepLooping ) {
			// Pick the minimal unvisited reachable node
			double iDist = Double.POSITIVE_INFINITY;
			int iNext = -1;
			for ( int i = 0; i < cellList.size(); i++ ) {
				if ( !vis[ i ] && dist[ i ] < iDist ) {
					iDist = dist[ i ];
					iNext = i;
				}
			}
			
			if ( iNext == -1 ) {
				return null;
			}
			
			// Update the structures
			vis[ iNext ] = true;
			for ( int i = 0; i < cellList.size(); i++ ) {
				
				double adjdst;
				Cell n = cellList.get( iNext );
				Cell c = cellList.get( i );
				if ( ( i == iTo || c.owner == player ) && PlayState.areDirectlyReachable( n, c ) ) {
					adjdst = Math.hypot( n.posX - c.posX, n.posY - c.posY );
				} else {
					adjdst = Double.POSITIVE_INFINITY;
				}
				
				if ( !vis[ i ] && dist[ iNext ] + adjdst < dist[ i ] ) {
					dist[ i ] = dist[ iNext ] + adjdst;
					prev[ i ] = iNext;
				}
			}
			
			// If it's what we're looking for, BINGO
			if ( iNext == iTo ) {
				keepLooping = false;
			}
		}
		
		LinkedList<Cell> path = new LinkedList<Cell>();
		int p = prev[ iTo ];
		while ( p != iFrom ) {
			path.addFirst( cellList.get( p ) );
			p = prev[ p ];
		}
		return new ArrayList<Cell>( path );
	}
}
