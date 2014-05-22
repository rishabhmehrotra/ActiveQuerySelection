/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.jforests.eval.ranking;

import java.util.Arrays;


import edu.uci.jforests.dataset.RankingDataset;
import edu.uci.jforests.sample.RankingSample;
import edu.uci.jforests.sample.Sample;
import edu.uci.jforests.util.ArraysUtil;
import edu.uci.jforests.util.Constants;
import edu.uci.jforests.util.ScoreBasedComparator;
import edu.uci.jforests.util.ScoreBasedComparator.TieBreaker;
import edu.uci.jforests.util.concurrency.BlockingThreadPoolExecutor;
import edu.uci.jforests.util.concurrency.TaskCollection;
import edu.uci.jforests.util.concurrency.TaskItem;

/**
 * Normalized Discounted Cumulative Gain (NDCG)
 */

/**
 * @author Yasser Ganjisaffar <ganjisaffar at gmail dot com>
 */

public class NDCGEval extends RankingEvaluationMetric {

	public static final int MAX_TRUNCATION_LEVEL = 10;
	public static final int GAIN_LEVELS = 5;
	public static double[] GAINS = new double[] { 0, 1, 3, 7, 15 };
	public static double[] discounts;

	private TaskCollection<PerQueryNDCGWorker> ndcgWorkers;
	private int evalTruncationLevel;
	private int maxDocsPerQuery;

	public static synchronized void initialize(int maxDocsPerQuery) {
		if (discounts == null || discounts.length < maxDocsPerQuery) {
			discounts = new double[maxDocsPerQuery];
			for (int p = 0; p < maxDocsPerQuery; p++) {
				discounts[p] = Constants.LN2 / Math.log(2 + p);
			}	
		}		
	}

	public NDCGEval(int maxDocsPerQuery, int evalTruncationLevel) throws Exception {
		super(true);
		this.maxDocsPerQuery = maxDocsPerQuery;
		initialize(maxDocsPerQuery);
		if (evalTruncationLevel > MAX_TRUNCATION_LEVEL) {
			throw new Exception("Evalutation truncation level " + evalTruncationLevel + " is larger than "
					+ MAX_TRUNCATION_LEVEL);
		}
		this.evalTruncationLevel = evalTruncationLevel;
		int numWorkers = BlockingThreadPoolExecutor.getInstance().getMaximumPoolSize();
		ndcgWorkers = new TaskCollection<NDCGEval.PerQueryNDCGWorker>();
		for (int i = 0; i < numWorkers; i++) {
			ndcgWorkers.addTask(new PerQueryNDCGWorker());
		}
	}

	public static int[][] getLabelCountsForQueries(double[] labels, int[] boundaries) {
		int numQueries = boundaries.length - 1;
		int[][] labelCounts = new int[numQueries][GAIN_LEVELS];
		for (int q = 0; q < numQueries; q++) {
			int begin = boundaries[q];
			int end = boundaries[q + 1];
			for (int i = begin; i < end; i++) {
				if ((int) labels[i] >= GAIN_LEVELS)
				{
					System.err.println("query " + q + " line "+i+" label " + ((int) labels[i]));
					labels[i] = GAIN_LEVELS-1; 
				}
				labelCounts[q][(int) labels[i]]++;
			}
		}
		return labelCounts;
	}

	/*
	 * Calculates Maximum DCG for all queries up to the given truncation
	 */
	public static double[][] getMaxDCGForAllQueriesUptoTruncation(double[] labels, int[] boundaries, int trunc,
			int[][] labelCounts) throws Exception {
		double[][] maxDCG = new double[trunc][];
		for (int t = 1; t <= trunc; t++) {
			maxDCG[t - 1] = getMaxDCGForAllQueriesAtTruncation(labels, boundaries, t, labelCounts);
		}
		return maxDCG;
	}

	/*
	 * Calculates Maximum DCG for all queries at the given truncation
	 */
	public static double[] getMaxDCGForAllQueriesAtTruncation(double[] labels, int[] boundaries, int trunc,
			int[][] labelCounts) throws Exception {
		if (discounts == null) {
			throw new Exception("Not initialized.");
		}
		double[] maxDCG = new double[boundaries.length - 1];

		int[] tempCounts = new int[GAIN_LEVELS];
		for (int q = 0; q < boundaries.length - 1; q++) {
			int maxTrunc = Math.min(trunc, boundaries[q + 1] - boundaries[q]);

			int topLabel = GAIN_LEVELS - 1;
			maxDCG[q] = 0;
			System.arraycopy(labelCounts[q], 0, tempCounts, 0, GAIN_LEVELS);
			for (int t = 0; t < maxTrunc; t++) {
				while (tempCounts[topLabel] == 0 && topLabel > 0) {
					topLabel--;
				}
				maxDCG[q] += GAINS[topLabel] * discounts[t];
				tempCounts[topLabel]--;
			}
		}

		return maxDCG;
	}
	
