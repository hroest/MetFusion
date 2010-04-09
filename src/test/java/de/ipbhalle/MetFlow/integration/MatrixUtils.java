/**
 * created by Michael Gerlich on Apr 7, 2010
 * last modified Apr 7, 2010 - 2:45:15 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class MatrixUtils {

	public static enum Ways {
		GREATER, LESS
	}
	
	public static enum Order {
		ASCENDING, DESCENDING
	}
	
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
					else break;	// break as no more fitting elements are encountered
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
					else break;	// break as no more fitting elements are encountered
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
	
	private static int getRank(double[] arr, double number) {
		int rank = 0;
		for (int i = 0; i < arr.length; i++) {
			if(arr[i] == number)
				rank++;
		}
		return rank;
	}
	
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
