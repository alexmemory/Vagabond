package org.vagabond.test.explanation.ranking;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.Comparator;

import org.junit.Test;
import org.vagabond.commandline.explgen.ExplGenOptions;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.ranking.scoring.EntropyScore;
import org.vagabond.explanation.ranking.scoring.ExplanationSizeScore;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.explanation.ranking.scoring.ScoreExplSetComparator;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.explanation.ranking.scoring.WeightedCombinedWMScoring;
import org.vagabond.test.AbstractVagabondTest;

public class TestSideEffectSizeFunctions extends AbstractVagabondTest {
	
	private ExplanationSetGenerator explSetGen = new ExplanationSetGenerator();

	
	private void setUp (String filename) throws Exception {
		loadToDB(filename);
	}
	
	@Test
	public void testSideEffectSize () throws Exception {
		IScoringFunction f = new  SideEffectSizeScore();
		Comparator<IExplanationSet> comp = new ScoreExplSetComparator(f);
		
		setUp ("/home/zwork/git/wangzhenmasterthesis/thesis/tests/data/set5/test.xml");
		
		IMarkerSet mkset = MarkerParser.getInstance().parseMarkers(
				new FileInputStream("/home/zwork/git/wangzhenmasterthesis/thesis/tests/data/set5/data_5000_err_200.txt"));
		
		ExplanationCollection col;
		ExplanationSetGenerator gen = new ExplanationSetGenerator();
		col = gen.findExplanations(mkset);
		
		int score = 0;
		for (IExplanationSet explset:col.getExplSets())
		{
			score = score + f.getScore(explset);
		}

		System.out.println("score is:"+ score);
	}

	
}