	static final class NDCGSwapScorer extends RankingEvaluationMetric.SwapScorer {
		
		final double[] maxDCG;
		final int[] labels;
		
		NDCGSwapScorer (double[] targets, int[] boundaries, int trunc, int[][] labelCounts) throws Exception
		{
			super(targets, boundaries, trunc, labelCounts);
			maxDCG = NDCGEval.getMaxDCGForAllQueriesAtTruncation(targets, boundaries, trunc, labelCounts);
			this.labels = new int[targets.length];
			for(int i=0;i<targets.length;i++)
			{
				labels[i] = (int) targets[i];
			}
			assert NDCGEval.discounts != null;
		}
		
		@Override
		public final double getDelta(int queryIndex, int betterIdx, int rank_i, int worseIdx, int rank_j)
		{			
			assert betterIdx < labels.length;
			assert worseIdx < labels.length;
			
			final double queryMaxDcg = maxDCG[queryIndex];
			return (NDCGEval.GAINS[labels[betterIdx]] - NDCGEval.GAINS[labels[worseIdx]])
					//* Math.abs((NDCGEval.discounts[rank_i] - NDCGEval.discounts[rank_j])) / queryMaxDcg;
					* ((NDCGEval.discounts[rank_j] - NDCGEval.discounts[rank_i])) / queryMaxDcg;
		}
		
	}
	

	private class PerQueryNDCGWorker extends NDCGWorker {
		private double[][] queryresult;
		
		public double[][] getQueryResults() {
			return queryresult;
		}
		
		public PerQueryNDCGWorker() {
			super();
		}
		
		@Override
		public void init(RankingSample sample, double[] scores, int beginIdx,
				int endIdx, TieBreaker tieBreaker) {
			super.init(sample, scores, beginIdx, endIdx, tieBreaker);
			queryresult = new double[MAX_TRUNCATION_LEVEL][endIdx - beginIdx];
		}
		
