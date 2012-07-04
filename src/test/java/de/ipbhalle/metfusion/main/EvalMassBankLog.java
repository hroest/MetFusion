/**
 * created by Michael Gerlich, Jun 18, 2012 - 3:57:42 PM
 */ 

package de.ipbhalle.metfusion.main;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.awt.X11.Screen;

import de.ipbhalle.io.FileNameFilterImpl;
import de.ipbhalle.metfusion.wrapper.Result;

public class EvalMassBankLog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String evalDir = "/home/mgerlich/evaluation/MetFusion/allSpectra/results/2010-11-26_10-50-42_ESIMS2_withCorrect_MSBI_MassBank/";
		FilenameFilter filterLog = new FileNameFilterImpl("", ".log", "_result");
		// log files
		File dirLog = new File(evalDir);
		File[] files = dirLog.listFiles(filterLog);
		System.out.println("#" + files.length);
		
		List<EvalLogShort> logs = new ArrayList<EvalLogShort>();
		List<Float> avgScores50to100 = new ArrayList<Float>();
		List<Float> scores100 = new ArrayList<Float>();
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getName());
			EvalLogShort log = new EvalLogShort(files[i]);
			List<Result> primaries = log.getPrimaries();
			if(primaries.size() <= 50) {
				avgScores50to100.add(0f);
				scores100.add(0f);
			}
			else {
				List<Float> scores = new ArrayList<Float>();
				for (int j = 50; j < primaries.size(); j++) {
					scores.add((float) primaries.get(j).getScore());
				}
				EvalMassBankScores embs = new EvalMassBankScores(scores);
				float avg = embs.getAvg50to100();
				System.out.println(avg);
				avgScores50to100.add(avg);
				
				scores100.add(embs.getHit100());
			}
		}
		
		float avg = 0.0f;
		int countLarger30 = 0;
		float min = 1.0f;
		float max = 0.0f;
		List<Float> larger = new ArrayList<Float>();
		for (Float f : avgScores50to100) {
			avg += f.floatValue();
			if(f.floatValue() > 0.3f) {
				countLarger30++;
				larger.add(f);
				if(f < min)
					min = f;
				if(f > max)
					max = f;
			}
		}

		avg = avg / (float) avgScores50to100.size();
		System.out.println("#avg scores " + avgScores50to100.size() + " -> == " + avg);
		System.out.println("#larger 0.3 -> " + countLarger30 + " -> min = " + min + " -> max = " + max);
		
		// top 100
		min = 1.0f;
		max = 0.0f;
		larger = new ArrayList<Float>();
		int countLarger100 = 0;
		float avg100 = 0.0f;
		int count40 = 0;
		for (Float f : scores100) {
			avg100 += f.floatValue();
			if(f.floatValue() > 0f) {
				countLarger100++;
				larger.add(f);
				if(f < min)
					min = f;
				if(f > max)
					max = f;
			}
			
			if(f.floatValue() > 0.4f)
				count40++;
		}
		
		avg100 = avg100 / (float) scores100.size();
		System.out.println("#avg100 scores " + scores100.size() + " -> == " + avg100);
		System.out.println("#larger 0.0 -> " + countLarger100 + " -> min = " + min + " -> max = " + max);
		System.out.println("#larger 0.4 -> " + count40);
	}

}
