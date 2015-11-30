/*
 * Copyright 2015 Otto (GmbH & Co KG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.batch;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.core.input.Input;
import org.apache.flink.core.table.HamcrestVerifier;
import org.apache.flink.core.table.OutputMatcherFactory;
import org.apache.flink.core.runtime.OutputVerifier;
import org.apache.flink.streaming.DataStreamTestEnvironment;
import org.apache.flink.core.trigger.VerifyFinishedTrigger;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;

import java.util.Collection;

public class TestBase {

	/**
	 * Test Environment
	 */
	private DataSetTestEnvironment env;

	/**
	 * Creates a new {@link DataStreamTestEnvironment}
	 */
	@Before
	public void initialize() throws Exception {
		env = DataSetTestEnvironment.createTestEnvironment(2);
	}


	/**
	 * Creates a DataSet from a {@link Collection}.
	 *
	 * @param input to emit
	 * @param <OUT> type of the emitted records
	 * @return a DataSet containing the input
	 *
	 */
	public <OUT> DataSet<OUT> createTestDataSet(Collection<OUT> input) {
		return env.fromCollection(input);
	}

	/**
	 * Creates a DataSet from an Input object.
	 *
	 * @param input to emit
	 * @param <OUT> type of the emitted records
	 * @return a DataSet containing the input
	 */
	public <OUT> DataSet<OUT> createTestDataSet(Input<OUT> input) {
		return env.fromCollection(input.getInput());
	}

	/**
	 * Creates a TestOutputFormat using {@link org.hamcrest.Matcher} to verify the output.
	 *
	 * @param matcher of type Iterable<IN>
	 * @param <IN>
	 * @return the created sink.
	 */
	public <IN> TestOutputFormat<IN> createTestOutputFormat(org.hamcrest.Matcher<Iterable<IN>> matcher) {
		OutputVerifier<IN> verifier = new HamcrestVerifier<IN>(matcher);
		return env.createTestOutputFormat(verifier);
	}

	/**
	 * Creates a TestOutputFormat using {@link org.hamcrest.Matcher} to verify the output.
	 *
	 * @param matcher of type Iterable<IN>
	 * @param <IN>
	 * @return the created sink.
	 */
	public <IN> TestOutputFormat<IN> createTestOutputFormat(org.hamcrest.Matcher<Iterable<IN>> matcher,
	                                        VerifyFinishedTrigger trigger) {
		OutputVerifier<IN> verifier = new HamcrestVerifier<>(matcher);
		return env.createTestOutputFormat(verifier,trigger);
	}

	/**
	 * Inspect a {@link DataSet} using a {@link OutputMatcherFactory}.
	 *
	 * @param dataSet  {@link DataSet} to test.
	 * @param matcher {@link OutputMatcherFactory} to use.
	 * @param <T>     type of the dataSet.
	 */
	public <T> void assertDataSet(DataSet<T> dataSet, Matcher<Iterable<T>> matcher) {
		dataSet.output(createTestOutputFormat(matcher));
	}

	/**
	 * Inspect a {@link DataSet} using a {@link OutputMatcherFactory}.
	 *
	 * @param dataSet  {@link DataSet} to test.
	 * @param matcher {@link OutputMatcherFactory} to use.
	 * @param trigger {@link VerifyFinishedTrigger}
	 *                to finish the assertion early.
	 * @param <T>     type of the dataSet.
	 */
	public <T> void assertDataSet(DataSet<T> dataSet,
	                             Matcher<Iterable<T>> matcher,
	                             VerifyFinishedTrigger trigger) {
		dataSet.output(createTestOutputFormat(matcher, trigger));
	}

	public void setParallelism(int n) {
		env.setParallelism(n);
	}

	/**
	 * Executes the test and verifies the output received.
	 */
	@After
	public void executeTest() throws Throwable {
		try {
			env.executeTest();
		} catch (AssertionError assertionError) {
			if (env.hasBeenStopped()) {
				//the execution has been forcefully stopped inform the user!
				throw new AssertionError("Test terminated due timeout!" +
						assertionError.getMessage());
			}
			throw assertionError;
		}
	}
}
