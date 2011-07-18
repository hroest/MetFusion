/**
 * created by Michael Gerlich on Apr 7, 2010
 * last modified Apr 7, 2010 - 2:45:15 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.metfusion.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;

import de.ipbhalle.metfusion.wrapper.ScoreRankPair;


public class MatrixUtils {

	public static enum Ways {
		GREATER, LESS
	}
	
	public static enum Order {
		ASCENDING, DESCENDING
	}
	
	public static void main(String[] args) {
		double[] values = {0.89d, 0.89d, 0.73d, 0.6d, 0.6d, 0.5d, 0.4d, 0.3d, 0.8d, 0.5d, 0.3d, 0.6d, 0.6d, 0.4d};
		
		System.out.println("which values => 0.5:");
		int[] idx = which(values, 0.5d, true, Ways.GREATER);
		for (int i = 0; i < idx.length; i++) {
			System.out.println("idx["+i+"] = " + idx[i] + " -> " + values[idx[i]]);
		}
		System.out.println();
		
		System.out.println("which values are minimal:");
		int[] mins = whichMin(values);
		for (int i = 0; i < mins.length; i++) {
			System.out.println("mins["+i+"] = " + mins[i] + " -> " + values[mins[i]]);
		}
		System.out.println();
		
		System.out.println("which values are maximal:");
		int[] maxs = whichMax(values);
		for (int i = 0; i < maxs.length; i++) {
			System.out.println("maxs["+i+"] = " + maxs[i] + " -> " + values[maxs[i]]);
		}
		System.out.println();
		
		System.out.println("what is min and max:");
		double min = min(values);
		double max = max(values);
		System.out.println("min = " + min + "\tmax = " + max + "\n");
		
		System.out.println("ranks:");
		int[] ranks = rank(values, Order.DESCENDING);
		for (int i = 0; i < ranks.length; i++) {
			System.out.print(ranks[i] + "  ");
		}
		System.out.println();
		
		System.out.println("new ranking test:");
		Queue<ScoreRankPair> q = ranking(values);
		System.out.println();
		for (Iterator<ScoreRankPair> it = q.iterator(); it.hasNext();) {
			ScoreRankPair srp = (ScoreRankPair) it.next();
			System.out.println(srp.getScore() + "  " + srp.getRank() + "  " + srp.getIndex());
		}
	}
	
	public static Queue<ScoreRankPair> ranking(double[] arr) {
		//int[] ranks = new int[arr.length];		// array of tied ranks
		List<Double> values = new ArrayList<Double>();	// list of unique scores from array arr
		Map<Double, Integer> map = new HashMap<Double, Integer>();	// mapping of scores to number of same scores present in arr
		Map<Double, List<Integer>> findPositions = new HashMap<Double, List<Integer>>();	// speichere indexposition des vorkommens des wertes
		
		for (int i = 0; i < arr.length; i++) {
			double val = arr[i];
			if(!values.contains(val)) {
				List<Integer> position = new ArrayList<Integer>();
				position.add(i);						// fuege indexposition des aktuellen double wertes zu liste hinzu
				findPositions.put(arr[i], position);	// speichere double-wert und liste mit auftretenden indices
				
				values.add(arr[i]);			// fuege der liste den uniqen wert hinzu
				map.put(arr[i], 1);			// speichere den wert mit aktuellem #vorkommen von 1
			}
			else {
				int count = map.get(val);	// erhoehe vorkommen des wertes um 1
				count++;
				map.put(val, count);		// und speichere neues vorkommen fuer aktuellen wert
				
				List<Integer> position = findPositions.get(val);
				position.add(i);			// aktualisiere positionen des wertes
				findPositions.put(val, position);
			}
		}
		
		Collections.sort(values);		// sortiere unique liste von vorhandenen double werten	-> max hinten, min vorne
		Collections.reverse(values);	// vertausche reihenfolge der sortierten liste damit max vorne und min hinten
		
		int count = 0;		// counter fuer anzahl elemente
		int sumRank = 0;	// summe der bisher erstellten raenge
		Queue<ScoreRankPair> q = new ArrayBlockingQueue<ScoreRankPair>(arr.length);
		for (Iterator<Double> it = values.iterator(); it.hasNext();) {
			double val = it.next().doubleValue();		// aktueller wert, beginnend beim max, absteigend iteriert
			List<Integer> positions = findPositions.get(val);
			int rank = map.get(val);
			
			if(count > 0)	// rank veraenderung erst ab index 1
				rank = rank + sumRank;
			
			for (int i = 0; i < positions.size(); i++) {
				//q.add(positions.get(i));
				//ScoreRankPair srp = new ScoreRankPair(val, rank, positions.get(i));
				q.add(new ScoreRankPair(val, rank, positions.get(i)));
			}
			count++;
			sumRank = sumRank + positions.size();	// summe aller bisherigen raenge neu setzen/inkrementieren
		}
		
//		for (Iterator<ScoreRankPair> it = q.iterator(); it.hasNext();) {
//			ScoreRankPair srp = (ScoreRankPair) it.next();
//			System.out.println(srp.getScore() + "  " + srp.getRank() + "  " + srp.getIndex());
//		}
		
		return q;
	}
	
//	private static String[] gradient = {"#FF0000", "#FF1900", "#FF3300", "#FF4C00", "#FF6600", "#FF7F00", "#FF9900", "#FFB200", "#FFCC00", "#FFE500",
//		"#FFFF00", "#E5FF00", "#CCFF00", "#B2FF00", "#99FF00", "#7FFF00", "#66FF00", "#4CFF00", "#32FF00", "#19FF00", "#00FF00"};
	
	public static int[] which(double[] arr, double thresh, boolean include, Ways way) {
		if(thresh < 0d || thresh > 1.0d) {
			thresh = 0.5d;
		}
		
		// only sort arr (ascending) if way is LESS (<)
		//Arrays.sort(arr);
		int[] idx = new int[arr.length];
		List<Integer> list = new ArrayList<Integer>();
		
		switch(way) {
			case GREATER: {
				if(include)
					System.out.println("Using >=");
				else System.out.println("Using >");
				
				int counter = 0;
				for (int i = 0; i < arr.length; i++) {
					if(include && arr[i] >= thresh) {
						//idx[counter] = i;
						list.add(i);
						counter++;
					}
					else if(!include && arr[i] > thresh) {
						//idx[counter] = i;
						list.add(i);
						counter++;
					}
					//else break;	// break as no more fitting elements are encountered
				}
				break;
			}
				
			case LESS: {
				if(include)
					System.out.println("Using <=");
				else System.out.println("Using <");
				
				// sort ascending
				Arrays.sort(arr);
				
				int counter = 0;
				for (int i = 0; i < arr.length; i++) {
					if(include && arr[i] <= thresh) {
						//idx[counter] = i;
						list.add(i);
						counter++;
					}
					else if(!include && arr[i] < thresh) {
						//idx[counter] = i;
						list.add(i);
						counter++;
					}
					//else break;	// break as no more fitting elements are encountered
				}
				break;
			}
				
			default: {
				System.out.println("Using >=");
				if(!include)
					include = true;
				
				int counter = 0;
				for (int i = 0; i < arr.length; i++) {
					if(include && arr[i] >= thresh) {
						//idx[counter] = i;
						list.add(i);
						counter++;
					}
				}
				break;
			}
		}

		idx = new int[list.size()];
		for (int i = 0; i < idx.length; i++) {
			idx[i] = list.get(i);
		}
		return idx;
	}
	
	/**
	 * Returns the indices of the elements in arr that equal min.
	 * 
	 * @param arr the arr
	 * 
	 * @return the int[] 
	 */
	public static int[] whichMin(double[] arr) {
		double[] copy = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			copy[i] = arr[i];
		}
		Arrays.sort(copy);		// sort array ascending - minimum first
		
		double min = copy[0];
		int count = 1;
		for (int i = 1; i < copy.length; i++) {
			if(copy[i] == min)
				count++;
			else break;
		}
		
		int[] result = new int[count];
		int check = 0;
		for (int i = 0; i < arr.length; i++) {
			if(arr[i] == min && check < count) {
				result[check] = i;
				check++;
			}
			if(check == count)
				break;
		}
		return result;
	}
	
	/**
	 * Returns the indices of the elements in arr that equal max.
	 * 
	 * @param arr the array 
	 * 
	 * @return the int[] 
	 */
	public static int[] whichMax(double[] arr) {
		double[] copy = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			copy[i] = arr[i];
		}
		Arrays.sort(copy);		// sort array ascending - maximum last
		
		double max = copy[copy.length - 1];
		int count = 1;
		for (int i = copy.length - 2; i > 0; i--) {
			if(copy[i] == max)
				count++;
			else break;
		}
		
		int[] result = new int[count];
		int check = 0;
		for (int i = 0; i < arr.length; i++) {
			if(arr[i] == max && check < count) {
				result[check] = i;
				check++;
			}
			if(check == count)
				break;
		}
		return result;
	}
	
	/**
	 * Retrieves the minimum value of the array.
	 * 
	 * @param arr the array to be searched for
	 * 
	 * @return the minimum as double 
	 */
	public static double min(double[] arr) {
		double[] copy = Arrays.copyOf(arr, arr.length);
		Arrays.sort(copy);	// sort ascending, minimum at first position
		return copy[0];
	}
	
	/**
	 * Retrieves the maximum value of the array.
	 * 
	 * @param arr the array to be searched for
	 * 
	 * @return the maximum as double
	 */
	public static double max(double[] arr) {
		double[] copy = Arrays.copyOf(arr, arr.length);
		Arrays.sort(copy);	// sort ascending, maximum at last position
		return copy[copy.length -1];
	}
	
	/**
	 * Implements tied.ranks() method from R with ties.method="max".
	 * 
	 * @param arr
	 * @param order
	 * @return
	 */
	public static int[] rank(double[] arr, Order order) {
		int[] ranking = new int[arr.length];
		Set<Double> set = new TreeSet<Double>();
		Map<Double, Integer> ranks = new HashMap<Double, Integer>();
		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i]);
			if(ranks.containsKey(arr[i])) {
				continue;
			}
			int rank = getRank(arr, arr[i]);
			ranks.put(arr[i], rank);
		}
		
		set = ranks.keySet();	// all keys from ranks
		List<Double> list = new ArrayList<Double>();
		list.addAll(set);		// add all keys from set to list
		Collections.sort(list);	// sort list ascending
		Collections.reverse(list);	// reverse list order == descending
		
		int sum = 0;
		int count = 1;
		int prev = 0;
		// tied.ranking with maximum ties.method
		for (Double d : list) {
			count = ranks.get(d);
			for (int i = prev; i < count + prev; i++) {
				ranking[i] = count + prev;
			}
			sum += ranks.get(d);
			prev = sum;
		}
		System.out.println("sum should be " + arr.length + ", is -> " + sum);
		return ranking;
	}
	
	/**
	 * Retrieves the rank of a certain number inside an array.
	 * 
	 * @param arr the arr
	 * @param number the number of the rank 
	 * 
	 * @return the rank - returns 0 if number not found in array!
	 */
	private static int getRank(double[] arr, double number) {
		int rank = 0;
		for (int i = 0; i < arr.length; i++) {
			if(arr[i] == number)
				rank++;
		}
		return rank;
	}
	
	/**
	 * Retrieves the tied rank (maximum method) of a certain number inside an array.
	 * 
	 * @param arr the arr
	 * @param number the number
	 * 
	 * @return the tied rank - returns 0 if number not found in array!
	 */
	public static int getTiedRank(double[] arr, double number) {
		int result = 0;
		
		int[] ranking = new int[arr.length];
		Set<Double> set = new TreeSet<Double>();
		Map<Double, Integer> ranks = new HashMap<Double, Integer>();
		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i]);
			if(ranks.containsKey(arr[i])) {
				continue;
			}
			int rank = getRank(arr, arr[i]);
			ranks.put(arr[i], rank);
		}
		
		set = ranks.keySet();	// all keys from ranks
		List<Double> list = new ArrayList<Double>();
		list.addAll(set);		// add all keys from set to list
		Collections.sort(list);	// sort list ascending
		Collections.reverse(list);	// reverse list order == descending
		
		int sum = 0;
		int count = 1;
		int prev = 0;
		// tied.ranking with maximum ties.method
		for (Double d : list) {
			count = ranks.get(d);
			for (int i = prev; i < count + prev; i++) {
				ranking[i] = count + prev;
			}
			if(d == number)
				return ranking[prev];
			sum += ranks.get(d);
			prev = sum;
		}
		
		return result;
	}
	
	public static int[] order(double[] arr, Order order) {
		int[] result = new int[arr.length];
		List<Double> temp = new ArrayList<Double>();
		List<Double> temp_orig = new ArrayList<Double>();
		//Map<Integer, Double> pos_orig = new HashMap<Integer, Double>();
		//Map<Integer, Double> pos_new = new HashMap<Integer, Double>();
		
		for (int i = 0; i < arr.length; i++) {
			temp.add(arr[i]);
			temp_orig.add(arr[i]);
			//pos_orig.put(i, arr[i]);
		}
		
		Collections.sort(temp);
		Collections.reverse(temp);
		for (int i = 0; i < temp.size(); i++) {
			Double d = temp.get(i);
			int pos = temp_orig.indexOf(d);
			temp_orig.set(pos, -1000d);
			pos += 1;		// change index position to mimic R behaviour
			//pos_new.put(pos, d);
			result[i] = pos;
		}
		return result;
	}
}