		@Override
		public void run() {

			int qid=0;
			for (int q = beginIdx; q < endIdx; q++) {
				int begin = sample.queryBoundaries[q];
				int numDocs = sample.queryBoundaries[q + 1] - begin;

				double[][] maxDCG = ((RankingDataset) (sample.dataset)).maxDCG;

				comparator.offset = begin;

				for (int d = 0; d < numDocs; d++) {
					permutation[d] = d;
				}				
				ArraysUtil.sort(permutation, numDocs, comparator);

				if (numDocs > MAX_TRUNCATION_LEVEL) {
					numDocs = MAX_TRUNCATION_LEVEL;
				}
				try {
					double dcg = 0;
					if (maxDCG[0][sample.queryIndices[q]] == 0) {
						for (int t = 0; t < MAX_TRUNCATION_LEVEL; t++) {
							result[t] += 1;
							queryresult[t][qid] += 1;
						}
					} else {
						for (int t = 0; t < numDocs; t++) {
							dcg += GAINS[(int) (sample.targets[begin + permutation[t]])] * discounts[t];
							if (dcg > 0) {
								result[t] += dcg / maxDCG[t][sample.queryIndices[q]];
								queryresult[t][qid] += dcg / maxDCG[t][sample.queryIndices[q]];
							}
						}
						if (dcg > 0) {
							for (int t = numDocs; t < MAX_TRUNCATION_LEVEL; t++) {
								result[t] += dcg / maxDCG[t][sample.queryIndices[q]];
								queryresult[t][qid] += dcg / maxDCG[t][sample.queryIndices[q]];
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				qid++;
			}

		}
		
		
		
	}
	
	private class NDCGWorker extends TaskItem {

		protected int[] permutation;
		protected RankingSample sample;
		protected int beginIdx;
		protected int endIdx;
		protected double[] result;
		
		protected ScoreBasedComparator comparator;

		public NDCGWorker() {
			permutation = new int[maxDocsPerQuery];
			comparator = new ScoreBasedComparator();
			result = new double[MAX_TRUNCATION_LEVEL];
		}

		public void init(RankingSample sample, double[] scores, int beginIdx, int endIdx, TieBreaker tieBreaker) {
			this.sample = sample;
			this.beginIdx = beginIdx;
			this.endIdx = endIdx;
			comparator.labels = sample.targets;
			comparator.scores = scores;
			comparator.tieBreaker = tieBreaker;
			Arrays.fill(result, 0);	
		}

		public double[] getResults() {
			return result;
		}	
		

		public void run() {

			for (int q = beginIdx; q < endIdx; q++) {
				int begin = sample.queryBoundaries[q];
				int numDocs = sample.queryBoundaries[q + 1] - begin;

				double[][] maxDCG = ((RankingDataset) (sample.dataset)).maxDCG;

				comparator.offset = begin;

				for (int d = 0; d < numDocs; d++) {
					permutation[d] = d;
				}				
				ArraysUtil.sort(permutation, numDocs, comparator);

				if (numDocs > MAX_TRUNCATION_LEVEL) {
					numDocs = MAX_TRUNCATION_LEVEL;
				}
				try {
					double dcg = 0;
					if (maxDCG[0][sample.queryIndices[q]] == 0) {
						for (int t = 0; t < MAX_TRUNCATION_LEVEL; t++) {
							result[t] += 1;
						}
					} else {
						for (int t = 0; t < numDocs; t++) {
							dcg += GAINS[(int) (sample.targets[begin + permutation[t]])] * discounts[t];
							if (dcg > 0) {
								result[t] += dcg / maxDCG[t][sample.queryIndices[q]];
							}
						}
						if (dcg > 0) {
							for (int t = numDocs; t < MAX_TRUNCATION_LEVEL; t++) {
								result[t] += dcg / maxDCG[t][sample.queryIndices[q]];
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	public double[] getNDCGatAllTruncations(double[] predictions, Sample sample, TieBreaker tieBreaker) throws Exception {
		if (((RankingDataset) (sample.dataset)).maxDCG == null) {
			throw new Exception("maxDCG is not initialized for dataset.");
		}
		RankingSample rankingSample = (RankingSample) sample;

		int chunkSize = 1 + (rankingSample.numQueries / ndcgWorkers.getSize());
		int offset = 0;
		int workerCount = 0;
		for (int i = 0; i < ndcgWorkers.getSize() && offset < rankingSample.numQueries; i++) {
			int endOffset = offset + Math.min(rankingSample.numQueries - offset, chunkSize);
			NDCGWorker worker = ndcgWorkers.getTask(i);
			workerCount++;
			worker.init(rankingSample, predictions, offset, endOffset, tieBreaker);
			BlockingThreadPoolExecutor.getInstance().execute(worker);
			offset += chunkSize;
		}
		BlockingThreadPoolExecutor.getInstance().await();

		double[] result = new double[MAX_TRUNCATION_LEVEL];
		for (int i = 0; i < workerCount; i++) {
			double[] localResult = ndcgWorkers.getTask(i).getResults();
			for (int t = 0; t < MAX_TRUNCATION_LEVEL; t++) {
				result[t] += localResult[t];
			}
		}

		for (int t = 0; t < MAX_TRUNCATION_LEVEL; t++) {
			result[t] /= rankingSample.numQueries;
		}
		return result;
	}
	
	public double[][] getNDCGatAllTruncationsAllQueries(double[] predictions, Sample sample, TieBreaker tieBreaker) throws Exception {
		if (((RankingDataset) (sample.dataset)).maxDCG == null) {
			throw new Exception("maxDCG is not initialized for dataset.");
		}
		RankingSample rankingSample = (RankingSample) sample;

		int chunkSize = 1 + (rankingSample.numQueries / ndcgWorkers.getSize());
		int offset = 0;
		int workerCount = 0;
		for (int i = 0; i < ndcgWorkers.getSize() && offset < rankingSample.numQueries; i++) {
			int endOffset = offset + Math.min(rankingSample.numQueries - offset, chunkSize);
			NDCGWorker worker = ndcgWorkers.getTask(i);
			workerCount++;
			worker.init(rankingSample, predictions, offset, endOffset, tieBreaker);
			BlockingThreadPoolExecutor.getInstance().execute(worker);
			offset += chunkSize;
		}
		BlockingThreadPoolExecutor.getInstance().await();

		double[][] result = new double[MAX_TRUNCATION_LEVEL][rankingSample.numQueries];
		offset = 0;
		for (int i = 0; i < workerCount; i++) {
			double[][] localResult = ndcgWorkers.getTask(i).getQueryResults();
			
			for (int t = 0; t < MAX_TRUNCATION_LEVEL; t++) {
				System.arraycopy(localResult[t], 0, result[t], offset, localResult[t].length);
			}
			offset += localResult[0].length;
		}
		assert offset == rankingSample.numQueries;
		return result;
	}
	
	
	@Override
	public double measure(double[] predictions, Sample sample) throws Exception {
		return getNDCGatAllTruncations(predictions, sample, TieBreaker.ReverseLabels)[evalTruncationLevel - 1];
	}

	@Override
	public double[] measureByQuery(double[] predictions, Sample sample)
			throws Exception {
		return getNDCGatAllTruncationsAllQueries(predictions, sample, TieBreaker.ReverseLabels)[evalTruncationLevel - 1];
	}

	@Override
	public SwapScorer getSwapScorer(double[] targets, int[] boundaries,
			int trunc, int[][] labelCounts) throws Exception {
		return new NDCGSwapScorer(targets, boundaries, trunc, labelCounts);
	}
}
